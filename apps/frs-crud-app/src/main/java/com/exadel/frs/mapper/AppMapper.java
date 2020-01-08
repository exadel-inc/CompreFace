package com.exadel.frs.mapper;

import com.exadel.frs.dto.AppDto;
import com.exadel.frs.dto.ui.AppCreateDto;
import com.exadel.frs.dto.ui.AppResponseDto;
import com.exadel.frs.dto.ui.AppUpdateDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.User;
import com.exadel.frs.entity.UserAppRole;
import com.exadel.frs.enums.AppRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserAppRoleMapper.class, AppModelAccessMapper.class, MlModelMapper.class, UserMapper.class})
public interface AppMapper {

    @Mapping(source = "organizationId", target = "organization.id")
    App toEntity(AppDto appDto);
    App toEntity(AppCreateDto appCreateDto);
    App toEntity(AppUpdateDto appUpdateDto);

    @Mapping(source = "organization.id", target = "organizationId")
    AppDto toDto(App app);
    List<AppDto> toDto(List<App> apps);

    @Mapping(source = "userAppRoles", target = "owner", qualifiedByName = "getOwner")
    AppResponseDto toResponseDto(App app);
    List<AppResponseDto> toResponseDto(List<App> apps);

    @Named("getOwner")
    default User getOwner(List<UserAppRole> userAppRoles) {
        return userAppRoles.stream()
                .filter(userAppRole -> userAppRole.getRole().equals(AppRole.OWNER))
                .findFirst()
                .orElseThrow()
                .getUser();
    }

}
