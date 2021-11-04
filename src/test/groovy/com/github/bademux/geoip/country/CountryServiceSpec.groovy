package com.github.bademux.geoip.country

import spock.lang.Specification

import java.lang.Void as Should

class CountryServiceSpec extends Specification {

    Should "Get Northern hemisphere countries ForIps"() {

        given:
        var addresses = ipsForCountres.keySet().collect(Inet4Address.&getByName) as Set<Inet4Address>
        var ipCheckerClient = Mock(IpCheckerClient) {
            1 * fetchCountryFor(addresses) >> (ipsForCountres.values() as Set<String>)
        }
        var countriesProvider = Mock(CountriesProvider) {
            1 * getNorthernHemisphereCountries() >> whiteList
        }
        var countryService = new CountryService(ipCheckerClient, countriesProvider)

        when:
        var result = countryService.getNorthcountriesForIps(addresses)

        then:
        result == expectedCountries as Set<String>

        where:
        whiteList             | ipsForCountres                                  || expectedCountries
        ['Belarus']           | [:]                                             || []
        ['Belarus']           | ['127.0.0.1': 'Belarus', '127.0.0.2': 'Poland'] || ['Belarus']
        ['Belarus', 'Poland'] | ['127.0.0.1': 'Belarus', '127.0.0.2': 'Poland'] || ['Belarus', 'Poland']

    }

}
