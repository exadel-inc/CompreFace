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

package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.enums.RetrainOption.NO;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import java.util.List;
import java.util.Random;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@Import({FaceService.class, FaceDao.class})
@MockBeans({
        @MockBean(RetrainService.class),
        @MockBean(FaceClassifierManager.class)
})
public class FaceServiceTestIT {

    @Autowired
    private FacesRepository facesRepository;

    @Autowired
    private FaceService faceService;

    private static final String MODEL_KEY = randomUUID().toString();
    private static final String MODEL_KEY_OTHER = randomUUID().toString();

    @BeforeEach
    void setUp() {
        val faceA = makeFace("A", MODEL_KEY);
        val faceB = makeFace("B", MODEL_KEY_OTHER);
        val faceC = makeFace("C", MODEL_KEY);

        facesRepository.saveAll(List.of(faceA, faceB, faceC));
    }

    @Test
    public void deleteFaceByGuid() {
        val faces = facesRepository.findAll();
        val face = faces.get(new Random().nextInt(faces.size()));

        faceService.deleteFaceById(face.getId(), face.getApiKey(), NO.name());

        val actual = facesRepository.findAll();

        assertThat(actual).hasSize(faces.size() - 1);
        assertThat(actual).doesNotContain(face);
    }

    @Test
    public void findFaces() {
        //todo
    }

    @Test
    public void deleteFaceByName() {
        //todo
    }

    @Test
    public void deleteFacesByModel() {
        //todo
    }

    @AfterEach
    public void cleanUp() {
        facesRepository.deleteAll();
    }

    public static Face makeFace(final String name, final String modelApiKey) {
        return new Face()
                .setFaceName(name)
                .setApiKey(modelApiKey)
                .setFaceImg("hex-string-1".getBytes())
                .setRawImg("hex-string-2".getBytes())
                .setId(randomUUID().toString());
    }
}