package com.techart.writersblock.devotion;

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

public class DevotionCursorAdapter extends CursorAdapter {
    public DevotionCursorAdapter(Context context, Cursor c, int flags) {
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
                cursor.getColumnIndex(WritersBlockContract.SpiritualEntry.SPIRITUAL_TITLE));
        String timeCreated = cursor.getString(
                cursor.getColumnIndex(WritersBlockContract.SpiritualEntry.SPIRITUAL_CREATED));
        String isPosted = cursor.getString(
                cursor.getColumnIndex(WritersBlockContract.SpiritualEntry.SPIRITUAL_FIREBASE_URL));

        TextView title = view.findViewById(R.id.tvTitle);
        title.setText(noteTitle);
        TextView time = view.findViewById(R.id.tv_timeCreated);
        time.setText(timeCreated);
        ImageView iv = view.findViewById(R.id.ivFile);

        if (isPosted.length() > 5)   {
            iv.setImageResource(R.drawable.ic_file_blue);
        } else {
            iv.setImageResource(R.drawable.ic_file_grey);
        }

    }
}
