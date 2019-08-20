package com.techart.writersblock.utils;

import android.content.Context;
import android.graphics.Color;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * Validates entries on UI components
 * Created by Kelvin on 17/09/2017.
 */

public final class EditorUtils {

    private EditorUtils() {
    }


    /**
     * Inspects if the line count is not less than 10
     * @param context The context of the invoking method
     * @param layOutLineCount The number of lines written in a particular editText component
     * @return true if it confines to the condition
     */
    public static boolean validateMainText(Context context, int layOutLineCount) {
        int lineCount = 8;
        if (layOutLineCount <= lineCount) {
            Toast.makeText(context, "Text too short, at least " + lineCount + " lines", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public static boolean isEmpty(Context context, String title, String placeHolder)
    {
        if (title.isEmpty())
        {
            Toast.makeText(context,"Type in "+ placeHolder,Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public static boolean dropDownValidator(@NonNull String stringValue, String defaultValue, @NonNull TextView textView) {

        if (stringValue.equals(defaultValue)){
            textView.setTextColor(Color.RED);
            textView.setError("Kindly select category");
            return false;
        } else {
            textView.setError(null);
            return true;
        }
    }

    public static boolean editTextValidator(@NonNull String stringValue, @NonNull EditText textView, String message) {
        if (stringValue.isEmpty()){
            textView.setError(message);
            return false;
        } else {
            textView.setError(null);
            return true;
        }
    }

    public static boolean dropDownValidator(Context context, String signUpAs){
        if (signUpAs == null) {
            Toast.makeText(context, "Select either Writer or Reader", Toast.LENGTH_LONG).show();
            return false;
        } else  {
            return true;
        }
    }

    public static boolean doPassWordsMatch(String first, String second , @NonNull EditText textView)  {
        if (first.equals(second)) {
            textView.setError(null);
            return true;
        } else {
            textView.setError("Ensure that passwords match");
            return false;
        }
    }

    public static boolean isEmailValid(String email, @NonNull EditText textView) {
        //CharSequence target = email;
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textView.setError(null);
            return true;
        } else {
            textView.setError("Email address not valid");
            return false;
        }
    }
}
