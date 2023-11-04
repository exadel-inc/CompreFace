package com.exadel.frs.commonservice.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FacesPoseDto {

    private Double pitch;
    private Double roll;
    private Double yaw;
}
