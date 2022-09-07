package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.ModelStatistic;
import com.exadel.frs.commonservice.entity.ModelStatisticProjection;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelStatisticRepository extends JpaRepository<ModelStatistic, Long> {

    Stream<ModelStatistic> findAllByModelIdInAndCreatedDate(Set<Long> modelIds, LocalDateTime createdDate);

    @Query("SELECT new com.exadel.frs.commonservice.entity.ModelStatisticProjection(SUM(statistic.requestCount), CAST(statistic.createdDate AS date)) " +
            "FROM ModelStatistic AS statistic " +
            "JOIN statistic.model AS model " +
            "WHERE model.guid = :modelGuid AND CAST(statistic.createdDate AS date) BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(statistic.createdDate AS date) " +
            "ORDER BY CAST(statistic.createdDate AS date) DESC")
    List<ModelStatisticProjection> findAllSummarizedByDay(String modelGuid, Date startDate, Date endDate);
}
