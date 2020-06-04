package com.exadel.frs.system.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CallStatisticsServiceImpl implements CallStatisticsService {
    private final CallStatisticsRepository repository;

    @Override
    public Page<CallStatisticsInfo> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
