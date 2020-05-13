package com.exadel.frs.core.trainservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.exadel.frs.core.trainservice.entity.mongo.Face;
import com.exadel.frs.core.trainservice.repository.mongo.FacesRepository;
import com.exadel.frs.core.trainservice.system.feign.python.FacesClient;
import com.exadel.frs.core.trainservice.system.feign.python.ScanResponse;
import com.exadel.frs.core.trainservice.system.feign.python.ScanResult;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.val;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.mock.web.MockMultipartFile;

class ScanServiceImplTest {

    @Test
    void scanAndSaveFace() throws IOException {
        val scanFacesClient = mock(FacesClient.class);
        val facesRepository = mock(FacesRepository.class);
        val gridFsOperations = mock(GridFsOperations.class);

        val scanResult = new ScanResult().setEmbedding(List.of(100500D));
        val scanResponse = new ScanResponse().setResult(List.of(scanResult));
        val mockFile = new MockMultipartFile("mockFile", "".getBytes());
        val faceName = "faceName";
        val modelKey = "modelKey";
        val threshold = 1.0D;
        val faceId = new ObjectId("507f1f77bcf86cd799439011");

        when(scanFacesClient.scanFaces(mockFile, 1, threshold))
                .thenReturn(scanResponse);

        when(gridFsOperations.store(any(InputStream.class), anyString())).thenReturn(faceId);

        val actual = new ScanServiceImpl(scanFacesClient, facesRepository, gridFsOperations)
                .scanAndSaveFace(mockFile, faceName, threshold, modelKey);

        assertThat(actual).isNotNull();
        assertThat(actual.getFaceName()).isEqualTo(faceName);
        assertThat(actual.getApiKey()).isEqualTo(modelKey);
        assertThat(actual.getFaceImgId()).isEqualTo(faceId);
        assertThat(actual.getRawImgId()).isEqualTo(faceId);
        assertThat(actual.getEmbeddings()).allSatisfy(
                embedding -> assertThat(embedding.getEmbedding()).isEqualTo(scanResult.getEmbedding())
        );

        verify(scanFacesClient).scanFaces(mockFile, 1, threshold);
        verify(gridFsOperations).store(any(InputStream.class), anyString());
        verify(facesRepository).save(any(Face.class));
        verifyNoMoreInteractions(scanFacesClient, gridFsOperations, facesRepository);
    }
}