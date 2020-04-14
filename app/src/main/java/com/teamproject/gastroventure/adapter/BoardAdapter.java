package com.teamproject.gastroventure.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.datainterface.DataInterface;
import com.teamproject.gastroventure.vo.BoardVo;

import java.util.ArrayList;

/**
 * Created by 82108 on 2020-03-16.
 */
public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {

    ArrayList<BoardVo> arrayList;
    Context context;
    DataInterface dataInterface;


    public BoardAdapter(ArrayList<BoardVo> arrayList, Context context, DataInterface dataInterface) {
        this.arrayList = arrayList;
        this.context = context;
        this.dataInterface = dataInterface;
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_item, parent, false);
        BoardViewHolder holder = new BoardViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, final int position) {
        holder.write_user.setText(arrayList.get(position).getWrite_user());
        holder.board_num.setText(String.valueOf(holder.getAdapterPosition()+1));
        holder.board_title.setText(arrayList.get(position).getBoard_title());
        holder.board_date.setText(arrayList.get(position).getBoard_date());

    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class BoardViewHolder extends RecyclerView.ViewHolder {
        TextView write_user;
        TextView board_num;
        TextView board_title;
        TextView board_date;


        public BoardViewHolder(@NonNull final View itemView) {
            super(itemView);
            this.write_user = itemView.findViewById(R.id.write_uesr);
            this.board_num = itemView.findViewById(R.id.board_num);
            this.board_title = itemView.findViewById(R.id.board_title);
            this.board_date = itemView.findViewById(R.id.board_date);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String key = arrayList.get(getAdapterPosition()).getBoard_key();
                    dataInterface.dataDetail(key);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String key = arrayList.get(getAdapterPosition()).getBoard_key();
                    dataInterface.dataRemove(key, getAdapterPosition());
                    return false;
                }
            });

        }
    }
}