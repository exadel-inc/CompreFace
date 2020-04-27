package com.exadel.frs.core.trainservice.config;

import de.flapdoodle.embed.mongo.config.IMongodConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(MongoConfig.class)
public class MongoTestConfig {

    @Autowired
    public IMongodConfig iMongodConfig;

    @Autowired
    public void overrideProperties() {
        System.setProperty("spring.data.mongodb.uri", "mongodb://localhost:" + iMongodConfig.net().getPort());
    }


}
