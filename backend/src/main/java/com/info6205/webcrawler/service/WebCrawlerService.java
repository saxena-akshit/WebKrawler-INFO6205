package com.info6205.webcrawler.service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;
import com.info6205.webcrawler.entity.UrlTask;

@Service
public class WebCrawlerService {

    private static final Logger logger = LogManager.getLogger(WebCrawlerService.class);

    private final ExecutorService threadPool;
    private final RateLimiter rateLimiter;
    private final PriorityBlockingQueue<UrlTask> queue;
    private final Set<String> visitedUrls;
    private final int maxDepth;

    private final Neo4jService neo4jService;
    private final PageRankCalculator pageRankCalculator;

    public WebCrawlerService(
            @Value("${crawler.threadPoolSize}") int threadPoolSize,
            @Value("${crawler.maxDepth}") int maxDepth,
            @Value("${crawler.rateLimit}") double rateLimit,
            Neo4jService neo4jService,
            PageRankCalculator pageRankCalculator) {
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.rateLimiter = RateLimiter.create(rateLimit);
        this.queue = new PriorityBlockingQueue<>(10, Comparator.comparingInt(UrlTask::getPriority));
        this.visitedUrls = Collections.synchronizedSet(new HashSet<>());
        this.maxDepth = maxDepth;
        this.neo4jService = neo4jService;
        this.pageRankCalculator = pageRankCalculator;
    }

    public Map<String, Object> startCrawl(String startUrl) throws InterruptedException {
        logger.info("Starting web crawl with starting URL: {}", startUrl);

        // Clear existing Neo4j graph
        neo4jService.clearGraph();
        queue.add(new UrlTask(startUrl, 0, calculatePriority(startUrl)));

        while (true) {
            UrlTask task = queue.poll(1, TimeUnit.SECONDS); // Wait for tasks to be added to the queue
            if (task == null && threadPool.isTerminated()) {
                break; // Exit if no tasks are pending and all threads are done
            }
            if (task == null || task.getDepth() >= maxDepth || visitedUrls.contains(task.getUrl())) {
                continue;
            }
            if (task.getDepth() >= maxDepth || visitedUrls.contains(task.getUrl())) {
                continue;
            }

            // TODO: remove in prod
            if (visitedUrls.size() > 20) {
                break;
            }

            visitedUrls.add(task.getUrl());
            CompletableFuture.runAsync(() -> processUrl(task), threadPool)
                    .exceptionally(e -> {
                        logger.error("Error processing URL {}: {}", task.getUrl(), e.getMessage());
                        return null;
                    });
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Thread pool termination interrupted: {}", e.getMessage());
        }
        return calculatePageRankResponse();
    }

    private void processUrl(UrlTask task) {
        System.out.println("********\nProcessing URL\n********" + task.getUrl());
        rateLimiter.acquire();
        try {
            Document doc = Jsoup.connect(task.getUrl()).get();
            Elements links = doc.select("a[href]");
            Set<String> extractedLinks = links.stream()
                    .map(link -> link.attr("abs:href"))
                    .filter(link -> link.startsWith("http"))
                    .collect(Collectors.toSet());

            for (String link : extractedLinks) {
                if (!visitedUrls.contains(link)) {
                    queue.add(new UrlTask(link, task.getDepth() + 1, calculatePriority(link)));
                }
            }

            neo4jService.createNode(task.getUrl());
            for (String link : extractedLinks) {
                neo4jService.addEdge(task.getUrl(), link);
            }
        } catch (IOException e) {
            logger.warn("Failed to process URL {}: {}", task.getUrl(), e.getMessage());
        }
    }

    private Map<String, Object> calculatePageRankResponse() {
        List<String> nodes = neo4jService.getNodes();
        Map<String, List<String>> graph = neo4jService.getGraph();
        Map<String, Double> pageRanks = pageRankCalculator.computePageRank(nodes, graph);
        neo4jService.updatePageRank(pageRanks);

        List<Map<String, Object>> resultData = pageRanks.entrySet().stream()
                .map(entry -> {
                    String url = entry.getKey();
                    Double rank = entry.getValue();
                    int inDegree = (int) nodes.stream().filter(node -> graph.getOrDefault(node, new ArrayList<>()).contains(url)).count();
                    return Map.<String, Object>of("url", url, "rank", rank, "in_degree", inDegree);
                })
                .sorted(Comparator.comparingDouble((Map<String, Object> m) -> (Double) m.get("rank")).reversed()) // Sort by rank descending
                .collect(Collectors.toList());

        return Map.of(
                "status", "success",
                "timestamp", ZonedDateTime.now().toString(),
                "total_urls_crawled", nodes.size(),
                "data", resultData
        );
    }

    private int calculatePriority(String url) {
        return url.endsWith(".edu") || url.endsWith(".gov") ? 1 : 10;
    }
}
