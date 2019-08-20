package com.techart.writersblock;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Chapter;
import com.techart.writersblock.models.Chapters;
import com.techart.writersblock.models.Devotion;
import com.techart.writersblock.models.Poem;
import com.techart.writersblock.models.Story;
import com.techart.writersblock.utils.ImageUtils;
import com.techart.writersblock.utils.NumberUtils;
import com.techart.writersblock.utils.TimeUtils;
import com.techart.writersblock.viewholders.ArticleViewHolder;
import com.techart.writersblock.viewholders.StoryViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    private boolean mProcessLike = false;
    private boolean mProcessView = false;
    private EditText etSearch;
    private Long timeAccessed;
    private RecyclerView rvSearchResults;
    private String searchText;
    private ArrayList<String> contents= new ArrayList<>(Arrays.asList(Constants.STORY_HOLDER, Constants.POEM_HOLDER, Constants.DEVOTION_HOLDER));
    private Query storyRef;
    private Query articleRef;
    private int pageCount;
    private String postType;
    private AlertDialog updateDialog;
    private final String[] categories = {Constants.STORY_HOLDER, Constants.POEM_HOLDER, Constants.DEVOTION_HOLDER};
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        etSearch = findViewById(R.id.et_search);
        rvSearchResults = findViewById(R.id.rv_search);
        ImageView imFilter = findViewById(R.id.iv_filter);
        ImageView imBack = findViewById(R.id.iv_back);
        rvSearchResults.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvSearchResults.setLayoutManager(linearLayoutManager);

        imBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectSearchField();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                searchText = etSearch.getText().toString().trim();
                if (searchText.isEmpty() && postType != null){
                    initSearchFor();
                } else if (searchText != null && postType != null) {
                    firebaseSearch();
                } else if (postType != null){
                    Toast.makeText(SearchActivity.this, "Unable to initialize search",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void initSearchFor() {
        switch (postType) {
            case Constants.POEM_HOLDER:
                articleRef = FireBaseUtils.mDatabasePoems.orderByChild(Constants.TIME_CREATED);
                firebaseArticleSearch();
                break;
            case Constants.DEVOTION_HOLDER:
                articleRef = FireBaseUtils.mDatabaseDevotions.orderByChild(Constants.TIME_CREATED);
                firebaseDevotionSearch();
                break;
            case Constants.STORY_HOLDER:
                storyRef = FireBaseUtils.mDatabaseStory.orderByChild(Constants.TIME_CREATED);
                firebaseStorySearch();
                break;
        }
    }

    private void firebaseSearch() {
        if (postType.equals(Constants.STORY_HOLDER )){
            storyRef = FireBaseUtils.mDatabaseStory.orderByChild("title").startAt(searchText).endAt(searchText + "\uf8ff");
            firebaseStorySearch();
        } else {
            articleRef = searchForArticleWith();
            firebaseArticleSearch();
        }
    }

    private Query searchForArticleWith(){
        if (postType.equals(Constants.DEVOTION_HOLDER)){
            return FireBaseUtils.mDatabaseDevotions.orderByChild("title").startAt(searchText).endAt(searchText + "\uf8ff");
        } else {
            return FireBaseUtils.mDatabasePoems.orderByChild("title").startAt(searchText).endAt(searchText + "\uf8ff");
        }
    }

    /*
    private void firebaseArticleSearch() {
        FirebaseRecyclerAdapter<Poem,ArticleViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Poem, ArticleViewHolder>(
                Poem.class,R.layout.item_article,ArticleViewHolder.class, articleRef) {
            @Override
            protected void populateViewHolder(ArticleViewHolder viewHolder, final Poem model, int position) {
                final String post_key = getRef(position).getKey();
                viewHolder.post_title.setText(model.getTitle());
                viewHolder.setTint(SearchActivity.this);
                viewHolder.post_author.setText(getString(R.string.article_author,model.getAuthor()));
                viewHolder.setIvImage(SearchActivity.this, ImageUtils.getPoemUrl());
                if (model.getNumLikes() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumLikes());
                    viewHolder.numLikes.setText(count);
                }
                if (model.getNumComments() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumComments());
                    viewHolder.numComments.setText(count);
                }
                if (model.getNumViews() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumViews());
                    viewHolder.tvNumViews.setText(getString(R.string.viewers,count));
                }
                if (model.getTimeCreated() != null) {
                    String time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.timeTextView.setText(time);
                }
                viewHolder.setLikeBtn(post_key);
                viewHolder.setPostViewed(post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessView = true;
                        FireBaseUtils.mDatabaseViews.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessView) {
                                    if (!dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
                                        FireBaseUtils.addPoemView(model,post_key);
                                        mProcessView = false;
                                        FireBaseUtils.onPoemViewed(post_key);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        Intent readPoemIntent = new Intent(SearchActivity.this,ScrollingActivity.class);
                        readPoemIntent.putExtra(Constants.POST_CONTENT, model.getPoemText());
                        readPoemIntent.putExtra(Constants.POST_TITLE, model.getTitle());
                        startActivity(readPoemIntent);
                    }
                });

                viewHolder.post_author.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent readPoemIntent = new Intent(SearchActivity.this,AuthorsProfileActivity.class);
                        readPoemIntent.putExtra(Constants.POST_AUTHOR, model.getAuthor());
                        startActivity(readPoemIntent);
                    }
                });

                viewHolder.btnLiked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        FireBaseUtils.mDatabaseLike.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
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
                        Intent likedPostsIntent = new Intent(SearchActivity.this,LikesActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
                viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(SearchActivity.this,CommentActivity.class);
                        commentIntent.putExtra(Constants.POST_KEY,post_key);
                        commentIntent.putExtra(Constants.POST_TITLE,model.getTitle());
                        commentIntent.putExtra(Constants.POST_TYPE,Constants.POEM_HOLDER);
                        startActivity(commentIntent);
                    }
                });

                viewHolder.btnViews.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(SearchActivity.this,ViewsActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
            }
        };
        rvSearchResults.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }*/

    private void firebaseArticleSearch() {
        FirebaseRecyclerOptions<Poem> response = new FirebaseRecyclerOptions.Builder<Poem>()
                                                             .setQuery(articleRef, Poem.class)
                                                             .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Poem, ArticleViewHolder>(response) {
            @NonNull
            @Override
            public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_storyrow, parent, false);
                return new ArticleViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ArticleViewHolder viewHolder, int position, @NonNull final Poem model) {
                final String post_key = getRef(position).getKey();
                viewHolder.post_title.setText(model.getTitle());
                viewHolder.setTint(SearchActivity.this);
                viewHolder.post_author.setText(getString(R.string.article_author,model.getAuthor()));
                viewHolder.setIvImage(SearchActivity.this, ImageUtils.getPoemUrl());
                if (model.getNumLikes() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumLikes());
                    viewHolder.numLikes.setText(count);
                }
                if (model.getNumComments() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumComments());
                    viewHolder.numComments.setText(count);
                }
                if (model.getNumViews() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumViews());
                    viewHolder.tvNumViews.setText(getString(R.string.viewers,count));
                }
                if (model.getTimeCreated() != null) {
                    String time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.timeTextView.setText(time);
                }
                viewHolder.setLikeBtn(post_key);
                viewHolder.setPostViewed(post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessView = true;
                        FireBaseUtils.mDatabaseViews.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessView) {
                                    if (!dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
                                        FireBaseUtils.addPoemView(model,post_key);
                                        mProcessView = false;
                                        FireBaseUtils.onPoemViewed(post_key);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        Intent readPoemIntent = new Intent(SearchActivity.this,ScrollingActivity.class);
                        readPoemIntent.putExtra(Constants.POST_CONTENT, model.getPoemText());
                        readPoemIntent.putExtra(Constants.POST_TITLE, model.getTitle());
                        startActivity(readPoemIntent);
                    }
                });

                viewHolder.post_author.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent readPoemIntent = new Intent(SearchActivity.this,AuthorsProfileActivity.class);
                        readPoemIntent.putExtra(Constants.POST_AUTHOR, model.getAuthor());
                        startActivity(readPoemIntent);
                    }
                });

                viewHolder.btnLiked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        FireBaseUtils.mDatabaseLike.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
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
                        Intent likedPostsIntent = new Intent(SearchActivity.this,LikesActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
                viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(SearchActivity.this,CommentActivity.class);
                        commentIntent.putExtra(Constants.POST_KEY,post_key);
                        commentIntent.putExtra(Constants.POST_TITLE,model.getTitle());
                        commentIntent.putExtra(Constants.POST_TYPE,Constants.POEM_HOLDER);
                        startActivity(commentIntent);
                    }
                });

                viewHolder.btnViews.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(SearchActivity.this,ViewsActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
            }
        };
        rvSearchResults.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    /*
    private void firebaseDevotionSearch() {
        FirebaseRecyclerAdapter<Devotion,ArticleViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Devotion, ArticleViewHolder>(
                Devotion.class,R.layout.item_article,ArticleViewHolder.class, articleRef) {
            @Override
            protected void populateViewHolder(ArticleViewHolder viewHolder, final Devotion model, int position) {
                final String post_key = getRef(position).getKey();
                viewHolder.post_title.setText(model.getTitle());
                viewHolder.setTint(SearchActivity.this);
                viewHolder.post_author.setText(getString(R.string.article_author,model.getAuthor()));
                viewHolder.setIvImage(SearchActivity.this, ImageUtils.getPoemUrl());
                if (model.getNumLikes() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumLikes());
                    viewHolder.numLikes.setText(count);
                }
                if (model.getNumComments() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumComments());
                    viewHolder.numComments.setText(count);
                }
                if (model.getNumViews() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumViews());
                    viewHolder.tvNumViews.setText(getString(R.string.viewers,count));
                }
                if (model.getTimeCreated() != null) {
                    String time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.timeTextView.setText(time);
                }
                viewHolder.setLikeBtn(post_key);
                viewHolder.setPostViewed(post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessView = true;
                        FireBaseUtils.mDatabaseViews.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessView) {
                                    if (!dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
                                        FireBaseUtils.addDevotionView(model,post_key);
                                        mProcessView = false;
                                        FireBaseUtils.onPoemViewed(post_key);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        Intent readPoemIntent = new Intent(SearchActivity.this,ScrollingActivity.class);
                        readPoemIntent.putExtra(Constants.POST_CONTENT, model.getDevotionText());
                        readPoemIntent.putExtra(Constants.POST_TITLE, model.getTitle());
                        startActivity(readPoemIntent);
                    }
                });

                viewHolder.post_author.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent readPoemIntent = new Intent(SearchActivity.this,AuthorsProfileActivity.class);
                        readPoemIntent.putExtra(Constants.POST_AUTHOR, model.getAuthor());
                        startActivity(readPoemIntent);
                    }
                });

                viewHolder.btnLiked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        FireBaseUtils.mDatabaseLike.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
                                        FireBaseUtils.mDatabaseLike.child(post_key).child(FireBaseUtils.getUiD()).removeValue();
                                        FireBaseUtils.onPoemDisliked(post_key);
                                        mProcessLike = false;
                                    } else {
                                        FireBaseUtils.addDevotionLike(model, post_key);
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
                        Intent likedPostsIntent = new Intent(SearchActivity.this,LikesActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
                viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(SearchActivity.this,CommentActivity.class);
                        commentIntent.putExtra(Constants.POST_KEY,post_key);
                        commentIntent.putExtra(Constants.POST_TITLE,model.getTitle());
                        commentIntent.putExtra(Constants.POST_TYPE,Constants.POEM_HOLDER);
                        startActivity(commentIntent);
                    }
                });

                viewHolder.btnViews.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(SearchActivity.this,ViewsActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
            }
        };
        rvSearchResults.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }*/

    private void firebaseDevotionSearch() {
        FirebaseRecyclerOptions<Devotion> response = new FirebaseRecyclerOptions.Builder<Devotion>()
                                                          .setQuery(articleRef, Devotion.class)
                                                          .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Devotion, ArticleViewHolder>(response) {
            @NonNull
            @Override
            public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_storyrow, parent, false);
                return new ArticleViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ArticleViewHolder viewHolder, int position, @NonNull final Devotion model) {
                final String post_key = getRef(position).getKey();
                viewHolder.post_title.setText(model.getTitle());
                viewHolder.setTint(SearchActivity.this);
                viewHolder.post_author.setText(getString(R.string.article_author,model.getAuthor()));
                viewHolder.setIvImage(SearchActivity.this, ImageUtils.getPoemUrl());
                if (model.getNumLikes() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumLikes());
                    viewHolder.numLikes.setText(count);
                }
                if (model.getNumComments() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumComments());
                    viewHolder.numComments.setText(count);
                }
                if (model.getNumViews() != null) {
                    String count = NumberUtils.shortenDigit(model.getNumViews());
                    viewHolder.tvNumViews.setText(getString(R.string.viewers,count));
                }
                if (model.getTimeCreated() != null) {
                    String time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.timeTextView.setText(time);
                }
                viewHolder.setLikeBtn(post_key);
                viewHolder.setPostViewed(post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessView = true;
                        FireBaseUtils.mDatabaseViews.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessView) {
                                    if (!dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
                                        FireBaseUtils.addDevotionView(model,post_key);
                                        mProcessView = false;
                                        FireBaseUtils.onPoemViewed(post_key);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        Intent readPoemIntent = new Intent(SearchActivity.this,ScrollingActivity.class);
                        readPoemIntent.putExtra(Constants.POST_CONTENT, model.getDevotionText());
                        readPoemIntent.putExtra(Constants.POST_TITLE, model.getTitle());
                        startActivity(readPoemIntent);
                    }
                });

                viewHolder.post_author.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent readPoemIntent = new Intent(SearchActivity.this,AuthorsProfileActivity.class);
                        readPoemIntent.putExtra(Constants.POST_AUTHOR, model.getAuthor());
                        startActivity(readPoemIntent);
                    }
                });

                viewHolder.btnLiked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        FireBaseUtils.mDatabaseLike.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
                                        FireBaseUtils.mDatabaseLike.child(post_key).child(FireBaseUtils.getUiD()).removeValue();
                                        FireBaseUtils.onPoemDisliked(post_key);
                                        mProcessLike = false;
                                    } else {
                                        FireBaseUtils.addDevotionLike(model, post_key);
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
                        Intent likedPostsIntent = new Intent(SearchActivity.this,LikesActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
                viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(SearchActivity.this,CommentActivity.class);
                        commentIntent.putExtra(Constants.POST_KEY,post_key);
                        commentIntent.putExtra(Constants.POST_TITLE,model.getTitle());
                        commentIntent.putExtra(Constants.POST_TYPE,Constants.POEM_HOLDER);
                        startActivity(commentIntent);
                    }
                });

                viewHolder.btnViews.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(SearchActivity.this,ViewsActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
            }
        };
        rvSearchResults.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onStart(){
        super.onStart();
        firebaseRecyclerAdapter.startListening();
        selectSearchField();
    }

    /*
    @Override
    public void onNewIntent(Intent intent){
        Bundle extra = intent.getExtras();
        if (extra != null){
            postType = extra.getString(Constants.POST_TYPE);
            searchText = extra.getString("heading");
            firebaseSearch();
        } else {
            selectSearchField();
        }
    }*/

    private void selectSearchField()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
        builder.setSingleChoiceItems(categories, -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                updateDialog.dismiss();
                etSearch.setHint(getString(R.string.search_title,contents.get(item)));
                postType = contents.get(item);
                initSearchFor();
            }
        });
        builder.setTitle("Search for?");
        updateDialog = builder.create();
        updateDialog.show();
    }

    /*private void firebaseStorySearch() {
        FirebaseRecyclerAdapter<Story,StoryViewHolder> fireBaseRecyclerAdapter = new FirebaseRecyclerAdapter<Story, StoryViewHolder>(
                Story.class,R.layout.item_storyrow,StoryViewHolder.class, storyRef)
        {
                @Override
                protected void populateViewHolder(StoryViewHolder viewHolder, final Story model, int position) {
                final String post_key = getRef(position).getKey();
                FireBaseUtils.mDatabaseLike.child(post_key).keepSynced(true);
                viewHolder.tvTitle.setText(model.getTitle());
                viewHolder.setTint(SearchActivity.this);
                viewHolder.tvCategory.setText(getString(R.string.post_category,model.getCategory()));
                viewHolder.tvStatus.setText(getString(R.string.post_status,model.getStatus()));
                viewHolder.tvChapters.setText(getString(R.string.post_chapters, NumberUtils.setPlurality(model.getChapters(),"Chapter")));
                if (model.getImageUrl() == null){
                    viewHolder.setIvImage(SearchActivity.this, ImageUtils.getStoryUrl(model.getCategory().trim()));
                } else {
                    viewHolder.setIvImage(SearchActivity.this,model.getImageUrl());
                }

                viewHolder.tvAuthor.setText(getString(R.string.post_author,model.getAuthor()));

                if (model.getNumLikes() != null) {
                    viewHolder.tvNumLikes.setText(String.format("%s",model.getNumLikes().toString()));
                }

                if (model.getNumComments() != null) {
                    viewHolder.tvNumComments.setText(String.format("%s",model.getNumComments().toString()));
                }

                if (model.getNumViews() != null) {
                    viewHolder.tvNumViews.setText(String.format("%s",model.getNumViews().toString()));
                }
                if (model.getTimeCreated() != null) {
                    String time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.tvTime.setText(time);
                }

                viewHolder.tvAuthor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent readPoemIntent = new Intent(SearchActivity.this,AuthorsProfileActivity.class);
                        readPoemIntent.putExtra(Constants.POST_AUTHOR, model.getAuthor());
                        startActivity(readPoemIntent);
                    }
                });
                viewHolder.setLikeBtn(post_key);
                viewHolder.setPostViewed(post_key);

                if (model.getLastUpdate() != null) {
                    Boolean t = TimeUtils.currentTime() - model.getLastUpdate() < TimeUtils.MILLISECONDS_DAY; //&& res;
                    viewHolder.setVisibility(t);
                }

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addToViews(model.getDescription(),post_key,model);
                    }
                });

                viewHolder.btnLiked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        FireBaseUtils.mDatabaseLike.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
                                        FireBaseUtils.mDatabaseLike.child(post_key).child(FireBaseUtils.getUiD()).removeValue();
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
                        Intent likedPostsIntent = new Intent(SearchActivity.this,LikesActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
                viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(SearchActivity.this,CommentActivity.class);
                        commentIntent.putExtra(Constants.POST_KEY,post_key);
                        commentIntent.putExtra(Constants.POST_TITLE,model.getTitle());
                        commentIntent.putExtra(Constants.POST_TYPE,Constants.STORY_HOLDER);
                        startActivity(commentIntent);
                    }
                });
                viewHolder.btnViews.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(SearchActivity.this,ViewsActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
            }
        };

        rvSearchResults.setAdapter(fireBaseRecyclerAdapter);
        fireBaseRecyclerAdapter.notifyDataSetChanged();
    }*/

    private void firebaseStorySearch() {
        FirebaseRecyclerOptions<Story> response = new FirebaseRecyclerOptions.Builder<Story>()
                                                            .setQuery(storyRef, Story.class)
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
                FireBaseUtils.mDatabaseLike.child(post_key).keepSynced(true);
                viewHolder.tvTitle.setText(model.getTitle());
                viewHolder.setTint(SearchActivity.this);
                viewHolder.tvCategory.setText(getString(R.string.post_category,model.getCategory()));
                viewHolder.tvStatus.setText(getString(R.string.post_status,model.getStatus()));
                viewHolder.tvChapters.setText(getString(R.string.post_chapters, NumberUtils.setPlurality(model.getChapters(),"Chapter")));
                if (model.getImageUrl() == null){
                    viewHolder.setIvImage(SearchActivity.this, ImageUtils.getStoryUrl(model.getCategory().trim()));
                } else {
                    viewHolder.setIvImage(SearchActivity.this,model.getImageUrl());
                }

                viewHolder.tvAuthor.setText(getString(R.string.post_author,model.getAuthor()));

                if (model.getNumLikes() != null) {
                    viewHolder.tvNumLikes.setText(String.format("%s",model.getNumLikes().toString()));
                }

                if (model.getNumComments() != null) {
                    viewHolder.tvNumComments.setText(String.format("%s",model.getNumComments().toString()));
                }

                if (model.getNumViews() != null) {
                    viewHolder.tvNumViews.setText(String.format("%s",model.getNumViews().toString()));
                }
                if (model.getTimeCreated() != null) {
                    String time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.tvTime.setText(time);
                }

                viewHolder.tvAuthor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent readPoemIntent = new Intent(SearchActivity.this,AuthorsProfileActivity.class);
                        readPoemIntent.putExtra(Constants.POST_AUTHOR, model.getAuthor());
                        startActivity(readPoemIntent);
                    }
                });
                viewHolder.setLikeBtn(post_key);
                viewHolder.setPostViewed(post_key);

                if (model.getLastUpdate() != null) {
                    Boolean t = TimeUtils.currentTime() - model.getLastUpdate() < TimeUtils.MILLISECONDS_DAY; //&& res;
                    viewHolder.setVisibility(t);
                }

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addToViews(model.getDescription(),post_key,model);
                    }
                });

                viewHolder.btnLiked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        FireBaseUtils.mDatabaseLike.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
                                        FireBaseUtils.mDatabaseLike.child(post_key).child(FireBaseUtils.getUiD()).removeValue();
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
                        Intent likedPostsIntent = new Intent(SearchActivity.this,LikesActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
                viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(SearchActivity.this,CommentActivity.class);
                        commentIntent.putExtra(Constants.POST_KEY,post_key);
                        commentIntent.putExtra(Constants.POST_TITLE,model.getTitle());
                        commentIntent.putExtra(Constants.POST_TYPE,Constants.STORY_HOLDER);
                        startActivity(commentIntent);
                    }
                });
                viewHolder.btnViews.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(SearchActivity.this,ViewsActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
            }
        };
        rvSearchResults.setAdapter(firebaseRecyclerAdapter);
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
                        FireBaseUtils.addStoryView(model,post_key);
                        FireBaseUtils.onStoryViewed(post_key);
                        initializeChapters(post_key, model);
                    } else {
                        mProcessView = false;
                        showDescription(description,post_key,model);
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
        addToLibrary(model,post_key);
        loadChapters(model.getCategory().trim(), post_key);
    }


    private void loadChapters(String status, final String post_key) {
        final ProgressDialog progressDialog = new ProgressDialog(SearchActivity.this);
        progressDialog.setMessage("Loading chapters");
        progressDialog.setCancelable(true);
        progressDialog.show();

        FireBaseUtils.mDatabaseChapters.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pageCount = ((int) dataSnapshot.getChildrenCount());
                for (DataSnapshot chapterSnapShot: dataSnapshot.getChildren()) {
                    Chapter chapter = chapterSnapShot.getValue(Chapter.class);
                    contents.add(chapter.getContent());
                }
                if (contents.size() == pageCount) {
                    progressDialog.dismiss();
                    Chapters chapters = Chapters.getInstance();
                    chapters.setChapters(contents);
                    Intent readIntent = new Intent(SearchActivity.this, ActivityRead.class);
                    readIntent.putExtra(Constants.POST_KEY,post_key);
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
                    Map<String,Object> values = new HashMap<>();
                    values.put(Constants.POST_KEY,  post_key);
                    values.put(Constants.POST_TITLE, model.getTitle());
                    values.put(Constants.CHAPTER_ADDED, 0);
                    values.put("lastAccessed", timeAccessed);
                    FireBaseUtils.mDatabaseLibrary.child(FireBaseUtils.getUiD()).child(post_key).setValue(values);
                    Toast.makeText(SearchActivity.this,model.getTitle() + " added to library",Toast.LENGTH_LONG).show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
        builder.setMessage(description)
                .setPositiveButton("Start Reading", dialogClickListener)
                .setNegativeButton("Back", dialogClickListener)
                .show();
    }

}
