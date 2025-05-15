package com.jian.tangthucac.crawler.impl;

import android.content.Context;
import android.util.Log;

import com.jian.tangthucac.crawler.NovelCrawler;
import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.utils.ContentNormalizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Crawler thu thập truyện từ trang Wikidich
 */
public class WikidichCrawler implements NovelCrawler {
    private static final String TAG = "WikidichCrawler";
    private static final String BASE_URL = "https://wikidich.com";
    private static final String SEARCH_URL = BASE_URL + "/search?keyword=%s&page=%d";
    private static final String NOVEL_URL = BASE_URL + "/novel/%s";
    private static final String CHAPTER_LIST_URL = BASE_URL + "/novel/%s/chapters";
    private static final String CHAPTER_URL = BASE_URL + "/novel/%s/chapter/%s";

    private final Context context;
    private final ExecutorService executor;

    /**
     * Khởi tạo crawler với context
     * @param context Application context
     */
    public WikidichCrawler(Context context) {
        this.context = context;
        this.executor = Executors.newFixedThreadPool(3);
    }

    @Override
    public void searchNovels(String keyword, int page, OnSearchCompleteListener listener) {
        executor.execute(() -> {
            try {
                List<OriginalStory> results = new ArrayList<>();
                String url = String.format(SEARCH_URL, keyword.replace(" ", "+"), page);

                Log.d(TAG, "Tìm kiếm truyện từ URL: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                Elements novelItems = doc.select(".novel-item");

                for (Element item : novelItems) {
                    try {
                        Element linkElement = item.selectFirst("h2 a");
                        String title = linkElement.text();
                        String url_path = linkElement.attr("href");
                        String id = url_path.substring(url_path.lastIndexOf('/') + 1);

                        Element authorElement = item.selectFirst(".author");
                        String author = authorElement != null ? authorElement.text() : "Không xác định";

                        Element descElement = item.selectFirst(".desc");
                        String description = descElement != null ? descElement.text() : "";

                        Element coverElement = item.selectFirst("img");
                        String coverUrl = coverElement != null ? coverElement.attr("src") : "";

                        OriginalStory story = new OriginalStory(id, title, author, description);
                        story.setCoverImageUrl(coverUrl);
                        story.setSourceName("Wikidich");
                        story.setSourceBaseUrl(BASE_URL);

                        results.add(story);
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý item truyện: " + e.getMessage());
                    }
                }

                if (listener != null) {
                    listener.onSearchCompleted(results);
                }
            } catch (IOException e) {
                Log.e(TAG, "Lỗi khi tìm kiếm truyện: " + e.getMessage());
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }

    @Override
    public void getNovelDetails(String storyId, OnNovelDetailListener listener) {
        executor.execute(() -> {
            try {
                String url = String.format(NOVEL_URL, storyId);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                Element titleElement = doc.selectFirst("h1.title");
                String title = titleElement != null ? titleElement.text() : "Không xác định";

                Element authorElement = doc.selectFirst(".author");
                String author = authorElement != null ? authorElement.text() : "Không xác định";

                Element descElement = doc.selectFirst(".desc");
                String description = descElement != null ? descElement.text() : "";

                Element coverElement = doc.selectFirst(".novel-cover img");
                String coverUrl = coverElement != null ? coverElement.attr("src") : "";

                OriginalStory story = new OriginalStory(storyId, title, author, description);
                story.setCoverImageUrl(coverUrl);
                story.setSourceName("Wikidich");
                story.setSourceBaseUrl(BASE_URL);

                // Thêm thông tin khác nếu có
                Element genreElement = doc.selectFirst(".novel-genres");
                if (genreElement != null) {
                    story.setGenre(genreElement.text());
                }

                Element statusElement = doc.selectFirst(".novel-status");
                if (statusElement != null) {
                    story.setStatus(statusElement.text());
                }

                if (listener != null) {
                    listener.onNovelDetailLoaded(story);
                }
            } catch (IOException e) {
                Log.e(TAG, "Lỗi khi lấy thông tin truyện: " + e.getMessage());
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }

    @Override
    public void getChapterList(String storyId, OnChapterListListener listener) {
        executor.execute(() -> {
            try {
                String url = String.format(CHAPTER_LIST_URL, storyId);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                Elements chapterItems = doc.select(".chapter-item");

                List<String> chapterIds = new ArrayList<>();
                List<String> chapterTitles = new ArrayList<>();

                for (Element item : chapterItems) {
                    Element linkElement = item.selectFirst("a");
                    if (linkElement != null) {
                        String href = linkElement.attr("href");
                        // Lấy ID của chương từ URL
                        String chapterId = href.substring(href.lastIndexOf('/') + 1);
                        String title = linkElement.text();

                        chapterIds.add(chapterId);
                        chapterTitles.add(title);
                    }
                }

                if (listener != null) {
                    listener.onChapterListLoaded(chapterIds, chapterTitles);
                }
            } catch (IOException e) {
                Log.e(TAG, "Lỗi khi lấy danh sách chương: " + e.getMessage());
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }

    @Override
    public void getChapterContent(String storyId, String chapterId, OnChapterContentListener listener) {
        executor.execute(() -> {
            try {
                String url = String.format(CHAPTER_URL, storyId, chapterId);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                Element titleElement = doc.selectFirst(".chapter-title");
                String title = titleElement != null ? titleElement.text() : "Chương " + chapterId;

                Element contentElement = doc.selectFirst(".chapter-content");
                String content = "";

                if (contentElement != null) {
                    // Xóa các phần tử không mong muốn
                    contentElement.select(".ads, .comment-section").remove();
                    content = contentElement.html();

                    // Chuẩn hóa nội dung
                    content = ContentNormalizer.normalizeChapterContent(content);
                }

                if (listener != null) {
                    listener.onChapterContentLoaded(chapterId, title, content);
                }
            } catch (IOException e) {
                Log.e(TAG, "Lỗi khi lấy nội dung chương: " + e.getMessage());
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }
}
