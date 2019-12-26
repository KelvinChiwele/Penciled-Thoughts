package com.techart.writersblock.poems;

import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.techart.writersblock.CommentActivity;
import com.techart.writersblock.LikesActivity;
import com.techart.writersblock.R;
import com.techart.writersblock.ScrollingActivity;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Poem;
import com.techart.writersblock.utils.ImageUtils;
import com.techart.writersblock.utils.NumberUtils;
import com.techart.writersblock.utils.TimeUtils;
import com.techart.writersblock.viewholders.ArticleViewHolder;

import java.util.Date;


public class AuthorsPoemsListActivity extends AppCompatActivity {
    private RecyclerView mPoemList;
    private String author;
    private String postTitle;
    private String postContent;

    private boolean mProcessLike = false;
    ProgressBar progressBar;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabrecyclerviewer);

        author = getIntent().getStringExtra("author");
        setTitle(author + "'s poems");
        FireBaseUtils.mDatabaseLike.keepSynced(true);
        FireBaseUtils.mDatabasePoems.keepSynced(true);
        progressBar = findViewById(R.id.pb_loading);
        mPoemList = findViewById(R.id.poem_list);
        mPoemList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AuthorsPoemsListActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mPoemList.setLayoutManager(linearLayoutManager);
        bindView();
    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    private void bindView() {
        FirebaseRecyclerOptions<Poem> response = new FirebaseRecyclerOptions.Builder<Poem>()
                .setQuery(FireBaseUtils.mDatabasePoems.orderByChild(Constants.POST_AUTHOR).equalTo(author), Poem.class)
                                                         .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Poem, ArticleViewHolder>(response) {
            @NonNull
            @Override
            public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_article, parent, false);
                return new ArticleViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ArticleViewHolder viewHolder, int position, @NonNull final Poem model) {
                final String post_key = getRef(position).getKey();
                viewHolder.post_title.setText(model.getTitle());
                progressBar.setVisibility(View.GONE);
                viewHolder.post_author.setText(getString(R.string.article_author,model.getAuthor()));
                viewHolder.setIvImage(AuthorsPoemsListActivity.this, ImageUtils.getPoemUrl());
                if (model.getNumLikes() != null)
                {
                    String count = NumberUtils.shortenDigit(model.getNumLikes());
                    viewHolder.numLikes.setText(count);
                }
                if (model.getNumComments() != null)
                {
                    String count = NumberUtils.shortenDigit(model.getNumComments());
                    viewHolder.numComments.setText(count);
                }
                if (model.getNumViews() != null)
                {
                    String count = NumberUtils.shortenDigit(model.getNumViews());
                    viewHolder.tvNumViews.setText(getString(R.string.viewers,count));
                }
                if (model.getTimeCreated() != null)
                {
                    String time = TimeUtils.timeElapsed(currentTime() - model.getTimeCreated());
                    viewHolder.timeTextView.setText(time);
                }

                viewHolder.setLikeBtn(post_key);
                postContent = model.getPoemText();
                postTitle = model.getTitle();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent readPoemIntent = new Intent(AuthorsPoemsListActivity.this,ScrollingActivity.class);
                        readPoemIntent.putExtra(Constants.POST_CONTENT, postContent);
                        readPoemIntent.putExtra(Constants.POST_TITLE, postTitle);
                        startActivity(readPoemIntent);
                    }
                });

                viewHolder.btnLiked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        FireBaseUtils.mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (mProcessLike) {
                                    if (dataSnapshot.child(post_key).hasChild(Constants.AUTHOR_URL)) {
                                        FireBaseUtils.mDatabaseLike.child(post_key).child(FireBaseUtils.getUiD()).removeValue();
                                        FireBaseUtils.onPoemDisliked(post_key);
                                        mProcessLike = false;
                                    } else {
                                        FireBaseUtils.addPoemLike(model, post_key);
                                        mProcessLike = false;
                                        FireBaseUtils.onPoemLiked(post_key);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                viewHolder.numLikes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(AuthorsPoemsListActivity.this,LikesActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
                viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(AuthorsPoemsListActivity.this,CommentActivity.class);
                        commentIntent.putExtra(Constants.POST_KEY,post_key);
                        commentIntent.putExtra(Constants.POST_TITLE,model.getTitle());
                        commentIntent.putExtra(Constants.POST_TYPE,Constants.POEM_HOLDER);

                        startActivity(commentIntent);
                    }
                });
            }
        };
        mPoemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    private long currentTime()
    {
        Date date = new Date();
        return date.getTime();
    }

    @Override
    public void onBackPressed()
    {
        setResult(RESULT_OK,getIntent());
        finish();
    }
}

