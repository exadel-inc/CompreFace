package com.exadel.frs.commonservice.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@NoArgsConstructor
@JsonInclude(NON_NULL)
public class ExecutionTimeDto {

    private Double age;
    private Double gender;
    private Double pose;
    private Double detector;
    private Double calculator;
    private Double mask;
}