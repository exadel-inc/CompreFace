package com.exadel.frs.helpers;

import com.exadel.frs.mapper.ModelAccessTypeMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ModelAccessTypeConverter implements AttributeConverter<ModelAccessType, String> {

    @Override
    public String convertToDatabaseColumn(ModelAccessType modelAccessType) {
        return ModelAccessTypeMapper.INSTANCE.fromModelAccessType(modelAccessType);
    }

    @Override
    public ModelAccessType convertToEntityAttribute(String code) {
        return ModelAccessTypeMapper.INSTANCE.toModelAccessType(code);
    }

}
