package com.techart.writersblock;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.firebase.client.Firebase;
import com.google.firebase.database.FirebaseDatabase;
import com.techart.writersblock.utils.TypefaceUtil;

/**
 * Initializes Fire base context
 * Created by Kelvin on 31/05/2017.
 */

public class WritersBlock extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        TypefaceUtil.overrideFonts(getApplicationContext());
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
