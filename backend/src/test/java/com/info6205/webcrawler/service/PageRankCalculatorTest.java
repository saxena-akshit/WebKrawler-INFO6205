package com.info6205.webcrawler.service;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PageRankCalculatorTest {

    private final PageRankCalculator pageRankCalculator = new PageRankCalculator();

    @Test
    void testComputePageRank() {
        List<String> nodes = List.of("A", "B", "C");
        Map<String, List<String>> graph = Map.of(
                "A", List.of("B"),
                "B", List.of("C"),
                "C", List.of("A")
        );

        Map<String, Double> pageRanks = pageRankCalculator.computePageRank(nodes, graph);

        assertNotNull(pageRanks);
        assertEquals(3, pageRanks.size());
        assertTrue(pageRanks.values().stream().allMatch(rank -> rank > 0));
    }

    @Test
    void testGetInNeighbors() {
        Map<String, List<String>> graph = Map.of(
                "A", List.of("B"),
                "B", List.of("C"),
                "C", List.of("A")
        );

        List<String> inNeighbors = pageRankCalculator.getInNeighbors("B", graph);
        assertNotNull(inNeighbors);
        assertEquals(1, inNeighbors.size());
        assertEquals("A", inNeighbors.get(0));
    }

    @Test
    void testGetOutDegree() {
        Map<String, List<String>> graph = Map.of(
                "A", List.of("B", "C"),
                "B", List.of("C"),
                "C", List.of("A")
        );

        int outDegree = pageRankCalculator.getOutDegree("A", graph);
        assertEquals(2, outDegree);
    }
}
