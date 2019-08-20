package com.techart.writersblock.poems;

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

public class PoemEditorActivity extends AppCompatActivity {
    private ProgressDialog mProgress;
    private String action;
    private EditText editor;
    private EditText title;
    private String noteFilter;
    private String oldText;
    private String poemTitle;
    private String newText;
    private String newTitle;
    private String poemUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        editor = findViewById(R.id.editText);
        title = findViewById(R.id.editTitle);
        mProgress = new ProgressDialog(this);

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(WritersBlockContract.PoemEntry.CONTENT_ITEM_TYPE);
        //Checks if user is creating a new note or editing an existing one
        if (uri == null) {
            action = Intent.ACTION_INSERT;
            poemTitle = "New";
            poemUrl = "null";
            setTitle(poemTitle);
            title.setHint("Tap to write title");
            editor.setHint("Tap to write poem");
            title.requestFocus();
        } else {
            action = Intent.ACTION_EDIT;
            noteFilter = WritersBlockContract.PoemEntry.POEM_ID + "=" + uri.getLastPathSegment();
            Cursor cursor = getContentResolver().query(uri,
                    WritersBlockContract.PoemEntry.ALL_COLUMNS, noteFilter, null, null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(WritersBlockContract.PoemEntry.POEM_TEXT));
            poemTitle = cursor.getString(cursor.getColumnIndex(WritersBlockContract.PoemEntry.POEM_TITLE));
            poemUrl = cursor.getString(cursor.getColumnIndex(WritersBlockContract.PoemEntry.POEM_FIREBASEURL));
            setTitle("Editing " + poemTitle);
            editor.setText(oldText);
            title.setText(poemTitle);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       newText = editor.getText().toString().trim();
       newTitle = title.getText().toString().trim();
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
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
    
    private void determineAction() {
        switch (action)
        {
            case Intent.ACTION_INSERT: startPosting();
                break;
            case Intent.ACTION_EDIT: updatePoem();
        }
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

    private void deleteNote() {
        getContentResolver().delete(WritersBlockContract.PoemEntry.CONTENT_URI,
                noteFilter, null);
        Toast.makeText(this, getString(R.string.poem_deleted),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void finishEditing()
    {
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
                else if (oldText.equals(newText) && poemTitle.equals(newTitle)) {
                    setResult(RESULT_CANCELED);
                } else {
                    updateNote();
                }
        }
        finish();
    }

    private void updateNote() {
        ContentValues values = new ContentValues();
        values.put(WritersBlockContract.PoemEntry.POEM_TITLE, newTitle);
        values.put(WritersBlockContract.PoemEntry.POEM_TEXT, newText);
        values.put(WritersBlockContract.PoemEntry.POEM_FIREBASEURL, poemUrl);
        getContentResolver().update(WritersBlockContract.PoemEntry.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, getString(R.string.poem_updated), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote() {
        ContentValues values = new ContentValues();
        values.put(WritersBlockContract.PoemEntry.POEM_TITLE, newTitle);
        values.put(WritersBlockContract.PoemEntry.POEM_TEXT, newText);
        values.put(WritersBlockContract.PoemEntry.POEM_FIREBASEURL, poemUrl);
        getContentResolver().insert(WritersBlockContract.PoemEntry.CONTENT_URI, values);
        Toast.makeText(getApplicationContext(),poemTitle + " saved", Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
    }

    private void startPosting()
    {
        newText = editor.getText().toString().trim();
        newTitle = title.getText().toString().trim();
        if(validate()) {
            postPoem();
        }
    }

    private boolean validate(){
        return EditorUtils.isEmpty(this,newTitle,"poem title") &&
                EditorUtils.validateMainText(this,editor.getLineCount());
    }

    private void updatePoem() {
        mProgress.setMessage("Updating poem...");
        mProgress.show();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.POEM,newText);
        values.put(Constants.POEM_TITLE,newTitle.toUpperCase());
        FireBaseUtils.mDatabasePoems.child(poemUrl).updateChildren(values);
        mProgress.dismiss();
        Toast.makeText(getApplicationContext(),"Poem updates", Toast.LENGTH_LONG).show();
        finishEditing();
    }

    private void postPoem() {
        mProgress.setMessage("Posting poem...");
        mProgress.show();
        poemUrl = FireBaseUtils.mDatabasePoems.push().getKey();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.POEM_TITLE,newTitle.toUpperCase());
        values.put(Constants.POEM,newText);
        values.put(Constants.NUM_LIKES,0);
        values.put(Constants.NUM_COMMENTS,0);
        values.put(Constants.NUM_VIEWS,0);
        values.put(Constants.AUTHOR_URL,FireBaseUtils.getUiD());
        values.put(Constants.POST_AUTHOR,FireBaseUtils.getAuthor());
        values.put(Constants.TIME_CREATED,ServerValue.TIMESTAMP);
        FireBaseUtils.mDatabasePoems.child(poemUrl).setValue(values);
        mProgress.dismiss();
        Toast.makeText(getApplicationContext(),"Poem posted", Toast.LENGTH_LONG).show();
        finishEditing();
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
}
