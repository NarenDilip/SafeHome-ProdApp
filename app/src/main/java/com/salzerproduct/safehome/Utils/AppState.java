package com.salzerproduct.safehome.Utils;

import android.app.Application;


/**
 * Created by krishna on 18/8/15.
 */
public class AppState extends Application {
    // System info
    public static String SYSTEM_INFO;
    private static AppState mInstance;

    public AppState() {
        super();

    }


    @Override
    public void onCreate() {

        super.onCreate();

        mInstance = this;
        if (AppState.SYSTEM_INFO == null) {
            // Utility.updateSystemInfo(this);
        }


    }


}
