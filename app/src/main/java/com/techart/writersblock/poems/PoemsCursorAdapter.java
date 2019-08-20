package com.techart.writersblock.poems;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.techart.writersblock.R;
import com.techart.writersblock.sqliteutils.WritersBlockContract;

/**
 * Created by Kelvin on 30/05/2017.
 */

public class PoemsCursorAdapter extends CursorAdapter {
    public PoemsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.item_filelist, parent, false
        );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String noteTitle = cursor.getString(
                cursor.getColumnIndex(WritersBlockContract.PoemEntry.POEM_TITLE));
        String timeCreated = cursor.getString(
                cursor.getColumnIndex(WritersBlockContract.PoemEntry.POEM_CREATED));
        String isPosted = cursor.getString(
                cursor.getColumnIndex(WritersBlockContract.PoemEntry.POEM_FIREBASEURL));


        TextView tv = view.findViewById(R.id.tvTitle);
        TextView tm = view.findViewById(R.id.tv_timeCreated);
        ImageView im = view.findViewById(R.id.ivFile);
        if (isPosted.length() > 5)
        {

            im.setImageResource(R.drawable.ic_file_blue);
        }
        else
        {
            im.setImageResource(R.drawable.ic_file_grey);
        }
        tv.setText(noteTitle);
        tm.setText(timeCreated);
    }
}
