package com.techart.writersblock.stories;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.techart.writersblock.R;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.utils.EditorUtils;

/**
 * Presents a provision for adding Story Title, Category and Description
 * Invokes StoryEditorActivity
 */
public class StoryPrologueActivity extends AppCompatActivity {

    private Spinner spCategory;

    private String category;

    private EditText etStoryTitle;
    private EditText etStoryDescription;

    private String title;
    private String description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_storyoverview);
        setTitle(Constants.CREATE_STORY);

        spCategory = findViewById(R.id.categories);

        ArrayAdapter<CharSequence> pagesAdapter =  ArrayAdapter.createFromResource(StoryPrologueActivity.this, R.array.categories, R.layout.spinnertxt);
        pagesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pagesAdapter.notifyDataSetChanged();
        spCategory.setAdapter(pagesAdapter);
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = spCategory.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        etStoryTitle = findViewById(R.id.editStoryTitle);
        etStoryDescription = findViewById(R.id.editStoryDescription);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_nextbutton,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        setValues();
        if (id == R.id.action_next) {
            startWriting();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validate() {
        TextView tvCategoryError = (TextView) spCategory.getSelectedView();
        return EditorUtils.dropDownValidator(category,getResources().getString(R.string.default_category), tvCategoryError) &&
             EditorUtils.editTextValidator(title,etStoryTitle,"Kindly set story title");
    }

    private void startWriting() {
        if (validate()){
            Intent intent = new Intent(StoryPrologueActivity.this, StoryEditorActivity.class);
            intent.putExtra("Category",category);
            intent.putExtra("Title",title);
            intent.putExtra("Description", description);
            startActivity(intent);
        }

    }

    private void setValues() {
        title = etStoryTitle.getText().toString().trim();
        description = etStoryDescription.getText().toString().trim();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Create chapter to save changes");
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
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
