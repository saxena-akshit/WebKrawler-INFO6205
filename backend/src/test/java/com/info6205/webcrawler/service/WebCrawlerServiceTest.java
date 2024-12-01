package com.info6205.webcrawler.service;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.info6205.webcrawler.entity.UrlTask;

class WebCrawlerServiceTest {

    private WebCrawlerService webCrawlerService;
    private Neo4jService mockNeo4jService;
    private PageRankCalculator mockPageRankCalculator;

    @BeforeEach
    void setUp() {
        mockNeo4jService = mock(Neo4jService.class);
        mockPageRankCalculator = mock(PageRankCalculator.class);

        webCrawlerService = new WebCrawlerService(
                10, // threadPoolSize
                3, // maxDepth
                1.0, // rateLimit
                mockNeo4jService,
                mockPageRankCalculator
        );
    }

    @Test
    void testStartCrawl() throws Exception {
        when(mockNeo4jService.getNodes()).thenReturn(List.of("https://www.northeastern.edu/"));
        when(mockNeo4jService.getGraph()).thenReturn(Map.of(
                "https://www.northeastern.edu/", List.of("https://graduate.northeastern.edu/")
        ));
        when(mockPageRankCalculator.computePageRank(anyList(), anyMap())).thenReturn(Map.of(
                "https://www.northeastern.edu/", 0.5,
                "https://graduate.northeastern.edu/", 0.5
        ));

        Map<String, Object> response = webCrawlerService.startCrawl("https://www.northeastern.edu/");

        assertNotNull(response);
        assertEquals("success", response.get("status"));
    }

    @Test
    void testCalculatePriority() {
        int priority = webCrawlerService.calculatePriority("https://www.northeastern.edu/");
        assertEquals(10, priority); // Default priority
    }

    @Test
    void testProcessUrl() {
        UrlTask task = new UrlTask("https://www.northeastern.edu/", 0, 10);
        webCrawlerService.processUrl(task);

        verify(mockNeo4jService, times(1)).createNode(task.getUrl());
        verify(mockNeo4jService, atLeastOnce()).addEdge(anyString(), anyString());
    }

    @Test
    void testBlacklistPriority() {
        // Test if a blacklisted URL is assigned the maximum priority
        int priority = webCrawlerService.calculatePriority("https://www.facebook.com");
        assertEquals(Integer.MAX_VALUE, priority);
        assertEquals(true, webCrawlerService.blacklistedUrlsList.contains("https://www.facebook.com"));
    }

    @Test
    void testLowPriorityForImages() {
        // Test if an image URL is assigned a low priority
        int priority = webCrawlerService.calculatePriority("https://www.example.com/image.jpg");
        assertEquals(20, priority);
        assertEquals(true, webCrawlerService.lowPriorityUrlsList.contains("https://www.example.com/image.jpg"));
    }

    @Test
    void testHighPriorityForGovUrls() {
        // Test if a .gov URL is assigned a high priority
        int priority = webCrawlerService.calculatePriority("https://www.whitehouse.gov");
        assertEquals(1, priority);
    }

    @Test
    void testHighPriorityForResearchUrls() {
        // Test if a URL containing 'research' is assigned a medium-high priority
        int priority = webCrawlerService.calculatePriority("https://www.example.com/research");
        assertEquals(5, priority);
    }

    @Test
    void testOnionUrls() {
        // Test if a .onion URL is assigned the maximum priority
        int priority = webCrawlerService.calculatePriority("http://example.onion");
        assertEquals(Integer.MAX_VALUE, priority);
    }

    @Test
    void testProcessUrlWithValidLinks() {
        // Mock extracted links and Neo4j interactions
        UrlTask task = new UrlTask("https://www.northeastern.edu/", 0, 10);
        when(mockNeo4jService.getNodes()).thenReturn(List.of("https://www.northeastern.edu/"));
        when(mockNeo4jService.getGraph()).thenReturn(Map.of(
                "https://www.northeastern.edu/", List.of("https://graduate.northeastern.edu/")
        ));

        webCrawlerService.processUrl(task);

        // Verify interactions with Neo4j service
        verify(mockNeo4jService, times(1)).createNode("https://www.northeastern.edu/");
        verify(mockNeo4jService, atLeastOnce()).addEdge(anyString(), anyString());
    }

    @Test
    void testPageRankResponseStructure() {
        // Mock data for nodes and graph
        when(mockNeo4jService.getNodes()).thenReturn(List.of("https://www.northeastern.edu/"));
        when(mockNeo4jService.getGraph()).thenReturn(Map.of(
                "https://www.northeastern.edu/", List.of("https://graduate.northeastern.edu/")
        ));
        when(mockPageRankCalculator.computePageRank(anyList(), anyMap())).thenReturn(Map.of(
                "https://www.northeastern.edu/", 0.8,
                "https://graduate.northeastern.edu/", 0.2
        ));

        Map<String, Object> response = webCrawlerService.calculatePageRankResponse();

        assertNotNull(response);
        assertEquals("success", response.get("status"));
        assertEquals(2, ((List<?>) response.get("data")).size());
    }

    @Test
    void testNoDuplicateBlacklistedEntries() {
        // Test blacklisting of the same URL multiple times
        webCrawlerService.calculatePriority("https://www.facebook.com");
        webCrawlerService.calculatePriority("https://www.facebook.com");

        // Assert that the blacklisted URL is only added once
        assertEquals(1, webCrawlerService.blacklistedUrlsList.stream()
                .filter(url -> url.equals("https://www.facebook.com")).count());
    }

    @Test
    void testThreadPoolShutdown() throws Exception {
        // Mock Neo4j interactions
        when(mockNeo4jService.getNodes()).thenReturn(List.of("https://www.northeastern.edu/"));
        when(mockNeo4jService.getGraph()).thenReturn(Map.of(
                "https://www.northeastern.edu/", List.of("https://graduate.northeastern.edu/")
        ));

        webCrawlerService.startCrawl("https://www.northeastern.edu/");

        // Verify thread pool shutdown
        assertEquals(true, webCrawlerService.threadPool.isShutdown());
    }

}
