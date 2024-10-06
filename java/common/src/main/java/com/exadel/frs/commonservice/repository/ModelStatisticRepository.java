package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.ModelStatistic;
import com.exadel.frs.commonservice.projection.ModelStatisticProjection;
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

    @Query("""
            select
                new com.exadel.frs.commonservice.projection.ModelStatisticProjection(sum(statistic.requestCount), cast(statistic.createdDate as date))
            from
                ModelStatistic as statistic
            join
                statistic.model as model
            where
                model.guid = :modelGuid
            and
                cast(statistic.createdDate as date) between :startDate and :endDate
            group by
                cast(statistic.createdDate as date)
            order by
                cast(statistic.createdDate as date) desc
            """)
    List<ModelStatisticProjection> findAllSummarizedByDay(String modelGuid, Date startDate, Date endDate);
}
