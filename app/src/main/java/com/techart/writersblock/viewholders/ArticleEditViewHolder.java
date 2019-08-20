package com.techart.writersblock.viewholders;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.techart.writersblock.R;
import com.techart.writersblock.constants.FireBaseUtils;

/**
 * Created by kelvin on 2/12/18.
 */


public final class ArticleEditViewHolder extends RecyclerView.ViewHolder
{
    public TextView post_title;
    public TextView numLikes;
    public TextView numComments;
    private TextView tvNumViews;
    public TextView tvTimeCreated;

    public View mView;

    private DatabaseReference mDatabaseLike;

    public Button btEdit;
    public ImageButton btnLiked;
    public ImageButton btnDelete;
    public ImageButton btnComment;
    public ImageButton btnViews;

    public ArticleEditViewHolder(View itemView) {
        super(itemView);
        post_title = itemView.findViewById(R.id.post_title);
        btnLiked = itemView.findViewById(R.id.likeBtn);
        btnDelete = itemView.findViewById(R.id.im_del);
        btnComment = itemView.findViewById(R.id.commentBtn);
        tvNumViews = itemView.findViewById(R.id.tv_numviews);
        btnViews = itemView.findViewById(R.id.bt_views);
        numLikes = itemView.findViewById(R.id.tv_likes);
        numComments = itemView.findViewById(R.id.tv_comments);

        tvTimeCreated = itemView.findViewById(R.id.tvTime);
        btEdit = itemView.findViewById(R.id.bt_edit);

        this.mView = itemView;
        mDatabaseLike = FireBaseUtils.mDatabaseLike;
        mDatabaseLike.keepSynced(true);
    }


    public void setLikeBtn(String post_key) {
        FireBaseUtils.setLikeBtn(post_key,btnLiked);
    }
}
