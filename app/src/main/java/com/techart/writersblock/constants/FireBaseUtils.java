package com.techart.writersblock.constants;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.techart.writersblock.R;
import com.techart.writersblock.models.Devotion;
import com.techart.writersblock.models.Poem;
import com.techart.writersblock.models.Story;

import java.util.HashMap;
import java.util.Map;

/**
 * Has constants for Fire base variable names
 * Created by Kelvin on 11/09/2017.
 */

public final class FireBaseUtils {

    public static final DatabaseReference mDatabaseStory = FirebaseDatabase.getInstance().getReference().child(Constants.STORY_KEY);
    public static final DatabaseReference mDatabaseNumber = FirebaseDatabase.getInstance().getReference().child(Constants.NUMBER_KEY);
    public static final DatabaseReference mDatabasePoems = FirebaseDatabase.getInstance().getReference().child(Constants.POEM_KEY);
    public static final DatabaseReference mDatabaseDevotions = FirebaseDatabase.getInstance().getReference().child(Constants.DEVOTION_KEY);
    public static final DatabaseReference mDatabaseNotifications = FirebaseDatabase.getInstance().getReference().child(Constants.NOTIFICATION_KEY);
    public static final DatabaseReference mDatabaseChapters = FirebaseDatabase.getInstance().getReference().child(Constants.CHAPTER_KEY);
    public static final DatabaseReference mDatabaseLike = FirebaseDatabase.getInstance().getReference().child(Constants.LIKE_KEY);
    public static final DatabaseReference mDatabaseComment = FirebaseDatabase.getInstance().getReference().child(Constants.COMMENTS_KEY);
    public static final DatabaseReference mDatabaseChaptersComment = FirebaseDatabase.getInstance().getReference().child(Constants.CHAPTERS_COMMENTS_KEY);
    public static final DatabaseReference mDatabaseWritersChat = FirebaseDatabase.getInstance().getReference().child(Constants.WRITERS_CHATS_KEY);

    public static final DatabaseReference mDatabaseGeneralChats = FirebaseDatabase.getInstance().getReference().child(Constants.GENERAL_CHATS_KEY);
    public static final DatabaseReference mDatabaseReplies = FirebaseDatabase.getInstance().getReference().child(Constants.REPLIES_KEY);
    public static final DatabaseReference mDatabaseChapterReplies = FirebaseDatabase.getInstance().getReference().child(Constants.CHAPTER_REPLIES_KEY);
    public static final DatabaseReference mDatabaseViews = FirebaseDatabase.getInstance().getReference().child(Constants.VIEWS_KEY);
    public static final DatabaseReference mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);
    public static final DatabaseReference mDatabaseLibrary = FirebaseDatabase.getInstance().getReference().child(Constants.LIBRARY);
    public static final DatabaseReference mDatabaseStamp = FirebaseDatabase.getInstance().getReference().child(Constants.STAMP_KEY);
    public static final DatabaseReference mDatabaseVersion = FirebaseDatabase.getInstance().getReference().child(Constants.VERSION_KEY);

    public static final StorageReference mStoragePhotos = FirebaseStorage.getInstance().getReference();
    public static String status;


    private FireBaseUtils() {

    }

    public static void isComplete(String status, DatabaseReference databaseReference) {
        if (status.equals("Complete")) {
            databaseReference.keepSynced(false);
        } else {
            databaseReference.keepSynced(true);
        }
    }

    @NonNull
    public static String getEmail() {
        return FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }


    public static void updateNotifications(String category,String title, String signedAs, String action, String postUrl, String message, String imageUrl) {
        String url = mDatabaseNotifications.push().getKey();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.MESSAGE,message);
        values.put(Constants.ACTION,action);
        values.put(Constants.POST_TITLE,title);
        values.put(Constants.SIGNED_IN_AS,signedAs);
        values.put(Constants.AUTHOR_URL,getUiD());
        values.put(Constants.IMAGE_URL,imageUrl);
        values.put(Constants.POST_TYPE,category);
        values.put(Constants.EMAIL, getEmail());
        values.put(Constants.USER, getAuthor());
        values.put(Constants.POST_KEY, postUrl);
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        mDatabaseNotifications.child(url).setValue(values);
        stamp();
    }

    public static void stamp() {
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        mDatabaseStamp.child("-LIaZ1jO8SXe4peE3JAc").setValue(values);
    }

    @NonNull
    public static String getAuthor() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getDisplayName(); //(a > b) ? a : b
    }

    @NonNull
    public static String getUiD() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getUid();
    }

    public static void setLikeBtn(final String post_key, final ImageView btnLiked) {
        mDatabaseLike.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null && dataSnapshot.child(getUiD()).hasChild(Constants.AUTHOR_URL)) {
                    btnLiked.setImageResource(R.drawable.ic_thumb_up_blue_24dp);
                } else {
                    btnLiked.setImageResource(R.drawable.ic_thumb_up_grey_24dp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void setPostViewed(final String post_key, final ImageView btViewed) {
        mDatabaseViews.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null && dataSnapshot.child(getUiD()).hasChild(Constants.AUTHOR_URL)) {
                    btViewed.setImageResource(R.drawable.ic_visibility_blue_24px);
                } else {
                    btViewed.setImageResource(R.drawable.ic_visibility_grey_24px);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void onStoryDisliked(String post_key) {
        mDatabaseStory.child(post_key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Story story = mutableData.getValue(Story.class);
                if (story == null) {
                    return Transaction.success(mutableData);
                }
                story.setNumLikes(story.getNumLikes() - 1);
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

    public static void onStoryLiked(String post_key) {
        mDatabaseStory.child(post_key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Story story = mutableData.getValue(Story.class);
                if (story == null) {
                    return Transaction.success(mutableData);
                }
                story.setNumLikes(story.getNumLikes() + 1);
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

    public static void onDevotionDisliked(String post_key) {
        mDatabaseDevotions.child(post_key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Devotion devotion = mutableData.getValue(Devotion.class);
                if (devotion == null) {
                    return Transaction.success(mutableData);
                }
                devotion.setNumLikes(devotion.getNumLikes() - 1);
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

    public static void onDevotionLiked(String post_key) {
        mDatabaseDevotions.child(post_key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Devotion devotion = mutableData.getValue(Devotion.class);
                if (devotion == null) {
                    return Transaction.success(mutableData);
                }
                devotion.setNumLikes(devotion.getNumLikes() + 1);
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

    public static void onPoemDisliked(String post_key) {
        mDatabasePoems.child(post_key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Poem poem = mutableData.getValue(Poem.class);
                if (poem == null) {
                    return Transaction.success(mutableData);
                }
                poem.setNumLikes(poem.getNumLikes() - 1);
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

    public static void onPoemLiked(String post_key) {
        mDatabasePoems.child(post_key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Poem poem = mutableData.getValue(Poem.class);
                if (poem == null) {
                    return Transaction.success(mutableData);
                }
                poem.setNumLikes(poem.getNumLikes() + 1);
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


    public static void updateStatus(String status, String post_key) {
        mDatabaseStory.child(post_key).child(Constants.STORY_STATUS).setValue(status);
    }

    public static void updateCategory(String category, String post_key) {
        mDatabaseStory.child(post_key).child(Constants.STORY_CATEGORY).setValue(category);
    }

    public static void addStoryLike(Story model, String post_key) {
        Map<String, Object> values = new HashMap<>();
        values.put(Constants.AUTHOR_URL, getUiD());
        values.put(Constants.USER, FireBaseUtils.getAuthor());
        values.put(Constants.POST_TITLE, model.getTitle());
        values.put(Constants.POST_TYPE, Constants.STORY_HOLDER);
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        mDatabaseLike.child(post_key).child(getUiD()).setValue(values);
    }

    public static void addDevotionLike(Devotion model, String post_key) {
        Map<String, Object> values = new HashMap<>();
        values.put(Constants.AUTHOR_URL, getUiD());
        values.put(Constants.USER, getAuthor());
        values.put(Constants.POST_TITLE, model.getTitle());
        values.put(Constants.POST_TYPE, Constants.DEVOTION_HOLDER);
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        mDatabaseLike.child(post_key).child(getUiD()).setValue(values);
    }

    public static void addPoemLike(Poem model, String post_key) {
        Map<String, Object> values = new HashMap<>();
        values.put(Constants.AUTHOR_URL, getUiD());
        values.put(Constants.USER, getAuthor());
        values.put(Constants.POST_TITLE, model.getTitle());
        values.put(Constants.POST_TYPE, Constants.POEM_HOLDER);
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        mDatabaseLike.child(post_key).child(getUiD()).setValue(values);
    }

    public static void onPoemViewed(String post_key) {
        mDatabasePoems.child(post_key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Poem poem = mutableData.getValue(Poem.class);
                if (poem == null) {
                    return Transaction.success(mutableData);
                }
                poem.setNumViews(poem.getNumViews() + 1);
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

    public static void onDevotionViewed(String post_key) {
        mDatabaseDevotions.child(post_key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Devotion devotion = mutableData.getValue(Devotion.class);
                if (devotion == null) {
                    return Transaction.success(mutableData);
                }
                devotion.setNumViews(devotion.getNumViews() + 1);
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

    public static void onStoryViewed(String post_key) {
        mDatabaseStory.child(post_key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Story story = mutableData.getValue(Story.class);
                if (story == null) {
                    return Transaction.success(mutableData);
                }
                story.setNumViews(story.getNumViews() + 1);
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

    public static void addStoryView(Story model, String post_key) {
        Map<String, Object> values = new HashMap<>();
        values.put(Constants.AUTHOR_URL, getUiD());
        values.put(Constants.USER, getAuthor());
        values.put(Constants.POST_TITLE, model.getTitle());
        values.put(Constants.POEM_KEY, post_key);
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        mDatabaseViews.child(post_key).child(getUiD()).setValue(values);
    }

    public static void addDevotionView(Devotion model, String post_key) {
        Map<String, Object> values = new HashMap<>();
        values.put(Constants.AUTHOR_URL, getUiD());
        values.put(Constants.USER, getAuthor());
        values.put(Constants.POST_TITLE, model.getTitle());
        values.put(Constants.POEM_KEY, post_key);
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        mDatabaseViews.child(post_key).child(getUiD()).setValue(values);
    }

    public static void addPoemView(Poem model, String post_key) {
        Map<String, Object> values = new HashMap<>();
        values.put(Constants.AUTHOR_URL, getUiD());
        values.put(Constants.USER, getAuthor());
        values.put(Constants.POST_TITLE, model.getTitle());
        values.put(Constants.POEM_KEY, post_key);
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        mDatabaseViews.child(post_key).child(getUiD()).setValue(values);
    }

    public static void deleteStory(final String post_key) {
        mDatabaseStory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(post_key)) {
                    mDatabaseStory.child(post_key).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void deleteChapter(final String post_key) {
        mDatabaseChapters.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(post_key)) {
                    mDatabaseChapters.child(post_key).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void deleteDevotion(final String post_key) {
        mDatabaseDevotions.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(post_key)) {
                    mDatabaseDevotions.child(post_key).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void deletePoem(final String post_key) {
        mDatabasePoems.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(post_key)) {
                    mDatabasePoems.child(post_key).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void deleteComment(final String post_key) {
        mDatabaseComment.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(post_key).hasChild(Constants.COMMENT_TEXT)) {
                    mDatabaseComment.child(post_key).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void deleteLike(final String post_key) {
        mDatabaseLike.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(post_key)) {
                    mDatabaseLike.child(post_key).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void deleteFromLib(final String post_key) {
        mDatabaseLibrary.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FireBaseUtils.mDatabaseLibrary.child(getUiD()).child(post_key).removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
