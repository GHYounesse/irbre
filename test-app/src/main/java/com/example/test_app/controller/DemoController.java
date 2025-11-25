package com.example.test_app.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/user/{id}")
    public String getUser(@PathVariable String id) {
        return "User " + id;
    }

    @GetMapping("/compute")
    public int compute() {
        int x = 0;
        for (int i = 0; i < 5_000_000; i++) {
            x += i;
        }
        return x;
    }
}
