package com.kallie.breakcalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    /*@PostMapping("/calculate")
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
    }*/
    
    @GetMapping("/standings")
    public List<Map<String, Object>> getStandings(@RequestParam String url) {
        System.out.println("DEBUG: Received GET request to /api/standings");
        System.out.println("DEBUG: URL parameter: " + url);
        
        try {
            StandingsProcessor standingsProcessor = new StandingsProcessor(url);
            Object[][] standings = standingsProcessor.getCurrentStandings();
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object[] standing : standings) {
                Map<String, Object> standingMap = new HashMap<>();
                standingMap.put("team", standing[0]);
                standingMap.put("points", standing[1]);
                result.add(standingMap);
            }
            return result;
        } catch (Exception e) {
            System.out.println("DEBUG: Error occurred: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
