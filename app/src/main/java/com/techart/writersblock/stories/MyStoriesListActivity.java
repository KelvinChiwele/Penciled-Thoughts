package com.techart.writersblock.stories;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.techart.writersblock.R;
import com.techart.writersblock.chapters.MyChaptersListActivity;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.sqliteutils.WritersBlockContract;

public class MyStoriesListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private String filter;
    private StoryCursorAdapter cursorAdapter;
    private static final int EDITOR_REQUEST_CODE = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_listview);
        setTitle("Stories");
        cursorAdapter = new StoryCursorAdapter(this, null, 0);

        ListView list = findViewById(R.id.lvItems);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri  uri= Uri.parse(WritersBlockContract.StoryEntry.CONTENT_URI + "/" +id);
                Intent intent = new Intent(MyStoriesListActivity.this, MyChaptersListActivity.class);
                intent.putExtra(WritersBlockContract.StoryEntry.CONTENT_ITEM_TYPE, uri);

                filter = WritersBlockContract.StoryEntry.STORY_ID + "=" + id;
                Cursor cursor = getContentResolver().query(WritersBlockContract.StoryEntry.CONTENT_URI,
                        WritersBlockContract.StoryEntry.STORY_COLUMNS, filter, null, null);
                cursor.moveToFirst();
                filter = cursor.getString(cursor.getColumnIndex(WritersBlockContract.StoryEntry.STORY_ID));
                String storyUrl = cursor.getString(cursor.getColumnIndex(WritersBlockContract.StoryEntry.STORY_REFID));
                intent.putExtra("id",filter);
                intent.putExtra(Constants.STORY_REFID,storyUrl);
                startActivityForResult(intent,EDITOR_REQUEST_CODE);
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_storylist,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            Intent editorStoryIntent = new Intent(MyStoriesListActivity.this, StoryPrologueActivity.class);
            startActivity(editorStoryIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, WritersBlockContract.StoryEntry.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}

