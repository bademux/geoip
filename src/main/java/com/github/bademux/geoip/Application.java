package com.github.bademux.geoip;

import com.github.bademux.geoip.country.CountriesProvider;
import com.github.bademux.geoip.country.CountryService;
import com.github.bademux.geoip.country.IPStackClient;
import com.github.bademux.geoip.country.IpCheckerClient;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.springframework.boot.Banner.Mode.OFF;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@RequiredArgsConstructor
public class Application implements AutoCloseable {

    @Delegate(types = AutoCloseable.class)
    private final ConfigurableApplicationContext context;

    public static void main(String... args) {
        run(args);
    }

    public static Application run(String... args) {
        return new SpringApplicationBuilder(Application.class, Config.class)
                .bannerMode(OFF)
                .main(Application.class)
                .build()
                .run(args)
                .getBean(Application.class);
    }

    public int getPort() {
        return requireNonNull(context.getEnvironment().getProperty("local.server.port", int.class));
    }

    public int getAdminPort() {
        return requireNonNull(context.getEnvironment().getProperty("local.management.server.port", int.class, getPort()));
    }

    @Configuration
    public static class Config {

        @Bean
        public CountriesProvider countriesProvider(@Value("${app.countries.northernhemisphere}") List<String> northernHemisphereCountries) {
            return new CountriesProvider(Set.copyOf(northernHemisphereCountries));
        }

        @Bean
        public IpCheckerClient ipCheckerClient(@Value("${app.ipstack.baseurl}") URI baseUrl, @Value("${app.ipstack.accessKey}") String accessKey) {
            return new IPStackClient(baseUrl, accessKey);
        }

        @Bean
        public CountryService countryService(IpCheckerClient ipCheckerClient, CountriesProvider countriesProvider) {
            return new CountryService(ipCheckerClient, countriesProvider);
        }

        @Bean
        public Clock clock() {
            return Clock.systemUTC();
        }

    }

}
