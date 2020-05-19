package com.exadel.frs.mapper;

import com.exadel.frs.dto.ui.AppOwnerDto;
import com.exadel.frs.dto.ui.UserResponseDto;
import com.exadel.frs.dto.ui.UserUpdateResponseDto;
import com.exadel.frs.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {UserOrgRoleMapper.class, UserAppRoleMapper.class})
public interface UserMapper {

    @Mapping(source = "guid", target = "userId")
    AppOwnerDto toAppOwnerDto(User entity);

    @Mapping(source = "guid", target = "userId")
    UserResponseDto toResponseDto(User entity);

    UserUpdateResponseDto toUserUpdateResponseDto(User user);

    List<UserResponseDto> toResponseDto(List<User> entities);
}
