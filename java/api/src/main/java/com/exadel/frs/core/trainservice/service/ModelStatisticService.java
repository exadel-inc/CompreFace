package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.entity.ModelStatistic;
import com.exadel.frs.commonservice.enums.TableName;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.ModelStatisticRepository;
import com.exadel.frs.commonservice.repository.TableLockRepository;
import com.exadel.frs.core.trainservice.cache.ModelStatisticCacheEntry;
import com.exadel.frs.core.trainservice.cache.ModelStatisticCacheProvider;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelStatisticService {

    private static final String CRON_EXPRESSION = "*/10 * * * * *";

    private final ModelRepository modelRepository;
    private final TableLockRepository lockRepository;
    private final ModelStatisticRepository statisticRepository;
    private final ModelStatisticCacheProvider statisticCacheProvider;

    @Transactional
    @Scheduled(cron = CRON_EXPRESSION)
    public void updateAndRecordStatistics() {
        val cronStep = new CronStep();
        lockRepository.findByTableName(TableName.MODEL_STATISTIC);

        val cache = statisticCacheProvider.getCopyAsMap();
        statisticCacheProvider.invalidateAll();

        if (cache.isEmpty()) {
            log.info("No statistic to update and record");
            return;
        }

        val updatedStatistics = updateStatistics(cache, cronStep);
        val recordedStatistics = recordStatistics(cache);

        val updateCount = updatedStatistics.size();
        val recordCount = recordedStatistics.size();

        val statistics = new ArrayList<ModelStatistic>(updateCount + recordCount);
        statistics.addAll(updatedStatistics);
        statistics.addAll(recordedStatistics);

        statisticRepository.saveAll(statistics);
        log.info("The {} statistics have been updated and the {} statistics have been recorded", updateCount, recordCount);
    }

    private List<ModelStatistic> updateStatistics(final Map<Long, ModelStatisticCacheEntry> cache,
                                                  final CronStep cronStep) {
        val updatedStatistics = new ArrayList<ModelStatistic>();

        findStatisticsToUpdate(cache.keySet(), cronStep).forEach(statistic -> {
            val cacheKey = statistic.getModel().getId();
            val cacheEntry = cache.get(cacheKey);

            val totalRequestCount = statistic.getRequestCount() + cacheEntry.getRequestCount();
            statistic.setRequestCount(totalRequestCount);

            updatedStatistics.add(statistic);
            cache.remove(cacheKey);
        });

        return updatedStatistics;
    }

    private List<ModelStatistic> findStatisticsToUpdate(final Set<Long> modelIds,
                                                        final CronStep cronStep) {
        return statisticRepository.findAllByModelIdsAndCreatedDateBetween(
                modelIds, cronStep.getCurrent(), cronStep.getNext());
    }

    private List<ModelStatistic> recordStatistics(final Map<Long, ModelStatisticCacheEntry> cache) {
        val recordedStatistics = new ArrayList<ModelStatistic>();

        modelRepository.findAllByIds(cache.keySet()).forEach(model -> {
            val cacheKey = model.getId();
            val cacheEntry = cache.get(cacheKey);

            val statistic = ModelStatistic.builder()
                                          .requestCount(cacheEntry.getRequestCount())
                                          .createdDate(LocalDateTime.now())
                                          .model(model)
                                          .build();

            recordedStatistics.add(statistic);
            cache.remove(cacheKey);
        });

        return recordedStatistics;
    }

    @Getter
    private static class CronStep {

        private static final CronExpression CRON = CronExpression.parse(CRON_EXPRESSION);

        private final LocalDateTime current;
        private final LocalDateTime next;

        private CronStep() {
            current = LocalDateTime.now();
            next = CRON.next(current);
        }
    }
}
