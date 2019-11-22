package com.exadel.frs.controller;

import com.exadel.frs.exception.AppOrModelNotFoundException;
import com.exadel.frs.helpers.SecurityUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProxyController {

    private static final String API_KEY_HEADER = "X-Api-Key";
    private static final String APP_GUID_HEADER = "x-frs-app-key";
    private static final String MODEL_GUID_HEADER = "x-frs-model-key";

    private final SecurityUtils securityUtils;

    @Value("${proxy.baseUrl}")
    private String baseUrl;

    @RequestMapping(value = "/**")
    @ApiOperation(value = "Send request to core service")
    public ResponseEntity<String> proxy(
            @ApiParam(value = "GUID of application", required = true) @RequestHeader(APP_GUID_HEADER) String appGuid,
            @ApiParam(value = "GUID of model, to which application has access", required = true) @RequestHeader(MODEL_GUID_HEADER) String modelGuid,
            @ApiParam(value = "Headers that will be proxied to core service", required = true) @RequestHeader MultiValueMap<String, String> headers,
            @ApiParam(value = "String parameters that will be proxied to core service") @RequestParam(required = false) Map<String, String> params,
            @ApiParam(value = "Files that will be proxied to core service") @RequestParam(required = false) Map<String, MultipartFile> files,
            HttpServletRequest request) {
        if (!securityUtils.isAppHasAccessToModel(appGuid, modelGuid)) {
            throw new AppOrModelNotFoundException();
        }
        String url = request.getRequestURI().replaceFirst("/proxy", "");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);
        files.forEach((key, file) -> body.add(key, file.getResource()));
        headers.add(API_KEY_HEADER, modelGuid);
        headers.remove(APP_GUID_HEADER);
        headers.remove(MODEL_GUID_HEADER);
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.exchange(baseUrl + url,
                    HttpMethod.resolve(request.getMethod()),
                    new HttpEntity<>(body, headers),
                    String.class);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.BAD_REQUEST);
        }
    }

}
