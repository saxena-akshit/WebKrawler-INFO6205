package com.info6205.webcrawler.entity;

public class UrlTask {

    public final String url;
    public final int depth;
    public final int priority;

    public UrlTask(String url, int depth, int priority) {
        this.url = url;
        this.depth = depth;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
