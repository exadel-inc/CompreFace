package com.exadel.frs.core.trainservice.dto;

import lombok.Data;

@Data
public class UploadFileSizeResponse {
    private Integer clientMaxFileSize;
    private Integer clientMaxBodySize;
}
