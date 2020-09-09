package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.component.migration.MigrationComponent;
import com.exadel.frs.core.trainservice.component.migration.MigrationStatusStorage;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class MigrateControllerTest {

    private static final String URL = "migrate_url";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MigrationStatusStorage migrationStatusStorage;

    @MockBean
    private MigrationComponent migrationComponent;

    @Test
    void migrate() throws Exception {
        mockMvc.perform(post(API_V1 + "/migrate").param("url", URL))
               .andExpect(status().isOk())
               .andExpect(content().string("Migration started"));

        verify(migrationStatusStorage).startMigration();
        verify(migrationComponent).migrate(URL);
        verifyNoMoreInteractions(migrationStatusStorage);
        verifyNoMoreInteractions(migrationComponent);
    }
}