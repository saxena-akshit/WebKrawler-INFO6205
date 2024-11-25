package com.info6205.webcrawler.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping(value = "/test-api-new", method = RequestMethod.HEAD)
    public ResponseEntity<Void> testApi() {
        return ResponseEntity.ok().build();
    }
}
