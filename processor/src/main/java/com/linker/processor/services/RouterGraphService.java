package com.linker.processor.services;

import com.google.common.collect.ImmutableList;
import com.linker.common.router.Router;
import com.linker.common.router.RouterGraph;
import com.linker.common.router.RouterLink;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.configurations.ApplicationConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RouterGraphService {

    final ApplicationConfig applicationConfig;
    final ProcessorUtils processorUtils;

    RouterGraph routerGraph;
    Map<String, Router> routerMap;

    Graph<String, DefaultEdge> graph;

    ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> path;

    public void loadGraph(RouterGraph routerGraph) {
        log.info("load router graph");
        this.routerGraph = routerGraph;
        routerMap = routerGraph.getRouters().stream().collect(Collectors.toMap(Router::getName, r -> r));

        setupGraph(routerMap.keySet(), routerGraph.getLinks(), applicationConfig.getDomainName());
        // TODO: diff the old graph and new graph
    }

    void setupGraph(Set<String> routers, Set<RouterLink> links, String currentRouterName) {
        graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        routers.forEach(routerName -> graph.addVertex(routerName));
        links.forEach(routerLink -> graph.addEdge(routerLink.getN1(), routerLink.getN2()));
        DijkstraShortestPath<String, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        if (StringUtils.isBlank(currentRouterName)) {
            currentRouterName = applicationConfig.getDomainName();
        }
        path = dijkstraAlg.getPaths(currentRouterName);
    }

    List<String> computePath(String to) {
        try {
            GraphPath<String, DefaultEdge> result = path.getPath(to);
            return result.getVertexList();
        } catch (Exception e) {
            log.error("not path found from {} to {}", path.getSourceVertex(), to);
            return ImmutableList.of();
        }
    }

    public String getNextRouterName(String to) {
        if (!this.processorUtils.isDomainRouter()) {
            throw new UnsupportedOperationException("This domain is not configured as router");
        }
        List<String> path = this.computePath(to);
        switch (path.size()) {
            case 0:
                return null;
            case 1:
                return path.get(0);
            default:
                return path.get(1);
        }
    }

    public Router getCurrentRouter() {
        if (!processorUtils.isDomainRouter()) {
            throw new IllegalStateException("current domain should be router");
        }

        if (routerMap == null) {
            throw new IllegalStateException("router map is not initialized");
        }

        return routerMap.get(applicationConfig.getDomainName());
    }

    public Set<String> getLinkedRouterNames(String name) {
        return Graphs.neighborSetOf(graph, name);
    }

    public List<Router> getCurrentLinkedRouters() {
        return getLinkedRouterNames(getCurrentRouter().getName()).stream().map(routerMap::get).collect(Collectors.toList());
    }

    public Router getDomainLinkedRouter(String domainName) {
        Optional<Map.Entry<String, Router>> entry = routerMap.entrySet().stream().filter(e -> e.getValue().getDomains().contains(domainName)).findAny();
        return entry.map(Map.Entry::getValue).orElse(null);
    }
}
