package com.jian.tangthucac.crawler;

import android.content.Context;
import android.util.Log;

import com.jian.tangthucac.crawler.impl.WikidichCrawler;
import com.jian.tangthucac.crawler.impl.QidianCrawler;

/**
 * Factory tạo và quản lý các instance của crawler
 */
public class CrawlerFactory {
    private static final String TAG = "CrawlerFactory";

    // Các nguồn truyện được hỗ trợ
    public static final String SOURCE_WIKIDICH = "wikidich";
    public static final String SOURCE_QIDIAN = "qidian";
    public static final String SOURCE_AUTO = "auto";

    private static CrawlerFactory instance;

    /**
     * Phương thức tương thích ngược - lấy instance của factory
     * @param context Application context
     * @return Instance của CrawlerFactory
     */
    public static CrawlerFactory getInstance(Context context) {
        if (instance == null) {
            instance = new CrawlerFactory();
        }
        return instance;
    }

    /**
     * Phương thức tương thích ngược - kiểm tra nguồn có hỗ trợ không
     * @param source Tên nguồn cần kiểm tra
     * @return true nếu nguồn được hỗ trợ
     */
    public boolean isSourceSupported(String source) {
        return isSupportedSource(source);
    }

    /**
     * Phương thức tương thích ngược - lấy crawler cho nguồn
     * @param source Tên nguồn
     * @return Crawler cho nguồn đó
     */
    public NovelCrawler getCrawler(String source) {
        return createCrawler(source, null);
    }

    /**
     * Tạo crawler phù hợp với nguồn được yêu cầu
     * @param source Mã nguồn truyện
     * @param context Application context
     * @return NovelCrawler instance tương ứng
     */
    public static NovelCrawler createCrawler(String source, Context context) {
        Log.d(TAG, "Tạo crawler cho nguồn: " + source);

        switch (source) {
            case SOURCE_WIKIDICH:
                return new WikidichCrawler(context);
            case SOURCE_QIDIAN:
                return new QidianCrawler(context);
            case SOURCE_AUTO:
            default:
                // Mặc định sử dụng Wikidich
                return new WikidichCrawler(context);
        }
    }

    /**
     * Kiểm tra xem nguồn có hỗ trợ không
     * @param source Mã nguồn cần kiểm tra
     * @return true nếu nguồn được hỗ trợ
     */
    public static boolean isSupportedSource(String source) {
        return source.equals(SOURCE_WIKIDICH) ||
               source.equals(SOURCE_QIDIAN) ||
               source.equals(SOURCE_AUTO);
    }
}
