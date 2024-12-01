package com.info6205.webcrawler.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class Neo4jServiceTest {

    private static final String TEST_URI = "neo4j+s://test.neo4j.io";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpass";

    @Mock
    private Driver mockNeo4jDriver;

    @Mock
    private Session mockSession;

    @Mock
    private Result mockResult;

    private Neo4jService neo4jService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock the neo4jDriver to return mockSession directly
        when(mockNeo4jDriver.session()).thenReturn(mockSession);

        // Create the service with the mocked driver
        neo4jService = new Neo4jService(TEST_URI, TEST_USERNAME, TEST_PASSWORD) {
            @Override
            protected Driver createNeo4jDriver(String uri, String username, String password) {
                return mockNeo4jDriver;
            }
        };
    }

    @Test
    void testCreateNode_Success() {
        String testUrl = "http://example.com";

        // Prepare the mock to accept the specific run call
        when(mockSession.run(
                "MERGE (n:Page {url: $url}) ON CREATE SET n.visited = false",
                Map.of("url", testUrl)
        )).thenReturn(mockResult);

        // Execute the method
        neo4jService.createNode(testUrl);

        // Verify the exact method call
        verify(mockSession).run(
                "MERGE (n:Page {url: $url}) ON CREATE SET n.visited = false",
                Map.of("url", testUrl)
        );
    }

    @Test
    void testCreateNode_ExceptionHandling() {
        String testUrl = "http://example.com";

        // Simulate an exception during session run
        doThrow(new RuntimeException("Connection error")).when(mockSession)
                .run(anyString(), anyMap());

        // Verify that the method handles the exception gracefully
        assertDoesNotThrow(() -> neo4jService.createNode(testUrl));
    }

    @Test
    void testAddEdge_Success() {
        String fromUrl = "http://from.com";
        String toUrl = "http://to.com";

        assertDoesNotThrow(() -> neo4jService.addEdge(fromUrl, toUrl));

        verify(mockSession).run(
                "MATCH (a:Page {url: $fromUrl}), (b:Page {url: $toUrl}) "
                + "MERGE (a)-[:LINKS_TO]->(b)",
                Map.of("fromUrl", fromUrl, "toUrl", toUrl)
        );
    }

    @Test
    void testGetNodes_Success() {
        // Prepare mock result
        when(mockSession.run("MATCH (n:Page) RETURN n.url AS url"))
                .thenReturn(mockResult);

        // Mock the result list behavior
        when(mockResult.list(any())).thenReturn(List.of("http://example1.com", "http://example2.com"));

        List<String> nodes = neo4jService.getNodes();

        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        verify(mockSession).run("MATCH (n:Page) RETURN n.url AS url");
    }

    @Test
    void testGetNodes_ExceptionReturnsNull() {
        // Simulate an exception
        when(mockSession.run("MATCH (n:Page) RETURN n.url AS url"))
                .thenThrow(new RuntimeException("Database error"));

        List<String> nodes = neo4jService.getNodes();

        assertNull(nodes);
    }

    @Test
    void testGetGraph_Success() {
        // Prepare mock result with stream of records
        when(mockSession.run("MATCH (a:Page)-[:LINKS_TO]->(b:Page) RETURN a.url AS from, b.url AS to"))
                .thenReturn(mockResult);

        // Create mock records
        Record record1 = mock(Record.class);
        Record record2 = mock(Record.class);
        when(record1.get("from")).thenReturn(Values.value("http://from1.com"));
        when(record1.get("to")).thenReturn(Values.value("http://to1.com"));
        when(record2.get("from")).thenReturn(Values.value("http://from1.com"));
        when(record2.get("to")).thenReturn(Values.value("http://to2.com"));

        // Mock the stream behavior
        when(mockResult.stream()).thenReturn(Stream.of(record1, record2));

        Map<String, List<String>> graph = neo4jService.getGraph();

        assertNotNull(graph);
        assertTrue(graph.containsKey("http://from1.com"));
        assertEquals(2, graph.get("http://from1.com").size());
    }

    @Test
    void testUpdatePageRank_Success() {
        Map<String, Double> pageRanks = Map.of(
                "http://example1.com", 0.5,
                "http://example2.com", 0.5
        );

        // Use argument matchers for both arguments
        when(mockSession.run(
                eq("MATCH (n:Page {url: $url}) SET n.pageRank = $rank"),
                any(Map.class)
        )).thenReturn(mockResult);

        assertDoesNotThrow(() -> neo4jService.updatePageRank(pageRanks));

        // Use argument matchers for verification
        verify(mockSession, times(2)).run(
                eq("MATCH (n:Page {url: $url}) SET n.pageRank = $rank"),
                any(Map.class)
        );
    }

    @Test
    void testClearGraph_Success() {
        assertDoesNotThrow(() -> neo4jService.clearGraph());

        verify(mockSession).run("MATCH ()-[r]->() DELETE r");
        verify(mockSession).run("MATCH (n) DELETE n");
    }

    @Test
    void testClose_DriverClosed() {
        neo4jService.close();

        verify(mockNeo4jDriver).close();
    }
}
