package com.info6205.webcrawler.service;

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

    public void addNode(String url, boolean visited) {
        try (Session session = neo4jDriver.session()) {
            Result result = session.run("MERGE (n:Page {url: $url}) SET n.visited = $visited",
                    Map.of("url", url, "visited", visited));
            System.out.println(result.consume());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addEdge(String fromUrl, String toUrl) {
        try (Session session = neo4jDriver.session()) {
            String queryString = String.format("MATCH (a:Page {url: %s}), (b:Page {url: %s}) MERGE (a)-[:LINKS_TO]->(b)", fromUrl, toUrl);
            session.run(queryString);
        }
    }
}
