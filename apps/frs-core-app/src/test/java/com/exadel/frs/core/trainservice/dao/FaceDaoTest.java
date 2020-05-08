package com.exadel.frs.core.trainservice.dao;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.exadel.frs.core.trainservice.system.Token;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class FaceDaoTest {

    @Mock
    private FacesRepository facesRepository;

    @InjectMocks
    private FaceDao faceDao;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void deleteFacesByApiKey() {
        val token = new Token(randomAlphabetic(10), randomAlphabetic(10));
        val faces = List.of(new Face());
        when(facesRepository.deleteFacesByApiKey(token.getModelApiKey())).thenReturn(faces);

        val actual = faceDao.deleteFacesByApiKey(token);

        assertThat(actual).isEqualTo(faces);

        verify(facesRepository).deleteFacesByApiKey(token.getModelApiKey());
        verifyNoMoreInteractions(facesRepository);
    }

    @Test
    void countFacesInModel() {
        val token = new Token(randomAlphabetic(10), randomAlphabetic(10));
        val facesCount = nextInt();

        when(facesRepository.countByApiKey(token.getModelApiKey())).thenReturn(facesCount);

        val actual = faceDao.countFacesInModel(token.getModelApiKey());

        assertThat(actual).isEqualTo(facesCount);

        verify(facesRepository).countByApiKey(token.getModelApiKey());
        verifyNoMoreInteractions(facesRepository);
    }
}