package com.github.bademux.geoip.country;

import java.net.Inet4Address;
import java.util.Set;

public interface IpCheckerClient {
    /**
     * TODO: think about cache based on usage pattern
     *
     * @param ips to be queried
     * @return unique list of countries, elements are non null
     */
    Set<String> fetchCountryFor(Set<Inet4Address> ips);

    boolean healthcheck();

}

