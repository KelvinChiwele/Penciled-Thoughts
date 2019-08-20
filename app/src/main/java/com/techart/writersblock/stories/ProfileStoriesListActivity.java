package com.techart.writersblock.stories;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techart.writersblock.R;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Story;
import com.techart.writersblock.utils.ImageUtils;
import com.techart.writersblock.utils.UploadUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.techart.writersblock.utils.ImageUtils.hasPermissions;

public class ProfileStoriesListActivity extends AppCompatActivity {
    private RecyclerView mPoemList;
    private DatabaseReference mDatabaseStory;
    private AlertDialog updateDialog;
    private static final int GALLERY_REQUEST = 1;
    private int PERMISSION_ALL = 1;
    private String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Uri uriFromPath;
    private Uri uri;
    private ArrayList<String> contents;
    private String[] categories;
    ProgressBar progressBar;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabrecyclerviewer);
        setTitle("Stories");
        mDatabaseStory = FireBaseUtils.mDatabaseStory;
        DatabaseReference mDatabaseLike = FireBaseUtils.mDatabaseLike;
        mDatabaseLike.keepSynced(true);
        mDatabaseStory.keepSynced(true);
        mPoemList = findViewById(R.id.poem_list);
        mPoemList.setHasFixedSize(true);
        progressBar = findViewById(R.id.pb_loading);
        contents = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.categories)));
        categories = getResources().getStringArray(R.array.categories);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ProfileStoriesListActivity.this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_storylist,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent editorStoryIntent = new Intent(ProfileStoriesListActivity.this, StoryPrologueActivity.class);
            startActivity(editorStoryIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void onToggleButtonClicked(View view, String postKey) {
        ((ToggleButton) view).setChecked(((ToggleButton) view).isChecked());
        FireBaseUtils.updateStatus(((ToggleButton) view).getText().toString(),postKey);
        Toast.makeText(this,"Story marked as " + ((ToggleButton) view).getText(),Toast.LENGTH_LONG).show();
    }

    /*
    private void bindView() {
        Query query = mDatabaseStory.orderByChild(Constants.POST_AUTHOR).equalTo(FireBaseUtils.getAuthor());
        FirebaseRecyclerAdapter<Story,StoryViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Story, StoryViewHolder>(
                Story.class,R.layout.item_storyrow_del,StoryViewHolder.class, query) {
            @Override
            protected void populateViewHolder(final StoryViewHolder viewHolder, final Story model, int position) {
                final String post_key = getRef(position).getKey();
                progressBar.setVisibility(View.GONE);
                viewHolder.tvTitle.setText(model.getTitle());
                viewHolder.tvCategory.setText(model.getCategory());
                viewHolder.tbStatus.setChecked(model.getStatus().equals("Complete"));
                viewHolder.tbStatus.setTextColor(setColor(model.getStatus().equals("Complete")));

                if (model.getImageUrl() != null) {
                    viewHolder.setIvImage(ProfileStoriesListActivity.this,model.getImageUrl());
                } else {
                    viewHolder.setIvImage(ProfileStoriesListActivity.this, ImageUtils.getStoryUrl(model.getCategory().trim()));
                }

                viewHolder.tvCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateStoryDialog(post_key, model.getCategory().trim());
                    }
                });

                viewHolder.btCover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            onGetPermission();
                        }  else {
                            Intent imageIntent = new Intent();
                            imageIntent.setType("image/*");
                            imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(imageIntent,GALLERY_REQUEST);
                        }
                    }
                });

                viewHolder.tvSetCover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setPhoto(post_key,model.getImageUrl(), model.getTitle());
                    }
                });

                viewHolder.tvView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (uriFromPath != null) {
                            viewHolder.setIvImage(ProfileStoriesListActivity.this,uriFromPath);
                        } else {
                            Toast.makeText(ProfileStoriesListActivity.this,"Tap on image to upload new image",Toast.LENGTH_LONG).show();
                        }
                    }
                });

                viewHolder.tbStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onToggleButtonClicked(v,post_key);
                    }
                });
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });

                viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    deleteCaution(post_key);
                    }
                });

                viewHolder.btEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(ProfileStoriesListActivity.this,StoryDialogActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        likedPostsIntent.putExtra(Constants.CHAPTER_TITLE, model.getChapters());
                        likedPostsIntent.putExtra(Constants.STORY_CHAPTERCOUNT,model.getChapters().toString());
                        startActivity(likedPostsIntent);
                    }
                });
            }
        };
        mPoemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }*/

    private void bindView() {
        FirebaseRecyclerOptions<Story> response = new FirebaseRecyclerOptions.Builder<Story>()
                                                          .setQuery(mDatabaseStory.orderByChild(Constants.POST_AUTHOR).equalTo(FireBaseUtils.getAuthor()), Story.class)
                                                          .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Story, StoryViewHolder>(response) {
            @NonNull
            @Override
            public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_storyrow_del, parent, false);
                return new StoryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final StoryViewHolder viewHolder, int position, @NonNull final Story model) {
                final String post_key = getRef(position).getKey();
                progressBar.setVisibility(View.GONE);
                viewHolder.tvTitle.setText(model.getTitle());
                viewHolder.tvCategory.setText(model.getCategory());
                viewHolder.tbStatus.setChecked(model.getStatus().equals("Complete"));
                viewHolder.tbStatus.setTextColor(setColor(model.getStatus().equals("Complete")));

                if (model.getImageUrl() != null) {
                    viewHolder.setIvImage(ProfileStoriesListActivity.this,model.getImageUrl());
                } else {
                    viewHolder.setIvImage(ProfileStoriesListActivity.this, ImageUtils.getStoryUrl(model.getCategory().trim()));
                }

                viewHolder.tvCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateStoryDialog(post_key, model.getCategory().trim());
                    }
                });

                viewHolder.btCover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            onGetPermission();
                        }  else {
                            Intent imageIntent = new Intent();
                            imageIntent.setType("image/*");
                            imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(imageIntent,GALLERY_REQUEST);
                        }
                    }
                });

                viewHolder.tvSetCover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setPhoto(post_key,model.getImageUrl(), model.getTitle());
                    }
                });

                viewHolder.tvView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (uriFromPath != null) {
                            viewHolder.setIvImage(ProfileStoriesListActivity.this,uriFromPath);
                        } else {
                            Toast.makeText(ProfileStoriesListActivity.this,"Tap on image to upload new image",Toast.LENGTH_LONG).show();
                        }
                    }
                });

                viewHolder.tbStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onToggleButtonClicked(v,post_key);
                    }
                });
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });

                viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteCaution(post_key);
                    }
                });

                viewHolder.btEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(ProfileStoriesListActivity.this,StoryDialogActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        likedPostsIntent.putExtra(Constants.CHAPTER_TITLE, model.getChapters());
                        likedPostsIntent.putExtra(Constants.STORY_CHAPTERCOUNT,model.getChapters().toString());
                        startActivity(likedPostsIntent);
                    }
                });
            }
        };
        mPoemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }


    private int setColor(boolean isChecked)
    {
        if (isChecked){
            return R.color.colorAccent;
        }else{
            return R.color.colorPrimary;
        }
    }


    @TargetApi(23)
    private void onGetPermission() {
        // only for MarshMallow and newer versions
        if(!hasPermissions(this, PERMISSIONS)){
            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                onPermissionDenied();
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        } else {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_REQUEST);
        }
    }

    // Trigger gallery selection for a photo
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_REQUEST);
        } else {
            //do something like displaying a message that he did not allow the app to access gallery and you wont be able to let him select from gallery
            onPermissionDenied();
        }
    }

    private void onPermissionDenied() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            ActivityCompat.requestPermissions(ProfileStoriesListActivity.this, PERMISSIONS, PERMISSION_ALL);
                        }
                        if (button == DialogInterface.BUTTON_NEGATIVE) {
                            dialog.dismiss();
                        }
                    }
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("YOU NEED TO ALLOW ACCESS TO MEDIA STORAGE")
                .setMessage("Without this permission you can not upload an image")
                .setPositiveButton("ALLOW", dialogClickListener)
                .setNegativeButton("DENY", dialogClickListener)
                .show();
    }



    private void updateStoryDialog(final String post_key, String category){
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileStoriesListActivity.this);
        builder.setSingleChoiceItems(categories, contents.indexOf(category), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                FireBaseUtils.updateCategory(contents.get(item),post_key);
                updateDialog.dismiss();
                Toast.makeText(getBaseContext(),"Story Updated",Toast.LENGTH_LONG).show();
            }
        });
        updateDialog = builder.create();
        updateDialog.show();
    }


    private void setPhoto(final String post_key, final String imageUrl, final String storyTitle) {
        if (imageUrl != null) {
            deletePrompt(post_key,imageUrl,storyTitle);
        }else {
            upload(post_key,storyTitle);
        }
    }


    /**
     * Uploads image to cloud storage
     */
    private void upload(final String post_key,final String storyTitle) {
        final ProgressDialog mProgress = new ProgressDialog(ProfileStoriesListActivity.this);
        mProgress.setMessage("Uploading photo, please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();
        final StorageReference filePath = FireBaseUtils.mStoragePhotos.child("stories/"+reformatStoryTitle(storyTitle) + post_key);

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
                    sendPost(task,post_key);
                    mProgress.dismiss();
                    UploadUtils.makeNotification("Upload successful",ProfileStoriesListActivity.this);
                    finish();
                } else {
                    // Handle failures
                    UploadUtils.makeNotification("Image upload failed",ProfileStoriesListActivity.this);
                }
            }
        });

    }

    private String reformatStoryTitle(String storyTitle) {
        return storyTitle.replace(" ","_").toLowerCase();
    }

    private void sendPost(@NonNull Task<Uri> task, String post_key) {
        Map<String,Object> values = new HashMap<>();
        values.put("imageUrl",task.getResult().toString());
        FireBaseUtils.mDatabaseStory.child(post_key).updateChildren(values);
    }

    private void deletePrompt(final String post_key, final String imageUrl,final String storyTitle) {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    upload(post_key,storyTitle);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                }
                            });
                        }
                        if (button == DialogInterface.BUTTON_NEGATIVE) {
                            dialog.dismiss();
                        }
                    }
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Current display picture will be permanently deleted")
                .setPositiveButton("UPLOAD", dialogClickListener)
                .setNegativeButton("CANCEL", dialogClickListener)
                .show();
    }


    private void deleteCaution(final String post_key)
    {
        DialogInterface.OnClickListener dialogClickListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int button) {
                    if (button == DialogInterface.BUTTON_POSITIVE)
                    {
                        FireBaseUtils.deleteComment(post_key);
                        FireBaseUtils.deleteLike(post_key);
                        FireBaseUtils.deleteChapter(post_key);
                        FireBaseUtils.deleteStory(post_key);
                        FireBaseUtils.deleteFromLib(post_key);
                        dialog.dismiss();
                    }
                    if (button == DialogInterface.BUTTON_NEGATIVE)
                    {
                        dialog.dismiss();
                    }
                }
            };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Deletes all chapters, comments, likes & views related to this story")
                .setTitle("This action is irreversible!")
                .setPositiveButton("Delete", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null){
            uri = data.getData();
            if (uri != null){
                String realPath = ImageUtils.getRealPathFromUrl(this, uri);
                uriFromPath = Uri.fromFile(new File(realPath));
            }
        }
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvTitle;
        TextView tvSetCover;
        TextView tvView;
        Button tvCategory;
        Button btEdit;
        View mView;
        ImageButton btCover;

        FirebaseAuth mAUth;

        ImageButton btnDelete;

        ToggleButton tbStatus;

        public StoryViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSetCover = itemView.findViewById(R.id.tv_setCover);
            tvView = itemView.findViewById(R.id.tv_view);
            tbStatus = itemView.findViewById(R.id.tb_status);
            tvCategory = itemView.findViewById(R.id.tv_category);
            btEdit = itemView.findViewById(R.id.bt_edit);
            btCover = itemView.findViewById(R.id.btCover);

            btnDelete = itemView.findViewById(R.id.im_delete);
            this.mView = itemView;
            mAUth = FirebaseAuth.getInstance();
        }

        void setIvImage(Context context, String image) {
            Glide.with(context)
                    .load(image)
                    .into(btCover);
        }

        void setIvImage(Context context, int resourceValue) {
            Glide.with(context)
                    .load(resourceValue)
                    .into(btCover);
        }

        void setIvImage(Context context, Uri image)
        {
            Glide.with(context)
                    .load(image)
                    .into(btCover);
            tvSetCover.setVisibility(View.VISIBLE);
        }
    }
}

