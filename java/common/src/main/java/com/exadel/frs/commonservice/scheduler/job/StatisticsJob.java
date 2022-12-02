package com.exadel.frs.commonservice.scheduler.job;

import com.exadel.frs.commonservice.projection.ModelSubjectProjection;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.enums.GlobalRole;
import com.exadel.frs.commonservice.exception.ApperyServiceException;
import com.exadel.frs.commonservice.repository.InstallInfoRepository;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.UserRepository;
import com.exadel.frs.commonservice.system.feign.ApperyStatisticsClient;
import com.exadel.frs.commonservice.system.feign.StatisticsFacesEntity;
import feign.FeignException;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.Range.between;

@NoArgsConstructor
@Component
public class StatisticsJob extends QuartzJobBean {

    @Value("${app.feign.appery-io.api-key}")
    private String statisticsApiKey;
    private ApperyStatisticsClient apperyStatisticsClient;
    private InstallInfoRepository installInfoRepository;
    private ModelRepository modelRepository;
    private UserRepository userRepository;

    private final List<Range<Long>> ranges = List.of(
            Range.between(1L, 10L),
            Range.between(11L, 50L),
            Range.between(51L, 200L),
            Range.between(201L, 500L),
            Range.between(501L, 2000L),
            between(2001L, 10000L),
            between(10001L, 50000L),
            between(50001L, 200000L),
            between(200001L, 1000000L)
    );

    @Autowired
    public StatisticsJob(final ApperyStatisticsClient apperyStatisticsClient, final InstallInfoRepository installInfoRepository,
                         final ModelRepository modelRepository,
                         final UserRepository userRepository) {
        this.apperyStatisticsClient = apperyStatisticsClient;
        this.installInfoRepository = installInfoRepository;
        this.modelRepository = modelRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void executeInternal(final JobExecutionContext context) {
        if (StringUtils.isEmpty(statisticsApiKey)) {
            return;
        }

        User user = userRepository.findByGlobalRole(GlobalRole.OWNER);

        if (Objects.isNull(user)) {
            return;
        }

        if (!user.isAllowStatistics()) {
            return;
        }

        List<ModelSubjectProjection> projections = modelRepository.getModelSubjectsCount();
        String installGuid = installInfoRepository.findTopByOrderByInstallGuid().getInstallGuid();

        try {
            for (ModelSubjectProjection projection : projections) {
                apperyStatisticsClient.create(statisticsApiKey, new StatisticsFacesEntity(
                        installGuid, projection.guid(), getSubjectsRange(projection.subjectCount())
                ));
            }
        } catch (FeignException exception) {
            throw new ApperyServiceException();
        }
    }

    private String getSubjectsRange(Long subjectCount) {
        if (subjectCount == 0) {
            return "0";
        }

        for (Range<Long> range : ranges) {
            if (range.contains(subjectCount)) {
                return range.getMinimum() + "-" + range.getMaximum();
            }
        }

        return "1000001+";
    }
}
