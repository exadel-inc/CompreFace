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

package com.exadel.frs.core.trainservice.repository;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import com.exadel.frs.core.trainservice.entity.mongo.Face;
import com.exadel.frs.core.trainservice.repository.mongo.FacesRepository;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class FacesRepositoryTest {

    @Autowired
    private FacesRepository facesRepository;
    private final static String MODEL_KEY = "model_key";
    private final static String MODEL_KEY_OTHER = "model_key_other";

    @BeforeEach
    void setUp() {
        val faceA = makeFace("A", MODEL_KEY);
        val faceB = makeFace("B", MODEL_KEY_OTHER);
        val faceC = makeFace("C", MODEL_KEY);

        facesRepository.saveAll(List.of(faceA, faceB, faceC));
    }

    @AfterEach
    public void cleanUp() {
        facesRepository.deleteAll();
    }

    public static Face makeFace(final String name, final String modelApiKey) {
        val face = new Face()
                .setFaceName(name)
                .setApiKey(modelApiKey);
        face.setEmbeddings(List.of(
                new Face.Embedding()
                        .setEmbedding(List.of(0.0D))
                        .setCalculatorVersion("1.0")
                )
        );
        face.setFaceImgId(new ObjectId("hex-string-1".getBytes()));
        face.setRawImgId(new ObjectId("hex-string-2".getBytes()));
        face.setId("Id_" + name);

        return face;
    }

    @Test
    public void getAll() {
        val actual = facesRepository.findAll();

        assertThat(actual).isNotNull();
        assertThat(actual).hasSize(3);
        assertThat(actual).allSatisfy(
                face -> {
                    assertThat(face.getId()).isNotEmpty();
                    assertThat(face.getFaceName()).isNotEmpty();
                    assertThat(face.getApiKey()).isNotEmpty();
                    assertThat(face.getFaceImgId()).isNotNull();
                    assertThat(face.getRawImgId()).isNotNull();
                    assertThat(face.getEmbeddings()).isNotEmpty();
                }
        );
    }

    @Test
    public void findNamesForApiGuid() {
        val expected = Arrays.asList("A", "C");
        val actual = facesRepository.findByApiKey(MODEL_KEY).stream()
                                    .map(Face::getFaceName)
                                    .collect(toList());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void countByApiKey() {
        val expected = facesRepository.findByApiKey(MODEL_KEY);
        val actual = facesRepository.countByApiKey(MODEL_KEY);

        assertThat(actual).isGreaterThan(0);
        assertThat(actual).isEqualTo(expected.size());
    }

    @Test
    public void findFaceIdsIn() {
        val faces = facesRepository.findByIdIn(List.of("Id_A", "Id_B"));
        assertThat(faces).isNotNull();
        assertThat(faces).hasSize(2);
    }
}