package com.exadel.frs.commonservice.entity;

import java.util.Date;
import lombok.Value;

@Value
public class ModelStatisticProjection {

    long requestCount;
    Date createdDate;
}
