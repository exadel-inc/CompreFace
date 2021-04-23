package com.exadel.frs.commonservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatisticsType {

    USER_CREATE("UC"),
    APP_CREATE("AC"),
    FACE_DETECTION_CREATE("DC"),
    FACE_RECOGNITION_CREATE("RC"),
    FACE_VERIFICATION_CREATE("VC");

    private final String code;
}