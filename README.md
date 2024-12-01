# WebKrawler-INFO6205

Repository containing the codebase of the Webcrawler for INFO6205 Fall 2024 Project

## Overview

WebKrawler is a Spring Boot application that performs asynchronous, priority-based web crawling and PageRank computation. It stores the crawled data in a Neo4j graph database and uses Log4j for comprehensive logging. Key features include:

1. Asynchronous URL crawling
2. Storage of nodes and edges in Neo4j
3. Priority-based crawling with blacklisted and low-priority URLs
4. PageRank computation
5. Detailed logging and error handling
6. Unit tests and
7. Benchmarking for performance validation
8. Deployed on AWS

## Features

Core Services

## WebCrawlerService:

Crawls the web starting from a root URL.
Implements rate limiting, priority-based URL queuing, and depth-based recursion.
Maintains a blacklist and prioritizes educational, government, and research-based URLs.
Logging and Benchmarking
Log4j provides detailed logs for crawling operations, errors, and performance metrics.
Unit tests validate functionality, and benchmarking measures performance.

## PageRankCalculator:

Computes PageRank values using iterative calculations until convergence.

## Neo4jService:

Manages interactions with the Neo4j graph database.
Creates nodes and edges, retrieves graph data, and updates PageRank values.

## Getting Started

Prerequisites
Java: Ensure Java 11 or later is installed.
Spring Boot: Use Maven for dependency management.
Neo4j: Install and configure a Neo4j database.
Set up Neo4j credentials and URL in application.properties.

Installation

1. Clone the repository:
   git clone git@github.com:saxena-akshit/WebKrawler-INFO6205.git
2. cd backend
3. Build the application:
   mvn clean install

4. Configure application.properties:
   Replace placeholders with your Neo4j database details:
   arduino
   Copy code
   neo4j.url=bolt://localhost:7687
   neo4j.username=yourUsername
   neo4j.password=yourPassword
   crawler.threadPoolSize=10
   crawler.maxDepth=3
   crawler.rateLimit=2.0
   Running the Application
   Start the Neo4j database service.

5. Run the application:
   mvn spring-boot:run
   The application starts on http://localhost:8080.
