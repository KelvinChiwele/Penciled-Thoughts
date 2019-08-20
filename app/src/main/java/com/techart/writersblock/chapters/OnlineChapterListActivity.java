package com.techart.writersblock.chapters;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.techart.writersblock.R;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Chapter;
import com.techart.writersblock.viewholders.ChapterViewHolder;


public class OnlineChapterListActivity extends AppCompatActivity {
    private RecyclerView mPoemList;
    private String storyUrl;
    ProgressBar progressBar;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabrecyclerviewer);
        storyUrl = getIntent().getStringExtra(Constants.STORY_REFID);
        setTitle("Chapters");
        mPoemList = findViewById(R.id.poem_list);
        progressBar = findViewById(R.id.pb_loading);
        mPoemList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(OnlineChapterListActivity.this);
        mPoemList.setLayoutManager(linearLayoutManager);
        bindView();
    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    /*
    private void bindView() {
        if(storyUrl != null){
            FirebaseRecyclerAdapter<Chapter, ChapterViewHolder> firebaseRecyclerAdapter =
                    new FirebaseRecyclerAdapter<Chapter, ChapterViewHolder>(
                    Chapter.class,R.layout.item_chapter,ChapterViewHolder.class, FireBaseUtils.mDatabaseChapters.child(storyUrl)) {
                @Override
                protected void populateViewHolder(ChapterViewHolder viewHolder, final Chapter model, int position) {
                    final String post_key = getRef(position).getKey();
                    progressBar.setVisibility(View.GONE);
                    viewHolder.tvTitle.setText(model.getChapterTitle());
                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent readIntent = new Intent(OnlineChapterListActivity.this,ChapterEditorOnlineActivity.class);
                            readIntent.putExtra(Constants.POST_KEY,post_key);
                            readIntent.putExtra(Constants.STORY_REFID,storyUrl);
                            readIntent.putExtra(Constants.CHAPTER_TITLE,model.getChapterTitle());
                            readIntent.putExtra(Constants.CHAPTER_CONTENT,model.getContent());
                            startActivity(readIntent);
                        }
                    });
                }
            };
            mPoemList.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this,"Kindly reload",Toast.LENGTH_LONG).show();
        }
    }*/

    private void bindView() {
        if(storyUrl != null){
            FirebaseRecyclerOptions<Chapter> response = new FirebaseRecyclerOptions.Builder<Chapter>()
                                                                 .setQuery(FireBaseUtils.mDatabaseDevotions, Chapter.class)
                                                                 .build();
            firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chapter, ChapterViewHolder>(response) {
                @NonNull
                @Override
                public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.item_chapter, parent, false);
                    return new ChapterViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull ChapterViewHolder viewHolder, int position, @NonNull final Chapter model) {
                    final String post_key = getRef(position).getKey();
                    progressBar.setVisibility(View.GONE);
                    viewHolder.tvTitle.setText(model.getChapterTitle());
                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent readIntent = new Intent(OnlineChapterListActivity.this,ChapterEditorOnlineActivity.class);
                            readIntent.putExtra(Constants.POST_KEY,post_key);
                            readIntent.putExtra(Constants.STORY_REFID,storyUrl);
                            readIntent.putExtra(Constants.CHAPTER_TITLE,model.getChapterTitle());
                            readIntent.putExtra(Constants.CHAPTER_CONTENT,model.getContent());
                            startActivity(readIntent);
                        }
                    });
                }
            };
            mPoemList.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this,"Kindly reload",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed(){
        setResult(RESULT_OK,getIntent());
        finish();
    }
}

