package com.exadel.frs.core.trainservice.dao;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.dto.EmbeddingInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SubjectDaoTest extends EmbeddedPostgreSQLTest {

    // TODO add fk to model api_key

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private EmbeddingRepository embeddingRepository;

    @Autowired
    private ImgRepository imgRepository;

    @Test
    void testIsAbleToInsertSubjectWithoutEmbedding() {
        insertSubject("subject_without_embeddings");
    }

    @Test
    void testIsAbleToInsertSubjectWithEmbedding() {
        final Subject subject = insertSubject(
                "subject_with_embeddings",
                new EmbeddingInfo("calc", new double[]{1.1, 5.6}, null)
        );

        assertThat(embeddingRepository.findBySubjectId(subject.getId()), hasSize(1));
    }

    @Test
    void testIsAbleToInsertSubjectWithEmbeddingAndImg() {
        final Subject subject = insertSubject(
                "subject_with_embedding_and_img",
                new EmbeddingInfo("calc", new double[]{1.1, 5.6}, img())
        );

        final List<Embedding> embeddings = embeddingRepository.findBySubjectId(subject.getId());
        assertThat(embeddings, hasSize(1));

        final Embedding embedding = embeddings.iterator().next();

        assertThat(embedding.getImg(), notNullValue());
        assertThat(embedding.getImg().getId(), notNullValue());
    }

    @Test
    void testIsAbleToUpdateSubjectName() {
        final String oldSubjectName = "oldSubjectName";
        final String newSubjectName = "newSubjectName";

        final Subject subject = insertSubject(
                oldSubjectName,
                new EmbeddingInfo("calc", new double[]{1.1, 5.6}, img())
        );
        assertThat(subject.getSubjectName(), is(oldSubjectName));

        Optional<UUID> newSubjectIdOptional = subjectDao.updateSubjectName(subject.getApiKey(), oldSubjectName, newSubjectName);
        assertThat(newSubjectIdOptional.isPresent(), is(true));

        final UUID newSubjectId = newSubjectIdOptional.get();

        assertThat(newSubjectId, is(subject.getId()));
        assertThat(subjectRepository.findById(subject.getId()).orElseThrow().getSubjectName(), is(newSubjectName));
    }

    @Test
    void testNotUpdatedWhenUpdateWithSameName() {
        final String oldSubjectName = "oldSubjectName";

        final Subject subject = insertSubject(
                oldSubjectName,
                new EmbeddingInfo("calc", new double[]{1.1, 5.6}, img())
        );
        assertThat(subject.getSubjectName(), is(oldSubjectName));

        Optional<UUID> newSubjectIdOptional = subjectDao.updateSubjectName(subject.getApiKey(), oldSubjectName, oldSubjectName);
        assertThat(newSubjectIdOptional.isEmpty(), is(true));
    }

    @Test
    void testReassignEmbeddings() {
        final UUID apiKey = UUID.randomUUID();
        final String subjectName1 = "Rob Pike";
        final String subjectName2 = "Bob Dylan";

        final Subject subject1 = insertSubject(
                apiKey,
                subjectName1,
                new EmbeddingInfo("calc1", new double[]{1.1, 5.6}, img())
        );
        subjectDao.addEmbedding(subject1, new EmbeddingInfo("calc1", new double[]{2.2, 3.3}, null));
        subjectDao.addEmbedding(subject1, new EmbeddingInfo("calc1", new double[]{2.2, 5.3}, null));

        final Subject subject2 = insertSubject(
                apiKey,
                subjectName2,
                new EmbeddingInfo("calc2", new double[]{1.1, 5.6}, img())
        );
        subjectDao.addEmbedding(subject2, new EmbeddingInfo("calc3", new double[]{2.2, 5.3}, img()));

        Optional<UUID> newSubjectIdOptional = subjectDao.updateSubjectName(apiKey.toString(), subjectName1, subjectName2);
        assertThat(newSubjectIdOptional.isPresent(), is(true));

        // no old subject
        assertThat(subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(apiKey.toString(), subjectName1).isEmpty(), is(true));
        assertThat(embeddingRepository.countByApiKey(apiKey.toString()), is(5));
    }

    @Test
    void testUpdateSubjectName() {
        final UUID apiKey = UUID.randomUUID();

        insertSubject(
                apiKey,
                "Rob Pike",
                new EmbeddingInfo("calc1", new double[]{1.1, 5.6}, img())
        );
        insertSubject(
                apiKey,
                "Bob Dylan",
                new EmbeddingInfo("calc2", new double[]{1.1, 5.6}, img())
        );

        assertThat(embeddingRepository.getUniqueCalculators(), hasItems("calc1", "calc2"));
    }
//
//    @Test
//    void testGetBySubjectApiKey() {
//        int subjectCount = 3;
//        int embeddingsCountForEachSubject = 3;
//        String apiKey = UUID.randomUUID().toString();
//
//        List<UUID> savedEmbeddingIds = IntStream.range(0, subjectCount)
//                .mapToObj(i -> subjectRepository.save(subject(RandomStringUtils.randomAlphabetic(10), apiKey)))
//                .flatMap(subject -> {
//                    List<UUID> embeddingIds = new ArrayList<>();
//                    for (int i = 0; i < embeddingsCountForEachSubject; i++) {
//                        Img img = imgRepository.save(img());
//                        embeddingIds.add(embeddingRepository.save(embedding(subject, "calc1", img)).getId());
//                    }
//                    return embeddingIds.stream();
//                }).collect(Collectors.toList());
//
//        for (UUID savedEmbeddingId : savedEmbeddingIds) {
//            assertThat(embeddingRepository.findByIdAndSubjectApiKey(savedEmbeddingId, apiKey), notNullValue());
//        }
//    }

    private static byte[] img() {
        return new byte[]{
                (byte) 0xCA,
                (byte) 0xFE,
                (byte) 0xBA,
                (byte) 0xBE,
        };
    }

    private Subject insertSubject(String subjectName) {
        return insertSubject(subjectName, null);
    }

    private Subject insertSubject(String subjectName, EmbeddingInfo embeddingInfo) {
        return insertSubject(UUID.randomUUID(), subjectName, embeddingInfo);
    }

    private Subject insertSubject(UUID apiKey, String subjectName, EmbeddingInfo embeddingInfo) {
        final Pair<Subject, Embedding> pair = subjectDao.addEmbedding(
                apiKey.toString(),
                subjectName,
                embeddingInfo
        );

        final Subject subject = pair.getLeft();

        assertThat(subject.getId(), notNullValue());
        if (embeddingInfo != null) {
            // should be saved embedding
            final Embedding embedding = pair.getRight();
            assertThat(embedding.getId(), notNullValue());
        }

        return subject;
    }
}