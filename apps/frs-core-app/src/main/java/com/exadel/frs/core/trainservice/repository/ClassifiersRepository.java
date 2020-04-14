package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.domain.Classifier;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassifiersRepository extends MongoRepository<Classifier, String> {

    List<Classifier> findByApiKey(final String modelApiKey);
}