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

package com.exadel.frs.validation;

import static com.google.common.io.Files.getFileExtension;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import com.exadel.frs.exception.FileExtensionException;
import com.exadel.frs.system.global.ImageProperties;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ImageExtensionValidator {

    private final ImageProperties imageProperties;

    public void validate(final Collection<MultipartFile> files) {
        val formats = imageProperties.getTypes();
        val wrongFileNames = files.stream()
                                  .filter(file ->
                                          !formats.contains(
                                                  getFileExtension(file.getOriginalFilename().toLowerCase())
                                          )
                                  )
                                  .map(file -> file.getOriginalFilename())
                                  .collect(toList());

        if (isNotEmpty(wrongFileNames)) {
            throw new FileExtensionException(wrongFileNames);
        }
    }
}