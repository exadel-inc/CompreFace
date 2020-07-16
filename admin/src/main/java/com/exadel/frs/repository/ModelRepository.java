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

package com.exadel.frs.repository;

import com.exadel.frs.entity.Model;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ModelRepository extends JpaRepository<Model, Long> {

    @Query("select distinct m " +
            "from Model m " +
            "left join AppModel am on m.id = am.id.modelId " +
            "where am.id.appId = :appId OR m.app.id = :appId")
    List<Model> findAllByAppId(Long appId);

    Optional<Model> findByGuid(String guid);

    boolean existsByNameAndAppId(String name, Long appId);
}
