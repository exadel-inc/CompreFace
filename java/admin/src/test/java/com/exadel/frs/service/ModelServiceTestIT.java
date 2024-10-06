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

package com.exadel.frs.service;

import com.exadel.frs.DbHelper;
import com.exadel.frs.EmbeddedPostgreSQLTest;
import com.exadel.frs.commonservice.projection.EmbeddingProjection;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ModelServiceTestIT extends EmbeddedPostgreSQLTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    EmbeddingRepository embeddingRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    ImgRepository imgRepository;

    @Autowired
    ModelService modelService;

    @Test
    @Transactional
    void testGetSummarizedByDayModelStatistics() {
        var user = dbHelper.insertUser("john@gmail.com");
        var model = dbHelper.insertModel();
        var app = model.getApp();

        var statisticsBefore = modelService.getSummarizedByDayModelStatistics(app.getGuid(), model.getGuid(), user.getId());

        dbHelper.insertModelStatistic(3, now(UTC).minusMonths(1), model);
        dbHelper.insertModelStatistic(5, now(UTC).minusMonths(1), model);
        dbHelper.insertModelStatistic(8, now(UTC).minusMonths(7), model);
        dbHelper.insertModelStatistic(4, now(UTC).minusMonths(4), model);
        dbHelper.insertModelStatistic(9, now(UTC).plusMonths(1), model);

        var statisticsAfter = modelService.getSummarizedByDayModelStatistics(app.getGuid(), model.getGuid(), user.getId());

        assertThat(statisticsBefore).isEmpty();
        assertThat(statisticsAfter).hasSize(2);

        assertThat(statisticsAfter.get(0).requestCount()).isEqualTo(8);
        assertThat(statisticsAfter.get(1).requestCount()).isEqualTo(4);
    }

    @Test
    void testCloneSubjects() {
        final Model model = dbHelper.insertModel();

        int count = 5;
        for (int i = 0; i < count; i++) {
            var subject = dbHelper.insertSubject(model, "subject" + i);
            dbHelper.insertEmbeddingWithImg(subject);
        }

        var newApiKey = UUID.randomUUID().toString();
        modelService.cloneSubjects(model.getApiKey(), newApiKey);

        // check subjects
        compareSubjects(
                subjectRepository.findByApiKey(model.getApiKey()),
                subjectRepository.findByApiKey(newApiKey)
        );

        // check embeddings
        compareEmbeddings(
                embeddingRepository.findBySubjectApiKey(model.getApiKey(), Pageable.unpaged()).getContent(),
                embeddingRepository.findBySubjectApiKey(newApiKey, Pageable.unpaged()).getContent()
        );
    }

    private void compareEmbeddings(List<EmbeddingProjection> originals, List<EmbeddingProjection> clones) {
        assertThat(originals).isNotNull();
        assertThat(clones).isNotNull();

        assertThat(originals.size()).isEqualTo(clones.size());
        // compare subject names
        assertThat(originals.stream().map(EmbeddingProjection::subjectName))
                .containsExactlyInAnyOrder(originals.stream().map(EmbeddingProjection::subjectName).toArray(String[]::new));
    }

    private static void compareSubjects(List<Subject> originals, List<Subject> clones) {
        assertThat(originals).isNotNull();
        assertThat(clones).isNotNull();

        assertThat(originals.size()).isEqualTo(clones.size());
        // compare subject names
        assertThat(originals.stream().map(Subject::getSubjectName))
                .containsExactlyInAnyOrder(originals.stream().map(Subject::getSubjectName).toArray(String[]::new));
    }
}