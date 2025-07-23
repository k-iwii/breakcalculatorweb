package com.kallie.breakcalculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TeamsPageProcessor {
    private String serverUrl, tournamentSlug;
    private List<Team> teams = new ArrayList<>(), jrTeams = new ArrayList<>();
    private int roundsPassed = -1;

    public int getNumTeams() {
        return teams.size();
    }

    public int getNumJrTeams() {
        return jrTeams.size();
    }

    public TeamsPageProcessor(String serverUrl, String tournamentSlug) {
        this.serverUrl = serverUrl;
        this.tournamentSlug = tournamentSlug;
        System.out.println("DEBUG: Server URL: " + this.serverUrl);
        System.out.println("DEBUG: Tournament slug: " + this.tournamentSlug);
    }

    // create a map <team display names, Team objects>
    private HashMap<String, Team> findTeams(List<List<Map<String, Object>>> teamsData) {
        String urlString = serverUrl + "api/v1/tournaments/" + tournamentSlug + "/teams";
        System.out.println("DEBUG: Fetching teams from: " + urlString);

        Team[] teamArray = new Team[0];
        HashMap<String, Team> teamMap = new HashMap<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String response = urlToString(urlString);
            
            // Check if response is an HTTP error code
            try {
                Integer.parseInt(response);
                System.out.println("DEBUG: API returned error code: " + response);
                return new HashMap<String, Team>(); // Exit if we got an error code instead of JSON
            } catch (NumberFormatException e) {
                // Response is not a number, proceed with JSON parsing
            }
            
            teamArray = mapper.readValue(response, Team[].class);
            System.out.println("DEBUG: Found " + teamArray.length + " teams");
            
            displayNameType(teamsData, teamArray);

            for (Team team : teamArray) {
                teams.add(team);
                teamMap.put(team.getDisplayName(), team);
                if (team.isJunior())
                    jrTeams.add(team);
            }
            System.out.println("DEBUG: Total teams: " + teams.size() + ", Junior teams: " + jrTeams.size());
        } catch (IOException e) {
            System.out.println("DEBUG: Error in findTeams: " + e.getMessage());
            e.printStackTrace();
        }

        return teamMap;
    }
    
    private String urlToString(String urlString) throws IOException {
        System.out.println("DEBUG: Making HTTP request to: " + urlString);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        System.out.println("DEBUG: HTTP response code: " + responseCode);
        if (responseCode != 200) {
            return responseCode + "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return reader.lines().collect(Collectors.joining());
        }
    }
    

    // return a 2D array [Team object][points]
    public Object[][] processTeamsPage(List<List<Map<String, Object>>> teamsData) {
        List<Object[]> currentResults = new ArrayList<>();

        // get a <name, Team> map for quick lookup
        HashMap<String, Team> teamMap = findTeams(teamsData);
        
        for (List<Map<String, Object>> teamData : teamsData) {
            // get team name
            String teamName = StringEscapeUtils.unescapeHtml4(teamData.get(1).get("text").toString());

            if (roundsPassed == -1)
                findRoundsPassed(teamData);

            // find the Team object that matches this team name
            Team teamObject = teamMap.get(teamName);
            if (teamObject == null) {
                // If exact match not found, try to find by checking if the displayed name contains the team reference
                String capName = teamName.toUpperCase();
                for (Team team : teams) {
                    if (capName.contains(team.getDisplayName().toUpperCase())) {
                        teamObject = team;
                        break;
                    }
                }
            }

            // get points
            String strPoints = teamData.get(9).get("sort").toString();
            if (strPoints.endsWith(".0"))
                strPoints = strPoints.substring(0, strPoints.length() - 2);
            int teamPoints = Integer.parseInt(strPoints);
            
            currentResults.add(new Object[]{teamObject, teamPoints});
        }
        
        // Sort by points (descending order)
        //currentResults.sort((a, b) -> Integer.compare((Integer)b[1], (Integer)a[1]));
        
        return currentResults.toArray(new Object[0][]);
    }

    // populate the displayName field of each Team object based on whether reference or shortName is used in team page
    public void displayNameType(List<List<Map<String, Object>>> teamsData, Team[] teamArray) {
        boolean isReference = false;
        for (int i = 0; i < teamArray.length; i++) {
            if (teamArray[i].getReference().equals(teamArray[i].getShortName()))
                continue;
            
            String teamName = StringEscapeUtils.unescapeHtml4(teamsData.get(i).get(0).get("text").toString());

            // check if this name is a reference or shortName
            // prob have to loop again b/c api and standings list teams in diff orders
            for (Team team : teamArray) {
                if (team.getReference() != null && team.getReference().equalsIgnoreCase(teamName)) {
                    isReference = true;
                    break;
                } else if (team.getShortName() != null && team.getShortName().equalsIgnoreCase(teamName)) {
                    isReference = false;
                    break;
                }
            }
        }

        // set the displayName for each team
        for (Team team : teamArray) {
            if (isReference) {
                team.setDisplayName(team.getReference());
            } else {
                team.setDisplayName(team.getShortName());
            }
        }
    }

    
    // retrieve the current team data from the tournament calicotab page
    public List<List<Map<String, Object>>> readTeamsPage() {
        String urlString = serverUrl + tournamentSlug + "/tab/team/";
        List<List<Map<String, Object>>> ans = new ArrayList<>();

        try {
            String url = urlToString(urlString);

            if (url.equals("403")) {
                return new ArrayList<>();
            }
            
            Document doc = Jsoup.parse(url);            

            Elements scriptElements = doc.select("script");

            for (Element script : scriptElements) {
                String scriptContent = script.html();
                int startIdx = scriptContent.indexOf("tablesData: [");
                if (startIdx == -1)
                    continue;
                
                int endIdx = scriptContent.lastIndexOf("]");
                if (endIdx == -1)
                    continue;
                    
                scriptContent = scriptContent.substring(startIdx + 13, endIdx);

                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> map = mapper.readValue(scriptContent, Map.class);

                @SuppressWarnings("unchecked")
                List<List<Map<String, Object>>> teams = (List<List<Map<String, Object>>>)map.get("data");
                                
                ans = teams;
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return ans;
    }

    // find the number of rounds in the tournament; this is the same as the number of rounds passed
    private void findRoundsPassed(List<Map<String, Object>> teamData) {
        if (teamData.isEmpty() || teamData.size() < 3) {
            System.out.println("DEBUG: No rounds passed found in teams data.");
            return;
        }

        int x = 1;
        for (Map<String, Object> info : teamData) {
            if (info.size() < 3)
                x++;
        }

        roundsPassed = teamData.size() - x;
    }

    public int getRoundsPassed() { 
        return roundsPassed;
    }
}