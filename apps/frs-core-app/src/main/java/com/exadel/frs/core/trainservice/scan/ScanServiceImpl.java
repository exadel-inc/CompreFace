package com.exadel.frs.core.trainservice.scan;

import com.exadel.frs.core.trainservice.domain.Face;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ScanServiceImpl implements ScanService {
    private final PythonClient pythonClient;
    private final FacesRepository facesRepository;

    @Override
    public void scanAndSaveFace(MultipartFile file, String faceName, Double detProbThreshold, String xApiKey) {
        //call to python
        //In process discussing add faceImgId,rawImgId,modelName from python server
        var scanResponse = pythonClient.scanFaces(file, 1, detProbThreshold);
        var embedding = scanResponse.getResult().stream()
                .findFirst().orElseThrow()
                .getEmbedding();
        var embeddingToSave = Stream.of(
                new Face.Embedding()
                        .setEmbedding(embedding)
        ).collect(Collectors.toList());
        //save result
        System.out.println(scanResponse);
        var face = new Face()
                .setEmbeddings(embeddingToSave)
                .setFaceName(faceName)
//                .setFaceImgId()
                .setApiKey(xApiKey);
//                .setRawImgId();
        facesRepository.save(face);
    }
}
