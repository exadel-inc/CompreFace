package com.exadel.frs.commonservice.entity;

import java.util.UUID;
import lombok.Value;

@Value
public class EmbeddingSubjectProjection {

    UUID embeddingId;
    double[] embeddingData;
    String subjectName;
}
