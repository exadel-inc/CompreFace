package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.Embedding;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EmbeddingRepository extends JpaRepository<Embedding, UUID> {

    @EntityGraph("embedding-with-subject")
    List<Embedding> findBySubjectApiKey(String apiKey);

    List<Embedding> findBySubjectId(UUID subjectId);

    @Query("select distinct(e.calculator) from Embedding e")
    List<String> getUniqueCalculators();
}
