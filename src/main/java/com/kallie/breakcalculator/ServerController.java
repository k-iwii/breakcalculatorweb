package com.kallie.breakcalculator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api") // Base path for all endpoints in this controller
public class ServerController {
    @GetMapping("/test")
    public String testEndpoint(@RequestParam(value = "name", required = false) String name) {
        if (name != null) {
            return "Hello, " + name + "!";
        }
        return "This is a test response!";
    }
}
