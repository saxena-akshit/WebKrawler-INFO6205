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

## Core Services

#### WebCrawlerService:

Crawls the web starting from a root URL.
Implements rate limiting, priority-based URL queuing, and depth-based recursion.
Maintains a blacklist and prioritizes educational, government, and research-based URLs. Unit tests validate functionality, and benchmarking measures performance.

#### PageRankCalculator:

Computes PageRank values using iterative calculations until convergence.

#### Neo4jService:

Manages interactions with the Neo4j graph database.
Creates nodes and edges, retrieves graph data, and updates PageRank values.

#### Log4J logging:

Detailed and descriptive logging is enabled via Log4J that logs crawling operations, errors and performance metrics

## Getting Started

Prerequisites
Java: Ensure Java 11 or later is installed.
Spring Boot: Use Maven for dependency management.
Neo4j: Install and configure a Neo4j database.
Set up Neo4j credentials and URL in application.properties.

#### Installation

1. Clone the repository:\
   `git clone git@github.com:saxena-akshit/WebKrawler-INFO6205.git`
2. Navigate to Directory:\
   `cd backend`
3. Build the application:\
   `mvn clean install`
4. Create a `.env` file in `/backend`

5. Configure `application.properties`: (add the following in `.env`)

   `neo4j.url={actual-url}`\
   `neo4j.username=neo4j`\
   `neo4j.password={actual-pw}`\
   `crawler.threadPoolSize=10`\
   `crawler.maxDepth=3`\
   `crawler.rateLimit=100`\

6. The actual credentials can be found in the PDF report for this project. They have not been commited to VCS.\

#### Running the app

1. Run the application:\
   `mvn spring-boot:run`\
   The application starts on http://localhost:8080. \

2. The crawl endpoint is \
   `{baseUrl}/api/crawler/start?startUrl=https://www.northeastern.edu`

#### Sample Response

```
{
  "status": "success",
  "total_urls_crawled": 390,
  "timestamp": "2024-12-02T21:17:54.448226-05:00[America/New_York]",
  "data": [
    {
      "url": "https://recreation.northeastern.edu/",
      "rank": 0.1182050831675692,
      "in_degree": 68
    },
    {
      "url": "https://www.nulondon.ac.uk/",
      "rank": 0.09103020536792787,
      "in_degree": 112
    },
    {
      "url": "https://registrar.northeastern.edu/",
      "rank": 0.04365506862823522,
      "in_degree": 52
    },
    {
      "url": "https://chancellor.northeastern.edu/",
      "rank": 0.031303563848941486,
      "in_degree": 41
    },
    ...
    ...
    ...
    {
      "url": "https://calendar.northeastern.edu/event/mens-basketball-vs-lasalle",
      "rank": 3.566481507310896e-36,
      "in_degree": 1
    },
    {
      "url": "https://www.nulondon.ac.uk/study/visit-us/pg-events/taster-lectures/",
      "rank": 1.4107267516398406e-38,
      "in_degree": 1
    }
  ]
}

```

1. The resultant list is sorted based on the **rank**, while this rank is closely related to **in-degree**, it is not the same.
2. As we can see it's possible for a lower in-degree URL to have a higher rank, this is due to the PageRank algorithm that assigns weight to the URLs, so a URL with low in-degree but _high quality_ of in-degree (\***\*meaning the URLs linking to this URL are themselves high ranking\*\***) will result in a higher rank compared to another URL with high in-degree but _low quality_ of in-degree

#### Automatic deployment

A github actions pipeline is setup that automatically deploys the most recent code on a running AWS instance, so the service is publically available over the internet, _however_ the instance is an \***\*ec2.small\*\*** with limited memory so testing and benchmarking are performed better on local system, our benchmarks are performed on a system these configurations:

M2, 8-core CPU, 10-core GPU, 8GB unified memory

#### Sample benchmark results:

Benchmarks are generated on every run, a file is created in `/backend` with the name `crawler_performance_metrics.csv` in which lines are appended for subsequent runs.

```
StartTimestamp,StartUrl,ThreadPoolSize,MaxDepth,TotalUrlsCrawled,CrawlDurationMs,UrlsPerSecond,PeakMemoryUsedMB
2024-12-03T02:14:25.031889Z,https://www.northeastern.edu,48,5,501,191325,2.62,217
```

Detailed benchmarking can be found in the accompanying Project Report

#### Libraries Used:

- **Spring Boot Starter Web**: Provides a starter for building web applications, including RESTful APIs, using Spring Boot.
- **Neo4j OGM Core**: Object Graph Mapping (OGM) library for interacting with Neo4j databases.
- **Neo4j Java Driver**: Official Java driver for connecting to Neo4j databases.
- **Jsoup**: Library for parsing and manipulating HTML documents.
- **Guava**: A set of core libraries for Java by Google, offering utilities like collections, caching, and concurrency.
- **Spring Dotenv**: Supports loading environment variables from `.env` files in Spring Boot applications.
- **Spring Boot Starter Test**: Provides testing support for Spring Boot applications, including JUnit and Spring testing utilities.
- **Mockito Core**: Library for mocking objects in unit tests.
- **Apache Commons CSV**: A library for parsing and writing CSV files.
