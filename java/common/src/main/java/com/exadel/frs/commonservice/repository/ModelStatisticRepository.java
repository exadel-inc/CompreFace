package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.ModelStatistic;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelStatisticRepository extends JpaRepository<ModelStatistic, Long> {

    @Query("select ms " +
            "from ModelStatistic ms " +
            "join ms.model m " +
            "where m.id in :ids " +
            "and ms.createdDate between :start and :end")
    List<ModelStatistic> findAllByModelIdsAndCreatedDateBetween(
            @Param("ids")
            Set<Long> modelIds,
            @Param("start")
            LocalDateTime startDate,
            @Param("end")
            LocalDateTime endDate);
}
