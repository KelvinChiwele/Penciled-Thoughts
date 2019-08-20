package com.techart.writersblock.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Static class for converting milliseconds to user friendly
 * time lapse
 * Created by Kelvin on 06/08/2017.
 */
public final class TimeUtils {
    //constants, time in seconds
    public static final int  MILLISECONDS_DAY = 86400000;
    private static final int  MINUTE = 60;
    private static final int  HOUR = 3600;
    private static final int  DAY= 86400;
    private static final int  WEEK= 604800;
    private static final int  MONTH= 2628000;
    private static final int  YEAR= 31556926;
    private static final int MILLISECONDS_IN_A_SECOND = 1000;

    private TimeUtils()
    {
    }

    /**
     * Convert milliseconds to seconds
     * @param timePostedInMilliseconds time to be converted
     * @return time in seconds
     */
    private static long millisecondsToSeconds(long timePostedInMilliseconds)
    {
        return timePostedInMilliseconds / MILLISECONDS_IN_A_SECOND;
    }

    /**
     * Converts seconds to either minutes, hours, days, weeks or months
     * @param timePostedInMilliseconds time to be converted
     * @return time lapse
     */
    public static String timeElapsed(long timePostedInMilliseconds) {
        long timeInSeconds = millisecondsToSeconds(currentTime() - timePostedInMilliseconds);
        if (timeInSeconds < MINUTE)
        {
            return "just now";
        }
        else if (timeInSeconds < HOUR) {
            return setPlurality(timeInSeconds/MINUTE,"min") + " ago";
        }
        else if (timeInSeconds < DAY) {
            return setPlurality(timeInSeconds/HOUR,"hr")+ " ago";
        }
        else if(timeInSeconds < WEEK) {
            return setPlurality(timeInSeconds/DAY,"day") + " ago";
        }
        else if (timeInSeconds < MONTH) {
           return setPlurality(timeInSeconds/WEEK,"week") + " ago";
        }
        else {
            return timeStampToDate(timePostedInMilliseconds);
        }
    }

    /**
     * Determines the sets the plurality of a word
     * @param value count
     * @param word distinction
     * @return return a string
     */
    private static  String setPlurality(long value, String word) {
        if(value == 1) {
            return "a " + word;
        }
        return value + " " + word + "s";
    }

    /**
     * Converts milliseconds to Month,day and time
     * @param timePostedInMilliseconds time to be converted
     * @return return date and time
     */
    private static String timeStampToDate(long timePostedInMilliseconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM d, ''yy");
        return simpleDateFormat.format(timePostedInMilliseconds);
    }

    /**
     * Gets the real-time time of day in milliseconds
     * @return return time in milliseconds
     */
    public static long currentTime() {
        Date date = new Date();
        return date.getTime();
    }
}
