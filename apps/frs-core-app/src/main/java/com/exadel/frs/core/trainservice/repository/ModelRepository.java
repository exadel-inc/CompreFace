package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.entity.Model;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ModelRepository extends MongoRepository<Model, String> {

    @Query(value="{}", fields="{classifier : 0}")
    List<Model> findAllWithoutClassifier();

    Optional<Model> findFirstByModelKey(String modelKey);

    void deleteByModelKey(String modelKey);
}