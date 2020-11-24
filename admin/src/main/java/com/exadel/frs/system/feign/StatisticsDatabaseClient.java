package com.exadel.frs.system.feign;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        url = "${app.feign.appery-io.url}",
        name = "statistics-general"
)
public interface StatisticsDatabaseClient {

    String DATABASE_ID_HEADER = "X-Appery-Database-Id";

    @PostMapping(path = "/statistics_general", produces = APPLICATION_JSON_VALUE)
    void create(@RequestHeader(value = DATABASE_ID_HEADER) String apiKey, StatisticsGeneralEntity entity);

    @PostMapping(path = "/statistics_faces", produces = APPLICATION_JSON_VALUE)
    void create(@RequestHeader(value = DATABASE_ID_HEADER) String apiKey, StatisticsFacesEntity entity);

}
