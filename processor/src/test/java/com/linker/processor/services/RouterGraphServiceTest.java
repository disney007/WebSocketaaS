package com.linker.processor.services;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.linker.common.router.RouterLink;
import com.linker.processor.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class RouterGraphServiceTest extends IntegrationTest {

    @Autowired
    RouterGraphService routerGraphService;
    Set<String> routers;
    Set<RouterLink> links;

    @Before
    public void setup() {
        routers = new HashSet<>();
        for (int i = 1; i <= 13; ++i) {
            routers.add(String.valueOf(i));
        }
        links = new HashSet<>();
        link(links, 1, 2);
        link(links, 1, 3);
        link(links, 1, 10);
        link(links, 3, 5);
        link(links, 5, 6);
        link(links, 5, 7);
        link(links, 5, 8);
        link(links, 8, 9);
        link(links, 8, 10);
        link(links, 9, 10);
        link(links, 11, 12);
    }

    @Test
    public void testRouterGraph() {
        doTestRouterGraph(1, 1, ImmutableList.of(1));
        doTestRouterGraph(1, 3, ImmutableList.of(1, 3));
        doTestRouterGraph(3, 8, ImmutableList.of(3, 5, 8));
        doTestRouterGraph(3, 6, ImmutableList.of(3, 5, 6));
        doTestRouterGraph(1, 10, ImmutableList.of(1, 10));
        doTestRouterGraph(2, 10, ImmutableList.of(2, 1, 10));
        doTestRouterGraph(3, 4, ImmutableList.of());
        doTestRouterGraph(6, 9, ImmutableList.of(6, 5, 8, 9));
        doTestRouterGraph(11, 12, ImmutableList.of(11, 12));
        doTestRouterGraph(1, 11, ImmutableList.of());

        doTestRouterGraph(3, 1, ImmutableList.of(3, 1));
        doTestRouterGraph(8, 3, ImmutableList.of(8, 5, 3));
        doTestRouterGraph(6, 3, ImmutableList.of(6, 5, 3));
        doTestRouterGraph(10, 1, ImmutableList.of(10, 1));
        doTestRouterGraph(10, 2, ImmutableList.of(10, 1, 2));
        doTestRouterGraph(4, 3, ImmutableList.of());
        doTestRouterGraph(9, 6, ImmutableList.of(9, 8, 5, 6));
        doTestRouterGraph(12, 11, ImmutableList.of(12, 11));
        doTestRouterGraph(11, 1, ImmutableList.of());

        doTestRouterGraph(11, 13, ImmutableList.of());
    }

    public void doTestRouterGraph(int start, int to, List<Integer> expectedPath) {
        routerGraphService.setupGraph(routers, links, String.valueOf(start));
        List<String> actualPath = routerGraphService.computePath(String.valueOf(to));
        assertEquals(expectedPath.stream().map(String::valueOf).collect(Collectors.toList()), actualPath);
    }

    @Test
    public void testGetNextRouter() {
        setDomainAsRouter();
        doTestGetNextRouter(1, 10, 10);
        doTestGetNextRouter(1, 1, 1);
        doTestGetNextRouter(1, 11, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetNextRouter_notRouter() {
        doTestGetNextRouter(1, 10, 10);
    }

    public void doTestGetNextRouter(int start, int to, Integer expectedValue) {
        routerGraphService.setupGraph(routers, links, String.valueOf(start));
        String actualResult = routerGraphService.getNextRouterName(String.valueOf(to));
        assertEquals(expectedValue != null ? String.valueOf(expectedValue) : null, actualResult);
    }

    @Test
    public void testGetLinkedRouterNames() {
        routerGraphService.setupGraph(routers, links, String.valueOf(1));
        doTestGetLinkedRouterNames(1, ImmutableSet.of(10, 2, 3));
        doTestGetLinkedRouterNames(11, ImmutableSet.of(12));
        doTestGetLinkedRouterNames(13, ImmutableSet.of());
    }

    public void doTestGetLinkedRouterNames(int name, Set<Integer> expectedLinkedNames) {
        Set<String> actualLinkedNames = routerGraphService.getLinkedRouterNames(String.valueOf(name));
        assertEquals(expectedLinkedNames.stream().map(String::valueOf).collect(Collectors.toSet()), actualLinkedNames);
    }


    void link(Set<RouterLink> routerLinks, int n1, int n2) {
        routerLinks.add(new RouterLink(String.valueOf(n1), String.valueOf(n2)));
    }
}
