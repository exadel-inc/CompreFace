package com.exadel.frs.controller;

import com.exadel.frs.entity.Model;
import com.exadel.frs.enums.AppModelAccess;
import com.exadel.frs.exception.AccessDeniedException;
import com.exadel.frs.exception.AppOrModelNotFoundException;
import com.exadel.frs.repository.AppModelRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.validation.ImageExtensionValidator;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ProxyController.PREFIX)
@RequiredArgsConstructor
public class ProxyController {

    static final String PREFIX = "/api";
    private static final String X_FRS_API_KEY_HEADER = "x-frs-api-key";
    private static final String X_API_KEY_HEADER = "X-Api-Key";

    private final ModelRepository modelRepository;
    private final AppModelRepository appModelRepository;

    @Value("${proxy.baseUrl}")
    private String baseUrl;

    private static final List<UrlMethod> readOnlyApiMethods = List.of(
            new UrlMethod(HttpMethod.GET, "/faces"),
            new UrlMethod(HttpMethod.GET, "/retrain"),
            new UrlMethod(HttpMethod.GET, "/status"),
            new UrlMethod(HttpMethod.POST, "/recognize"));

    @Data
    @AllArgsConstructor
    private static class UrlMethod {
        private HttpMethod httpMethod;
        private String url;
    }

    private AppModelAccess getAppModelAccessType(String appApiKey, String modelApiKey) {
        Model model = modelRepository.findByApiKey(modelApiKey)
                .orElseThrow(AppOrModelNotFoundException::new);
        if (appApiKey.equals(model.getApp().getApiKey())) {
            return AppModelAccess.TRAIN;
        }
        return appModelRepository.findByAppApiKeyAndModelApiKey(appApiKey, modelApiKey)
                .orElseThrow(AppOrModelNotFoundException::new)
                .getAccessType();
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    @ApiOperation(value = "Send request to core service")
    public ResponseEntity<String> proxy(
            @ApiParam(value = "Api key of application and model", required = true) @RequestHeader(X_FRS_API_KEY_HEADER) String apiKey,
            @ApiParam(value = "Headers that will be proxied to core service", required = true) @RequestHeader MultiValueMap<String, String> headers,
            @ApiParam(value = "String parameters that will be proxied to core service") @RequestParam(required = false) Map<String, String> params,
            @ApiParam(value = "Files that will be proxied to core service") @RequestParam(required = false) Map<String, MultipartFile> files,
            HttpServletRequest request) {
        int apiKeyLength = apiKey.length() / 2;
        String appApiKey = apiKey.substring(0, apiKeyLength);
        String modelApiKey = apiKey.substring(apiKeyLength);
        if (AppModelAccess.READONLY == getAppModelAccessType(appApiKey, modelApiKey)) {
            if (readOnlyApiMethods.stream()
                    .noneMatch(urlMethod -> request.getRequestURI().startsWith(PREFIX + urlMethod.getUrl())
                            && request.getMethod().equals(urlMethod.getHttpMethod().toString()))) {
                throw new AccessDeniedException();
            }
        }

        new ImageExtensionValidator().validate(files.values());

        val remoteUrl = baseUrl + request.getRequestURI().replaceFirst(PREFIX, "");
        val body = new LinkedMultiValueMap<String, Object>();

        params.forEach(body::add);
        files.forEach((key, file) -> body.add(key, file.getResource()));
        headers.remove(X_FRS_API_KEY_HEADER);
        headers.add(X_API_KEY_HEADER, apiKey);
        try {
            return new RestTemplate().exchange(remoteUrl, HttpMethod.resolve(request.getMethod()),
                    new HttpEntity<>(body, headers), String.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        }
    }
}
