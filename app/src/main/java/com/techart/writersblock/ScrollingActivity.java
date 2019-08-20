package com.techart.writersblock;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;

import com.techart.writersblock.constants.Constants;

/**
 * Presents the view for reading items
 */
public class ScrollingActivity extends AppCompatActivity {
    private ShareActionProvider mShareActionProvider;
    private String postTitle;
    private String postContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        postTitle = getIntent().getStringExtra(Constants.POST_TITLE);
        postContent = getIntent().getStringExtra(Constants.POST_CONTENT);
        setTitle(postTitle);
        TextView tvPoem = findViewById(R.id.tvPoem);
        tvPoem.setText(postContent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareIntent(createShareIntent());
        return true;
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT,  Constants.SENT_FROM + postTitle + "\n\n" + postContent);
        mShareActionProvider.setShareIntent(shareIntent);
        return shareIntent;
    }

    private void setShareIntent(Intent shareIntent){
        if (mShareActionProvider != null){
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
}
