package com.techart.writersblock.stories;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.techart.writersblock.R;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.sqliteutils.WritersBlockContract;
import com.techart.writersblock.utils.EditorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.techart.writersblock.constants.Constants.AUTHORITY;

/**
 * Provides the ability to write a Story, Store in a local db and/or post
 * to Server. Only called once per Story.
 * Invoked by StoryPrologueActivity
 */
public class StoryEditorActivity extends AppCompatActivity {

    private ProgressDialog mProgress;

    private String chapterUrl = "null";

    private EditText chapterTitle;
    private EditText editor;
    private String action;
    private String storyTitle;
    private String storyCategory;
    private String storyDescription;
    private String storyUrl ="null";

    private String newText;
    private String newTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storyeditor);
        chapterTitle = findViewById(R.id.editTitle);
        editor = findViewById(R.id.editText);
        storyTitle = getIntent().getStringExtra("Title");
        storyDescription = getIntent().getStringExtra("Description");
        storyCategory = getIntent().getStringExtra("Category");
        action = Intent.ACTION_INSERT;
        setTitle("First Chapter");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        newText = editor.getText().toString().trim();
        newTitle = chapterTitle.getText().toString().trim();
        int id = item.getItemId();
        switch (id) {
            case R.id.action_post:
                if ( EditorUtils.isEmpty(this,newTitle, "chapter") && EditorUtils.validateMainText(this,editor.getLayout().getLineCount())) {
                    postStory();
                }
                break;
            case R.id.action_delete:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void finishEditing() {
        newText = editor.getText().toString().trim();
        newTitle = chapterTitle.getText().toString().trim();
        switch (action) {
            case Intent.ACTION_INSERT:
                insertNewStory();
                break;
            case Intent.ACTION_EDIT:
                break;
        }
        setResult(RESULT_OK);
        finish();
    }

    private void insertNewStory()
    {
        ArrayList<ContentProviderOperation> newStory= new ArrayList<>();
        newStory.add(ContentProviderOperation.
                newInsert(WritersBlockContract.StoryEntry.CONTENT_URI)
                .withValue(WritersBlockContract.StoryEntry.STORY_REFID,storyUrl)
                .withValue(WritersBlockContract.StoryEntry.STORY_TITLE,storyTitle)
                .withValue(WritersBlockContract.StoryEntry.STORY_CATEGORY,storyCategory)
                .withValue(WritersBlockContract.StoryEntry.STORY_DESCRIPTION,storyDescription)
                .build());
        newStory.add(ContentProviderOperation.
                newInsert(WritersBlockContract.ChapterEntry.CONTENT_URI)
                .withValueBackReference(WritersBlockContract.ChapterEntry.CHAPTER_STORY_ID,0)
                .withValue(WritersBlockContract.ChapterEntry.CHAPTER_TITLE,newTitle)
                .withValue(WritersBlockContract.ChapterEntry.CHAPTER_CONTENT,newText)
                .withValue(WritersBlockContract.ChapterEntry.CHAPTER_URL,chapterUrl)
                .withValue(WritersBlockContract.ChapterEntry.CHAPTER_FIREBASE_STORY_URL,storyUrl)
                .build()
        );
        try {
            getContentResolver().
                    applyBatch(AUTHORITY,newStory);
            Toast.makeText(getApplicationContext(),"Saved", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
        } catch (RemoteException e) {
            setResult(RESULT_CANCELED);
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            setResult(RESULT_CANCELED);
            e.printStackTrace();
        }
    }


    private void postStory() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Posting ...");
        mProgress.show();
        DatabaseReference newPost = FireBaseUtils.mDatabaseStory.push();
        storyUrl = newPost.getKey();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.STORY_TITLE,storyTitle.toUpperCase());
        values.put(Constants.STORY_DESCRIPTION,storyDescription);
        values.put(Constants.STORY_CATEGORY,storyCategory);
        String status = "Ongoing";
        values.put(Constants.STORY_STATUS, status);
        values.put(Constants.STORY_CHAPTERCOUNT,0);
        values.put(Constants.NUM_LIKES,0);
        values.put(Constants.NUM_COMMENTS,0);
        values.put(Constants.NUM_VIEWS,0);
        values.put(Constants.AUTHOR_URL, FireBaseUtils.getUiD());
        values.put(Constants.POST_AUTHOR,FireBaseUtils.getAuthor());
        values.put(Constants.TIME_CREATED,ServerValue.TIMESTAMP);
        FireBaseUtils.mDatabaseStory.child(storyUrl).setValue(values);
        postStoryChapter();
        Toast.makeText(getApplicationContext(),"Story successfully posted", Toast.LENGTH_LONG).show();
    }

    private void postStoryChapter() {
        chapterUrl = FireBaseUtils.mDatabaseChapters.child(storyUrl).push().getKey();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.CHAPTER_TITLE,newTitle);
        values.put(Constants.CHAPTER_CONTENT,newText);
        FireBaseUtils.mDatabaseChapters.child(storyUrl).child(chapterUrl).setValue(values);
        mProgress.dismiss();
        finishEditing();
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
}
