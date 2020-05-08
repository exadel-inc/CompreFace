package com.exadel.frs.core.trainservice.component;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import com.exadel.frs.core.trainservice.domain.EmbeddingFaceList;
import com.exadel.frs.core.trainservice.exception.ModelHasNoFacesException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

class FaceClassifierManagerTest {

    @Mock
    private ModelDao modelDao;

    @Mock
    private FaceDao faceDao;

    @Mock
    private FaceClassifierLockManager lockManager;

    @Mock
    private ApplicationContext context;

    @InjectMocks
    private FaceClassifierManager manager;

    private static final String MODEL_KEY = "modelKey";

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void saveClassifier() {
        //TODO: test
    }

    @Test
    void removeFaceClassifier() {
        //TODO: test
    }

    @Test
    void initNewClassifier() {
        val adapterMock = mock(FaceClassifierAdapter.class);
        val faceList = mock(EmbeddingFaceList.class);

        when(faceDao.countFacesInModel(MODEL_KEY)).thenReturn(nextInt());
        when(faceDao.findAllFaceEmbeddingsByApiKey(MODEL_KEY)).thenReturn(faceList);
        when(context.getBean(FaceClassifierAdapter.class)).thenReturn(adapterMock);

        manager.initNewClassifier(MODEL_KEY);

        val inOrder = inOrder(faceDao, lockManager, context, adapterMock);
        inOrder.verify(faceDao).countFacesInModel(MODEL_KEY);
        inOrder.verify(lockManager).lock(MODEL_KEY);
        inOrder.verify(context).getBean(FaceClassifierAdapter.class);
        inOrder.verify(faceDao).findAllFaceEmbeddingsByApiKey(MODEL_KEY);
        inOrder.verify(adapterMock).train(faceList, MODEL_KEY);

        verifyNoMoreInteractions(faceDao, lockManager, context, adapterMock);
        verifyNoInteractions(modelDao);
    }

    @Test
    void initNewClassifierIfNoFaces() {
        when(faceDao.countFacesInModel(MODEL_KEY)).thenReturn(0);

        assertThrows(ModelHasNoFacesException.class, () -> manager.initNewClassifier(MODEL_KEY));
    }

    @Test
    void abortClassifierTraining() {
        //TODO: test
    }

    @Test
    void isTraining() {
        //TODO: test
    }
}