package com.exadel.frs.commonservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelFaceProjection {

    private String guid;

    private long facesCount;
}
