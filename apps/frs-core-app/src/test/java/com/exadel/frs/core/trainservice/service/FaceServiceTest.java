package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.enums.RetrainOption.NO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.entity.Face;
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

    @Mock
    private FaceClassifierManager classifierManager;

    @InjectMocks
    private FaceService faceService;

    private static final String APP_KEY = "app_key";
    private static final String MODEL_KEY = ":model_key";
    private static final String API_KEY = APP_KEY + MODEL_KEY;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void findAllFaceNames() {
        val faces = new HashMap<String, List<String>>();
        val token = new Token(APP_KEY, MODEL_KEY);

        when(systemService.buildToken(API_KEY)).thenReturn(token);
        when(faceDao.findAllFaceNamesByApiKey(token.getModelApiKey())).thenReturn(faces);

        val actual = faceService.findAllFaceNames(API_KEY);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(faces);

        verify(faceDao).findAllFaceNamesByApiKey(token.getModelApiKey());
        verifyNoMoreInteractions(faceDao);
    }

    @Test
    void deleteFaceByName() {
        val faceName = "face_name";
        val token = new Token(APP_KEY, MODEL_KEY);

        when(systemService.buildToken(API_KEY)).thenReturn(token);

        faceService.deleteFaceByName(faceName, API_KEY, NO.name());

        verify(systemService).buildToken(API_KEY);
        verify(faceDao).deleteFaceByName(faceName, token.getModelApiKey());
        verifyNoMoreInteractions(systemService, faceDao);
    }

    @Test
    void deleteFacesByModel() {
        val token = new Token(APP_KEY, MODEL_KEY);
        val faces = List.of(new Face(), new Face(), new Face());

        when(systemService.buildToken(API_KEY)).thenReturn(token);
        when(faceDao.deleteFacesByApiKey(token.getModelApiKey())).thenReturn(faces);
        doNothing().when(classifierManager).removeFaceClassifier(token.getModelApiKey());

        val actual = faceService.deleteFacesByModel(API_KEY);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(faces.size());

        val inOrder = inOrder(systemService, classifierManager, faceDao);
        inOrder.verify(systemService).buildToken(API_KEY);
        inOrder.verify(classifierManager).removeFaceClassifier(token.getAppApiKey(), token.getModelApiKey());
        inOrder.verify(faceDao).deleteFacesByApiKey(token.getModelApiKey());
        verifyNoMoreInteractions(systemService, faceDao);
        verifyNoInteractions(retrainService);
    }
}