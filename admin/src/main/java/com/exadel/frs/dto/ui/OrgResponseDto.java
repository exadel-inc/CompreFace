package com.exadel.frs.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrgResponseDto {

    private String id;
    private String name;
    private String role;
}