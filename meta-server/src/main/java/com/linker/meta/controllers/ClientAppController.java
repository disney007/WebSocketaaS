package com.linker.meta.controllers;

import com.linker.common.client.ClientApp;
import com.linker.meta.services.ClientAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clientApps")
@RequiredArgsConstructor
public class ClientAppController {

    final ClientAppService clientAppService;

    @GetMapping("")
    public List<ClientApp> getClientApps() {
        return clientAppService.getAllClientApps();
    }
}
