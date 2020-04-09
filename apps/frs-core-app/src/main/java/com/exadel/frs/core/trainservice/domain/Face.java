package com.exadel.frs.core.trainservice.domain;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "FACES")
@Accessors(chain = true)
public class Face {

    @Id
    private String id;

    @Field("embeddings")
    private List<Embedding> embeddings;

    @Field("face_name")
    private String faceName;

    @Field("raw_img_fs_id")
    private ObjectId rawImgId;

    @Field("face_img_fs_id")
    private ObjectId faceImgId;

    @Field("api_key")
    private String apiKey;

    @Data
    @Accessors(chain = true)
    public static class Embedding {

        @Field("array")
        private List<Double> embedding;

        @Field("calculator_version")
        private String calculatorVersion;
    }
}