package com.exadel.frs.mapper;

import com.exadel.frs.dto.AppDto;
import com.exadel.frs.entity.App;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserAppRoleMapper.class, AppModelAccessMapper.class, MlModelMapper.class})
public interface AppMapper {

    @Mapping(source = "organizationId", target = "organization.id")
    App toEntity(AppDto appDto);

    @Mapping(source = "organization.id", target = "organizationId")
    AppDto toDto(App app);

    List<AppDto> toDto(List<App> apps);

}
