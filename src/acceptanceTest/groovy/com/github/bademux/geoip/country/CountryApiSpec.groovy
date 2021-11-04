package com.github.bademux.geoip.country

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.testcontainers.Testcontainers
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import utils.ApplicationManager
import utils.RecordingWireMock

import java.lang.Void as Should
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

class CountryApiSpec extends Specification {

    Should 'check ips for northern hemisphere for Belarus Poland and Singapore'() {
        given:
        def request = HttpRequest.newBuilder(app.getHttpAddress('/api/v1/northcountries?ip=80.94.224.0&ip=31.60.0.0&ip=103.4.97.94')).build()

        when:
        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        then:
        verifyAll(response) {
            statusCode() == 200
            json.parseText(body()).northcountries.sort() == ['Belarus', 'Poland']
        }
    }

    static final HttpClient client = HttpClient.newHttpClient()
    static final JsonSlurper json = new JsonSlurper()

    @Shared
    @AutoCleanup(value = 'stop')
    WireMockServer ipCheckApi = new WireMockServer(wireMockConfig()
            .withRootDirectory("src/acceptanceTest/resources/${this.class.simpleName}")
            .notifier(new Slf4jNotifier(true))
            .dynamicPort())
            .with(true, WireMockServer::start)

    @AutoCleanup(value = 'stopRecordingIfNeeded')
    RecordingWireMock recordingWireMock = new RecordingWireMock(ipCheckApi, 'http://api.ipstack.com/').start()

    @Shared
    @AutoCleanup
    ApplicationManager app = new ApplicationManager()
            .withEnv('SPRING_APPLICATION_JSON', JsonOutput.toJson([
                    'debug'                : true,
                    'app.ipstack.baseurl'  : "http://host.testcontainers.internal:${ipCheckApi.port()}",
                    'app.ipstack.accessKey': 'ACCESS_KEY',
            ]))
            .with(true, {
                Testcontainers.exposeHostPorts(ipCheckApi.port())
                start()
            })

}
