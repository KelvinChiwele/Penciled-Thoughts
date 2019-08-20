package com.techart.writersblock.stories;

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
 * Maps columns from the Cursor to the widgets on views
 * View icon is blue if story is not posted.
 * Created by Kelvin on 30/05/2017.
 */

public class StoryCursorAdapter extends CursorAdapter {
    public StoryCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.item_doclist, parent, false
        );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String storyTitle = cursor.getString(
                cursor.getColumnIndex(WritersBlockContract.StoryEntry.STORY_TITLE));

        String timeCreated = cursor.getString(
                cursor.getColumnIndex(WritersBlockContract.StoryEntry.STORY_CREATED));

        String hasUrl = cursor.getString(
                cursor.getColumnIndex(WritersBlockContract.StoryEntry.STORY_REFID));

        TextView tvStoryTitle = view.findViewById(R.id.tvTitle);
        tvStoryTitle.setText(storyTitle);

        TextView time = view.findViewById(R.id.tv_timeCreated);
        time.setText(timeCreated);
        ImageView im = view.findViewById(R.id.ivFile);

        if (hasUrl.length() > 5) {
             im.setImageResource(R.drawable.ic_book_blue);
        }
        else {
             im.setImageResource(R.drawable.ic_book_grey);
        }
    }
}
