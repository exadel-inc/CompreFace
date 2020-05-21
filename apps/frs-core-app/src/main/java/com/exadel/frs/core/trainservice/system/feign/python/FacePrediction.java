package com.exadel.frs.core.trainservice.system.feign.python;

import lombok.Value;

@Value
public class FacePrediction {

    ScanBox box;
    String face_name;
    float probability;
    float is_face_prob;
}