package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.commonservice.entity.Embedding;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.UUID;

@Value
public class EmbeddingMeta {

    String subjectName;
    @Nullable
    UUID imgId;

    public static EmbeddingMeta from(Embedding embedding) {
        return new EmbeddingMeta(
                embedding.getSubject().getSubjectName(),
                embedding.getImg() != null ? embedding.getImg().getId() : null
        );
    }
}
