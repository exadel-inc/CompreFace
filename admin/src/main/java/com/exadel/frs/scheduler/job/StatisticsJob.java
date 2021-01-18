package com.exadel.frs.scheduler.job;

import com.exadel.frs.entity.Face;
import com.exadel.frs.entity.Model;
import com.exadel.frs.exception.ApperyServiceException;
import com.exadel.frs.repository.FacesRepository;
import com.exadel.frs.repository.InstallInfoRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.system.feign.ApperyStatisticsClient;
import com.exadel.frs.system.feign.StatisticsFacesEntity;
import feign.FeignException;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Range;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    public StatisticsJob(final ApperyStatisticsClient apperyStatisticsClient, final InstallInfoRepository installInfoRepository, final FacesRepository facesRepository, final ModelRepository modelRepository) {
        this.apperyStatisticsClient = apperyStatisticsClient;
        this.installInfoRepository = installInfoRepository;
        this.facesRepository = facesRepository;
        this.modelRepository = modelRepository;
    }

    @Override
    public void executeInternal(final JobExecutionContext context) {
        List<Model> models = modelRepository.findAll();
        Map<String, String> facesModelGuidAndRangeMap = new HashMap<>();

        for (Model model : models) {
            String apiKey = model.getApiKey();
            List<Face> faces = facesRepository.findByApiKey(apiKey);
            facesModelGuidAndRangeMap.put(model.getGuid(), getFacesRange(faces.size()));
        }

        try {
            for (Map.Entry<String, String> modelRangeEntry : facesModelGuidAndRangeMap.entrySet()) {
                apperyStatisticsClient.create(statisticsApiKey, new StatisticsFacesEntity(
                        installInfoRepository.findAll().get(0).getInstallGuid(),
                        modelRangeEntry.getKey(), modelRangeEntry.getValue()
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

        List<Range> ranges = List.of(
                Range.between(1, 10),
                Range.between(10, 50),
                Range.between(50, 200),
                Range.between(500, 2000),
                between(2000, 10000),
                between(10000, 50000),
                between(50000, 200000),
                between(200000, 1000000)
        );

        for (Range range : ranges) {
            if (range.contains(facesCount)) {
                return range.getMinimum() + "-" + range.getMaximum();
            }
        }

        return "1000000+";
    }
}