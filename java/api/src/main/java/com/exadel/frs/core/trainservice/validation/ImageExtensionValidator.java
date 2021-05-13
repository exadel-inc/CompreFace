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

package com.exadel.frs.core.trainservice.validation;

import com.exadel.frs.commonservice.exception.FileExtensionException;
import com.exadel.frs.commonservice.exception.InvalidBase64Exception;
import com.exadel.frs.commonservice.system.global.ImageProperties;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import static com.google.common.io.Files.getFileExtension;

@Component
@RequiredArgsConstructor
public class ImageExtensionValidator {

    private final ImageProperties imageProperties;

    public void validate(final MultipartFile file) {
        if (file == null) {
            return;
        }

        val formats = imageProperties.getTypes();
        String originalFilename = file.getOriginalFilename();
        val isWrongFormat = StringUtils.isEmpty(originalFilename) || !formats.contains(getFileExtension(originalFilename.toLowerCase()));

        if (isWrongFormat) {
            throw new FileExtensionException(originalFilename);
        }
    }

    public void validateBase64(final String base64Image) {
        if (!Base64.isBase64(base64Image)) {
            throw new InvalidBase64Exception();
        }
    }
}