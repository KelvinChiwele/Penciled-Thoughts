package com.techart.writersblock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Notice;
import com.techart.writersblock.models.Users;
import com.techart.writersblock.utils.TimeUtils;
import com.techart.writersblock.viewholders.NoticeViewHolder;

import static android.widget.Toast.LENGTH_LONG;
import static com.techart.writersblock.constants.Constants.STAMP_KEY;

/**
 * Holds notification and news fragments
 */
public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView rvNotice;
    private TextView tvEmpty;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);
        rvNotice = findViewById(R.id.rv_news);
        rvNotice.setHasFixedSize(true);
        int lastAccessedPage = getIntent().getIntExtra(Constants.STAMP_KEY, 0);
        setTimeAccessed(lastAccessedPage);
        FireBaseUtils.mDatabaseNotifications.keepSynced(true);
        tvEmpty = findViewById(R.id.tv_empty);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvNotice.setLayoutManager(linearLayoutManager);
        //setView();
        writerView();
    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    private void setView() {
        FireBaseUtils.mDatabaseUsers.child(FireBaseUtils.getUiD()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                if (user != null){
                    switch (user.getSignedAs().trim()) {
                        case "Writer": {
                            writerView();
                            break;
                        }
                        case "Reader": {
                            readersView();
                            break;
                        }
                        default:
                            Toast.makeText(NotificationsActivity.this, "Could not open notifications " + user.getSignedAs(), LENGTH_LONG).show();
                            break;
                    }
                } else {
                    Toast.makeText(NotificationsActivity.this,"Error...! Try later",LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setTimeAccessed(int lastAccessedPage) {
        SharedPreferences mPref = getSharedPreferences(String.format("%s", getString(R.string.app_name)), MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(STAMP_KEY,lastAccessedPage);
        editor.apply();
    }

    private void readersView() {
        FirebaseRecyclerOptions<Notice> response = new FirebaseRecyclerOptions.Builder<Notice>()
                                                           .setQuery(FireBaseUtils.mDatabaseNotifications.orderByChild(Constants.SIGNED_IN_AS).equalTo("Reader"), Notice.class)
                                                           .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Notice, NoticeViewHolder>(response) {
            @NonNull
            @Override
            public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_notifications, parent, false);
                return new NoticeViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NoticeViewHolder viewHolder, int position, @NonNull final Notice model) {
                viewHolder.makePortionBold(model.getUser() + " " + model.getAction() + model.getPostTitle(), model.getUser());
                tvEmpty.setVisibility(View.GONE);
                if (model.getTimeCreated() != null) {
                    String time = TimeUtils.timeElapsed( model.getTimeCreated());
                    viewHolder.tvTime.setText(time);
                }

                if (model.getImageUrl() != null && !model.getImageUrl().equals("default")) {
                    viewHolder.setIvImage(NotificationsActivity.this, model.getImageUrl());
                } else {
                    viewHolder.setIvImage(NotificationsActivity.this, R.drawable.placeholder);
                }
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectIntent(model);
                    }
                });
            }
        };
        rvNotice.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }


    private void writerView() {
        FirebaseRecyclerOptions<Notice> response = new FirebaseRecyclerOptions.Builder<Notice>()
                                                            .setQuery(FireBaseUtils.mDatabaseNotifications.orderByChild(Constants.SIGNED_IN_AS).equalTo("Writer"), Notice.class)
                                                            .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Notice, NoticeViewHolder>(response) {
            @NonNull
            @Override
            public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_notifications, parent, false);
                return new NoticeViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NoticeViewHolder viewHolder, int position, @NonNull final Notice model) {
                viewHolder.makePortionBold(model.getUser() + " " + model.getAction() + model.getPostTitle(), model.getUser());
                tvEmpty.setVisibility(View.GONE);
                if (model.getTimeCreated() != null) {
                    String time = TimeUtils.timeElapsed( model.getTimeCreated());
                    viewHolder.tvTime.setText(time);
                }

                if (model.getImageUrl() != null && !model.getImageUrl().equals("default")) {
                    viewHolder.setIvImage(NotificationsActivity.this, model.getImageUrl());
                } else {
                    viewHolder.setIvImage(NotificationsActivity.this, R.drawable.placeholder);
                }
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectIntent(model);
                    }
                });
            }
        };
        rvNotice.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }



    /**
     * Resolves activity to start
     *
     * @param notice name of category clicked
     */
    private void selectIntent(Notice notice) {
        Intent readPoemIntent;
        if (notice.getPostType().equals("story") || notice.getPostType().equals("poem") || notice.getPostType().equals("devotion")) {
            readPoemIntent = new Intent(NotificationsActivity.this, CommentActivity.class);
            readPoemIntent.putExtra(Constants.POST_KEY, notice.getPostKey());
            readPoemIntent.putExtra(Constants.POST_TITLE, notice.getPostTitle());
            readPoemIntent.putExtra(Constants.POST_TYPE, notice.getPostType());
            startActivity(readPoemIntent);
        } else  if (notice.getPostType().equals("writersInbox")) {
            readPoemIntent = new Intent(NotificationsActivity.this, WritersChatRoomActivity.class);
            startActivity(readPoemIntent);
        } else  if (notice.getPostType().equals("generalInbox")) {
            readPoemIntent = new Intent(NotificationsActivity.this, GeneralChatRoomActivity.class);
            startActivity(readPoemIntent);
        } else {
            Toast.makeText(NotificationsActivity.this, "Could not open new screen", Toast.LENGTH_LONG).show();
        }
    }

    private void selectInten2(Notice notice) {
        Intent readPoemIntent;
        if (notice.getPostType().equals("story") || notice.getPostType().equals("poem") || notice.getPostType().equals("devotion")) {
            readPoemIntent = new Intent(NotificationsActivity.this, CommentActivity.class);
            readPoemIntent.putExtra(Constants.POST_KEY, notice.getPostKey());
            readPoemIntent.putExtra(Constants.POST_TITLE, notice.getPostTitle());
            readPoemIntent.putExtra(Constants.POST_TYPE, notice.getPostType());
            startActivity(readPoemIntent);
        } else  if (notice.getPostType().equals("writersInbox")) {
            readPoemIntent = new Intent(NotificationsActivity.this, WritersChatRoomActivity.class);
            startActivity(readPoemIntent);
        } else  if (notice.getPostType().equals("generalInbox")) {
            readPoemIntent = new Intent(NotificationsActivity.this, GeneralChatRoomActivity.class);
            startActivity(readPoemIntent);
        } else {
            Toast.makeText(NotificationsActivity.this, "Could not open new screen", Toast.LENGTH_LONG).show();
        }
    }
}
