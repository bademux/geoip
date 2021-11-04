package com.github.bademux.geoip.country;

import com.github.bademux.geoip.api.NorthcountriesV1Api;
import com.github.bademux.geoip.api.dto.NorthcountriesApiDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.github.bademux.geoip.country.Utils.parseIps;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class CountryController implements NorthcountriesV1Api {

    private final CountryService countryService;

    @Override
    public ResponseEntity<NorthcountriesApiDto> getNorthcountriesForIps(List<Object> ip) {
        var result = countryService.getNorthcountriesForIps(parseIps(ip));
        return ResponseEntity.ok(new NorthcountriesApiDto().northcountries(new ArrayList<>(result)));
    }

}
