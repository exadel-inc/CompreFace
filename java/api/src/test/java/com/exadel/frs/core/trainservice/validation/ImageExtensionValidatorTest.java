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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.exadel.frs.commonservice.exception.FileExtensionException;
import com.exadel.frs.commonservice.system.global.ImageProperties;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.multipart.MultipartFile;

class ImageExtensionValidatorTest {

    @Mock
    private ImageProperties imageProperties;

    @InjectMocks
    private ImageExtensionValidator validator;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    public void validateFileWithExpectedExtension() {
        val file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("image.jpg");
        when(imageProperties.getTypes()).thenReturn(List.of("jpg"));

        validator.validate(file);

        verify(file).getOriginalFilename();
        verify(imageProperties).getTypes();
        verifyNoMoreInteractions(imageProperties);
    }

    @Test
    public void validateFileUppercaseExtension() {
        val file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("IMAGE.JPG");
        when(imageProperties.getTypes()).thenReturn(List.of("jpg"));

        validator.validate(file);

        verify(file).getOriginalFilename();
        verify(imageProperties).getTypes();
        verifyNoMoreInteractions(imageProperties);
    }

    @Test
    public void validateNull() {
        validator.validate(null);
    }

    @Test
    public void exceptionIfFileWithWrongExtension() {
        val file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("image.tmp");

        assertThatThrownBy(() ->
                validator.validate(file)
        ).isInstanceOf(FileExtensionException.class);
    }
}