package com.exadel.frs.commonservice.system.global;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegExConstants {

    public static final String CONTAINS_SPECIAL_CHARACTERS = "[`~!@\"#№$%^:;&?<>(){|},/\\\\*+=]+";
    public static final String DOES_NOT_CONTAIN_SPECIAL_CHARACTERS = "[^`~!@\"#№$%^:;&?<>(){|},/\\\\*+=]+";
}
