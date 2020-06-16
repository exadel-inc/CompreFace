/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.controller;

import static com.exadel.frs.enums.AppModelAccess.READONLY;
import static com.exadel.frs.enums.AppModelAccess.TRAIN;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import com.exadel.frs.enums.AppModelAccess;
import com.exadel.frs.exception.AccessDeniedException;
import com.exadel.frs.exception.ModelDoesNotBelongToAppException;
import com.exadel.frs.repository.AppModelRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.validation.ImageExtensionValidator;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ProxyController.PREFIX)
@RequiredArgsConstructor
public class ProxyController {

    static final String PREFIX = "/api";
    private static final String X_FRS_API_KEY_HEADER = "x-frs-api-key";
    private static final String X_API_KEY_HEADER = "X-Api-Key";

    private final ModelRepository modelRepository;
    private final AppModelRepository appModelRepository;
    private final ImageExtensionValidator imageValidator;

    @Value("${proxy.baseUrl}")
    private String baseUrl;

    private static final List<UrlMethod> readOnlyApiMethods = ImmutableList.of(
            new UrlMethod(HttpMethod.GET, "/faces"),
            new UrlMethod(HttpMethod.GET, "/retrain"),
            new UrlMethod(HttpMethod.GET, "/status"),
            new UrlMethod(HttpMethod.POST, "/recognize")
    );

    @lombok.Value
    private static class UrlMethod {

        private HttpMethod httpMethod;
        private String url;
    }

    private AppModelAccess getAppModelAccessType(final String appApiKey, final String modelApiKey) {
        val model = modelRepository.findByApiKey(modelApiKey)
                                   .orElseThrow(() -> new ModelDoesNotBelongToAppException(modelApiKey, appApiKey));
        if (appApiKey.equals(model.getApp().getApiKey())) {
            return TRAIN;
        }

        return appModelRepository.findByAppApiKeyAndModelApiKey(appApiKey, modelApiKey)
                                 .orElseThrow(() -> new ModelDoesNotBelongToAppException(modelApiKey, appApiKey))
                                 .getAccessType();
    }

    @RequestMapping(value = "/v1/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    @ApiOperation(value = "Send request to core service")
    public ResponseEntity<String> proxyV1(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey,
            @ApiParam(value = "Headers that will be proxied to core service", required = true)
            @RequestHeader
            final MultiValueMap<String, String> headers,
            @ApiParam(value = "String parameters that will be proxied to core service")
            @RequestParam(required = false)
            final Map<String, String> params,
            @ApiParam(value = "Files that will be proxied to core service")
            @RequestParam(required = false)
            final Map<String, MultipartFile> files,
            final HttpServletRequest request
    ) {
        val apiKeyLength = apiKey.length() / 2;
        val appApiKey = apiKey.substring(0, apiKeyLength);
        val modelApiKey = apiKey.substring(apiKeyLength);

        val appHasOnlyReadAccessToModel = READONLY == getAppModelAccessType(appApiKey, modelApiKey);
        val isReadOnlyRequest = readOnlyApiMethods.stream()
                .anyMatch(urlMethod ->
                        request.getRequestURI().startsWith(PREFIX + "/v1" + urlMethod.getUrl()) &&
                                request.getMethod().equals(urlMethod.getHttpMethod().toString())
                );

        if (appHasOnlyReadAccessToModel && isNotTrue(isReadOnlyRequest)) {
            throw new AccessDeniedException();
        }

        imageValidator.validate(files.values());

        val remoteUrl = baseUrl + request.getRequestURI().replaceFirst(PREFIX + "/v1", "");
        val body = new LinkedMultiValueMap<String, Object>();

        params.forEach(body::add);
        files.forEach((key, file) -> body.add(key, file.getResource()));
        headers.remove(X_FRS_API_KEY_HEADER);
        headers.add(X_API_KEY_HEADER, apiKey);

        try {
            return new RestTemplate().exchange(
                    remoteUrl,
                    HttpMethod.resolve(request.getMethod()),
                    new HttpEntity<>(body, headers),
                    String.class
            );
        } catch (HttpClientErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        }
    }
}