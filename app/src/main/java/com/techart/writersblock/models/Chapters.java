package com.techart.writersblock.models;

import java.util.List;

/**
 * Model for chapters
 * Created by Kelvin on 30/08/2017.
 */

public class Chapters {
    private static Chapters instance;

    private Chapters() {}

    private List<String> chapters;


    public List<String> getChapters() {
        return chapters;
    }

    public void setChapters(List<String> chapters) {
        this.chapters = chapters;
    }

    public static synchronized Chapters getInstance(){
        if(instance==null){
            instance=new Chapters();
        }
        return instance;
    }
}
