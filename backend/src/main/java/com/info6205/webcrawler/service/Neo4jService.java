package com.info6205.webcrawler.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Neo4jService {

    private final Driver neo4jDriver;

    public Neo4jService(@Value("${neo4j.url}") String uri,
            @Value("${neo4j.username}") String username,
            @Value("${neo4j.password}") String password) {
        this.neo4jDriver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public void createNode(String url) {
        try (Session session = neo4jDriver.session()) {
            session.run(
                    "MERGE (n:Page {url: $url}) ON CREATE SET n.visited = false",
                    Map.of("url", url)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addEdge(String fromUrl, String toUrl) {
        try (Session session = neo4jDriver.session()) {
            session.run(
                    "MATCH (a:Page {url: $fromUrl}), (b:Page {url: $toUrl}) "
                    + "MERGE (a)-[:LINKS_TO]->(b)",
                    Map.of("fromUrl", fromUrl, "toUrl", toUrl)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getNodes() {
        try (Session session = neo4jDriver.session()) {
            Result result = session.run("MATCH (n:Page) RETURN n.url AS url");
            return result.list(record -> record.get("url").asString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, List<String>> getGraph() {
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(
                    "MATCH (a:Page)-[:LINKS_TO]->(b:Page) RETURN a.url AS from, b.url AS to"
            );

            Map<String, List<String>> graph = new HashMap<>();
            result.stream().forEach(record -> {
                String from = record.get("from").asString();
                String to = record.get("to").asString();
                graph.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
            });
            return graph;
        }
    }

    public void updatePageRank(Map<String, Double> pageRanks) {
        try (Session session = neo4jDriver.session()) {
            for (Map.Entry<String, Double> entry : pageRanks.entrySet()) {
                session.run(
                        "MATCH (n:Page {url: $url}) SET n.pageRank = $rank",
                        Map.of("url", entry.getKey(), "rank", entry.getValue())
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearGraph() {
        try (Session session = neo4jDriver.session()) {
            session.run("MATCH ()-[r]->() DELETE r");
            session.run("MATCH (n) DELETE n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (neo4jDriver != null) {
            neo4jDriver.close();
        }
    }
}
