package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.bean.BLEDevice;
import com.qhiehome.ihome.bean.PublishBean;
import com.qhiehome.ihome.network.model.park.publish.PublishparkRequest;

import java.util.ArrayList;

/**
 * BindLockAdapter recyclerView adapter {@link com.qhiehome.ihome.activity.BindLockActivity}
 */

public class PublishParkingAdapter extends RecyclerView.Adapter<PublishParkingAdapter.PublishParkingHolder>{

    private Context mContext;
    private ArrayList<PublishBean> mPublishList;

    public PublishParkingAdapter(Context context, ArrayList<PublishBean> mPublishList) {
        this.mContext = context;
        this.mPublishList = mPublishList;
    }

    @Override
    public PublishParkingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_publish_parking, parent, false);
        return new PublishParkingHolder(view);
    }

    @Override
    public void onBindViewHolder(final PublishParkingHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        PublishBean requestBean = mPublishList.get(adapterPosition);
        holder.mTvParkingId.setText(requestBean.getParkingId());
        holder.mTvParkingPeriod.setText("发布时间段 " + requestBean.getStartTime() + " ~ " + requestBean.getEndTime());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPublishList == null? 0: mPublishList.size();
    }

    public static class PublishParkingHolder extends RecyclerView.ViewHolder {

        TextView mTvParkingId;
        TextView mTvParkingPeriod;

        private PublishParkingHolder(View itemView) {
            super(itemView);
            mTvParkingId = (TextView) itemView.findViewById(R.id.tv_parking_id);
            mTvParkingPeriod = (TextView) itemView.findViewById(R.id.tv_parking_period);
        }
    }

    public interface OnClickListener {
        void onClick(int i);
    }

    public void setOnItemClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    private OnClickListener onClickListener;
}