package com.exadel.frs.core.trainservice.dao;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Img;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.exception.EmbeddingNotFoundException;
import com.exadel.frs.commonservice.exception.SubjectAlreadyExistsException;
import com.exadel.frs.commonservice.exception.SubjectNotFoundException;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.commonservice.system.global.ImageProperties;
import com.exadel.frs.core.trainservice.dto.EmbeddingInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SubjectDao {

    private final SubjectRepository subjectRepository;
    private final EmbeddingRepository embeddingRepository;
    private final ImgRepository imgRepository;
    private final ImageProperties imageProperties;

    public Collection<String> getSubjectNames(final String apiKey) {
        return subjectRepository.getSubjectNames(apiKey);
    }

    public List<Embedding> loadAllEmbeddingsByIds(Iterable<UUID> ids) {
        return embeddingRepository.findByIdIn(ids);
    }

    @Transactional
    public Subject deleteSubjectByName(final String apiKey, final String subjectName) {
        final Optional<Subject> subjectOptional = subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(apiKey, subjectName);
        if (subjectOptional.isEmpty()) {
            throw new SubjectNotFoundException(subjectName);
        }

        final var subject = subjectOptional.get();

        // order is important
        embeddingRepository.deleteBySubjectId(subject.getId());
        imgRepository.deleteBySubjectId(subject.getId());
        subjectRepository.delete(subject);

        return subject;
    }

    @Transactional
    public int removeAllSubjectEmbeddings(final String apiKey, final String subjectName) {
        final Optional<Subject> subjectOptional = subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(apiKey, subjectName);
        if (subjectOptional.isEmpty()) {
            // nothing has been removed
            return 0;
        }

        final var subject = subjectOptional.get();

        int deleted = embeddingRepository.deleteBySubjectId(subject.getId());
        imgRepository.deleteBySubjectId(subject.getId());
        subjectRepository.delete(subject);

        return deleted;
    }

    @Transactional
    public int removeAllSubjectEmbeddings(final String apiKey) {
        int deleted = embeddingRepository.deleteBySubjectApiKey(apiKey);
        imgRepository.deleteBySubjectApiKey(apiKey);
        subjectRepository.deleteByApiKey(apiKey);

        return deleted;
    }

    @Transactional
    public Embedding removeSubjectEmbedding(final String apiKey, final UUID embeddingId) {
        var embedding = embeddingRepository
                .findById(embeddingId)
                .filter(e -> apiKey.equals(e.getSubject().getApiKey()))
                .orElseThrow(() -> new EmbeddingNotFoundException(embeddingId));

        embeddingRepository.delete(embedding);

        // in case it was embedding with img and no more embeddings were calculated for img, we should also remove img
        if (embedding.getImg() != null && embedding.getImg().getId() != null) {
            UUID imgId = embedding.getImg().getId();
            final int embeddingsWithImg = imgRepository.countRelatedEmbeddings(imgId);
            if (embeddingsWithImg == 0) {
                // no more embeddings calculated for img, no need to keep it
                imgRepository.deleteById(imgId);
            }
        }

        return embedding;
    }

    @Transactional
    public boolean updateSubjectName(final String apiKey, final String oldSubjectName, final String newSubjectName) {
        final Optional<Subject> subjectWithOldName = subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(
                apiKey,
                oldSubjectName
        );

        if (subjectWithOldName.isEmpty()) {
            // no source subject
            throw new SubjectNotFoundException(oldSubjectName);
        }

        var sourceSubject = subjectWithOldName.get();

        final Optional<Subject> subjectWithNewName = subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(
                apiKey,
                newSubjectName
        );

        if (subjectWithNewName.isPresent()) {
            // subject with such name already exists

            var targetSubject = subjectWithNewName.get();

            if (sourceSubject.getId().equals(targetSubject.getId())) {
                // we found same subject => simple name update during transaction close
                sourceSubject.setSubjectName(newSubjectName);
            } else {
                // we should try to reassign existing embeddings
                embeddingRepository.reassignEmbeddings(sourceSubject, targetSubject);
                subjectRepository.delete(sourceSubject);
            }
        } else {
            // no subject with new name => simple name update during transaction close
            sourceSubject.setSubjectName(newSubjectName);
        }

        return true;
    }

    @Transactional
    public int deleteSubjectsByApiKey(final String apiKey) {
        // order is important
        embeddingRepository.deleteBySubjectApiKey(apiKey);
        imgRepository.deleteBySubjectApiKey(apiKey);
        return subjectRepository.deleteByApiKey(apiKey);
    }

    public Subject createSubject(final String apiKey, final String subjectName) {
        final Optional<Subject> subjectOptional = subjectRepository.findByApiKeyAndSubjectNameIgnoreCase(apiKey, subjectName);
        if (subjectOptional.isPresent()) {
            throw new SubjectAlreadyExistsException();
        }

        return saveSubject(apiKey, subjectName);
    }

    @Transactional
    public Pair<Subject, Embedding> addEmbedding(final String apiKey,
                                                 final String subjectName,
                                                 final @Nullable EmbeddingInfo embeddingInfo) {

        var subject = subjectRepository
            .findByApiKeyAndSubjectNameIgnoreCase(apiKey, subjectName)  // subject already exists
            .orElseGet(() -> saveSubject(apiKey, subjectName));         // add new subject

        Embedding embedding = null;
        if (embeddingInfo != null) {
            embedding = saveEmbeddingInfo(subject, embeddingInfo);
        }

        return Pair.of(subject, embedding);
    }

    private Subject saveSubject(String apiKey, String subjectName) {
        var subject = new Subject()
                .setApiKey(apiKey)
                .setSubjectName(subjectName);

        return subjectRepository.save(subject);
    }

    private Embedding saveEmbeddingInfo(Subject subject, EmbeddingInfo embeddingInfo) {
        var embedding = new Embedding();
        embedding.setSubject(subject);
        embedding.setEmbedding(embeddingInfo.getEmbedding());
        embedding.setCalculator(embeddingInfo.getCalculator());
        if (embeddingInfo.getSource() != null && imageProperties.isSaveImagesToDB()) {
            Img img = new Img();
            img.setContent(embeddingInfo.getSource());
            imgRepository.save(img);
            embedding.setImg(img);
        }

        return embeddingRepository.save(embedding);
    }
}
