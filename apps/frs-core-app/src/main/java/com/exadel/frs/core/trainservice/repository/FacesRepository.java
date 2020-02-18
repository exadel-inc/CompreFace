package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.domain.Face;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacesRepository extends MongoRepository<Face, String> {

    List<Face> findByApiKey(String appKey);
}