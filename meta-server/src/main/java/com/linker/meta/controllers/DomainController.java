package com.linker.meta.controllers;

import com.linker.common.router.Domain;
import com.linker.common.router.DomainGraph;
import com.linker.common.router.DomainLink;
import com.linker.meta.configurations.ApplicationConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/domains")
@RequiredArgsConstructor
public class DomainController {
    final ApplicationConfig applicationConfig;

    @GetMapping("")
    public List<Domain> getDomains() {
        return applicationConfig.getDomains();
    }

    @GetMapping("/links")
    public Set<DomainLink> getLinks() {
        return applicationConfig.getDomainLinks();
    }

    @GetMapping("/graph")
    public DomainGraph domainGraph() {
        return new DomainGraph(getDomains(), getLinks());
    }
}
