package com.techart.writersblock.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.techart.writersblock.R;

public final class ChapterViewHolder extends RecyclerView.ViewHolder {
    public TextView tvTitle;
    public TextView tvTime;
    public ImageView btDelete;
    public View mView;

    public ChapterViewHolder(View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tvTitle);
        tvTime = itemView.findViewById(R.id.tv_timeCreated);
        btDelete = itemView.findViewById(R.id.iv_delete);
        this.mView = itemView;
    }
}