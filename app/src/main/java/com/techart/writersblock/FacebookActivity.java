package com.techart.writersblock;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for editing Description of posted story
 * Uses Story URL to map each story during an update
 * Created by Kelvin on 30/07/2017.
 */

public class FacebookActivity extends AppCompatActivity {

    String newText;
    private EditText etDialogEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_storyedit);
        etDialogEditor = findViewById(R.id.et_dialog_editor);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvUpdate = findViewById(R.id.tv_update);
        TextView tvCancel = findViewById(R.id.tv_cancel);

        tvTitle.setText("Enter your Facebook page link");
        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication(),"Update Cancelled...!",Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void update() {
        newText = etDialogEditor.getText().toString().trim();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.FACEBOOK, newText);
        FireBaseUtils.mDatabaseUsers.child(FireBaseUtils.getUiD()).updateChildren(values);
        Toast.makeText(getApplication(),"Update Successful...!",Toast.LENGTH_LONG).show();
        testFacebook();
    }

    private void testFacebook(){
        DialogInterface.OnClickListener dialogClickListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int button) {
                    if (button == DialogInterface.BUTTON_POSITIVE) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(newText));
                        startActivity(i);
                    }
                    if (button == DialogInterface.BUTTON_NEGATIVE) {
                        dialog.dismiss();
                        finish();
                    }
                }
            };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to test if the link works?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .setCancelable(false)
                .show();
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
        builder.setMessage("Changes will not be saved...!")
                .setPositiveButton("Exit", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener)
                .show();
    }
}
