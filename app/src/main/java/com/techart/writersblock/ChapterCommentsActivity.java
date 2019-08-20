package com.techart.writersblock;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Chapter;
import com.techart.writersblock.models.Comment;
import com.techart.writersblock.utils.NumberUtils;
import com.techart.writersblock.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

public class ChapterCommentsActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mCommentList;
    private EditText mEtComment;
    private String post_key;
    private String chapterKey;
    private Boolean isSent;
    private String postType;
    private String postName;
    private String time;
    private TextView tvEmpty;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        post_key = getIntent().getStringExtra(Constants.POST_KEY);
        chapterKey = getIntent().getStringExtra(Constants.CHAPTER);
        postName = getIntent().getStringExtra(Constants.POST_TITLE);
        postType = getIntent().getStringExtra(Constants.POST_TYPE);
        setTitle("Comments on "+ postName);
        tvEmpty = findViewById(R.id.tv_empty);
        mCommentList = findViewById(R.id.comment_recyclerview);
        mCommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChapterCommentsActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mCommentList.setLayoutManager(linearLayoutManager);
        init();
        initCommentSection();
    }

    /*
    private void initCommentSection() {
        Query commentsQuery = FireBaseUtils.mDatabaseChaptersComment.child(post_key).orderByChild(Constants.TIME_CREATED);
        FirebaseRecyclerAdapter<Comment, CommentHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentHolder>(
                Comment.class, R.layout.item_comment, CommentHolder.class, commentsQuery)
        {
            @Override
            protected void populateViewHolder(CommentHolder viewHolder, final Comment model, int position) {
                final String comment_key = getRef(position).getKey();
                tvEmpty.setVisibility(View.GONE);
                if (model.getAuthorUrl() != null){
                    setVisibility(model.getAuthorUrl(),viewHolder);
                }
                if (model.getAuthor() != null){
                    viewHolder.authorTextView.setText(model.getAuthor());
                }

                if (model.getTimeCreated() != null){
                    time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.timeTextView.setText(time);
                }
                viewHolder.commentTextView.setText(model.getCommentText());

                if (model.getReplies() != null && model.getReplies() != 0){
                    viewHolder.tvViewReplies.setVisibility(View.VISIBLE);
                    viewHolder.tvViewReplies.setText(getString(R.string.replies, NumberUtils.setUsualPlurality(model.getReplies(),"reply")));
                    viewHolder.tvViewReplies.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent replyIntent = new Intent(ChapterCommentsActivity.this,ChaptersReplyActivity.class);
                            replyIntent.putExtra(Constants.POST_KEY,post_key);
                            replyIntent.putExtra(Constants.COMMENT_KEY,comment_key);
                            replyIntent.putExtra(Constants.POST_AUTHOR,model.getAuthor());
                            replyIntent.putExtra(Constants.COMMENT_TEXT,model.getCommentText());
                            replyIntent.putExtra(Constants.TIME_CREATED,time);
                            replyIntent.putExtra(Constants.POST_TYPE,postType);
                            startActivity(replyIntent);
                        }
                    });
                }
                viewHolder.tvReply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent replyIntent = new Intent(ChapterCommentsActivity.this,ChaptersReplyActivity.class);
                        replyIntent.putExtra(Constants.POST_KEY,post_key);
                        replyIntent.putExtra(Constants.COMMENT_KEY,comment_key);
                        replyIntent.putExtra(Constants.POST_AUTHOR,model.getAuthor());
                        replyIntent.putExtra(Constants.COMMENT_TEXT,model.getCommentText());
                        replyIntent.putExtra(Constants.TIME_CREATED,time);
                        replyIntent.putExtra(Constants.POST_TYPE,postType);
                        startActivity(replyIntent);
                    }
                });
            }
        };
        mCommentList.setAdapter(firebaseRecyclerAdapter);
    }*/

    private void initCommentSection() {
        Query commentsQuery = FireBaseUtils.mDatabaseChaptersComment.child(post_key).orderByChild(Constants.TIME_CREATED);
        FirebaseRecyclerOptions<Comment> response = new FirebaseRecyclerOptions.Builder<Comment>()
                                                            .setQuery(commentsQuery, Comment.class)
                                                            .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentHolder>(response) {
            @NonNull
            @Override
            public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_comment, parent, false);
                return new CommentHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentHolder viewHolder, int position, @NonNull final Comment model) {
                final String comment_key = getRef(position).getKey();
                tvEmpty.setVisibility(View.GONE);
                if (model.getAuthorUrl() != null){
                    setVisibility(model.getAuthorUrl(),viewHolder);
                }
                if (model.getAuthor() != null){
                    viewHolder.authorTextView.setText(model.getAuthor());
                }

                if (model.getTimeCreated() != null){
                    time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.timeTextView.setText(time);
                }
                viewHolder.commentTextView.setText(model.getCommentText());

                if (model.getReplies() != null && model.getReplies() != 0){
                    viewHolder.tvViewReplies.setVisibility(View.VISIBLE);
                    viewHolder.tvViewReplies.setText(getString(R.string.replies, NumberUtils.setUsualPlurality(model.getReplies(),"reply")));
                    viewHolder.tvViewReplies.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent replyIntent = new Intent(ChapterCommentsActivity.this,ChaptersReplyActivity.class);
                            replyIntent.putExtra(Constants.POST_KEY,post_key);
                            replyIntent.putExtra(Constants.COMMENT_KEY,comment_key);
                            replyIntent.putExtra(Constants.POST_AUTHOR,model.getAuthor());
                            replyIntent.putExtra(Constants.COMMENT_TEXT,model.getCommentText());
                            replyIntent.putExtra(Constants.TIME_CREATED,time);
                            replyIntent.putExtra(Constants.POST_TYPE,postType);
                            startActivity(replyIntent);
                        }
                    });
                }
                viewHolder.tvReply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent replyIntent = new Intent(ChapterCommentsActivity.this,ChaptersReplyActivity.class);
                        replyIntent.putExtra(Constants.POST_KEY,post_key);
                        replyIntent.putExtra(Constants.COMMENT_KEY,comment_key);
                        replyIntent.putExtra(Constants.POST_AUTHOR,model.getAuthor());
                        replyIntent.putExtra(Constants.COMMENT_TEXT,model.getCommentText());
                        replyIntent.putExtra(Constants.TIME_CREATED,time);
                        replyIntent.putExtra(Constants.POST_TYPE,postType);
                        startActivity(replyIntent);
                    }
                });
            }
        };
        mCommentList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    private void setVisibility(String url, CommentHolder viewHolder) {
        if (FireBaseUtils.getUiD() != null && FireBaseUtils.getUiD().equals(url)){
            viewHolder.commentTextView.setBackground(getResources().getDrawable(R.drawable.tv_circular_active_background));
        }
    }

    private void init() {
        mEtComment = findViewById(R.id.et_comment);
        findViewById(R.id.iv_send).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_send) {
            sendComment();
        }
    }

    private void sendComment() {
        final String comment = mEtComment.getText().toString().trim();
        isSent = false;
        if (!comment.isEmpty())
        {
            final ProgressDialog progressDialog = new ProgressDialog(ChapterCommentsActivity.this);
            progressDialog.setMessage("Sending comment..");
            progressDialog.setCancelable(true);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            FireBaseUtils.mDatabaseChaptersComment.child(post_key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!isSent)
                    {
                        DatabaseReference newComment = FireBaseUtils.mDatabaseChaptersComment.child(post_key).push();
                        Map<String,Object> values = new HashMap<>();
                        values.put(Constants.AUTHOR_URL,FireBaseUtils.getUiD());
                        values.put(Constants.POST_AUTHOR,FireBaseUtils.getAuthor());
                        values.put(Constants.COMMENT_TEXT,comment);
                     //   values.put(Constants.REPLIES,0);
                        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
                        newComment.setValue(values);
                        isSent = true;
                        progressDialog.dismiss();
                        onCommentSent();
                        mEtComment.setText("");
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else
        {
            Toast.makeText(this,"Nothing to send",Toast.LENGTH_LONG ).show();
        }
    }

    private void onCommentSent() {
        FireBaseUtils.mDatabaseChaptersComment.child(post_key).child(chapterKey).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Chapter chapter = mutableData.getValue(Chapter.class);
                if (chapter == null) {
                    return Transaction.success(mutableData);
                } else if (chapter.getNumComments() ==  null){
                    Map<String,Object> values = new HashMap<>();
                    values.put(Constants.NUM_COMMENTS,1);
                    FireBaseUtils.mDatabaseChaptersComment.child(post_key).updateChildren(values);
                    return Transaction.success(mutableData);
                }
                chapter.setNumComments(chapter.getNumComments() + 1 );
                // Set value and report transaction success
                mutableData.setValue(chapter);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if (resultCode == RESULT_OK){
            post_key = data.getStringExtra(Constants.POST_KEY);
            postName = data.getStringExtra(Constants.POST_TITLE);
            postType = data.getStringExtra(Constants.POST_TYPE);
        }
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {
        public TextView authorTextView;
        public TextView commentTextView;
        public TextView timeTextView;
        public TextView tvReply;
        public TextView tvViewReplies;

        public CommentHolder(View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.tvAuthor);
            timeTextView = itemView.findViewById(R.id.tvTime);
            tvReply = itemView.findViewById(R.id.tvReply);
            tvViewReplies = itemView.findViewById(R.id.tv_view_replies);
            commentTextView = itemView.findViewById(R.id.tvComment);
        }
    }

}
