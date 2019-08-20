package com.techart.writersblock;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.Chapter;
import com.techart.writersblock.models.Chapters;
import com.techart.writersblock.models.Library;
import com.techart.writersblock.models.MainContext;
import com.techart.writersblock.models.Users;
import com.techart.writersblock.utils.ImageUtils;
import com.techart.writersblock.utils.NumberUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.techart.writersblock.utils.ImageUtils.hasPermissions;


public class LibraryActivity extends AppCompatActivity {
    private RecyclerView rvReadingList;
    private ArrayList<String> contents;
    private ArrayList<String> chapterTitles;
    private int pageCount;
    private SharedPreferences mPref;

    private TextView tvSetPhoto;
    private ProgressDialog mProgress;

    private ImageView imProfilePicture;
    private String currentPhotoUrl;
    private boolean isAttached;
    // GALLERY_REQUEST is a constant integer
    private static final int GALLERY_REQUEST = 1;
    // The request code used in ActivityCompat.requestPermissions()
    // and returned in the Activity's onRequestPermissionsResult()
    private final int PERMISSION_ALL = 1;
    private final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Uri uri;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_activity);
        if (FireBaseUtils.getAuthor() != null){
            setTitle(FireBaseUtils.getAuthor());
        }
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        loadProfilePicture();

        tvSetPhoto = findViewById(R.id.tv_setPhoto);
        imProfilePicture = findViewById(R.id.ib_profile);
        TextView tvBio = findViewById(R.id.tv_biography);
        tvBio.setVisibility(View.GONE);

        FireBaseUtils.mDatabaseLibrary.child(FireBaseUtils.getUiD()).keepSynced(true);
        rvReadingList = findViewById(R.id.rv_libraryBook);
        rvReadingList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LibraryActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvReadingList.setLayoutManager(linearLayoutManager);
        bindView();
        tvSetPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPhoto();
            }
        });
    }


    private void bindView() {
        FirebaseRecyclerOptions<Library> response = new FirebaseRecyclerOptions.Builder<Library>()
                                                           .setQuery(FireBaseUtils.mDatabaseLibrary.child(FireBaseUtils.getUiD()), Library.class)
                                                           .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Library, LibraryViewHolder>(response) {
            @NonNull
            @Override
            public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.items_library, parent, false);
                return new LibraryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull LibraryViewHolder viewHolder, int position, @NonNull final Library model) {
                final String post_key = model.getPostKey();
                viewHolder.tvTitle.setText(model.getPostTitle());
                mPref = getSharedPreferences(String.format("%s",getString(R.string.app_name)),MODE_PRIVATE);
                int lastAccessedPage = mPref.getInt(post_key,-1);
                int pageCount = mPref.getInt(post_key+1,-1);
                if (lastAccessedPage != -1 && pageCount != -1){
                    viewHolder.tvTime.setText(getString(R.string.reading_progess, NumberUtils.setPlurality(lastAccessedPage + 1,"chapter"), pageCount));
                }
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // onStoryOpened(post_key);
                        initializeChapters(post_key);
                    }
                });
                viewHolder.tvRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        storyDeleted(post_key,"Remove story from reading list?");
                    }
                });
            }
        };
        rvReadingList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    private void storyExists(final String title, final String key) {

        FireBaseUtils.mDatabaseLibrary.child(FireBaseUtils.getUiD()).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(Constants.POST_KEY)) {
                    // startReadActivity(title, key);
                } else {
                    storyDeleted(key,"Story was deleted by Author");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void initializeChapters(String post_key) {
        FireBaseUtils.mDatabaseChapters.child(post_key).keepSynced(true);
        contents = new ArrayList<>();
        loadChapters(post_key);
    }


    private void loadChapters(final String post_key) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading chapters");
        progressDialog.setCancelable(true);
        progressDialog.show();

        FireBaseUtils.mDatabaseChapters.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pageCount = ((int) dataSnapshot.getChildrenCount());
                for (DataSnapshot chapterSnapShot : dataSnapshot.getChildren()) {
                    Chapter chapter = chapterSnapShot.getValue(Chapter.class);
                    contents.add(chapter.getContent());
                }

                if (contents.size() == pageCount) {
                    progressDialog.dismiss();
                    Chapters chapters = Chapters.getInstance();
                    MainContext mainContext = MainContext.getInstance();
                    chapters.setChapters(contents);
                    Intent readIntent = new Intent(mainContext.getContext(), ActivityRead.class);
                    readIntent.putExtra(Constants.POST_KEY, post_key);
                    startActivity(readIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void storyDeleted(final String key, String msg )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                FireBaseUtils.deleteFromLib(key);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_writer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_logout:
                logOut();
                break;
            case R.id.action_chat:
                Intent readIntent = new Intent(LibraryActivity.this, WritersChatRoomActivity.class);
                startActivity(readIntent);
                break;
            case R.id.action_changedp:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    onGetPermission();
                } else {
                    Intent imageIntent = new Intent();
                    imageIntent.setType("image/*");
                    imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(imageIntent, GALLERY_REQUEST);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeDialog() {
        if (isAttached) {
            mProgress.dismiss();
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
                            ActivityCompat.requestPermissions(LibraryActivity.this, PERMISSIONS, PERMISSION_ALL);
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

    private void loadProfilePicture(){
        FireBaseUtils.mDatabaseUsers.child(FireBaseUtils.getUiD()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                if (users.getImageUrl() != null && users.getImageUrl().length() > 7) {
                    currentPhotoUrl = users.getImageUrl();
                    setPicture(currentPhotoUrl);
                } else {
                    Toast.makeText(getBaseContext(),"No image found",Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setPicture(String url) {
        if (isAttached){
            Glide.with(this)
            .load(url)
            .into(imProfilePicture);
            imProfilePicture.setColorFilter(ContextCompat.getColor(this, R.color.colorTint));
        }
    }

    private void setPicture(Uri url) {
        Glide.with(this)
        .load(url)
        //.centerCrop()
        .into(imProfilePicture);
        tvSetPhoto.setVisibility(View.VISIBLE);
    }

   private void upload(){
       mProgress = new ProgressDialog(LibraryActivity.this);
       mProgress.setMessage("Uploading photo, please wait...");
       mProgress.setCanceledOnTouchOutside(false);
       mProgress.show();
       StorageReference filePath = FireBaseUtils.mStoragePhotos.child("stories/"+FireBaseUtils.getAuthor());
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
       UploadTask uploadTask2 = filePath.putBytes(data);
       uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
           @Override
           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
              // currentPhotoUrl = taskSnapshot.getDownloadUrl().toString();
               Map<String,Object> values = new HashMap<>();
              // values.put("imageUrl",taskSnapshot.getDownloadUrl().toString());
               FireBaseUtils.mDatabaseUsers.child(FireBaseUtils.getUiD()).updateChildren(values);
               tvSetPhoto.setVisibility(View.INVISIBLE);
               closeDialog();
               Toast.makeText(LibraryActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {

               Toast.makeText(LibraryActivity.this, "Upload Failed -> " + e, Toast.LENGTH_LONG).show();
           }
       });
   }

    private void deletePrompt() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentPhotoUrl);
                            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    upload();
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

    private void setPhoto() {
        if (currentPhotoUrl != null) {
            deletePrompt();
        }else {
            upload();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null){
            uri = data.getData();
            if (uri != null){
                String realPath = ImageUtils.getRealPathFromUrl(this, uri);
                Uri uriFromPath = Uri.fromFile(new File(realPath));
                setPicture(uriFromPath);
            }
        }
    }

    private void logOut() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE)
                        {
                            FirebaseAuth.getInstance().signOut();
                        }
                    }
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    public static class LibraryViewHolder extends RecyclerView.ViewHolder
    {
        final TextView tvTitle;
        final TextView tvTime;
        final TextView tvRemove;
        final View mView;

        public LibraryViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime =  itemView.findViewById(R.id.tv_timeAdded);
            tvRemove =  itemView.findViewById(R.id.tv_remove);
            this.mView = itemView;
        }
    }
}