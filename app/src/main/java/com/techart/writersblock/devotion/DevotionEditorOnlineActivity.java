package com.techart.writersblock.devotion;

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

/**
 * Handles updates to exiting devotions using the postUrl
 */
public class DevotionEditorOnlineActivity extends AppCompatActivity {

    private ProgressDialog mProgress;

    private EditText editor;
    private EditText title;

    private String newText;
    private String newTitle;

    private String devotionUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        editor = findViewById(R.id.editText);
        title = findViewById(R.id.editTitle);
        mProgress = new ProgressDialog(this);

        Intent intent = getIntent();
        devotionUrl = intent.getStringExtra(Constants.POST_KEY);
        String oldText = intent.getStringExtra(Constants.DEVOTION);
        String oldTitle = intent.getStringExtra(Constants.DEVOTION_TITLE);

        title.setText(oldTitle);
        editor.setText(oldText);
        title.requestFocus();
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
                startUpdating();
                break;
            case R.id.action_delete:
                clearComponents();
                break;
        }
        return super.onOptionsItemSelected(item);
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
        finish();
    }


    /**
     * Clears EditTexts
     */
    private void clearComponents() {
        title.setText("");
        editor.setText("");
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
