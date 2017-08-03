package com.qhiehome.ihome.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.inquiry.reserveowned.ReserveOwnedRequest;
import com.qhiehome.ihome.network.model.inquiry.reserveowned.ReserveOwnedResponse;
import com.qhiehome.ihome.network.service.inquiry.ReserveOwnedService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReserveListActivity extends BaseActivity {

    //    @BindView(R.id.tb_reserve)
//    Toolbar mTbReserve;
//    @BindView(R.id.rv_reserve)
//    RecyclerView mRvReserve;
    private ReserveAdapter mReserveAdapter;

    private BNRoutePlanNode.CoordinateType mCoordinateType;
    private String mSDCardPath = null;
    private static final String APP_FOLDER_NAME = "ihome";
    private final static String authBaseArr[] =
            {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    private final static String authComArr[] = {Manifest.permission.READ_PHONE_STATE};
    private final static int authBaseRequestCode = 1;
    private final static int authComRequestCode = 2;
    private boolean hasInitSuccess = false;
    private boolean hasRequestComAuth = false;
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    public static List<Activity> activityList = new LinkedList<Activity>();
    public static final String RESET_END_NODE = "resetEndNode";
    private Context mContext;
    private static final String APP_ID = "9901662";

    private RecyclerView mRvReserve;
    private Toolbar mTbReserve;
    private SwipeRefreshLayout mSrlReserve;

    private List<ReserveOwnedResponse.DataBean.ReservationBean> mReservationBeanList = new ArrayList<>();
    private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_list);
//        ButterKnife.bind(this);
        mRvReserve = (RecyclerView) findViewById(R.id.rv_reserve);
        mTbReserve = (Toolbar) findViewById(R.id.tb_reserve);
        mSrlReserve = (SwipeRefreshLayout) findViewById(R.id.srl_reserve);
        mContext = this;
        initToolbar();

        mSrlReserve.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reserveRequest();
                mReserveAdapter.notifyDataSetChanged();
            }
        });
        mSrlReserve.setRefreshing(true);
        if (initDirs()) {
            initNavi();
        }
        reserveRequest();
        initRecyclerView();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ReserveListActivity.class);
        context.startActivity(intent);
    }
    

    private void initToolbar() {
        setSupportActionBar(mTbReserve);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbReserve.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /******RecyclerView********/
    private void initRecyclerView() {
        mRvReserve.setLayoutManager(new LinearLayoutManager(this));
        mReserveAdapter = new ReserveAdapter();
        mRvReserve.setAdapter(mReserveAdapter);
        DividerItemDecoration did = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);
        mRvReserve.addItemDecoration(did);
    }

    class ReserveAdapter extends RecyclerView.Adapter<ReserveAdapter.MyViewHolder> {
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(ReserveListActivity.this).inflate(R.layout.item_reserve_list, parent, false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            holder.tv_estate.setText(mReservationBeanList.get(position).getEstate().getName());
            holder.tv_time.setText(TIME_FORMAT.format(mReservationBeanList.get(position).getStartTime()) + "~" + TIME_FORMAT.format(mReservationBeanList.get(position).getEndTime()));

            holder.btn_navi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //导航到小区
                    if (BaiduNaviManager.isNaviInited()) {
                        routeplanToNavi(BNRoutePlanNode.CoordinateType.BD09LL, position);
                    }
                }
            });

            holder.btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: 2017/8/3 请求取消预约
                }
            });
        }


        @Override
        public int getItemCount() {
//            String table = ParkingSQLHelper.TABLE_NAME;
//            String[] columns = new String[]{"count(shareID)"};
//            String selection = "startTime>?";
//            String[] selectionArgs = new String[]{String.valueOf(System.currentTimeMillis())};
//            String orderBy = "startTime, endTime";
//            Cursor cursor = mParkingDatabase.query(table,columns,selection,selectionArgs,null,null,orderBy,null);
//            while(cursor.moveToNext()){
//                num = cursor.getInt(cursor.getColumnIndex("count(shareID"));
//            }
            return mReservationBeanList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_estate;
            TextView tv_time;
            Button btn_navi;
            Button btn_cancel;

            public MyViewHolder(View view) {
                super(view);
                tv_estate = (TextView) view.findViewById(R.id.tv_reserve_estate);
                tv_time = (TextView) view.findViewById(R.id.tv_reserve_time);
                btn_navi = (Button) view.findViewById(R.id.btn_reserve_navi);
                btn_cancel = (Button) view.findViewById(R.id.btn_reserve_cancel);
            }

        }
    }

    private void reserveRequest(){
        ReserveOwnedService reserveOwnedService = ServiceGenerator.createService(ReserveOwnedService.class);
        ReserveOwnedRequest reserveOwnedRequest = new ReserveOwnedRequest(SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, Constant.TEST_PHONE_NUM));
        Call<ReserveOwnedResponse> call = reserveOwnedService.reserveOwned(reserveOwnedRequest);
        call.enqueue(new Callback<ReserveOwnedResponse>() {
            @Override
            public void onResponse(Call<ReserveOwnedResponse> call, Response<ReserveOwnedResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE){
                    mReservationBeanList = response.body().getData().getReservation();
                }
                mSrlReserve.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ReserveOwnedResponse> call, Throwable t) {
                ToastUtil.showToast(ReserveListActivity.this, "网络连接异常");
                mSrlReserve.setRefreshing(false);
            }
        });
    }

    /*********导航功能**********/
    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    String authinfo = null;
    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                    // showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                    // showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 内部TTS播报状态回调接口
     */
    private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

        @Override
        public void playEnd() {
            // showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
            // showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

    public void showToastMsg(final String msg) {

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hasBasePhoneAuth() {
        // TODO Auto-generated method stub

        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasCompletePhoneAuth() {
        // TODO Auto-generated method stub

        PackageManager pm = this.getPackageManager();
        for (String auth : authComArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void initNavi() {

        BNOuterTTSPlayerCallback ttsCallback = null;

        // 申请权限
        if (Build.VERSION.SDK_INT >= 23) {

            if (!hasBasePhoneAuth()) {

                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;

            }
        }

        BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败, " + msg;
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mContext, authinfo, Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void initSuccess() {
                Toast.makeText(mContext, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                hasInitSuccess = true;
                initSetting();
            }

            public void initStart() {
                Toast.makeText(mContext, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
                Toast.makeText(mContext, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
            }

        }, null, ttsHandler, ttsPlayStateListener);

    }

    private void initSetting() {
        // BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
        BNaviSettingManager
                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        // BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
        BNaviSettingManager.setIsAutoQuitWhenArrived(true);
        Bundle bundle = new Bundle();
        // 必须设置APPID，否则会静音
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, APP_ID);
        BNaviSettingManager.setNaviSdkParam(bundle);
    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }


    private void routeplanToNavi(BNRoutePlanNode.CoordinateType coType, int position) {
        mCoordinateType = coType;
        if (!hasInitSuccess) {
            Toast.makeText(mContext, "还未初始化!", Toast.LENGTH_SHORT).show();
        }
        // 权限申请
        if (Build.VERSION.SDK_INT >= 23) {
            // 保证导航功能完备
            if (!hasCompletePhoneAuth()) {
                if (!hasRequestComAuth) {
                    hasRequestComAuth = true;
                    this.requestPermissions(authComArr, authComRequestCode);
                    return;
                } else {
                    Toast.makeText(mContext, "没有完备的权限!", Toast.LENGTH_SHORT).show();
                }
            }

        }
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;
        switch (coType) {
//            case GCJ02: {
//                sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
//                break;
//            }
//            case WGS84: {
//                sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
//                break;
//            }
//            case BD09_MC: {
//                sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
//                break;
//            }
            case BD09LL: {
                sNode = new BNRoutePlanNode((double) SharedPreferenceUtil.getFloat(mContext, Constant.CURRENT_LONGITUDE, 0), (double) SharedPreferenceUtil.getFloat(mContext, Constant.CURRENT_LATITUDE, 0), "我的位置", null, coType);
                try{
                    eNode = new BNRoutePlanNode(mReservationBeanList.get(position).getEstate().getX(), mReservationBeanList.get(position).getEstate().getY(), mReservationBeanList.get(position).getEstate().getName(), null, coType);
                }catch (Exception e){
                    e.printStackTrace();
                    ToastUtil.showToast(this, e.getMessage());
                }
                //查询数据库得到目的地经纬度
//                mParkingReadDB = mParkingSQLHelper.getReadableDatabase();
//                Cursor cursor = mParkingReadDB.query(ParkingSQLHelper.TABLE_NAME,
//                        new String[]{"startTime", "estateName", "x", "y"},
//                        null, null, null, null, "startTime ASC");
//                while (cursor.moveToNext()) {
//                    long startTime = cursor.getLong(cursor.getColumnIndex("startTime"));
//                    if (startTime >= System.currentTimeMillis()) {
//                        String estateName = cursor.getString(cursor.getColumnIndex("estateName"));
//                        double x = cursor.getDouble(cursor.getColumnIndex("x"));
//                        double y = cursor.getDouble(cursor.getColumnIndex("y"));
//                        eNode = new BNRoutePlanNode(x, y, estateName, null, coType);
//                        break;
//                    }
//                }


//                mParkingReadDB.close();
                break;
            }
            default:
                ;
        }
        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);

            // 开发者可以使用旧的算路接口，也可以使用新的算路接口,可以接收诱导信息等
            BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
            //BaiduNaviManager.getInstance().launchNavigator(this.getActivity(), list, 1, true, new DemoRoutePlanListener(sNode),
            //        eventListerner);
        }
    }

    BaiduNaviManager.NavEventListener eventListerner = new BaiduNaviManager.NavEventListener() {

        @Override
        public void onCommonEventCall(int what, int arg1, int arg2, Bundle bundle) {
            //BNEventHandler.getInstance().handleNaviEvent(what, arg1, arg2, bundle);
        }
    };


    public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
            /*
             * 设置途径点以及resetEndNode会回调该接口
             */

            for (Activity ac : activityList) {

                if (ac.getClass().getName().endsWith("BNDemoGuideActivity")) {

                    return;
                }
            }
            Intent intent = new Intent(ReserveListActivity.this, NaviGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
            // TODO Auto-generated method stub
            Toast.makeText(mContext, "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == authBaseRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                } else {
                    Toast.makeText(mContext, "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            initNavi();
        } else if (requestCode == authComRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                }
            }
            routeplanToNavi(mCoordinateType, -1);
        }

    }


}