package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.Img;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.UUID;

public interface ImgRepository extends PagingAndSortingRepository<Img, UUID> {
    Collection<Img> findBySubjectId(UUID subjectId);

    @Query("select img.id from Img img where img.subject.id = :subjectId")
    Collection<UUID> getIdsBySubjectId(UUID subjectId);
}
