package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.ModelStatistic;
import com.exadel.frs.commonservice.entity.ModelStatisticProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelStatisticRepository extends JpaRepository<ModelStatistic, Long> {

    Stream<ModelStatistic> findAllByModelIdInAndCreatedDate(Set<Long> modelIds, LocalDateTime createdDate);

    List<ModelStatisticProjection> findAllByModelGuidAndCreatedDateBetween(String modelGuid, LocalDateTime from, LocalDateTime to);
}
