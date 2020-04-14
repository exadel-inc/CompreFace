package com.exadel.frs.core.trainservice.domain;

import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "CLASSIFIER")
@Accessors(chain = true)
public class Classifier {

    @Id
    private String id;

    @Field("api_key")
    private String apiKey;

    @Field("class_2_face_name")
    private Map<Integer, String> classifierToFaceName;

    @Field("classifier_fs_id")
    private String classifierFsId;

    @Field("embedding_calculator_version")
    private String embeddingCalculatorVersion;

    @Field("version")
    private String version;
}