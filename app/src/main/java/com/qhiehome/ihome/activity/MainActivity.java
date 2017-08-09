package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.mapapi.SDKInitializer;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.fragment.MeFragment;
import com.qhiehome.ihome.fragment.ParkFragment;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    TextView mTvPark;
    TextView mTvMe;

    private List<TextView> mTabTextIndicators = new ArrayList<>();

    private Context mContext;

//    Toolbar mToolbar;

    Fragment mParkFragment;
    Fragment mMeFragment;
    Fragment mThisFragment;
    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mContext = this;
        initData();
        initView();
        initFragments(savedInstanceState);
    }

    private void initData() {
    }

    private void initView() {
//        initToolbar();
        mTvPark = (TextView) findViewById(R.id.tv_park);
        mTvMe = (TextView) findViewById(R.id.tv_me);
        mTabTextIndicators.add(mTvPark);
        mTabTextIndicators.add(mTvMe);

        RelativeLayout mRlPark = (RelativeLayout) findViewById(R.id.rl_park);
        RelativeLayout mRlMe = (RelativeLayout) findViewById(R.id.rl_me);

        mRlPark.setOnClickListener(this);
        mRlMe.setOnClickListener(this);
    }

    private void initFragments(Bundle savedInstanceState) {
        mFragmentManager = getSupportFragmentManager();
        // First init load ParkFragment
        if (savedInstanceState == null) {
            mParkFragment = ParkFragment.newInstance();
            mMeFragment = MeFragment.newInstance();
            mFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, mParkFragment, ParkFragment.TAG).commit();
        } else {
            mParkFragment = mFragmentManager.findFragmentByTag(ParkFragment.TAG);
            mMeFragment = mFragmentManager.findFragmentByTag(MeFragment.TAG);
            mFragmentManager.beginTransaction()
                    .show(mParkFragment)
                    .hide(mMeFragment)
                    .commit();
        }
        mThisFragment = mParkFragment;
    }

    @Override
    public void onClick(View v) {
        resetOtherTabText();
        switch (v.getId()) {
            case R.id.rl_park:
                mTvPark.setTextColor(getResources().getColor(R.color.colorAccent));
                switchContent(mMeFragment, mParkFragment);
                break;
            case R.id.rl_me:
                String phoneNum = SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, "");
                if (TextUtils.isEmpty(phoneNum)) {
                    mTvPark.setTextColor(getResources().getColor(R.color.colorAccent));
                    new MaterialDialog.Builder(mContext)
                            .title("去登录")
                            .content("确定登录吗？")
                            .positiveText("登录")
                            .negativeText("取消")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    LoginActivity.start(mContext);
                                }
                            })
                            .show();
                } else {
                    mTvMe.setTextColor(getResources().getColor(R.color.colorAccent));
                    switchContent(mParkFragment, mMeFragment);
                }
                break;
            default:
                break;
        }
    }

    private void resetOtherTabText() {
        for (TextView textView: mTabTextIndicators) {
            textView.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void switchContent(Fragment from, Fragment to) {
        if (mThisFragment != to) {
            mThisFragment = to;
            if (!to.isAdded()) {
                String tag = (to instanceof MeFragment) ? MeFragment.TAG: ParkFragment.TAG;
                mFragmentManager.beginTransaction().hide(from).add(R.id.fragment_container, to, tag).commit();
            } else {
                mFragmentManager.beginTransaction().hide(from).show(to).commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mParkFragment.onActivityResult(requestCode, resultCode, data);
    }
}
