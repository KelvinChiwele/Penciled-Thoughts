package com.techart.writersblock.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.techart.writersblock.R;
import com.techart.writersblock.constants.FireBaseUtils;

/**
 * Created by kelvin on 2/12/18.
 */

public final class StoryViewHolder extends RecyclerView.ViewHolder {
    public TextView tvTitle;
    public TextView tvState;
    public TextView tvAuthor;
    public TextView tvCategory;
    public TextView tvStatus;
    public TextView tvChapters;
    public TextView tvNumLikes;
    public TextView tvNumComments;
    public TextView tvNumViews;
    public TextView tvTime;
    public ImageView ivStory;
    public View mView;
    public DatabaseReference mDatabaseLike;
    public ImageButton btnDelete;
    public ImageButton btnLiked;
    public ImageButton btnComment;
    public ImageButton btnViews;

    public StoryViewHolder(View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tv_title);
        tvState = itemView.findViewById(R.id.tv_state);
        tvAuthor = itemView.findViewById(R.id.tv_author);
        tvStatus = itemView.findViewById(R.id.tv_status);
        tvChapters = itemView.findViewById(R.id.tv_chapters);
        tvCategory = itemView.findViewById(R.id.tv_category);
        ivStory = itemView.findViewById(R.id.iv_news);

        btnDelete = itemView.findViewById(R.id.im_delete);
        btnLiked = itemView.findViewById(R.id.likeBtn);
        btnComment = itemView.findViewById(R.id.commentBtn);
        btnViews = itemView.findViewById(R.id.bt_views);
        tvTime = itemView.findViewById(R.id.tv_time);
        tvNumLikes = itemView.findViewById(R.id.tv_numlikes);
        tvNumComments = itemView.findViewById(R.id.tv_numcomments);
        tvNumViews = itemView.findViewById(R.id.tv_numviews);
        this.mView = itemView;
        mDatabaseLike = FireBaseUtils.mDatabaseLike;
        mDatabaseLike.keepSynced(true);
    }

    public void setTint(Context context){
        ivStory.setColorFilter(ContextCompat.getColor(context, R.color.colorTint));
    }

    public void setVisibility(Boolean isVisible) {
        if (isVisible){
            tvState.setVisibility(View.VISIBLE);
        }else{
            tvState.setVisibility(View.INVISIBLE);
        }
    }

    public void setIvImage(Context context, int resourceValue) {
        Glide.with(context)
                .load(resourceValue)
                .into(ivStory);
    }

    public void setIvImage(Context context, String imageUrl) {
        Glide.with(context)
        .load(imageUrl)
        .into(ivStory);
    }
    public void setLikeBtn(String post_key) {
        FireBaseUtils.setLikeBtn(post_key,btnLiked);
    }
    public void setPostViewed(String post_key) {
        FireBaseUtils.setPostViewed(post_key,btnViews);
    }
}
