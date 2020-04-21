package com.exadel.frs.core.trainservice.domain;


import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@Document(collection = "MODELS")
@Accessors(chain = true)
public class Model {

    @Id
    private String id;

    @Field("classifier")
    private FaceClassifier classifier;

    @Field("classifierName")
    private String classifierName;

    @Field("api_key")
    private String apiKey;

}
