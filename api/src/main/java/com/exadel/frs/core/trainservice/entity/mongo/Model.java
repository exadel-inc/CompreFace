package com.exadel.frs.core.trainservice.entity.mongo;

import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@Document(collection = "MODELS")
@Accessors(chain = true)
public class Model {

    @Id
    private String id;

    @Field("model_key")
    @Indexed
    private String modelKey;

    @Field("classifier")
    private FaceClassifier classifier;

    @Field("faces")
    private List<ObjectId> faces;

    @Field("calculator_version")
    private String calculatorVersion;
}