package com.exadel.frs.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExceptionResponseDto {
    private String message;
    private Integer code;
}
