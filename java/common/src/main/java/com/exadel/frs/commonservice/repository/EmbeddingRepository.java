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

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface EmbeddingRepository extends JpaRepository<Embedding, UUID> {

    // Note: consumer should consume in transaction
    @EntityGraph("embedding-with-subject")
    Stream<Embedding> findBySubjectApiKey(String apiKey);

    List<Embedding> findBySubjectId(UUID subjectId);

    @Query("select e from Embedding e where e.img is not null and e.calculator <> :calculator")
    List<Embedding> getWithImgAndCalculatorNotEq(@Param("calculator") String calculator);

    @Modifying
    @Query("update Embedding e set e.embedding = :embedding, e.calculator = :calculator where e.id = :embeddingId")
    int updateEmbedding(@Param("embeddingId") UUID embeddingId,
                        @Param("embedding") double[] embedding,
                        @Param("calculator") String calculator);

    @Modifying
    @Query("delete from Embedding e where e.subject.id = :subjectId")
    int deleteBySubjectId(@Param("subjectId") UUID subjectId);

    @Modifying
    @Query("delete from Embedding where id in (select distinct(e.id) from Embedding e where e.subject.apiKey = :apiKey)")
    int deleteBySubjectApiKey(@Param("apiKey") String apiKey);

    @Modifying
    @Query("update Embedding e set e.subject = :toSubject where e.subject = :fromSubject")
    int reassignEmbeddings(@Param("fromSubject") Subject fromSubject, @Param("toSubject") Subject toSubject);

    @Query("select " +
            " new com.exadel.frs.commonservice.entity.EmbeddingProjection(e.id, e.subject.subjectName)" +
            " from " +
            "   Embedding e " +
            " where " +
            "   e.subject.apiKey = :apiKey")
    Page<EmbeddingProjection> findBySubjectApiKey(String apiKey, Pageable pageable);

    @Query("select distinct(e.calculator) from Embedding e")
    List<String> getUniqueCalculators();

    @Query("select " +
            "   count(e) " +
            " from " +
            "   Embedding e " +
            " where " +
            "   e.subject.apiKey = :apiKey " +
            "   and e.calculator <> :calculator")
    Long countBySubjectApiKeyAndCalculatorNotEq(@Param("apiKey") String apiKey,
                                                @Param("calculator") String calculator);

    @Query("select " +
            "   count(e) " +
            " from " +
            "   Embedding e " +
            " where " +
            "   e.subject.apiKey <> :apiKey " +
            "   and e.calculator <> :calculator")
    Long countBySubjectApiKeyNotEqAndCalculatorNotEq(@Param("apiKey") String apiKey,
                                                     @Param("calculator") String calculator);
}