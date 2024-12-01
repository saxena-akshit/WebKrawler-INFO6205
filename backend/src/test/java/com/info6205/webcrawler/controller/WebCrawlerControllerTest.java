package com.info6205.webcrawler.controller;

import com.info6205.webcrawler.service.Neo4jService;
import com.info6205.webcrawler.service.WebCrawlerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(WebCrawlerController.class)
public class WebCrawlerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebCrawlerService crawlerService;

    @MockBean
    private Neo4jService neo4jService;

    @Test
    public void testStartCrawl() throws Exception {
        String startUrl = "https://www.northeastern.edu/";
        Map<String, Object> mockResponse = Collections.singletonMap("message", "Crawl started");

        when(crawlerService.startCrawl(Mockito.eq(startUrl))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/crawler/start")
                        .param("startUrl", startUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Crawl started"));
    }
}
