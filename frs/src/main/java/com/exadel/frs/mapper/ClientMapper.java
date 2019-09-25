package com.exadel.frs.mapper;

import com.exadel.frs.dto.ClientDto;
import com.exadel.frs.entity.Client;
import org.mapstruct.Mapper;

@Mapper
public interface ClientMapper {

    ClientDto toDto(Client entity);

    Client toEntity(ClientDto dto);

}
