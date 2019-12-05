package com.exadel.frs.dto;

import com.exadel.frs.enums.AppModelAccess;
import lombok.Data;

@Data
public class ModelAccessDto {

    private Long modelId;
    private AppModelAccess accessType;

}
