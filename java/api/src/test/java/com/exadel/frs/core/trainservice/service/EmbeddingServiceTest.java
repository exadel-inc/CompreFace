package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.entity.*;
import com.exadel.frs.core.trainservice.DbHelper;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.dao.SubjectDao;
import com.exadel.frs.core.trainservice.system.global.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class EmbeddingServiceTest extends EmbeddedPostgreSQLTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    SubjectDao subjectDao;

    @Autowired
    EmbeddingService embeddingService;

    @Test
    void testListEmbeddings() {
        final Model model = dbHelper.insertModel();

        int count = 10;
        for (int i = 0; i < count; i++) {
            dbHelper.insertEmbeddingNoImg(dbHelper.insertSubject(model, "subject" + i));
        }

        // new EmbeddingInfo("calc", new double[]{1.0, 2.0}, img())

        var size = 5;
        final Page<EmbeddingProjection> page = embeddingService.listEmbeddings(model.getApiKey(), null, PageRequest.of(0, size));

        assertThat(page.getTotalElements(), is((long) count));
        assertThat(page.getTotalPages(), is(count / size));
        assertThat(page.getSize(), is(size));
    }

    @Test
    void testListEmbeddingsWithSubjectName() {
        final Model model = dbHelper.insertModel();

        int count = 1;
        var subjectName = "Johnny Depp";
        dbHelper.insertEmbeddingNoImg(dbHelper.insertSubject(model, subjectName));
        dbHelper.insertEmbeddingNoImg(dbHelper.insertSubject(model, "Not Johnny Depp"));

        var size = 1;
        final Page<EmbeddingProjection> page = embeddingService.listEmbeddings(model.getApiKey(), subjectName, PageRequest.of(0, size));

        assertThat(page.getTotalElements(), is((long) count));
        assertThat(page.getTotalPages(), is(count / size));
        assertThat(page.getSize(), is(size));
    }

    @Test
    void testCountEmbeddingsByApiKeyAndCalculatorNotEq() {
        var currentCalc = "Facenet2018";

        var correctCalcEmbedding = dbHelper.insertEmbeddingNoImg(
                dbHelper.insertSubject(Constants.DEMO_API_KEY, "subject1"),
                currentCalc
        );

        var wrongCalcEmbedding = dbHelper.insertEmbeddingNoImg(
                dbHelper.insertSubject(Constants.DEMO_API_KEY, "subject2"),
                currentCalc + "somegarbage"
        );

        assertThat(embeddingService.isDemoCollectionInconsistent(), is(true));

        deleteSubject(wrongCalcEmbedding, correctCalcEmbedding);
    }

    @Test
    void testIsDbInconsistent() {
        var currentCalc = "currentCalc";

        var model = dbHelper.insertModel();

        var embeddings = new Embedding[]{
                // incorrect Demo Subject
                dbHelper.insertEmbeddingWithImg(
                        dbHelper.insertSubject(Constants.DEMO_API_KEY, "demo_subject_1"),
                        currentCalc + "somegarbage"
                ),
                // correct Demo Subject
                dbHelper.insertEmbeddingWithImg(
                        dbHelper.insertSubject(Constants.DEMO_API_KEY, "demo_subject_2"),
                        currentCalc
                ),
                // incorrect Regular Subject <<<<<<< this one should cause issue
                dbHelper.insertEmbeddingWithImg(
                        dbHelper.insertSubject(model, "regular_subject_1"),
                        currentCalc + "somegarbage"
                ),
                // correct Regular Subject
                dbHelper.insertEmbeddingWithImg(
                        dbHelper.insertSubject(model, "regular_subject_2"),
                        currentCalc
                )
        };

        assertThat(embeddingService.isDbInconsistent(currentCalc), is(true));

        deleteSubject(embeddings);
    }

    private void deleteSubject(Embedding... embeddings) {
        for (Embedding embedding : embeddings) {
            final Subject subject = embedding.getSubject();
            assertThat(subject, notNullValue());

            final Subject deleted = subjectDao.deleteSubjectByName(
                    subject.getApiKey(),
                    subject.getSubjectName()
            );

            assertThat(deleted.getId(), is(subject.getId()));
        }
    }

    @Test
    void testGetImg() {
        var model = dbHelper.insertModel();
        var subject = dbHelper.insertSubject(model, "subject");
        final Embedding embedding = dbHelper.insertEmbeddingWithImg(subject);

        final Optional<Img> img = embeddingService.getImg(subject.getApiKey(), embedding.getId());
        assertThat(img.isPresent(), is(true));
        assertThat(img.get().getContent(), is(embedding.getImg().getContent()));
    }
}