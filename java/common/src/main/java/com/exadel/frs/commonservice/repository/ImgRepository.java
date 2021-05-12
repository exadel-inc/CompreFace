package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.Img;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.UUID;

public interface ImgRepository extends PagingAndSortingRepository<Img, UUID> {
    Collection<Img> findBySubjectId(UUID subjectId);

    @Query("select img.id from Img img where img.subject.id = :subjectId")
    Collection<UUID> getIdsBySubjectId(UUID subjectId);

    @Modifying
    @Query("delete from Img img where img.subject.id = :subjectId")
    void deleteBySubjectId(@Param("subjectId") UUID subjectId);

    @Modifying
    @Query("delete from Img img where img.subject.apiKey = :apiKey")
    void deleteBySubjectApiKey(@Param("apiKey") String apiKey);
}
