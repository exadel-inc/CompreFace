package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.commonservice.enums.TableLockName.MODEL_STATISTIC_LOCK;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.temporal.ChronoUnit.HOURS;
import com.exadel.frs.commonservice.entity.ModelStatistic;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.ModelStatisticRepository;
import com.exadel.frs.commonservice.repository.TableLockRepository;
import com.exadel.frs.core.trainservice.cache.ModelStatisticCacheProvider;
import com.exadel.frs.core.trainservice.util.CronExecution;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
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

    private static final String CRON_EXPRESSION_PLACEHOLDER = "${statistic.model.cron-expression}";

    @Value(CRON_EXPRESSION_PLACEHOLDER)
    private String cronExpression;
    private CronExecution cronExecution;

    private final ModelRepository modelRepository;
    private final TableLockRepository lockRepository;
    private final ModelStatisticRepository statisticRepository;
    private final ModelStatisticCacheProvider statisticCacheProvider;

    @PostConstruct
    private void postConstruct() {
        cronExecution = new CronExecution(cronExpression);
    }

    @Transactional
    @Scheduled(cron = CRON_EXPRESSION_PLACEHOLDER, zone = "UTC")
    public void updateAndRecordStatistics() {
        if (statisticCacheProvider.isEmpty()) {
            log.info("No statistic to update or record.");
            return;
        }

        val lastExecution = getLastExecution();

        if (lastExecution == null) {
            log.error("Couldn't update or record statistics due to can't calculate the execution time for your cron expression.");
            statisticCacheProvider.invalidateCache();
            return;
        }

        val cache = statisticCacheProvider.getCacheCopyAsMap();
        statisticCacheProvider.invalidateCache();

        // Used to obtain a table lock. Only one application instance per time can execute the method.
        lockRepository.lockByName(MODEL_STATISTIC_LOCK);

        val updatedStatistics = updateStatistics(cache, lastExecution);
        val recordedStatistics = recordStatistics(cache, lastExecution);

        val updateCount = updatedStatistics.size();
        val recordCount = recordedStatistics.size();

        val statistics = new ArrayList<ModelStatistic>(updateCount + recordCount);
        statistics.addAll(updatedStatistics);
        statistics.addAll(recordedStatistics);

        statisticRepository.saveAll(statistics);
        log.info("The statistics have been updated({}) and recorded({})", updateCount, recordCount);
    }

    private List<ModelStatistic> updateStatistics(final Map<Long, Integer> cache, final LocalDateTime createDate) {
        val modelIds = cache.keySet();
        val statisticsToUpdate = statisticRepository.findAllByModelIdInAndCreatedDate(modelIds, createDate);
        val updatedStatistics = new ArrayList<ModelStatistic>();

        statisticsToUpdate.forEach(statistic -> {
            val cacheKey = statistic.getModel().getId();
            val cacheRequestCount = cache.get(cacheKey);
            val totalRequestCount = statistic.getRequestCount() + cacheRequestCount;

            statistic.setRequestCount(totalRequestCount);

            updatedStatistics.add(statistic);
            cache.remove(cacheKey);
        });

        return updatedStatistics;
    }

    private List<ModelStatistic> recordStatistics(final Map<Long, Integer> cache, final LocalDateTime createDate) {
        val modelIds = cache.keySet();
        val models = modelRepository.findAllByIdIn(modelIds);
        val recordedStatistics = new ArrayList<ModelStatistic>();

        models.forEach(model -> {
            val cacheKey = model.getId();
            val cacheRequestCount = cache.get(cacheKey);
            val statistic = ModelStatistic.builder()
                                          .requestCount(cacheRequestCount)
                                          .createdDate(createDate)
                                          .model(model)
                                          .build();

            recordedStatistics.add(statistic);
        });

        return recordedStatistics;
    }

    private LocalDateTime getLastExecution() {
        return cronExecution.getLastExecutionBefore(now(UTC))
                            .flatMap(current -> cronExecution.getLastExecutionBefore(current))
                            .map(last -> last.toLocalDateTime().truncatedTo(HOURS))
                            .orElse(null);
    }
}
