package com.exadel.frs.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModelDto {

    private Long id;
    private String name;
    private String guid;
    private List<AppAccess> appAccess;

}
