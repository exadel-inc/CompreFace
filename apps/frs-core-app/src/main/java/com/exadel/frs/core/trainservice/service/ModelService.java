package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.component.FaceClassifierAdapter;
import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.domain.Model;
import com.exadel.frs.core.trainservice.exception.ClassifierNotTrained;
import com.exadel.frs.core.trainservice.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;

    public Model saveModel(String modelId, FaceClassifier classifier) {
        Model model = Model.builder()
                .id(modelId)
                .classifier(classifier)
                .classifierName(FaceClassifierAdapter.CLASSIFIER_IMPLEMENTATION_BEAN_NAME)
                .build();
        return modelRepository.save(model);
    }

    public FaceClassifier getModel(String modelId) {
        return modelRepository.findById(modelId).orElseThrow(ClassifierNotTrained::new).getClassifier();
    }

    public void deleteModel(String modelId) {
        try {
            modelRepository.deleteById(modelId);
        } catch (EmptyResultDataAccessException e) {
            log.info("Model with id : {} not found", modelId);
        }
    }


}
