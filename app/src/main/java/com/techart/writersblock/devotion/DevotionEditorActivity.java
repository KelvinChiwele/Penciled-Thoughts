package com.techart.writersblock.devotion;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ServerValue;
import com.techart.writersblock.R;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.sqliteutils.WritersBlockContract;
import com.techart.writersblock.utils.EditorUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles devotion user actions as writing and posting
 */
public class DevotionEditorActivity extends AppCompatActivity {

    private ProgressDialog mProgress;

    private String action;
    private EditText editor;
    private EditText title;
    private String noteFilter;
    private String oldText;
    private String oldTitle;

    private String newText;
    private String newTitle;

    private String devotionUrl;

    private Boolean isPostEdited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        editor = findViewById(R.id.editText);
        title = findViewById(R.id.editTitle);
        mProgress = new ProgressDialog(this);

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(WritersBlockContract.SpiritualEntry.CONTENT_ITEM_TYPE);
        //Checks if user is creating a new note or editing an existing one
        if (uri == null) {
            isPostEdited = false;
            devotionUrl = "null";
            oldTitle = "New";
            isPostEdited = false;
            action = Intent.ACTION_INSERT;
            setTitle(oldTitle);
            title.setHint("Tap to write devotion title");
            editor.setHint("Tap to write devotion");
            title.requestFocus();
        } else {
            action = Intent.ACTION_EDIT;
            noteFilter = WritersBlockContract.SpiritualEntry.SPIRITUAL_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri,
                    WritersBlockContract.SpiritualEntry.ALL_COLUMNS, noteFilter, null, null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(WritersBlockContract.SpiritualEntry.SPIRITUAL_TEXT));
            oldTitle = cursor.getString(cursor.getColumnIndex(WritersBlockContract.SpiritualEntry.SPIRITUAL_TITLE));
            devotionUrl = cursor.getString(cursor.getColumnIndex(WritersBlockContract.SpiritualEntry.SPIRITUAL_FIREBASE_URL));

            setTitle("Editing " + oldTitle);
            editor.setText(oldText);
            title.setText(oldTitle);
            title.requestFocus();
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * Handles action bar item clicks
     * @param item item that has been clicked
     * @return true if it was handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       newText = editor.getText().toString().trim();
       newTitle = title.getText().toString().trim();
        int id = item.getItemId();
        switch (id) {
            case R.id.action_post:
                determineAction();
                break;
            case R.id.action_delete:
                determineDelete();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void determineAction()
    {
        switch (action)
        {
            case Intent.ACTION_INSERT: startPosting();
                break;
            case Intent.ACTION_EDIT: startUpdating();
                break;
        }
    }

    private void updatePoem() {
        mProgress.setMessage("Updating devotion...");
        mProgress.show();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.DEVOTION,newText);
        values.put(Constants.DEVOTION_TITLE,newTitle);
        FireBaseUtils.mDatabaseDevotions.child(devotionUrl).updateChildren(values);
        mProgress.dismiss();
        Toast.makeText(getApplicationContext(),"Devotion Updated", Toast.LENGTH_LONG).show();
        finishEditing();
    }

    /**
     * Determines which delete action to do
     */
    private void determineDelete() {
        switch (action)
        {
            case Intent.ACTION_INSERT: clearComponents();
                break;
            case Intent.ACTION_EDIT:deleteNote();
                break;
        }
    }

    /**
     * Clears EditTexts
     */
    private void clearComponents() {
        title.setText("");
        editor.setText("");
    }

    /**
     * Deletes Devotion from Database
     */
    private void deleteNote() {
        getContentResolver().delete(WritersBlockContract.SpiritualEntry.CONTENT_URI,
                noteFilter, null);
        Toast.makeText(this, getString(R.string.devotion_deleted),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void finishEditing() {
       newText = editor.getText().toString().trim();
       newTitle = title.getText().toString().trim();
        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0) {
                    setResult(RESULT_CANCELED);
                } else {
                    insertNote();
                }
                break;
            case Intent.ACTION_EDIT:
                if (newText.length() == 0) {
                    deleteNote();
                }
                else if (oldText.equals(newText) && oldTitle.equals(newTitle)) {
                    setResult(RESULT_CANCELED);
                } else {
                    updateNote();
                }
        }
        finish();
    }

    private void updateNote() {
        ContentValues values = new ContentValues();
        values.put(WritersBlockContract.SpiritualEntry.SPIRITUAL_TITLE, newTitle);
        values.put(WritersBlockContract.SpiritualEntry.SPIRITUAL_TEXT, newText);
        values.put(WritersBlockContract.SpiritualEntry.SPIRITUAL_FIREBASE_URL, devotionUrl);
        getContentResolver().update(WritersBlockContract.SpiritualEntry.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote() {
        ContentValues values = new ContentValues();
        values.put(WritersBlockContract.SpiritualEntry.SPIRITUAL_TITLE, newTitle);
        values.put(WritersBlockContract.SpiritualEntry.SPIRITUAL_TEXT, newText);
        values.put(WritersBlockContract.SpiritualEntry.SPIRITUAL_FIREBASE_URL, devotionUrl);
        getContentResolver().insert(WritersBlockContract.SpiritualEntry.CONTENT_URI, values);
        Toast.makeText(getApplicationContext(), oldTitle + " saved", Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
    }

    private void startPosting() {
        newText = editor.getText().toString().trim();
        newTitle = title.getText().toString().trim();
        if(validate()) {
            postPoem();
        }
    }

    private boolean validate(){
        return EditorUtils.isEmpty(this,newTitle,"devotion title") &&
                EditorUtils.validateMainText(this,editor.getLineCount());
    }

    private void startUpdating() {
        newText = editor.getText().toString().trim();
        newTitle = title.getText().toString().trim();
        if(validate()) {
            updatePoem();
        }
    }

    private void postPoem() {
        mProgress.setMessage("Posting devotion...");
        mProgress.show();
        devotionUrl = FireBaseUtils.mDatabaseDevotions.push().getKey();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.DEVOTION_TITLE,newTitle.toUpperCase());
        values.put(Constants.DEVOTION,newText);
        values.put(Constants.IS_EDITED,isPostEdited);
        values.put(Constants.NUM_LIKES,0);
        values.put(Constants.NUM_COMMENTS,0);
        values.put(Constants.NUM_VIEWS,0);
        values.put(Constants.AUTHOR_URL,FireBaseUtils.getUiD());
        values.put(Constants.POST_AUTHOR,FireBaseUtils.getAuthor());
        values.put(Constants.TIME_CREATED,ServerValue.TIMESTAMP);
        FireBaseUtils.mDatabaseDevotions.child(devotionUrl).setValue(values);
        mProgress.dismiss();
        Toast.makeText(getApplicationContext(),"Devotion posted", Toast.LENGTH_LONG).show();
        finishEditing();
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
}
