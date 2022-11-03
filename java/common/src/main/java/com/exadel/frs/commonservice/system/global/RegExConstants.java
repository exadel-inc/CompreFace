package com.exadel.frs.commonservice.system.global;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegExConstants {

    public static final String ALLOWED_SPECIAL_CHARACTERS = "[^;/\\\\]+";
    public static final String PROHIBITED_SPECIAL_CHARACTERS = "[;/\\\\]+";
}
