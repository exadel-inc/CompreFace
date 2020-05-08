package com.exadel.frs.core.trainservice.domain;

import static lombok.AccessLevel.PRIVATE;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;

@Data
@Accessors(chain = true)
@FieldDefaults(level = PRIVATE)
public class EmbeddingFaceList {

    String calculatorVersion;

    /**
    Pair of id and face name as key and list of embeddings as value
     */
    Map<Pair<String, String>, List<List<Double>>> faceEmbeddings = new HashMap<>();
}
