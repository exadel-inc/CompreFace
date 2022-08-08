package com.exadel.frs.commonservice.entity;

import java.time.LocalDateTime;
import lombok.Value;

@Value
public class ModelStatisticProjection {

    int requestCount;
    LocalDateTime createdDate;
}
