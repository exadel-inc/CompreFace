package com.exadel.frs.system.statistics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CallStatisticsService {
    Page<CallStatisticsInfo> findAll(Pageable pageable);
}
