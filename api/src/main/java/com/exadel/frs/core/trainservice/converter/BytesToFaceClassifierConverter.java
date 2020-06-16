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

package com.exadel.frs.core.trainservice.converter;

import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import lombok.SneakyThrows;
import lombok.val;
import org.bson.types.Binary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class BytesToFaceClassifierConverter implements Converter<Binary, FaceClassifier> {

    @SneakyThrows
    @Override
    public FaceClassifier convert(final Binary bytes) {
        try (val ois = new ObjectInputStream(
                new ByteArrayInputStream(bytes.getData()))) {

            return (FaceClassifier) ois.readObject();
        }
    }
}
