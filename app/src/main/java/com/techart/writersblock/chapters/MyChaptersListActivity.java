package com.techart.writersblock.chapters;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.techart.writersblock.R;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.sqliteutils.WritersBlockContract;

public class MyChaptersListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>
{

    private ChaptersCursorAdapter cursorAdapter;
    private String chapterFilter;
    private Intent intent;

    private static final int EDITOR_REQUEST_CODE = 1001;
    private String uriLastPathSegmentSegment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_listview);

        setTitle("Chapters");

        intent = getIntent();

        Uri uri = intent.getParcelableExtra(WritersBlockContract.StoryEntry.CONTENT_ITEM_TYPE);
        if (uri == null){
            Toast.makeText(this,"kindly reload the chapter",Toast.LENGTH_LONG).show();
            finish();
        }
        uriLastPathSegmentSegment = uri.getLastPathSegment();

        chapterFilter = WritersBlockContract.ChapterEntry.CHAPTER_STORY_ID + "=" + uriLastPathSegmentSegment;
        cursorAdapter = new ChaptersCursorAdapter(this, null, 0);

        ListView list = findViewById(R.id.lvItems);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MyChaptersListActivity.this, ChapterEditorActivity.class);
                Uri uri = Uri.parse(WritersBlockContract.ChapterEntry.CONTENT_URI + "/" + id);
                intent.putExtra(WritersBlockContract.ChapterEntry.CONTENT_ITEM_TYPE, uri);
                intent.putExtra("id", id);
                startActivityForResult(intent,EDITOR_REQUEST_CODE);
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_list,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                String storyUrl = intent.getStringExtra(Constants.STORY_REFID);
                String storyId = intent.getStringExtra("id");
                Intent intent = new Intent(MyChaptersListActivity.this, AddChapterActivity.class);
                intent.putExtra("id", storyId);
                intent.putExtra(Constants.STORY_REFID, storyUrl);
                startActivityForResult(intent,EDITOR_REQUEST_CODE);
                break;
            case R.id.action_delete_all:
                deleteDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteStory() {
        String storyFilter = WritersBlockContract.StoryEntry.STORY_ID + "=" + uriLastPathSegmentSegment;
        getContentResolver().delete(WritersBlockContract.StoryEntry.CONTENT_URI,
                storyFilter, null);
        Toast.makeText(this, getString(R.string.story_deleted),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }


    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Deleting all will delete the story?");
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteStory();
                dialog.dismiss();
            }
        })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, WritersBlockContract.ChapterEntry.CONTENT_URI,
                null, chapterFilter, null, null);
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