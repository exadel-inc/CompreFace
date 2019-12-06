package com.exadel.frs.mapper;

import com.exadel.frs.dto.UserDto;
import com.exadel.frs.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserOrganizationRoleMapper.class, UserAppRoleMapper.class})
public interface UserMapper {

    User toEntity(UserDto dto);

    UserDto toDto(User entity);

}
