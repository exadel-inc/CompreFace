package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.entity.Face;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacesRepository extends MongoRepository<Face, String> {

    List<Face> findByApiKey(String modelApiKey);

    List<Face> deleteByApiKeyAndFaceName(String modelApiKey, String faceName);

    List<Face> deleteFacesByApiKey(String modelApiKey);

    List<Face> findByIdIn(List<String> ids);
}