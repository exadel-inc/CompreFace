package com.exadel.frs.core.trainservice.dao;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SubjectRepositoryTest extends EmbeddedPostgreSQLTest {

    // TODO add fk to model api_key

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private EmbeddingRepository embeddingRepository;

    @Autowired
    private ImgRepository imgRepository;

    @Test
    void testIsAbleToInsertSubject() {
        final Subject subject = subjectRepository.save(subject("Brad Pit"));
        assertThat(subject.getId(), notNullValue());
    }

    private static Embedding embedding(Subject subject) {
        return embedding(subject, "FakeCalc1", null);
    }

    private static Embedding embedding(Subject subject, String calc, Img img) {
        Embedding embedding = new Embedding();
        embedding.setCalculator(calc);
        embedding.setEmbedding(new double[]{1.1, 1.56});
        embedding.setSubject(subject);
        embedding.setImg(img);

        return embedding;
    }

    private static Subject subject(String subjectName, String apiKey) {
        Subject subject = new Subject();
        subject.setApiKey(apiKey);
        subject.setSubjectName(subjectName);

        return subject;
    }

    private static Subject subject(String subjectName) {
        return subject(subjectName, UUID.randomUUID().toString());
    }

    private static Img img(Subject subject) {
        Img img = new Img();
        img.setSubject(subject);
        img.setContent(new byte[]{
                (byte) 0xCA,
                (byte) 0xFE,
                (byte) 0xBA,
                (byte) 0xBE,
        });
        return img;
    }

    @Test
    void testIsAbleToInsertSubjectWithEmbedding() {
        Subject subject = subjectRepository.save(subject("Donald Duck"));
        Embedding embedding = embeddingRepository.save(embedding(subject));

        assertThat(embedding.getId(), notNullValue());
        assertThat(embedding.getEmbedding(), notNullValue());

        assertThat(embeddingRepository.findBySubjectId(subject.getId()), hasSize(1));
    }

    @Test
    void testSubjectAndImgAndEmbedding() {
        Subject subject = subjectRepository.save(subject("Rob Pike"));
        embeddingRepository.save(embedding(subject));

        Img img = imgRepository.save(img(subject));
        Embedding embedding2 = embeddingRepository.save(embedding(subject, "calc2", img));

        assertThat(embeddingRepository.getUniqueCalculators(), hasItems("calc2", "FakeCalc1"));
        Embedding fromDb = embeddingRepository.findById(embedding2.getId()).orElseThrow();

        System.out.println(fromDb.getImg().getId());
    }

    @Test
    void testGetImgsBySubject() {
        Subject subject = subjectRepository.save(subject("Rob Pike"));

        UUID[] ids = IntStream.range(0, 7)
                .mapToObj(i -> imgRepository.save(img(subject)))
                .map(Img::getId)
                .toArray(UUID[]::new);

        var result = imgRepository.findBySubjectId(subject.getId())
                .stream()
                .map(Img::getId)
                .collect(Collectors.toList());

        assertThat(result, containsInAnyOrder(ids));
    }

    @Test
    void testGetBySubjectApiKey() {
        int subjectCount = 5;
        int embeddingsCountForEachSubject = 5;
        String apiKey = UUID.randomUUID().toString();

        UUID[] savedEmbeddingIds = IntStream.range(0, subjectCount)
                .mapToObj(i -> subjectRepository.save(subject(RandomStringUtils.randomAlphabetic(10), apiKey)))
                .flatMap(subject -> {
                    List<UUID> embeddingIds = new ArrayList<>();
                    for (int i = 0; i < embeddingsCountForEachSubject; i++) {
                        Img img = imgRepository.save(img(subject));
                        embeddingIds.add(embeddingRepository.save(embedding(subject, "calc1", img)).getId());
                    }
                    return embeddingIds.stream();
                })
                .toArray(UUID[]::new);

        List<UUID> foundEmbeddingIds = embeddingRepository.findBySubjectApiKey(apiKey)
                .map(embedding -> {
                    System.out.println(embedding.getImg().getId());
                    return embedding.getId();
                })
                .collect(Collectors.toList());

        assertThat(foundEmbeddingIds, containsInAnyOrder(savedEmbeddingIds));
    }
}