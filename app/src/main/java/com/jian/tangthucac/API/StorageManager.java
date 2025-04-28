
package com.jian.tangthucac.API;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.jian.tangthucac.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

public class StorageManager {
    private static final String TAG = "StorageManager";
    private FirebaseStorage storage;

    public StorageManager() {
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Upload image file lên Firebase Storage
     * @param imageUri URI của file hình ảnh
     * @param callback callback để nhận kết quả
     */
    public void uploadImage(Uri imageUri, final FirebaseManager.FirebaseCallback<String> callback) {
        if (imageUri == null) {
            callback.onError(new IllegalArgumentException("imageUri không được null"));
            return;
        }

        // Tạo đường dẫn lưu trữ file
        String imageName = "image-" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child("images/" + imageName);

        // Upload file
        UploadTask uploadTask = ref.putFile(imageUri);

        // Lấy URL download sau khi upload hoàn tất
        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                callback.onSuccess(downloadUri.toString());
            } else {
                callback.onError(task.getException());
            }
        });
    }

    /**
     * Lấy URL download của file từ Storage
     * @param path đường dẫn đến file trên Storage
     * @param callback callback để nhận kết quả
     */
    public void getDownloadUrl(String path, final FirebaseManager.FirebaseCallback<String> callback) {
        StorageReference ref = storage.getReference(path);
        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            callback.onSuccess(uri.toString());
        }).addOnFailureListener(e -> {
            callback.onError(e);
        });
    }

    /**
     * Xóa file trên Storage
     * @param path đường dẫn đến file
     * @param callback callback để nhận kết quả
     */
    public void deleteFile(String path, final FirebaseManager.FirebaseCallback<Boolean> callback) {
        StorageReference ref = storage.getReference(path);
        ref.delete().addOnSuccessListener(aVoid -> {
            callback.onSuccess(true);
        }).addOnFailureListener(e -> {
            callback.onError(e);
        });
    }

    /**
     * Tải hình ảnh vào ImageView sử dụng Glide
     * @param context Context
     * @param url URL của hình ảnh
     * @param imageView ImageView để hiển thị
     */
    public void loadImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
             .load(url)
             .placeholder(R.drawable.loading_placeholder)
             .error(R.drawable.error_image)
             .into(imageView);
    }
}
