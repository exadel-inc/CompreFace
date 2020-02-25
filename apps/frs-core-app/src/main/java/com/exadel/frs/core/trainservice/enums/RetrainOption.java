package com.exadel.frs.core.trainservice.enums;

public enum RetrainOption {
    YES,
    NO,
    FORCE;

    public static RetrainOption from(String retrain) {
        RetrainOption retrainOption;
        try {
            retrainOption = valueOf(retrain);
        } catch (IllegalArgumentException e) {
            retrainOption = FORCE;
        }

        return retrainOption;
    }
}
