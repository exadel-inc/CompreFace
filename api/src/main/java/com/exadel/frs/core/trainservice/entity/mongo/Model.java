/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.entity.mongo;

import com.exadel.frs.core.trainservice.component.classifiers.Classifier;
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
    private Classifier classifier;

    @Field("faces")
    private List<ObjectId> faces;

    @Field("calculator_version")
    private String calculatorVersion;
}