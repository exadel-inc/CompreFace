package com.exadel.frs.core.trainservice.service;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import com.exadel.frs.commonservice.entity.ModelStatistic;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.ModelStatisticRepository;
import com.exadel.frs.core.trainservice.cache.ModelStatisticCacheProvider;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelStatisticService {

    @Value("${statistic.model.scheduler.delay}")
    private long schedulerDaley;

    private final ModelRepository modelRepository;
    private final ModelStatisticRepository statisticRepository;
    private final ModelStatisticCacheProvider statisticCacheProvider;

    @Transactional
    @Scheduled(fixedDelayString = "${statistic.model.scheduler.delay}")
    public void recordStatistics() {
        if (statisticCacheProvider.isEmpty()) {
            log.info("No statistic to record");
            return;
        }

        val statisticsToRecord = new ArrayList<ModelStatistic>();
        val statisticsToUpdate = getStatisticsToUpdate();

        statisticsToUpdate.forEach(statistic -> {
            val modelId = statistic.getModel().getId();
            val cacheEntry = statisticCacheProvider.get(modelId);
            val totalRequestCount = statistic.getRequestCount() + cacheEntry.getRequestCount();

            statistic.setRequestCount(totalRequestCount);
            statisticCacheProvider.invalidate(modelId);
        });

        modelRepository.findAllByIds(statisticCacheProvider.getKeySet()).forEach(model -> {
            val cacheEntry = statisticCacheProvider.get(model.getId());
            val statistic = ModelStatistic.builder()
                                          .requestCount(cacheEntry.getRequestCount())
                                          .createdDate(now())
                                          .model(model)
                                          .build();

            statisticsToRecord.add(statistic);
        });

        statisticRepository.saveAll(statisticsToRecord);
        statisticCacheProvider.invalidateAll();
        log.info("Statistics have been recorded");
    }

    private List<ModelStatistic> getStatisticsToUpdate() {
        val modelIds = statisticCacheProvider.getKeySet();
        return statisticRepository.findAllByModelIdsAndCreatedDateBetween(
                modelIds, now().minus(schedulerDaley, MILLIS), now()
        );
    }
}
