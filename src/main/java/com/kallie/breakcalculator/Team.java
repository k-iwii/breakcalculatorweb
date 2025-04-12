package com.kallie.breakcalculator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Team {
    private int id;
    private String name;
    private String url;
    private boolean isJunior;
    /*@JsonProperty("break_categories")
    private String[] breakCategories;*/
    private Speaker[] speakers;

    public Team() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @JsonProperty("reference")
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

    /*public String[] getBreakCategories() {
        return breakCategories;
    }

    // fix this stupid break categories situation where tourneys will sometimes hide break categories
    public void setBreakCategories(String[] breakCategories) {
        this.breakCategories = breakCategories;
        this.isJunior = breakCategories.length > 1;
    }

    public void checkAndSetBreakCategories() {
        if (this.breakCategories == null) {
            Speaker youngerSpeaker = speakers[0].getBreakCategories().length > speakers[1].getBreakCategories().length ? speakers[0] : speakers[1];
            this.breakCategories = youngerSpeaker.getBreakCategories();
            this.isJunior = breakCategories.length > 1;
        }
    }*/

    public Speaker[] getSpeakers() {
        return speakers;
    }

    public void setSpeakers(Speaker[] speakers) {
        this.speakers = speakers;

        isJunior = true;
        for (Speaker speaker : speakers) {
            if (!speaker.isJunior()) {
                isJunior = false;
                break;
            }
        }
    }

    // Custom method to check and set name if null
    @JsonProperty("code_name")
    public void checkAndSetName(String codeName) {
        if (this.name == null) {
            this.name = codeName;
        }
    }
    
}
