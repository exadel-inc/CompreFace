package com.exadel.frs.commonservice.system.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        url = "${app.feign.appery-io.url}",
        name = "statistics-general"
)
public interface ApperyStatisticsClient {

    String DATABASE_ID_HEADER = "X-Appery-Database-Id";

    @PostMapping(path = "/statistics_general")
    void create(@RequestHeader(value = DATABASE_ID_HEADER) String apiKey, StatisticsGeneralEntity entity);

    @PostMapping(path = "/statistics_faces")
    void create(@RequestHeader(value = DATABASE_ID_HEADER) String apiKey, StatisticsFacesEntity entity);

}