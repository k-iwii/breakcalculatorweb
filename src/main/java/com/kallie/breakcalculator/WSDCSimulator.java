package com.kallie.breakcalculator;

import java.util.*;

public class WSDCSimulator {

    int teams, jrTeams;
    int openBreak, jrBreak;
    int roundsLeft;
    int simulationRuns; // 100,000

	static int[][] permutations = { {0, 1}, {1, 0}};

    public WSDCSimulator(int teams, int jrTeams, int openBreak, int jrBreak, int roundsLeft, int simulationRuns) {
        this.teams = teams;
        this.jrTeams = jrTeams;
        this.openBreak = openBreak;
        this.jrBreak = jrBreak;
        this.roundsLeft = roundsLeft;
        this.simulationRuns = simulationRuns;
    }

    int maxOpen = 0, maxJr = 0;
    int minOpen = Integer.MAX_VALUE, minJr = Integer.MAX_VALUE;
    int[] minOpenFrac = new int[2], minJrFrac = new int[2];
    int[] maxOpenFrac = new int[2], maxJrFrac = new int[2];
    int[][] minArr = new int[teams][2], maxArr = new int[teams][2];

	public void beginSim(int[][] startingPoints) {
        sortDescending(startingPoints);

        for (int i = 0; i < simulationRuns; i++) {
            // simulate using duplicate array of startingPoints
            int[][] sim = simTourney(Arrays.stream(startingPoints).map(a -> Arrays.copyOf(a, a.length)).toArray(int[][]::new));

            //UPDATE JR
            if (jrTeams > 0) {
                if (roundsLeft == 5) {
                    handleBestCase(sim);
                    handleWorstCase(sim);
                } else {
                    if (!handleJr(sim)) continue;
                }
            }

            // UPDATE OPEN
            handleOpen(sim);
        }

        System.out.println(" --- OPEN BREAK ---");
        System.out.println("Best case: " + minOpenFrac[0] + "/" + minOpenFrac[1] + " open teams on " + minOpen + " break.");
        System.out.println("Worst case: " + maxOpenFrac[0] + "/" + maxOpenFrac[1] + " open teams on " + maxOpen + " break.");

        if (jrTeams > 0) {
            System.out.println("\n --- JUNIOR BREAK ---");
            System.out.println("Best case: " + minJrFrac[0] + "/" + minJrFrac[1] + " junior teams on " + minJr + " break.");
            System.out.println("Worst case: " + maxJrFrac[0] + "/" + maxJrFrac[1] + " junior teams on " + maxJr + " break.");
        }
    }

    public int[][] simTourney(int[][] sim) {
        for (int round = 0; round < roundsLeft; round++) {
            for (int room = 0; room < teams; room += 2) {
                int rand = (int) (Math.random() * 2);

                sim[room][0] += permutations[rand][0];
                sim[room + 1][0] += permutations[rand][1];
            }

            sortDescending(sim);
        }

        return sim;
    }
    
    public void handleOpen(int[][] sim) {
        int x = sim[openBreak - 1][0]; // x = lowest # points needed to break

        int[] frac = findFraction(x, sim);

        double ratio = (double) frac[0] / frac[1];
        double minRatio = (double) minOpenFrac[0] / minOpenFrac[1];
        double maxRatio = (double) maxOpenFrac[0] / maxOpenFrac[1];

        if (x < minOpen || x == minOpen && ratio > minRatio) {
            minOpen = x; minOpenFrac = frac; //minArr = sim;
        }
        if (x > maxOpen || x == maxOpen && ratio < maxRatio) {
            maxOpen = x; maxOpenFrac = frac; //maxArr = sim;
        }
    }

    public int[] findFraction(int x, int[][] sim) {
        int total = 0; // total occurences of x
        int broke = 0; // occurences of x among teams that broke
        for (int i = openBreak - 1; i > 0; i--) {
            if (sim[i][0] == x) {
                broke++;
                total++;
            }
            if (sim[i][0] > x) break;
        }

        for (int i = openBreak; i < teams; i++) {
            if (sim[i][0] == x) total++;
            if (sim[i][0] < x) break;
        }

        return new int[] {broke, total};
    }

    public void handleBestCase(int[][] sim) {
        int x = sim[teams - Math.max(jrTeams - jrBreak - openBreak, 0) - 1][0];

        // TEAM ALLOCATION
        int j = jrTeams;
        int openBreakingJrs = 0; // # junior teams that broke open
        for (int i = teams - 1; i >= teams - jrBreak && j > 0; i--, j--) // fill up the last jrBreak teams
            sim[i][1] = 1;
        for (int i = 0; i < openBreak && j > 0; i++, j--) { // fill up the first openBreak teams
            sim[i][1] = 1;
            openBreakingJrs++;
        }
        for (int i = teams - jrBreak - 1; i > 0 && j > 0; i--, j--) // if u have any more teams, fill them from the bottom up
            sim[i][1] = 1;
        
        // GETTING JR FRACTION
        int broke = 0, total = 0;
        for (int i = teams - jrTeams + openBreakingJrs; i < teams; i++) {
            if (sim[i][0] == x && sim[i][1] == 1) {
                if (i < teams - jrTeams + openBreakingJrs + jrBreak)
                    broke++;
                total++;
            }
            if (sim[i][0] < x) break;
        }
        for (int i = teams - jrTeams + openBreakingJrs - 1; i > openBreak; i--) {
            if (sim[i][0] == x && sim[i][1] == 1) total++;
            if (sim[i][0] > x || sim[i][0] == 0) break;
        }

        // UPDATING THE SAVED BEST CASE
        double ratio = (double) broke / total;
        double minJrRatio = (double) minJrFrac[0] / minJrFrac[1];
        if (x < minJr || x == minJr && ratio > minJrRatio) {
            minJr = x;
            minJrFrac = new int[] {broke, total};
            minArr = sim;
        } 
    }

    public void handleWorstCase(int[][] sim) {
        int x = sim[openBreak + jrBreak - 1][0];

        // GETTING JR FRACTION
        int broke = 0, total = 0;
        int i = openBreak;
        for (int j = 0; i < Math.min(teams, openBreak + jrTeams) && j < jrBreak; i++) {
            if (sim[i][0] == x) {
                broke++;
                total++;
                j++;
            }
            if (sim[i][0] < x) break;
        }
        for (; i < Math.min(teams, openBreak + jrTeams); i++) {
            if (sim[i][0] == x) total++;
            if (sim[i][0] < x) break;
        }

        // UPDATING THE SAVED WORST CASE
        double ratio = (double) broke / total;
        double maxJrRatio = (double) maxJrFrac[0] / maxJrFrac[1];
        if (x > maxJr || x == maxJr && ratio < maxJrRatio) {
            maxJr = x;
            maxJrFrac = new int[] {broke, total};
            maxArr = sim;
        }
    }

    public boolean handleJr(int[][] sim) {
        // FINDING X (min # points needed to break jr)
        int x = -1;
        ArrayList<Integer> jrBreaking = new ArrayList<>();
        int i = openBreak;
        for (int j = 0; i < teams; i++) {
            if (sim[i][1] == 1) {
                jrBreaking.add(sim[i][0]);
                j++;

                if (j == jrBreak) {
                    x = sim[i][0];
                    break;
                }
            }
        }

        if (x == -1) return false; // invalid case

        // GETTING JR FRACTION
        int broke = 0, total = 0;
        for (int j : jrBreaking) { // count # of jr teams that broke on x
            if (j == x) {
                broke++;
                total++;
            }
        }
        for (i += 1; i < teams; i++) { // count total # of jr teams on x
            if (sim[i][0] == x && sim[i][1] == 1) total++;
            if (sim[i][0] < x) break;
        }

        // UPDATING THE SAVED CASES
        double ratio = (double) broke / total;
        double minJrRatio = (double) minJrFrac[0] / minJrFrac[1];
        double maxJrRatio = (double) maxJrFrac[0] / maxJrFrac[1];

        if (x < minJr || x == minJr && ratio > minJrRatio) {
            minJr = x;
            minJrFrac = new int[] {broke, total};
            minArr = sim;
        }
        if (x > maxJr || x == maxJr && ratio < maxJrRatio) {
            maxJr = x;
            maxJrFrac = new int[] {broke, total};
            maxArr = sim;
        }

        return true;
    }

    public void sortDescending(int[][] arr) {
        Arrays.sort(arr, new Comparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                return Integer.compare(b[0], a[0]); // compare based on 1st column in descending order
            }
        });
    }

    public void printArr(int[] arr) {
        for (int i = teams - 1; i >= 0; i -= 2)
            System.out.print(arr[i] + " " + arr[i + 1] + " ");
        System.out.println();
    }
}

// unfortunately wsdc doesn't work for anything with esl/efl