package com.exadel.frs.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public enum ModelAccessType {
    READONLY("R"),
    TRAIN("T");

    @Getter
    @Setter
    private String code;
}
