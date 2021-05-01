package com.exadel.frs.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VersionConsistenseDto {
    boolean demoFaceCollectionIsInconsistent;
    boolean dbIsInconsistent;
    boolean saveImagesToDB;
}
