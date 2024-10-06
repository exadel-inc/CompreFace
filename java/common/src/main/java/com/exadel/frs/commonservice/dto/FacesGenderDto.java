package com.exadel.frs.commonservice.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FacesGenderDto {
    private Double probability;
    private String value;
}
