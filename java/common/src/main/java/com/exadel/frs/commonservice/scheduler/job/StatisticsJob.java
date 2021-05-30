package com.exadel.frs.commonservice.scheduler.job;

import com.exadel.frs.commonservice.entity.ModelSubjectProjection;
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

    private List<Range> ranges = List.of(
            Range.between(1, 10),
            Range.between(11, 50),
            Range.between(51, 200),
            Range.between(201, 500),
            Range.between(501, 2000),
            between(2001, 10000),
            between(10001, 50000),
            between(50001, 200000),
            between(200001, 1000000)
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
                        installGuid, projection.getGuid(), getSubjectsRange(projection.getSubjectCount())
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

        for (Range range : ranges) {
            if (range.contains(subjectCount)) {
                return range.getMinimum() + "-" + range.getMaximum();
            }
        }

        return "1000001+";
    }
}