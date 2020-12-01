package com.exadel.frs.core.trainservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public enum DbAction {
    INSERT("I"),
    DELETE("D"),
    DELETE_ALL("DA");

    @Getter
    @Setter
    private String code;

}
