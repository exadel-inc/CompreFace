package com.exadel.frs.commonservice.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FacesAgeDto {
    private Double probability;
    private Integer high;
    private Integer low;
}
