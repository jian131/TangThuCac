package com.jian.tangthucac.API;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.jian.tangthucac.model.Chapter;
import com.jian.tangthucac.model.Story;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WikidichScraper {
    private static final String TAG = "WikidichScraper";
    private static final String BASE_URL = "https://truyenwikidich.net";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface OnScrapingCompleteListener {
        void onStoryScraped(Story story);
        void onError(Exception e);
    }

    public interface OnChapterScrapingListener {
        void onChapterScraped(Chapter chapter);
        void onError(Exception e);
    }

    public static void scrapeStory(String storyUrl, OnScrapingCompleteListener listener) {
        executor.execute(() -> {
            try {
                Story story = new Story();

                // Kết nối và lấy trang HTML
                Document doc = Jsoup.connect(storyUrl).get();

                // Trích xuất thông tin truyện
                String title = doc.select(".book-info h1").text();
                String author = doc.select(".book-info .author span").text();
                String imageUrl = doc.select(".book-img img").attr("src");
                if (!imageUrl.startsWith("http")) {
                    imageUrl = BASE_URL + imageUrl;
                }
                String description = doc.select(".desc-text").text();

                // Lấy danh sách thể loại
                Elements genreElements = doc.select(".book-info .tag a");
                List<String> genreList = new ArrayList<>();
                for (Element genreElement : genreElements) {
                    genreList.add(genreElement.text());
                }

                // Lấy danh sách chương
                Elements chapterElements = doc.select(".list-chapter li a");
                Map<String, Chapter> chapters = new HashMap<>();
                int chapterCount = 0;
                for (Element chapterElement : chapterElements) {
                    String chapterTitle = chapterElement.text();
                    String chapterUrl = chapterElement.attr("href");
                    if (!chapterUrl.startsWith("http")) {
                        chapterUrl = BASE_URL + chapterUrl;
                    }

                    Chapter chapter = new Chapter();
                    chapter.setTitle(chapterTitle);
                    chapter.setUrl(chapterUrl);

                    chapters.put("chapter_" + chapterCount, chapter);
                    chapterCount++;
                }

                // Thiết lập các giá trị cho story
                story.setTitle(title);
                story.setAuthor(author);
                story.setImage(imageUrl);
                story.setDescription(description);
                story.setGenres(genreList);
                story.setChapters(chapters);
                story.setTotalChapters(chapterCount);

                // Trả về kết quả trên main thread
                final Story finalStory = story;
                mainHandler.post(() -> listener.onStoryScraped(finalStory));
            } catch (IOException e) {
                Log.e(TAG, "Lỗi khi scrape truyện: " + e.getMessage());
                mainHandler.post(() -> listener.onError(e));
            }
        });
    }

    public static void scrapeChapter(String chapterUrl, OnChapterScrapingListener listener) {
        executor.execute(() -> {
            try {
                // Kết nối và lấy trang HTML
                Document doc = Jsoup.connect(chapterUrl).get();

                // Trích xuất thông tin chương
                String title = doc.select(".chapter-title").text();

                // Lấy nội dung chương (loại bỏ quảng cáo và các phần thừa)
                Element contentElement = doc.select("#chapter-content").first();
                String content = "";
                if (contentElement != null) {
                    // Loại bỏ các phần tử không mong muốn
                    contentElement.select("script, ins, iframe").remove();
                    content = contentElement.html();
                }

                Chapter chapter = new Chapter();
                chapter.setTitle(title);
                chapter.setContent(content);
                chapter.setUrl(chapterUrl);

                // Trả về kết quả trên main thread
                final Chapter finalChapter = chapter;
                mainHandler.post(() -> listener.onChapterScraped(finalChapter));
            } catch (IOException e) {
                Log.e(TAG, "Lỗi khi scrape chương: " + e.getMessage());
                mainHandler.post(() -> listener.onError(e));
            }
        });
    }
}
