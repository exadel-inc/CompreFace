package com.exadel.frs.core.trainservice.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;


@Data
@Document(collection = "faces")
@Accessors(chain = true)
public class Face {

    @Id
    private String id;

    private List<Embedding> embeddings;

    @Field("face_name")
    private String faceName;

    @Field("raw_img_id")
    private String rawImgId;

    @Field("face_img_id")
    private String faceImgId;

    @Field("api_key")
    private String apiKey;

    @Data
    @Accessors(chain = true)
    public static class Embedding {

        private List<Double> embedding;

        @Field("model_name")
        private String modelName;
    }
}
