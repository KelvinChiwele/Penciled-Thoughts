package com.techart.writersblock.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.techart.writersblock.R;
import com.techart.writersblock.constants.FireBaseUtils;

/**
 * ViewHolder for Articles: Poems & devotion
 * Created by kelvin on 2/12/18.
 */

public final class ArticleViewHolder extends RecyclerView.ViewHolder
{
    public TextView post_title;
    public TextView numLikes;
    public TextView numComments;
    public TextView tvNumViews;
    public ImageView ivArticle;

    public TextView timeTextView;
    public View mView;

    public DatabaseReference mDatabaseLike;
    FirebaseAuth mAUth;

    public TextView post_author;
    public ImageButton btnLiked;
    public ImageButton btnDelete;
    public ImageButton btnComment;
    public ImageButton btnViews;

    public ArticleViewHolder(View itemView) {
        super(itemView);
        post_title = itemView.findViewById(R.id.post_title);
        post_author = itemView.findViewById(R.id.post_author);

        ivArticle = itemView.findViewById(R.id.iv_news);

        timeTextView = itemView.findViewById(R.id.tvTime);
        btnLiked = itemView.findViewById(R.id.likeBtn);
        btnDelete = itemView.findViewById(R.id.im_del);
        btnComment = itemView.findViewById(R.id.commentBtn);
        tvNumViews = itemView.findViewById(R.id.tv_numviews);
        btnViews = itemView.findViewById(R.id.bt_views);
        numLikes = itemView.findViewById(R.id.tv_likes);
        numComments = itemView.findViewById(R.id.tv_comments);

        this.mView = itemView;
        mDatabaseLike = FireBaseUtils.mDatabaseLike;
        mAUth = FirebaseAuth.getInstance();
    }

    public void setIvImage(Context context, int image)
    {
        Glide.with(context)
                .load(image)
                .into(ivArticle);
    }
    public void setTint(Context context){
        ivArticle.setColorFilter(ContextCompat.getColor(context, R.color.colorTint));
    }
    public void setLikeBtn(String post_key) {
        FireBaseUtils.setLikeBtn(post_key,btnLiked);
    }
    public void setPostViewed(String post_key) {
        FireBaseUtils.setPostViewed(post_key,btnViews);
    }
}
