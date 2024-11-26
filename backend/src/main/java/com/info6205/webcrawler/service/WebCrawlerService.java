package com.info6205.webcrawler.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;
import com.info6205.webcrawler.entity.UrlTask;

@Service
public class WebCrawlerService {

    private final ExecutorService threadPool;
    private final RateLimiter rateLimiter;
    private final PriorityQueue<UrlTask> queue;
    private final Set<String> visitedUrls;
    private final int maxDepth;

    private final Neo4jService neo4jService;

    public WebCrawlerService(@Value("${crawler.threadPoolSize}") int threadPoolSize,
            @Value("${crawler.maxDepth}") int maxDepth, Neo4jService neo4jService) {
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.rateLimiter = RateLimiter.create(10); // 10 requests/second
        this.queue = new PriorityQueue<>(Comparator.comparingInt(UrlTask::getPriority));
        this.visitedUrls = Collections.synchronizedSet(new HashSet<>());
        this.maxDepth = maxDepth;

        this.neo4jService = neo4jService;
    }

    public void startCrawl(String startUrl) {
        queue.add(new UrlTask(startUrl, 0, calculatePriority(startUrl)));

        while (!queue.isEmpty()) {
            UrlTask task = queue.poll();

            if (task.depth >= maxDepth || visitedUrls.contains(task.url)) {
                continue;
            }

            visitedUrls.add(task.url);

            CompletableFuture.runAsync(() -> processUrl(task), threadPool)
                    .exceptionally(e -> {
                        // TODO: Change to a proper log statement
                        System.out.println("Error processing URL " + task.url + "\n" + e);
                        return null;
                    });
        }

        threadPool.shutdown();
        // Wait for threads to finish and trigger PageRank calculation
    }

    private void processUrl(UrlTask task) {
        rateLimiter.acquire();
        neo4jService.addNode("www.facebook.com", false);
        // Fetch URL, parse links, and add new tasks to the queue
    }

    private int calculatePriority(String url) {
        // Heuristic for prioritizing `.edu`, `.gov`, etc.

        // Generating random priority for now
        // TODO: Update it to calculate priortiy
        Random random = new Random();
        return random.nextInt();
    }
}
