package com.exadel.frs.helpers;

import com.exadel.frs.mapper.ModelAccessTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
@Component
public class ModelAccessTypeConverter implements AttributeConverter<ModelAccessType, String> {

    @Autowired
    @Qualifier("modelAccessTypeMapperImpl")
    private ModelAccessTypeMapper modelAccessTypeMapper;

    @Override
    public String convertToDatabaseColumn(ModelAccessType modelAccessType) {
        return modelAccessTypeMapper.fromModelAccessType(modelAccessType);
    }

    @Override
    public ModelAccessType convertToEntityAttribute(String code) {
        return modelAccessTypeMapper.toModelAccessType(code);
    }

}
