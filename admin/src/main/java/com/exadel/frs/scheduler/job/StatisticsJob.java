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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class StatisticsJob implements Job {

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
    public void execute(final JobExecutionContext context) {
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
        String range = "0";

        if (facesCount == 0) {
            return range;
        } else if (facesCount >= 1 && facesCount <= 10) {
            range = "1-10";
        } else if (facesCount >= 10 && facesCount <= 50) {
            range = "10-50";
        } else if (facesCount >= 50 && facesCount <= 200) {
            range = "50-200";
        } else if (facesCount >= 200 && facesCount <= 500) {
            range = "200-500";
        } else if (facesCount >= 500 && facesCount <= 2000) {
            range = "500-2000";
        } else if (facesCount >= 2000 && facesCount <= 10000) {
            range = "2000-10000";
        } else if (facesCount >= 10000 && facesCount <= 50000) {
            range = "10000-50000";
        } else if (facesCount >= 50000 && facesCount <= 200000) {
            range = "50000-200000";
        } else if (facesCount >= 200000 && facesCount <= 1000000) {
            range = "200000-1000000";
        } else {
            range = "1000000+";
        }
        return range;
    }
}
