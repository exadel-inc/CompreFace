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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Embedding {

        @Field("array")
        private List<Double> embedding;

        @Field("calculator_version")
        private String calculatorVersion;
    }
}