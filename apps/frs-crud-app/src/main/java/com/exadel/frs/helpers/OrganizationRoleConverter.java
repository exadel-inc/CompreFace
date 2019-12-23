package com.exadel.frs.helpers;

import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.IncorrectOrganizationRoleException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class OrganizationRoleConverter implements AttributeConverter<OrganizationRole, String> {

    @Override
    public String convertToDatabaseColumn(OrganizationRole organizationRole) {
        return organizationRole == null ? null : organizationRole.getCode();
    }

    @Override
    public OrganizationRole convertToEntityAttribute(String code) {
        return code == null ? null : Stream.of(OrganizationRole.values())
                .filter(organizationRole -> organizationRole.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IncorrectOrganizationRoleException(code));
    }

}
