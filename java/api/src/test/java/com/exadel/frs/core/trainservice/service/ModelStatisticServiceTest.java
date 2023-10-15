package com.exadel.frs.core.trainservice.service;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThat;
import com.exadel.frs.commonservice.repository.ModelStatisticRepository;
import com.exadel.frs.core.trainservice.DbHelper;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.cache.ModelStatisticCacheProvider;
import com.exadel.frs.core.trainservice.util.CronExecution;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import javax.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Transactional
class ModelStatisticServiceTest extends EmbeddedPostgreSQLTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ModelStatisticCacheProvider statisticCacheProvider;

    @Autowired
    private ModelStatisticRepository statisticRepository;

    @Autowired
    private ModelStatisticService statisticService;

    private final CronExecution cronExecution = new CronExecution("0 0 * ? * *");

    @Test
    void shouldTerminateExecutionWithoutUpdateOrRecordStatistic() {
        assertThat(statisticRepository.count()).isZero();

        statisticService.updateAndRecordStatistics();

        assertThat(statisticRepository.count()).isZero();
    }

    @Test
    void shouldRecordOneStatistic() {
        var model = dbHelper.insertModel();

        statisticCacheProvider.incrementRequestCount(model.getId());
        statisticCacheProvider.incrementRequestCount(model.getId());
        statisticCacheProvider.incrementRequestCount(model.getId());

        assertThat(statisticRepository.count()).isZero();

        statisticService.updateAndRecordStatistics();

        assertThat(statisticRepository.count()).isEqualTo(1);

        var statistics = statisticRepository.findAll();
        var actual = statistics.get(0);

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isPositive();
        assertThat(actual.getCreatedDate()).isBefore(now(UTC));
        assertThat(actual.getRequestCount()).isEqualTo(3);
        assertThat(actual.getModel().getId()).isEqualTo(model.getId());
    }

    @Test
    void shouldRecordThreeStatistics() {
        var model1 = dbHelper.insertModel();
        var model2 = dbHelper.insertModel();
        var model3 = dbHelper.insertModel();

        statisticCacheProvider.incrementRequestCount(model1.getId());
        statisticCacheProvider.incrementRequestCount(model2.getId());
        statisticCacheProvider.incrementRequestCount(model2.getId());
        statisticCacheProvider.incrementRequestCount(model3.getId());
        statisticCacheProvider.incrementRequestCount(model3.getId());
        statisticCacheProvider.incrementRequestCount(model3.getId());

        assertThat(statisticRepository.count()).isZero();

        statisticService.updateAndRecordStatistics();

        assertThat(statisticRepository.count()).isEqualTo(3);

        var statistics = statisticRepository.findAll();

        var optional1 = statistics.stream().filter(e -> e.getModel().getId().equals(model1.getId())).findFirst();
        var optional2 = statistics.stream().filter(e -> e.getModel().getId().equals(model2.getId())).findFirst();
        var optional3 = statistics.stream().filter(e -> e.getModel().getId().equals(model3.getId())).findFirst();

        assertThat(optional1).isPresent();
        assertThat(optional2).isPresent();
        assertThat(optional3).isPresent();

        var actual1 = optional1.get();
        var actual2 = optional2.get();
        var actual3 = optional3.get();

        assertThat(actual1.getId()).isPositive();
        assertThat(actual1.getCreatedDate()).isBefore(now(UTC));
        assertThat(actual1.getRequestCount()).isEqualTo(1);
        assertThat(actual1.getModel().getId()).isEqualTo(model1.getId());

        assertThat(actual2.getId()).isPositive();
        assertThat(actual2.getCreatedDate()).isBefore(now(UTC));
        assertThat(actual2.getRequestCount()).isEqualTo(2);
        assertThat(actual2.getModel().getId()).isEqualTo(model2.getId());

        assertThat(actual3.getId()).isPositive();
        assertThat(actual3.getCreatedDate()).isBefore(now(UTC));
        assertThat(actual3.getRequestCount()).isEqualTo(3);
        assertThat(actual3.getModel().getId()).isEqualTo(model3.getId());
    }

    @Test
    void shouldUpdateOneStatistic() {
        var model = dbHelper.insertModel();
        var statistic = dbHelper.insertModelStatistic(model, 5, getCreateDate());

        statisticCacheProvider.incrementRequestCount(model.getId());
        statisticCacheProvider.incrementRequestCount(model.getId());
        statisticCacheProvider.incrementRequestCount(model.getId());

        assertThat(statisticRepository.count()).isEqualTo(1);

        statisticService.updateAndRecordStatistics();

        assertThat(statisticRepository.count()).isEqualTo(1);

        var statistics = statisticRepository.findAll();
        var actual = statistics.get(0);

        assertThat(actual.getId()).isPositive();
        assertThat(actual.getCreatedDate()).isBefore(now(UTC));
        assertThat(actual.getRequestCount()).isEqualTo(8);
        assertThat(actual.getModel().getId()).isEqualTo(model.getId());
    }

    @Test
    void shouldUpdateThreeStatistics() {
        var model1 = dbHelper.insertModel();
        var model2 = dbHelper.insertModel();
        var model3 = dbHelper.insertModel();

        var statistic1 = dbHelper.insertModelStatistic(model1, 1, getCreateDate());
        var statistic2 = dbHelper.insertModelStatistic(model2, 2, getCreateDate());
        var statistic3 = dbHelper.insertModelStatistic(model3, 3, getCreateDate());

        statisticCacheProvider.incrementRequestCount(model1.getId());
        statisticCacheProvider.incrementRequestCount(model2.getId());
        statisticCacheProvider.incrementRequestCount(model2.getId());
        statisticCacheProvider.incrementRequestCount(model3.getId());
        statisticCacheProvider.incrementRequestCount(model3.getId());
        statisticCacheProvider.incrementRequestCount(model3.getId());

        assertThat(statisticRepository.count()).isEqualTo(3);

        statisticService.updateAndRecordStatistics();

        assertThat(statisticRepository.count()).isEqualTo(3);

        var statistics = statisticRepository.findAll();

        var optional1 = statistics.stream().filter(e -> e.getModel().getId().equals(model1.getId())).findFirst();
        var optional2 = statistics.stream().filter(e -> e.getModel().getId().equals(model2.getId())).findFirst();
        var optional3 = statistics.stream().filter(e -> e.getModel().getId().equals(model3.getId())).findFirst();

        assertThat(optional1).isPresent();
        assertThat(optional2).isPresent();
        assertThat(optional3).isPresent();

        var actual1 = optional1.get();
        var actual2 = optional2.get();
        var actual3 = optional3.get();

        assertThat(actual1.getId()).isPositive();
        assertThat(actual1.getCreatedDate()).isBefore(now(UTC));
        assertThat(actual1.getRequestCount()).isEqualTo(2);
        assertThat(actual1.getModel().getId()).isEqualTo(model1.getId());

        assertThat(actual2.getId()).isPositive();
        assertThat(actual2.getCreatedDate()).isBefore(now(UTC));
        assertThat(actual2.getRequestCount()).isEqualTo(4);
        assertThat(actual2.getModel().getId()).isEqualTo(model2.getId());

        assertThat(actual3.getId()).isPositive();
        assertThat(actual3.getCreatedDate()).isBefore(now(UTC));
        assertThat(actual3.getRequestCount()).isEqualTo(6);
        assertThat(actual3.getModel().getId()).isEqualTo(model3.getId());
    }

    @Test
    void shouldUpdateOneStatisticAndRecordOneStatistic() {
        var model1 = dbHelper.insertModel();
        var model2 = dbHelper.insertModel();

        var statisticToUpdate = dbHelper.insertModelStatistic(model1, 1, getCreateDate());

        statisticCacheProvider.incrementRequestCount(model1.getId());
        statisticCacheProvider.incrementRequestCount(model1.getId());
        statisticCacheProvider.incrementRequestCount(model2.getId());
        statisticCacheProvider.incrementRequestCount(model2.getId());

        assertThat(statisticRepository.count()).isEqualTo(1);

        statisticService.updateAndRecordStatistics();

        assertThat(statisticRepository.count()).isEqualTo(2);

        var statistics = statisticRepository.findAll();

        var updatedStatisticOptional = statistics.stream().filter(e -> e.getModel().getId().equals(model1.getId())).findFirst();
        var recordedStatisticOptional = statistics.stream().filter(e -> e.getModel().getId().equals(model2.getId())).findFirst();

        assertThat(updatedStatisticOptional).isPresent();
        assertThat(recordedStatisticOptional).isPresent();

        var updatedStatistic = updatedStatisticOptional.get();
        var recordedStatistic = recordedStatisticOptional.get();

        assertThat(updatedStatistic.getId()).isPositive();
        assertThat(updatedStatistic.getCreatedDate()).isBefore(now(UTC));
        assertThat(updatedStatistic.getRequestCount()).isEqualTo(3);
        assertThat(updatedStatistic.getModel().getId()).isEqualTo(model1.getId());

        assertThat(recordedStatistic.getId()).isPositive();
        assertThat(recordedStatistic.getCreatedDate()).isBefore(now(UTC));
        assertThat(recordedStatistic.getRequestCount()).isEqualTo(2);
        assertThat(recordedStatistic.getModel().getId()).isEqualTo(model2.getId());
    }

    @Test
    void shouldUpdateThreeStatisticsAndRecordThreeStatistics() {
        var model1 = dbHelper.insertModel();
        var model2 = dbHelper.insertModel();
        var model3 = dbHelper.insertModel();
        var model4 = dbHelper.insertModel();
        var model5 = dbHelper.insertModel();
        var model6 = dbHelper.insertModel();

        var statisticToUpdate1 = dbHelper.insertModelStatistic(model1, 1, getCreateDate());
        var statisticToUpdate2 = dbHelper.insertModelStatistic(model2, 2, getCreateDate());
        var statisticToUpdate3 = dbHelper.insertModelStatistic(model3, 3, getCreateDate());

        statisticCacheProvider.incrementRequestCount(model1.getId());
        statisticCacheProvider.incrementRequestCount(model1.getId());
        statisticCacheProvider.incrementRequestCount(model2.getId());
        statisticCacheProvider.incrementRequestCount(model2.getId());
        statisticCacheProvider.incrementRequestCount(model3.getId());
        statisticCacheProvider.incrementRequestCount(model3.getId());

        statisticCacheProvider.incrementRequestCount(model4.getId());
        statisticCacheProvider.incrementRequestCount(model5.getId());
        statisticCacheProvider.incrementRequestCount(model5.getId());
        statisticCacheProvider.incrementRequestCount(model6.getId());
        statisticCacheProvider.incrementRequestCount(model6.getId());
        statisticCacheProvider.incrementRequestCount(model6.getId());

        assertThat(statisticRepository.count()).isEqualTo(3);

        statisticService.updateAndRecordStatistics();

        assertThat(statisticRepository.count()).isEqualTo(6);

        var statistics = statisticRepository.findAll();

        var updatedStatisticOptional1 = statistics.stream().filter(e -> e.getModel().getId().equals(model1.getId())).findFirst();
        var updatedStatisticOptional2 = statistics.stream().filter(e -> e.getModel().getId().equals(model2.getId())).findFirst();
        var updatedStatisticOptional3 = statistics.stream().filter(e -> e.getModel().getId().equals(model3.getId())).findFirst();
        var recordedStatisticOptional1 = statistics.stream().filter(e -> e.getModel().getId().equals(model4.getId())).findFirst();
        var recordedStatisticOptional2 = statistics.stream().filter(e -> e.getModel().getId().equals(model5.getId())).findFirst();
        var recordedStatisticOptional3 = statistics.stream().filter(e -> e.getModel().getId().equals(model6.getId())).findFirst();

        assertThat(updatedStatisticOptional1).isPresent();
        assertThat(updatedStatisticOptional2).isPresent();
        assertThat(updatedStatisticOptional3).isPresent();
        assertThat(recordedStatisticOptional1).isPresent();
        assertThat(recordedStatisticOptional2).isPresent();
        assertThat(recordedStatisticOptional3).isPresent();

        var updatedStatistic1 = updatedStatisticOptional1.get();
        var updatedStatistic2 = updatedStatisticOptional2.get();
        var updatedStatistic3 = updatedStatisticOptional3.get();
        var recordedStatistic1 = recordedStatisticOptional1.get();
        var recordedStatistic2 = recordedStatisticOptional2.get();
        var recordedStatistic3 = recordedStatisticOptional3.get();

        assertThat(updatedStatistic1.getId()).isPositive();
        assertThat(updatedStatistic1.getCreatedDate()).isBefore(now(UTC));
        assertThat(updatedStatistic1.getRequestCount()).isEqualTo(3);
        assertThat(updatedStatistic1.getModel().getId()).isEqualTo(model1.getId());

        assertThat(updatedStatistic2.getId()).isPositive();
        assertThat(updatedStatistic2.getCreatedDate()).isBefore(now(UTC));
        assertThat(updatedStatistic2.getRequestCount()).isEqualTo(4);
        assertThat(updatedStatistic2.getModel().getId()).isEqualTo(model2.getId());

        assertThat(updatedStatistic3.getId()).isPositive();
        assertThat(updatedStatistic3.getCreatedDate()).isBefore(now(UTC));
        assertThat(updatedStatistic3.getRequestCount()).isEqualTo(5);
        assertThat(updatedStatistic3.getModel().getId()).isEqualTo(model3.getId());

        assertThat(recordedStatistic1.getId()).isPositive();
        assertThat(recordedStatistic1.getCreatedDate()).isBefore(now(UTC));
        assertThat(recordedStatistic1.getRequestCount()).isEqualTo(1);
        assertThat(recordedStatistic1.getModel().getId()).isEqualTo(model4.getId());

        assertThat(recordedStatistic2.getId()).isPositive();
        assertThat(recordedStatistic2.getCreatedDate()).isBefore(now(UTC));
        assertThat(recordedStatistic2.getRequestCount()).isEqualTo(2);
        assertThat(recordedStatistic2.getModel().getId()).isEqualTo(model5.getId());

        assertThat(recordedStatistic3.getId()).isPositive();
        assertThat(recordedStatistic3.getCreatedDate()).isBefore(now(UTC));
        assertThat(recordedStatistic3.getRequestCount()).isEqualTo(3);
        assertThat(recordedStatistic3.getModel().getId()).isEqualTo(model6.getId());
    }

    private LocalDateTime getCreateDate() {
        return cronExecution.getLastExecutionBefore(ZonedDateTime.now(UTC))
                            .flatMap(cronExecution::getLastExecutionBefore)
                            .map(last -> last.toLocalDateTime().truncatedTo(HOURS))
                            .orElse(null);
    }
}
