package com.exadel.frs.commonservice.projection;

import com.exadel.frs.commonservice.enums.ModelType;
import java.time.LocalDateTime;

public record ModelProjection(

        String guid,
        String name,
        String apiKey,
        ModelType type,
        LocalDateTime createdDate) {

}
