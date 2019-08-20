package com.techart.writersblock.chapters;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.techart.writersblock.R;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Library;
import com.techart.writersblock.sqliteutils.WritersBlockContract;
import com.techart.writersblock.utils.EditorUtils;

import java.util.HashMap;
import java.util.Map;

public class AddChapterActivity extends AppCompatActivity {

    private ProgressDialog mProgress;
    private EditText editor;
    private EditText title;
    private String chapterUrl = "null";

    private String newText;
    private String newTitle;

    private String storyId;
    private String storyUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_storyeditor);
        editor = findViewById(R.id.editText);
        title = findViewById(R.id.editTitle);
        mProgress = new ProgressDialog(this);

        Intent intent = getIntent();
        storyId = intent.getStringExtra("id");
        storyUrl = intent.getStringExtra(Constants.STORY_REFID);
        if (storyId != null) {
            setTitle("Adding " + Constants.CHAPTER);
            title.requestFocus();
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
        if (id == R.id.action_post) {
            if (storyUrl != "null" && !storyUrl.equals("null"))
            {
                startPosting();
            } else  {
                postDialog();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void postDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Kindly post the first chapter before adding this chapter");
        builder.setPositiveButton("POST", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
            }
        })
        .setNegativeButton("EXIT EDITOR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finishEditing();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void finishEditing() {
       newText = editor.getText().toString().trim();
       newTitle = title.getText().toString().trim();
        if (newText.length() == 0) {
            setResult(RESULT_CANCELED);
        } else
        {
            insertChapter();
        }
        finish();
    }

    private void insertChapter()
    {
        ContentValues values = new ContentValues();
        values.put(WritersBlockContract.ChapterEntry.CHAPTER_STORY_ID, storyId);
        values.put(WritersBlockContract.ChapterEntry.CHAPTER_TITLE, newTitle);
        values.put(WritersBlockContract.ChapterEntry.CHAPTER_CONTENT, newText);
        values.put(WritersBlockContract.ChapterEntry.CHAPTER_URL, chapterUrl);
        getContentResolver().insert(WritersBlockContract.ChapterEntry.CONTENT_URI, values);
        Toast.makeText(this, getString(R.string.chapter_posted), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }
    private void startPosting() {
        newText = editor.getText().toString().trim();
        newTitle = title.getText().toString().trim();
        if (EditorUtils.isEmpty(this,newTitle, "chapter title") && EditorUtils.validateMainText(this,editor.getLayout().getLineCount()))
        {
            postStoryChapter();
        }
        else
        {
            showErrorDialog();
        }
    }

    private void showErrorDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Error...! You have not written anything");
        builder.setPositiveButton("Stay in editor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        })
                .setNegativeButton("Exit editor", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void postStoryChapter()
    {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Posting ...");
        mProgress.show();
        DatabaseReference mDatabaseChapters = FireBaseUtils.mDatabaseChapters.child(storyUrl);
        chapterUrl = mDatabaseChapters.push().getKey();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.CHAPTER_CONTENT,newText);
        values.put(Constants.CHAPTER_TITLE,newTitle);
        mDatabaseChapters.child(chapterUrl).setValue(values);
        updateLibrary(storyUrl);
        mProgress.dismiss();
        Toast.makeText(getApplicationContext(),"Chapter Added", Toast.LENGTH_LONG).show();
        finishEditing();
    }

    private void updateLibrary(String storyUrl) {
        FireBaseUtils.mDatabaseLibrary.child(FireBaseUtils.getUiD()).child(storyUrl).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Library library = mutableData.getValue(Library.class);
                if (library == null) {
                    return Transaction.success(mutableData);
                }
                library.setChaptersAdded(library.getChaptersAdded() + 1 );
                // Set value and report transaction success
                mutableData.setValue(library);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
}
