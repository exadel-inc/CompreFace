package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.service.ScanServiceImpl.MAX_FACES_TO_RECOGNIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.entity.mongo.Face;
import com.exadel.frs.core.trainservice.exception.TooManyFacesException;
import com.exadel.frs.core.trainservice.system.feign.python.FacesClient;
import com.exadel.frs.core.trainservice.system.feign.python.ScanResponse;
import com.exadel.frs.core.trainservice.system.feign.python.ScanResult;
import java.io.IOException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

class ScanServiceImplTest {

    @Mock
    private FacesClient scanFacesClient;

    @Mock
    private FaceDao faceDao;

    @Mock
    private MockMultipartFile mockFile;

    @InjectMocks
    private ScanServiceImpl scanService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    private static final String FACE_NAME = "faceName";
    private static final String MODEL_KEY = "modelKey";
    private static final double THRESHOLD = 1.0;
    private static final double EMBEDDING = 100500;
    private static final ScanResult SCAN_RESULT = new ScanResult().setEmbedding(List.of(EMBEDDING));

    @Test
    void scanAndSaveFace() throws IOException {
        val scanResponse = new ScanResponse().setResult(List.of(SCAN_RESULT));
        val embeddings = List.of(new Face.Embedding(List.of(EMBEDDING), null));
        val face = new Face();

        when(scanFacesClient.scanFaces(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD))
                .thenReturn(scanResponse);

        when(faceDao.addNewFace(embeddings, mockFile, FACE_NAME, MODEL_KEY)).thenReturn(face);

        val actual = scanService.scanAndSaveFace(mockFile, FACE_NAME, THRESHOLD, MODEL_KEY);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(face);

        verify(scanFacesClient).scanFaces(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD);
        verify(faceDao).addNewFace(embeddings, mockFile, FACE_NAME, MODEL_KEY);
        verifyNoMoreInteractions(scanFacesClient, faceDao);
    }

    @Test
    void tooManyFacesScan() {
        val scanResponse = new ScanResponse().setResult(List.of(SCAN_RESULT, SCAN_RESULT));

        when(scanFacesClient.scanFaces(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD))
                .thenReturn(scanResponse);

        assertThrows(
                TooManyFacesException.class,
                () -> scanService.scanAndSaveFace(mockFile, FACE_NAME, THRESHOLD, MODEL_KEY)
        );

        verify(scanFacesClient).scanFaces(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD);
        verifyNoInteractions(faceDao);
        verifyNoMoreInteractions(scanFacesClient);
    }
}