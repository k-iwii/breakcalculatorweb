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
    private String lastFetchedUrl = "";
    
    @GetMapping("/standings")
    public List<Object[]> getStandings(@RequestParam String url) {
        System.out.println("DEBUG: Received GET request to /api/standings");
        System.out.println("DEBUG: URL parameter: " + url);
        
        // Check if we already have standings for this URL
        if (!currentStandings.isEmpty() && url.equals(lastFetchedUrl)) {
            System.out.println("DEBUG: Returning cached standings for URL: " + url);
            return currentStandings;
        }
        
        currentStandings.clear();
        
        try {
            StandingsProcessor standingsProcessor = new StandingsProcessor(url);
            Object[][] teamResults = standingsProcessor.getCurrentStandings();

            teams = standingsProcessor.getNumTeams();
            jrTeams = standingsProcessor.getNumJrTeams();
            roundsPassed = standingsProcessor.getRoundsPassed();

            
            for (Object[] team : teamResults)
                currentStandings.add(team);
            
            // Cache the URL for future requests
            lastFetchedUrl = url;
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

    @GetMapping("/row-colour")
    public String[] findRowColour(@RequestParam(required = false) String breakType,
                                 @RequestParam(required = false) String caseType) {
        System.out.println("DEBUG: findRowColour called for breakType: " + breakType + ", caseType: " + caseType);
        
        int len = roundsPassed;
        if (format.equals("bp"))
            len *= 3;
        
        String[] pointColours = new String[len];
        int guaranteedBreak = getGuaranteedBreak(breakType, caseType);

        for (int i = 0; i < len; i++) {
            if (i >= guaranteedBreak)
                pointColours[i] = "#CDF8FC"; // blue
            else if (i + roundsLeft >= guaranteedBreak || i >= guaranteedBreak - 1)
                pointColours[i] = "#CDFCD7"; // green
            else if (i + roundsLeft * 2 >= guaranteedBreak || i + roundsLeft >= guaranteedBreak - 1)
                pointColours[i] = "#E4FCCD"; // light green
            else if (i + roundsLeft * 3 >= guaranteedBreak || i + roundsLeft * 2 >= guaranteedBreak - 1)
                pointColours[i] = "#FBF4CB"; // orange
            else
                pointColours[i] = "#FBDDCB"; // red
        }
        
        return pointColours;
    }

    public int getGuaranteedBreak(String breakType, String caseType) {
        if (format.equals("bp")) {
            if (breakType.equals("open") && caseType.equals("best"))
                return BPSimulator.minOpen + 1; // +1 because of 0-indexing
            else if (breakType.equals("open") && caseType.equals("worst"))
                return BPSimulator.maxOpen + 1;
            else if (breakType.equals("junior") && caseType.equals("best"))
                return BPSimulator.minJr + 1;
            else if (breakType.equals("junior") && caseType.equals("worst"))
                return BPSimulator.maxJr + 1;
        } else if (format.equals("ws")) {
            if (breakType.equals("open") && caseType.equals("best"))
                return WSDCSimulator.minOpen + 1; // +1 because of 0-indexing
            else if (breakType.equals("open") && caseType.equals("worst"))
                return WSDCSimulator.maxOpen + 1;
            else if (breakType.equals("junior") && caseType.equals("best"))
                return WSDCSimulator.minJr + 1;
            else if (breakType.equals("junior") && caseType.equals("worst"))
                return WSDCSimulator.maxJr + 1;
        }
        
        return -1; // Invalid break type or case type
    }

    @GetMapping("/team-colour")
    public String getTeamColour(@RequestParam String breakType,
                               @RequestParam String caseType,
                               @RequestParam int teamPoints) {
        String[] pointColours = findRowColour(breakType, caseType);
        if (teamPoints >= 0 && teamPoints < pointColours.length) {
            return pointColours[teamPoints];
        }
        return "";
    }

    @GetMapping("/results")
    public Map<String, List<String>> getResults(@RequestParam(required = false, defaultValue = "false") boolean showJunior) {
        if (format.equals("bp"))
            while (teams % 4 != 0) teams++;
        else if (format.equals("ws"))
            while (teams % 2 != 0) teams++;
        
        int[][] startingPoints = new int[teams][2]; // [0] = points, [1] = junior status
        for (int i = 0; i < currentStandings.size() && i < teams; i++) {
            Object[] teamResults = currentStandings.get(i);
            Team t = (Team) teamResults[0];
            int points = (int) teamResults[1];

            startingPoints[i][1] = t.isJunior() ? 1 : 0; // 1 for junior, 0 for open

            // if mid-tourney, use points from standings
            if (roundsLeft != totalRounds)
                startingPoints[i][0] = points;
        }
        
        Map<String, List<String>> breakResults = new HashMap<>();

        if (format.equals("bp")){
            BPSimulator sim = new BPSimulator(teams, jrTeams, openBreak, jrBreak, roundsLeft, simulationRuns);    
            breakResults = sim.beginSim(startingPoints, showJunior);
        } else if (format.equals("ws")) {
            WSDCSimulator sim = new WSDCSimulator(teams, jrTeams, openBreak, jrBreak, roundsLeft, simulationRuns);
            breakResults = sim.beginSim(startingPoints, showJunior);
        }

        return breakResults;
    }
}
