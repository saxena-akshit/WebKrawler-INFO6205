package com.info6205.webcrawler.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class PageRankCalculator {

    private static final int MAX_ITERATIONS = 20;
    private static final double CONVERGENCE_THRESHOLD = 1e-5;

    public Map<String, Double> computePageRank(List<String> nodes, Map<String, List<String>> graph) {
        int totalNodes = nodes.size();
        double initialRank = 1.0 / totalNodes;

        // Initialize ranks
        Map<String, Double> ranks = new HashMap<>();
        for (String node : nodes) {
            ranks.put(node, initialRank);
        }

        // Start iterations
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            Map<String, Double> newRanks = new HashMap<>();
            double diff = 0.0;

            for (String node : nodes) {
                double rankSum = 0.0;

                // Calculate rank contribution from in-neighbors
                for (String neighbor : getInNeighbors(node, graph)) {
                    rankSum += ranks.get(neighbor) / getOutDegree(neighbor, graph);
                }

                // Update rank
                newRanks.put(node, rankSum);
                diff += Math.abs(newRanks.get(node) - ranks.get(node));
            }

            // Check for convergence
            ranks = newRanks;
            if (diff < CONVERGENCE_THRESHOLD) {
                break;
            }
        }

        // Normalize ranks to sum to 1
        double totalRank = ranks.values().stream().mapToDouble(Double::doubleValue).sum();
        for (Map.Entry<String, Double> entry : ranks.entrySet()) {
            ranks.put(entry.getKey(), entry.getValue() / totalRank);
        }

        return ranks;
    }

    private List<String> getInNeighbors(String node, Map<String, List<String>> graph) {
        return graph.entrySet().stream()
                .filter(entry -> entry.getValue().contains(node))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private int getOutDegree(String node, Map<String, List<String>> graph) {
        return graph.getOrDefault(node, Collections.emptyList()).size();
    }
}
