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
                3,  // maxDepth
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
}
