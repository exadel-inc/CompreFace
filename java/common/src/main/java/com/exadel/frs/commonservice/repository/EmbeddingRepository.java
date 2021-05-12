package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.Embedding;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface EmbeddingRepository extends JpaRepository<Embedding, UUID> {

    // consumer should be wrapped in Transactional
    @EntityGraph("embedding-with-subject")
    Stream<Embedding> findBySubjectApiKey(String apiKey);

    @EntityGraph("embedding-with-subject")
    Optional<Embedding> findBySubjectApiKeyAndSubjectSubjectNameIgnoreCase(String apiKey, String subjectName);

    @Modifying
    @Query("delete from Embedding e where e.subject.id = :subjectId")
    void deleteBySubjectId(@Param("subjectId") UUID subjectId);

    @Modifying
    @Query("delete from Embedding e where e.subject.apiKey = :apiKey")
    void deleteBySubjectApiKey(@Param("apiKey") String apiKey);

    List<Embedding> findBySubjectId(UUID subjectId);

    @Query("select distinct(e.calculator) from Embedding e")
    List<String> getUniqueCalculators();
}
