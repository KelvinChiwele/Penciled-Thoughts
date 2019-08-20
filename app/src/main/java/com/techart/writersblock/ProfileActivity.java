package com.techart.writersblock;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.devotion.DevotionEditorActivity;
import com.techart.writersblock.devotion.MySpiritualsListActivity;
import com.techart.writersblock.devotion.ProfileDevotionsListActivity;
import com.techart.writersblock.models.Users;
import com.techart.writersblock.poems.MyPoemsListActivity;
import com.techart.writersblock.poems.PoemEditorActivity;
import com.techart.writersblock.poems.ProfilePoemsListActivity;
import com.techart.writersblock.stories.MyStoriesListActivity;
import com.techart.writersblock.stories.ProfileStoriesListActivity;
import com.techart.writersblock.stories.StoryPrologueActivity;
import com.techart.writersblock.utils.ImageUtils;
import com.techart.writersblock.utils.UploadUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.techart.writersblock.utils.ImageUtils.hasPermissions;

/**
 * Displays users private content. Such as
 * 1. Posted items
 * 2. Locally stored Articles
 * 3. Action such as changing and setting of dps
 */
public class ProfileActivity extends AppCompatActivity {
    private TextView tvSetPhoto;
    private TextView tvBio;
    private ProgressDialog mProgress;
    private ImageView imProfilePicture;
    private String currentPhotoUrl;
    private String currentFacebookUrl;
    private String currentBiographyUrl;
    private boolean isAttached;


    // GALLERY_REQUEST is a constant integer
    private static final int GALLERY_REQUEST = 1;
    // The request code used in ActivityCompat.requestPermissions()
    // and returned in the Activity's onRequestPermissionsResult()
    private int PERMISSION_ALL = 1;
    private String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Uri uri;
    FloatingActionButton fbPoems;
    FloatingActionButton fbDevotion;
    FloatingActionButton fbStories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle(FireBaseUtils.getAuthor());
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        loadProfilePicture();
        tvSetPhoto = findViewById(R.id.tv_setPhoto);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tv_readingList = findViewById(R.id.tv_readingList);
        imProfilePicture = findViewById(R.id.ib_profile);
        RelativeLayout mypoems = findViewById(R.id.mypoems);
        RelativeLayout myspirituals = findViewById(R.id.rv_myspirituals);
        RelativeLayout mystories = findViewById(R.id.rv_mystories);
        RelativeLayout postedPoems = findViewById(R.id.rv_postedpoems);
        RelativeLayout postedSpirituals = findViewById(R.id.rv_postedspirituals);
        RelativeLayout postedStories = findViewById(R.id.rv_postedstories);
        tvBio = findViewById(R.id.tv_biography);
        TextView tvFaceBook = findViewById(R.id.tv_facebook);
        // fab = findViewById(R.id.fab);


        //Sets new DP buy first deleting existing one
        tvSetPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPhoto();
            }
        });
        tvBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bioIntent = new Intent(ProfileActivity.this, BiographyActivity.class);
                bioIntent.putExtra(Constants.FACEBOOK,currentBiographyUrl);
                startActivity(bioIntent);
            }
        });
        tv_readingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, LibraryActivity.class);
                startActivity(intent);
            }
        });
        //Handles on clicks which brings up a larger image than that displayed
        imProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDo handle on image button clicks
            }
        });
        mypoems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MyPoemsListActivity.class);
                startActivity(intent);
            }
        });
        myspirituals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MySpiritualsListActivity.class);
                startActivity(intent);
            }
        });
        mystories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MyStoriesListActivity.class);
                startActivity(intent);
            }
        });

        postedPoems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfilePoemsListActivity.class);
                startActivity(intent);
            }
        });
        postedSpirituals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileDevotionsListActivity.class);
                startActivity(intent);
            }
        });

        postedStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileStoriesListActivity.class);
                startActivity(intent);
            }
        });
        fbPoems = findViewById(R.id.item_poem);
        fbPoems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iInformation = new Intent(ProfileActivity.this, PoemEditorActivity.class);
                startActivity(iInformation);
            }
        });

        fbDevotion = findViewById(R.id.item_devotion);
        fbDevotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iInformation = new Intent(ProfileActivity.this, DevotionEditorActivity.class);
                startActivity(iInformation);
            }
        });

        fbStories = findViewById(R.id.item_story);
        fbStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iInformation = new Intent(ProfileActivity.this, StoryPrologueActivity.class);
                startActivity(iInformation);
            }
        });

        tvFaceBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bioIntent = new Intent(ProfileActivity.this, FacebookActivity.class);
                bioIntent.putExtra(Constants.FACEBOOK,currentFacebookUrl);
                startActivity(bioIntent);
            }
        });
        FirebaseMessaging.getInstance().subscribeToTopic("writers");
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
            case R.id.action_chat:
                Intent readIntent = new Intent(ProfileActivity.this, WritersChatRoomActivity.class);
                startActivity(readIntent);
                break;
            case R.id.action_bio:
                Intent bioIntent = new Intent(ProfileActivity.this, BiographyActivity.class);
                startActivity(bioIntent);
                break;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                        ActivityCompat.requestPermissions(ProfileActivity.this, PERMISSIONS, PERMISSION_ALL);
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

                if (users.getBiography() != null && !users.getBiography().isEmpty() ){
                    tvBio.setText(users.getBiography());
                    currentBiographyUrl = users.getFacebook();
                } else {
                    tvBio.setVisibility(View.GONE);
                }

                if (users.getFacebook() != null){
                    currentFacebookUrl = users.getFacebook();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setPicture(String url) {
        if (isAttached){
            Glide.with(this)
                .load(url)
                    //.centerCrop()
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


    /**
     * Uploads image to cloud storage
     */
    private void upload() {
        mProgress = new ProgressDialog(ProfileActivity.this);
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setMessage("Uploading, please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setProgress(0);
        mProgress.show();
        final StorageReference filePath = FireBaseUtils.mStoragePhotos.child("profiles/"+FireBaseUtils.getAuthor());
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
                    sendPost(task);
                    tvSetPhoto.setVisibility(View.INVISIBLE);
                    mProgress.dismiss();
                    UploadUtils.makeNotification("Upload successful",ProfileActivity.this);
                    finish();
                } else {
                    // Handle failures
                    UploadUtils.makeNotification("Image upload failed",ProfileActivity.this);
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

    private void sendPost(@NonNull Task<Uri> task) {
        Map<String,Object> values = new HashMap<>();
        values.put("imageUrl",task.getResult().toString());
        FireBaseUtils.mDatabaseUsers.child(FireBaseUtils.getUiD()).updateChildren(values);
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
            if (button == DialogInterface.BUTTON_POSITIVE)  {
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
}
