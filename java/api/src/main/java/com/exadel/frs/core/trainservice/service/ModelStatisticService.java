package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.commonservice.enums.TableLockName.MODEL_STATISTIC_LOCK;
import com.exadel.frs.commonservice.entity.ModelStatistic;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.ModelStatisticRepository;
import com.exadel.frs.commonservice.repository.TableLockRepository;
import com.exadel.frs.core.trainservice.cache.ModelStatisticCacheProvider;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final TableLockRepository lockRepository;
    private final ModelStatisticRepository statisticRepository;
    private final ModelStatisticCacheProvider statisticCacheProvider;

    @Transactional
    @Scheduled(cron = "${statistic.model.cron-expression}")
    public void updateAndRecordStatistics() {
        if (statisticCacheProvider.isEmpty()) {
            log.info("No statistic to update or record");
            return;
        }

        // Used to obtain a table lock. Only one application instance per time can execute the method.
        lockRepository.lockByName(MODEL_STATISTIC_LOCK);

        val currentDate = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        val cache = statisticCacheProvider.getCacheCopyAsMap();
        statisticCacheProvider.invalidateCache();

        val updatedStatistics = updateStatistics(cache, currentDate);
        val recordedStatistics = recordStatistics(cache, currentDate);

        val updateCount = updatedStatistics.size();
        val recordCount = recordedStatistics.size();

        val statistics = new ArrayList<ModelStatistic>(updateCount + recordCount);
        statistics.addAll(updatedStatistics);
        statistics.addAll(recordedStatistics);

        statisticRepository.saveAll(statistics);
        log.info("The statistics have been updated({}) and recorded({})", updateCount, recordCount);
    }

    private List<ModelStatistic> updateStatistics(final Map<Long, Integer> cache, final LocalDateTime createdDate) {
        val modelIds = cache.keySet();
        val statisticsToUpdate = statisticRepository.findAllByModelIdInAndCreatedDate(modelIds, createdDate);
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

    private List<ModelStatistic> recordStatistics(final Map<Long, Integer> cache, final LocalDateTime createdDate) {
        val modelIds = cache.keySet();
        val models = modelRepository.findAllByIdIn(modelIds);
        val recordedStatistics = new ArrayList<ModelStatistic>();

        models.forEach(model -> {
            val cacheKey = model.getId();
            val cacheRequestCount = cache.get(cacheKey);
            val statistic = ModelStatistic.builder()
                                          .requestCount(cacheRequestCount)
                                          .createdDate(createdDate)
                                          .model(model)
                                          .build();

            recordedStatistics.add(statistic);
        });

        return recordedStatistics;
    }
}
