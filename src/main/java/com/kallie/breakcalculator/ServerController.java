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
    
    static final int simulationRuns = 100000;
    private String format;
    private int totalRounds, roundsLeft, roundsPassed;
    private int openBreak, jrBreak;
    private int teams, jrTeams;

    private List<Object[]> currentStandings = new ArrayList<>();
    
    @GetMapping("/standings")
    public List<Object[]> getStandings(@RequestParam String url) {
        System.out.println("DEBUG: Received GET request to /api/standings");
        System.out.println("DEBUG: URL parameter: " + url);
        
        try {
            StandingsProcessor standingsProcessor = new StandingsProcessor(url);
            Object[][] teamResults = standingsProcessor.getCurrentStandings();

            teams = standingsProcessor.getNumTeams();
            jrTeams = standingsProcessor.getNumJrTeams();
            roundsPassed = standingsProcessor.getRoundsPassed();

            
            for (Object[] team : teamResults)
                currentStandings.add(team);
            return currentStandings;
        } catch (Exception e) {
            System.out.println("DEBUG: Error occurred: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @PostMapping("/tournament-details")
    public String saveTournamentDetails(
        @RequestParam String format,
        @RequestParam int totalRounds,
        @RequestParam String openElimRound,
        @RequestParam String jrElimRound
    ) {
        this.format = format;
        this.totalRounds = totalRounds;

        if (roundsPassed == -1 || roundsPassed >= totalRounds) // roundsPassed wrong
            roundsLeft = totalRounds;
        else
            roundsLeft = totalRounds - roundsPassed;

        switch (openElimRound.toLowerCase()) {
            case "partial-double-octofinals":
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
            case "junior-partial-double-octofinals":
                jrBreak = 48; break;
            case "junior-octofinals":
                jrBreak = 32; break;
            case "junior-quarterfinals":
                jrBreak = 16; break;
            case "junior-semifinals":
                jrBreak = 8; break;
            case "junior-finals":
                jrBreak = 4; break;
            default:
                jrBreak = 0;
        }

        if (this.format.equals("ws")) {
            openBreak /= 2;
            jrBreak /= 2;
        }
        
        return "Tournament details saved successfully";
    }
    
    @GetMapping("/jr-status")
    public Map<String, Boolean> getJrStatus(@RequestParam(required = false, defaultValue = "false") boolean showJunior) {
        Map<String, Boolean> status = new HashMap<>();
        status.put("showJunior", showJunior);
        return status;
    }

    @GetMapping("/results")
    public Map<String, List<String>> getResults(@RequestParam(required = false, defaultValue = "false") boolean showJunior) {
        int[][] startingPoints = new int[teams][2]; // [0] = points, [1] = junior status
        for (int i = 0; i < teams; i++) {
            Object[] teamResults = currentStandings.get(i);
            Team t = (Team) teamResults[0];
            int points = (int) teamResults[1];
            startingPoints[i][0] = points;
            startingPoints[i][1] = t.isJunior() ? 1 : 0; // 1 for junior, 0 for open
        }
        
        Map<String, List<String>> breakResults = new HashMap<>();

        if (format.equals("bp")){
            while (teams % 4 != 0) teams++; // add swings; if mid-tourney, swings will enter as 0-pt teams

            BPSimulator sim = new BPSimulator(teams, jrTeams, openBreak, jrBreak, roundsLeft, simulationRuns);

            if (roundsLeft == totalRounds) {
                // reset all scores, only keep jr status
                for (int i = 0; i < teams; i++)
                    startingPoints[i][0] = 0;
            }
                
            breakResults = sim.beginSim(startingPoints, showJunior);
        } else if (format.equals("ws")) {
            if (teams % 2 != 0) teams++; // add swings

            WSDCSimulator sim = new WSDCSimulator(teams, jrTeams, openBreak, jrBreak, roundsLeft, simulationRuns);

            if (roundsLeft == totalRounds) {
                // reset all scores, only keep jr status
                for (int i = 0; i < teams; i++)
                    startingPoints[i][0] = 0;
            }
            breakResults = sim.beginSim(startingPoints, showJunior);
        }

        return breakResults;
    }
}
