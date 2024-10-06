package com.exadel.frs.commonservice.projection;

import java.util.UUID;

/**
 * @param embeddingData embedding column of embedding table
 */
public record EnhancedEmbeddingProjection(UUID embeddingId, double[] embeddingData, String subjectName) {

}
