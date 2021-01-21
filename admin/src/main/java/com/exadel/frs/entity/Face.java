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

package com.exadel.frs.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@EqualsAndHashCode(of = "id")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Face {

    public Face(Face face) {
        this.id = face.id;
        this.faceName = face.faceName;
        this.apiKey = face.apiKey;
        this.embedding = face.embedding;
    }

    @Id
    private String id;

    @Column(name = "face_name")
    private String faceName;

    @Column(name = "api_key")
    private String apiKey;

    @Type(type = "jsonb")
    @Column(name = "embeddings")
    private Embedding embedding;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Embedding {

        private List<Double> embeddings;
        private String calculatorVersion;
    }
}
