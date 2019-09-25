package com.exadel.frs.service;

import com.exadel.frs.dto.ClientDto;
import com.exadel.frs.entity.Client;
import com.exadel.frs.mapper.ClientMapper;
import com.exadel.frs.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper = Mappers.getMapper(ClientMapper.class);
    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public ClientDto getClient(Long id) {
        return clientMapper.toDto(clientRepository.findById(id).orElseThrow());
    }

    public List<ClientDto> getClients() {
        return clientRepository.findAll()
                .stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    public void createClient(ClientDto clientDto) {
        clientDto.setPassword(encoder.encode(clientDto.getPassword()));
        clientRepository.save(clientMapper.toEntity(clientDto));
    }

    public void updateClient(Long id, ClientDto clientDto) {
        Client client = clientRepository.findById(id).orElseThrow();
        if (!StringUtils.isEmpty(clientDto.getName())) {
            client.setName(clientDto.getName());
        }
        if (!StringUtils.isEmpty(clientDto.getEmail())) {
            client.setEmail(clientDto.getEmail());
        }
        if (!StringUtils.isEmpty(clientDto.getPassword())) {
            client.setPassword(encoder.encode(clientDto.getPassword()));
        }
        clientRepository.save(client);
    }

    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }

}
