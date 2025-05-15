package com.jian.tangthucac.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jian.tangthucac.model.SearchKeywordMap;
import com.jian.tangthucac.model.TranslatedChapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Dịch vụ kết nối với các API dịch thuật như Claude AI và DeepL
 */
public class TranslationService {
    private static final String TAG = "TranslationService";

    // API endpoints
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String DEEPL_API_URL = "https://api-free.deepl.com/v2/translate";

    // Tài khoản API (sẽ được cấu hình từ tài khoản người dùng)
    private String claudeApiKey;
    private String deeplApiKey;

    // Các mã ngôn ngữ hỗ trợ
    public static final String LANGUAGE_ZH = "zh"; // Tiếng Trung
    public static final String LANGUAGE_VI = "vi"; // Tiếng Việt
    public static final String LANGUAGE_EN = "en"; // Tiếng Anh

    // Các công cụ dịch hỗ trợ
    public static final String ENGINE_CLAUDE = "claude";
    public static final String ENGINE_DEEPL = "deepl";

    // Singleton instance
    private static TranslationService instance;

    // RequestQueue cho HTTP requests
    private RequestQueue requestQueue;

    // Executor cho các tác vụ nền
    private final Executor executor;

    // Media type
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Maximum text length for a single translation request
    private static final int MAX_TEXT_LENGTH = 5000;

    // Bộ đệm dịch thuật: lưu trữ văn bản đã dịch để tránh dịch lại
    private final Map<String, String> translationCache;
    private static final int MAX_CACHE_SIZE = 1000;

    // Cache constants
    private static final String PREF_NAME = "translation_cache";
    private static final String TRANSLATION_CACHE_KEY = "translation_cache_data";

    // SharedPreferences cho bộ đệm dịch thuật
    private SharedPreferences translationPreferences;
    private final Gson gson;

    /**
     * Callback để nhận kết quả dịch
     */
    public interface OnTranslationListener {
        void onTranslationCompleted(String translatedText);
        void onError(Exception e);
    }

    /**
     * Callback để nhận kết quả dịch từ khóa
     */
    public interface OnKeywordTranslationListener {
        void onKeywordTranslated(SearchKeywordMap keywordMap);
        void onError(Exception e);
    }

    /**
     * Callback để nhận kết quả dịch bài viết
     */
    public interface OnContentTranslationListener {
        void onContentTranslated(TranslatedChapter chapter);
        void onError(Exception e);
    }

    private TranslationService() {
        executor = Executors.newCachedThreadPool();
        translationCache = new ConcurrentHashMap<>();
        gson = new Gson();
    }

    public static synchronized TranslationService getInstance() {
        if (instance == null) {
            instance = new TranslationService();
        }
        return instance;
    }

    /**
     * Khởi tạo service với API keys
     */
    public void initialize(Context context, String claudeApiKey, String deeplApiKey) {
        this.claudeApiKey = claudeApiKey;
        this.deeplApiKey = deeplApiKey;
        this.requestQueue = Volley.newRequestQueue(context);
        this.translationPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadTranslationCache();
    }

    /**
     * Tải cache dịch thuật từ SharedPreferences
     */
    private void loadTranslationCache() {
        executor.execute(() -> {
            try {
                String cacheJson = translationPreferences.getString(TRANSLATION_CACHE_KEY, null);
                if (cacheJson != null) {
                    Type cacheType = new TypeToken<Map<String, String>>(){}.getType();
                    Map<String, String> loadedCache = gson.fromJson(cacheJson, cacheType);
                    if (loadedCache != null) {
                        translationCache.putAll(loadedCache);
                        Log.d(TAG, "Đã tải " + translationCache.size() + " mục dịch từ cache");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tải cache dịch thuật: " + e.getMessage());
            }
        });
    }

    /**
     * Lưu cache dịch thuật vào SharedPreferences
     */
    private void saveTranslationCache() {
        if (translationPreferences == null) return;

        executor.execute(() -> {
            try {
                // Giới hạn kích thước cache để tránh quá lớn
                Map<String, String> cacheToSave = new HashMap<>();
                int count = 0;
                for (Map.Entry<String, String> entry : translationCache.entrySet()) {
                    if (count++ >= MAX_CACHE_SIZE) break;
                    cacheToSave.put(entry.getKey(), entry.getValue());
                }

                String cacheJson = gson.toJson(cacheToSave);
                translationPreferences.edit()
                    .putString(TRANSLATION_CACHE_KEY, cacheJson)
                    .apply();

                Log.d(TAG, "Đã lưu " + cacheToSave.size() + " mục dịch vào cache");
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lưu cache dịch thuật: " + e.getMessage());
            }
        });
    }

    /**
     * Tạo khóa cache từ văn bản nguồn, ngôn ngữ nguồn và đích
     */
    private String generateCacheKey(String text, String sourceLanguage, String targetLanguage) {
        // Sử dụng hash của văn bản kết hợp với ngôn ngữ để tạo khóa ngắn hơn
        return sourceLanguage + "_" + targetLanguage + "_" + Math.abs(text.hashCode());
    }

    /**
     * Dịch văn bản sử dụng Claude
     */
    public void translateWithClaude(String text, String sourceLanguage, String targetLanguage, OnTranslationListener listener) {
        if (claudeApiKey == null || claudeApiKey.isEmpty()) {
            listener.onError(new Exception("Claude API key chưa được cấu hình"));
            return;
        }

        // Kiểm tra cache trước
        String cacheKey = generateCacheKey(text, sourceLanguage, targetLanguage);
        if (translationCache.containsKey(cacheKey)) {
            String cachedTranslation = translationCache.get(cacheKey);
            Log.d(TAG, "Sử dụng bản dịch từ cache");
            listener.onTranslationCompleted(cachedTranslation);
            return;
        }

        executor.execute(() -> {
            try {
                // Tạo prompt cho Claude với ngữ cảnh dịch thuật
                String prompt = buildClaudeTranslationPrompt(text, sourceLanguage, targetLanguage);

                // Tạo JSON request body
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "claude-3-opus-20240229");
                requestBody.put("max_tokens", 4000);
                requestBody.put("temperature", 0.3);

                // Thêm thông tin tin nhắn
                JSONArray messages = new JSONArray();
                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", prompt);
                messages.put(message);

                requestBody.put("messages", messages);

                // Tạo HTTP request
                JsonObjectRequest request = new JsonObjectRequest(
                    CLAUDE_API_URL,
                    requestBody,
                    response -> {
                        try {
                            // Parse kết quả
                            JSONArray content = response.getJSONObject("content").getJSONArray("parts");
                            String translatedText = content.getString(0);
                            translatedText = cleanTranslatedText(translatedText);

                            // Lưu vào cache
                            translationCache.put(cacheKey, translatedText);
                            saveTranslationCache();

                            // Xử lý kết quả và gọi callback
                            listener.onTranslationCompleted(translatedText);
                        } catch (JSONException e) {
                            listener.onError(new Exception("Lỗi khi parse kết quả Claude: " + e.getMessage()));
                        }
                    },
                    error -> listener.onError(new Exception("Lỗi khi gọi Claude API: " + error.getMessage()))
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("X-Api-Key", claudeApiKey);
                        headers.put("anthropic-version", "2023-06-01");
                        return headers;
                    }
                };

                // Thêm request vào hàng đợi
                requestQueue.add(request);
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    /**
     * Dịch văn bản sử dụng DeepL
     */
    public void translateWithDeepL(String text, String sourceLanguage, String targetLanguage, OnTranslationListener listener) {
        if (deeplApiKey == null || deeplApiKey.isEmpty()) {
            listener.onError(new Exception("DeepL API key chưa được cấu hình"));
            return;
        }

        // Kiểm tra cache trước
        String cacheKey = generateCacheKey(text, sourceLanguage, targetLanguage);
        if (translationCache.containsKey(cacheKey)) {
            String cachedTranslation = translationCache.get(cacheKey);
            Log.d(TAG, "Sử dụng bản dịch DeepL từ cache");
            listener.onTranslationCompleted(cachedTranslation);
            return;
        }

        executor.execute(() -> {
            try {
                // Map mã ngôn ngữ sang định dạng của DeepL
                String source = mapLanguageToDeepL(sourceLanguage);
                String target = mapLanguageToDeepL(targetLanguage);

                // Tạo JSON request body
                JSONObject requestBody = new JSONObject();
                requestBody.put("text", text);
                requestBody.put("source_lang", source);
                requestBody.put("target_lang", target);
                requestBody.put("preserve_formatting", true);

                // Tạo HTTP request
                JsonObjectRequest request = new JsonObjectRequest(
                    DEEPL_API_URL,
                    requestBody,
                    response -> {
                        try {
                            // Parse kết quả
                            JSONArray translations = response.getJSONArray("translations");
                            String translatedText = translations.getJSONObject(0).getString("text");
                            translatedText = cleanTranslatedText(translatedText);

                            // Lưu vào cache
                            translationCache.put(cacheKey, translatedText);
                            saveTranslationCache();

                            // Xử lý kết quả và gọi callback
                            listener.onTranslationCompleted(translatedText);
                        } catch (JSONException e) {
                            listener.onError(new Exception("Lỗi khi parse kết quả DeepL: " + e.getMessage()));
                        }
                    },
                    error -> listener.onError(new Exception("Lỗi khi gọi DeepL API: " + error.getMessage()))
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", "DeepL-Auth-Key " + deeplApiKey);
                        return headers;
                    }
                };

                // Thêm request vào hàng đợi
                requestQueue.add(request);
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }

    /**
     * Dịch từ khóa tìm kiếm
     */
    public void translateKeyword(String keyword, String sourceLanguage, String targetLanguage, OnKeywordTranslationListener listener) {
        // Sử dụng Claude cho độ chính xác cao hơn
        translateWithClaude(keyword, sourceLanguage, targetLanguage, new OnTranslationListener() {
            @Override
            public void onTranslationCompleted(String translatedText) {
                SearchKeywordMap keywordMap = new SearchKeywordMap();

                if (sourceLanguage.equals(LANGUAGE_VI) && targetLanguage.equals(LANGUAGE_ZH)) {
                    keywordMap.setVietnameseKeyword(keyword);
                    keywordMap.setChineseKeyword(translatedText);
                } else if (sourceLanguage.equals(LANGUAGE_ZH) && targetLanguage.equals(LANGUAGE_VI)) {
                    keywordMap.setChineseKeyword(keyword);
                    keywordMap.setVietnameseKeyword(translatedText);
                } else if (targetLanguage.equals(LANGUAGE_EN)) {
                    keywordMap.setEnglishKeyword(translatedText);
                    if (sourceLanguage.equals(LANGUAGE_VI)) {
                        keywordMap.setVietnameseKeyword(keyword);
                    } else if (sourceLanguage.equals(LANGUAGE_ZH)) {
                        keywordMap.setChineseKeyword(keyword);
                    }
                }

                keywordMap.setTranslationEngine(ENGINE_CLAUDE);
                keywordMap.setUseCount(1);
                keywordMap.setLastUsedTime(System.currentTimeMillis());

                listener.onKeywordTranslated(keywordMap);
            }

            @Override
            public void onError(Exception e) {
                // Fallback to DeepL if Claude fails
                translateWithDeepL(keyword, sourceLanguage, targetLanguage, new OnTranslationListener() {
                    @Override
                    public void onTranslationCompleted(String translatedText) {
                        SearchKeywordMap keywordMap = new SearchKeywordMap();

                        if (sourceLanguage.equals(LANGUAGE_VI) && targetLanguage.equals(LANGUAGE_ZH)) {
                            keywordMap.setVietnameseKeyword(keyword);
                            keywordMap.setChineseKeyword(translatedText);
                        } else if (sourceLanguage.equals(LANGUAGE_ZH) && targetLanguage.equals(LANGUAGE_VI)) {
                            keywordMap.setChineseKeyword(keyword);
                            keywordMap.setVietnameseKeyword(translatedText);
                        } else if (targetLanguage.equals(LANGUAGE_EN)) {
                            keywordMap.setEnglishKeyword(translatedText);
                            if (sourceLanguage.equals(LANGUAGE_VI)) {
                                keywordMap.setVietnameseKeyword(keyword);
                            } else if (sourceLanguage.equals(LANGUAGE_ZH)) {
                                keywordMap.setChineseKeyword(keyword);
                            }
                        }

                        keywordMap.setTranslationEngine(ENGINE_DEEPL);
                        keywordMap.setUseCount(1);
                        keywordMap.setLastUsedTime(System.currentTimeMillis());

                        listener.onKeywordTranslated(keywordMap);
                    }

                    @Override
                    public void onError(Exception e) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    /**
     * Dịch nội dung chương truyện
     */
    public void translateChapter(TranslatedChapter chapter, String sourceLanguage, String targetLanguage, OnContentTranslationListener listener) {
        if (chapter == null || chapter.getContent() == null || chapter.getContent().isEmpty()) {
            listener.onError(new Exception("Nội dung chương trống"));
            return;
        }

        // Dịch tiêu đề trước
        translateWithClaude(chapter.getTitle(), sourceLanguage, targetLanguage, new OnTranslationListener() {
            @Override
            public void onTranslationCompleted(String translatedTitle) {
                // Lưu tiêu đề đã dịch
                chapter.setTitleVi(translatedTitle);

                // Tiếp tục dịch nội dung
                translateWithClaude(chapter.getContent(), sourceLanguage, targetLanguage, new OnTranslationListener() {
                    @Override
                    public void onTranslationCompleted(String translatedContent) {
                        // Lưu nội dung đã dịch
                        chapter.setContentVi(translatedContent);
                        chapter.setTranslationEngine(ENGINE_CLAUDE);
                        chapter.setTranslationQuality(0.9); // Chất lượng cao từ Claude
                        chapter.setTranslatedTime(System.currentTimeMillis());

                        // Gọi callback
                        listener.onContentTranslated(chapter);
                    }

                    @Override
                    public void onError(Exception e) {
                        // Fallback to DeepL for content
                        translateWithDeepL(chapter.getContent(), sourceLanguage, targetLanguage, new OnTranslationListener() {
                            @Override
                            public void onTranslationCompleted(String translatedContent) {
                                // Lưu nội dung đã dịch
                                chapter.setContentVi(translatedContent);
                                chapter.setTranslationEngine(ENGINE_DEEPL);
                                chapter.setTranslationQuality(0.7); // Chất lượng từ DeepL
                                chapter.setTranslatedTime(System.currentTimeMillis());

                                // Gọi callback
                                listener.onContentTranslated(chapter);
                            }

                            @Override
                            public void onError(Exception e) {
                                listener.onError(e);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // Fallback to DeepL for title
                translateWithDeepL(chapter.getTitle(), sourceLanguage, targetLanguage, new OnTranslationListener() {
                    @Override
                    public void onTranslationCompleted(String translatedTitle) {
                        // Lưu tiêu đề đã dịch và tiếp tục với nội dung
                        chapter.setTitleVi(translatedTitle);

                        // Tiếp tục dịch nội dung với DeepL
                        translateWithDeepL(chapter.getContent(), sourceLanguage, targetLanguage, new OnTranslationListener() {
                            @Override
                            public void onTranslationCompleted(String translatedContent) {
                                // Lưu nội dung đã dịch
                                chapter.setContentVi(translatedContent);
                                chapter.setTranslationEngine(ENGINE_DEEPL);
                                chapter.setTranslationQuality(0.7); // Chất lượng từ DeepL
                                chapter.setTranslatedTime(System.currentTimeMillis());

                                // Gọi callback
                                listener.onContentTranslated(chapter);
                            }

                            @Override
                            public void onError(Exception e) {
                                listener.onError(e);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    /**
     * Dịch một danh sách các chương với chiến lược tiết kiệm băng thông
     * Sử dụng kết hợp cache + batch translation khi có thể
     * @param chapters Danh sách các chương cần dịch
     * @param sourceLanguage Ngôn ngữ nguồn
     * @param targetLanguage Ngôn ngữ đích
     * @param listener Callback khi hoàn thành dịch từng chương
     */
    public void batchTranslateChapters(List<TranslatedChapter> chapters, String sourceLanguage,
                                      String targetLanguage, OnContentTranslationListener listener) {
        if (chapters == null || chapters.isEmpty()) {
            return;
        }

        // Thực hiện dịch lần lượt từng chương, sử dụng executor để tránh nghẽn
        final int[] processed = {0};
        final int total = chapters.size();

        for (TranslatedChapter chapter : chapters) {
            // Kiểm tra cache trước
            String titleCacheKey = generateCacheKey(chapter.getTitle(), sourceLanguage, targetLanguage);
            String contentCacheKey = generateCacheKey(chapter.getContent(), sourceLanguage, targetLanguage);

            boolean needsTranslation = !translationCache.containsKey(titleCacheKey) ||
                                      !translationCache.containsKey(contentCacheKey);

            if (needsTranslation) {
                // Phân bổ thời gian giữa các request để tránh quá tải API
                long delay = processed[0] * 500; // 500ms giữa các yêu cầu

                executor.execute(() -> {
                    try {
                        Thread.sleep(delay);
                        translateChapter(chapter, sourceLanguage, targetLanguage, new OnContentTranslationListener() {
                            @Override
                            public void onContentTranslated(TranslatedChapter translatedChapter) {
                                processed[0]++;
                                listener.onContentTranslated(translatedChapter);
                            }

                            @Override
                            public void onError(Exception e) {
                                processed[0]++;
                                listener.onError(e);
                            }
                        });
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Batch translation interrupted", e);
                    }
                });
            } else {
                // Trích xuất từ cache
                executor.execute(() -> {
                    try {
                        String cachedTitle = translationCache.get(titleCacheKey);
                        String cachedContent = translationCache.get(contentCacheKey);

                        chapter.setTitleVi(cachedTitle);
                        chapter.setContentVi(cachedContent);
                        chapter.setTranslationEngine(ENGINE_CLAUDE); // Giả định engine cache
                        chapter.setTranslationQuality(0.9); // Giả định chất lượng cao từ cache
                        chapter.setTranslatedTime(System.currentTimeMillis());

                        processed[0]++;
                        listener.onContentTranslated(chapter);
                    } catch (Exception e) {
                        processed[0]++;
                        listener.onError(e);
                    }
                });
            }
        }
    }

    /**
     * Khả năng kiểm soát băng thông - giới hạn số lượng requests/phút
     */
    private final int MAX_REQUESTS_PER_MINUTE = 30;
    private final Queue<Long> requestTimestamps = new ConcurrentLinkedQueue<>();

    /**
     * Kiểm tra và giới hạn tốc độ request
     * @return true nếu có thể gửi request ngay, false nếu cần đợi
     */
    private boolean canSendRequest() {
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60 * 1000;

        // Xóa các timestamps cũ hơn 1 phút
        while (!requestTimestamps.isEmpty() && requestTimestamps.peek() < oneMinuteAgo) {
            requestTimestamps.poll();
        }

        // Kiểm tra nếu đã đạt giới hạn
        if (requestTimestamps.size() < MAX_REQUESTS_PER_MINUTE) {
            requestTimestamps.add(currentTime);
            return true;
        }

        return false;
    }

    /**
     * Đợi cho đến khi có thể gửi request
     */
    private void waitForRequestSlot() {
        while (!canSendRequest()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Phát hiện ngôn ngữ của văn bản dựa trên thuật toán đơn giản
     * @param text Văn bản cần phát hiện ngôn ngữ
     * @return Mã ngôn ngữ (ZH, VI, EN)
     */
    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return LANGUAGE_EN; // Mặc định là tiếng Anh nếu rỗng
        }

        // Đếm các ký tự đặc trưng
        int chineseChars = 0;
        int vietnameseChars = 0;
        int englishChars = 0;

        for (char c : text.toCharArray()) {
            // Phạm vi ký tự tiếng Trung
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
                chineseChars++;
            }
            // Các ký tự tiếng Việt đặc trưng
            else if (c == 'ă' || c == 'â' || c == 'đ' || c == 'ê' || c == 'ô' || c == 'ơ' || c == 'ư'
                    || c == 'Ă' || c == 'Â' || c == 'Đ' || c == 'Ê' || c == 'Ô' || c == 'Ơ' || c == 'Ư'
                    || c == 'á' || c == 'à' || c == 'ả' || c == 'ã' || c == 'ạ'
                    || c == 'é' || c == 'è' || c == 'ẻ' || c == 'ẽ' || c == 'ẹ'
                    || c == 'í' || c == 'ì' || c == 'ỉ' || c == 'ĩ' || c == 'ị'
                    || c == 'ó' || c == 'ò' || c == 'ỏ' || c == 'õ' || c == 'ọ'
                    || c == 'ú' || c == 'ù' || c == 'ủ' || c == 'ũ' || c == 'ụ'
                    || c == 'ý' || c == 'ỳ' || c == 'ỷ' || c == 'ỹ' || c == 'ỵ') {
                vietnameseChars++;
            }
            // Các ký tự tiếng Anh
            else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                englishChars++;
            }
        }

        // Xác định ngôn ngữ dựa trên tỷ lệ ký tự
        if (chineseChars > vietnameseChars && chineseChars > englishChars) {
            return LANGUAGE_ZH;
        } else if (vietnameseChars > englishChars) {
            return LANGUAGE_VI;
        } else {
            return LANGUAGE_EN;
        }
    }

    /**
     * Đánh giá chất lượng bản dịch
     */
    public double evaluateTranslationQuality(String original, String translated, String sourceLanguage, String targetLanguage) {
        // Các yếu tố đánh giá:
        // 1. Độ dài tương đối (nếu bản dịch quá ngắn hoặc quá dài có thể không chính xác)
        // 2. Dấu câu và định dạng
        // 3. Sự nhất quán của từ khóa

        // Tính toán đơn giản dựa trên độ dài
        if (original == null || translated == null) {
            return 0.0;
        }

        double score = 1.0;

        // Kiểm tra độ dài
        double lengthRatio = (double) translated.length() / original.length();
        if (lengthRatio < 0.5 || lengthRatio > 2.0) {
            score -= 0.3; // Độ dài quá khác biệt
        }

        // Kiểm tra dấu câu cơ bản
        int originalPunctuationCount = countPunctuation(original);
        int translatedPunctuationCount = countPunctuation(translated);
        double punctuationRatio = (double) translatedPunctuationCount / (originalPunctuationCount > 0 ? originalPunctuationCount : 1);
        if (punctuationRatio < 0.7 || punctuationRatio > 1.5) {
            score -= 0.2; // Dấu câu khác biệt nhiều
        }

        // Đảm bảo điểm tối thiểu là 0.0 và tối đa là 1.0
        return Math.max(0.0, Math.min(1.0, score));
    }

    /**
     * Đếm số lượng dấu câu trong văn bản
     */
    private int countPunctuation(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == '.' || c == ',' || c == '!' || c == '?' || c == ';' || c == ':' || c == '"' || c == '\'') {
                count++;
            }
        }
        return count;
    }

    /**
     * Map mã ngôn ngữ sang định dạng của DeepL
     */
    private String mapLanguageToDeepL(String language) {
        switch (language) {
            case LANGUAGE_ZH:
                return "ZH";
            case LANGUAGE_VI:
                return "VI";
            case LANGUAGE_EN:
                return "EN";
            default:
                return language.toUpperCase();
        }
    }

    /**
     * Tạo prompt cho Claude dịch thuật
     */
    private String buildClaudeTranslationPrompt(String text, String sourceLanguage, String targetLanguage) {
        String sourceName = getLanguageName(sourceLanguage);
        String targetName = getLanguageName(targetLanguage);

        StringBuilder prompt = new StringBuilder();
        prompt.append("Dịch văn bản sau đây từ ").append(sourceName).append(" sang ").append(targetName).append(":\n\n");
        prompt.append("```\n").append(text).append("\n```\n\n");
        prompt.append("Chỉ trả về bản dịch, không thêm giải thích hay bình luận. ");
        prompt.append("Giữ nguyên định dạng, dấu câu và các ký tự đặc biệt. ");

        if (targetLanguage.equals(LANGUAGE_VI)) {
            prompt.append("Đây là bản dịch sang tiếng Việt, hãy đảm bảo sử dụng từ ngữ tự nhiên, đúng ngữ pháp và có tính văn học cao.");
        }

        return prompt.toString();
    }

    /**
     * Lấy tên đầy đủ của ngôn ngữ
     */
    private String getLanguageName(String languageCode) {
        switch (languageCode) {
            case LANGUAGE_ZH:
                return "tiếng Trung";
            case LANGUAGE_VI:
                return "tiếng Việt";
            case LANGUAGE_EN:
                return "tiếng Anh";
            default:
                return languageCode;
        }
    }

    /**
     * Làm sạch văn bản đã dịch, loại bỏ các thông tin không cần thiết
     */
    private String cleanTranslatedText(String text) {
        // Loại bỏ các dấu backtick và thông tin dư thừa có thể xuất hiện trong kết quả
        text = text.replaceAll("```.*?```", "").trim();
        text = text.replaceAll("^```", "").replaceAll("```$", "").trim();

        // Loại bỏ các chú thích không cần thiết
        text = text.replaceAll("\\[Bản dịch\\]:", "").trim();
        text = text.replaceAll("Bản dịch:", "").trim();

        return text;
    }

    /**
     * Xóa bộ nhớ đệm dịch thuật
     */
    public void clearTranslationCache() {
        translationCache.clear();
        if (translationPreferences != null) {
            translationPreferences.edit().remove(TRANSLATION_CACHE_KEY).apply();
        }
        Log.d(TAG, "Đã xóa cache dịch thuật");
    }
}
