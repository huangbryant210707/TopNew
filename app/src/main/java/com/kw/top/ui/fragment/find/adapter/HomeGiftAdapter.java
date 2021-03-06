package com.kw.top.ui.fragment.find.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.kw.top.R;
import com.kw.top.retrofit.HttpHost;
import com.kw.top.tools.GlideTools;
import com.kw.top.ui.fragment.find.bean.HomeInfoBean;
import com.kw.top.utils.DisplayUtils;
import com.kw.top.utils.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by shibing on 2018/9/24.
 */

public class HomeGiftAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context mContext;
    private List<HomeInfoBean.GiftListBean> list;
    private int maxWidth;
    private OnItemClickListener listener;

    public HomeGiftAdapter(Context mContext, List<HomeInfoBean.GiftListBean> list) {
        this.mContext = mContext;
        this.list = list;
        maxWidth = DisplayUtils.getScreenWidth(mContext) - DisplayUtils.dip2px(mContext, 20);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.homegift_item, null, false);
        return new ViewHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final ViewHodler viewHodler = (ViewHodler) holder;
        if (listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(position);
                }
            });
        }
        Glide.with(mContext)
                .asBitmap()
                .load(HttpHost.qiNiu + list.get(position).getHeadImg())
                .apply(GlideTools.getOptions())
                .into(viewHodler.imageView);
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }


    public class ViewHodler extends RecyclerView.ViewHolder {
        @BindView(R.id.gift_item)
        CircleImageView imageView;


        public ViewHodler(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    public void addOnItemOnClick(OnItemClickListener listener) {
        this.listener = listener;
    }


}
