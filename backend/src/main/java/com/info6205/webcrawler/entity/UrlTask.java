package com.info6205.webcrawler.entity;

public class UrlTask {

    private final String url;
    private final int depth;
    private final int priority;

    public UrlTask(String url, int depth, int priority) {
        this.url = url;
        this.depth = depth;
        this.priority = priority;
    }

    public int getPriority() {
        return this.priority;
    }

    public int getDepth() {
        return this.depth;
    }

    public String getUrl() {
        return this.url;
    }
}
