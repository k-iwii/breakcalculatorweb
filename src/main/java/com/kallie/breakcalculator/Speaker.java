package com.kallie.breakcalculator;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Connection;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;

public class Speaker {
    private int id;
    private String name;
    private String url;
    private boolean isJunior;
    @JsonProperty("categories")
    private String[] breakCategories;

    public Speaker() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isJunior() {
        return isJunior;
    }

    public String[] getBreakCategories() {
        return breakCategories;
    }

    public void setBreakCategories(String[] breakCategories) {
        this.breakCategories = breakCategories;
        ObjectMapper objectMapper = new ObjectMapper();
        
        for (String category : breakCategories) {
            try {
                JsonNode rootNode = objectMapper.readTree(new URL(category));
                JsonNode nameNode = rootNode.path("name");
                if (!nameNode.isMissingNode()) {
                    String name = nameNode.asText().toLowerCase();
                    if (name.contains("junior") || name.contains("novice")) {
                        this.isJunior = true;
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
