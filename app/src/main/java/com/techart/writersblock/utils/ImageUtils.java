package com.techart.writersblock.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;

import com.techart.writersblock.R;

/**
 * Class for working with images
 * Created by Kelvin on 17/09/2017.
 */

public final class ImageUtils {

    private ImageUtils() {
    }

    public static int getStoryUrl(String category) {
        switch (category) {
            case "Action":
                return R.drawable.action;
            case "Adventure":
                return R.drawable.adventure;
            case "Comedy":
                return R.drawable.comedy;
            case "Drama":
                return R.drawable.drama;
            case "Fiction":
                return R.drawable.fiction;
            case "Horror":
                return R.drawable.horror;
            case "Mystery":
                return R.drawable.mystery;
            case "Romance":
                return R.drawable.romance;
            case "Sci-Fi":
                return R.drawable.scifi;
            case "Folklore":
                return R.drawable.folklore;
            case "Thriller":
                return R.drawable.horror;
            case "Tragedy":
                return R.drawable.tragedy;
            case "Zambian":
                return R.drawable.zambian;
            default:
                return R.drawable.romance;
        }
    }

    public static int getPoemUrl() {
        return R.drawable.poem;
    }

    public static int getDevotionUrl() {
        return R.drawable.devotion;
    }

    public static String getRealPathFromUrl(Context context, Uri imageUrl) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(imageUrl,projection,null,null,null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
