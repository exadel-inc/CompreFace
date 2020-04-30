package com.exadel.frs.core.trainservice.component.migration;

import com.exadel.frs.core.trainservice.component.FaceClassifierLockManager;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.entity.Model;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.exadel.frs.core.trainservice.system.feign.FacesClient;
import com.exadel.frs.core.trainservice.system.feign.FeignClientFactory;
import com.exadel.frs.core.trainservice.system.feign.ScanResponse;
import com.exadel.frs.core.trainservice.util.MultipartFileData;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationComponent {

    private final FeignClientFactory feignClientFactory;
    private final FaceClassifierLockManager lockManager;
    private final FaceClassifierManager faceManager;
    private final MigrationStatusStorage migrationStatusStorage;
    private final FacesRepository facesRepository;
    private final GridFsOperations gridFsOperations;
    private final ModelDao modelDao;

    @SneakyThrows
    @Async
    public void migrate(String url) {
        log.info("Waiting till current models finish training");
        try {
            lockManager.getCountDownLatch().await();
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
        log.info("Models finished training");

        log.info("Migrating...");
        try {
            FacesClient migrationServerFeignClient = feignClientFactory.getFeignClient(FacesClient.class, url);

            String migrationCalculatorVersion = migrationServerFeignClient.getStatus().getCalculatorVersion();

            log.info("Calculating embedding for faces");
            List<Face> all = facesRepository.findAll();
            for (Face face : all) {
                if (face.getEmbeddings().stream()
                        .anyMatch(embedding -> migrationCalculatorVersion.equals(embedding.getCalculatorVersion()))) {
                    continue;
                } else {
                    GridFSFile one = gridFsOperations.findOne(new Query(Criteria.where("_id").is(face.getFaceImgId())));
                    if (one == null) {
                        continue;
                    }
                    GridFsResource fsResource = gridFsOperations.getResource(one);
                    MultipartFile file = new MultipartFileData(IOUtils.toByteArray(fsResource.getInputStream()),
                            face.getFaceName(), null);

                    ScanResponse scanResponse = migrationServerFeignClient.scanFaces(file, 1, null);

                    List<Double> embeddings = scanResponse.getResult().stream()
                            .findFirst().orElseThrow()
                            .getEmbedding();
                    Face.Embedding faceEmbeddings = new Face.Embedding(embeddings, scanResponse.getCalculatorVersion());
                    face.getEmbeddings().clear();
                    face.getEmbeddings().add(faceEmbeddings);
                    facesRepository.save(face);
                }
            }
            log.info("Calculating embedding for faces finished");

            log.info("Retraining models");

            List<Model> models = modelDao.findAll();
            for (val model : models) {
                List<ObjectId> faces = model.getFaces();
                if (faces == null || faces.isEmpty()) {
                    faceManager.initNewClassifier(model.getId());
                } else {
                    faceManager.initNewClassifier(model.getId(),
                            faces.stream().map(ObjectId::toString).collect(Collectors.toList()));
                }
            }

            log.info("Retraining models finished");

            log.info("Migration successfully finished");
        } catch (Exception e) {
            log.info("Migration finished with exception");
            throw e;
        } finally {
            migrationStatusStorage.finishMigration();
        }
    }

}
