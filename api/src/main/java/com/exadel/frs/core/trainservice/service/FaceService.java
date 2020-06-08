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

package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.enums.RetrainOption.getTrainingOption;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encode;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import com.exadel.frs.core.trainservice.system.SystemService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaceService {

    private final FaceDao faceDao;
    private final ModelDao modelDao;
    private final SystemService systemService;
    private final RetrainService retrainService;
    private final FaceClassifierManager classifierManager;

    public Map<String, List<String>> findAllFaceNames(final String apiKey) {
        val token = systemService.buildToken(apiKey);

        return faceDao.findAllFaceNamesByApiKey(token.getModelApiKey());
    }

    public void deleteFaceByName(
            final String faceName,
            final String apiKey,
            final String retrain
    ) {
        val faceNameEncoded = encode(faceName, UTF_8);
        val token = systemService.buildToken(apiKey);

        faceDao.deleteFaceByName(faceNameEncoded, token.getModelApiKey());

        getTrainingOption(retrain).run(token, retrainService);
    }

    public int deleteFacesByModel(final String modelKey) {
        val token = systemService.buildToken(modelKey);
        classifierManager.removeFaceClassifier(token.getModelApiKey());
        val deletedFaces = faceDao.deleteFacesByApiKey(token.getModelApiKey());

        return deletedFaces.size();
    }

}