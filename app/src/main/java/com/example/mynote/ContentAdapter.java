package com.example.mynote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

    private final List<Map<String, String>> mContentList;
    private OnItemClickListener mOnItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View ticklerView;
        final TextView contentText;
        final TextView showTime;

        public ViewHolder(View view) {
            super(view);
            ticklerView = view;
            contentText = view.findViewById(R.id.show_content);
            showTime = view.findViewById(R.id.show_time);
        }
    }

    public ContentAdapter(List<Map<String, String>> contentList) {
        mContentList = contentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_item, parent, false);
        //设置子项点击事件，并传递数据到添加页
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.contentText.setText(mContentList.get(position).get("content"));
        holder.showTime.setText(mContentList.get(position).get("time"));
        if (mOnItemClickListener != null) {
            holder.ticklerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(position);
                }
            });
            holder.ticklerView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onLongClick(position);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mContentList.size();
    }

    // 设置点击和长按事件
    public interface OnItemClickListener {
        // 点击
        void onClick(int position);

        // 长按
        void onLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
}