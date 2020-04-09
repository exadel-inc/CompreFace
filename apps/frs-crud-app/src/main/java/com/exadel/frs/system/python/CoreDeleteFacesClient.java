package com.exadel.frs.system.python;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface CoreDeleteFacesClient {

    @RequestLine("DELETE /api/v1/faces")
    @Headers("x-frs-api-key: {apiKey}")
    int deleteFaces(@Param("apiKey") final String apiKey);
}