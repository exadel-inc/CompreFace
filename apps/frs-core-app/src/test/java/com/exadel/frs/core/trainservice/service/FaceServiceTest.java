package com.exadel.frs.core.trainservice.service;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.domain.Face;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class FaceServiceTest {

    @Mock
    private FaceClassifierStorage storage;

    @Mock
    private FacesRepository facesRepository;

    @InjectMocks
    private FaceService faceService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void deleteFacesByApiKey() {
        val appKey = randomAlphabetic(10);
        val modelApiKey = randomAlphabetic(10);
        val faces = List.of(new Face());
        when(facesRepository.deleteFacesByApiKey(modelApiKey)).thenReturn(faces);

        val actual = faceService.deleteFacesByApiKey(appKey, modelApiKey);

        assertThat(actual).isEqualTo(faces);

        verify(storage).removeFaceClassifier(appKey, modelApiKey);
        verify(facesRepository).deleteFacesByApiKey(modelApiKey);
        verifyNoMoreInteractions(storage, facesRepository);
    }
}