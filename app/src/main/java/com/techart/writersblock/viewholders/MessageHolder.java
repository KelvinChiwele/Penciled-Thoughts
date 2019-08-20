package com.techart.writersblock.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techart.writersblock.R;

public final class MessageHolder extends RecyclerView.ViewHolder {
        public TextView authorTextView;
        public TextView commentTextView;
        public TextView timeTextView;
        public ImageView ivSample;
        public View itemView;

        public MessageHolder(View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.tvAuthor);
            timeTextView = itemView.findViewById(R.id.tvTime);
            commentTextView = itemView.findViewById(R.id.tvComment);
            ivSample = itemView.findViewById(R.id.iv_sample);
            this.itemView = itemView;
        }

        public void setImage(Context context, String image) {
            Glide.with(context)
                    .load(image)
                    .into(ivSample);
        }
    }