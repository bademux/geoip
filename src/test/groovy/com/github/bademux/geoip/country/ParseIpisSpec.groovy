package com.github.bademux.geoip.country

import spock.lang.Specification

import java.lang.Void as Should

class ParseIpisSpec extends Specification {

    Should "parse ips ok"() {

        when:
        var result = Utils.parseIps(ips)

        then:
        result == expectedIps.collect(Inet4Address.&getByName) as Set<Inet4Address>

        where:
        ips           || expectedIps
        null          || []
        [null]        || []
        []            || []
        ['127.0.0.1'] || ['127.0.0.1']

    }

    Should "parse ips fails"() {
        given:
        var ips = ['b.a.d.ip']

        when:
        Utils.parseIps(ips)

        then:
        thrown(UnknownHostException)

    }
}
