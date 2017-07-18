package com.qhiehome.ihome;

import android.app.Application;

import com.mob.MobApplication;

/**
 * This is the global entrance of the app
 *
 * The Ihome application will use below structures:
 *     1. Http Request structure: Retrofit
 *     2. View binding: butterknife
 *     3. ORM structure interact with database: Active Android
 */

public class IhomeApplication extends MobApplication {

    private static final String TAG = "IhomeApplication";

    private static IhomeApplication ihomeApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        ihomeApplication = this;
    }

    public static IhomeApplication getInstance() {
        return ihomeApplication;
    }
}
