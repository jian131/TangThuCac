package com.jian.tangthucac.crawler.impl;

import android.content.Context;
import android.util.Log;

import com.jian.tangthucac.crawler.NovelCrawler;
import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.utils.ContentNormalizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Crawler thu thập truyện từ trang Qidian (sử dụng API)
 */
public class QidianCrawler implements NovelCrawler {
    private static final String TAG = "QidianCrawler";
    private static final String BASE_URL = "https://www.qidian.com";
    private static final String API_SEARCH_URL = "https://www.qidian.com/search/api?kw=%s&page=%d";
    private static final String API_NOVEL_URL = "https://www.qidian.com/book/%s";
    private static final String API_CHAPTER_LIST_URL = "https://www.qidian.com/book/%s/catalog";
    private static final String API_CHAPTER_URL = "https://www.qidian.com/chapter/%s/%s";

    private final Context context;
    private final OkHttpClient client;
    private final ExecutorService executor;

    /**
     * Khởi tạo crawler với context
     * @param context Application context
     */
    public QidianCrawler(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder().build();
        this.executor = Executors.newFixedThreadPool(3);
    }

    @Override
    public void searchNovels(String keyword, int page, OnSearchCompleteListener listener) {
        executor.execute(() -> {
            String url = String.format(API_SEARCH_URL, keyword.replace(" ", "+"), page);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Lỗi khi tìm kiếm truyện: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful() || responseBody == null) {
                            throw new IOException("Phản hồi không thành công: " + response);
                        }

                        String jsonData = responseBody.string();
                        List<OriginalStory> results = parseSearchResults(jsonData);

                        if (listener != null) {
                            listener.onSearchCompleted(results);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý kết quả tìm kiếm: " + e.getMessage());
                        if (listener != null) {
                            listener.onError(e);
                        }
                    }
                }
            });
        });
    }

    private List<OriginalStory> parseSearchResults(String jsonData) throws JSONException {
        List<OriginalStory> results = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray books = jsonObject.getJSONArray("books");

        for (int i = 0; i < books.length(); i++) {
            JSONObject book = books.getJSONObject(i);

            String id = book.getString("bookId");
            String title = book.getString("bookName");
            String author = book.getString("authorName");
            String description = book.optString("description", "");
            String coverUrl = book.optString("coverUrl", "");

            OriginalStory story = new OriginalStory(id, title, author, description);
            story.setCoverImageUrl(coverUrl);
            story.setSourceName("Qidian");
            story.setSourceBaseUrl(BASE_URL);

            // Thêm thông tin khác
            story.setGenre(book.optString("categoryName", ""));
            story.setStatus(book.optString("bookStatus", ""));

            results.add(story);
        }

        return results;
    }

    @Override
    public void getNovelDetails(String storyId, OnNovelDetailListener listener) {
        executor.execute(() -> {
            String url = String.format(API_NOVEL_URL, storyId);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Lỗi khi lấy thông tin truyện: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful() || responseBody == null) {
                            throw new IOException("Phản hồi không thành công: " + response);
                        }

                        String htmlData = responseBody.string();
                        OriginalStory story = parseNovelDetails(htmlData, storyId);

                        if (listener != null) {
                            listener.onNovelDetailLoaded(story);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý thông tin truyện: " + e.getMessage());
                        if (listener != null) {
                            listener.onError(e);
                        }
                    }
                }
            });
        });
    }

    private OriginalStory parseNovelDetails(String htmlData, String storyId) {
        // Xử lý dữ liệu HTML để lấy thông tin truyện
        // Trong thực tế, cần sử dụng Jsoup để phân tích HTML

        // Demo: Tạo dữ liệu mẫu
        OriginalStory story = new OriginalStory(storyId, "Truyện Qidian " + storyId, "Tác giả Qidian", "Đây là mô tả của truyện...");
        story.setSourceName("Qidian");
        story.setSourceBaseUrl(BASE_URL);
        story.setGenre("Tu tiên");
        story.setStatus("Đang ra");

        return story;
    }

    @Override
    public void getChapterList(String storyId, OnChapterListListener listener) {
        executor.execute(() -> {
            String url = String.format(API_CHAPTER_LIST_URL, storyId);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Lỗi khi lấy danh sách chương: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful() || responseBody == null) {
                            throw new IOException("Phản hồi không thành công: " + response);
                        }

                        String htmlData = responseBody.string();
                        List<String> chapterIds = new ArrayList<>();
                        List<String> chapterTitles = new ArrayList<>();

                        // Xử lý dữ liệu HTML để lấy danh sách chương
                        // Demo: Tạo dữ liệu mẫu
                        for (int i = 1; i <= 10; i++) {
                            chapterIds.add(String.valueOf(i));
                            chapterTitles.add("Chương " + i);
                        }

                        if (listener != null) {
                            listener.onChapterListLoaded(chapterIds, chapterTitles);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý danh sách chương: " + e.getMessage());
                        if (listener != null) {
                            listener.onError(e);
                        }
                    }
                }
            });
        });
    }

    @Override
    public void getChapterContent(String storyId, String chapterId, OnChapterContentListener listener) {
        executor.execute(() -> {
            String url = String.format(API_CHAPTER_URL, storyId, chapterId);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Lỗi khi lấy nội dung chương: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful() || responseBody == null) {
                            throw new IOException("Phản hồi không thành công: " + response);
                        }

                        String htmlData = responseBody.string();

                        // Xử lý dữ liệu HTML để lấy nội dung chương
                        String title = "Chương " + chapterId;
                        StringBuilder contentBuilder = new StringBuilder();

                        // Demo: Tạo nội dung mẫu
                        contentBuilder.append("<p>Đây là nội dung chương ").append(chapterId).append(" của truyện ").append(storyId).append("</p>");
                        contentBuilder.append("<p>这是小说").append(storyId).append("的第").append(chapterId).append("章内容</p>");

                        for (int i = 0; i < 10; i++) {
                            contentBuilder.append("<p>Đoạn văn mẫu thứ ").append(i+1).append(" của chương.</p>");
                        }

                        String content = ContentNormalizer.normalizeChapterContent(contentBuilder.toString());

                        if (listener != null) {
                            listener.onChapterContentLoaded(chapterId, title, content);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý nội dung chương: " + e.getMessage());
                        if (listener != null) {
                            listener.onError(e);
                        }
                    }
                }
            });
        });
    }
}
