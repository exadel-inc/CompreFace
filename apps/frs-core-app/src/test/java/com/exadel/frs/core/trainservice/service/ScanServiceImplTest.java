package com.exadel.frs.core.trainservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.system.feign.FacesClient;
import com.exadel.frs.core.trainservice.system.feign.ScanResponse;
import com.exadel.frs.core.trainservice.system.feign.ScanResult;
import java.io.IOException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ScanServiceImplTest {

    @Test
    void scanAndSaveFace() throws IOException {
        val scanFacesClient = mock(FacesClient.class);
        val faceDao = mock(FaceDao.class);
        val mockFile = new MockMultipartFile("mockFile", "".getBytes());

        val scanResult = new ScanResult().setEmbedding(List.of(100500D));
        val scanResponse = new ScanResponse().setResult(List.of(scanResult));
        val embeddings = List.of(new Face.Embedding(List.of(100500D), null));
        val faceName = "faceName";
        val modelKey = "modelKey";
        val threshold = 1.0D;
        val face = new Face();

        when(scanFacesClient.scanFaces(mockFile, 1, threshold))
                .thenReturn(scanResponse);

        when(faceDao.addNewFace(embeddings, mockFile, faceName, modelKey)).thenReturn(face);

        val actual = new ScanServiceImpl(scanFacesClient, faceDao)
                .scanAndSaveFace(mockFile, faceName, threshold, modelKey);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(face);

        verify(scanFacesClient).scanFaces(mockFile, 1, threshold);
        verify(faceDao).addNewFace(embeddings, mockFile, faceName, modelKey);
        verifyNoMoreInteractions(scanFacesClient, faceDao);
    }
}