package com.exadel.frs.core.trainservice.dao;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.core.trainservice.dto.EmbeddingInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class SubjectDao {

    private final SubjectRepository subjectRepository;
    private final EmbeddingRepository embeddingRepository;
    private final ImgRepository imgRepository;

    @Transactional
    public Optional<Subject> deleteSubjectById(final String apiKey, final UUID subjectId) {
        return deleteSubject(() -> subjectRepository.findById(subjectId).filter(subject -> apiKey.equals(subject.getApiKey())));
    }

    @Transactional
    public Optional<Subject> deleteSubjectByName(final String apiKey, final String subjectName) {
        return deleteSubject(() -> subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(apiKey, subjectName));
    }

    @Transactional
    public Optional<Embedding> removeSubjectEmbedding(final String apiKey, final UUID embeddingId) {
        final Optional<Embedding> embeddingOptional = embeddingRepository
                .findById(embeddingId)
                .filter(embedding -> apiKey.equals(embedding.getSubject().getApiKey()));

        if (embeddingOptional.isPresent()) {
            final var embedding = embeddingOptional.get();
            embeddingRepository.delete(embedding);

            // in case it was embedding with img and no more embeddings were calculated for img, we should also remove img
            UUID imgId = null;
            if (embedding.getImg() != null && embedding.getImg().getId() != null) {
                imgId = embedding.getImg().getId();
                final int embeddingsWithImg = imgRepository.countRelatedEmbeddings(imgId);
                if (embeddingsWithImg == 0) {
                    // no more embeddings calculated for img, no need to keep it
                    imgRepository.deleteById(imgId);
                }
            }
        }

        return embeddingOptional;
    }

    @Transactional
    public Optional<UUID> updateSubjectName(final String apiKey, final String oldSubjectName, final String newSubjectName) {
        if (StringUtils.isEmpty(newSubjectName) || newSubjectName.equalsIgnoreCase(oldSubjectName)) {
            // no need to update with empty or similar name
            return Optional.empty();
        }

        final Optional<Subject> subjectWithOldName = subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(
                apiKey,
                oldSubjectName
        );

        if (subjectWithOldName.isEmpty()) {
            // no source subject
            return Optional.empty();
        }

        var sourceSubject = subjectWithOldName.get();

        final Optional<Subject> subjectWithNewName = subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(
                apiKey,
                newSubjectName
        );

        UUID newSubjectId;
        if (subjectWithNewName.isPresent()) {
            // subject with such name already exists, we should try to reassign existing embeddings

            var targetSubject = subjectWithNewName.get();
            embeddingRepository.reassignEmbeddings(sourceSubject, targetSubject);

            subjectRepository.delete(sourceSubject);

            newSubjectId = targetSubject.getId();
        } else {
            // no subject with new name => simple name update during transaction close

            sourceSubject.setSubjectName(newSubjectName);
            newSubjectId = sourceSubject.getId();
        }

        return Optional.of(newSubjectId);
    }

    // Note: should be invoked in transaction
    private Optional<Subject> deleteSubject(Supplier<Optional<Subject>> supplier) {
        final Optional<Subject> subjectOptional = supplier.get();

        if (subjectOptional.isPresent()) {
            final var subject = subjectOptional.get();

            imgRepository.deleteBySubjectId(subject.getId());
            embeddingRepository.deleteBySubjectId(subject.getId());
            subjectRepository.delete(subject);
        }

        return subjectOptional;
    }

    @Transactional
    public int deleteSubjectsByApiKey(final String apiKey) {
        imgRepository.deleteBySubjectApiKey(apiKey);
        embeddingRepository.deleteBySubjectApiKey(apiKey);
        return subjectRepository.deleteByApiKey(apiKey);
    }

    public int countSubjectsInModel(final String apiKey) {
        return subjectRepository.countByApiKey(apiKey);
    }

    public int countEmbeddingsInModel(final String apiKey) {
        return embeddingRepository.countByApiKey(apiKey);
    }

    @Transactional
    public Embedding addEmbedding(final Subject subject, final EmbeddingInfo embeddingInfo) {
        return saveEmbeddingInfo(subject, embeddingInfo);
    }

    @Transactional
    public Pair<Subject, Embedding> addEmbedding(final String apiKey,
                                                 final String subjectName,
                                                 final @Nullable EmbeddingInfo embeddingInfo) {
        final Optional<Subject> subjectOptional = subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(apiKey, subjectName);

        Subject subject;
        if (subjectOptional.isPresent()) {
            // subject with current name already exists
            subject = subjectOptional.get();
        } else {
            // no subject with such name, we should create one
            subject = new Subject();
            subject.setApiKey(apiKey);
            subject.setSubjectName(subjectName);

            subjectRepository.save(subject);
        }

        Embedding embedding = null;
        if (embeddingInfo != null) {
            embedding = saveEmbeddingInfo(subject, embeddingInfo);
        }

        return Pair.of(subject, embedding);
    }

    private Embedding saveEmbeddingInfo(Subject subject, EmbeddingInfo embeddingInfo) {
        Img img = null;
        if (embeddingInfo.getSource() != null) {
            img = new Img();
            img.setContent(embeddingInfo.getSource());

            imgRepository.save(img);
        }

        var embedding = new Embedding();
        embedding.setSubject(subject);
        embedding.setEmbedding(embeddingInfo.getEmbedding());
        embedding.setCalculator(embeddingInfo.getCalculator());
        embedding.setImg(img);

        return embeddingRepository.save(embedding);
    }
}
