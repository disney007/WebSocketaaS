package com.linker.meta.controllers;

import com.linker.common.router.Router;
import com.linker.common.router.RouterGraph;
import com.linker.common.router.RouterLink;
import com.linker.meta.services.RouterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/routers")
@RequiredArgsConstructor
public class RouterController {
    final RouterService routerService;

    @GetMapping("")
    public List<Router> getRouters() {
        return routerService.getAllRouters();
    }

    @GetMapping("/links")
    public Set<RouterLink> getRouterLinks() {
        return routerService.getAllRouterLinks();
    }

    @GetMapping("/graph")
    public RouterGraph routerGraph() {
        return new RouterGraph(routerService.getAllRouters(), routerService.getAllRouterLinks());
    }
}
