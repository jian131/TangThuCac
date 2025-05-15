package com.jian.tangthucac.API;

import android.content.Context;
import android.util.Log;

import com.jian.tangthucac.model.SearchKeywordMap;
import com.jian.tangthucac.model.TranslatedChapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Lớp cung cấp dịch vụ dịch thuật
 */
public class TranslationService {
    private static final String TAG = "TranslationService";
    private static TranslationService instance;

    // API keys
    private String claudeApiKey;
    private String deeplApiKey;

    // API endpoints
    private static final String CLAUDE_API_ENDPOINT = "https://api.anthropic.com/v1/messages";
    private static final String DEEPL_API_ENDPOINT = "https://api-free.deepl.com/v2/translate";

    // OkHttpClient
    private final OkHttpClient client;

    // Media type
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Translation engines
    public static final String ENGINE_CLAUDE = "claude";
    public static final String ENGINE_DEEPL = "deepl";

    // Languages
    public static final String LANGUAGE_ZH = "zh"; // Chinese
    public static final String LANGUAGE_VI = "vi"; // Vietnamese
    public static final String LANGUAGE_EN = "en"; // English

    // Maximum text length for a single translation request
    private static final int MAX_TEXT_LENGTH = 5000;

    // Interface for translation callbacks
    public interface OnTranslationListener {
        void onTranslationCompleted(String translatedText);
        void onError(Exception e);
    }

    // Interface for keyword translation callbacks
    public interface OnKeywordTranslationListener {
        void onKeywordTranslated(SearchKeywordMap keywordMap);
        void onError(Exception e);
    }

    private TranslationService() {
        client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
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
    }

    /**
     * Dịch văn bản sử dụng Claude
     */
    public void translateWithClaude(String text, String sourceLanguage, String targetLanguage, OnTranslationListener listener) {
        if (claudeApiKey == null || claudeApiKey.isEmpty()) {
            listener.onError(new Exception("Claude API key not set"));
            return;
        }

        // Xây dựng prompt
        String prompt = buildClaudePrompt(text, sourceLanguage, targetLanguage);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "claude-3-opus-20240229");
            requestBody.put("max_tokens", 4000);
            requestBody.put("temperature", 0.1);

            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(message);

            requestBody.put("messages", messages);

            Request request = new Request.Builder()
                .url(CLAUDE_API_ENDPOINT)
                .addHeader("x-api-key", claudeApiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        listener.onError(new Exception("API request failed: " + response.code()));
                        return;
                    }

                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject content = jsonResponse.getJSONArray("content").getJSONObject(0);
                        String translatedText = content.getString("text");

                        // Process the translated text (remove any additional comments Claude might add)
                        translatedText = cleanClaudeTranslation(translatedText);

                        listener.onTranslationCompleted(translatedText);
                    } catch (JSONException e) {
                        listener.onError(e);
                    }
                }
            });
        } catch (JSONException e) {
            listener.onError(e);
        }
    }

    /**
     * Dịch văn bản sử dụng DeepL
     */
    public void translateWithDeepL(String text, String sourceLanguage, String targetLanguage, OnTranslationListener listener) {
        if (deeplApiKey == null || deeplApiKey.isEmpty()) {
            listener.onError(new Exception("DeepL API key not set"));
            return;
        }

        // Map ngôn ngữ sang mã DeepL
        String deeplSourceLang = mapToDeepLLanguage(sourceLanguage);
        String deeplTargetLang = mapToDeepLLanguage(targetLanguage);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("text", text);
            requestBody.put("source_lang", deeplSourceLang);
            requestBody.put("target_lang", deeplTargetLang);
            requestBody.put("preserve_formatting", 1);

            Request request = new Request.Builder()
                .url(DEEPL_API_ENDPOINT)
                .addHeader("Authorization", "DeepL-Auth-Key " + deeplApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        listener.onError(new Exception("API request failed: " + response.code()));
                        return;
                    }

                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray translations = jsonResponse.getJSONArray("translations");
                        String translatedText = translations.getJSONObject(0).getString("text");

                        listener.onTranslationCompleted(translatedText);
                    } catch (JSONException e) {
                        listener.onError(e);
                    }
                }
            });
        } catch (JSONException e) {
            listener.onError(e);
        }
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
    public void translateChapter(TranslatedChapter chapter, String sourceLanguage, String targetLanguage, OnTranslationListener listener) {
        // Sử dụng Claude cho chất lượng cao nhất
        // Dịch tiêu đề
        translateWithClaude(chapter.getTitle(), sourceLanguage, targetLanguage, new OnTranslationListener() {
            @Override
            public void onTranslationCompleted(String translatedTitle) {
                // Lưu tiêu đề đã dịch
                if (targetLanguage.equals(LANGUAGE_VI)) {
                    chapter.setTitleVi(translatedTitle);
                }

                // Dịch nội dung - cần chia nhỏ nếu quá dài
                String content = chapter.getContent();
                if (content.length() > MAX_TEXT_LENGTH) {
                    // Chia nội dung thành các phần nhỏ hơn
                    translateLongContent(content, sourceLanguage, targetLanguage, new OnTranslationListener() {
                        @Override
                        public void onTranslationCompleted(String translatedContent) {
                            if (targetLanguage.equals(LANGUAGE_VI)) {
                                chapter.setContentVi(translatedContent);
                            }

                            // Cập nhật thông tin dịch
                            chapter.setTranslationEngine(ENGINE_CLAUDE);
                            chapter.setTranslatedTime(System.currentTimeMillis());

                            listener.onTranslationCompleted(translatedContent);
                        }

                        @Override
                        public void onError(Exception e) {
                            listener.onError(e);
                        }
                    });
                } else {
                    // Dịch nội dung nếu không quá dài
                    translateWithClaude(content, sourceLanguage, targetLanguage, new OnTranslationListener() {
                        @Override
                        public void onTranslationCompleted(String translatedContent) {
                            if (targetLanguage.equals(LANGUAGE_VI)) {
                                chapter.setContentVi(translatedContent);
                            }

                            // Cập nhật thông tin dịch
                            chapter.setTranslationEngine(ENGINE_CLAUDE);
                            chapter.setTranslatedTime(System.currentTimeMillis());

                            listener.onTranslationCompleted(translatedContent);
                        }

                        @Override
                        public void onError(Exception e) {
                            listener.onError(e);
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                // Fallback to DeepL if Claude fails
                translateWithDeepL(chapter.getTitle(), sourceLanguage, targetLanguage, new OnTranslationListener() {
                    @Override
                    public void onTranslationCompleted(String translatedTitle) {
                        // Save translated title
                        if (targetLanguage.equals(LANGUAGE_VI)) {
                            chapter.setTitleVi(translatedTitle);
                        }

                        // Translate content using DeepL
                        translateWithDeepL(chapter.getContent(), sourceLanguage, targetLanguage, new OnTranslationListener() {
                            @Override
                            public void onTranslationCompleted(String translatedContent) {
                                if (targetLanguage.equals(LANGUAGE_VI)) {
                                    chapter.setContentVi(translatedContent);
                                }

                                // Update translation info
                                chapter.setTranslationEngine(ENGINE_DEEPL);
                                chapter.setTranslatedTime(System.currentTimeMillis());

                                listener.onTranslationCompleted(translatedContent);
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
     * Xây dựng prompt cho Claude
     */
    private String buildClaudePrompt(String text, String sourceLanguage, String targetLanguage) {
        String sourceLangName = getLanguageName(sourceLanguage);
        String targetLangName = getLanguageName(targetLanguage);

        return "Dịch đoạn văn bản sau từ " + sourceLangName + " sang " + targetLangName +
               ". Hãy dịch chính xác, tự nhiên và giữ nguyên định dạng. " +
               "Chỉ trả về bản dịch, không thêm giải thích hay bình luận.\n\n" +
               "Văn bản gốc:\n" + text;
    }

    /**
     * Xử lý bản dịch từ Claude
     */
    private String cleanClaudeTranslation(String translation) {
        // Loại bỏ các dòng dư thừa Claude đôi khi thêm vào
        if (translation.startsWith("Bản dịch:")) {
            translation = translation.substring("Bản dịch:".length()).trim();
        }
        return translation;
    }

    /**
     * Dịch nội dung dài bằng cách chia nhỏ
     */
    private void translateLongContent(String content, String sourceLanguage, String targetLanguage, OnTranslationListener listener) {
        Log.d(TAG, "Translating long content of length: " + content.length());

        // Chia nội dung thành các phần nhỏ hơn khoảng 5000 ký tự, cố gắng chia ở ranh giới câu
        StringBuilder fullTranslation = new StringBuilder();
        StringBuilder currentChunk = new StringBuilder();
        String[] paragraphs = content.split("\n");

        int chunkCount = 0;
        int totalChunks = (int) Math.ceil(content.length() / (double) MAX_TEXT_LENGTH);

        for (String paragraph : paragraphs) {
            // Nếu thêm đoạn này vào chunk hiện tại sẽ vượt quá kích thước tối đa
            if (currentChunk.length() + paragraph.length() + 1 > MAX_TEXT_LENGTH) {
                // Dịch chunk hiện tại
                String chunkToTranslate = currentChunk.toString();
                int currentChunkNumber = ++chunkCount;

                translateWithClaude(chunkToTranslate, sourceLanguage, targetLanguage, new OnTranslationListener() {
                    @Override
                    public void onTranslationCompleted(String translatedChunk) {
                        synchronized (fullTranslation) {
                            fullTranslation.append(translatedChunk).append("\n\n");

                            Log.d(TAG, "Translated chunk " + currentChunkNumber + "/" + totalChunks);

                            // Nếu đây là chunk cuối cùng, gọi listener
                            if (currentChunkNumber == totalChunks) {
                                listener.onTranslationCompleted(fullTranslation.toString().trim());
                            }
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        listener.onError(e);
                    }
                });

                // Bắt đầu chunk mới
                currentChunk = new StringBuilder(paragraph).append("\n");
            } else {
                // Thêm đoạn vào chunk hiện tại
                currentChunk.append(paragraph).append("\n");
            }
        }

        // Dịch chunk cuối cùng nếu còn
        if (currentChunk.length() > 0) {
            String chunkToTranslate = currentChunk.toString();
            int currentChunkNumber = ++chunkCount;

            translateWithClaude(chunkToTranslate, sourceLanguage, targetLanguage, new OnTranslationListener() {
                @Override
                public void onTranslationCompleted(String translatedChunk) {
                    synchronized (fullTranslation) {
                        fullTranslation.append(translatedChunk);

                        Log.d(TAG, "Translated chunk " + currentChunkNumber + "/" + totalChunks);

                        // Nếu đây là chunk cuối cùng, gọi listener
                        if (currentChunkNumber == totalChunks) {
                            listener.onTranslationCompleted(fullTranslation.toString().trim());
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    listener.onError(e);
                }
            });
        }
    }

    /**
     * Chuyển đổi mã ngôn ngữ sang tên ngôn ngữ
     */
    private String getLanguageName(String langCode) {
        switch (langCode) {
            case LANGUAGE_ZH:
                return "tiếng Trung";
            case LANGUAGE_VI:
                return "tiếng Việt";
            case LANGUAGE_EN:
                return "tiếng Anh";
            default:
                return langCode;
        }
    }

    /**
     * Chuyển đổi mã ngôn ngữ sang mã DeepL
     */
    private String mapToDeepLLanguage(String langCode) {
        switch (langCode) {
            case LANGUAGE_ZH:
                return "ZH";
            case LANGUAGE_VI:
                return "VI";
            case LANGUAGE_EN:
                return "EN";
            default:
                return langCode.toUpperCase();
        }
    }

    /**
     * Nhận diện ngôn ngữ từ văn bản
     * Sử dụng một thuật toán đơn giản dựa trên bảng mã để phân biệt tiếng Trung và tiếng Việt
     */
    public String detectLanguage(String text) {
        if (text == null || text.isEmpty()) {
            return LANGUAGE_EN; // Mặc định là tiếng Anh
        }

        text = text.trim();

        // Đếm số ký tự Trung Quốc
        int chineseCharCount = 0;
        // Đếm số ký tự Latin + dấu tiếng Việt
        int vietnameseCharCount = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Phạm vi ký tự Hán
            if (c >= 0x4E00 && c <= 0x9FFF) {
                chineseCharCount++;
            }
            // Ký tự Latin cơ bản và dấu tiếng Việt
            else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
                     "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ".indexOf(c) >= 0) {
                vietnameseCharCount++;
            }
        }

        // Quyết định ngôn ngữ dựa trên tỷ lệ
        if (chineseCharCount > vietnameseCharCount * 0.3) {
            return LANGUAGE_ZH;
        } else if (vietnameseCharCount > text.length() * 0.3) {
            return LANGUAGE_VI;
        } else {
            return LANGUAGE_EN;
        }
    }
}
