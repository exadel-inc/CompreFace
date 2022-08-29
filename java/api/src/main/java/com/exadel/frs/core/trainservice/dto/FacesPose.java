package com.exadel.frs.core.trainservice.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FacesPose {

    private Double pitch;
    private Double roll;
    private Double yaw;
}
