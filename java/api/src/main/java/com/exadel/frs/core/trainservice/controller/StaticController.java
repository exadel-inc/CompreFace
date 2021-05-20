package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.core.trainservice.service.EmbeddingService;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.exadel.frs.core.trainservice.system.global.Constants.*;

@RestController
@RequestMapping(API_V1 + "/static")
@RequiredArgsConstructor
public class StaticController {

    private final EmbeddingService embeddingService;

    @GetMapping(value = "/{apiKey}/images/{embeddingId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] downloadImg(
            @ApiParam(value = API_KEY_DESC, required = true) @PathVariable("apiKey") final String apiKey,
            @ApiParam(value = IMAGE_ID_DESC, required = true) @PathVariable final UUID embeddingId) {
        return embeddingService.getImg(apiKey, embeddingId)
                .map(Img::getContent)
                .orElse(new byte[]{});
    }
}
