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

package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.EmbeddingProjection;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.FacesRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.core.trainservice.cache.*;
import com.exadel.frs.core.trainservice.component.migration.MigrationComponent;
import com.exadel.frs.core.trainservice.component.migration.MigrationStatusStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
@Slf4j
public class MigrateController {

    private final MigrationComponent migrationComponent;
    private final MigrationStatusStorage migrationStatusStorage;

    @PostMapping(value = "/migrate")
    public ResponseEntity<String> migrate() {
        migrationStatusStorage.startMigration();
        migrationComponent.migrate();

        return ResponseEntity.ok("Migration started");
    }

    @PostMapping(value = "/migrate/subject")
    public ResponseEntity<String> migrateSubject() {
        migrationStatusStorage.startMigration();
        migrationComponent.migrateSubject();

        return ResponseEntity.ok("Subject migration started");
    }

    // TODO remove below code later

    @Autowired
    ImgRepository imgRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    EmbeddingRepository embeddingRepository;

    @Autowired
    FacesRepository facesRepository;

    @Autowired
    EmbeddingCacheProvider embeddingCacheProvider;

    @PostMapping(value = "/migrate/compare", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void compare() {
        String apiKey = "001bb7fb-70df-479e-b6a6-0800078ba23a";

        log.debug("Start building...");
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long start = System.currentTimeMillis();
        // TODO
        EmbeddingCollection coll = embeddingCacheProvider.getOrLoad(apiKey);
        long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        log.debug("Build subject collection in {}ms, memory {}MB", (System.currentTimeMillis() - start), (after - before) / 1024 / 1024);

        before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        start = System.currentTimeMillis();
        FaceCollection coll2 = FaceCollection.buildFromFaces(facesRepository.findByApiKey(apiKey));
        after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        log.debug("Build faces collection in {}ms, memory {}MB", (System.currentTimeMillis() - start), (after - before) / 1024 / 1024);

        int index = 0;
        EmbeddingProjection toRemove = null;
        Integer toRemoveIndex = null;

        EmbeddingProjection checkInc = null;
        Integer checkIncIndex = null;
        INDArray checkArray = null;

        log.debug("Trying to compare all values...");
        for (Map.Entry<Integer, EmbeddingProjection> entryOne : coll.getIndexMap().entrySet()) {
            index++;
            Map.Entry<FaceBO, Integer> entryTwo = coll2.getFacesMap().entrySet().stream().filter(e -> e.getKey().getName().equals(entryOne.getValue().getSubjectName())).findFirst().orElseThrow();

            if (index == 117) {
                toRemove = entryOne.getValue();
                toRemoveIndex = entryOne.getKey();
            }
            if (checkInc == null && toRemove != null && entryOne.getKey() > toRemoveIndex) {
                checkInc = entryOne.getValue();
                checkIncIndex = entryOne.getKey();
                checkArray = coll.getEmbeddings().getRow(checkIncIndex);
            }

            double[] oneDouble = coll.getEmbeddings().getRow(entryOne.getKey()).toDoubleVector();
            double[] twoDouble = coll2.getEmbeddings().getRow(entryTwo.getValue()).toDoubleVector();

            compare(oneDouble, twoDouble);
        }

        Subject subject = new Subject();
        subject.setSubjectName("testsubjecttoadd");

        Img img = new Img();
        img.setId(UUID.randomUUID());

        Embedding embedding = new Embedding();
        embedding.setEmbedding(new double[512]);
        embedding.setCalculator("calc");
        embedding.setSubject(subject);
        embedding.setImg(img);

        log.debug("trying to add...");
        coll.addEmbedding(embedding);

        log.debug("trying remove...");
        coll.removeEmbedding(toRemove);
        if (coll.getIndexMap().get(checkIncIndex - 1).equals(checkInc)) {
            throw new IllegalStateException("No index shift after remove!");
        }

        log.debug("trying find by img id...");
        INDArray found = coll.getRawEmbeddingById(checkInc.getEmbeddingId()).orElseThrow(() -> new IllegalStateException("no embedding found by image"));
        compare(found.toDoubleVector(), checkArray.toDoubleVector());

        log.debug("SUCCESS!!!");
    }

    private void compare(double[] oneDouble, double[] twoDouble) {
        if (oneDouble.length != twoDouble.length) {
            throw new IllegalStateException("Wrong size");
        }
        for (int i = 0; i < oneDouble.length; i++) {
            if (oneDouble[i] != twoDouble[i]) {
                throw new IllegalStateException("Uneq on index " + i + ", one: " + oneDouble[i] + ", two" + twoDouble[i]);
            }
        }
    }

    @GetMapping(value = "/migrate/test/get", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getImageById(@RequestParam("id") UUID id, HttpServletResponse response) throws IOException {
        Optional<Img> img = imgRepository.findById(id);
        if (img.isPresent()) {
            IOUtils.write(img.get().getContent(), response.getOutputStream());
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }

    @GetMapping(value = "/migrate/test/get2")
    public ResponseEntity<?> getSubject(@RequestParam("id") UUID id) {
        Optional<Subject> subj = subjectRepository.findById(id);
        if (subj.isPresent()) {
            return ResponseEntity.ok(subj);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}