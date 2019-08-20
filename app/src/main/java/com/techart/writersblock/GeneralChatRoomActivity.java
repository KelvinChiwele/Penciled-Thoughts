package com.techart.writersblock;

import android.app.ProgressDialog;
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
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Comment;
import com.techart.writersblock.models.Profile;
import com.techart.writersblock.utils.TimeUtils;
import com.techart.writersblock.utils.UploadUtils;
import com.techart.writersblock.viewholders.MessageHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GeneralChatRoomActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private RecyclerView mCommentList;
    private EditText mEtComment;
    private Boolean isSent;
    private String time;
    private ProgressBar progressBar;
    private static final int GALLERY_REQUEST = 1;
    private String postKey;
    private Uri uri;
    StorageReference filePath;
    private String currentMessage;

    private PopupMenu popupMenu;
    private final static int EDIT = 1;

    private final static int DELETE = 2;
    private final static int CANCEL = 3;
    private final static int NEW = 4;

    private  int menuAction;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        setTitle("Chat Room");
        mCommentList = findViewById(R.id.comment_recyclerview);
        mCommentList.setHasFixedSize(true);
        progressBar = findViewById(R.id.pb_loading);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(GeneralChatRoomActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mCommentList.setLayoutManager(linearLayoutManager);
        menuAction = NEW;
        init();
        initCommentSection();
    }

    private void initCommentSection() {
        FirebaseRecyclerOptions<Comment> response = new FirebaseRecyclerOptions.Builder<Comment>()
                                                            .setQuery(FireBaseUtils.mDatabaseGeneralChats, Comment.class)
                                                            .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, MessageHolder>(response) {
            @NonNull
            @Override
            public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_inbox, parent, false);
                return new MessageHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final MessageHolder viewHolder, int position, @NonNull final Comment model) {
                progressBar.setVisibility(View.GONE);
                final String post_key = getRef(position).getKey();
                final String current_message = model.getCommentText();
                if (model.getAuthorUrl() != null){
                    setVisibility(model.getAuthorUrl(),viewHolder);
                }
                if (model.getAuthor() != null){
                    viewHolder.authorTextView.setText(model.getAuthor());
                }

                if (model.getCommentText() != null){
                    viewHolder.commentTextView.setText(model.getCommentText());
                    viewHolder.ivSample.setVisibility(View.GONE);
                } else {
                    viewHolder.setImage(getApplicationContext(),model.getImageUrl());
                    viewHolder.commentTextView.setVisibility(View.GONE);
                }

                if (model.getTimeCreated() != null){
                    time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.timeTextView.setText(time);
                }

                viewHolder.ivSample.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (model.getImageUrl() != null){
                            Intent intent = new Intent(GeneralChatRoomActivity.this,FullImageActivity.class);
                            intent.putExtra(Constants.IMAGE_URL,model.getImageUrl());
                            startActivity(intent);
                        }
                    }
                });

                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (FireBaseUtils.getUiD() != null  && FireBaseUtils.getUiD().equals(model.getAuthorUrl())){
                            postKey = post_key;
                            currentMessage = current_message;
                            popupMenu = new PopupMenu(GeneralChatRoomActivity.this, viewHolder.commentTextView);
                            //ToDo final issue
                            popupMenu.getMenu().add(Menu.NONE, EDIT, Menu.NONE, "Edit");
                            popupMenu.getMenu().add(Menu.NONE, DELETE, Menu.NONE, "Delete");
                            popupMenu.getMenu().add(Menu.NONE, CANCEL, Menu.NONE, "Cancel");
                            popupMenu.setOnMenuItemClickListener(GeneralChatRoomActivity.this);
                            popupMenu.show();
                            return true;
                        } else {
                            return  false;
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

    private void setVisibility(String url, MessageHolder viewHolder) {
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
        switch (v.getId()) {
            case R.id.iv_send:
                if (menuAction == NEW){
                    sendComment();
                } else {
                    editMessage();
                }
                menuAction = NEW;
                break;
            case R.id.iv_image:
                Intent intent = new Intent(GeneralChatRoomActivity.this,ImageActivity.class);
                postKey = getIntent().getStringExtra(Constants.POST_KEY);
                startActivityForResult(intent,GALLERY_REQUEST);
                break;
        }
    }

    private void sendComment() {
        final String comment = mEtComment.getText().toString().trim();
        isSent = false;
        if (!comment.isEmpty()) {
            final ProgressDialog progressDialog = new ProgressDialog(GeneralChatRoomActivity.this);
            progressDialog.setMessage("Sending comment..");
            progressDialog.setCancelable(true);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            FireBaseUtils.mDatabaseGeneralChats.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!isSent) {
                        DatabaseReference newComment = FireBaseUtils.mDatabaseGeneralChats.push();
                        Map<String,Object> values = new HashMap<>();
                        values.put(Constants.AUTHOR_URL,FireBaseUtils.getUiD());
                        values.put(Constants.POST_AUTHOR,FireBaseUtils.getAuthor());
                        values.put(Constants.COMMENT_TEXT,comment);
                        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
                        newComment.setValue(values);
                        FireBaseUtils.updateNotifications("generalInbox","","Reader","wrote a new message",newComment.getKey(),comment, Profile.getInstance().getImageUrl());
                        isSent = true;
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

    private void sendImage(DatabaseReference newComment,String imageUrl) {
        final ProgressDialog progressDialog = new ProgressDialog(GeneralChatRoomActivity.this);
        progressDialog.setMessage("Sending answer...");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.AUTHOR_URL, FireBaseUtils.getUiD());
        values.put(Constants.POST_AUTHOR, FireBaseUtils.getAuthor());
        values.put(Constants.IMAGE_URL, imageUrl);
        values.put(Constants.POST_KEY, postKey);
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        newComment.setValue(values);
        progressDialog.dismiss();
        FireBaseUtils.updateNotifications("generalInbox","","Reader","sent an image",newComment.getKey(),"", Profile.getInstance().getImageUrl());
    }

    @Override
    public void onBackPressed() {
        finish();
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

    private void deleteMessage(){
        FireBaseUtils.mDatabaseGeneralChats.child(postKey).removeValue();
        Toast.makeText(this,"Message deleted",Toast.LENGTH_LONG).show();
    }

    private void editMessage(){
        final String comment = mEtComment.getText().toString().trim();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.COMMENT_TEXT,comment);
        FireBaseUtils.mDatabaseGeneralChats.child(postKey).updateChildren(values);
        mEtComment.setText("");
        Toast.makeText(this,"Message updated",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            uri = Uri.parse(data.getStringExtra(Constants.URI));
            postKey = getIntent().getStringExtra(Constants.POST_KEY);
            upload();
        }
    }

    /**
     * Uploads image to cloud storage
     */
    private void upload() {
        final DatabaseReference newComment = FireBaseUtils.mDatabaseGeneralChats.push();
        final String url = newComment.getKey();
        final ProgressDialog mProgress = new ProgressDialog(GeneralChatRoomActivity.this);
        mProgress.setMessage("Uploading photo, please wait...");
        mProgress.setProgress(0);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();
        filePath = FireBaseUtils.mStoragePhotos.child("generalChats" + "/" + url);
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
                    UploadUtils.makeNotification("Image upload failed", GeneralChatRoomActivity.this);
                }
            }
        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                mProgress.setProgress(currentProgress);
            }
        });
    }

}
