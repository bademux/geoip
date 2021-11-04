package com.github.bademux.geoip.country;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.net.Inet4Address;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

@UtilityClass
class Utils {

    public static Set<Inet4Address> parseIps(List<Object> ip) {
        if (ip == null) {
            return Set.of();
        }
        return ip.stream()
                .filter(Objects::nonNull)
                .map(ipStr -> createInetAddress((String) ipStr))
                .collect(toUnmodifiableSet());
    }

    @SneakyThrows
    private static Inet4Address createInetAddress(String ip) {
        var address = Inet4Address.getByName(ip);
        if (!(address instanceof Inet4Address)) {
            throw new IllegalArgumentException("bad inet address" + address);
        }
        return (Inet4Address) address;
    }

}
