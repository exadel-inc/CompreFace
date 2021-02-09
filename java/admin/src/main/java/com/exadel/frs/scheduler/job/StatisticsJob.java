package com.exadel.frs.scheduler.job;

import com.exadel.frs.commonservice.entity.ModelFaceProjection;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.enums.GlobalRole;
import com.exadel.frs.exception.ApperyServiceException;
import com.exadel.frs.repository.FacesRepository;
import com.exadel.frs.repository.InstallInfoRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.UserRepository;
import com.exadel.frs.system.feign.ApperyStatisticsClient;
import com.exadel.frs.system.feign.StatisticsFacesEntity;
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
    private FacesRepository facesRepository;
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
                         final FacesRepository facesRepository, final ModelRepository modelRepository,
                         final UserRepository userRepository) {
        this.apperyStatisticsClient = apperyStatisticsClient;
        this.installInfoRepository = installInfoRepository;
        this.facesRepository = facesRepository;
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

        List<ModelFaceProjection> modelFaces = modelRepository.getModelFacesCount();
        String installGuid = installInfoRepository.findTopByOrderByInstallGuid().getInstallGuid();

        try {
            for (ModelFaceProjection modelFace : modelFaces) {
                apperyStatisticsClient.create(statisticsApiKey, new StatisticsFacesEntity(
                        installGuid, modelFace.getGuid(), getFacesRange((int) modelFace.getFacesCount())
                ));
            }
        } catch (FeignException exception) {
            throw new ApperyServiceException();
        }
    }

    private String getFacesRange(int facesCount) {
        if (facesCount == 0) {
            return "0";
        }

        for (Range range : ranges) {
            if (range.contains(facesCount)) {
                return range.getMinimum() + "-" + range.getMaximum();
            }
        }

        return "1000001+";
    }
}