package com.linker.meta.services;

import com.google.common.collect.Lists;
import com.linker.common.router.Router;
import com.linker.common.router.RouterLink;
import com.linker.meta.configurations.ApplicationConfig;
import com.linker.meta.repositories.RouterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouterService {
    static final String ROUTER_LINK_KEY = "ROUTER_LINK_KEY";

    final ApplicationConfig applicationConfig;
    final RouterRepository routerRepository;
    final RedisTemplate redisTemplate;

    @PostConstruct
    public void loadDefaultRouters() {
        log.info("load default routers");
        applicationConfig.getRouters().forEach(routerRepository::save);

        if (applicationConfig.getRouterLinks().size() > 0) {
            Set<RouterLink> routerLinks = getAllRouterLinks();
            routerLinks.addAll(applicationConfig.getRouterLinks());
            setRouterLinks(routerLinks);
        }
    }

    public List<Router> getAllRouters() {
        return Lists.newArrayList(routerRepository.findAll());
    }

    public Set<RouterLink> getAllRouterLinks() {
        return redisTemplate.opsForSet().members(ROUTER_LINK_KEY);
    }

    @Transactional
    public void setRouterLinks(Set<RouterLink> routerLinks) {
        SetOperations setOperations = redisTemplate.opsForSet();
        redisTemplate.delete(ROUTER_LINK_KEY);
        routerLinks.stream().forEach(routerLink -> setOperations.add(ROUTER_LINK_KEY, routerLink));
    }
}
