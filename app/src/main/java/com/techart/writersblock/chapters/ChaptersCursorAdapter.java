package com.techart.writersblock.chapters;

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
 * Cursor adapter for chapters
 * Created by Kelvin on 30/05/2017.
 */

class ChaptersCursorAdapter extends CursorAdapter {
    public ChaptersCursorAdapter(Context context, Cursor c, int flags) {
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
                cursor.getColumnIndex(WritersBlockContract.ChapterEntry.CHAPTER_TITLE));
        String timeCreated = cursor.getString(
                cursor.getColumnIndex(WritersBlockContract.ChapterEntry.CHAPTER_CREATED));
        String hasUrl = cursor.getString(
                cursor.getColumnIndex(WritersBlockContract.ChapterEntry.CHAPTER_URL));

        ImageView iv = view.findViewById(R.id.ivFile);

        TextView tv = view.findViewById(R.id.tvTitle);
        TextView tm = view.findViewById(R.id.tv_timeCreated);
        tv.setText(noteTitle);
        tm.setText(timeCreated);
        if (hasUrl.length() > 5)
        {

            iv.setImageResource(R.drawable.ic_file_blue);
        }
        else
        {
            iv.setImageResource(R.drawable.ic_file_grey);
        }
    }
}
