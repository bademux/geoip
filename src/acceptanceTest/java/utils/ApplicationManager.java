package utils;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ContainerPort;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.time.Duration.ofSeconds;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

public final class ApplicationManager<SELF extends ApplicationManager<SELF>> extends GenericContainer<SELF> {

    static {
        DockerHackfixRMISocketFactory.init();
    }

    private static final int debugPort = 5005;
    private static final int jmxPort = 6301;
    private static final int port = 8080;
    private static final int adminPort = 8090;
    private final Path destfile;

    public ApplicationManager() {
        this(512, Boolean.parseBoolean(System.getenv("DEBUG_CONTAINER")));
    }

    @SneakyThrows
    public ApplicationManager(long memoryInMb, boolean debugEnabled) {
        //see gradle.build/jib.to.image for more info about default name, override registry
        super(DockerImageName.parse(getDockerName()).asCompatibleSubstituteFor(getDockerName()));
        logger().info("Running with memory limit {}Mb", memoryInMb);
        //cmd.getHostConfig().withReadonlyRootfs(true) is not possible due to jacoco java agent
        withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withMemory(memoryInMb * 1024 * 1024).withMemorySwap(0L).withPublishAllPorts(true));
        withLogConsumer(outputFrame -> logger().info(outputFrame.getUtf8String().trim()));
        withStartupAttempts(3);
        waitingFor(Wait.forHttp("/actuator/health/readinessState").forPort(adminPort).withStartupTimeout(ofSeconds(60)));
        addExposedPort(jmxPort);
        var props = getJacocoPropsFromCmd();
        destfile = getDestFile(props.get("destfile"));
        var javaOptsJmx = Optional.ofNullable(props.get("javaagent"))
                .map(this::setupJacocoAgent)
                .map(s -> createJmxOpts(s, jmxPort))
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
        if (debugEnabled) {
            javaOptsJmx.add("-agentlib:jdwp=transport=dt_socket,server=n,suspend=y,address=host.testcontainers.internal:" + debugPort);
            Testcontainers.exposeHostPorts(debugPort);
        }
        withEnv("JAVA_TOOL_OPTIONS", String.join(" ", javaOptsJmx));
        withEnv("APP_LOG_APPENDER", "console-docker");
    }

    private static Collection<String> createJmxOpts(String targetAgentJar, int jmxPort) {
        return List.of("-Dcom.sun.management.jmxremote=true", "-Dcom.sun.management.jmxremote.local.only=false",
                "-Dcom.sun.management.jmxremote.port=" + jmxPort, "-Dcom.sun.management.jmxremote.rmi.port=" + jmxPort,
                "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
                "-Dsun.rmi.transport.tcp.responseTimeout=5000", "-Dsun.rmi.transport.tcp.handshakeTimeout=2000",
                "-javaagent:" + targetAgentJar + "=output=none,jmx=true");
    }

    private static String getDockerName() {
        return ofNullable(System.getenv("DOCKER_NAME")).orElseThrow();
    }

    private String setupJacocoAgent(String jacocoAgentPath) {
        String targetAgentJar = "/org.jacoco.agent.jar";
        MountableFile javaagent = ofNullable(jacocoAgentPath)
                .or(ApplicationManager::getJacocoAgent)
                .map(MountableFile::forHostPath)
                .orElseThrow(() -> new IllegalArgumentException("javaagent path to jar is mandatory"));
        logger().info("Writing jacoco coverage report with agent {}", javaagent.getFilesystemPath());
        withCopyFileToContainer(javaagent, targetAgentJar);
        return targetAgentJar;
    }

    private Path getDestFile(String destfile) {
        return Path.of(ofNullable(destfile)
                .or(() -> ofNullable(System.getProperty("com.github.bademux.geoip.jacoco.destfile")))
                .orElse("build/jacoco/report.exec"));
    }

    private static Map<String, String> getJacocoPropsFromCmd() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(s -> s.startsWith("-javaagent:"))
                .findFirst()
                .map(ApplicationManager::parseJacocoCmd)
                .orElseGet(Map::of);
    }

    @NotNull
    private static Map<String, String> parseJacocoCmd(String agentCmd) {
        var ret = new HashMap<String, String>(2);
        try {
            Scanner scanner = new Scanner(agentCmd).useDelimiter("=");
            String agent = scanner.next();
            scanner.skip("=");
            scanner.useDelimiter(",").tokens()
                    .filter(s -> s.startsWith("destfile="))
                    .findFirst()
                    .map(s -> s.substring(s.lastIndexOf('=') + 1))
                    .ifPresent(destfile -> ret.put("destfile", destfile));
            Optional.ofNullable(agent)
                    .map(s -> s.substring(s.indexOf(':') + 1))
                    .ifPresent(path -> ret.put("javaagent", path));
            return ret;
        } catch (Exception ignore) {
            return ret;
        }
    }

    private static Optional<String> getJacocoAgent() {
        return new Scanner(System.getProperty("java.class.path")).useDelimiter(":").tokens()
                .filter(Pattern.compile("org\\.jacoco\\.agent-\\d\\.\\d\\.\\d-runtime\\.jar$").asPredicate())
                .findAny();
    }

    @Override
    protected void containerIsStopping(InspectContainerResponse containerInfo) {
        try {
            writeJacocoExecData();
        } catch (Exception e) {
            logger().error("Can't write jacoco execution data due to error: '{}'", e.getMessage(), e);
        }
    }

    @SneakyThrows
    private void writeJacocoExecData() {
        var objectName = new ObjectName("org.jacoco:type=Runtime");
        var url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + getContainerIpAddress() + ':' + getMappedPort(jmxPort) + "/jmxrmi");
        logger().info("Connecting to jmx {}", url);
        try (var jmxc = JMXConnectorFactory.connect(url)) {
            var conn = jmxc.getMBeanServerConnection();
            var attributes = conn.getAttributes(objectName, new String[]{"Version", "SessionId"}).asList();
            logger().info("Dumping jacoco exec report: {} {}", attributes.toArray());
            var data = (byte[]) conn.invoke(objectName, "getExecutionData", new Object[]{false}, new String[]{boolean.class.toString()});
            Path uniqueReportFileName = attributes.stream()
                    .filter(attr -> "SessionId".equals(attr.getName()))
                    .findAny()
                    .map(Attribute::getValue)
                    .map(sessId -> sessId + "_" + destfile.getFileName().toString())
                    .map(Path::of)
                    .orElseThrow();
            Path file = Optional.ofNullable(destfile.getParent())
                    .map(parent -> parent.resolve(uniqueReportFileName))
                    .orElse(uniqueReportFileName);
            Files.write(file, data);
        }
    }

    @Override
    protected Logger logger() {
        return getLogger("app");
    }

    public URI getHttpAddress(String path) {
        return getAddress(port, path).normalize();
    }

    public URI getAdminHttpAddress(String path) {
        return getAddress(adminPort, path);
    }

    private URI getAddress(int mappedPort, String path) {
        return URI.create("http://" + getContainerIpAddress() + ':' + getMappedPort(mappedPort) + '/' + requireNonNull(path));
    }

}

/**
 * init asap with utils.DockerHackfixRMISocketFactory#init(), then target container run using env var
 * JAVA_TOOL_OPTIONS="-Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001"
 */
@Slf4j
final class DockerHackfixRMISocketFactory extends RMISocketFactory {

    @Delegate(types = RMIServerSocketFactory.class)
    private final RMISocketFactory socketFactory = RMISocketFactory.getDefaultSocketFactory();

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        DockerClientFactory docker = DockerClientFactory.instance();
        String dockerHostIpAddress = docker.dockerHostIpAddress();
        if (dockerHostIpAddress.equals(host)) {
            log.debug("Making initial connection to {}:{}", dockerHostIpAddress, port);
            return socketFactory.createSocket(dockerHostIpAddress, port);
        }
        log.debug("try to translate {}:{} that comes from the jmx server", host, port);
        int publicJmxPort = docker.client().listContainersCmd().exec().stream()
                .filter(container -> container.getNetworkSettings().getNetworks().values().stream().map(ContainerNetwork::getIpAddress).anyMatch(host::equals))
                .map(Container::getPorts)
                .flatMap(Stream::of)
                .filter(containerPort -> Objects.equals(containerPort.getPrivatePort(), port))
                .map(ContainerPort::getPublicPort)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No container found for " + host + ':' + port));
        log.debug("connecting to {}:{}", dockerHostIpAddress, publicJmxPort);
        return socketFactory.createSocket(dockerHostIpAddress, publicJmxPort);
    }

    @SneakyThrows
    public static void init() {
        RMISocketFactory.setSocketFactory(new DockerHackfixRMISocketFactory());
    }

}
