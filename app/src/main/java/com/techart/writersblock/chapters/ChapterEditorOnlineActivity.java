package com.techart.writersblock.chapters;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.techart.writersblock.R;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.utils.EditorUtils;

import java.util.HashMap;
import java.util.Map;

public class ChapterEditorOnlineActivity extends AppCompatActivity {
    private String chapterUrl;
    private EditText editor;
    private String storyUrl;
    private String newText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        editor = findViewById(R.id.editText);

        Intent intent = getIntent();
        storyUrl = intent.getStringExtra(Constants.STORY_REFID);
        chapterUrl = intent.getStringExtra(Constants.POST_KEY);
        String oldText = intent.getStringExtra(Constants.CHAPTER_CONTENT);
        String oldTitle = intent.getStringExtra(Constants.CHAPTER_TITLE);
        setTitle("Editing Episode " + oldTitle);
        editor.setText(oldText);
        editor.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_onlineeditor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_update) {
            startPosting();
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean validate() {
        return EditorUtils.validateMainText(this,editor.getLayout().getLineCount());
    }

    /*
        if() checks if the story was posted, if not queries for the url from the story and then posts chapter
        else if () checks if the story & chapter were posted & then updates the chapter
        else posts the chapter
     */
    private void startPosting() {
        newText = editor.getText().toString().trim();
        if (validate() && storyUrl != null && chapterUrl != null){
            postChapter();
        }
    }

    private void postChapter() {
        ProgressDialog mProgress = new ProgressDialog(this);
        mProgress.setMessage("Posting ...");
        mProgress.show();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.CHAPTER_CONTENT,newText);
        FireBaseUtils.mDatabaseChapters.child(storyUrl).child(chapterUrl).updateChildren(values);
        mProgress.dismiss();
        Toast.makeText(getApplicationContext(),"Chapter successfully posted", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int button) {
                if (button == DialogInterface.BUTTON_POSITIVE) {
                    finish();
                }
                if (button == DialogInterface.BUTTON_NEGATIVE) {
                    dialog.dismiss();
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CHANGES WILL NOT BE SAVED!")
        .setPositiveButton("Understood", dialogClickListener)
        .setNegativeButton("Stay in editor", dialogClickListener)
        .show();
    }
}
