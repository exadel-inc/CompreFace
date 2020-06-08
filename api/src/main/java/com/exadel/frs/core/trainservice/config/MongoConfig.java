/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.config;

import com.exadel.frs.core.trainservice.converter.BytesToFaceClassifierConverter;
import com.exadel.frs.core.trainservice.converter.FaceClassifierToBytesConverter;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.util.Collection;
import java.util.List;

@Configuration
class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.uri}")
    private String host;

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new BytesToFaceClassifierConverter(),
                new FaceClassifierToBytesConverter()
        ));
    }

    @Override
    protected Collection<String> getMappingBasePackages() {
        return List.of("com.exadel.frs.core.trainservice.converter");
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(host);
    }

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    public MappingMongoConverter mappingMongoConverter() throws Exception {
        val mappingMongoConverter = super.mappingMongoConverter();
        mappingMongoConverter.setCustomConversions(mongoCustomConversions());

        return mappingMongoConverter;
    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter(), "FACES");
    }
}