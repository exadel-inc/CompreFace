package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.EmbeddingProjection;
import com.exadel.frs.commonservice.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

public interface EmbeddingRepository extends JpaRepository<Embedding, UUID> {

    // Note: consumer should consume in transaction
    @EntityGraph("embedding-with-subject")
    Stream<Embedding> findBySubjectApiKey(String apiKey);

    @Transactional
    default <T> T doWithEmbeddingsStream(String apiKey, Function<Stream<Embedding>, T> func) {
        try (Stream<Embedding> stream = findBySubjectApiKey(apiKey)) {
            return func.apply(stream);
        }
    }

    List<Embedding> findBySubjectId(UUID subjectId);

    @Modifying
    @Query("delete from Embedding e where e.subject.id = :subjectId")
    int deleteBySubjectId(@Param("subjectId") UUID subjectId);

    @Modifying
    @Query("delete from Embedding e where e.subject.apiKey = :apiKey")
    int deleteBySubjectApiKey(@Param("apiKey") String apiKey);

    @Modifying
    @Query("delete from Embedding e where e.subject.apiKey = :apiKey and e.id = :id")
    int deleteBySubjectApiKeyAndId(@Param("apiKey") String apiKey, @Param("id") UUID embeddingId);

    @Modifying
    @Query("update Embedding e set e.subject = :toSubject where e.subject = :fromSubject")
    int reassignEmbeddings(@Param("fromSubject") Subject fromSubject, @Param("toSubject") Subject toSubject);

    @Query("select distinct(e.calculator) from Embedding e")
    List<String> getUniqueCalculators();

    @Query("select count(e) from Embedding e where e.subject.apiKey = :apiKey")
    int countByApiKey(@Param("apiKey") String apiKey);

    @Query("select " +
            " new com.exadel.frs.commonservice.entity.EmbeddingProjection(e.id, e.subject.subjectName)" +
            " from " +
            "   Embedding e " +
            " where " +
            "   e.subject.apiKey = :apiKey")
    Page<EmbeddingProjection> findBySubjectApiKey(String apiKey, Pageable pageable);
}