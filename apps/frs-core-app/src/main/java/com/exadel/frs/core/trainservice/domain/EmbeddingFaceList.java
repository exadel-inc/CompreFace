package com.exadel.frs.core.trainservice.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmbeddingFaceList {
    String calculatorVersion;

    /**
    Pair of id and face name as key and list of embeddings as value
     */
    Map<Pair<String, String>, List<List<Double>>> faceEmbeddings;
}
