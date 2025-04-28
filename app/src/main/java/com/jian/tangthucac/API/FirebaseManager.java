
package com.jian.tangthucac.API;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.jian.tangthucac.model.Chapter;
import com.jian.tangthucac.model.Library;
import com.jian.tangthucac.model.Story;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseAuth auth;

    private FirebaseManager() {
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    /**
     * Lấy danh sách tất cả truyện
     */
    public void getAllStories(final FirebaseCallback<List<Story>> callback) {
        DatabaseReference storiesRef = database.getReference("stories");
        storiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Story> stories = new ArrayList<>();
                for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                    Story story = storySnapshot.getValue(Story.class);
                    if (story != null) {
                        story.setId(storySnapshot.getKey());
                        stories.add(story);
                    }
                }
                callback.onSuccess(stories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    /**
     * Lấy truyện theo thể loại
     */
    public void getStoriesByGenre(String genre, final FirebaseCallback<List<Story>> callback) {
        DatabaseReference storiesRef = database.getReference("stories");
        storiesRef.orderByChild("genre").equalTo(genre).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Story> stories = new ArrayList<>();
                for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                    Story story = storySnapshot.getValue(Story.class);
                    if (story != null) {
                        story.setId(storySnapshot.getKey());
                        stories.add(story);
                    }
                }
                callback.onSuccess(stories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    /**
     * Thêm truyện vào thư viện cá nhân của user
     */
    public void addToLibrary(Story story, final FirebaseCallback<Boolean> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new Exception("Người dùng chưa đăng nhập"));
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference libraryRef = database.getReference("Users")
                .child(userId)
                .child("Library")
                .child(story.getTitle());

        Map<String, Object> libraryItem = new HashMap<>();
        libraryItem.put("title", story.getTitle());
        libraryItem.put("author", story.getAuthor());
        libraryItem.put("image", story.getImage());

        libraryRef.setValue(libraryItem)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e));
    }

    /**
     * Xóa truyện khỏi thư viện cá nhân
     */
    public void removeFromLibrary(String storyTitle, final FirebaseCallback<Boolean> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new Exception("Người dùng chưa đăng nhập"));
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference libraryRef = database.getReference("Users")
                .child(userId)
                .child("Library");

        libraryRef.orderByChild("title").equalTo(storyTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                itemSnapshot.getRef().removeValue()
                                        .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                                        .addOnFailureListener(e -> callback.onError(e));
                                return;
                            }
                        } else {
                            callback.onError(new Exception("Không tìm thấy truyện trong thư viện"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.toException());
                    }
                });
    }

    /**
     * Interface callback cho các phương thức Firebase
     */
    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
