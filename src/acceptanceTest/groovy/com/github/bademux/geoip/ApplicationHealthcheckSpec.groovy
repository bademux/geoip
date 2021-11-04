package com.github.bademux.geoip

import groovy.json.JsonOutput
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import utils.ApplicationManager

import java.lang.Void as Should
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ApplicationHealthcheckSpec extends Specification {

    @Unroll
    Should 'run healthcheck "#liveEndpoint" request'() {
        given:
        def request = HttpRequest.newBuilder(app.getAdminHttpAddress(liveEndpoint)).build()
        when:
        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        then:
        response.statusCode() == 200
        where:
        liveEndpoint << ['actuator/health/livenessState', 'actuator/health/readinessState']
    }

    static final HttpClient client = HttpClient.newHttpClient()

    @Shared
    @AutoCleanup
    ApplicationManager app = new ApplicationManager()
            .withEnv('SPRING_APPLICATION_JSON', JsonOutput.toJson([
                    'debug': true,
            ]))
            .with(true, ApplicationManager::start)

}
