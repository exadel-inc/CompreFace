package com.exadel.frs.core.trainservice.dao;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.core.trainservice.dto.EmbeddingInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SubjectDao {

    private final SubjectRepository subjectRepository;
    private final EmbeddingRepository embeddingRepository;
    private final ImgRepository imgRepository;

    @Transactional
    public <T> T doWithEmbeddingsStream(String apiKey, Function<Stream<Embedding>, T> func) {
        try (Stream<Embedding> stream = embeddingRepository.findBySubjectApiKey(apiKey)) {
            return func.apply(stream);
        }
    }

    public boolean updateSubjectName(final String apiKey, String oldSubjectName, String newSubjectName) {
        final Optional<Subject> oldSubjectOptional = subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(
                apiKey,
                oldSubjectName
        );

        var updated = false;
        if (oldSubjectOptional.isPresent()) {
            var oldSubject = oldSubjectOptional.get();
            oldSubject.setSubjectName(newSubjectName);

            subjectRepository.save(oldSubject);
            updated = true;
        }

        return updated;
    }

    @Transactional
    public Optional<Subject> deleteSubjectByName(final String apiKey, final String subjectName) {
        return deleteSubject(() -> subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(apiKey, subjectName));
    }

    @Transactional
    public Optional<Subject> deleteSubjectById(final UUID subjectId) {
        return deleteSubject(() -> subjectRepository.findById(subjectId));
    }

    @Transactional
    public Optional<Subject> removeSubjectImg(final UUID imgId) {
//        imgRepository.findby
//        return deleteSubject(() -> subjectRepository.findById(subjectId));
    }

    private Optional<Subject> deleteSubject(Supplier<Optional<Subject>> supplier) {
        final Optional<Subject> subjectOptional = supplier.get();

        if (subjectOptional.isPresent()) {
            final var subject = subjectOptional.get();

            embeddingRepository.deleteBySubjectId(subject.getId());
            imgRepository.deleteBySubjectId(subject.getId());

            subjectRepository.delete(subject);
        }

        return subjectOptional;
    }

    @Transactional
    public void deleteSubjectsByApiKey(final String apiKey) {
        embeddingRepository.deleteBySubjectApiKey(apiKey);
        imgRepository.deleteBySubjectApiKey(apiKey);
        subjectRepository.deleteByApiKey(apiKey);
    }

    public int countSubjectsInModel(final String apiKey) {
        return subjectRepository.countByApiKey(apiKey);
    }

    @Transactional
    public void addEmbedding(final Subject subject, final EmbeddingInfo embeddingInfo) {
        saveEmbeddingInfo(subject, embeddingInfo);
    }

    @Transactional
    public Subject addNewSubject(final String apiKey, final String subjectName, final @Nullable EmbeddingInfo embeddingInfo) {
        var subject = new Subject();
        subject.setApiKey(apiKey);
        subject.setSubjectName(subjectName);

        subjectRepository.save(subject);

        if (embeddingInfo != null) {
            saveEmbeddingInfo(subject, embeddingInfo);
        }

        return subject;
    }

    private void saveEmbeddingInfo(Subject subject, EmbeddingInfo embeddingInfo) {
        var img = new Img();
        img.setSubject(subject);
        img.setContent(embeddingInfo.getImgContent());

        imgRepository.save(img);

        var embedding = new Embedding();
        embedding.setSubject(subject);
        embedding.setEmbedding(embeddingInfo.getEmbedding());
        embedding.setCalculator(embeddingInfo.getCalculator());
        embedding.setImg(img);

        embeddingRepository.save(embedding);
    }
}
