package com.kallie.breakcalculator;

import java.util.List;
import java.util.Map;

public class CalicotabDataFetcher {
    private String serverUrl, tournamentSlug;
    private int roundsPassed = -1;
    
    private StandingsPageProcessor standingsProcessor;
    private TeamsPageProcessor teamsProcessor;
    private ParticipantsPageProcessor participantsProcessor;

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
        participantsProcessor = new ParticipantsPageProcessor(serverUrl, tournamentSlug);
    }

    public int getNumTeams() {
        return getMax(standingsProcessor.getNumTeams(), teamsProcessor.getNumTeams(), participantsProcessor.getNumTeams());
    }

    public int getNumJrTeams() {
        return getMax(standingsProcessor.getNumJrTeams(), teamsProcessor.getNumJrTeams(), participantsProcessor.getNumJrTeams());
    }

    public int getRoundsPassed() { 
        return roundsPassed;
    }

    public int getMax(int a, int b, int c) {
        return Math.max(a, Math.max(b, c));
    }

    public Object[][] getCurrentStandings() {
        Object[][] currentResults;
        List<List<Map<String, Object>>> standingsData = standingsProcessor.readStandingsPage();

        if (standingsData.isEmpty()) {
            System.out.println("DEBUG: No standings data found -- not an ongoing tournament");
            List<List<Map<String, Object>>> teamsData = teamsProcessor.readTeamsPage(); 

            if (teamsData.isEmpty()) {
                System.out.println("DEBUG: No teams data found -- not a completed tournament");
                List<List<Map<String, Object>>> participantsData = participantsProcessor.readParticipantsPage();

                currentResults = participantsProcessor.processParticipantsPage(participantsData);
                roundsPassed = participantsProcessor.getRoundsPassed();
            } else {
                currentResults = teamsProcessor.processTeamsPage(teamsData);
                roundsPassed = teamsProcessor.getRoundsPassed();
            }
        } else {
            currentResults = standingsProcessor.processStandingsPage(standingsData);
            roundsPassed = standingsProcessor.getRoundsPassed();
        }

        return currentResults;
    }
}