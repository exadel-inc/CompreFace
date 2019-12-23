package com.exadel.frs.dto;

import com.exadel.frs.enums.AppModelAccess;
import lombok.Data;

@Data
public class AppAccessDto {

    private Long appId;
    private AppModelAccess accessType;

}
