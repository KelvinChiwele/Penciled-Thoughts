package com.techart.writersblock.sqliteutils;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.techart.writersblock.constants.Constants.AUTHORITY;

/**
 * Defines constants that help with the Content URIs:
 * 1. Column names
 * 2. Table names
 * Created by Kelvin on 30/05/2017.
 */

public class WritersBlockContract {


    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    protected static final String PATH_POEMS = "poems";
    protected static final String PATH_SPIRITUALS = "spirituals";
    protected static final String PATH_STORIES = "stories";
    protected static final String PATH_CHAPTERS = "chapter";

    /**
     * Defines content URIs for the POEM table
     */
    public static final class PoemEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_POEMS).build();

        /**
         * Special type prefixes that specify if a URI returns a list or a specific item
         */
        public static final String CONTENT_TYPE =  CONTENT_URI  + PATH_POEMS;
        public static final String CONTENT_ITEM_TYPE = CONTENT_URI + "/#" + PATH_POEMS;

        /**
         * Constants for identifying POEM table and columns
         */
        public static final String TABLE_POEM = "poems";
        public static final String POEM_ID = "_id";
        public static final String POEM_TITLE = "poemTitle";
        public static final String POEM_TEXT = "poemText";
        public static final String POEM_FIREBASEURL = "poemFireBaseUrl";
        public static final String POEM_CREATED = "poemCreated";

        public static final String[] ALL_COLUMNS =
                {POEM_ID, POEM_TITLE,POEM_FIREBASEURL, POEM_TEXT, POEM_CREATED};
        //SQL to create POEM table
        protected static final String CREATE_POEM_TABLE =
                "CREATE TABLE " + TABLE_POEM + " ("
                        + POEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + POEM_TITLE + " VARCHAR(60) NOT NULL, "
                        + POEM_TEXT + " TEXT NOT NULL, "
                        + POEM_FIREBASEURL + " VARCHAR(100), "
                        + POEM_CREATED + " TEXT default CURRENT_TIMESTAMP"
                        + ")";

        // Define a function to build a URI to find a specific movie by it's identifier
        public static Uri buildPoemUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    public static final class SpiritualEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPIRITUALS).build();

        /**
         * Special type prefixes that specify if a URI returns a list or a specific item
         */
        public static final String CONTENT_TYPE =  CONTENT_URI  + PATH_SPIRITUALS;
        public static final String CONTENT_ITEM_TYPE = CONTENT_URI + "/#" + PATH_SPIRITUALS;


        /**
         * Constants for identifying DEVOTION table and columns
         */
        public static final String TABLE_SPIRITUAL = "spirituals";
        public static final String SPIRITUAL_ID = "_id";
        public static final String SPIRITUAL_TITLE = "spiritualTitle";
        public static final String SPIRITUAL_TEXT = "spiritualsText";
        public static final String SPIRITUAL_FIREBASE_URL = "spiritualFireBaseUrl";
        public static final String SPIRITUAL_CREATED = "spiritualsCreated";

        public static final String[] ALL_COLUMNS =
                {SPIRITUAL_ID, SPIRITUAL_TITLE, SPIRITUAL_TEXT, SPIRITUAL_FIREBASE_URL, SPIRITUAL_CREATED};
        //SQL to create POEM table
        protected static final String CREATE_SPIRITUALS_TABLE =
                "CREATE TABLE " + TABLE_SPIRITUAL + " ("
                        + SPIRITUAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + SPIRITUAL_TITLE + " VARCHAR(60) NOT NULL, "
                        + SPIRITUAL_TEXT + " TEXT NOT NULL, "
                        + SPIRITUAL_FIREBASE_URL + " VARCHAR(100), "
                        + SPIRITUAL_CREATED + " TEXT default CURRENT_TIMESTAMP"
                        + ")";

        // Define a function to build a URI to find a specific movie by it's identifier
        public static Uri buildPoemUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class StoryEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STORIES).build();

        /**
         * Special type prefixes that specify if a URI returns a list or a specific item
         */
        public static final String CONTENT_TYPE = CONTENT_URI  +  PATH_STORIES;
        public static final String CONTENT_ITEM_TYPE = CONTENT_URI + "/#" + PATH_STORIES;

        /**
         * Constants for identifying STORY table and columns
         */
        public static final String TABLE_STORIES = "stories";
        public static final String STORY_ID = "_id";
        public static final String STORY_REFID = "storyRefId";
        public static final String STORY_TITLE = "storyTitle";
        public static final String STORY_CATEGORY = "storyCategory";
        public static final String STORY_DESCRIPTION = "storyDescription";
        public static final String STORY_CREATED = "storyCreated";


        public static final String[] STORY_COLUMNS =
                {STORY_ID, STORY_TITLE,STORY_CATEGORY,STORY_REFID, STORY_DESCRIPTION};

        //SQL to create STORY table
        protected static final String CREATE_STORY_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_STORIES + " ("
                        + STORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + STORY_REFID + " VARCHAR(100) , "
                        + STORY_TITLE + " VARCHAR(60) NOT NULL, "
                        + STORY_DESCRIPTION + " TEXT, "
                        + STORY_CATEGORY + " VARCHAR(12), "
                        + STORY_CREATED + " TEXT default CURRENT_TIMESTAMP"
                        + ")";


        // Define a function to build a URI to find a specific movie by it's identifier
        public static Uri buildStoryUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ChapterEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHAPTERS).build();

        /**
         * Special type prefixes that specify if a URI returns a list or a specific item
         */
        public static final String CONTENT_TYPE = CONTENT_URI  + PATH_CHAPTERS;
        public static final String CONTENT_ITEM_TYPE = CONTENT_URI + "/#" + PATH_CHAPTERS;

        /**
         * Constants for identifying CHAPTER table and columns
         */
        public static final String TABLE_CHAPTER = "chapter";
        public static final String CHAPTER_ID = "_id";
        public static final String CHAPTER_TITLE = "chapterTitle";
        public static final String CHAPTER_CONTENT = "chapterContent";
        public static final String CHAPTER_IS_EDITED = "isChapterEdited";
        public static final String CHAPTER_URL = "chapterUrl";
        public static final String CHAPTER_FIREBASE_STORY_URL = "FireBaseStoryUrl";
        public static final String CHAPTER_STORY_ID = "_idChapterStory";
        public static final String CHAPTER_CREATED = "chapterCreated";

        public static final String[] CHAPTER_COLUMNS =
                {CHAPTER_ID, CHAPTER_TITLE, CHAPTER_STORY_ID,CHAPTER_IS_EDITED, CHAPTER_FIREBASE_STORY_URL, CHAPTER_URL, CHAPTER_CONTENT};

        //SQL to create CHAPTER  table
        protected static final String CREATE_CHAPTER_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_CHAPTER + " ("
                        + CHAPTER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + CHAPTER_TITLE + " VARCHAR(100), "
                        + CHAPTER_CONTENT + " TEXT, "
                        + CHAPTER_CREATED + " TEXT default CURRENT_TIMESTAMP, "
                        + CHAPTER_URL + " VARCHAR(100) , "
                        + CHAPTER_FIREBASE_STORY_URL + " VARCHAR(100) , "
                        + CHAPTER_IS_EDITED + " VARCHAR(5), "
                        + CHAPTER_STORY_ID + " INTEGER, " + " FOREIGN KEY (" + CHAPTER_STORY_ID + ")" + " REFERENCES "  + StoryEntry.TABLE_STORIES + "(" + StoryEntry.STORY_ID  + ")"
                        + ")";

        // Define a function to build a URI to find a specific movie by it's identifier
        public static Uri buildChapterUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}