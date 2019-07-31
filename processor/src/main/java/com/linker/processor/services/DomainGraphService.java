package com.linker.processor.services;

import com.google.common.collect.ImmutableList;
import com.linker.common.router.Domain;
import com.linker.common.router.DomainGraph;
import com.linker.common.router.DomainLink;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DomainGraphService {

    final ApplicationConfig applicationConfig;
    final ProcessorUtils processorUtils;

    DomainGraph domainGraph;
    Map<String, Domain> domainMap;

    Graph<String, DefaultEdge> graph;

    ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> path;

    public void loadGraph(DomainGraph domainGraph) {
        log.info("load domain graph");
        this.domainGraph = domainGraph;
        domainMap = domainGraph.getDomains().stream().collect(Collectors.toMap(Domain::getName, r -> r));

        setupGraph(domainMap.keySet(), domainGraph.getLinks(), applicationConfig.getDomainName());
    }

    void setupGraph(Set<String> routers, Set<DomainLink> links, String currentRouterName) {
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

    public String getNextDomainName(String to) {
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

    public Domain getCurrentDomain() {
        if (domainMap == null) {
            throw new IllegalStateException("domain map is not initialized");
        }

        return domainMap.get(applicationConfig.getDomainName());
    }

    public Set<String> getLinkedDomainNames(String name) {
        return Graphs.neighborSetOf(graph, name);
    }

    public List<Domain> getCurrentLinkedDomains() {
        return getLinkedDomainNames(getCurrentDomain().getName()).stream().map(domainMap::get).collect(Collectors.toList());
    }
}
