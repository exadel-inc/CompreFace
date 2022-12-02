package com.exadel.frs.commonservice.projection;

import java.util.UUID;
import lombok.Value;

@Value
public class EnhancedEmbeddingProjection {

    UUID embeddingId;
    double[] embeddingData; // embedding column of embedding table
    String subjectName;
}
