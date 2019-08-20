package com.techart.writersblock.chapters;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.techart.writersblock.R;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Chapter;
import com.techart.writersblock.viewholders.ChapterViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DeleteOnlineChapterActivity extends AppCompatActivity {
    private RecyclerView mPoemList;
    private String storyUrl;
    ProgressBar progressBar;
    String chapterKey;
    int chapterNumber;
    List<String> chapters;
    String chapterCount;
    private int pageCount;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabrecyclerviewer);
        storyUrl = getIntent().getStringExtra(Constants.STORY_REFID);
        chapterCount = getIntent().getStringExtra(Constants.STORY_CHAPTERCOUNT);
        setTitle("Delete Chapter");
        mPoemList = findViewById(R.id.poem_list);
        progressBar = findViewById(R.id.pb_loading);
        mPoemList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DeleteOnlineChapterActivity.this);
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
        if (storyUrl != null) {
            FirebaseRecyclerAdapter<Chapter, ChapterViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chapter, ChapterViewHolder>(
                    Chapter.class, R.layout.item_chapter, ChapterViewHolder.class, FireBaseUtils.mDatabaseChapters.child(storyUrl)) {
                @Override
                protected void populateViewHolder(ChapterViewHolder viewHolder, final Chapter model, int position) {
                    final String post_key = getRef(position).getKey();
                    progressBar.setVisibility(View.GONE);
                    viewHolder.btDelete.setVisibility(View.VISIBLE);
                    viewHolder.tvTitle.setText("Chapter " + model.getChapterTitle());
                    viewHolder.btDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteMessage(post_key, Integer.valueOf(model.getChapterTitle()));
                        }
                    });
                }
            };
            mPoemList.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Kindly reload", Toast.LENGTH_LONG).show();
        }
    }*/

    private void bindView() {
        if(storyUrl != null){
            FirebaseRecyclerOptions<Chapter> response = new FirebaseRecyclerOptions.Builder<Chapter>()
                                                                .setQuery(FireBaseUtils.mDatabaseChapters.child(storyUrl), Chapter.class)
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
                    viewHolder.btDelete.setVisibility(View.VISIBLE);
                    viewHolder.tvTitle.setText("Chapter " + model.getChapterTitle());
                    viewHolder.btDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteMessage(post_key, Integer.valueOf(model.getChapterTitle()));
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


    private DatabaseReference.CompletionListener mRemoveListener =
            new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    if (error == null) {
                        iterateChapters();
                        Toast.makeText(DeleteOnlineChapterActivity.this, "Chapter " + chapterNumber + " deleted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(DeleteOnlineChapterActivity.this, "Chapter " + chapterNumber + "not  deleted", Toast.LENGTH_LONG).show();
                    }
                }
            };

    private void deleteMessage(String postKey, int chapter) {
        chapterKey = postKey;
        chapterNumber = chapter;
        FireBaseUtils.mDatabaseChapters.child(storyUrl).child(postKey).removeValue(mRemoveListener);
    }

    private void iterateChapters() {
        chapters = new ArrayList<>();
        FireBaseUtils.mDatabaseChapters.child(storyUrl).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pageCount = ((int) dataSnapshot.getChildrenCount());
                for (DataSnapshot chapterSnapShot : dataSnapshot.getChildren()) {
                    chapters.add(chapterSnapShot.getKey());

                    if (chapters.size() == pageCount) {
                        editChapter();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void editChapter() {
        int counter = 1;
        for (int i = 0; i < chapters.size(); i++) {
            Map<String, Object> values = new HashMap<>();
            values.put(Constants.CHAPTER_TITLE, String.valueOf(counter++));
            FireBaseUtils.mDatabaseChapters.child(storyUrl).child(chapters.get(i)).updateChildren(values);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, getIntent());
        finish();
    }
}

