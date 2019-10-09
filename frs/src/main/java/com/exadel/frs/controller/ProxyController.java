package com.exadel.frs.controller;

import com.exadel.frs.helpers.SecurityUtils;
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
@RequestMapping("/proxy")
@RequiredArgsConstructor
public class ProxyController {

    private static final String API_KEY_HEADER = "X-Api-Key";
    private static final String APP_GUID_HEADER = "app-guid";
    private static final String MODEL_GUID_HEADER = "model-guid";

    private final SecurityUtils securityUtils;

    @Value("${proxy.baseUrl}")
    private String baseUrl;

    @RequestMapping(value = "/**")
    public ResponseEntity<String> proxy(
            @RequestHeader(APP_GUID_HEADER) String appGuid,
            @RequestHeader(MODEL_GUID_HEADER) String modelGuid,
            @RequestHeader MultiValueMap<String, String> headers,
            @RequestParam(required = false) Map<String, String> params,
            @RequestParam(required = false) Map<String, MultipartFile> files,
            HttpServletRequest request) {
        if (!securityUtils.isAppHasAccessToModel(appGuid, modelGuid)) {
            throw new RuntimeException("App or model does not exists, or app do not have permission to this model");
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
