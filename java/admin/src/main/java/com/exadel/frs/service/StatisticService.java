package com.exadel.frs.service;

import static com.exadel.frs.commonservice.enums.GlobalRole.OWNER;
import static org.apache.commons.lang3.Range.between;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import com.exadel.frs.commonservice.entity.InstallInfo;
import com.exadel.frs.commonservice.projection.ModelSubjectProjection;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.repository.InstallInfoRepository;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.UserRepository;
import com.exadel.frs.commonservice.system.feign.ApperyStatisticsClient;
import com.exadel.frs.commonservice.system.feign.StatisticsFacesEntity;
import feign.FeignException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticService {

    private static final List<Range<Long>> RANGES = List.of(
            between(1L, 10L), between(11L, 50L), between(51L, 200L),
            between(201L, 500L), between(501L, 2000L), between(2001L, 10000L),
            between(10001L, 50000L), between(50001L, 200000L), between(200001L, 1000000L)
    );

    @Value("${app.feign.appery-io.api-key}")
    private String apperyApiKey;

    private final ApperyStatisticsClient apperyClient;
    private final InstallInfoRepository installInfoRepository;
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;

    @Scheduled(cron = "@midnight", zone = "UTC")
    public void recordStatistics() {
        boolean statisticsAreNotAllowed = !areStatisticsAllowed();
        if (statisticsAreNotAllowed) {
            log.info("Statistics are not allowed");
            return;
        }

        List<ModelSubjectProjection> subjectCountPerModel = modelRepository.getModelSubjectsCount();
        InstallInfo installInfo = installInfoRepository.findTopByOrderByInstallGuid();

        if (installInfo == null) {
            log.warn("In order to record statistics, at least one InstallInfo must be present");
            return;
        }

        List<StatisticsFacesEntity> statistics = createStatistics(installInfo.getInstallGuid(), subjectCountPerModel);
        sendStatistics(statistics);
    }

    private boolean areStatisticsAllowed() {
        if (isNotBlank(apperyApiKey)) {
            User owner = userRepository.findByGlobalRole(OWNER);
            return owner != null && owner.isAllowStatistics();
        } else {
            return false;
        }
    }

    private List<StatisticsFacesEntity> createStatistics(String installInfoGuid, List<ModelSubjectProjection> subjectCountPerModel) {
        return subjectCountPerModel.stream()
                                   .map(subjectCount -> createStatistic(installInfoGuid, subjectCount))
                                   .toList();
    }

    private StatisticsFacesEntity createStatistic(String installInfoGuid, ModelSubjectProjection subjectCount) {
        return new StatisticsFacesEntity(
                installInfoGuid,
                subjectCount.guid(),
                getSubjectRange(subjectCount.subjectCount())
        );
    }

    private String getSubjectRange(Long subjectCount) {
        if (subjectCount == null || subjectCount == 0) {
            return "0";
        }

        return RANGES.stream()
                     .filter(range -> range.contains(subjectCount))
                     .map(range -> range.getMinimum() + "-" + range.getMaximum())
                     .findFirst()
                     .orElse("1000001+");
    }

    private void sendStatistics(List<StatisticsFacesEntity> statistics) {
        try {
            statistics
                    .forEach(statistic -> apperyClient.create(apperyApiKey, statistic));
        } catch (FeignException e) {
            log.info(e.getMessage());
        }
    }
}
