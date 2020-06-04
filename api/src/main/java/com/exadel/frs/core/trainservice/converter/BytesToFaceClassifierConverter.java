package com.exadel.frs.core.trainservice.converter;

import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import lombok.SneakyThrows;
import lombok.val;
import org.bson.types.Binary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

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
