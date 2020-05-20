package com.exadel.frs.core.trainservice.service;

import static java.util.stream.Collectors.toList;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.system.feign.FacesClient;
import java.io.IOException;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ScanServiceImpl implements ScanService {

    private static final int FACE_LIMIT = 2;

    private final FacesClient facesClient;
    private final FaceDao faceDao;

    @Override
    public Face scanAndSaveFace(
            final MultipartFile file,
            final String faceName,
            final Double detProbThreshold,
            final String modelKey
    ) throws IOException {
        val scanResponse = facesClient.scanFaces(file, FACE_LIMIT, detProbThreshold);

        val embedding = scanResponse.getResult().stream()
                                    .findFirst().orElseThrow()
                                    .getEmbedding();

        val embeddingToSave = Stream.of(
                new Face.Embedding()
                        .setEmbedding(embedding)
                        .setCalculatorVersion(scanResponse.getCalculatorVersion())
        ).collect(toList());

        return faceDao.addNewFace(embeddingToSave, file, faceName, modelKey);
    }
}