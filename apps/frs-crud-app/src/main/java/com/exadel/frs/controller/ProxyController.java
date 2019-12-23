package com.exadel.frs.controller;

import com.exadel.frs.entity.Model;
import com.exadel.frs.enums.AppModelAccess;
import com.exadel.frs.exception.AccessDeniedException;
import com.exadel.frs.exception.AppOrModelNotFoundException;
import com.exadel.frs.repository.ModelRepository;
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

    @Value("${proxy.baseUrl}")
    private String baseUrl;

    private static final List<UrlMethod> readOnlyApiUrls = List.of(
            new UrlMethod(HttpMethod.GET, "/faces"),
            new UrlMethod(HttpMethod.GET, "/retrain"),
            new UrlMethod(HttpMethod.GET, "/status"),
            new UrlMethod(HttpMethod.POST, "/recognize"));

    private static final List<UrlMethod> trainApiUrls = List.of(
            new UrlMethod(HttpMethod.POST, "/faces"),
            new UrlMethod(HttpMethod.POST, "/retrain"),
            new UrlMethod(HttpMethod.DELETE, "/retrain"),
            new UrlMethod(HttpMethod.DELETE, "/faces"));

    @Data
    @AllArgsConstructor
    private static class UrlMethod {
        private HttpMethod httpMethod;
        private String url;
    }

    private AppModelAccess getAppModelAccessType(String appGuid, String modelGuid) {
        Model model = modelRepository.findByGuid(modelGuid)
                .orElseThrow(AppOrModelNotFoundException::new);
        if (appGuid.equals(model.getApp().getGuid())) {
            return AppModelAccess.TRAIN;
        }
        return model.getAppModelAccess()
                .stream()
                .filter(appModel -> appGuid.equals(appModel.getApp().getGuid()))
                .findFirst()
                .orElseThrow(AppOrModelNotFoundException::new)
                .getAccessType();
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    @ApiOperation(value = "Send request to core service")
    public ResponseEntity<String> proxy(
            @ApiParam(value = "GUID of application and model", required = true) @RequestHeader(X_FRS_API_KEY_HEADER) String apiKey,
            @ApiParam(value = "Headers that will be proxied to core service", required = true) @RequestHeader MultiValueMap<String, String> headers,
            @ApiParam(value = "String parameters that will be proxied to core service") @RequestParam(required = false) Map<String, String> params,
            @ApiParam(value = "Files that will be proxied to core service") @RequestParam(required = false) Map<String, MultipartFile> files,
            HttpServletRequest request) {
        int guidLength = apiKey.length() / 2;
        String appGuid = apiKey.substring(0, guidLength);
        String modelGuid = apiKey.substring(guidLength);
        if (AppModelAccess.READONLY == getAppModelAccessType(appGuid, modelGuid)) {
            readOnlyApiUrls.stream()
                    .filter(urlMethod -> request.getRequestURI().startsWith(PREFIX + urlMethod.getUrl())
                            && request.getMethod().equals(urlMethod.getHttpMethod().toString()))
                    .findFirst()
                    .orElseThrow(AccessDeniedException::new);
        }
        String remoteUrl = baseUrl + request.getRequestURI().replaceFirst(PREFIX, "");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);
        files.forEach((key, file) -> body.add(key, file.getResource()));
        headers.remove(X_FRS_API_KEY_HEADER);
        headers.add(X_API_KEY_HEADER, apiKey);
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.exchange(remoteUrl, HttpMethod.resolve(request.getMethod()),
                    new HttpEntity<>(body, headers), String.class);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        }
    }

}
