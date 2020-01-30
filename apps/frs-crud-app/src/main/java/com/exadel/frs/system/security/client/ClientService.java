package com.exadel.frs.system.security.client;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService implements ClientDetailsService {

    private final ClientRepository clientRepository;

    @Override
    public ClientDetails loadClientByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Client with id %s not found", clientId)));
    }

    public List<? extends ClientDetails> saveAll(List<Client> clientsDetail) {
        return clientRepository.saveAll(clientsDetail);
    }
}