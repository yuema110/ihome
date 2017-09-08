package com.qhiehome.ihome.activity;

import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.LogUtil;


public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private static final int SPLASH_DURATION = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThisFullScreen();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.start(SplashActivity.this);
                finish();
            }
        }, SPLASH_DURATION);
    }

    private void setThisFullScreen() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

}
