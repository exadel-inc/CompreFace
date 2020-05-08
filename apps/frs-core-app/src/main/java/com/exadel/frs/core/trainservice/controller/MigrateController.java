package com.exadel.frs.core.trainservice.controller;


import com.exadel.frs.core.trainservice.component.migration.MigrationComponent;
import com.exadel.frs.core.trainservice.component.migration.MigrationStatusStorage;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
public class MigrateController {

    private final MigrationComponent migrationComponent;
    private final MigrationStatusStorage migrationStatusStorage;

    @PostMapping(value = "/migrate")
    public ResponseEntity migrate(@RequestParam @Valid @URL final String url) {
        migrationStatusStorage.startMigration();
        migrationComponent.migrate(url);
        return ResponseEntity.ok("Migration started");
    }
}
