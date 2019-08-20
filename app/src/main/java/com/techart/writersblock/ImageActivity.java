package com.techart.writersblock;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.techart.writersblock.constants.Constants;
import com.techart.writersblock.utils.ImageUtils;

import java.io.File;

import static com.techart.writersblock.utils.ImageUtils.hasPermissions;


public class ImageActivity extends AppCompatActivity implements View.OnClickListener {

    ProgressBar progressBar;
    //string resources

    private String realPath;


    TextView tvAuthor;
    TextView tvTime;
    // ImageView iv_sample;

    private String imageUrl;
    PhotoView iv_sample;

    //image
    private static final int GALLERY_REQUEST = 1;
    private Uri uri;

    //Permission
    private int PERMISSION_ALL = 1;
    private String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);

        imageUrl = getIntent().getStringExtra(Constants.IMAGE_URL);
        iv_sample = findViewById(R.id.iv_sample);
        Glide.with(ImageActivity.this)
                .load(imageUrl)
                .into(iv_sample);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_send) {
            exit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (uri == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                onGetPermission();
            } else {
                Intent imageIntent = new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(imageIntent, GALLERY_REQUEST);
            }
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
                            ActivityCompat.requestPermissions(ImageActivity.this, PERMISSIONS, PERMISSION_ALL);
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
            realPath = ImageUtils.getRealPathFromUrl(this, uri);
            Uri uriFromPath = Uri.fromFile(new File(realPath));
            setImage(iv_sample, uriFromPath);
        } else {
            finish();
        }
    }

    /**
     * inflates image into the image view
     *
     * @param image       component into which image will be inflated
     * @param uriFromPath uri of image to be inflated
     */
    private void setImage(ImageView image, Uri uriFromPath) {
        RequestOptions options = new RequestOptions()
                .centerCrop();
        Glide.with(this)
                .load(uriFromPath)
                .apply(options)
                .into(image);
    }

    public void exit() {
        Intent data = new Intent();
        data.putExtra(Constants.URI, uri.toString());
        setResult(RESULT_OK, data);
        finish();
    }
}
