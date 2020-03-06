package com.exadel.frs.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAutocompleteDto {

    private String query;
    private int length;
    private List<UserResponseDto> results;
}