package com.exadel.frs.core.trainservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VersionConsistenceDto {
    Boolean demoFaceCollectionIsInconsistent;
    Boolean dbIsInconsistent;
    Boolean saveImagesToDB;
}
