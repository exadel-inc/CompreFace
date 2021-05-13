package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.commonservice.entity.Embedding;
import lombok.Value;

import java.util.UUID;

/**
 * Current class works as a key for embedding collection cache.
 */
@Value
public class SubjectMeta {

    UUID subjectId;
    String subjectName;
    UUID embeddingId;

    public static SubjectMeta from(Embedding embedding) {
        return new SubjectMeta(
                embedding.getSubject().getId(),
                embedding.getSubject().getSubjectName(),
                embedding.getId()
        );
    }

    public SubjectMeta withNewValues(UUID newSubjectId, String newSubjectName) {
        return new SubjectMeta(
                newSubjectId,
                newSubjectName,
                this.getEmbeddingId()
        );
    }
}
