/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.component.migration;

import com.exadel.frs.commonservice.entity.Face.Embedding;
import com.exadel.frs.commonservice.entity.Image;
import com.exadel.frs.commonservice.repository.FacesRepository;
import com.exadel.frs.core.trainservice.repository.ImagesRepository;
import com.exadel.frs.core.trainservice.sdk.faces.feign.FacesFeignClient;
import com.exadel.frs.core.trainservice.util.MultipartFileData;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static com.exadel.frs.core.trainservice.system.global.Constants.CALCULATOR_PLUGIN;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationComponent {

    private final MigrationStatusStorage migrationStatusStorage;
    private final FacesRepository facesRepository;
    private final ImagesRepository imagesRepository;
    private final FacesFeignClient feignClient;

    private final JdbcTemplate jdbcTemplate;
    private final SubjectMigrationService subjectMigrationService;


    @SneakyThrows
    @Async
    public void migrate() {
        try {
            log.info("Migrating...");
            processFaces();
            log.info("Calculating embedding for faces finished");

            log.info("Migration successfully finished");
        } catch (Exception e) {
            log.info("Migration finished with exception");
            throw e;
        } finally {
            migrationStatusStorage.finishMigration();
        }
    }

    @SneakyThrows
    @Async
    public void migrateSubject() {
        try {
            log.info("Migrating face/image --> subject/img...");
            processSubjects();
            log.info("Migration successfully finished");
        } catch (Exception e) {
            log.info("Migration finished with exception");
            throw e;
        } finally {
            migrationStatusStorage.finishMigration();
        }
    }

    private void processSubjects() {
        final String sql = "select " +
                "   f.id as face_id, " +
                "   f.face_name, " +
                "   f.api_key, " +
                "   case when raw_img_fs is null then false else true end as has_image "+
                "from " +
                "   face f inner join image i on f.id = i.face_id " +
                "where " +
                "   migrated = ? and face_name = ?";


        // just as wrapper to bypass immutable variables inside closure
        final var counter = new AtomicInteger(0);

        long start = System.currentTimeMillis();
        jdbcTemplate.query(sql, new Object[]{false, "lisan"}, rs -> {
            final var apiKey = rs.getString("api_key");
            final var faceId = rs.getString("face_id");
            final var faceName = rs.getString("face_name");
            final var hasImage = rs.getBoolean("has_image");

            subjectMigrationService.doFaceMigrationInTransaction(apiKey, faceId, faceName, hasImage);
            counter.incrementAndGet();

            log.debug("{} face(s) done", counter.get());
        });

        log.info("Total records: {} in {}ms", counter.get(), (System.currentTimeMillis() - start));
    }

    private void processFaces() {
        val migrationCalculatorVersion = feignClient.getStatus().getCalculatorVersion();
        log.info("Calculating embedding for faces");
        val all = facesRepository.findAll();
        for (val face : all) {
            log.info("Processing facename {} with id {}", face.getFaceName(), face.getId());
            if (!migrationCalculatorVersion.equals(face.getEmbedding().getCalculatorVersion())) {
                val image = imagesRepository.findById(face.getId()).orElse(new Image());
                if (image.getRawImg() == null) {
                    continue;
                }
                val file = new MultipartFileData(image.getRawImg(), face.getFaceName(), null);

                val faceEmbedding = new Embedding();
                try {
                    val findFacesResponse = feignClient.findFaces(file, 1, null, CALCULATOR_PLUGIN);
                    val embeddings = findFacesResponse.getResult().stream()
                            .findFirst().orElseThrow()
                            .getEmbedding();
                    faceEmbedding.setEmbeddings(Arrays.asList(embeddings));
                    faceEmbedding.setCalculatorVersion(findFacesResponse.getPluginsVersions().getCalculator());
                } catch (FeignException.InternalServerError | FeignException.BadRequest error) {
                    log.error("Error during processing facename {} with id {}", face.getFaceName(), face.getId(), error);
                }

                face.setEmbedding(faceEmbedding);
                facesRepository.save(face);
            }
        }
    }
}