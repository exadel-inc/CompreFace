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

package com.exadel.frs.service;

import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.dto.ModelCloneDto;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelCloneService {

    private final ModelRepository modelRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Model cloneModel(Model model, ModelCloneDto modelCloneDto) {
        val clone = new Model(model);
        clone.setId(null);
        clone.setName(modelCloneDto.getName());

        return modelRepository.save(clone);
    }
}
