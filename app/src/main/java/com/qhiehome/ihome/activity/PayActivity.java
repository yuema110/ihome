package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qhiehome.ihome.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PayActivity extends AppCompatActivity {

    @BindView(R.id.tb_pay)
    Toolbar mTbPay;
    @BindView(R.id.rv_pay)
    RecyclerView mRvPay;
    @BindView(R.id.btn_pay)
    Button mBtnPay;
    @BindView(R.id.layout_pay)
    RelativeLayout mLayoutPay;
    @BindView(R.id.btn_add_balance_1)
    Button mBtnAddBalance1;
    @BindView(R.id.btn_add_balance_2)
    Button mBtnAddBalance2;
    @BindView(R.id.btn_add_balance_3)
    Button mBtnAddBalance3;
    @BindView(R.id.btn_add_balance_4)
    Button mBtnAddBalance4;
    @BindArray(R.array.add_balance)
    String[] mPriceList;

    private Context mContext;
    private PayListAdapter mAdapter;
    private int mSelectedNum;
    private float mFee;
    private int mButtonClicked = 1;
    private List<Button> mBtnList = new ArrayList<>();

    private static final int ALI_PAY = 0;
    private static final int WECHAT_PAY = 1;
    private static final int ACCOUNT_BALANCE = 2;
    private static final String DECIMAL_2 = "%.2f";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        ButterKnife.bind(this);
        Intent intent = this.getIntent();
        boolean isPay = intent.getBooleanExtra("isPay", true);
        mContext = this;
        mSelectedNum = ALI_PAY;
        initToolbar();
        initRecyclerView();

        //支付
        if (isPay) {
            mLayoutPay.setVisibility(View.GONE);
            mFee = intent.getFloatExtra("grauFee", 0);
            mBtnPay.setText("确认支付：" + String.format(Locale.CHINA, DECIMAL_2, mFee) + "元");
        } else {//充值
            mBtnList.add(mBtnAddBalance1);
            mBtnList.add(mBtnAddBalance2);
            mBtnList.add(mBtnAddBalance3);
            mBtnList.add(mBtnAddBalance4);
            mButtonClicked = 1;
            mBtnAddBalance1.setTextColor(ContextCompat.getColor(mContext, R.color.pale_green));
            mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked-1] + "元");
        }


    }

    private void initToolbar() {
        setSupportActionBar(mTbPay);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbPay.setTitle("支付订单");
        mTbPay.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mTbPay.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        mRvPay.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PayListAdapter();
        mAdapter.setOnItemClickListener(new OnClickListener() {
            @Override
            public void onClick(View view, int i) {
                mSelectedNum = i;
                mAdapter.notifyDataSetChanged();
            }
        });
        mRvPay.setAdapter(mAdapter);
        DividerItemDecoration did = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);
        mRvPay.addItemDecoration(did);

    }

    @OnClick(R.id.btn_pay)
    public void onViewClicked() {

        // TODO: 2017/8/14 根据选择方式调用支付接口

    }

    @OnClick({R.id.btn_add_balance_1, R.id.btn_add_balance_2, R.id.btn_add_balance_3, R.id.btn_add_balance_4})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_add_balance_1:
                if (mButtonClicked != 1){
                    mBtnList.get(mButtonClicked-1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mButtonClicked = 1;
                    mBtnAddBalance1.setTextColor(ContextCompat.getColor(mContext, R.color.pale_green));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked-1] + "元");
                }
                break;
            case R.id.btn_add_balance_2:
                if (mButtonClicked != 2){
                    mBtnList.get(mButtonClicked-1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mButtonClicked = 2;
                    mBtnAddBalance2.setTextColor(ContextCompat.getColor(mContext, R.color.pale_green));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked-1] + "元");
                }
                break;
            case R.id.btn_add_balance_3:
                if (mButtonClicked != 3){
                    mBtnList.get(mButtonClicked-1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mButtonClicked = 3;
                    mBtnAddBalance3.setTextColor(ContextCompat.getColor(mContext, R.color.pale_green));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked-1] + "元");
                }
                break;
            case R.id.btn_add_balance_4:
                if (mButtonClicked != 4){
                    mBtnList.get(mButtonClicked-1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mButtonClicked = 4;
                    mBtnAddBalance4.setTextColor(ContextCompat.getColor(mContext, R.color.pale_green));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked-1] + "元");
                }
                break;
        }
    }

    private class PayListAdapter extends RecyclerView.Adapter<PayListAdapter.PayListHolder> {
        @Override
        public PayListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            PayListHolder payListHolder = new PayListHolder(LayoutInflater.from(mContext).inflate(R.layout.item_pay_list, parent, false));
            return payListHolder;
        }

        @Override
        public void onBindViewHolder(final PayListHolder holder, int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        onClickListener.onClick(holder.itemView, holder.getLayoutPosition());
                    }
                }
            });
            switch (position) {
                case ALI_PAY:
                    holder.iv_pay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_pay_alipay));
                    holder.tv_pay.setText("支付宝");
                    holder.tv_pay_info.setText("数亿用户都在用，安全可托付");
                    if (mSelectedNum == ALI_PAY) {
                        holder.iv_pay_select.setVisibility(View.VISIBLE);
                    } else {
                        holder.iv_pay_select.setVisibility(View.INVISIBLE);
                    }
                    break;
                case WECHAT_PAY:
                    holder.iv_pay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_pay_wechat));
                    holder.tv_pay.setText("微信");
                    holder.tv_pay_info.setText("推荐安装微信5.0以上版本的用户使用");
                    if (mSelectedNum == WECHAT_PAY) {
                        holder.iv_pay_select.setVisibility(View.VISIBLE);
                    } else {
                        holder.iv_pay_select.setVisibility(View.INVISIBLE);
                    }
                    break;
                case ACCOUNT_BALANCE:
                    holder.iv_pay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_pay_account));
                    holder.tv_pay.setText("账户余额");
                    // TODO: 2017/8/14 网络请求获得账户余额
                    holder.tv_pay_info.setText("账户余额：0元");
                    if (mSelectedNum == ACCOUNT_BALANCE) {
                        holder.iv_pay_select.setVisibility(View.VISIBLE);
                    } else {
                        holder.iv_pay_select.setVisibility(View.INVISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        class PayListHolder extends RecyclerView.ViewHolder {
            private ImageView iv_pay;
            private TextView tv_pay;
            private TextView tv_pay_info;
            private ImageView iv_pay_select;

            private PayListHolder(View view) {
                super(view);
                iv_pay = (ImageView) view.findViewById(R.id.iv_pay);
                tv_pay = (TextView) view.findViewById(R.id.tv_pay);
                tv_pay_info = (TextView) view.findViewById(R.id.tv_pay_info);
                iv_pay_select = (ImageView) view.findViewById(R.id.iv_pay_select);
            }
        }

        public void setOnItemClickListener(OnClickListener listener) {
            this.onClickListener = listener;
        }

        private OnClickListener onClickListener;
    }

    public interface OnClickListener {
        void onClick(View view, int i);
    }
}
