package com.exadel.frs.core.trainservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.domain.Face;
import com.exadel.frs.core.trainservice.system.SystemService;
import com.exadel.frs.core.trainservice.system.Token;
import java.util.HashMap;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class FaceServiceTest {

    @Mock
    private FaceDao faceDao;

    @Mock
    private SystemService systemService;

    @Mock
    private RetrainService retrainService;

    @InjectMocks
    private FaceService faceService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void findAllFaceNames() {
        val apiKey = "api_key";
        val faces = new HashMap<String, List<String>>();

        when(faceDao.findAllFaceNamesByApiKey(apiKey)).thenReturn(faces);

        val actual = faceService.findAllFaceNames(apiKey);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(faces);

        verify(faceDao).findAllFaceNamesByApiKey(apiKey);
        verifyNoMoreInteractions(faceDao);
    }

    @Test
    void deleteFaceByName() {
        val faceName = "face_name";
        val apiKey = "api_key";
        val token = new Token(null, null);

        when(systemService.buildToken(apiKey)).thenReturn(token);

        faceService.deleteFaceByName(faceName, apiKey, "NO");

        verify(systemService).buildToken(apiKey);
        verify(faceDao).deleteFaceByName(faceName, token);
        verifyNoMoreInteractions(systemService, faceDao);
    }

    @Test
    void deleteFacesByModel() {
        val apiKey = "api_key";
        val token = new Token(null, null);
        val faces = List.of(new Face(), new Face(), new Face());

        when(systemService.buildToken(apiKey)).thenReturn(token);
        when(faceDao.deleteFacesByApiKey(token)).thenReturn(faces);

        val actual = faceService.deleteFacesByModel(apiKey);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(faces.size());

        verify(systemService).buildToken(apiKey);
        verify(faceDao).deleteFacesByApiKey(token);
        verifyNoMoreInteractions(systemService, faceDao);
    }
}