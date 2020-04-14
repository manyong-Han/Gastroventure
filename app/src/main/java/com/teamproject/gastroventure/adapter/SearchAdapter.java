package com.teamproject.gastroventure.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teamproject.gastroventure.vo.SearchVo;
import com.teamproject.gastroventure.R;
import java.util.ArrayList;

/**
 * Created by 82108 on 2020-03-16.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private ArrayList<SearchVo> searchList;

    public SearchAdapter(ArrayList<SearchVo> searchList) {
        this.searchList = searchList;
    }

    @NonNull
    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item,parent,false);
        SearchViewHolder searchViewHolder = new SearchViewHolder(view);
        return searchViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.SearchViewHolder holder, int position) {
        holder.tv_place_name.setText(searchList.get(position).getPlace_name());
        holder.tv_address.setText(searchList.get(position).getAddress());
        holder.tv_phone.setText(searchList.get(position).getPhone());
        holder.tv_distance.setText(searchList.get(position).getDistance());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {

        TextView tv_place_name;
        TextView tv_address;
        TextView tv_phone;
        TextView tv_distance;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_place_name = itemView.findViewById(R.id.tv_place_name);
            this.tv_address = itemView.findViewById(R.id.tv_address);
            this.tv_phone = itemView.findViewById(R.id.tv_phone);
            this.tv_distance = itemView.findViewById(R.id.tv_distance);
        }
    }
}
