package com.techart.writersblock.setup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ServerValue;
import com.techart.writersblock.MainActivity;
import com.techart.writersblock.R;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Profile;
import com.techart.writersblock.utils.EditorUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles registration process
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername;
    private EditText etLogin;
    private EditText etPassword;
    private EditText etRepeatedPassword;
    private String firstPassword;
    private String name;
    private String email;
    private ProgressDialog mProgress;
    private String signingInAs;
    private SharedPreferences mPref;
    private ImageView ivLogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etUsername = findViewById(R.id.et_username);
        etLogin = findViewById(R.id.et_login);
        etPassword = findViewById(R.id.et_password);
        etRepeatedPassword = findViewById(R.id.et_repeatPassword);
        ivLogo = findViewById(R.id.iv_logo);
        Button btRegister = findViewById(R.id.bt_register);
        btRegister.setClickable(true);

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (haveNetworkConnection()) {
                    if (validateCredentials()) {
                        startRegister();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this,"Ensure that your internet is working",Toast.LENGTH_LONG ).show();
                }
            }
        });

        Glide.with(RegisterActivity.this)
                .load(R.drawable.logo)
                .into(ivLogo);
    }

    /**
     * Handles radio button clicks
     * @param view sends the radio button view
     */
    public void onRadioButtonClicked(View view) {
        ((RadioButton) view).setChecked(((RadioButton) view).isChecked());
        signingInAs = ((RadioButton) view).getText().toString();
    }

    /**
     * implementation of the registration
     */
    private void startRegister() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Signing Up  ...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,firstPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mPref = getSharedPreferences(String.format("%s",getString(R.string.app_name)),MODE_PRIVATE);
                        Map<String,Object> values = new HashMap<>();
                        values.put(Constants.USER_NAME,name);
                        values.put(Constants.IMAGE_URL,getString(R.string.default_key));
                        values.put(Constants.SIGNED_IN_AS,signingInAs);
                        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);

                    FireBaseUtils.mDatabaseUsers.child(FireBaseUtils.getUiD()).setValue(values);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    if (user != null) {
                        mProgress.dismiss();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            Profile profile = Profile.getInstance();
                                            profile.setSignedAs(signingInAs);
                                            SharedPreferences.Editor editor = mPref.edit();
                                            editor.putString("user",name);
                                            editor.apply();
                                            Toast.makeText(RegisterActivity.this, "User profile updated.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    } else {
                        mProgress.dismiss();
                        Toast.makeText(RegisterActivity.this, "Error encountered, Please try again later", Toast.LENGTH_LONG).show();
                    }
                } else {
                    mProgress.dismiss();
                    if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(RegisterActivity.this,"User already exits, use another email address",Toast.LENGTH_LONG ).show();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this,"Error encountered, Please try again later",Toast.LENGTH_LONG ).show();
                    }
                }
            }
        });
    }

    private boolean haveNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
            return netWorkInfo != null && netWorkInfo.getState() == NetworkInfo.State.CONNECTED;
        }
        return false;
    }

    /**
     * Validates the entries
     * @return true if they all true
     */
    private boolean validateCredentials()
    {
        firstPassword =  etPassword.getText().toString().trim();
        String repeatedPassword = etRepeatedPassword.getText().toString().trim();
        name =  etUsername.getText().toString().trim();
        email = etLogin.getText().toString().trim();
        return  EditorUtils.dropDownValidator(getApplicationContext(), signingInAs) &&
                EditorUtils.editTextValidator(name,etUsername,"enter a valid username") &&
                EditorUtils.editTextValidator(email,etLogin,"enter a valid email") &&
                EditorUtils.isEmailValid(email, etPassword) &&
                EditorUtils.editTextValidator(firstPassword,etPassword,"enter a valid password") &&
                EditorUtils.doPassWordsMatch(firstPassword, repeatedPassword,etRepeatedPassword);
    }

}

