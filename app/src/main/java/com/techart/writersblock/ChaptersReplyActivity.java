package com.techart.writersblock;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.techart.writersblock.models.Comment;
import com.techart.writersblock.utils.TimeUtils;
import com.techart.writersblock.viewholders.CommentHolder;

import java.util.HashMap;
import java.util.Map;

public class ChaptersReplyActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mCommentList;
    private EditText mEtComment;
    private String post_key;
    private Boolean isSent;
    private String postType;
    private String commentKey;
    private TextView tvEmpty;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        post_key = getIntent().getStringExtra(Constants.POST_KEY);
        commentKey = getIntent().getStringExtra(Constants.COMMENT_KEY);

        String postAuthor = getIntent().getStringExtra(Constants.POST_AUTHOR);
        String comment = getIntent().getStringExtra(Constants.COMMENT_TEXT);
        String time = getIntent().getStringExtra(Constants.TIME_CREATED);
        FireBaseUtils.mDatabaseReplies.child(commentKey).keepSynced(true);


        postType = getIntent().getStringExtra(Constants.POST_TYPE);
        setTitle("Replies to "+ postAuthor);
        tvEmpty = findViewById(R.id.tv_empty);
        TextView tvAuthor = findViewById(R.id.tvAuthor);
        TextView tvComment = findViewById(R.id.tvComment);
        TextView tvTime = findViewById(R.id.tvTime);
        tvAuthor.setText(postAuthor);
        tvComment.setText(comment);
        tvTime.setText(time);
        mCommentList = findViewById(R.id.comment_recyclerview);
        mCommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChaptersReplyActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mCommentList.setLayoutManager(linearLayoutManager);
        init();
        initCommentSection();
    }

    private void initCommentSection() {
        Query commentsQuery = FireBaseUtils.mDatabaseChapterReplies.child(commentKey).orderByChild(Constants.TIME_CREATED);
        FirebaseRecyclerOptions<Comment> response = new FirebaseRecyclerOptions.Builder<Comment>()
                                                            .setQuery(commentsQuery, Comment.class)
                                                            .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentHolder>(response) {
            @NonNull
            @Override
            public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_reply, parent, false);
                return new CommentHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentHolder viewHolder, int position, @NonNull final Comment model) {
                tvEmpty.setVisibility(View.GONE);
                if (model.getAuthorUrl() != null){
                    setVisibility(model.getAuthorUrl(),viewHolder);
                }
                viewHolder.authorTextView.setText(model.getAuthor());
                viewHolder.commentTextView.setText(model.getCommentText());
                String time = TimeUtils.timeElapsed(model.getTimeCreated());
                viewHolder.timeTextView.setText(time);
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
        if (!comment.isEmpty()) {
            final ProgressDialog progressDialog = new ProgressDialog(ChaptersReplyActivity.this);
            progressDialog.setMessage("Sending reply..");
            progressDialog.setCancelable(true);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            FireBaseUtils.mDatabaseChapterReplies.child(post_key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!isSent) {
                        DatabaseReference newComment = FireBaseUtils.mDatabaseChapterReplies.child(commentKey).push();
                        Map<String,Object> values = new HashMap<>();
                        values.put(Constants.USER,FireBaseUtils.getUiD());
                        values.put(Constants.POST_AUTHOR,FireBaseUtils.getAuthor());
                        values.put(Constants.AUTHOR_URL,FireBaseUtils.getUiD());
                        values.put(Constants.COMMENT_TEXT,comment);
                        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
                        newComment.setValue(values);
                        isSent = true;
                        Toast.makeText(ChaptersReplyActivity.this,"Reply sent....",Toast.LENGTH_LONG ).show();
                        onReplySent();
                        progressDialog.dismiss();
                        mEtComment.setText("");
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(this,"Nothing to send",Toast.LENGTH_LONG ).show();
        }
    }

    private void onReplySent() {
        FireBaseUtils.mDatabaseChapterReplies.child(post_key).child(commentKey).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Comment comment = mutableData.getValue(Comment.class);
                if (comment == null) {
                    return Transaction.success(mutableData);
                } else if (comment.getReplies() ==  null){
                    Map<String,Object> values = new HashMap<>();
                    values.put(Constants.REPLIES,1);
                    FireBaseUtils.mDatabaseChapterReplies.child(post_key).child(commentKey).updateChildren(values);
                    return Transaction.success(mutableData);
                }
                comment.setReplies(comment.getReplies() + 1 );
                // Set value and report transaction success
                mutableData.setValue(comment);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
            }
        });
    }



    private void setVisibility(String url, CommentHolder viewHolder) {
        if (FireBaseUtils.getUiD() != null && FireBaseUtils.getUiD().equals(url)){
            viewHolder.commentTextView.setBackground(getResources().getDrawable(R.drawable.tv_circular_active_background));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home) {
            Intent data = new Intent();
            data.putExtra(Constants.POST_KEY, post_key);
            data.putExtra(Constants.POST_TYPE, postType);
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
