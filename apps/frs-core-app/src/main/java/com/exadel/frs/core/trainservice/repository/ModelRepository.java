package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.entity.Model;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ModelRepository extends MongoRepository<Model, String> {
}
