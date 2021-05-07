package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.Subject;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends PagingAndSortingRepository<Subject, String> {

   List<Subject> findByApiKey(String apiKey);

   Optional<Subject> findBySubjectNameIgnoreCase(String subjectName);
}