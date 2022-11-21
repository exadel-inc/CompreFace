package com.exadel.frs.commonservice.entity;

import com.exadel.frs.commonservice.enums.ModelType;
import java.time.LocalDateTime;

public record ModelProjection(
        String guid,
        String name,
        String apiKey,
        ModelType type,
        LocalDateTime createdDate) {

}
