package com.exadel.frs.controller;

import com.exadel.frs.dto.ClientDto;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    public ClientDto getClient() {
        return clientService.getClient(securityUtils.getPrincipal().getId());
    }

    @PostMapping("/register")
    public void createClient(@RequestBody ClientDto clientDto) {
        clientService.createClient(clientDto);
    }

    @PutMapping("/update")
    public void updateClient(@RequestBody ClientDto clientDto) {
        clientService.updateClient(securityUtils.getPrincipal().getId(), clientDto);
    }

    @DeleteMapping("/delete")
    public void deleteClient() {
        clientService.deleteClient(securityUtils.getPrincipal().getId());
    }

}
