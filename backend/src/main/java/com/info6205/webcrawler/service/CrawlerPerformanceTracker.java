package com.info6205.webcrawler.service;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class CrawlerPerformanceTracker {

    private static final Logger logger = LogManager.getLogger(CrawlerPerformanceTracker.class);
    private static final String CSV_FILE_PATH = "crawler_performance_metrics.csv";

    private Instant startTime;
    private Instant endTime;
    private final AtomicInteger urlsCrawled = new AtomicInteger(0);
    private long peakMemoryUsed = 0;
    private int threadPoolSize;
    private int maxDepth;
    private String startUrl;

    public void startTracking(int threadPoolSize, int maxDepth, String startUrl) {
        this.threadPoolSize = threadPoolSize;
        this.maxDepth = maxDepth;
        this.startUrl = startUrl;
        this.urlsCrawled.set(0);
        this.startTime = Instant.now();
        this.peakMemoryUsed = 0;
    }

    public void incrementUrlsCrawled() {
        urlsCrawled.incrementAndGet();
        updateMemoryUsage();
    }

    private void updateMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        peakMemoryUsed = Math.max(peakMemoryUsed, heapUsage.getUsed());
    }

    public void endTracking() {
        this.endTime = Instant.now();
        logPerformanceMetrics();
        writeMetricsToCsv();
    }

    private void logPerformanceMetrics() {
        long duration = Duration.between(startTime, endTime).toMillis();
        double urlsPerSecond = (urlsCrawled.get() * 1000.0) / duration;

        logger.info("Crawl Performance Metrics:");
        logger.info("Start URL: {}", startUrl);
        logger.info("Thread Pool Size: {}", threadPoolSize);
        logger.info("Max Depth: {}", maxDepth);
        logger.info("Total URLs Crawled: {}", urlsCrawled.get());
        logger.info("Total Crawl Duration: {} ms", duration);
        logger.info("URLs Crawled per Second: {:.2f}", urlsPerSecond);
        logger.info("Peak Memory Used: {} MB", peakMemoryUsed / (1024 * 1024));
    }

    private void writeMetricsToCsv() {
        long duration = Duration.between(startTime, endTime).toMillis();
        double urlsPerSecond = (urlsCrawled.get() * 1000.0) / duration;

        try (@SuppressWarnings("deprecation") CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(CSV_FILE_PATH, true),
                CSVFormat.DEFAULT.withHeader(
                        "StartTimestamp",
                        "StartUrl",
                        "ThreadPoolSize",
                        "MaxDepth",
                        "TotalUrlsCrawled",
                        "CrawlDurationMs",
                        "UrlsPerSecond",
                        "PeakMemoryUsedMB"
                ))) {

            csvPrinter.printRecord(
                    startTime.toString(),
                    startUrl,
                    threadPoolSize,
                    maxDepth,
                    urlsCrawled.get(),
                    duration,
                    String.format("%.2f", urlsPerSecond),
                    peakMemoryUsed / (1024 * 1024)
            );
        } catch (IOException e) {
            logger.error("Error writing to CSV: {}", e.getMessage());
        }
    }
}
