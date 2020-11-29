package com.exadel.frs.core.trainservice.dao;

import com.exadel.frs.core.trainservice.entity.Model;
import com.exadel.frs.core.trainservice.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModelDao {

    private final ModelRepository modelRepository;

    public Model findByApiKey(final String modelKey) {
        return modelRepository.findByApiKey(modelKey).orElse(null);
    }
}