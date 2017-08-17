package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.indexable_recyclerview.AlphabetItem;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyResponse;
import com.qhiehome.ihome.network.model.park.reserve.ReserveRequest;
import com.qhiehome.ihome.network.model.park.reserve.ReserveResponse;
import com.qhiehome.ihome.network.service.park.ReserveService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.TimeUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.vivian.timelineitemdecoration.itemdecoration.DotItemDecoration;
import com.vivian.timelineitemdecoration.itemdecoration.SpanIndexListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkingTimelineActivity extends AppCompatActivity {

//    @BindView(R.id.rv_parking_timeline)
//    RecyclerView mRvParkingTimeline;
    @BindView(R.id.rv_parking_timeline)
    IndexFastScrollRecyclerView mRvParkingTimeline;
    @BindView(R.id.tb_parking_timeline)
    Toolbar mTbParkingTimeline;
    @BindView(R.id.tv_parking_name)
    TextView mTvParkingName;
    @BindView(R.id.tv_parking_timeline_guaranteeFee_num)
    TextView mTvGuaranteeFee;
    @BindView(R.id.btn_parking_timeline_reserve)
    Button mBtnReserve;

    private Context mContext;
    private ParkingAdapter mAdapter;
    private DotItemDecoration mItemDecoration;
    private ArrayList<Integer> mSectionPositions;
    private List<AlphabetItem> mAlphabetItems;
    private ParkingEmptyResponse.DataBean.EstateBean mEstateBean;
    private List<ParkingEmptyResponse.DataBean.EstateBean.ParkingBean.ShareBean> mShareBeanList = new ArrayList<>();
    private List<Boolean> mSelectedList = new ArrayList<>();
    private int mSelectedNum = 0;
    private float mGruaranteeFee = 0;
    private int mReserveNum = 0;
    private int mReserveFailedNum = 0;
    private int mSelectedIndex = -1;

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH");
    private static final int[] COLORS = {0xffFFAD6C, 0xff62f434, 0xffdeda78, 0xff7EDCFF, 0xff58fdea, 0xfffdc75f};//颜色组
    private static final String DECIMAL_2 = "%.2f";

    private static final int ERROR_CODE_UNPAY = 300;
    private static final int ERROR_CODE_RESERVED = 301;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_timeline);
        ButterKnife.bind(this);
        mContext = this;
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mEstateBean = (ParkingEmptyResponse.DataBean.EstateBean) bundle.get("estate");
        mGruaranteeFee = mEstateBean.getGuaranteeFee();
        initToolbar();
        initData();
        initRecyclerView();
        mTvGuaranteeFee.setText("0.00");

    }


    private void initToolbar() {
        setSupportActionBar(mTbParkingTimeline);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTvParkingName.setText(mEstateBean.getName() + "：" + String.format(DECIMAL_2, (float) mEstateBean.getUnitPrice()) + "元/小时");
        mTbParkingTimeline.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        mRvParkingTimeline.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mItemDecoration = new DotItemDecoration
                .Builder(this)
                .setOrientation(DotItemDecoration.VERTICAL)//if you want a horizontal item decoration,remember to set horizontal orientation to your LayoutManager
                .setItemStyle(DotItemDecoration.STYLE_DRAW)
                .setTopDistance(20)//dp
                .setItemInterVal(10)//dp
                .setItemPaddingLeft(20)//default value equals to item interval value
                .setItemPaddingRight(20)//default value equals to item interval value
                .setDotColor(Color.WHITE)
                .setDotRadius(2)//dp
                .setDotPaddingTop(0)
                .setDotInItemOrientationCenter(false)//set true if you want the dot align center
                .setLineColor(Color.BLACK)
                .setLineWidth(4)//dp
                .setEndText("")
                .setTextColor(Color.WHITE)
                .setTextSize(10)//sp
                .setDotPaddingText(2)//dp.The distance between the last dot and the end text
                .setBottomDistance(40)//you can add a distance to make bottom line longer
                .create();
        mItemDecoration.setSpanIndexListener(new SpanIndexListener() {
            @Override
            public void onSpanIndexChange(View view, int spanIndex) {
                view.setBackgroundResource(spanIndex == 0 ? R.drawable.pop_left : R.drawable.pop_right);
            }
        });
        mRvParkingTimeline.addItemDecoration(mItemDecoration);
        mAdapter = new ParkingAdapter();
        mAdapter.setOnItemClickListener(new OnClickListener() {
            @Override
            public void onClick(View view, int i) {
                /**********预约单个车位**********/
                mSelectedIndex = i;
                mAdapter.notifyDataSetChanged();
                mTvGuaranteeFee.setText(String.format(DECIMAL_2, mGruaranteeFee));
                /*********预约多个车位***********/
//                TextView tv_time = (TextView) view.findViewById(R.id.tv_parking_timeline_time);
//                TextView tv_info = (TextView) view.findViewById(R.id.tv_parking_timeline_info);
//                mSelectedList.set(i, !mSelectedList.get(i));
//                if (mSelectedList.get(i)) {
//                    //view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
//                    mSelectedNum++;
//                    tv_time.setTextColor(ContextCompat.getColor(mContext, R.color.white));
//                    tv_info.setTextColor(ContextCompat.getColor(mContext, R.color.white));
//                } else {
//                    mSelectedNum--;
//                    tv_time.setTextColor(ContextCompat.getColor(mContext, R.color.black));
//                    tv_info.setTextColor(ContextCompat.getColor(mContext, R.color.black));
//                }
//                mTvGuaranteeFee.setText(String.format(DECIMAL_2, mSelectedNum * mGruaranteeFee));
            }
        });
        mRvParkingTimeline.setAdapter(mAdapter);
        //set index ui
        mRvParkingTimeline.setIndexTextSize(12);
        mRvParkingTimeline.setIndexBarColor("#33334c");
        mRvParkingTimeline.setIndexBarCornerRadius(0);
        mRvParkingTimeline.setIndexBarTransparentValue((float) 0.4);
        mRvParkingTimeline.setIndexbarMargin(0);
        mRvParkingTimeline.setIndexbarWidth(40);
        mRvParkingTimeline.setPreviewPadding(0);
        mRvParkingTimeline.setIndexBarTextColor("#FFFFFF");
        mRvParkingTimeline.setIndexBarVisibility(true);
        mRvParkingTimeline.setIndexbarHighLateTextColor("#33334c");
        mRvParkingTimeline.setIndexBarHighLateTextVisibility(true);
//        mRvParkingTimeline.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    mRvParkingTimeline.setIndexBarVisibility(true);
//                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    mRvParkingTimeline.setIndexBarVisibility(false);
//                }
//            }
//        });
    }

    private void initData() {
        for (int i = 0; i < mEstateBean.getParking().size(); i++) {
            if (mEstateBean.getParking().get(i).getShare().size() != 0) {
                for (int j = 0; j < mEstateBean.getParking().get(i).getShare().size(); j++) {
                    mShareBeanList.add(mEstateBean.getParking().get(i).getShare().get(j));
                    mSelectedList.add(false);
                }
            }
        }
        Collections.sort(mShareBeanList);

        //Alphabet fast scroller data
        mAlphabetItems = new ArrayList<>();
        List<String> strAlphabets = new ArrayList<>();
        for (int i = 0; i<mShareBeanList.size(); i++){
            Date date = TimeUtil.getInstance().millis2Date(mShareBeanList.get(i).getStartTime());
            String hour = HOUR_FORMAT.format(date);
            if (!strAlphabets.contains(hour)){
                strAlphabets.add(hour);
                mAlphabetItems.add(new AlphabetItem(i, hour, false));
            }
        }
    }

    @OnClick(R.id.btn_parking_timeline_reserve)
    public void onReserveClicked() {
        /*********预约单个车位*********/
        if (mSelectedIndex == -1){
            ToastUtil.showToast(mContext, "请选择车位");
        }else {
            reserve_single_request();
        }
        /*********预约多个车位*********/
//        if (mSelectedNum == 0){
//            ToastUtil.showToast(mContext, "请选择车位");
//        }else {
//
//            for (int i = 0; i<mSelectedList.size(); i++){
//                if (mSelectedList.get(i)){
//                    reserve_request(mShareBeanList.get(i), false);
//                }
//            }
//
//        }
    }

    class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> implements SectionIndexer {
        @Override
        public ParkingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ParkingViewHolder parkingViewHolder = new ParkingViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_parking_timeline, parent, false));
            return parkingViewHolder;
        }

        @Override
        public void onBindViewHolder(final ParkingViewHolder holder, int position) {
            if (mShareBeanList.size() == 1 && position == 1){//防崩溃
                holder.tv_time.setText("");
                holder.tv_info.setText("");
                holder.itemView.setVisibility(View.INVISIBLE);
            }else {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onClickListener != null) {
                            onClickListener.onClick(holder.itemView, holder.getLayoutPosition());
                        }
                    }
                });
                Date startDate = TimeUtil.getInstance().millis2Date(mShareBeanList.get(position).getStartTime());
                Date endDate = TimeUtil.getInstance().millis2Date(mShareBeanList.get(position).getEndTime());
                String startTime = TIME_FORMAT.format(startDate);
                String endTime = TIME_FORMAT.format(endDate);
                holder.tv_time.setText(startTime + " - " + endTime);
                long timePeriod = mShareBeanList.get(position).getEndTime() - mShareBeanList.get(position).getStartTime();
                int minutes_total = (int) timePeriod/1000/60;
                int hours = minutes_total/60;
                int minutes = minutes_total%60;
                String time_length = "";
                if (hours == 0){
                    time_length = minutes + "分";
                }else {
                    if (minutes == 0){
                        time_length = hours + "小时";
                    }else {
                        time_length = hours + "小时" + minutes + "分";
                    }
                }
                holder.tv_info.setText(time_length);
                //holder.tv_time.setTextColor(COLORS[position % COLORS.length]);

                /**********预约单个车位**********/
                if (position != mSelectedIndex) {
                    holder.tv_info.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    holder.tv_time.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                }else {
                    holder.tv_info.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    holder.tv_time.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                }

            }
        }

        @Override
        public int getItemCount() {
            if (mShareBeanList.size() == 1){
                return 2;
            }else {
                return mShareBeanList.size();
            }
        }

        class ParkingViewHolder extends RecyclerView.ViewHolder {
            TextView tv_time;
            TextView tv_info;
            public ParkingViewHolder(View itemView) {
                super(itemView);
                tv_time = (TextView) itemView.findViewById(R.id.tv_parking_timeline_time);
                tv_info = (TextView) itemView.findViewById(R.id.tv_parking_timeline_info);
            }
        }

        public void setOnItemClickListener(OnClickListener listener) {
            this.onClickListener = listener;
        }

        private OnClickListener onClickListener;

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }

        @Override
        public Object[] getSections() {
            List<String> sections = new ArrayList<>(24);
            mSectionPositions = new ArrayList<>(24);
            for (int i = 0, size = mShareBeanList.size(); i<size; i++){
                String section = String.format(HOUR_FORMAT.format(mShareBeanList.get(i).getStartTime()));
                if (!sections.contains(section)){
                    sections.add(section);
                    mSectionPositions.add(i);
                }
            }
            return sections.toArray(new String[0]);
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return mSectionPositions.get(sectionIndex);
        }
    }

    public interface OnClickListener {
        void onClick(View view, int i);
    }

    private void reserve_single_request(){
        ReserveService reserveService = ServiceGenerator.createService(ReserveService.class);
        final ReserveRequest reserveRequest = new ReserveRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.SHA_256), mShareBeanList.get(mSelectedIndex).getId(), mShareBeanList.get(mSelectedIndex).getStartTime(), mShareBeanList.get(mSelectedIndex).getEndTime());
        Call<ReserveResponse> call = reserveService.reserve(reserveRequest);
        call.enqueue(new Callback<ReserveResponse>() {
            @Override
            public void onResponse(Call<ReserveResponse> call, Response<ReserveResponse> response) {
                int red = ContextCompat.getColor(mContext, android.R.color.holo_red_light);
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    Intent intent = new Intent(ParkingTimelineActivity.this, PayActivity.class);
                    intent.putExtra("grauFee", mGruaranteeFee);
                    intent.putExtra("isPay", true);
                    startActivity(intent);
                    ParkingTimelineActivity.this.finish();
                }else if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == ERROR_CODE_UNPAY){
                    new MaterialDialog.Builder(mContext)
                            .title("预约失败")
                            .titleColor(red)
                            .content("您有尚未支付的订单")
                            .contentColor(red)
                            .positiveText("去支付")
                            .positiveColor(red)
                            .negativeText("取消")
                            .canceledOnTouchOutside(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Intent intent = new Intent(ParkingTimelineActivity.this, ReserveListActivity.class);
                                    startActivity(intent);
                                    ParkingTimelineActivity.this.finish();
                                }
                            })
                            .show();
                }else if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == ERROR_CODE_RESERVED){
                    new MaterialDialog.Builder(mContext)
                            .title("预约失败")
                            .titleColor(red)
                            .content("您有已预约的订单")
                            .contentColor(red)
                            .positiveText("去停车")
                            .positiveColor(red)
                            .negativeText("取消")
                            .canceledOnTouchOutside(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Intent intent = new Intent(ParkingTimelineActivity.this, ReserveListActivity.class);
                                    startActivity(intent);
                                    ParkingTimelineActivity.this.finish();
                                }
                            })
                            .show();
                }else {
                    new MaterialDialog.Builder(mContext)
                            .title("预约失败")
                            .titleColor(red)
                            .content("您的车位已被预约")
                            .contentColor(red)
                            .negativeText("重新选择")
                            .canceledOnTouchOutside(false)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    ParkingTimelineActivity.this.finish();
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ReserveResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }


    /********预约多个车位*********/
//    private void reserve_request(ParkingEmptyResponse.DataBean.EstateBean.ParkingBean.ShareBean shareBean, boolean isLast){
//        ReserveService reserveService = ServiceGenerator.createService(ReserveService.class);
//        ReserveRequest reserveRequest = new ReserveRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.SHA_256), shareBean.getId(), shareBean.getStartTime(), shareBean.getEndTime());
//        Call<ReserveResponse> call = reserveService.reserve(reserveRequest);
//        call.enqueue(new Callback<ReserveResponse>() {
//            @Override
//            public void onResponse(Call<ReserveResponse> call, Response<ReserveResponse> response) {
//                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
//                    mReserveNum ++;
//                }else {
//                    mReserveFailedNum ++;
//                }
//                if (mReserveFailedNum + mReserveNum == mSelectedNum){//全部请求发出
//                    reserveResult();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ReserveResponse> call, Throwable t) {
//                ToastUtil.showToast(mContext, "网络连接异常");
//                mReserveFailedNum ++;
//                if (mReserveFailedNum + mReserveNum == mSelectedNum){//全部请求发出
//                    reserveResult();
//                }
//            }
//        });
//    }


    /********预约多个车位*********/
//    private void reserveResult(){
//        if (mReserveFailedNum == 0){//全部预约成功->跳转支付
//            Intent intent = new Intent(ParkingTimelineActivity.this, PayActivity.class);
//            intent.putExtra("grauFee", mGruaranteeFee * mSelectedNum);
//            intent.putExtra("isPay", true);
//            startActivity(intent);
//            ParkingTimelineActivity.this.finish();
//        }else if (mReserveNum > 0){//部分预约失败->确认预约或重新选择
//            int red = ContextCompat.getColor(mContext, android.R.color.holo_red_light);
//            new MaterialDialog.Builder(mContext)
//                    .title("预约失败")
//                    .titleColor(red)
//                    .content("您有"+ mReserveFailedNum + "个车位预约失败")
//                    .contentColor(red)
//                    .positiveText("确认预约")
//                    .positiveColor(red)
//                    .negativeText("重新选择")
//                    .canceledOnTouchOutside(false)
//                    .onPositive(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            Intent intent = new Intent(ParkingTimelineActivity.this, PayActivity.class);
//                            intent.putExtra("grauFee", mGruaranteeFee * mReserveNum);
//                            intent.putExtra("isPay", true);
//                            startActivity(intent);
//                            ParkingTimelineActivity.this.finish();
//                        }
//                    })
//                    .onNegative(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            ParkingTimelineActivity.this.finish();
//                        }
//                    })
//                    .show();
//        }else {//全部预约失败
//            int red = ContextCompat.getColor(mContext, android.R.color.holo_red_light);
//            new MaterialDialog.Builder(mContext)
//                    .title("预约失败")
//                    .titleColor(red)
//                    .content("您有"+ mReserveFailedNum + "个车位预约失败")
//                    .contentColor(red)
//                    .negativeText("重新选择")
//                    .canceledOnTouchOutside(false)
//                    .onNegative(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            ParkingTimelineActivity.this.finish();
//                        }
//                    })
//                    .show();
//        }
//    }
}