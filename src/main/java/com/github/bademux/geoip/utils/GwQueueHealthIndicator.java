package com.github.bademux.geoip.utils;

import com.github.bademux.geoip.country.IpCheckerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("ipCheckerClientHealth")
@RequiredArgsConstructor
public class GwQueueHealthIndicator implements HealthIndicator {

    private final IpCheckerClient ipCheckerClient;

    @Override
    public Health health() {
        return (ipCheckerClient.healthcheck() ? Health.up() : Health.down()).build();
    }
}
