package com.teamproject.gastroventure.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.datainterface.DataImgInterface;
import com.teamproject.gastroventure.vo.ReviewImgVo;

import java.util.ArrayList;

public class ReviewModifyImgAdapter extends RecyclerView.Adapter<ReviewModifyImgAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<ReviewImgVo> list;
    private DataImgInterface dataImgInterface;

    public ReviewModifyImgAdapter(Context context, ArrayList<ReviewImgVo> list, DataImgInterface dataImgInterface) {
        super();
        this.context = context;
        this.list = list;
        this.dataImgInterface = dataImgInterface;
    }

    public ReviewModifyImgAdapter(Context context, ArrayList<ReviewImgVo> list) {
        this.context = context;
        this.list = list;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView menu_image;

        public MyViewHolder(View itemView) {
            super(itemView);
            menu_image = itemView.findViewById(R.id.menu_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("123123","호출호출!!");
                    dataImgInterface.dataImgRemove(getAdapterPosition());
                }
            });
        }
    }

    private void removeItemView(int position) {
        list.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, list.size());
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(list.get(position).getMenu_image())
                .into(holder.menu_image);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
