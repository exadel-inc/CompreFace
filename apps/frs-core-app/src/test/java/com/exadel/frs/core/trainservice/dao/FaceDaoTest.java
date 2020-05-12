package com.exadel.frs.core.trainservice.dao;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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
import org.springframework.data.mongodb.gridfs.GridFsOperations;

class FaceDaoTest {

    @Mock
    private FacesRepository facesRepository;

    @Mock
    private GridFsOperations gridFsOperations;

    @InjectMocks
    private FaceDao faceDao;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void deleteFaceByName() {
        val faceName = "faceName";
        val token = new Token(randomAlphabetic(10), randomAlphabetic(10));
        val faces = List.of(new Face());
        when(facesRepository.deleteByApiKeyAndFaceName(token.getModelApiKey(), faceName)).thenReturn(faces);

        val actual = faceDao.deleteFaceByName(faceName, token.getModelApiKey());

        assertThat(actual).isEqualTo(faces);

        verify(facesRepository).deleteByApiKeyAndFaceName(token.getModelApiKey(), faceName);
        verify(gridFsOperations, times(2)).delete(any());
        verifyNoMoreInteractions(facesRepository);
    }

    @Test
    void deleteFacesByApiKey() {
        val token = new Token(randomAlphabetic(10), randomAlphabetic(10));
        val faces = List.of(new Face());
        when(facesRepository.deleteFacesByApiKey(token.getModelApiKey())).thenReturn(faces);

        val actual = faceDao.deleteFacesByApiKey(token.getModelApiKey());

        assertThat(actual).isEqualTo(faces);

        verify(facesRepository).deleteFacesByApiKey(token.getModelApiKey());
        verify(gridFsOperations, times(2)).delete(any());
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