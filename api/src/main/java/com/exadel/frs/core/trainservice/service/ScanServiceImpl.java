package com.exadel.frs.core.trainservice.service;

import static java.util.stream.Collectors.toList;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.entity.mongo.Face;
import com.exadel.frs.core.trainservice.exception.TooManyFacesException;
import com.exadel.frs.core.trainservice.system.feign.python.FacesClient;
import java.io.IOException;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ScanServiceImpl implements ScanService {

    public static final int MAX_FACES_TO_SAVE = 1;
    public static final int MAX_FACES_TO_RECOGNIZE = 2;

    private final FacesClient facesClient;
    private final FaceDao faceDao;

    @Override
    public Face scanAndSaveFace(
            final MultipartFile file,
            final String faceName,
            final Double detProbThreshold,
            final String modelKey
    ) throws IOException {
        val scanResponse = facesClient.scanFaces(file, MAX_FACES_TO_RECOGNIZE, detProbThreshold);
        val result = scanResponse.getResult();

        if (result.size() > MAX_FACES_TO_SAVE) {
            throw new TooManyFacesException();
        }

        val embedding = result.stream()
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