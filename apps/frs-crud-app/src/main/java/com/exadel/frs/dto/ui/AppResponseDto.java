package com.exadel.frs.dto.ui;

import lombok.Data;

@Data
public class AppResponseDto {

    private String id;
    private String name;
    private String apiKey;
    private String role;
    private AppOwnerDto owner;

}
