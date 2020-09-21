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

package com.exadel.frs.core.trainservice.cache;

import static com.exadel.frs.core.trainservice.repository.FacesRepositoryTest.makeFace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class FaceCacheProviderTest {

    @Mock
    private FaceDao faceDao;

    @InjectMocks
    private FaceCacheProvider faceCacheProvider;

    private static final String API_KEY = "model_key";

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void getOrLoad() {
        val faces = List.of(
                makeFace("A", API_KEY),
                makeFace("B", API_KEY)
        );
        when(faceDao.findAllFacesByApiKey(API_KEY)).thenReturn(faces);

        val actual = faceCacheProvider.getOrLoad(API_KEY);

        verify(faceDao).findAllFacesByApiKey(API_KEY);
        verifyNoMoreInteractions(faceDao);

        assertThat(actual).isNotNull();
        assertThat(actual.getFaces()).isNotNull();
        assertThat(actual.getFaces().size()).isEqualTo(faces.size());
    }
}
