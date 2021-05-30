package com.exadel.frs.commonservice.entity;

import lombok.Value;

import java.util.UUID;

@Value
public class EmbeddingProjection {

    UUID embeddingId;
    String subjectName;

    public static EmbeddingProjection from(Embedding embedding) {
        return new EmbeddingProjection(
                embedding.getId(),
                embedding.getSubject().getSubjectName()
        );
    }

    public EmbeddingProjection withNewSubjectName(String newSubjectName) {
        return new EmbeddingProjection(
                this.getEmbeddingId(),
                newSubjectName
        );
    }
}
