package com.exadel.frs.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModelDto {

    private String name;
    private String guid;
    private String apiKey;
    private List<AppAccessDto> appModelAccess;
    private Long appId;

}
