package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.entity.Face;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface FacesRepository extends JpaRepository<Face, Long> {

    List<Face> findByApiKey(String modelApiKey);

    List<Face> deleteByApiKeyAndFaceName(String modelApiKey, String faceName);

    @Modifying
    @Query("delete from Face where apiKey = ?1")
    void deleteFacesByApiKey(String modelApiKey);

    int countByApiKey(String modelApiKey);

    Optional<Face> findById(String id);

    List<Face> findByIdIn(List<String> ids);
}