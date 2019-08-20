package com.techart.writersblock.devotion;

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
import com.techart.writersblock.sqliteutils.WritersBlockContract;

public class MySpiritualsListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>
{
    private DevotionCursorAdapter cursorAdapter;
    private static final int EDITOR_REQUEST_CODE = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_listview);
        setTitle("List of devotions");
        cursorAdapter = new DevotionCursorAdapter(this, null, 0);
        ListView list = findViewById(R.id.lvItems);
        list.setAdapter(cursorAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editorIntent = new Intent(MySpiritualsListActivity.this, DevotionEditorActivity.class);
                Uri uri = Uri.parse(WritersBlockContract.SpiritualEntry.CONTENT_URI + "/" + id);
                editorIntent.putExtra(WritersBlockContract.SpiritualEntry.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(editorIntent,EDITOR_REQUEST_CODE);
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_storylist,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent editorIntent = new Intent(MySpiritualsListActivity.this, DevotionEditorActivity.class);
            startActivityForResult(editorIntent, EDITOR_REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, WritersBlockContract.SpiritualEntry.CONTENT_URI,
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

