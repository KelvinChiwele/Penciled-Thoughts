package com.techart.writersblock;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Comment;
import com.techart.writersblock.models.Devotion;
import com.techart.writersblock.models.Poem;
import com.techart.writersblock.models.Profile;
import com.techart.writersblock.models.Story;
import com.techart.writersblock.utils.NumberUtils;
import com.techart.writersblock.utils.TimeUtils;
import com.techart.writersblock.utils.UploadUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener,
        PopupMenu.OnMenuItemClickListener {
    private RecyclerView mCommentList;
    private EditText mEtComment;
    private String postKey;
    private Boolean isSent;
    private String postType;
    private String postName;
    private String time;
    private TextView tvEmpty;

    private ProgressBar progressBar;

    private static final int GALLERY_REQUEST = 1;
    private static final int REPLY_REQUEST = 2;

    private String currentMessage;
    private String commentKey;
    private Uri uri;
    StorageReference filePath;
    private PopupMenu popupMenu;
    private final static int EDIT = 1;

    private final static int DELETE = 2;
    private final static int CANCEL = 3;
    private final static int NEW = 4;

    private int menuAction;
    int count;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        FireBaseUtils.mDatabaseComment.keepSynced(true);
        FireBaseUtils.mDatabaseStory.keepSynced(true);
        FireBaseUtils.mDatabaseDevotions.keepSynced(true);
        FireBaseUtils.mDatabasePoems.keepSynced(true);

        postKey = getIntent().getStringExtra(Constants.POST_KEY);
        postName = getIntent().getStringExtra(Constants.POST_TITLE);
        postType = getIntent().getStringExtra(Constants.POST_TYPE);
        count = getIntent().getIntExtra(Constants.NUM_COMMENTS, 0);
        setTitle("Comments on "+ postName);
        progressBar = findViewById(R.id.pb_loading);
        tvEmpty = findViewById(R.id.tv_empty);
        mCommentList = findViewById(R.id.comment_recyclerview);
        mCommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CommentActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mCommentList.setLayoutManager(linearLayoutManager);
        if (count == 0) {
            progressBar.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        initCommentSection();
    }

    private void initCommentSection() {
        mEtComment = findViewById(R.id.et_comment);
        findViewById(R.id.iv_send).setOnClickListener(this);

        Query commentsQuery = FireBaseUtils.mDatabaseComment.child(postKey).orderByChild(Constants.TIME_CREATED);
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
            protected void onBindViewHolder(@NonNull final CommentHolder viewHolder, int position, @NonNull final Comment model) {
                final String comment_key = getRef(position).getKey();
                if (count != 0) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
                final String current_message = model.getCommentText();
                if (model.getAuthorUrl() != null){
                    setVisibility(model.getAuthorUrl(),viewHolder);
                }
                if (model.getAuthor() != null){
                    viewHolder.authorTextView.setText(model.getAuthor());
                }


                if (model.getCommentText() != null) {
                    viewHolder.commentTextView.setText(model.getCommentText());
                    viewHolder.ivSample.setVisibility(View.GONE);
                } else {
                    viewHolder.setImage(getApplicationContext(), model.getImageUrl());
                    viewHolder.commentTextView.setVisibility(View.GONE);
                }
                viewHolder.ivSample.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (model.getImageUrl() != null) {
                            Intent intent = new Intent(CommentActivity.this, FullImageActivity.class);
                            intent.putExtra(Constants.IMAGE_URL, model.getImageUrl());
                            startActivity(intent);
                        }
                    }
                });

                if (model.getTimeCreated() != null){
                    time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.timeTextView.setText(time);
                }

                if (model.getReplies() != null && model.getReplies() != 0){
                    viewHolder.tvViewReplies.setVisibility(View.VISIBLE);
                    viewHolder.tvViewReplies.setText(getString(R.string.replies, NumberUtils.setUsualPlurality(model.getReplies(),"reply")));
                    viewHolder.tvViewReplies.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent replyIntent = new Intent(CommentActivity.this,ReplyActivity.class);
                            replyIntent.putExtra(Constants.POST_KEY, postKey);
                            replyIntent.putExtra(Constants.COMMENT_KEY,comment_key);
                            replyIntent.putExtra(Constants.POST_AUTHOR,model.getAuthor());
                            replyIntent.putExtra(Constants.COMMENT_TEXT,model.getCommentText());
                            replyIntent.putExtra(Constants.TIME_CREATED,time);
                            replyIntent.putExtra(Constants.POST_TYPE,postType);
                            startActivityForResult(replyIntent, REPLY_REQUEST);
                        }
                    });
                }
                viewHolder.tvReply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent replyIntent = new Intent(CommentActivity.this,ReplyActivity.class);
                        replyIntent.putExtra(Constants.POST_KEY, postKey);
                        replyIntent.putExtra(Constants.COMMENT_KEY,comment_key);
                        replyIntent.putExtra(Constants.POST_AUTHOR,model.getAuthor());
                        replyIntent.putExtra(Constants.COMMENT_TEXT,model.getCommentText());
                        replyIntent.putExtra(Constants.TIME_CREATED,time);
                        replyIntent.putExtra(Constants.POST_TYPE,postType);
                        startActivityForResult(replyIntent, REPLY_REQUEST);
                    }
                });

                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (FireBaseUtils.getUiD() != null && FireBaseUtils.getUiD().equals(model.getAuthorUrl())) {
                            commentKey = comment_key;
                            currentMessage = current_message;
                            popupMenu = new PopupMenu(CommentActivity.this, viewHolder.commentTextView);
                            //ToDo final issue
                            popupMenu.getMenu().add(Menu.NONE, EDIT, Menu.NONE, "Edit");
                            popupMenu.getMenu().add(Menu.NONE, DELETE, Menu.NONE, "Delete");
                            popupMenu.getMenu().add(Menu.NONE, CANCEL, Menu.NONE, "Cancel");
                            popupMenu.setOnMenuItemClickListener(CommentActivity.this);
                            popupMenu.show();
                            return true;
                        } else {
                            return false;
                        }
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_send:
                if (menuAction == NEW) {
                    sendComment();
                } else {
                    editMessage();
                }
                menuAction = NEW;
                break;
            case R.id.iv_image:
                Intent intent = new Intent(CommentActivity.this, ImageActivity.class);
                postKey = getIntent().getStringExtra(Constants.POST_KEY);
                startActivityForResult(intent, GALLERY_REQUEST);
                break;
        }
    }

    private void sendComment() {
        final String comment = mEtComment.getText().toString().trim();
        isSent = false;
        if (!comment.isEmpty())
        {
            final ProgressDialog progressDialog = new ProgressDialog(CommentActivity.this);
            progressDialog.setMessage("Sending comment..");
            progressDialog.setCancelable(true);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            FireBaseUtils.mDatabaseComment.child(postKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!isSent)
                    {
                        DatabaseReference newComment = FireBaseUtils.mDatabaseComment.child(postKey).push();
                        Map<String,Object> values = new HashMap<>();
                        values.put(Constants.AUTHOR_URL,FireBaseUtils.getUiD());
                        values.put(Constants.POST_AUTHOR,FireBaseUtils.getAuthor());
                        values.put(Constants.COMMENT_TEXT,comment);
                        //   values.put(Constants.REPLIES,0);
                        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
                        newComment.setValue(values);
                        FireBaseUtils.updateNotifications(postType, postName, "Reader", "commented on ", postKey, comment, Profile.getInstance().getImageUrl());
                        isSent = true;
                        progressDialog.dismiss();
                        onCommentSent();
                        mEtComment.setText("");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else
        {
            Toast.makeText(this,"Nothing to send",Toast.LENGTH_LONG ).show();
        }
    }

    private void sendImage(DatabaseReference newComment, String imageUrl) {
        final ProgressDialog progressDialog = new ProgressDialog(CommentActivity.this);
        progressDialog.setMessage("Sending answer...");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        Map<String, Object> values = new HashMap<>();
        values.put(Constants.AUTHOR_URL, FireBaseUtils.getUiD());
        values.put(Constants.POST_AUTHOR, FireBaseUtils.getAuthor());
        values.put(Constants.IMAGE_URL, imageUrl);
        values.put(Constants.POST_KEY, postKey);
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        newComment.setValue(values);
        progressDialog.dismiss();
        FireBaseUtils.updateNotifications("writersInbox", "", Profile.getInstance().getSignedAs(), "sent an image in the writers chat room", newComment.getKey(), "", Profile.getInstance().getImageUrl());
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case EDIT:
                menuAction = EDIT;
                mEtComment.setText(currentMessage);
                break;
            case DELETE:
                deleteMessage();
                break;
            case CANCEL:
                popupMenu.dismiss();
                break;
        }
        return false;
    }

    private void deleteMessage() {
        FireBaseUtils.mDatabaseWritersChat.child(postKey).child(commentKey).removeValue();
        Toast.makeText(this, "Message deleted", Toast.LENGTH_LONG).show();
    }

    private void editMessage() {
        final String comment = mEtComment.getText().toString().trim();
        Map<String, Object> values = new HashMap<>();
        values.put(Constants.COMMENT_TEXT, comment);
        FireBaseUtils.mDatabaseWritersChat.child(postKey).child(commentKey).updateChildren(values);
        mEtComment.setText("");
        Toast.makeText(this, "Message updated", Toast.LENGTH_LONG).show();
    }


    private void onCommentSent() {
        switch (postType) {
            case Constants.POEM_HOLDER:
                poemCommentCount();
                break;
            case Constants.DEVOTION_HOLDER:
                devotionCommentCount();
                break;
            case Constants.STORY_HOLDER:
                storyCommentCount();
                break;
        }
    }

    private void poemCommentCount() {
        FireBaseUtils.mDatabasePoems.child(postKey).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Poem poem = mutableData.getValue(Poem.class);
                if (poem == null) {
                    return Transaction.success(mutableData);
                }
                poem.setNumComments(poem.getNumComments() + 1 );
                // Set value and report transaction success
                mutableData.setValue(poem);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
            }
        });
    }

    private void devotionCommentCount() {
        FireBaseUtils.mDatabaseDevotions.child(postKey).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Devotion devotion = mutableData.getValue(Devotion.class);
                if (devotion == null) {
                    return Transaction.success(mutableData);
                }
                devotion.setNumComments(devotion.getNumComments() + 1 );
                // Set value and report transaction success
                mutableData.setValue(devotion);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
            }
        });
    }
    private void storyCommentCount() {
        FireBaseUtils.mDatabaseStory.child(postKey).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Story story = mutableData.getValue(Story.class);
                if (story == null) {
                    return Transaction.success(mutableData);
                }
                story.setNumComments(story.getNumComments() + 1 );
                // Set value and report transaction success
                mutableData.setValue(story);
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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch (requestCode) {
                case REPLY_REQUEST:
                    postKey = data.getStringExtra(Constants.POST_KEY);
                    postName = data.getStringExtra(Constants.POST_TITLE);
                    postType = data.getStringExtra(Constants.POST_TYPE);
                    break;
                case GALLERY_REQUEST:
                    uri = Uri.parse(data.getStringExtra(Constants.URI));
                    postKey = getIntent().getStringExtra(Constants.POST_KEY);
                    upload()
                    ;
                    break;
            }


        }
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {
        public TextView authorTextView;
        public TextView commentTextView;
        public TextView timeTextView;
        public TextView tvReply;
        public TextView tvViewReplies;
        public ImageView ivSample;
        public View itemView;

        public CommentHolder(View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.tvAuthor);
            timeTextView = itemView.findViewById(R.id.tvTime);
            tvReply = itemView.findViewById(R.id.tvReply);
            tvViewReplies = itemView.findViewById(R.id.tv_view_replies);
            commentTextView = itemView.findViewById(R.id.tvComment);
            ivSample = itemView.findViewById(R.id.iv_sample);
            this.itemView = itemView;
        }

        public void setImage(Context context, String image) {
            Glide.with(context)
                    .load(image)
                    .into(ivSample);
        }
    }


    /**
     * Uploads image to cloud storage
     */
    private void upload() {
        final DatabaseReference newComment = FireBaseUtils.mDatabaseComment.child(postKey).push();
        final String url = newComment.getKey();

        final ProgressDialog mProgress = new ProgressDialog(CommentActivity.this);
        mProgress.setMessage("Uploading photo, please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();
        filePath = FireBaseUtils.mStoragePhotos.child("writersInbox" + "/" + url);
        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        //uploading the image
        UploadTask uploadTask = filePath.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    sendImage(newComment, task.getResult().toString());
                    mProgress.dismiss();
                } else {
                    // Handle failures
                    UploadUtils.makeNotification("Image upload failed", CommentActivity.this);
                }
            }
        });
    }

}
