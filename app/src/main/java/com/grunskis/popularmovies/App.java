package com.grunskis.popularmovies;

import android.app.Application;

import com.facebook.stetho.*;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }
}
