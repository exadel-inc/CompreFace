package com.exadel.frs.commonservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public enum StatisticsType {

    USER_CREATE("UC"),
    APP_CREATE("AC"),
    FACE_COLLECTION_CREATE("FC");

    @Getter
    @Setter
    private String code;
}