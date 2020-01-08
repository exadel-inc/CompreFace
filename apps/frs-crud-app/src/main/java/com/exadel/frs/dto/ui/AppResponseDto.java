package com.exadel.frs.dto.ui;

import lombok.Data;

@Data
public class AppResponseDto {

    private Long id;
    private String name;
    private UserResponseDto owner;

}
