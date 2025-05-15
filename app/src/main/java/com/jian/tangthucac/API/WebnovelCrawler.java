package com.jian.tangthucac.API;

import android.content.Context;
import android.util.Log;

import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.model.TranslatedChapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Crawler cho trang Webnovel.com
 */
public class WebnovelCrawler extends WebsiteCrawlerBase {
    private static final String TAG = "WebnovelCrawler";

    // URL endpoints
    private static final String SEARCH_URL = "https://www.webnovel.com/go/pcm/search/result?keywords=";
    private static final String NOVEL_DETAIL_URL = "https://www.webnovel.com/book/";
    private static final String API_URL = "https://www.webnovel.com/apiajax/";

    private final OkHttpClient client;

    public WebnovelCrawler(Context context) {
        super(context);
        client = new OkHttpClient.Builder().build();
    }

    @Override
    public String getSourceName() {
        return "Webnovel";
    }

    @Override
    public void searchNovel(String keyword, OnSearchResultListener listener) {
        executor.execute(() -> {
            try {
                String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
                String url = SEARCH_URL + encodedKeyword;

                // Trong ứng dụng thực tế, bạn sẽ gửi HTTP request tới trang web
                // và parse kết quả tìm kiếm. Ở đây chúng ta giả lập kết quả:
                Document doc = Jsoup.connect(url).get();

                List<OriginalStory> results = parseSearchResults(doc);

                // Trả kết quả về main thread
                mainHandler.post(() -> listener.onSearchCompleted(results));

            } catch (Exception e) {
                handleError(listener, "Lỗi khi tìm kiếm truyện: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void getNovelDetails(String novelId, OnNovelDetailListener listener) {
        executor.execute(() -> {
            try {
                String url = NOVEL_DETAIL_URL + novelId;

                // Trong ứng dụng thực tế, bạn sẽ gửi HTTP request tới trang web
                // và parse thông tin chi tiết truyện
                Document doc = Jsoup.connect(url).get();

                OriginalStory story = parseNovelDetails(doc, novelId);

                // Trả kết quả về main thread
                mainHandler.post(() -> listener.onNovelDetailLoaded(story));

            } catch (Exception e) {
                handleError(listener, "Lỗi khi lấy chi tiết truyện: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void getChapterList(String novelId, OnNovelDetailListener listener) {
        // Tương tự như getNovelDetails nhưng tập trung vào danh sách chương
        getNovelDetails(novelId, listener);
    }

    @Override
    public void getChapterContent(String chapterId, OnChapterContentListener listener) {
        executor.execute(() -> {
            try {
                // Trích xuất novelId và chapterNumber từ chapterId
                String[] parts = chapterId.split("_");
                String novelId = parts[0] + "_" + parts[1];
                int chapterNumber = Integer.parseInt(parts[2]);

                // API URL để lấy nội dung chương (đây là ví dụ, URL thực tế có thể khác)
                String url = API_URL + "chapter/GetContent?bookId=" + novelId + "&chapterId=" + chapterNumber;

                // Trong ứng dụng thực tế, bạn sẽ gửi HTTP request để lấy nội dung chương
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handleError(listener, "Lỗi khi lấy nội dung chương: " + e.getMessage(), e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            handleError(listener, "Lỗi HTTP: " + response.code(), null);
                            return;
                        }

                        try {
                            String jsonData = response.body().string();
                            JSONObject jsonObject = new JSONObject(jsonData);

                            // Parse JSON để lấy nội dung chương (cấu trúc JSON thực tế có thể khác)
                            TranslatedChapter chapter = parseChapterContent(jsonObject, chapterId);

                            mainHandler.post(() -> listener.onChapterLoaded(chapter));

                        } catch (JSONException e) {
                            handleError(listener, "Lỗi parse JSON: " + e.getMessage(), e);
                        }
                    }
                });

            } catch (Exception e) {
                handleError(listener, "Lỗi khi lấy nội dung chương: " + e.getMessage(), e);
            }
        });
    }

    // Helper methods để parse dữ liệu HTML/JSON

    private List<OriginalStory> parseSearchResults(Document doc) {
        List<OriginalStory> results = new ArrayList<>();

        try {
            // Đây là ví dụ, cấu trúc HTML thực tế của Webnovel sẽ khác
            Elements novelElements = doc.select(".j_pg_novel");

            for (Element novelElement : novelElements) {
                String id = novelElement.attr("data-bookid");
                String title = novelElement.select(".g_h_title").text();
                String author = novelElement.select(".g_r_author").text();
                String imageUrl = novelElement.select("img").attr("src");
                String desc = novelElement.select(".g_r_description").text();

                OriginalStory story = new OriginalStory();
                story.setId(generateNovelId(id));
                story.setTitle(title);
                story.setAuthor(author);
                story.setImageUrl(imageUrl);
                story.setDescription(desc);
                story.setSource(getSourceName());
                story.setSourceUrl(NOVEL_DETAIL_URL + id);
                story.setLanguage("zh");

                // Thêm dữ liệu fake khác
                ArrayList<String> genres = new ArrayList<>();
                genres.add("玄幻");
                genres.add("武侠");
                story.setGenres(genres);

                results.add(story);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi parse kết quả tìm kiếm: " + e.getMessage(), e);
        }

        return results;
    }

    private OriginalStory parseNovelDetails(Document doc, String novelId) {
        OriginalStory story = new OriginalStory();

        try {
            // Đây là ví dụ, cấu trúc HTML thực tế của Webnovel sẽ khác
            String title = doc.select(".g_h_title").text();
            String author = doc.select(".g_h_author").text();
            String imageUrl = doc.select(".g_h_cover img").attr("src");
            String desc = doc.select(".g_h_description").text();
            int chapterCount = Integer.parseInt(doc.select(".g_h_chapter_count").text().replaceAll("[^0-9]", ""));
            boolean completed = doc.select(".g_h_status").text().contains("完结");

            story.setId(novelId);
            story.setTitle(title);
            story.setAuthor(author);
            story.setImageUrl(imageUrl);
            story.setDescription(desc);
            story.setSource(getSourceName());
            story.setSourceUrl(NOVEL_DETAIL_URL + novelId);
            story.setLanguage("zh");
            story.setChapterCount(chapterCount);
            story.setCompleted(completed);

            // Thêm dữ liệu fake khác
            ArrayList<String> genres = new ArrayList<>();
            genres.add("玄幻");
            genres.add("武侠");
            story.setGenres(genres);

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi parse chi tiết truyện: " + e.getMessage(), e);
        }

        return story;
    }

    private TranslatedChapter parseChapterContent(JSONObject jsonObject, String chapterId) throws JSONException {
        TranslatedChapter chapter = new TranslatedChapter();

        // Đây là ví dụ, cấu trúc JSON thực tế của Webnovel sẽ khác
        JSONObject data = jsonObject.getJSONObject("data");
        String title = data.getString("title");
        String content = data.getString("content");
        int chapterNumber = data.getInt("chapterIndex");

        chapter.setId(chapterId);
        chapter.setTitle(title);
        chapter.setContent(content);
        chapter.setChapterNumber(chapterNumber);
        chapter.setSourceUrl(API_URL + "chapter/GetContent?chapterId=" + chapterId);

        return chapter;
    }
}
