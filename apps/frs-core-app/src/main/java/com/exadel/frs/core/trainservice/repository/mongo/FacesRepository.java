package com.exadel.frs.core.trainservice.repository.mongo;

import com.exadel.frs.core.trainservice.entity.mongo.Face;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacesRepository extends MongoRepository<Face, String> {

    List<Face> findByApiKey(final String modelApiKey);

    List<Face> deleteByApiKeyAndFaceName(final String modelApiKey, final String faceName);

    List<Face> deleteFacesByApiKey(final String modelApiKey);

    int countByApiKey(final String modelApiKey);

    List<Face> findByIdIn(List<String> ids);
}