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
    
    private String debateFormat;
    private int totalRounds;
    private int openBreak;
    private int jrBreak;
    private int teams;
    private int jrTeams;
    
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
    
    @PostMapping("/tournament-details")
    public String saveTournamentDetails(
        @RequestParam String debateFormat,
        @RequestParam int totalRounds,
        @RequestParam String openElimRound,
        @RequestParam String jrElimRound
    ) {
        this.debateFormat = debateFormat;
        this.totalRounds = totalRounds;

        switch (openElimRound.toLowerCase()) {
            case "partial double octofinals":
                openBreak = 48; break;
            case "octofinals":
                openBreak = 32; break;
            case "quarterfinals":
                openBreak = 16; break;
            case "semifinals":
                openBreak = 8; break;
            case "finals":
                openBreak = 4; break;
        }

        switch (jrElimRound.toLowerCase()) {
            case "junior partial double octofinals":
                jrBreak = 48; break;
            case "junior octofinals":
                jrBreak = 32; break;
            case "junior quarterfinals":
                jrBreak = 16; break;
            case "junior semifinals":
                jrBreak = 8; break;
            case "junior finals":
                jrBreak = 4; break;
        }

        if (this.debateFormat.equals("World Schools")) {
            openBreak /= 2;
            jrBreak /= 2;
        }
        
        return "Tournament details saved successfully";
    }
    
    @GetMapping("/results")
    public Map<String, Object> getResults() {
        Map<String, Object> results = new HashMap<>();
        results.put("debateFormat", this.debateFormat);
        results.put("numberOfRounds", this.totalRounds);

        
        
        return results;
    }
}
