package com.techart.writersblock.models;

/**
 * Model for devotion
 * Created by Kelvin on 05/06/2017.
 */

public class Devotion {
    private String title;
    private String authorUrl;
    private String author;
    private Boolean isPostEdited;
    private Long numLikes;
    private Long numComments;
    private Long numViews;
    private String devotionText;
    private Long timeCreated;

    public Devotion()
    {

    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }


    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public Boolean getIsPostEdited() {
        return isPostEdited;
    }
    public void setIsPostEdited(Boolean isPostEdited) {
        this.isPostEdited = isPostEdited;
    }


    public String getDevotionText() {
        return devotionText;
    }

    public void setDevotionText(String devotionText) {
        this.devotionText = devotionText;
    }

    public Long getTimeCreated() {
        return timeCreated;
    }
    public void setTimeCreated(Long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public Long getNumComments() {
        return numComments;
    }

    public void setNumComments(Long numComments) {
        this.numComments = numComments;
    }

    public Long getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(Long numLikes) {
        this.numLikes = numLikes;
    }


    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public Long getNumViews() {
        return numViews;
    }

    public void setNumViews(Long numViews) {
        this.numViews = numViews;
    }
}
