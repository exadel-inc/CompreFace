package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.component.migration.MigrationComponent;
import com.exadel.frs.core.trainservice.config.AsyncConfiguration;
import com.exadel.frs.core.trainservice.config.MongoTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;

@SpringBootTest
@Slf4j
@ContextConfiguration(classes = MongoTestConfig.class)
@EnabledIf(expression = "#{environment.acceptsProfiles('integration-test')}")
@ComponentScan(basePackages = {"com.exadel.frs.core.trainservice"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {AsyncConfiguration.class})})
public class MigrationComponentITest {

    @Autowired
    private MigrationComponent migrationComponent;

    @Test
    public void testMigration() {
        migrationComponent.migrate("mock");
    }

}
