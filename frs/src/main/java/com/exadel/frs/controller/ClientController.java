package com.exadel.frs.controller;

import com.exadel.frs.dto.ClientDto;
import com.exadel.frs.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/{id}")
    public ClientDto getClient(@PathVariable Long id) {
        return clientService.getClient(id);
    }

    @GetMapping("/")
    public List<ClientDto> getClients() {
        return clientService.getClients();
    }

    @PostMapping("")
    public void createClient(@RequestBody ClientDto clientDto) {
        clientService.createClient(clientDto);
    }

    @PutMapping("/{id}")
    public void updateClient(@PathVariable Long id, @RequestBody ClientDto clientDto) {
        clientService.updateClient(id, clientDto);
    }

    @DeleteMapping("/{id}")
    public void deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
    }

}
