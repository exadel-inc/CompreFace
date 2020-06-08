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

import com.exadel.frs.entity.ModelShareRequest;
import com.exadel.frs.entity.ModelShareRequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ModelShareRequestRepository extends JpaRepository<ModelShareRequest, ModelShareRequestId> {

    @Query("select m from ModelShareRequest m where m.id.requestId = :requestId")
    ModelShareRequest findModelShareRequestByRequestId(UUID requestId);
}