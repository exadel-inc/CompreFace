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

package com.exadel.frs.core.trainservice.repository.mongo;

import com.exadel.frs.core.trainservice.entity.mongo.Face;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacesRepository extends MongoRepository<Face, String> {

    List<Face> findByApiKey(final String modelApiKey);

    List<Face> deleteByApiKeyAndFaceName(final String modelApiKey, final String faceName);

    List<Face> deleteFacesByApiKey(final String modelApiKey);

    int countByApiKey(final String modelApiKey);

    List<Face> findByIdIn(List<String> ids);
}