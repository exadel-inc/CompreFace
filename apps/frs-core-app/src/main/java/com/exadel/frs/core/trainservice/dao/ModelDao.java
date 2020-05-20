package com.exadel.frs.core.trainservice.dao;

import static java.util.stream.Collectors.toList;
import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.entity.Model;
import com.exadel.frs.core.trainservice.exception.ModelNotFoundException;
import com.exadel.frs.core.trainservice.exception.ModelNotTrainedException;
import com.exadel.frs.core.trainservice.repository.ModelRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.types.ObjectId;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModelDao {

    private final ModelRepository modelRepository;

    public List<Model> findAllWithoutClassifier() {
        return modelRepository.findAllWithoutClassifier();
    }

    public Model saveModel(
            final String modelKey,
            final FaceClassifier classifier,
            final String calculatorVersion
    ) {
        val model = Model.builder()
                         .id(UUID.randomUUID().toString())
                         .modelKey(modelKey)
                         .classifier(classifier)
                         .faces(classifier.getUsedFaceIds().stream()
                                          .map(ObjectId::new)
                                          .collect(toList()))
                         .calculatorVersion(calculatorVersion)
                         .build();

        return modelRepository.save(model);
    }

    public Model updateModelApiKey(
            final String oldModelKey,
            final String newModelKey
    ) {
        val model = modelRepository.findFirstByModelKey(oldModelKey).orElseThrow(ModelNotFoundException::new);
        model.setModelKey(newModelKey);

        return modelRepository.save(model);
    }

    public FaceClassifier getModel(final String modelKey) {
        return modelRepository.findFirstByModelKey(modelKey).orElseThrow(ModelNotTrainedException::new).getClassifier();
    }

    public void deleteModel(final String modelKey) {
        try {
            modelRepository.deleteByModelKey(modelKey);
        } catch (EmptyResultDataAccessException e) {
            log.info("Model with id : {} not found", modelKey);
        }
    }
}