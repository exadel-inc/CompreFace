package com.exadel.frs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public enum OrganizationRole {
    OWNER("O"),
    ADMINISTRATOR("A"),
    USER("U");

    @Getter
    @Setter
    private String code;
}
