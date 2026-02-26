package com.cosmic.user_service.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class UserTestController {
    @GetMapping("/test")
    public String userTest() {
        return "Hello User, You are authorized!";
    }
}
