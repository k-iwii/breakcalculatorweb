package com.kallie.breakcalculator;

import java.util.List;
import java.util.Map;

public class CalicotabDataFetcher {
    private String serverUrl, tournamentSlug;
    private int roundsPassed = -1;
    
    private StandingsPageProcessor standingsProcessor;
    private TeamsPageProcessor teamsProcessor;

    public CalicotabDataFetcher(String url) {
        System.out.println("DEBUG: StandingsProcessor constructor called with URL: " + url);
        String[] tournamentUrl = url.split("/");
        if (tournamentUrl.length < 4) {
            System.out.println("ERROR: Invalid URL format. Expected format: protocol://domain/tournament-slug");
            return;
        }
        this.serverUrl = tournamentUrl[0] + "//" + tournamentUrl[2] + "/";
        this.tournamentSlug = tournamentUrl[3];
        System.out.println("DEBUG: Server URL: " + this.serverUrl);
        System.out.println("DEBUG: Tournament slug: " + this.tournamentSlug);

        standingsProcessor = new StandingsPageProcessor(serverUrl, tournamentSlug);
        teamsProcessor = new TeamsPageProcessor(serverUrl, tournamentSlug);
    }

    public int getNumTeams() {
        return Math.max(standingsProcessor.getNumTeams(), teamsProcessor.getNumTeams());
    }

    public int getNumJrTeams() {
        return Math.max(standingsProcessor.getNumJrTeams(), teamsProcessor.getNumJrTeams());
    }

    public int getRoundsPassed() { 
        return roundsPassed;
    }

    // kind of the main function!
    public Object[][] getCurrentStandings() {
        // first, try standingsprocessor
        
        List<List<Map<String, Object>>> standingsData = standingsProcessor.readStandingsPage();

        // occurs when the tournament has ended & the standings page is hidden; use teams page instead
        if (standingsData.isEmpty()) {
            System.out.println("DEBUG: No standings data found, using teams page instead.");

            List<List<Map<String, Object>>> teamsData = teamsProcessor.readTeamsPage();
            Object[][] currentResults = teamsProcessor.processTeamsPage(teamsData);
            roundsPassed = teamsProcessor.getRoundsPassed();
            return currentResults;
        }

        Object[][] currentResults = standingsProcessor.processStandingsPage(standingsData);
        roundsPassed = standingsProcessor.getRoundsPassed();
        return currentResults;
    }
}