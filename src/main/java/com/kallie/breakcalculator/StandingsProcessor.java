package com.kallie.breakcalculator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StandingsProcessor {
    private String serverUrl;
    private String tournamentSlug;
    private List<Team> teams = new ArrayList<>();
    private List<Team> jrTeams = new ArrayList<>();

    public StandingsProcessor(String url) {
        System.out.println("DEBUG: StandingsProcessor constructor called with URL: " + url);
        String[] tournamentUrl = url.split("/");
        this.serverUrl = tournamentUrl[0] + "//" + tournamentUrl[2] + "/";
        this.tournamentSlug = tournamentUrl[3];
        System.out.println("DEBUG: Server URL: " + this.serverUrl);
        System.out.println("DEBUG: Tournament slug: " + this.tournamentSlug);
        findTeams();
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
        Scanner scanner = new Scanner(url.openStream());
        StringBuilder inline = new StringBuilder();
        while (scanner.hasNext()) {
            inline.append(scanner.next());
        }
        scanner.close();

        return inline.toString();
    }
    
    private void findTeams() {
        String urlString = serverUrl + "api/v1/tournaments/" + tournamentSlug + "/teams";
        System.out.println("DEBUG: Fetching teams from: " + urlString);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String response = urlToString(urlString);
            System.out.println("DEBUG: Teams API response length: " + response.length());
            
            Team[] teamArray = mapper.readValue(response, Team[].class);
            System.out.println("DEBUG: Found " + teamArray.length + " teams");
            
            for (Team team : teamArray) {
                teams.add(team);
                if (team.isJunior())
                    jrTeams.add(team);
            }
            System.out.println("DEBUG: Total teams: " + teams.size() + ", Junior teams: " + jrTeams.size());
        } catch (IOException e) {
            System.out.println("DEBUG: Error in findTeams: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Object[][] getCurrentStandings() {
        List<List<Map<String, Object>>> standingsData = readStandings();
        List<Object[]> currentStandings = new ArrayList<>();
        
        // Create a map for quick team lookup by name
        Map<String, Team> teamMap = new HashMap<>();
        for (Team team : teams) {
            teamMap.put(team.getName(), team);
        }
        
        for (List<Map<String, Object>> teamData : standingsData) {
            String teamName = teamData.get(0).get("text").toString();
            
            int teamPoints = 0;
            if (teamData.get(1).containsKey("sort")) {
                teamPoints = Integer.parseInt(teamData.get(1).get("sort").toString());
            } else if (teamData.get(2).containsKey("sort")) {
                teamPoints = Integer.parseInt(teamData.get(2).get("sort").toString());
            }
            
            // Find the Team object that matches this team name
            Team teamObject = teamMap.get(teamName);
            if (teamObject == null) {
                // If exact match not found, try to find by checking if the displayed name contains the team reference
                for (Team team : teams) {
                    if (teamName.contains(team.getName())) {
                        teamObject = team;
                        break;
                    }
                }
            }
            
            currentStandings.add(new Object[]{teamObject, teamPoints});
        }
        
        // Sort by points (descending order)
        currentStandings.sort((a, b) -> Integer.compare((Integer)b[1], (Integer)a[1]));
        
        return currentStandings.toArray(new Object[0][]);
    }
    
    private List<List<Map<String, Object>>> readStandings() {
        String urlString = serverUrl + tournamentSlug + "/tab/current-standings/";
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
                int startIdx = scriptContent.indexOf("tablesData:[");
                int endIdx = scriptContent.lastIndexOf("]");
                if (startIdx == -1 || endIdx == -1)
                    continue;
                scriptContent = scriptContent.substring(0, endIdx);
                scriptContent = scriptContent.substring(startIdx + 12);

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
}