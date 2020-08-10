package com.exadel.frs.core.trainservice.repository.postgres;

import com.exadel.frs.core.trainservice.entity.postgres.Face;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface FacesRepository extends JpaRepository<Face, Long> {

    List<Face> findByApiKey(String modelApiKey);

    List<Face> deleteByApiKeyAndFaceName(String modelApiKey, String faceName);

    List<Face> deleteFacesByApiKey(String modelApiKey);

    int countByApiKey(String modelApiKey);

    List<Face> findByIdIn(List<Long> ids);
}