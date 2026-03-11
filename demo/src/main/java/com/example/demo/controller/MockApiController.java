package com.example.demo.controller;

import com.example.demo.client.JSONPlaceHolderClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class MockApiController {

    private final JSONPlaceHolderClient jsonPlaceHolderClient;

    public MockApiController(JSONPlaceHolderClient jsonPlaceHolderClient) {
        this.jsonPlaceHolderClient = jsonPlaceHolderClient;
    }

    @GetMapping("/users")
    public ResponseEntity<Object> getMockUsers() {
        var res = jsonPlaceHolderClient.getUsers();

        return ResponseEntity.ok(res);
    }
}
