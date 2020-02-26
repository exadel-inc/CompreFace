package com.exadel.frs.dto.ui;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ModelShareResponseDto {

    private UUID modelRequestUuid;
}