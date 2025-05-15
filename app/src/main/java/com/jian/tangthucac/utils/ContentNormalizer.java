package com.jian.tangthucac.utils;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    // Biên dịch sẵn các regex pattern để tăng tốc
    private static final Pattern[] COMPILED_AD_PATTERNS;

    // Bộ nhớ đệm cho văn bản đã chuẩn hóa
    private static final LruCache<String, String> NORMALIZED_CONTENT_CACHE;
    private static final LruCache<String, String> NORMALIZED_TITLE_CACHE;
    private static final LruCache<String, Double> CONTENT_QUALITY_CACHE;

    // Kích thước bộ nhớ đệm
    private static final int CONTENT_CACHE_SIZE = 100; // 100 chương
    private static final int TITLE_CACHE_SIZE = 300;   // 300 tiêu đề
    private static final int QUALITY_CACHE_SIZE = 100; // 100 đánh giá chất lượng

    // Tập hợp các tag HTML được phép giữ lại
    private static final Set<String> ALLOWED_TAGS = new HashSet<>(Arrays.asList(
        "p", "br", "b", "i", "em", "strong", "h1", "h2", "h3", "h4", "h5", "h6", "div", "span"
    ));

    // Khởi tạo
    static {
        // Biên dịch trước các mẫu regex
        COMPILED_AD_PATTERNS = new Pattern[AD_PATTERNS.length];
        for (int i = 0; i < AD_PATTERNS.length; i++) {
            COMPILED_AD_PATTERNS[i] = Pattern.compile(AD_PATTERNS[i], Pattern.CASE_INSENSITIVE);
        }

        // Khởi tạo bộ nhớ đệm
        NORMALIZED_CONTENT_CACHE = new LruCache<>(CONTENT_CACHE_SIZE);
        NORMALIZED_TITLE_CACHE = new LruCache<>(TITLE_CACHE_SIZE);
        CONTENT_QUALITY_CACHE = new LruCache<>(QUALITY_CACHE_SIZE);
    }

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

        // Kiểm tra trong bộ nhớ đệm
        String hash = String.valueOf(content.hashCode());
        String cachedContent = NORMALIZED_CONTENT_CACHE.get(hash);
        if (cachedContent != null) {
            return cachedContent;
        }

        try {
            // Loại bỏ quảng cáo dựa trên các mẫu
            content = removeAds(content);

            // Loại bỏ các thẻ HTML không cần thiết
            content = sanitizeHtml(content);

            // Chuẩn hóa khoảng cách và xuống dòng
            content = normalizeSpaces(content);

            // Lưu vào bộ nhớ đệm
            NORMALIZED_CONTENT_CACHE.put(hash, content);

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

        // Kiểm tra trong bộ nhớ đệm
        String hash = String.valueOf(title.hashCode());
        String cachedTitle = NORMALIZED_TITLE_CACHE.get(hash);
        if (cachedTitle != null) {
            return cachedTitle;
        }

        try {
            // Loại bỏ các ký tự đặc biệt và số thứ tự không cần thiết
            title = title.replaceAll("\\s*[-_:|]\\s*", " ").trim();

            // Loại bỏ các thẻ HTML nếu có
            title = Html.fromHtml(title).toString();

            // Lưu vào bộ nhớ đệm
            NORMALIZED_TITLE_CACHE.put(hash, title);

            return title;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuẩn hóa tiêu đề: " + e.getMessage());
            return title; // Trả về tiêu đề gốc nếu có lỗi
        }
    }

    /**
     * Loại bỏ quảng cáo dựa trên mẫu - tối ưu hóa hiệu suất
     */
    private static String removeAds(String content) {
        // Sử dụng các pattern đã biên dịch trước
        for (int i = 0; i < COMPILED_AD_PATTERNS.length; i++) {
            Matcher matcher = COMPILED_AD_PATTERNS[i].matcher(content);
            if (matcher.find()) {
                String adPattern = AD_PATTERNS[i];
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
     * Loại bỏ các thẻ HTML không cần thiết - tối ưu hóa hiệu suất
     */
    private static String sanitizeHtml(String content) {
        // Loại bỏ các thẻ script và style - các mẫu regex phổ biến
        content = content.replaceAll("<script[^>]*>.*?</script>", "");
        content = content.replaceAll("<style[^>]*>.*?</style>", "");

        // Loại bỏ các thuộc tính không cần thiết
        content = content.replaceAll("(?i)\\s+(onclick|ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup|style|class)=[\"\'][^\"\']*[\"\']", "");

        // Loại bỏ các thẻ không được phép
        StringBuilder allowedTagsPattern = new StringBuilder();
        boolean first = true;
        for (String tag : ALLOWED_TAGS) {
            if (!first) allowedTagsPattern.append("|");
            allowedTagsPattern.append(tag);
            first = false;
        }

        // Tạo và sử dụng một pattern duy nhất thay vì nhiều pattern riêng lẻ
        content = content.replaceAll("<(?!/?(" + allowedTagsPattern.toString() + "))[^>]*>", "");

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

        for (Pattern pattern : COMPILED_AD_PATTERNS) {
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

        // Kiểm tra trong bộ nhớ đệm
        String hash = String.valueOf(content.hashCode());
        Double cachedQuality = CONTENT_QUALITY_CACHE.get(hash);
        if (cachedQuality != null) {
            return cachedQuality;
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
        double finalScore = Math.max(0.0, score);

        // Lưu vào bộ nhớ đệm
        CONTENT_QUALITY_CACHE.put(hash, finalScore);

        return finalScore;
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

    /**
     * Xóa cache để giải phóng bộ nhớ
     */
    public static void clearCache() {
        NORMALIZED_CONTENT_CACHE.evictAll();
        NORMALIZED_TITLE_CACHE.evictAll();
        CONTENT_QUALITY_CACHE.evictAll();
        Log.d(TAG, "Đã xóa tất cả cache của ContentNormalizer");
    }

    /**
     * Trích xuất và so khớp đoạn văn từ hai nội dung (tiếng Trung và tiếng Việt)
     * Để cải thiện hiển thị song ngữ
     *
     * @param chineseContent Nội dung tiếng Trung
     * @param vietnameseContent Nội dung tiếng Việt
     * @return Mảng các cặp đoạn văn tương ứng [chinese, vietnamese]
     */
    public static Map<String, String> matchParagraphs(String chineseContent, String vietnameseContent) {
        if (TextUtils.isEmpty(chineseContent) || TextUtils.isEmpty(vietnameseContent)) {
            return new HashMap<>();
        }

        Map<String, String> matchedPairs = new HashMap<>();

        // Tách nội dung thành các đoạn
        String[] chineseParagraphs = chineseContent.split("(?<=<br\\s*/>)|(?<=</p>)");
        String[] vietnameseParagraphs = vietnameseContent.split("(?<=<br\\s*/>)|(?<=</p>)");

        // Số đoạn cần xử lý
        int paragraphCount = Math.min(chineseParagraphs.length, vietnameseParagraphs.length);

        // Ghép cặp các đoạn văn tương ứng nhau
        for (int i = 0; i < paragraphCount; i++) {
            String chinesePara = chineseParagraphs[i].trim();
            String vietnamesePara = vietnameseParagraphs[i].trim();

            if (!TextUtils.isEmpty(chinesePara) && !TextUtils.isEmpty(vietnamesePara)) {
                matchedPairs.put(chinesePara, vietnamesePara);
            }
        }

        return matchedPairs;
    }

    /**
     * Xếp hạng chất lượng của các đoạn văn để chọn đoạn tốt nhất khi hiển thị
     * @param paragraph Đoạn văn cần đánh giá
     * @return Điểm chất lượng (0.0-1.0)
     */
    public static double rankParagraphQuality(String paragraph) {
        if (TextUtils.isEmpty(paragraph)) {
            return 0.0;
        }

        double score = 1.0;

        // Kiểm tra chiều dài (đoạn quá ngắn thường kém chất lượng)
        if (paragraph.length() < 10) {
            score -= 0.3;
        }

        // Kiểm tra nội dung trùng lặp
        if (containsRepeatedPatterns(paragraph)) {
            score -= 0.2;
        }

        // Kiểm tra quảng cáo
        if (containsAds(paragraph)) {
            score -= 0.4;
        }

        return Math.max(0.0, score);
    }

    /**
     * Kiểm tra đoạn văn có chứa mẫu lặp lại không
     */
    private static boolean containsRepeatedPatterns(String text) {
        // Mẫu lặp lại đơn giản (3+ từ giống nhau liên tiếp)
        Pattern repeatedWordsPattern = Pattern.compile("(\\b\\w+\\b)(?:\\s+\\1){2,}");
        Matcher matcher = repeatedWordsPattern.matcher(text);
        return matcher.find();
    }

    /**
     * Gom các chương thành một nội dung duy nhất để tối ưu hóa việc dịch
     * @param chapters Danh sách các chương
     * @return Nội dung đã gộp với các định danh chương
     */
    public static String mergeChaptersForTranslation(List<String> chapters) {
        if (chapters == null || chapters.isEmpty()) {
            return "";
        }

        StringBuilder mergedContent = new StringBuilder();
        int chapterIndex = 0;

        for (String chapter : chapters) {
            if (!TextUtils.isEmpty(chapter)) {
                // Thêm đánh dấu chương để tách sau này
                mergedContent.append("---CHAPTER_MARK_").append(chapterIndex++).append("---\n");
                mergedContent.append(chapter).append("\n\n");
            }
        }

        return mergedContent.toString();
    }

    /**
     * Tách nội dung đã gộp thành các chương riêng biệt
     * @param mergedContent Nội dung đã gộp với các định danh
     * @return Danh sách các chương đã tách
     */
    public static List<String> splitMergedContent(String mergedContent) {
        if (TextUtils.isEmpty(mergedContent)) {
            return new ArrayList<>();
        }

        List<String> chapters = new ArrayList<>();
        Pattern chapterPattern = Pattern.compile("---CHAPTER_MARK_(\\d+)---\\s*([\\s\\S]*?)(?=---CHAPTER_MARK_|$)");
        Matcher matcher = chapterPattern.matcher(mergedContent);

        while (matcher.find()) {
            String chapterContent = matcher.group(2).trim();
            chapters.add(chapterContent);
        }

        return chapters;
    }

    /**
     * Giám sát và quản lý bộ nhớ cache - gọi từ Application khi ứng dụng cần giải phóng bộ nhớ
     * @param urgent True nếu cần giải phóng gấp, false để giải phóng từ từ
     */
    public static void manageMemory(boolean urgent) {
        if (urgent) {
            // Xóa toàn bộ cache nếu khẩn cấp
            clearCache();
        } else {
            // Xóa một phần cache
            if (NORMALIZED_CONTENT_CACHE.size() > CONTENT_CACHE_SIZE / 2) {
                NORMALIZED_CONTENT_CACHE.trimToSize(CONTENT_CACHE_SIZE / 2);
            }
            if (NORMALIZED_TITLE_CACHE.size() > TITLE_CACHE_SIZE / 2) {
                NORMALIZED_TITLE_CACHE.trimToSize(TITLE_CACHE_SIZE / 2);
            }
        }

        // Khuyến khích GC chạy
        System.gc();
    }
}
