package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserInfoActivity extends AppCompatActivity {

    private static final String TAG = UserInfoActivity.class.getSimpleName();

    @BindView(R.id.tb_userinfo)
    Toolbar mTbUserinfo;
    @BindView(R.id.rv_userinfo)
    RecyclerView mRvUserinfo;

    private UserInfoAdapter mAdapter;
    private static final String[] LIST_CONTENT = {"头像","手机号","昵称"};
    private List<String> userInfo;

    private String mPhoneNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);
        initData();
        initView();
        initUserInfo();
        ActivityManager.add(this);
    }

    private void initData() {
        mPhoneNum = getIntent().getStringExtra(Constant.PHONE_PARAM);
    }

    public static void start(Context context, String phoneNum) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        intent.putExtra(Constant.PHONE_PARAM, phoneNum);
        context.startActivity(intent);
    }

    private void initView() {
        initToolbar();
        initRecyclerView();
    }

    private void initToolbar() {
        setSupportActionBar(mTbUserinfo);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbUserinfo.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void initRecyclerView(){
        mRvUserinfo.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new UserInfoAdapter();
        mRvUserinfo.setAdapter(mAdapter);
        Context context = UserInfoActivity.this;
        DividerItemDecoration did = new DividerItemDecoration(context,LinearLayoutManager.VERTICAL);
        mRvUserinfo.addItemDecoration(did);
    }

    private void initUserInfo(){
        userInfo = new ArrayList<String>();
        userInfo.add("img_profile.jpg");
        userInfo.add(mPhoneNum);
        userInfo.add("铁锤");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.remove(this);
    }

    class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.MyViewHolder>{
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(UserInfoActivity.this).inflate(R.layout.item_user_info_list,parent,false));
            return viewHolder;
        }
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position){
            holder.tv_userinfo.setText(LIST_CONTENT[position]);
            if (position == 0){
                Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.img_profile);
                holder.iv_userimg.setImageBitmap(bm);
            }else {
                holder.tv_usercontent.setText(userInfo.get(position));
            }
        }
        @Override
        public int getItemCount(){
            return LIST_CONTENT.length;
        }

        class MyViewHolder extends ViewHolder{
            TextView tv_userinfo;
            TextView tv_usercontent;
            ImageView iv_userimg;
            public MyViewHolder(View view){
                super(view);
                tv_userinfo = (TextView) view.findViewById(R.id.tv_userinfo);
                tv_usercontent = (TextView) view.findViewById(R.id.tv_usercontent);
                iv_userimg = (ImageView) view.findViewById(R.id.iv_userimg);
            }

        }
    }

}