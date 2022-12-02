package com.exadel.frs.commonservice.projection;

import java.util.Date;
import lombok.Value;

@Value
public class ModelStatisticProjection {

    long requestCount;
    Date createdDate;
}
