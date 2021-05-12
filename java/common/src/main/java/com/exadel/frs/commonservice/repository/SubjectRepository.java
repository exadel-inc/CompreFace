package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.Subject;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubjectRepository extends PagingAndSortingRepository<Subject, UUID> {

    List<Subject> findByApiKey(String apiKey);

    Optional<Subject> findByApiKeyAndSubjectNameIgnoreCase(String apiKey, String subjectName);

    @Modifying
    @Query("delete from Subject s where s.apiKey = :apiKey")
    void deleteByApiKey(@Param("apiKey") String apiKey);

    int countByApiKey(String apiKey);
}