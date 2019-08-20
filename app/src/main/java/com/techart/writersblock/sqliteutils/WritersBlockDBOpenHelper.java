package com.techart.writersblock.sqliteutils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Interface to the device database
 * Created by Kelvin on 30/05/2017.
 */
public class WritersBlockDBOpenHelper extends SQLiteOpenHelper {

    //Constants for db name and version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "writersblock.db";

    public WritersBlockDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        addPoemTable(db);
        addSpiritualTable(db);
        addStoryTable(db);
        addChapterTable(db);
    }

    /**
     * Executes when there is an update
     * @param db the database
     * @param oldVersion old db version
     * @param newVersion new db version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WritersBlockContract.PoemEntry.TABLE_POEM);
        db.execSQL("DROP TABLE IF EXISTS " + WritersBlockContract.SpiritualEntry.TABLE_SPIRITUAL);
        db.execSQL("DROP TABLE IF EXISTS " + WritersBlockContract.StoryEntry.TABLE_STORIES);
        db.execSQL("DROP TABLE IF EXISTS " + WritersBlockContract.ChapterEntry.TABLE_CHAPTER);
        onCreate(db);
    }

    /**
     * Creates POEM table
     * @param db database
     */
    private void addPoemTable(SQLiteDatabase db)
    {
        db.execSQL(WritersBlockContract.PoemEntry.CREATE_POEM_TABLE);
    }

    /**
     * Creates DEVOTION table
     * @param db database
     */
    private void addSpiritualTable(SQLiteDatabase db)
    {
        db.execSQL(WritersBlockContract.SpiritualEntry.CREATE_SPIRITUALS_TABLE);
    }

    /**
     * Creates STORY table
     * @param db database
     */
    private void addStoryTable(SQLiteDatabase db)
    {
        db.execSQL(WritersBlockContract.StoryEntry.CREATE_STORY_TABLE);
    }

    /**
     * Creates CHAPTER table
     * @param db database
     */
    private void addChapterTable(SQLiteDatabase db)
    {
        db.execSQL(WritersBlockContract.ChapterEntry.CREATE_CHAPTER_TABLE);
    }
}
