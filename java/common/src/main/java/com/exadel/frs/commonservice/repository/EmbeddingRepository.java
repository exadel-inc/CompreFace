package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface EmbeddingRepository extends JpaRepository<Embedding, String> {
    Collection<Embedding> findBySubject(Subject subject);

    @Query("select distinct(e.calculator) from Embedding e")
    Collection<String> getDistinctCalculators();
}
