package com.github.bademux.geoip.country;

import lombok.RequiredArgsConstructor;

import java.net.Inet4Address;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class CountryService {

    private final IpCheckerClient ipCheckerClient;
    private final CountriesProvider countriesProvider;

    public Set<String> getNorthcountriesForIps(Set<Inet4Address> ips) {
        Set<String> allowedCountries = countriesProvider.getNorthernHemisphereCountries();
        Set<String> allCountryList = ipCheckerClient.fetchCountryFor(ips);
        return allCountryList.stream()
                .filter(allowedCountries::contains)
                .collect(toSet());
    }

}
