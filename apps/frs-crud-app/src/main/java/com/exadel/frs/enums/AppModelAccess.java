package com.exadel.frs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public enum AppModelAccess {
    READONLY("R"),
    TRAIN("T");

    @Getter
    @Setter
    private String code;
}
