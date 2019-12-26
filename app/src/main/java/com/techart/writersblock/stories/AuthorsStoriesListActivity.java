package com.techart.writersblock.stories;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.techart.writersblock.ActivityRead;
import com.techart.writersblock.AuthorsProfileActivity;
import com.techart.writersblock.CommentActivity;
import com.techart.writersblock.LikesActivity;
import com.techart.writersblock.R;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Chapter;
import com.techart.writersblock.models.Chapters;
import com.techart.writersblock.models.Story;
import com.techart.writersblock.utils.ImageUtils;
import com.techart.writersblock.utils.TimeUtils;
import com.techart.writersblock.viewholders.StoryViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.techart.writersblock.constants.FireBaseUtils.mDatabaseStory;


public class AuthorsStoriesListActivity extends AppCompatActivity {
    private RecyclerView mPoemList;
    private ArrayList<String> contents;
    private int pageCount;
    private String author;
    private Long timeAccessed;

    private boolean mProcessLike = false;
    private boolean mProcessView = false;
    ProgressBar progressBar;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorsstorylist);

        author = getIntent().getStringExtra("author");
        setTitle(author + "'s stories");
        FireBaseUtils.mDatabaseLike.keepSynced(true);
        mDatabaseStory.keepSynced(true);
        progressBar = findViewById(R.id.pb_loading);
        mPoemList = findViewById(R.id.poem_list);
        mPoemList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AuthorsStoriesListActivity.this);
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
        FirebaseRecyclerOptions<Story> response = new FirebaseRecyclerOptions.Builder<Story>()
                .setQuery(mDatabaseStory.orderByChild(Constants.AUTHOR_URL).equalTo(FireBaseUtils.getUiD()), Story.class)
                                                          .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Story, StoryViewHolder>(response) {
            @NonNull
            @Override
            public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_storyrow, parent, false);
                return new StoryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull StoryViewHolder viewHolder, int position, @NonNull final Story model) {
                final String post_key = getRef(position).getKey();
                progressBar.setVisibility(View.GONE);
                viewHolder.tvTitle.setText(model.getTitle());
                viewHolder.tvCategory.setText(getString(R.string.post_category,model.getCategory()));
                viewHolder.tvStatus.setText(model.getStatus());

                viewHolder.tvAuthor.setText(getString(R.string.post_author,model.getAuthor()));
                if (model.getNumLikes() != null)
                {
                    viewHolder.tvNumLikes.setText(String.format("%s",model.getNumLikes().toString()));
                }
                if (model.getNumComments() != null)
                {
                    viewHolder.tvNumComments.setText(String.format("%s",model.getNumComments().toString()));
                }if (model.getNumViews() != null)
                {
                    viewHolder.tvNumViews.setText(String.format("%s",model.getNumViews().toString()));
                }
                if (model.getTimeCreated() != null)
                {
                    String time = TimeUtils.timeElapsed(TimeUtils.currentTime() - model.getTimeCreated());
                    viewHolder.tvTime.setText(time);
                }

                if (model.getImageUrl() != null) {
                    viewHolder.setIvImage(AuthorsStoriesListActivity.this,model.getImageUrl());
                } else {
                    viewHolder.setIvImage(AuthorsStoriesListActivity.this, ImageUtils.getStoryUrl(model.getCategory().trim()));
                }


                viewHolder.tvAuthor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent readPoemIntent = new Intent(AuthorsStoriesListActivity.this,AuthorsProfileActivity.class);
                        readPoemIntent.putExtra(Constants.POST_AUTHOR, model.getAuthor());
                        startActivity(readPoemIntent);
                    }
                });

                viewHolder.setLikeBtn(post_key);
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addToViews(model.getDescription(), post_key, model);
                    }
                });

                viewHolder.btnLiked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        FireBaseUtils.mDatabaseChapters.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.child(post_key).hasChild(Constants.AUTHOR_URL))  {
                                        FireBaseUtils.mDatabaseChapters.child(post_key).child(FireBaseUtils.getUiD()).removeValue();
                                        FireBaseUtils.onStoryDisliked(post_key);
                                        mProcessLike = false;
                                    } else {
                                        FireBaseUtils.addStoryLike(model,post_key);
                                        mProcessLike = false;
                                        FireBaseUtils.onStoryLiked(post_key);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                viewHolder.tvNumLikes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(AuthorsStoriesListActivity.this,LikesActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
                viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(AuthorsStoriesListActivity.this,CommentActivity.class);
                        commentIntent.putExtra(Constants.POST_KEY,post_key);
                        commentIntent.putExtra(Constants.POST_TITLE,model.getTitle());
                        commentIntent.putExtra(Constants.POST_TYPE,Constants.STORY_HOLDER);
                        startActivity(commentIntent);
                    }
                });
            }
        };
        mPoemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }


    private void addToViews(final String description, final String post_key, final Story model) {
        mProcessView = true;
        FireBaseUtils.mDatabaseViews.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mProcessView) {
                    if (dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
                        mProcessView = false;
                        initializeChapters(post_key, model);
                    } else if (description.isEmpty()) {
                        mProcessView = false;
                        FireBaseUtils.addStoryView(model, post_key);
                        FireBaseUtils.onStoryViewed(post_key);
                        initializeChapters(post_key, model);
                    } else {
                        mProcessView = false;
                        showDescription(description, post_key, model);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeChapters(String post_key, Story model) {
        FireBaseUtils.mDatabaseChapters.child(post_key).keepSynced(true);
        contents = new ArrayList<>();
        addToLibrary(model, post_key);
        loadChapters(model.getCategory().trim(), post_key);
    }


    private void loadChapters(String status, final String post_key) {
        final ProgressDialog progressDialog = new ProgressDialog(AuthorsStoriesListActivity.this);
        progressDialog.setMessage("Loading chapters");
        progressDialog.setCancelable(true);
        progressDialog.show();

        FireBaseUtils.mDatabaseChapters.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pageCount = ((int) dataSnapshot.getChildrenCount());
                for (DataSnapshot chapterSnapShot : dataSnapshot.getChildren()) {
                    Chapter chapter = chapterSnapShot.getValue(Chapter.class);
                    contents.add(chapter.getContent());
                }
                if (contents.size() == pageCount) {
                    progressDialog.dismiss();
                    Chapters chapters = Chapters.getInstance();
                    chapters.setChapters(contents);
                    Intent readIntent = new Intent(AuthorsStoriesListActivity.this, ActivityRead.class);
                    readIntent.putExtra(Constants.POST_KEY, post_key);
                    startActivity(readIntent);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void addToLibrary(final Story model, final String post_key) {
        FireBaseUtils.mDatabaseLibrary.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(FireBaseUtils.getUiD()).hasChild(post_key)) {
                    Map<String, Object> values = new HashMap<>();
                    values.put(Constants.POST_KEY, post_key);
                    values.put(Constants.POST_TITLE, model.getTitle());
                    values.put(Constants.CHAPTER_ADDED, 0);
                    values.put("lastAccessed", timeAccessed);
                    FireBaseUtils.mDatabaseLibrary.child(FireBaseUtils.getUiD()).child(post_key).setValue(values);
                    Toast.makeText(AuthorsStoriesListActivity.this, model.getTitle() + " added to library", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void showDescription(String description, final String post_key, final Story model) {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            FireBaseUtils.addStoryView(model, post_key);
                            FireBaseUtils.onStoryViewed(post_key);
                            initializeChapters(post_key, model);
                        } else {
                            dialog.dismiss();
                        }
                    }
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(AuthorsStoriesListActivity.this);
        builder.setMessage(description)
        .setPositiveButton("Start Reading", dialogClickListener)
        .setNegativeButton("Back", dialogClickListener)
        .show();
    }
    @Override
    public void onBackPressed() {
        setResult(RESULT_OK,getIntent());
        finish();
    }
}

