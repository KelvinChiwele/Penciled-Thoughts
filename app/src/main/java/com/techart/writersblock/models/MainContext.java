package com.techart.writersblock.models;

import android.content.Context;

public class MainContext {
    private static MainContext instance;
    private Context context;

    public static synchronized MainContext getInstance() {
        if (instance == null) {
            instance = new MainContext();
        }
        return instance;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
