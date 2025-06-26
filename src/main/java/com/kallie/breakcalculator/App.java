package com.kallie.breakcalculator;

import java.util.Scanner;

public class App 
{
    static final int simulationRuns = 100000;
    static int roundsLeft;

    public static void main( String[] args ) {
        // hi
    }
}

/*
public static void main( String[] args ) {
    Scanner sc = new Scanner(System.in);
    String[] tournamentUrl = sc.next().split("/");
    
    String serverUrl = tournamentUrl[0] + "//" + tournamentUrl[2] + "/";
    String tournamentSlug = tournamentUrl[3];
    Calicotab calicotab = new Calicotab(serverUrl, tournamentSlug);
    calicotab.getTeams().forEach((team) -> {
        System.out.println(team.getName());
    });

    int openBreak = sc.nextInt();
    int jrBreak = sc.nextInt();
    int totalRounds = sc.nextInt();
    String format = sc.next();
    sc.close();

    int teams = calicotab.getTeams().size();
    int jrTeams = calicotab.getJrTeams().size();
    roundsLeft = calicotab.getRoundsLeft(totalRounds);
    int[][] startingPoints = new int[teams][2];

    if (format.equals("BP")){
        while (teams % 4 != 0) teams++; // add swings; if mid-tourney, swings will enter as 0-pt teams

        BPSimulator sim = new BPSimulator(teams, jrTeams, openBreak, jrBreak, roundsLeft, simulationRuns);
        startingPoints = calicotab.getStandings(new int[teams][2]);

        if (roundsLeft == totalRounds) {
            // reset all scores, only keep jr status
            for (int i = 0; i < teams; i++)
                startingPoints[i][0] = 0;
        }
            
        sim.beginSim(startingPoints);
    } else if (format.equals("WSDC")) {
        if (teams % 2 != 0) teams++; // add swings

        WSDCSimulator sim = new WSDCSimulator(teams, jrTeams, openBreak, jrBreak, roundsLeft, simulationRuns);
        startingPoints = calicotab.getStandings(new int[teams][2]);
        if (roundsLeft == totalRounds) {
            // reset all scores, only keep jr status
            for (int i = 0; i < teams; i++)
                startingPoints[i][0] = 0;
        }
        sim.beginSim(startingPoints);
    }
}
 */