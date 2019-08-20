package com.techart.writersblock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Notice;
import com.techart.writersblock.utils.TimeUtils;
import com.techart.writersblock.viewholders.NoticeViewHolder;


public class LikesActivity extends AppCompatActivity
{
    private String postKey;
    private RecyclerView mLikeList;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    int count;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_like);
        setTitle("Likes");
        postKey = getIntent().getStringExtra(Constants.POST_KEY);
        count = getIntent().getIntExtra(Constants.NUM_VIEWS, 0);
        FireBaseUtils.mDatabaseLike.child(postKey).keepSynced(true);
        mLikeList = findViewById(R.id.lv_notice);
        mLikeList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mLikeList.setLayoutManager(linearLayoutManager);
        progressBar = findViewById(R.id.pb_loading);
        tvEmpty = findViewById(R.id.tv_empty);
        tvEmpty.setText("No likes yet, be the first to like");
        if (count == 0) {
            progressBar.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        bindView();
    }

    private void bindView() {
        Query likeQuery = FireBaseUtils.mDatabaseLike.child(postKey).orderByChild(Constants.TIME_CREATED);
        FirebaseRecyclerOptions<Notice> response = new FirebaseRecyclerOptions.Builder<Notice>()
                                                           .setQuery(likeQuery, Notice.class)
                                                           .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Notice, NoticeViewHolder>(response) {
            @NonNull
            @Override
            public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.list_view, parent, false);
                return new NoticeViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NoticeViewHolder viewHolder, int position, @NonNull final Notice model) {
                if (count != 0) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
                String time = TimeUtils.timeElapsed(model.getTimeCreated());
                viewHolder.tvUser.setText(getString(R.string.liked,model.getUser(),model.getPostTitle()));
                viewHolder.tvTime.setText(time);
            }
        };
        mLikeList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }
}

