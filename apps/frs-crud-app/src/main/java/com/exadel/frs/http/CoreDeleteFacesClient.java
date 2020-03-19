package com.exadel.frs.http;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface CoreDeleteFacesClient {

    @RequestLine("DELETE /faces")
    @Headers("x-frs-api-key: {apiKey}")
    int deleteFaces(@Param("apiKey") final String apiKey);
}