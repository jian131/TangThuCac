package com.jian.tangthucac.API;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lớp tiện ích để chuẩn hóa nội dung chương từ các nguồn khác nhau
 * Loại bỏ quảng cáo, định dạng lại để hiển thị tốt hơn
 */
public class ContentNormalizer {
    private static final String TAG = "ContentNormalizer";

    // Các mẫu quảng cáo phổ biến cần loại bỏ
    private static final String[] AD_PATTERNS = {
        "đọc truyện tại.*",
        "truyện convert.*",
        "nguồn.*wikidict.*",
        "đăng ký thành viên.*",
        "truyện được lấy tại.*",
        "epub đọc.*",
        "convert.*",
        "please visit.*",
        "visit.*to.*read.*latest.*chapters.*",
        "sponsored content.*",
        "join.*telegram.*group.*",
        "follow us.*for latest.*"
    };

    // Tập hợp các tag HTML được phép giữ lại
    private static final Set<String> ALLOWED_TAGS = new HashSet<>(Arrays.asList(
        "p", "br", "b", "i", "em", "strong", "h1", "h2", "h3", "h4", "h5", "h6", "div", "span"
    ));

    /**
     * Chuẩn hóa nội dung chương
     *
     * @param content Nội dung HTML gốc
     * @return Nội dung đã được chuẩn hóa
     */
    public static String normalizeChapterContent(String content) {
        if (TextUtils.isEmpty(content)) {
            return "";
        }

        try {
            // Loại bỏ quảng cáo dựa trên các mẫu
            content = removeAds(content);

            // Loại bỏ các thẻ HTML không cần thiết
            content = sanitizeHtml(content);

            // Chuẩn hóa khoảng cách và xuống dòng
            content = normalizeSpaces(content);

            return content;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuẩn hóa nội dung: " + e.getMessage());
            return content; // Trả về nội dung gốc nếu có lỗi
        }
    }

    /**
     * Chuẩn hóa tiêu đề chương
     *
     * @param title Tiêu đề gốc
     * @return Tiêu đề đã được chuẩn hóa
     */
    public static String normalizeChapterTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            return "";
        }

        try {
            // Loại bỏ các ký tự đặc biệt và số thứ tự không cần thiết
            title = title.replaceAll("\\s*[-_:|]\\s*", " ").trim();

            // Loại bỏ các thẻ HTML nếu có
            title = Html.fromHtml(title).toString();

            return title;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuẩn hóa tiêu đề: " + e.getMessage());
            return title; // Trả về tiêu đề gốc nếu có lỗi
        }
    }

    /**
     * Loại bỏ quảng cáo dựa trên mẫu
     */
    private static String removeAds(String content) {
        // Compile các regex pattern
        for (String adPattern : AD_PATTERNS) {
            Pattern pattern = Pattern.compile(adPattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                // Loại bỏ toàn bộ đoạn văn bản chứa quảng cáo
                content = content.replaceAll("<p[^>]*>.*?" + adPattern + ".*?</p>", "");
                // Loại bỏ các span chứa quảng cáo
                content = content.replaceAll("<span[^>]*>.*?" + adPattern + ".*?</span>", "");
                // Loại bỏ văn bản quảng cáo nếu không nằm trong thẻ
                content = content.replaceAll(adPattern, "");
            }
        }
        return content;
    }

    /**
     * Loại bỏ các thẻ HTML không cần thiết
     */
    private static String sanitizeHtml(String content) {
        // Loại bỏ các thẻ script và style
        content = content.replaceAll("<script[^>]*>.*?</script>", "");
        content = content.replaceAll("<style[^>]*>.*?</style>", "");

        // Loại bỏ các thuộc tính không cần thiết
        content = content.replaceAll("(?i)\\s+(onclick|ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup|style|class)=[\"\'][^\"\']*[\"\']", "");

        // Loại bỏ các thẻ không được phép
        for (String tag : ALLOWED_TAGS) {
            // Giữ lại các thẻ được phép
            content = content.replaceAll("<(?!/?" + tag + ")[^>]*>", "");
        }

        return content;
    }

    /**
     * Chuẩn hóa khoảng cách và xuống dòng
     */
    private static String normalizeSpaces(String content) {
        // Thay thế nhiều dòng trống bằng một dòng trống
        content = content.replaceAll("(<br\\s*/?>\\s*){3,}", "<br/><br/>");

        // Thay thế nhiều khoảng trắng bằng một khoảng trắng
        content = content.replaceAll("\\s{2,}", " ");

        // Đảm bảo mỗi đoạn văn bản được tách biệt
        content = content.replaceAll("</p>\\s*<p>", "</p><br/><p>");

        return content;
    }

    /**
     * Kiểm tra nội dung có chứa quảng cáo hay không
     */
    public static boolean containsAds(String content) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }

        for (String adPattern : AD_PATTERNS) {
            Pattern pattern = Pattern.compile(adPattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Đánh giá chất lượng nội dung
     *
     * @param content Nội dung cần đánh giá
     * @return Điểm chất lượng từ 0.0 đến 1.0
     */
    public static double evaluateContentQuality(String content) {
        if (TextUtils.isEmpty(content)) {
            return 0.0;
        }

        double score = 1.0;

        // Kiểm tra chiều dài nội dung
        if (content.length() < 500) {
            score -= 0.3; // Nội dung quá ngắn
        }

        // Kiểm tra quảng cáo
        if (containsAds(content)) {
            score -= 0.2;
        }

        // Kiểm tra các ký tự lạ hoặc mã hóa không đúng
        int strangeChars = countStrangeCharacters(content);
        if (strangeChars > content.length() * 0.05) {
            score -= 0.2; // Có quá nhiều ký tự lạ
        }

        // Đảm bảo điểm tối thiểu là 0.0
        return Math.max(0.0, score);
    }

    /**
     * Đếm số lượng ký tự lạ trong nội dung
     */
    private static int countStrangeCharacters(String content) {
        int count = 0;
        Pattern pattern = Pattern.compile("[^\\p{L}\\p{N}\\p{P}\\s]");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            count++;
        }

        return count;
    }
}
