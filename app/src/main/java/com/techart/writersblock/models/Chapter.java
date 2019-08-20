package com.techart.writersblock.models;

/**
 * Model for chapters
 * Created by Kelvin on 30/08/2017.
 */

public class Chapter {
    private String chapterTitle;
    private String Content;
    private Long numComments;

    public Chapter()
    {

    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public Long getNumComments() {
        return numComments;
    }

    public void setNumComments(Long numComments) {
        this.numComments = numComments;
    }
}
