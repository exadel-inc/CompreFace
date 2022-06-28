package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.entity.ModelStatistic;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.ModelStatisticRepository;
import com.exadel.frs.core.trainservice.cache.ModelStatisticCacheProvider;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelStatisticService {

    private final ModelRepository modelRepository;
    private final ModelStatisticRepository statisticRepository;
    private final ModelStatisticCacheProvider statisticCacheProvider;

    @Transactional
    @Scheduled(fixedDelayString = "${statistic.model.scheduler.period}")
    public void recordStatistics() {
        if (statisticCacheProvider.isEmpty()) {
            log.info("No statistic to record");
            return;
        }

        val statistics = new ArrayList<ModelStatistic>();
        val modelIds = statisticCacheProvider.getKeySet();
        val models = modelRepository.findAllByIds(modelIds);

        models.forEach(model -> {
            val cacheEntry = statisticCacheProvider.get(model.getId());
            val statistic = ModelStatistic.builder()
                                          .requestCount(cacheEntry.getRequestCount())
                                          .createdDate(LocalDateTime.now())
                                          .model(model)
                                          .build();
            statistics.add(statistic);
        });

        statisticRepository.saveAll(statistics);
        statisticCacheProvider.invalidateAll();
        log.info("Statistics have been recorded");
    }
}
