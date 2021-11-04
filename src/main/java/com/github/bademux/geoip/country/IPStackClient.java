package com.github.bademux.geoip.country;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @see <a href="https://ipstack.com/documentation">API doc</a>
 */
@Slf4j
@RequiredArgsConstructor
public final class IPStackClient implements IpCheckerClient, AutoCloseable {

    private final ObjectMapper objectMapper = new ObjectMapper();
    //TODO: externalize nThreads to config
    private final ExecutorService executorService = newFixedThreadPoolWithBockingCaller(20);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .executor(executorService)
            .build();
    private final String countryName = "country_name";
    private final URI baseUrl; //https://api.ipstack.com/
    private final String accessKey;

    CompletableFuture<HttpResponse<Callable<Map<String, String>>>> requestCountryFor(Inet4Address address) {
        var uri = URI.create(baseUrl.toString() + '/' + requireNonNull(address).getHostAddress() +
                "?output=json&fields=" + countryName + "&access_key=" + accessKey);
        return httpClient.sendAsync(HttpRequest.newBuilder(uri).build(), this::asMap);
    }

    @SuppressWarnings("unchecked")
    private BodySubscriber<Callable<Map<String, String>>> asMap(ResponseInfo responseInfo) {
        return BodySubscribers.mapping(BodySubscribers.ofInputStream(), is -> () -> {
            try (InputStream stream = is) {
                return objectMapper.readValue(stream, Map.class);
            }
        });
    }

    @Override
    public Set<String> fetchCountryFor(Set<Inet4Address> ips) {
        var completableFutures = ips.stream()
                .map(this::requestCountryFor)
                .collect(toList());
        CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new)).join();
        return completableFutures.stream()
                .map(CompletableFuture::join)
                .map(this::handleResponse)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    @Override
    public boolean healthcheck() {
        var request = HttpRequest.newBuilder(URI.create(baseUrl + "1.1.1.1?output=json&fields=ip&access_key=" + accessKey)).build();
        try {
            return httpClient.send(request, discarding()).statusCode() == 200;
        } catch (Throwable e) {
            log.warn("Something bad happen on healthcheck", e);
        }
        return false;
    }

    @SneakyThrows
    private String handleResponse(HttpResponse<Callable<Map<String, String>>> response) {
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Can't process response, bad response code, resp: " + response);
        }
        var body = response.body().call();
        var status = body.get("status");
        if (status != null && !"success".equals(status)) {
            throw new IllegalStateException(String.format("Can't process response bad service status '%s', resp: %s", status, response));
        }
        return body.get(countryName);
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    /**
     * @param nThreads -  the number of threads in the pool + current caller if more then expected
     */
    private ThreadPoolExecutor newFixedThreadPoolWithBockingCaller(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(nThreads), new ThreadPoolExecutor.CallerRunsPolicy());
    }

}
