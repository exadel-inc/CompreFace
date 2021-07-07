package com.exadel.frs.core.trainservice.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FacesAge {
    private Double probability;
    private Integer high;
    private Integer low;
}
