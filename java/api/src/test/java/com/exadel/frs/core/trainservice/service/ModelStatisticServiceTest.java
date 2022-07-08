package com.exadel.frs.core.trainservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.entity.ModelStatistic;
import com.exadel.frs.commonservice.repository.ModelStatisticRepository;
import com.exadel.frs.core.trainservice.DbHelper;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.cache.ModelStatisticCacheProvider;
import java.util.List;
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

    @Test
    void shouldNotUpdateOrRecordAnyStatisticsWhenCacheIsEmpty() {
        var countBefore = statisticRepository.count();

        statisticService.updateAndRecordStatistics();

        var countAfter = statisticRepository.count();

        assertThat(countBefore).isZero();
        assertThat(countAfter).isZero();
    }

    @Test
    void shouldRecordStatisticWhenItExistsInCacheAndDoesNotExistInDatabase() {
        var model = dbHelper.insertModel();

        statisticCacheProvider.incrementRequestCount(model.getId());
        statisticCacheProvider.incrementRequestCount(model.getId());
        statisticCacheProvider.incrementRequestCount(model.getId());

        assertThat(statisticRepository.count()).isZero();

        statisticService.updateAndRecordStatistics();

        assertThat(statisticRepository.count()).isEqualTo(1);

        var statistics = statisticRepository.findAll();

        assertThat(statistics).isNotNull().isNotEmpty();

        var actual = statistics.get(0);

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull().isPositive();
        assertThat(actual.getRequestCount()).isEqualTo(3);
        assertThat(actual.getModel().getId()).isEqualTo(model.getId());
    }

    @Test
    void shouldUpdateStatisticWhenItExistsInCacheAndDatabase() {
        var model = dbHelper.insertModel();
        dbHelper.insertModelStatistic(5, model);

        statisticCacheProvider.incrementRequestCount(model.getId());
        statisticCacheProvider.incrementRequestCount(model.getId());

        assertThat(statisticRepository.count()).isEqualTo(1);

        statisticService.updateAndRecordStatistics();

        assertThat(statisticRepository.count()).isEqualTo(1);

        var statistics = statisticRepository.findAll();

        assertThat(statistics).isNotNull().isNotEmpty();

        var actual = statistics.get(0);

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull().isPositive();
        assertThat(actual.getRequestCount()).isEqualTo(7);
        assertThat(actual.getModel().getId()).isEqualTo(model.getId());
    }

    @Test
    void shouldUpdateStatisticWhenItExistsInCacheAndDatabaseAndRecordStatisticWhenItExistsInCacheButDoesNotInDatabase() {
        var model1 = dbHelper.insertModel();
        var model2 = dbHelper.insertModel();

        dbHelper.insertModelStatistic(5, model1);

        statisticCacheProvider.incrementRequestCount(model1.getId());
        statisticCacheProvider.incrementRequestCount(model1.getId());

        statisticCacheProvider.incrementRequestCount(model2.getId());
        statisticCacheProvider.incrementRequestCount(model2.getId());
        statisticCacheProvider.incrementRequestCount(model2.getId());

        assertThat(statisticRepository.count()).isEqualTo(1);

        statisticService.updateAndRecordStatistics();

        assertThat(statisticRepository.count()).isEqualTo(2);

        var statistics = statisticRepository.findAll();

        var updatedActual = getStatisticByModel(statistics, model1);
        var recordedActual = getStatisticByModel(statistics, model2);

        assertThat(updatedActual).isNotNull();
        assertThat(updatedActual.getCreatedDate()).isNotNull();
        assertThat(updatedActual.getRequestCount()).isEqualTo(7);
        assertThat(updatedActual.getModel().getId()).isEqualTo(model1.getId());

        assertThat(recordedActual).isNotNull();
        assertThat(recordedActual.getCreatedDate()).isNotNull();
        assertThat(recordedActual.getRequestCount()).isEqualTo(3);
        assertThat(recordedActual.getModel().getId()).isEqualTo(model2.getId());
    }

    private ModelStatistic getStatisticByModel(List<ModelStatistic> statistics, Model model) {
        return statistics.stream()
                         .filter(e -> e.getModel().getId().equals(model.getId()))
                         .findFirst()
                         .orElse(null);
    }
}
