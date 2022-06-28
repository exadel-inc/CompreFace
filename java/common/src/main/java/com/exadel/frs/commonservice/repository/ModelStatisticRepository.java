package com.exadel.frs.commonservice.repository;

import com.exadel.frs.commonservice.entity.ModelStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelStatisticRepository extends JpaRepository<ModelStatistic, Long> {

}
