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

    private final List<String> blacklistedUrlsList = Collections.synchronizedList(new ArrayList<>());
    private final List<String> lowPriorityUrlsList = Collections.synchronizedList(new ArrayList<>());

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


        // Print blacklisted and low priority URLs
        System.out.println("********Blacklisted URLs:********");
        blacklistedUrlsList.forEach(System.out::println);

        System.out.println("********Low Priority URLs:********");
        lowPriorityUrlsList.forEach(System.out::println);

        return calculatePageRankResponse();
    }

    void processUrl(UrlTask task) {
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

    private static final Set<String> BLACKLISTED_URLS = Set.of(
    "https://www.creativebookmark.com/",
    "https://www.ffupdate.org",
    "https://www.ad-tracker.example",
    "https://www.popup-ads-site.net",
    "https://www.bannerads.org",
    "https://nu.outsystemsenterprise.com/FSD/",
    "https://www.instagram.com/northeastern/",
    "https://www.northeastern.edu/charlotte/",
    "https://geo.northeastern.edu/blog/country/hong-kong/",
    "https://www.tiktok.com/@northeasternu",
    "https://research.northeastern.edu/cognitive-and-brain-health/",
    "https://www.facebook.com",
    "https://www.twitter.com",
    "https://www.instagram.com",
    "https://www.fonts.googleapis.com",
    "https://www.google.com",
    "https://www.youtube.com",
    "https://www.googletagmanager.com",
    "https://www.fonts.gstatic.com",
    "https://www.linkedin.com",
    "https://www.gmpg.org",
    "https://www.maps.google.com",
    "https://www.ajax.googleapis.com",
    "https://www.play.google.com",
    "https://www.youtu.be",
    "https://www.en.wikipedia.org",
    "https://www.cdnjs.cloudflare.com",
    "https://www.github.com",
    "https://www.wordpress.org",
    "https://www.plus.google.com",
    "https://www.pinterest.com",
    "https://www.drive.google.com",
    "https://www.support.google.com",
    "https://www.docs.google.com",
    "https://www.goo.gl",
    "https://www.bit.ly",
    "https://www.developers.google.com",
    "https://www.vimeo.com",
    "https://www.policies.google.com",
    "https://www.amazon.com",
    "https://www.creativecommons.org",
    "https://www.itunes.apple.com",
    "https://www.tiktok.com",
    "https://www.apps.apple.com",
    "https://www.medium.com",
    "https://www.flickr.com",
    "https://www.secure.gravatar.com",
    "https://www.accounts.google.com",
    "https://www.cloudflare.com",
    "https://www.soundcloud.com",
    "https://www.open.spotify.com",
    "https://www.gstatic.com",
    "https://www.sites.google.com",
    "https://www.ec.europa.eu",
    "https://www.lh3.googleusercontent.com",
    "https://www.t.me",
    "https://www.cdn.jsdelivr.net",
    "https://www.ncbi.nlm.nih.gov",
    "https://www.microsoft.com",
    "https://www.google-analytics.com",
    "https://www.paypal.com",
    "https://www.vk.com",
    "https://www.podcasts.apple.com",
    "https://www.w3.org",
    "https://www.x.com",
    "https://www.player.vimeo.com",
    "https://www.tinyurl.com",
    "https://www.reddit.com",
    "https://www.who.int",
    "https://www.forms.gle",
    "https://www.linktr.ee",
    "https://www.nytimes.com",
    "https://www.support.apple.com",
    "https://www.slideshare.net",
    "https://www.code.jquery.com",
    "https://www.mail.google.com",
    "https://www.twitch.tv",
    "https://www.amazon.co.uk",
    "https://www.meetup.com",
    "https://www.patreon.com",
    "https://www.mozilla.org",
    "https://www.api.whatsapp.com",
    "https://www.apple.com",
    "https://www.dropbox.com",
    "https://www.maps.googleapis.com",
    "https://www.forbes.com",
    "https://www.amazon.de",
    "https://www.theguardian.com",
    "https://www.maps.app.goo.gl",
    "https://www.support.microsoft.com",
    "https://www.bing.com",
    "https://www.s3.amazonaws.com",
    "https://www.de.wikipedia.org",
    "https://www.news.google.com",
    "https://www.wa.me",
    "https://www.ftc.gov"
);



    int calculatePriority(String url) {

        // Ignore blacklisted URLs
    for (String blacklistedUrl : BLACKLISTED_URLS) {
        if (url.contains(blacklistedUrl)) {
            synchronized (blacklistedUrlsList) {
                if (!blacklistedUrlsList.contains(url)) {
                    blacklistedUrlsList.add(url);
                }
            }
            return Integer.MAX_VALUE; 
        }
    }
    // Ignore dark web URLs
    if (url.contains(".onion")) {
        return Integer.MAX_VALUE; 
    }

    if (url.matches(".*\\.(jpg|jpeg|png|gif|bmp|mp4|avi|mkv|mov|wmv|flv|webm)$")) {
        synchronized (lowPriorityUrlsList) {
            if (!lowPriorityUrlsList.contains(url)) {
                lowPriorityUrlsList.add(url);
            }
        }
        return 20;
    }

    if (url.contains("facebook.com") || url.contains("twitter.com") || url.contains("instagram.com") || url.contains("tiktok.com")) {
        synchronized (blacklistedUrlsList) {
            if (!blacklistedUrlsList.contains(url)) {
                blacklistedUrlsList.add(url);
            }
        }
        return 20;
    }

    if (url.contains("ad") || url.contains("ads") || url.contains("tracker") || url.contains("banner")) {
        synchronized (lowPriorityUrlsList) {
            if (!lowPriorityUrlsList.contains(url)) {
                lowPriorityUrlsList.add(url);
            }
        }
        return 20;
    }

    if (url.endsWith(".edu") || url.endsWith(".gov")) {
        return 1;
    }

    if (url.contains("research") || url.contains("science")) {
        return 5;
    }
    // Default priority
    return 10;
    }
}
