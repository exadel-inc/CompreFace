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
@RequestMapping("/proxy")
@RequiredArgsConstructor
public class ProxyController {

    private static final String API_KEY_HEADER = "x-frs-api-key";
    private static final String APP_GUID_HEADER = "app-guid";
    private static final String MODEL_GUID_HEADER = "model-guid";

    private final ModelRepository modelRepository;

    @Value("${proxy.baseUrl}")
    private String baseUrl;

    private static final List<UrlMethod> readOnlyApiUrls = List.of(
            new UrlMethod(HttpMethod.GET, "/api/faces"),
            new UrlMethod(HttpMethod.GET, "/api/retrain"),
            new UrlMethod(HttpMethod.GET, "/api/status"),
            new UrlMethod(HttpMethod.POST, "/api/recognize"));

    private static final List<UrlMethod> trainApiUrls = List.of(
            new UrlMethod(HttpMethod.POST, "/api/faces"),
            new UrlMethod(HttpMethod.POST, "/api/retrain"),
            new UrlMethod(HttpMethod.DELETE, "/api/retrain"),
            new UrlMethod(HttpMethod.DELETE, "/api/faces"));

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
            @ApiParam(value = "GUID of application", required = true) @RequestHeader(APP_GUID_HEADER) String appGuid,
            @ApiParam(value = "GUID of model, to which application has access", required = true) @RequestHeader(MODEL_GUID_HEADER) String modelGuid,
            @ApiParam(value = "Headers that will be proxied to core service", required = true) @RequestHeader MultiValueMap<String, String> headers,
            @ApiParam(value = "String parameters that will be proxied to core service") @RequestParam(required = false) Map<String, String> params,
            @ApiParam(value = "Files that will be proxied to core service") @RequestParam(required = false) Map<String, MultipartFile> files,
            HttpServletRequest request) {
        if (AppModelAccess.READONLY == getAppModelAccessType(appGuid, modelGuid)) {
            readOnlyApiUrls.stream()
                    .filter(urlMethod -> request.getRequestURI().startsWith("/proxy" + urlMethod.getUrl())
                            && request.getMethod().equals(urlMethod.getHttpMethod().toString()))
                    .findFirst()
                    .orElseThrow(AccessDeniedException::new);
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
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        }
    }

}
