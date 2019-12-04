package com.techart.writersblock;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.constants.FireBaseUtils;
import com.techart.writersblock.models.MainContext;
import com.techart.writersblock.models.Profile;
import com.techart.writersblock.models.Stamp;
import com.techart.writersblock.models.Users;
import com.techart.writersblock.setup.LoginActivity;
import com.techart.writersblock.tabs.Tab1Stories;
import com.techart.writersblock.tabs.Tab2Poems;
import com.techart.writersblock.tabs.Tab3Devotion;
import com.techart.writersblock.utils.ImageUtils;
import com.techart.writersblock.utils.UploadUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static android.widget.Toast.LENGTH_LONG;
import static com.techart.writersblock.utils.ImageUtils.hasPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {
    //image
    private static final int GALLERY_REQUEST = 1;
    private FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    public static final String VERSION_CODE_KEY = "latest_app_version";

    //Permission
    private final int PERMISSION_ALL = 1;
    RelativeLayout linearLayout;
    TextView tvUpload;
    ImageButton ibDp;
    ImageButton ibCancel;
    StorageReference filePath;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private Intent intent;
    private ViewPager vp;
    private boolean isAttached;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private FloatingActionButton fab;
    private int mStamp;
    private int lastAccessedTime;
    private TextView textCartItemCount;
    private int mCartItemCount = 0;
    private Uri uri;
    private String currentPhotoUrl;
    private String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    MainContext mainContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser()==null) {
                    Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(loginIntent);
                }
            }
        };
        //Tabs
        setSupportActionBar(toolbar);


        //VIEWPAGER
        vp= findViewById(R.id.container);
        this.addPages();
        //TABLAYOUT
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(vp);
        tabLayout.addOnTabSelectedListener(this);
        //Setting the Default Map Value with the current version code
        HashMap<String, Object> firebaseDefaultMap = new HashMap<>();
        firebaseDefaultMap.put(VERSION_CODE_KEY, getCurrentVersionCode());
        mFirebaseRemoteConfig.setDefaults(firebaseDefaultMap);
        remoteConfigSetUp();


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iInformation = new Intent(MainActivity.this,GeneralChatRoomActivity.class);
                startActivity(iInformation);
            }
        });
        mPref = getSharedPreferences(String.format("%s",getString(R.string.app_name)),MODE_PRIVATE);
        haveNetworkConnection();
        setupDrawer(toolbar);
        loadProfilePicture();
       // AppRater.app_launched(MainActivity.this);
        FirebaseMessaging.getInstance().subscribeToTopic("all");
    }

    private void addPages() {
        MyPageAdapter pagerAdapter=new MyPageAdapter(this.getSupportFragmentManager());
        pagerAdapter.addFragment(new Tab1Stories());
        pagerAdapter.addFragment(new Tab2Poems());
        pagerAdapter.addFragment(new Tab3Devotion());
        vp.setOffscreenPageLimit(1);
        //SET ADAPTER TO VP
        vp.setAdapter(pagerAdapter);
    }

    public void onTabSelected(TabLayout.Tab tab) {
        vp.setCurrentItem(tab.getPosition());
    }
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }
    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.getTabAt(position).select();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void setupDrawer(Toolbar toolbar) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            //Name, email address, and profile photo Url
            FirebaseMessaging.getInstance().subscribeToTopic("all");
            TextView tvUser = header.findViewById(R.id.tvUser);
            TextView tvEmail = header.findViewById(R.id.tvEmail);
            tvUpload = header.findViewById(R.id.tv_upload);
            ibDp = header.findViewById(R.id.iv_change);
            ibCancel = header.findViewById(R.id.iv_remove);
            linearLayout = header.findViewById(R.id.ll_header_main);

            if (currentPhotoUrl != null && currentPhotoUrl.length() > 7) {
                setIvImage(currentPhotoUrl);
            }
            tvUser.setText(FireBaseUtils.getAuthor());
            tvEmail.setText(FireBaseUtils.getEmail());

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentPhotoUrl != null && currentPhotoUrl.length() > 7) {
                        /*Intent intent = new Intent(MainActivity.this, FullImageActivity.class);
                        intent.putExtra(Constants.IMAGE_URL, currentPhotoUrl);
                        startActivity(intent);*/
                    } else {
                        Toast.makeText(MainActivity.this, "No image, please upload", Toast.LENGTH_LONG).show();
                    }

                }
            });

            ibDp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        onGetPermission();
                    } else {
                        Intent imageIntent = new Intent();
                        imageIntent.setType("image/*");
                        imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(imageIntent, GALLERY_REQUEST);
                    }
                }
            });

            ibCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvUpload.setVisibility(View.GONE);
                    ibDp.setVisibility(View.VISIBLE);
                    ibCancel.setVisibility(View.GONE);
                    Glide.with(MainActivity.this)
                            .load(R.drawable.placeholder)
                            .into(new SimpleTarget<Drawable>() {
                                @Override
                                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        linearLayout.setBackground(resource);
                                    }
                                }
                            });
                }
            });

            tvUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvUpload.setVisibility(View.GONE);
                    upload();
                }
            });
        }
    }

    /**
     * Uploads image to cloud storage
     */
    private void upload() {
        final ProgressDialog mProgress = new ProgressDialog(MainActivity.this);
        mProgress.setMessage("Uploading photo, please wait...");
        mProgress.setProgress(0);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();
        filePath = FireBaseUtils.mStoragePhotos.child("Profiles" + "/" + FireBaseUtils.getUiD());
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
                    updateProfile(task);
                    mProgress.dismiss();
                    UploadUtils.makeNotification("Image upload complete", MainActivity.this);
                } else {
                    // Handle failures
                    UploadUtils.makeNotification("Image upload failed", MainActivity.this);

                }
                ibDp.setVisibility(View.VISIBLE);
                tvUpload.setVisibility(View.GONE);
                ibCancel.setVisibility(View.GONE);
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

    private void updateProfile(@NonNull Task<Uri> task) {
        FireBaseUtils.mDatabaseUsers.child(FireBaseUtils.getUiD()).child(Constants.IMAGE_URL).setValue(task.getResult().toString());
        loadProfilePicture();
    }

    private void loadProfilePicture() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FireBaseUtils.mDatabaseUsers.child(FireBaseUtils.getUiD()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Users users = dataSnapshot.getValue(Users.class);
                    Profile profile = Profile.getInstance();
                    if (users.getImageUrl() != null && users.getImageUrl().length() > 7) {
                        currentPhotoUrl = users.getImageUrl();
                        setIvImage(users.getImageUrl());
                        profile.setImageUrl(currentPhotoUrl);
                    }

                    if (users.getSignedAs() != null){
                        profile.setSignedAs(users.getSignedAs());
                    }

                    if (users.getBiography() != null && !users.getBiography().isEmpty() ){
                        profile.setBiography(users.getBiography());
                    }

                    if (users.getFacebook() != null){
                        profile.setFacebook(users.getFacebook());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    /**
     * requests for permission in android >= 23
     */
    @TargetApi(23)
    private void onGetPermission() {
        // only for MarshMallow and newer versions
        if (!hasPermissions(this, PERMISSIONS)) {
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

    /**
     * Trigger gallery selection for a photo
     *
     * @param requestCode
     * @param permissions  permissions to be requested
     * @param grantResults granted results
     */
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

    /**
     * Displays when permission is denied
     */
    private void onPermissionDenied() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
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


    public void setIvImage(String ivImage) {
        RequestOptions options = new RequestOptions()
                .fitCenter();
        Glide.with(this)
        .load(ivImage)
        .apply(options)
        .into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    linearLayout.setBackground(resource);
                }
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mainContext = MainContext.getInstance();
        mainContext.setContext(MainActivity.this);
        mStamp = mPref.getInt(Constants.STAMP_KEY,0);
        stamp();
    }

    private  void remoteConfigSetUp(){
        //Setting Developer Mode enabled to fast retrieve the values
        mFirebaseRemoteConfig.setConfigSettings(
                new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG)
                        .build());

        //Fetching the values here
        mFirebaseRemoteConfig.fetch().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                    //calling function to check if new version is available or not
                    int latestAppVersion = (int) mFirebaseRemoteConfig.getDouble(VERSION_CODE_KEY);
                    if (latestAppVersion > getCurrentVersionCode()) {
                        showUpdateAppDialog();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong please try again",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_library) {
            startLibraryActivity();
        } else if (id == R.id.nav_exit) {
            logOut();
        } else if (id == R.id.nav_about) {
            Intent editorDevotionIntent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(editorDevotionIntent);
        }
        else if (id == R.id.nav_facebook) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/penciledthoughts/"));
            startActivity(browserIntent);
        }  else if (id == R.id.nav_help) {
            Intent editorDevotionIntent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(editorDevotionIntent);
        }   else if (id == R.id.nav_writers) {
            Intent editorDevotionIntent = new Intent(MainActivity.this, WritersActivity.class);
            startActivity(editorDevotionIntent);
        }   else if (id == R.id.nav_shareapp) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.techart.writersblock");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startLibraryActivity() {
        switch (Profile.getInstance().getSignedAs()) {
            case "Writer": {
                Intent accountIntent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(accountIntent);
                break;
            }
            case "Reader": {
                Intent accountIntent = new Intent(MainActivity.this, LibraryActivity.class);
                startActivity(accountIntent);
                break;
            }
            default:
                Toast.makeText(MainActivity.this, "Could not open library ", LENGTH_LONG).show();
                break;
        }
    }

    private int getCurrentVersionCode() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not read app version code", Toast.LENGTH_LONG).show();
        }
        return -1;
    }

    /**
     * Checks for internet connection
     */
    private void haveNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
            if (netWorkInfo != null && netWorkInfo.getState() == NetworkInfo.State.CONNECTED) {
                Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Toast.makeText(getApplicationContext(),"No internet Connection", Toast.LENGTH_LONG).show();
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     * @param menu menu to be inflated
     * @return return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_notifications);

        View actionView = MenuItemCompat.getActionView(menuItem);
        textCartItemCount = actionView.findViewById(R.id.cart_badge);

        setupBadge();

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });
        return true;
    }

    private void setupBadge() {
        if (textCartItemCount != null) {
            if (mCartItemCount == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * Resolves section of the menu which was clicked
     * @param item selected menu item
     * @return onClick status
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            intent = new Intent(MainActivity.this, NotificationsActivity.class);
            intent.putExtra(Constants.STAMP_KEY, lastAccessedTime);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Loads the list of staff from database to Shared preferences for easy access
     */
    private void stamp() {
        FireBaseUtils.mDatabaseStamp.child("-LIaZ1jO8SXe4peE3JAc").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Stamp stamp = dataSnapshot.getValue(Stamp.class);
                lastAccessedTime = stamp.getTimeCreated().intValue();
                if (textCartItemCount != null) {
                    if (mStamp >= lastAccessedTime) {
                        textCartItemCount.setVisibility(View.GONE);
                    } else {
                        textCartItemCount.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showUpdateAppDialog() {
        if (isAttached){
            DialogInterface.OnClickListener dialogClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            if (button == DialogInterface.BUTTON_POSITIVE){
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.techart.writersblock"));
                                startActivity(browserIntent);
                            }
                        }
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.outdated))
                    .setMessage(getString(R.string.urgent))
                    .setCancelable(false)
                    .setPositiveButton("UPDATE", dialogClickListener)
                    .show();
        }

    }

    /**
     * Called upon selecting an image
     *
     * @param requestCode
     * @param resultCode  was operation successful or not
     * @param data        data returned from the operation
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            String realPath = ImageUtils.getRealPathFromUrl(this, uri);
            Uri uriFromPath = Uri.fromFile(new File(realPath));
            setIvImage(uriFromPath);
        }
    }

    public void setIvImage(Uri ivImage) {
        tvUpload.setVisibility(View.VISIBLE);
        ibDp.setVisibility(View.GONE);
        ibCancel.setVisibility(View.VISIBLE);
        Glide.with(this)
            .load(ivImage)
            .into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        linearLayout.setBackground(resource);
                    }
                }
            });
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
}
