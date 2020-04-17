package com.exadel.frs.core.trainservice.dao;

import com.exadel.frs.core.trainservice.domain.Classifier;
import com.exadel.frs.core.trainservice.repository.ClassifiersRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassifierDao {

    private final ClassifiersRepository classifiersRepository;

    public List<Classifier> findAllClassifiersByApiKey(final String modelApiKey) {
        return classifiersRepository.findByApiKey(modelApiKey);
    }
}