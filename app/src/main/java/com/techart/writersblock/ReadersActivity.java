package com.techart.writersblock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Users;
import com.techart.writersblock.utils.TimeUtils;
import com.techart.writersblock.viewholders.NoticeViewHolder;

/**
 * Retrieves and displays list of people who have viewed a particular post
 */
public class ReadersActivity extends AppCompatActivity
{
    String title;
    private RecyclerView mPoemList;
    private ProgressBar progressBar;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_like);
        setTitle("Readers");
        FireBaseUtils.mDatabaseUsers.keepSynced(true);
        mPoemList = findViewById(R.id.lv_notice);
        progressBar = findViewById(R.id.pb_loading);

        mPoemList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ReadersActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mPoemList.setLayoutManager(linearLayoutManager);
        bindView();
    }

    /**
     * Binds the view to a listview
     */
    private void bindView() {
        //ToDo fully implement class and method
        Query viewQuery = FireBaseUtils.mDatabaseUsers.orderByChild(Constants.SIGNED_IN_AS).equalTo("Reader");
        FirebaseRecyclerOptions<Users> response = new FirebaseRecyclerOptions.Builder<Users>()
                                                          .setQuery(viewQuery, Users.class)
                                                          .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, NoticeViewHolder>(response) {
            @NonNull
            @Override
            public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.list_view, parent, false);
                return new NoticeViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NoticeViewHolder viewHolder, int position, @NonNull final Users model) {
                progressBar.setVisibility(View.GONE);
                if (model.getTimeCreated() != null){
                    String time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.tvTime.setText(time);
                }
                viewHolder.tvUser.setText(model.getName());
            }
        };
        mPoemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }
}

