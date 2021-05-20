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

import com.exadel.frs.commonservice.sdk.faces.feign.FacesFeignClient;
import com.exadel.frs.core.trainservice.service.EmbeddingService;
import com.exadel.frs.core.trainservice.util.MultipartFileData;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static com.exadel.frs.core.trainservice.system.global.Constants.CALCULATOR_PLUGIN;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationComponent {

    private final MigrationStatusStorage migrationStatusStorage;
    private final FacesFeignClient feignClient;
    private final EmbeddingService embeddingService;

    @SneakyThrows
    @Async
    public void migrate() {
        try {
            log.info("Migrating...");
            recalculateEmbeddingsWithOutdatedCalculator();
            log.info("Calculating embedding for faces finished");

            log.info("Migration successfully finished");
        } catch (Exception e) {
            log.info("Migration finished with exception");
            throw e;
        } finally {
            migrationStatusStorage.finishMigration();
        }
    }

    int recalculateEmbeddingsWithOutdatedCalculator() {
        var currentCalculator = feignClient.getStatus().getCalculatorVersion();
        log.info("Embeddings migration for calculator version {}", currentCalculator);

        var toMigrate = embeddingService.getWithImgAndCalculatorNotEq(currentCalculator);
        log.info("Found {} embeddings to migrate", toMigrate.size());

        var recalculated = 0;
        for (var embedding : toMigrate) {
            log.info("Migrating embedding with id {}", embedding.getId());

            final Optional<double[]> newEmbedding = embeddingService.getImg(embedding)
                    .flatMap(img -> recalculate(embedding.getId(), img.getContent()));

            if (newEmbedding.isPresent()) {
                int updated = embeddingService.updateEmbedding(embedding.getId(), newEmbedding.get(), currentCalculator);
                recalculated += updated;
            }
        }

        return recalculated;
    }

    private Optional<double[]> recalculate(UUID embeddingId, byte[] content) {
        try {
            var findFacesResponse = feignClient.findFaces(
                    new MultipartFileData(content, "recalculated", null),
                    1,
                    null,
                    CALCULATOR_PLUGIN
            );

            return findFacesResponse.getResult().stream()
                    .findFirst()
                    .map(result -> ArrayUtils.toPrimitive(result.getEmbedding()));

        } catch (FeignException.InternalServerError | FeignException.BadRequest error) {
            log.error("Error during processing embedding with id " + embeddingId, error);
        }

        return Optional.empty();
    }
}