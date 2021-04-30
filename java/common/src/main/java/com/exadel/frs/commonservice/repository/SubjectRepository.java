package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.Subject;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SubjectRepository extends PagingAndSortingRepository<Subject, String> {

   List<Subject> findByApiKey(String apiKey);

}