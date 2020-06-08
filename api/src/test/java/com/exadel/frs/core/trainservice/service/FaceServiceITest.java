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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import com.exadel.frs.core.trainservice.entity.mongo.Face;
import com.exadel.frs.core.trainservice.repository.mongo.FacesRepository;
import com.exadel.frs.core.trainservice.system.SystemServiceImpl;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@Import({FaceService.class, FaceDao.class, SystemServiceImpl.class})
@MockBeans({@MockBean(RetrainService.class), @MockBean(FaceClassifierManager.class)})
public class FaceServiceITest {

    @Autowired
    private FacesRepository facesRepository;

    @Autowired
    private FaceService faceService;

    @MockBean
    private ModelDao modelDao;

    private final static String MODEL_KEY = UUID.randomUUID().toString();
    private final static String MODEL_KEY_OTHER = UUID.randomUUID().toString();

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
        return new Face()
                .setFaceName(name)
                .setApiKey(modelApiKey)
                .setFaceImgId(new ObjectId("hex-string-1".getBytes()))
                .setRawImgId(new ObjectId("hex-string-2".getBytes()))
                .setId("Id_" + name)
                .setEmbeddings(List.of(
                        new Face.Embedding()
                                .setEmbedding(List.of(0.0D))
                                .setCalculatorVersion("1.0")
                        )
                );
    }

}