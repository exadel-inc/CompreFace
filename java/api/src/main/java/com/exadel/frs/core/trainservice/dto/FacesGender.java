package com.exadel.frs.core.trainservice.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FacesGender {
    private Double probability;
    private String value;
}
