package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.core.trainservice.DbHelper;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.system.global.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingRepositoryTest extends EmbeddedPostgreSQLTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    EmbeddingRepository embeddingRepository;

    @Test
    void testFindBySubjectId() {
        final Subject subject = dbHelper.insertSubject("subject_name");
        final Embedding embedding1 = dbHelper.insertEmbeddingNoImg(subject);
        final Embedding embedding2 = dbHelper.insertEmbeddingWithImg(subject);

        final List<Embedding> result = embeddingRepository.findBySubjectId(subject.getId());

        assertThat(result)
                .hasSize(2)
                .contains(embedding1, embedding2);
    }

    @Test
    void testGetWithImgAndCalculatorNotEq() {
        var calc = Constants.FACENET2018;
        var subject = dbHelper.insertSubject("subject_name");

        // no image
        dbHelper.insertEmbeddingNoImg(subject, calc);
        // with image, wrong calc
        var embedding = dbHelper.insertEmbeddingWithImg(subject, calc + "any");
        // with image, correct calc
        dbHelper.insertEmbeddingWithImg(subject, calc);

        final List<Embedding> list = embeddingRepository.getWithImgAndCalculatorNotEq(calc);
        assertThat(list.size()).isPositive(); // we've polluted DB with previous tests, so we couldn't do exact count
    }

    @Test
    @Transactional
    void testUpdateEmbedding() {
        var subject = dbHelper.insertSubject("subject_name");
        var embedding = dbHelper.insertEmbeddingNoImg(subject, "calculator", new double[]{1.1, 2.2, 3.3});

        var newCalc = "new_calc";
        var newEmbedding = new double[]{4.4, 5.5, 6.6};

        int updated = embeddingRepository.updateEmbedding(embedding.getId(), newEmbedding, newCalc);
        assertThat(updated).isEqualTo(1);
    }

    @Test
    @Transactional
    void testDeleteBySubjectId() {
        var subject = dbHelper.insertSubject("subject_name");
        dbHelper.insertEmbeddingNoImg(subject);
        dbHelper.insertEmbeddingWithImg(subject);

        int deleted = embeddingRepository.deleteBySubjectId(subject.getId());
        assertThat(deleted).isEqualTo(2);
    }

    @Test
    @Transactional
    void testDeleteBySubjectApiKey() {
        var model = dbHelper.insertModel();

        var subject1 = dbHelper.insertSubject(model, "subject_name1");
        dbHelper.insertEmbeddingNoImg(subject1);
        dbHelper.insertEmbeddingWithImg(subject1);

        var subject2 = dbHelper.insertSubject(model, "subject_name2");
        dbHelper.insertEmbeddingNoImg(subject2);
        dbHelper.insertEmbeddingWithImg(subject2);

        int deleted = embeddingRepository.deleteBySubjectApiKey(model.getApiKey());
        assertThat(deleted).isEqualTo(4);
    }

    @Test
    @Transactional
    void testReassignEmbeddings() {
        var model = dbHelper.insertModel();

        var subject1 = dbHelper.insertSubject(model, "subject_name1");
        dbHelper.insertEmbeddingNoImg(subject1);
        dbHelper.insertEmbeddingWithImg(subject1);
        dbHelper.insertEmbeddingWithImg(subject1);

        var subject2 = dbHelper.insertSubject(model, "subject_name2");
        dbHelper.insertEmbeddingNoImg(subject2);
        dbHelper.insertEmbeddingWithImg(subject2);

        int reassigned = embeddingRepository.reassignEmbeddings(subject1, subject2);
        assertThat(reassigned).isEqualTo(3);
        assertThat(embeddingRepository.findBySubjectId(subject1.getId())).isEmpty();
        assertThat(embeddingRepository.findBySubjectId(subject2.getId())).hasSize(5);
    }

    @Test
    void testFindBySubjectApiKey() {
        var model = dbHelper.insertModel();

        var subject1 = dbHelper.insertSubject(model, "subject_name1");
        dbHelper.insertEmbeddingNoImg(subject1);
        dbHelper.insertEmbeddingWithImg(subject1);
        dbHelper.insertEmbeddingWithImg(subject1);

        var subject2 = dbHelper.insertSubject(model, "subject_name2");
        dbHelper.insertEmbeddingNoImg(subject2);
        dbHelper.insertEmbeddingWithImg(subject2);

        var page = embeddingRepository.findBySubjectApiKey(model.getApiKey(), Pageable.unpaged());
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent().stream().filter(p -> p.subjectName().equals(subject1.getSubjectName())).count()).isEqualTo(3);
        assertThat(page.getContent().stream().filter(p -> p.subjectName().equals(subject2.getSubjectName())).count()).isEqualTo(2);
    }

    @Test
    void testGetUniqueCalculators() {
        int cnt = 3;
        var calculators = IntStream.range(0, cnt)
                .mapToObj(i -> "just_inserted_calculators" + i)
                .peek(calculator -> dbHelper.insertEmbeddingNoImg(dbHelper.insertSubject("subject"), calculator))
                .collect(Collectors.toList());

        assertThat(embeddingRepository.getUniqueCalculators()).containsAll(calculators);
    }

    @Test
    void testCountBySubjectApiKeyAndCalculatorNotEq() {
        final Long count = embeddingRepository.countBySubjectApiKeyAndCalculatorNotEq(Constants.DEMO_API_KEY, "fakecalc");
        assertThat(count).isEqualTo(20); // demo pre-inserted records
    }

    @Test
    void testCountBySubjectApiKeyNotEqAndCalculatorNotEq() {
        dbHelper.insertEmbeddingWithImg(dbHelper.insertSubject("subject"), Constants.FACENET2018);

        final Subject subject = dbHelper.insertSubject("subject");
        dbHelper.insertEmbeddingWithImg(subject, "calc");
        dbHelper.insertEmbeddingWithImg(subject, Constants.FACENET2018);

        final Long count = embeddingRepository.countBySubjectApiKeyNotEqAndCalculatorNotEq(Constants.DEMO_API_KEY, Constants.FACENET2018);
        assertThat(count).isPositive(); // we've polluted DB with previous tests, so we couldn't do exact count
    }
}