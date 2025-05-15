package com.jian.tangthucac.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jian.tangthucac.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Helper để quản lý ảnh cục bộ từ assets hoặc resources
 */
public class LocalImageHelper {
    private static final String TAG = "LocalImageHelper";
    private static final String ASSETS_SAMPLE_IMAGES_DIR = "sample_images";
    private static final String[] DEFAULT_DRAWABLE_NAMES = {"default_cover", "placeholder_book", "error_image"};

    // Cache để lưu danh sách tệp ảnh trong assets
    private static String[] cachedAssetImageFiles = null;
    // Cache để lưu map tên truyện/id truyện -> tên tệp ảnh
    private static Map<String, String> storyIdToImageMap = new HashMap<>();

    /**
     * Lấy danh sách tệp ảnh trong thư mục assets/sample_images
     */
    private static String[] getAssetImageFiles(Context context) {
        if (cachedAssetImageFiles != null) {
            return cachedAssetImageFiles;
        }

        AssetManager assetManager = context.getAssets();
        try {
            cachedAssetImageFiles = assetManager.list(ASSETS_SAMPLE_IMAGES_DIR);
            return cachedAssetImageFiles;
        } catch (IOException e) {
            Log.e(TAG, "Error listing assets directory: " + e.getMessage());
            return new String[0];
        }
    }

    /**
     * Lấy đường dẫn ảnh trong assets cho một truyện
     */
    public static String getAssetImagePath(Context context, String storyId, String storyTitle) {
        String key = (storyId != null) ? storyId : storyTitle;

        // Kiểm tra trong cache
        if (storyIdToImageMap.containsKey(key)) {
            return ASSETS_SAMPLE_IMAGES_DIR + "/" + storyIdToImageMap.get(key);
        }

        // Lấy danh sách ảnh trong assets
        String[] imageFiles = getAssetImageFiles(context);
        if (imageFiles.length == 0) {
            return null;
        }

        // Chọn ảnh ngẫu nhiên nhưng cố định cho mỗi truyện
        int index = Math.abs(key.hashCode()) % imageFiles.length;
        String imageName = imageFiles[index];

        // Lưu vào cache
        storyIdToImageMap.put(key, imageName);

        return ASSETS_SAMPLE_IMAGES_DIR + "/" + imageName;
    }

    /**
     * Lấy resource drawable cho một truyện
     */
    public static int getDrawableResourceForStory(String storyId) {
        if (storyId == null || storyId.isEmpty()) {
            return R.drawable.default_cover;
        }

        int index = Math.abs(storyId.hashCode()) % DEFAULT_DRAWABLE_NAMES.length;
        String resourceName = DEFAULT_DRAWABLE_NAMES[index];

        switch (resourceName) {
            case "default_cover":
                return R.drawable.default_cover;
            case "placeholder_book":
                return R.drawable.placeholder_book;
            case "error_image":
                return R.drawable.error_image;
            default:
                return R.drawable.default_cover;
        }
    }

    /**
     * Tải ảnh từ assets vào ImageView
     */
    public static void loadImageFromAssets(Context context, String assetPath, ImageView imageView) {
        try {
            // Thử tải từ assets
            InputStream is = context.getAssets().open(assetPath);
            Drawable drawable = Drawable.createFromStream(is, null);
            imageView.setImageDrawable(drawable);
        } catch (IOException e) {
            Log.e(TAG, "Error loading asset image: " + e.getMessage());
            // Nếu lỗi, hiển thị ảnh mặc định từ resources
            imageView.setImageResource(R.drawable.error_image);
        }
    }

    /**
     * Tải ảnh cho story, ưu tiên theo thứ tự:
     * 1. URL web (nếu có)
     * 2. Ảnh từ assets
     * 3. Ảnh mặc định từ resources
     */
    public static void loadStoryImage(Context context, String imageUrl, String storyId, String storyTitle, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty() &&
            (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            // Nếu có URL hợp lệ, sử dụng Glide để tải
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.loading_placeholder)
                .error(R.drawable.error_image)
                .into(imageView);
            return;
        }

        // Thử tải từ assets
        String assetPath = getAssetImagePath(context, storyId, storyTitle);
        if (assetPath != null) {
            loadImageFromAssets(context, assetPath, imageView);
            return;
        }

        // Cuối cùng, sử dụng drawable mặc định
        imageView.setImageResource(getDrawableResourceForStory(storyId));
    }
}
