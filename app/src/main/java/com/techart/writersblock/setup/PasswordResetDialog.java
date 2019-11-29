package com.techart.writersblock.setup;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.techart.writersblock.R;

/**
 * Activity for editing title of posted story
 * Uses Story URL to map each story during an update
 * Created by Kelvin on 30/07/2017.
 */

public class PasswordResetDialog extends AppCompatActivity {

    private EditText etDialogEditor;
    String newText;
    private boolean isAttached;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_password_reset);
        etDialogEditor = findViewById(R.id.et_dialog_editor);
        TextView tvUpdate = findViewById(R.id.tv_update);
        TextView tvCancel = findViewById(R.id.tv_cancel);

        String email = getIntent().getStringExtra("Email");
        etDialogEditor.setText(email);

        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newText = etDialogEditor.getText().toString().trim();
                if (newText.isEmpty()){
                    etDialogEditor.setError("email address missing");
                } else {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(newText)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    onSent();
                                }
                            }
                        });
                }
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
    }



    public void onSent() {
        if (isAttached) {
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
            builder.setTitle("Sent to " + newText)
                    .setMessage("Go to mail to reset password")
                    .setPositiveButton("OK", dialogClickListener)
                    .setNegativeButton("Cancel", dialogClickListener)
                    .show();
        }
    }
}
