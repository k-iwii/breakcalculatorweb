package com.kallie.breakcalculator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    /*
        @RequestParam int openBreak,
        @RequestParam int jrBreak,
        @RequestParam int totalRounds,
        @RequestParam String format
     */
    @PostMapping("/calculate")
    public String calculateBreaks(@RequestParam String url) {
        System.out.println("DEBUG: Received POST request to /api/calculate");
        System.out.println("DEBUG: URL parameter: " + url);
        
        try {
            System.out.println("DEBUG: Creating StandingsProcessor...");
            StandingsProcessor standingsProcessor = new StandingsProcessor(url);
            System.out.println("DEBUG: StandingsProcessor created successfully");
            Object[][] standings = standingsProcessor.getCurrentStandings();
            
            return "Calculation initiated for tournament: " + url;
        } catch (Exception e) {
            System.out.println("DEBUG: Error occurred: " + e.getMessage());
            e.printStackTrace();
            return "Error processing request: " + e.getMessage();
        }
    }
    
    @GetMapping("/standings")
    public Object[][] getStandings(@RequestParam String url) {
        System.out.println("DEBUG: Received GET request to /api/standings");
        System.out.println("DEBUG: URL parameter: " + url);
        
        try {
            StandingsProcessor standingsProcessor = new StandingsProcessor(url);
            return standingsProcessor.getCurrentStandings();
        } catch (Exception e) {
            System.out.println("DEBUG: Error occurred: " + e.getMessage());
            e.printStackTrace();
            return new Object[0][0];
        }
    }
}
