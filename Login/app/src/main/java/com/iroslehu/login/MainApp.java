package com.iroslehu.login;

import android.app.Application;
import com.facebook.appevents.AppEventsLogger;

public class MainApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppEventsLogger.activateApp(this);


    }

}