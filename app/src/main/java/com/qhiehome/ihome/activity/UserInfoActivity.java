package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.ScanLockAdapter;
import com.qhiehome.ihome.adapter.UserLockAdapter;
import com.qhiehome.ihome.bean.UserLockBean;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedResponse;
import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdRequest;
import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdResponse;
import com.qhiehome.ihome.network.model.signin.SigninRequest;
import com.qhiehome.ihome.network.model.signin.SigninResponse;
import com.qhiehome.ihome.network.service.inquiry.ParkingOwnedService;
import com.qhiehome.ihome.network.service.lock.UpdateLockPwdService;
import com.qhiehome.ihome.network.service.signin.SigninService;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoActivity extends BaseActivity {

    private static final String TAG = UserInfoActivity.class.getSimpleName();

    @BindView(R.id.tb_userinfo)
    Toolbar mTbUserinfo;
    @BindView(R.id.rv_userinfo)
    RecyclerView mRvUserinfo;

    @BindView(R.id.vs_user_locks)
    ViewStub mViewStub;

    private static final String[] LIST_CONTENT = {"头像","手机号","昵称"};
    private List<String> userInfo;

    private Context mContext;

    private ArrayList<UserLockBean> mUserLocks;

    private long mCurrentTime;

    private StringBuilder mParkingIds;

    EditText mEtOldPwd;

    EditText mEtNewPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);
        mContext = this;
        initData();
        initView();
        initUserInfo();
    }

    private void initData() {
        //requestAvatarAndNickName();
        mUserLocks = new ArrayList<>();
        mParkingIds = new StringBuilder();
        inquiryOwnedParkings();
    }

    private void inquiryOwnedParkings() {
        mCurrentTime = System.currentTimeMillis();
        ParkingOwnedService parkingOwnedService = ServiceGenerator.createService(ParkingOwnedService.class);
        ParkingOwnedRequest parkingOwnedRequest = new ParkingOwnedRequest(Constant.TEST_PHONE_NUM);
        Call<ParkingOwnedResponse> call = parkingOwnedService.parkingOwned(parkingOwnedRequest);
        call.enqueue(new Callback<ParkingOwnedResponse>() {
            @Override
            public void onResponse(@NonNull  Call<ParkingOwnedResponse> call,@NonNull Response<ParkingOwnedResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    // success and then inflate ViewStub
                    List<ParkingResponse.DataBean.EstateBean> estateList = response.body().getData().getEstate();
                    if (estateList.size() != 0) {
                        mViewStub.inflate();
                        RecyclerView rvUserLocks = (RecyclerView) findViewById(R.id.rv_user_locks);
                        rvUserLocks.setHasFixedSize(true);
                        LinearLayoutManager llm = new LinearLayoutManager(mContext);
                        rvUserLocks.setLayoutManager(llm);
                        initLocks(estateList);
                        UserLockAdapter userLockAdapter = new UserLockAdapter(mContext, mUserLocks);
                        rvUserLocks.setAdapter(userLockAdapter);
                        initListener(userLockAdapter);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull  Call<ParkingOwnedResponse> call,@NonNull Throwable t) {

            }
        });
    }

    private void initListener(final UserLockAdapter userLockAdapter) {
        userLockAdapter.setOnItemClickListener(new UserLockAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int i) {
                UserLockBean userLockBean = mUserLocks.get(i);
                View controllLock = LayoutInflater.from(mContext).inflate(R.layout.dialog_controll_lock, null);
                ImageView imgUpLock = (ImageView) controllLock.findViewById(R.id.img_up_lock);
                ImageView imgDownLock = (ImageView) controllLock.findViewById(R.id.img_down_Lock);
                imgUpLock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtil.showToast(mContext, "raise up the lock");
                    }
                });
                imgDownLock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtil.showToast(mContext, "lower the lock");
                    }
                });
                new MaterialDialog.Builder(mContext)
                        .title("连接中").titleGravity(GravityEnum.CENTER)
                        .customView(controllLock ,false)
                        .show();
            }

            @Override
            public void onButtonClick(View view, int i) {
                final UserLockBean userLockBean = mUserLocks.get(i);
                MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                        .customView(R.layout.dialog_modify_pwd, false)
                        .positiveText("确定")
                        .negativeText("取消")
                        .build();
                View customView = dialog.getCustomView();
                if (customView != null) {
                    mEtOldPwd = (EditText) customView.findViewById(R.id.et_old_pwd);
                    mEtNewPwd = (EditText) customView.findViewById(R.id.et_new_pwd);
                }
                dialog.getBuilder()
                        .showListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                CommonUtil.showSoftKeyboard(mEtOldPwd, mContext);
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                int parkingId = userLockBean.getParkingId();
                                String oldPwd = mEtOldPwd.getText().toString();
                                String newPwd = mEtNewPwd.getText().toString();
                                if (!(TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd))) {
                                    modifyLockPwd(parkingId, mEtOldPwd.getText().toString(), mEtNewPwd.getText().toString());
                                }
                            }
                        })
                        .canceledOnTouchOutside(false)
                        .show();
            }
        });
    }

    private void modifyLockPwd(int parkingId, String oldPwd, String newPwd) {
        UpdateLockPwdService updateLockPwdService = ServiceGenerator.createService(UpdateLockPwdService.class);
        UpdateLockPwdRequest updateLockPwdRequest = new UpdateLockPwdRequest(parkingId, oldPwd, newPwd);
        Call<UpdateLockPwdResponse> call = updateLockPwdService.updateLockPwd(updateLockPwdRequest);
        call.enqueue(new Callback<UpdateLockPwdResponse>() {
            @Override
            public void onResponse(@NonNull Call<UpdateLockPwdResponse> call, @NonNull Response<UpdateLockPwdResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    ToastUtil.showToast(mContext, "modify password successfully");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdateLockPwdResponse> call, @NonNull Throwable t) {

            }
        });
    }


    private void initLocks(List<ParkingResponse.DataBean.EstateBean> estateList) {
        mUserLocks.clear();
        UserLockBean userLockBean;
        boolean isRented = false;
        for (ParkingResponse.DataBean.EstateBean estate: estateList) {
            for (ParkingResponse.DataBean.EstateBean.ParkingBean parkingBean: estate.getParking()) {
                List<ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean> share = parkingBean.getShare();
                for (int i = 0; i < share.size(); i++) {
                    ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean shareBean = share.get(i);
                    long startTime = shareBean.getStartTime();
                    long endTime = shareBean.getEndTime();
                    if (mCurrentTime >= startTime && mCurrentTime <= endTime) {
                        isRented = true;
                    }
                }
                userLockBean = new UserLockBean(estate.getName(), parkingBean.getName(), Integer.valueOf(parkingBean.getId()), parkingBean.getGatewayId(),
                        parkingBean.getLockMac(), isRented);
                mUserLocks.add(userLockBean);
                mParkingIds.append(parkingBean.getId()).append(",");
            }
        }
        SharedPreferenceUtil.setString(this, Constant.OWNED_PARKING_KEY, mParkingIds.deleteCharAt(mParkingIds.length() - 1).toString());
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, UserInfoActivity.class);
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
        UserInfoAdapter mAdapter = new UserInfoAdapter();
        mRvUserinfo.setAdapter(mAdapter);
        Context context = UserInfoActivity.this;
        DividerItemDecoration did = new DividerItemDecoration(context,LinearLayoutManager.VERTICAL);
        mRvUserinfo.addItemDecoration(did);
    }

    private void initUserInfo(){
        userInfo = new ArrayList<>();
        userInfo.add("img_profile.jpg");
        userInfo.add(SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, Constant.TEST_PHONE_NUM));
        userInfo.add("铁锤");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
