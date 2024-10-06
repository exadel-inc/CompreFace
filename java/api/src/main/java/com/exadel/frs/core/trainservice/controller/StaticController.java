package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_KEY_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.CACHE_CONTROL_HEADER_VALUE;
import static com.exadel.frs.core.trainservice.system.global.Constants.IMAGE_ID_DESC;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.core.trainservice.service.EmbeddingService;
import io.swagger.annotations.ApiParam;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(API_V1 + "/static")
@RequiredArgsConstructor
public class StaticController {

    private final EmbeddingService embeddingService;

    @ResponseBody
    @GetMapping(value = "/{apiKey}/images/{embeddingId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] downloadImg(
                              @ApiParam(value = API_KEY_DESC, required = true)
                              @PathVariable("apiKey")
                              final String apiKey,
                              @ApiParam(value = IMAGE_ID_DESC, required = true)
                              @PathVariable
                              final UUID embeddingId,
                              final HttpServletResponse response
    ) {
        response.addHeader(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER_VALUE);
        return embeddingService.getImg(apiKey, embeddingId)
                               .map(Img::getContent)
                               .orElse(new byte[]{});
    }
}
