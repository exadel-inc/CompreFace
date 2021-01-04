package com.exadel.frs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public enum ModelType implements EnumCode {

    RECOGNITION("R"),
    DETECTION("D");

    @Getter
    @Setter
    private String code;
}
