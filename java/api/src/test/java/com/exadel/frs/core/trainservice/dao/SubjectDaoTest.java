package com.exadel.frs.core.trainservice.dao;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.exception.SubjectAlreadyExistsException;
import com.exadel.frs.commonservice.exception.SubjectNotFoundException;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.core.trainservice.DbHelper;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.dto.EmbeddingInfo;
import com.exadel.frs.core.trainservice.service.NotificationReceiverService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubjectDaoTest extends EmbeddedPostgreSQLTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    SubjectDao subjectDao;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    EmbeddingRepository embeddingRepository;

    @Autowired
    ImgRepository imgRepository;

    @Test
    void testCreateSubjectByNameAlreadyExists() {
        var subjectName = "subject";
        var model = dbHelper.insertModel();

        dbHelper.insertSubject(model, subjectName);

        assertThrows(
                SubjectAlreadyExistsException.class,
                () -> subjectDao.createSubject(model.getApiKey(), subjectName)
        );
    }

    @Test
    void testCreateSubjectByName() {
        var model = dbHelper.insertModel();
        var subject = subjectDao.createSubject(model.getApiKey(), "subject");

        assertThat(subject).isNotNull();
        assertThat(subject.getId()).isNotNull();
        assertThat(subjectRepository.findById(subject.getId())).isNotNull();

    }

    @Test
    void testDeleteSubjectByNameNotFound() {
        assertThrows(
                SubjectNotFoundException.class,
                () -> subjectDao.deleteSubjectByName(UUID.randomUUID().toString(), "anyname")
        );
    }

    @Test
    void testDeleteSubjectByName() {
        var subject = dbHelper.insertSubject("subject");
        var embedding = dbHelper.insertEmbeddingWithImg(subject);

        subjectDao.deleteSubjectByName(subject.getApiKey(), subject.getSubjectName());

        // no embeddings
        assertThat(embeddingRepository.findBySubjectId(subject.getId())).isEmpty();
        // no images
        assertThat(imgRepository.getImgByEmbeddingId(subject.getApiKey(), embedding.getId())).isEmpty();
        // no subject
        assertThat(subjectRepository.findById(subject.getId())).isEmpty();
    }

    @Test
    void testRemoveAllSubjectEmbeddings() {
        var subject = dbHelper.insertSubject("subject");
        dbHelper.insertEmbeddingNoImg(subject);
        var embedding = dbHelper.insertEmbeddingWithImg(subject);

        int removed = subjectDao.removeAllSubjectEmbeddings(subject.getApiKey(), subject.getSubjectName());
        assertThat(removed).isEqualTo(2);

        // no embeddings
        assertThat(embeddingRepository.findBySubjectId(subject.getId())).isEmpty();
        // no images
        assertThat(imgRepository.getImgByEmbeddingId(subject.getApiKey(), embedding.getId())).isEmpty();
        // the subject doesn't exist anymore
        assertThat(subjectRepository.findById(subject.getId())).isEmpty();
    }

    @Test
    void testRemoveSubjectEmbeddingImgDeleted() {
        var subject = dbHelper.insertSubject("subject");
        var img = dbHelper.insertImg();

        // two embeddings referencing same image
        var embedding1 = dbHelper.insertEmbeddingWithImg(subject, "calc1", new double[]{1.1, 2.2}, img);
        var embedding2 = dbHelper.insertEmbeddingWithImg(subject, "calc2", new double[]{2.3, 3.4}, dbHelper.insertImg());

        var removed = subjectDao.removeSubjectEmbedding(subject.getApiKey(), embedding1.getId());
        assertThat(removed).isNotNull();
        assertThat(removed.getId()).isEqualTo(embedding1.getId());

        // embedding2 still exists
        assertThat(embeddingRepository.findById(embedding2.getId())).isPresent();
        // image deleted
        assertThat(imgRepository.findById(img.getId())).isEmpty();
        // subject still exists
        assertThat(subjectRepository.findById(subject.getId())).isPresent();
    }

    @Test
    void testRemoveSubjectEmbeddingImgNotDeleted() {
        var subject = dbHelper.insertSubject("subject");
        var img = dbHelper.insertImg();

        // two embeddings referencing same image
        var embedding1 = dbHelper.insertEmbeddingWithImg(subject, "calc1", new double[]{1.1, 2.2}, img);
        var embedding2 = dbHelper.insertEmbeddingWithImg(subject, "calc2", new double[]{2.3, 3.4}, img);

        var removed = subjectDao.removeSubjectEmbedding(subject.getApiKey(), embedding1.getId());
        assertThat(removed).isNotNull();
        assertThat(removed.getId()).isEqualTo(embedding1.getId());

        // embedding2 still exists
        assertThat(embeddingRepository.findById(embedding2.getId())).isPresent();
        // image still exists
        assertThat(imgRepository.findById(img.getId())).isPresent();
        // subject still exists
        assertThat(subjectRepository.findById(subject.getId())).isPresent();
    }

    @Test
    void testDeleteSubjectsByApiKey() {
        var model = dbHelper.insertModel();
        int count = 3;

        for (int i = 0; i < count; i++) {
            var subject = dbHelper.insertSubject(model, "subject" + i);
            dbHelper.insertEmbeddingWithImg(subject);
        }

        var deleted = subjectDao.deleteSubjectsByApiKey(model.getApiKey());
        assertThat(deleted).isEqualTo(count);
        assertThat(embeddingRepository.findBySubjectApiKey(model.getApiKey(), Pageable.unpaged()).isEmpty()).isTrue();
    }

    @Test
    void testAddEmbeddingNoEmbedding() {
        var model = dbHelper.insertModel();

        final Pair<Subject, Embedding> pair = subjectDao.addEmbedding(
                model.getApiKey(),
                "subject",
                null
        );

        var subject = pair.getLeft();
        var embedding = pair.getRight();

        assertThat(subject).isNotNull();
        assertThat(subject.getId()).isNotNull();
        assertThat(embedding).isNull();
    }

    @Test
    void testAddEmbeddingEmbeddingNoImg() {
        var model = dbHelper.insertModel();

        final Pair<Subject, Embedding> pair = subjectDao.addEmbedding(
                model.getApiKey(),
                "subject",
                new EmbeddingInfo("calc", new double[]{1.1, 5.6}, null)
        );

        var subject = pair.getLeft();
        var embedding = pair.getRight();

        assertThat(subject).isNotNull();
        assertThat(subject.getId()).isNotNull();
        assertThat(embedding).isNotNull();
        assertThat(embedding.getId()).isNotNull();

        // check embedding has been stored
        var embeddings = embeddingRepository.findBySubjectId(subject.getId());
        assertThat(embeddings).hasSize(1);

        var img = imgRepository.getImgByEmbeddingId(subject.getApiKey(), embedding.getId());
        assertThat(img).isEmpty();
    }

    @Test
    void testAddEmbeddingEmbeddingWithImg() {
        var model = dbHelper.insertModel();

        final Pair<Subject, Embedding> pair = subjectDao.addEmbedding(
                model.getApiKey(),
                "subject",
                new EmbeddingInfo("calc", new double[]{1.1, 5.6}, new byte[]{0xC, 0xA})
        );

        var subject = pair.getLeft();
        var embedding = pair.getRight();

        assertThat(subject).isNotNull();
        assertThat(subject.getId()).isNotNull();
        assertThat(embedding).isNotNull();
        assertThat(embedding.getId()).isNotNull();

        var embeddings = embeddingRepository.findBySubjectId(subject.getId());
        assertThat(embeddings).hasSize(1);

        var img = imgRepository.getImgByEmbeddingId(subject.getApiKey(), embedding.getId());
        assertThat(img).isPresent();
    }

    static Stream<Arguments> namePairs() {
        return Stream.of(
                Arguments.of("oldSubjectName", "newSubjectName"),
                Arguments.of("name", "naME")
        );
    }

    @ParameterizedTest
    @MethodSource("namePairs")
    void testIsAbleToUpdateSubjectName(String oldSubjectName, String newSubjectName) {
        var subject = dbHelper.insertSubject(oldSubjectName);

        boolean updated = subjectDao.updateSubjectName(subject.getApiKey(), oldSubjectName, newSubjectName);
        assertThat(updated).isTrue();
        assertThat(subjectRepository.findById(subject.getId()).orElseThrow().getSubjectName()).isEqualTo(newSubjectName);
    }

    @Test
    void testIsAbleToUpdateSubjectNameWithReassign() {
        var subjectName1 = "subjectName1";
        var subjectName2 = "subjectName2";

        var model = dbHelper.insertModel();

        var subject1 = dbHelper.insertSubject(model, subjectName1);
        dbHelper.insertEmbeddingWithImg(subject1);
        dbHelper.insertEmbeddingWithImg(subject1);

        var subject2 = dbHelper.insertSubject(model, subjectName2);
        dbHelper.insertEmbeddingWithImg(subject2);

        boolean updated = subjectDao.updateSubjectName(model.getApiKey(), subjectName1, subjectName2);
        assertThat(updated).isTrue();

        // no old subject
        final Optional<Subject> subject1Db = subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(model.getApiKey(), subjectName1);
        assertThat(subject1Db).isEmpty();
    }
}