package com.jian.tangthucac.API;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jian.tangthucac.R;

/**
 * Lớp quản lý và cung cấp URL ảnh từ các nguồn bên ngoài thay vì Firebase Storage
 */
public class ImageProvider {
    private static final String TAG = "ImageProvider";

    // Danh sách các URL ảnh mặc định để sử dụng khi không có ảnh thực
    private static final String[] DEFAULT_IMAGE_URLS = {
        "https://i.imgur.com/bqLz1jo.jpeg", // Ảnh truyện 1
        "https://i.imgur.com/f3ZL6CZ.jpeg", // Ảnh truyện 2
        "https://i.imgur.com/tGbaZCY.jpeg", // Ảnh truyện 3
        "https://i.imgur.com/gT9rLal.jpeg", // Ảnh truyện 4
        "https://i.imgur.com/MQJG3Gi.jpeg"  // Ảnh truyện 5
    };

    /**
     * Lấy URL ảnh mặc định dựa trên ID truyện
     * @param storyId ID của truyện
     * @return URL ảnh mặc định
     */
    public static String getDefaultImageUrl(String storyId) {
        if (storyId == null || storyId.isEmpty()) {
            return DEFAULT_IMAGE_URLS[0];
        }

        // Sử dụng hashCode của storyId để lấy URL ảnh từ mảng
        int index = Math.abs(storyId.hashCode()) % DEFAULT_IMAGE_URLS.length;
        return DEFAULT_IMAGE_URLS[index];
    }

    /**
     * Kiểm tra và chuẩn hóa URL ảnh
     * @param imageUrl URL ảnh cần kiểm tra
     * @param storyId ID của truyện để cung cấp ảnh mặc định
     * @return URL ảnh đã chuẩn hóa
     */
    public static String normalizeImageUrl(String imageUrl, String storyId) {
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals("null")) {
            return getDefaultImageUrl(storyId);
        }

        // Nếu là đường dẫn Firebase Storage (gs://) thì trả về ảnh mặc định
        if (imageUrl.startsWith("gs://")) {
            return getDefaultImageUrl(storyId);
        }

        // Nếu là URL http/https, trả về URL đó
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }

        // Nếu không phải URL http/https, thêm prefix của Imgur hoặc nguồn dự phòng khác
        return "https://i.imgur.com/" + imageUrl;
    }

    /**
     * Tải hình ảnh vào ImageView sử dụng Glide
     * @param context Context
     * @param url URL của hình ảnh
     * @param imageView ImageView để hiển thị
     */
    public static void loadImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
             .load(url)
             .placeholder(R.drawable.loading_placeholder)
             .error(R.drawable.error_image)
             .into(imageView);
    }
}
