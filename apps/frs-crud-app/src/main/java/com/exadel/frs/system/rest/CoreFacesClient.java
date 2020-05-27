package com.exadel.frs.system.rest;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface CoreFacesClient {

    @RequestLine("DELETE /api/v1/faces")
    @Headers("x-frs-api-key: {apiKey}")
    int deleteFaces(@Param("apiKey") final String apiKey);

    @RequestLine("PUT /api/v1/faces/api-key?new_model_api_key={newModelKey}")
    @Headers("x-frs-api-key: {apiKey}")
    void updateModelKeyForFaces(
            @Param("apiKey") final String apiKey,
            @Param("newModelKey") final String newModelKey
    );
}