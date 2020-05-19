package com.exadel.frs.core.trainservice.component.migration;

import static java.util.stream.Collectors.*;
import com.exadel.frs.core.trainservice.component.FaceClassifierLockManager;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import com.exadel.frs.core.trainservice.entity.mongo.Face;
import com.exadel.frs.core.trainservice.repository.mongo.FacesRepository;
import com.exadel.frs.core.trainservice.system.feign.FeignClientFactory;
import com.exadel.frs.core.trainservice.system.feign.python.FacesClient;
import com.exadel.frs.core.trainservice.util.MultipartFileData;
import feign.FeignException;
import java.io.IOException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
        waitUntilModelsFinishTraining();
        log.info("Models finished training");

        try {
            log.info("Migrating...");
            processFaces(url);
            log.info("Calculating embedding for faces finished");

            log.info("Retraining models");
            processModels();
            log.info("Retraining models finished");

            log.info("Migration successfully finished");
        } catch (Exception e) {
            log.info("Migration finished with exception");
            throw e;
        } finally {
            migrationStatusStorage.finishMigration();
        }
    }

    private void processModels() {
        val models = modelDao.findAllWithoutClassifier();
        for (val model : models) {
            log.info("Retraining model {}", model.getId());
            val faces = model.getFaces();
            if (faces == null || faces.isEmpty()) {
                faceManager.initNewClassifier(model.getId());
            } else {
                faceManager.initNewClassifier(
                        model.getId(),
                        faces.stream()
                             .map(ObjectId::toString)
                             .collect(toList())
                );
            }
        }
    }

    private void processFaces(String url) throws IOException {
        val migrationServerFeignClient = feignClientFactory.getFeignClient(FacesClient.class, url);

        val migrationCalculatorVersion = migrationServerFeignClient.getStatus().getCalculatorVersion();

        log.info("Calculating embedding for faces");
        val all = facesRepository.findAll();
        for (val face : all) {
            log.info("Processing facename {} with id {}", face.getFaceName(), face.getId());
            val count = face.getEmbeddings().stream()
                            .filter(embedding -> migrationCalculatorVersion.equals(embedding.getCalculatorVersion()))
                            .count();
            if (count == face.getEmbeddings().size()) {
                continue;
            } else {
                val one = gridFsOperations.findOne(new Query(Criteria.where("_id").is(face.getRawImgId())));
                if (one == null) {
                    continue;
                }
                val fsResource = gridFsOperations.getResource(one);
                val file = new MultipartFileData(IOUtils.toByteArray(fsResource.getInputStream()),
                        face.getFaceName(), null
                );

                try {
                    val scanResponse = migrationServerFeignClient.scanFaces(file, 1, null);

                    val embeddings = scanResponse.getResult().stream()
                                                 .findFirst().orElseThrow()
                                                 .getEmbedding();
                    val faceEmbeddings = new Face.Embedding(embeddings, scanResponse.getCalculatorVersion());
                    face.getEmbeddings().clear();
                    face.getEmbeddings().add(faceEmbeddings);
                    facesRepository.save(face);
                } catch (FeignException.InternalServerError | FeignException.BadRequest error) {
                    log.error("{} during processing facename {} with id {}", error.toString(), face.getFaceName(), face.getId());
                    face.getEmbeddings().clear();
                    facesRepository.save(face);
                }
            }
        }
    }

    private void waitUntilModelsFinishTraining() {
        try {
            lockManager.getCountDownLatch().await();
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }
}
