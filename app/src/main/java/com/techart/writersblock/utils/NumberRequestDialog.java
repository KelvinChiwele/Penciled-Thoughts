package com.techart.writersblock.utils;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.techart.writersblock.R;
import com.techart.writersblock.constants.FireBaseUtils;

/**
 * Activity for editing title of posted story
 * Uses Story URL to map each story during an update
 * Created by Kelvin on 30/07/2017.
 */

public class NumberRequestDialog extends AppCompatActivity {

    private EditText etDialogEditor;
    String newText;
    private static final String PHONE_NUMBER = "09[5-7][0-9]{7}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_number);
        etDialogEditor = findViewById(R.id.et_dialog_editor);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvUpdate = findViewById(R.id.tv_update);
        TextView tvCancel = findViewById(R.id.tv_cancel);
        tvTitle.setText("TO RECEIVE AIRTIME..." );

        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate() && FireBaseUtils.getUiD() != null){
                    update();
                }
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication(),"Cancelled...!",Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void update() {
        FireBaseUtils.mDatabaseNumber.child(FireBaseUtils.getUiD()).setValue(newText);
        Toast.makeText(getApplication(),"Number sent",Toast.LENGTH_LONG).show();
        finish();
    }

    private boolean validate() {
         newText = etDialogEditor.getText().toString().trim();

        if (newText.isEmpty()) {
            etDialogEditor.setError("enter a valid phone number");
            return false;
        } else if (!newText.matches(PHONE_NUMBER)){
            etDialogEditor.setError("check the digits");
            return false;
        } else {
            etDialogEditor.setError(null);
            return true;
        }
    }

}
