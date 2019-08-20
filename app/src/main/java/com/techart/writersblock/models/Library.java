package com.techart.writersblock.models;

/**
 * Stores stories that are currently been read
 * Created by Kelvin on 05/06/2017.
 */

public class Library {
    private String postTitle;
    private String postKey;
    private Integer chaptersAdded;
    private Long lastAccessed;

    public Library() {}

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public Integer getChaptersAdded() {
        return chaptersAdded;
    }

    public void setChaptersAdded(Integer chaptersAdded) {
        this.chaptersAdded = chaptersAdded;
    }

    public Long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
}
