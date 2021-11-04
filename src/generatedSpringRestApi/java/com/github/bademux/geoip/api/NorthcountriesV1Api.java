/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (5.3.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.github.bademux.geoip.api;

import com.github.bademux.geoip.api.dto.ErrorApiDto;
import com.github.bademux.geoip.api.dto.NorthcountriesApiDto;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
@Validated
@Api(value = "NorthcountriesV1", description = "the NorthcountriesV1 API")
public interface NorthcountriesV1Api {

    /**
     * GET /v1/northcountries : Returns list of countries from the northern hemisphere for the IPs
     *
     * @param ip IPs that should be checked (required)
     * @return OK (status code 200)
     *         or error (status code 400)
     */
    @ApiOperation(value = "Returns list of countries from the northern hemisphere for the IPs", nickname = "getNorthcountriesForIps", notes = "", response = NorthcountriesApiDto.class, tags={ "northcountries-v1", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = NorthcountriesApiDto.class),
        @ApiResponse(code = 400, message = "error", response = ErrorApiDto.class) })
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/v1/northcountries",
        produces = { "application/json" }
    )
    ResponseEntity<NorthcountriesApiDto> getNorthcountriesForIps(@NotNull @Size(min = 1, max = 50) @ApiParam(value = "IPs that should be checked", required = true) @Valid @RequestParam(value = "ip", required = true) List<Object> ip);

}