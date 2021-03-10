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

import com.exadel.frs.commonservice.entity.App;
import com.exadel.frs.commonservice.entity.Face;
import com.exadel.frs.commonservice.enums.ModelType;
import com.exadel.frs.commonservice.repository.FacesRepository;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.exadel.frs.core.trainservice.ItemsBuilder.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class FacesRepositoryTest extends EmbeddedPostgreSQLTest {

    @Autowired
    private FacesRepository facesRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final static String MODEL_KEY = "model_key";
    private final static String MODEL_KEY_OTHER = "model_key_other";

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM face");
        jdbcTemplate.execute("DELETE FROM model");
        jdbcTemplate.execute("DELETE FROM app");

        App app = makeApp(1L, MODEL_KEY);
        jdbcTemplate.update("INSERT INTO App(id, name, guid, api_key) VALUES (?, ?, ?, ?)",
                app.getId(),
                app.getName(),
                app.getGuid(),
                app.getApiKey());

        modelRepository.saveAll(List.of(
                makeModel(MODEL_KEY, ModelType.RECOGNITION, app),
                makeModel(MODEL_KEY_OTHER, ModelType.RECOGNITION, app)
        ));

        facesRepository.saveAll(List.of(
                makeFace("A", MODEL_KEY),
                makeFace("B", MODEL_KEY_OTHER),
                makeFace("C", MODEL_KEY)
        ));
    }

    @AfterEach
    public void cleanUp() {
        facesRepository.deleteAll();
    }

    @Test
    public void getAll() {
        val actual = facesRepository.findAll();

        assertThat(actual).isNotNull();
        assertThat(actual).hasSize(3);
        assertThat(actual).allSatisfy(
                face -> {
                    assertThat(face.getId()).isNotNull();
                    assertThat(face.getFaceName()).isNotEmpty();
                    assertThat(face.getApiKey()).isNotEmpty();
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
    public void findByGuid() {
        val faces = facesRepository.findAll();
        val face = faces.get(Math.abs(new Random().nextInt()) % faces.size());

        val actual = facesRepository.findById(face.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(face.getId());
    }
}