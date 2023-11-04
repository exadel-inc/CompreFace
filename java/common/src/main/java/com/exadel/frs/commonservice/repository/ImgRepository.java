package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.Img;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ImgRepository extends PagingAndSortingRepository<Img, UUID> {

    @Query("select count(e) from Embedding e where e.img.id = :imgId")
    int countRelatedEmbeddings(@Param("imgId") UUID imgId);

    // Joins, are prohibited in a bulk HQL query. You can use sub-queries in the WHERE clause, and the sub-queries themselves can contain joins.

    @Modifying
    @Query("delete from Img where id in (select distinct(i.id) from Img i join Embedding e on e.img.id = i.id where e.subject.id = :subjectId)")
    void deleteBySubjectId(@Param("subjectId") UUID subjectId);

    @Modifying
    @Query("delete from Img where id in (select distinct(i.id) from Img i join Embedding e on e.img.id = i.id where e.subject.apiKey = :apiKey)")
    void deleteBySubjectApiKey(@Param("apiKey") String apiKey);

    @Query("select i from Img i join Embedding e on e.img.id = i.id where e.id = :embeddingId and e.subject.apiKey = :apiKey")
    Optional<Img> getImgByEmbeddingId(@Param("apiKey") String apiKey, @Param("embeddingId") UUID embeddingId);

    @Query("select count(i) from Img i join Embedding e on e.img = i.id join Subject s on e.subject.id = s.id where s.apiKey=:apiKey")
    Long getImageCountByApiKey(@Param("apiKey") String apiKey);
}
