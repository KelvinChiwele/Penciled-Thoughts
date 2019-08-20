package com.techart.writersblock.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.techart.writersblock.R;

public final class NoticeViewHolder extends RecyclerView.ViewHolder {
    public TextView tvUser;
    public TextView tvTime;
    public View mView;
    public ImageView ivImage;
    public TextView tvNotice;

    public NoticeViewHolder(View itemView) {
        super(itemView);
        tvTime = itemView.findViewById(R.id.tv_time);
        tvUser = itemView.findViewById(R.id.tv_user);
        tvNotice = itemView.findViewById(R.id.tv_notifications);
        ivImage = itemView.findViewById(R.id.iv_disease);
        this.mView = itemView;
    }

    public void setIvImage(Context context, String image)  {
        Glide.with(context)
                .load(image)
                .apply(RequestOptions.circleCropTransform())
                .into(ivImage);
    }

    public void setIvImage(Context context, int image) {
        Glide.with(context)
                .load(image)
                .apply(RequestOptions.circleCropTransform())
                .into(ivImage);
    }

    public void makePortionBold(String text, String spanText) {
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        int start = text.indexOf(spanText);
        int end = start + spanText.length();
        sb.setSpan(boldStyle, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        tvNotice.setText(sb);
    }
}