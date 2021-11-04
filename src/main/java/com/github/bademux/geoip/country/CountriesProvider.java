package com.github.bademux.geoip.country;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public class CountriesProvider {

    private final Set<String> northernHemisphereCountries;

}
