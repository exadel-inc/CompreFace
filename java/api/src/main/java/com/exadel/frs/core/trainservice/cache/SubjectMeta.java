package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.commonservice.entity.Embedding;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Current class works as a key for embedding collection cache.
 */
@Value
public class SubjectMeta {

    UUID subjectId;
    String subjectName;
    UUID embeddingId;
    @Nullable // could be null for demo data
    UUID imgId;

    public static SubjectMeta from(Embedding embedding) {
        return new SubjectMeta(
                embedding.getSubject().getId(),
                embedding.getSubject().getSubjectName(),
                embedding.getId(),
                embedding.getImg() != null ? embedding.getImg().getId() : null
        );
    }

    public SubjectMeta withNewSubjectName(String newSubjectName) {
        return new SubjectMeta(
                this.getSubjectId(),
                newSubjectName,
                this.getEmbeddingId(),
                this.getImgId()
        );
    }
}
