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

public class AddChapterOnlineActivity extends AppCompatActivity {

    private ProgressDialog mProgress;
    private EditText editor;

    private String newText;

    private String storyUrl;
    private String chapters;
    private long chaptersNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_story);
        editor = findViewById(R.id.editText);
        mProgress = new ProgressDialog(this);

        Intent intent = getIntent();
        storyUrl = intent.getStringExtra(Constants.STORY_REFID);
        chapters = intent.getStringExtra(Constants.STORY_CHAPTERCOUNT);
        chaptersNum = Long.parseLong(chapters);
        chaptersNum++;
        setTitle("Adding Episode " + chaptersNum);
        editor.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_post) {
            startPosting();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startPosting() {
        newText = editor.getText().toString().trim();
        if (validate())  {
            postStoryChapter();
        }
    }

    private boolean validate()
    {
        return storyUrl != null && EditorUtils.validateMainText(this,editor.getLayout().getLineCount());
    }

    private void postStoryChapter() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Posting ...");
        mProgress.show();
        chapters = String.valueOf(chaptersNum);
        String  chapterUrl = FireBaseUtils.mDatabaseChapters.child(storyUrl).push().getKey();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.CHAPTER_CONTENT,newText);
        values.put(Constants.CHAPTER_TITLE,chapters);
        FireBaseUtils.mDatabaseChapters.child(storyUrl).child(chapterUrl).setValue(values);
        mProgress.dismiss();
        Toast.makeText(getApplicationContext(),"Chapter Added", Toast.LENGTH_LONG).show();
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
