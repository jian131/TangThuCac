# Hướng dẫn hoàn thiện tính năng tìm kiếm và dịch truyện Trung Quốc

## Cấu trúc dự án

Dự án gồm các phần chính sau:

1. **Mô hình dữ liệu**:

   - `OriginalStory.java`: Lưu trữ thông tin truyện gốc (tiếng Trung)
   - `TranslatedChapter.java`: Lưu trữ thông tin chương đã dịch
   - `SearchKeywordMap.java`: Ánh xạ từ khóa tìm kiếm giữa các ngôn ngữ

2. **API và Crawlers**:

   - `TranslationService.java`: Giao diện với API dịch thuật (Claude, DeepL)
   - `NovelCrawler.java`: Interface cho các crawlers
   - `WebsiteCrawlerBase.java`: Lớp cơ sở cho các crawlers
   - `WebnovelCrawler.java`: Crawler cho trang Webnovel.com
   - `CrawlerFactory.java`: Factory để quản lý các crawlers

3. **Quản lý dữ liệu**:

   - `ChineseNovelManager.java`: Quản lý dữ liệu truyện và cache

4. **Giao diện người dùng**:

   - `ChineseNovelSearchActivity.java`: Activity tìm kiếm truyện
   - `StoryDetailActivity.java`: Activity xem chi tiết truyện
   - `ApiSettingsActivity.java`: Activity cài đặt API keys
   - Các adapter: `SearchResultAdapter.java`, `ChapterAdapter.java`

5. **Background Tasks**:
   - `ChapterDownloadWorker.java`: Worker để tải và dịch nội dung chương

## Cách giải quyết các lỗi linter

### Thêm thư viện

Đã bổ sung các thư viện cần thiết vào file `app/build.gradle.kts`:

- Thư viện JSON: `org.json:json:20231013`
- WorkManager: `androidx.work:work-runtime:2.8.1`
- ViewBinding: `androidx.databinding:viewbinding:8.1.0`

### Sửa lỗi package

Đảm bảo import đúng các package và không sử dụng các package không tồn tại. Ví dụ:

- Sửa import `package android.content does not exist` bằng cách thêm thư viện Android core
- Sửa import `package androidx.annotation does not exist` bằng cách thêm thư viện androidx annotation

### Sửa lỗi API level

Cập nhật `compileSdk` và `targetSdk` trong file `app/build.gradle.kts` để đảm bảo có thể sử dụng các API mới nhất.

## Cấu hình Firebase

Firebase đã được cấu hình trong dự án, cần đảm bảo các thành phần sau:

1. **Firebase Authentication**: Đã được cấu hình để cho phép đăng nhập
2. **Firebase Realtime Database**: Cần tạo các node sau:
   - `chinese_novels`: Lưu trữ thông tin truyện gốc
   - `translated_chapters`: Lưu trữ thông tin chương đã dịch
   - `keyword_maps`: Lưu trữ ánh xạ từ khóa tìm kiếm
3. **Firebase Storage**: Cho lưu trữ hình ảnh bìa truyện

### Quy tắc bảo mật

```json
{
  "rules": {
    "chinese_novels": {
      ".read": true,
      ".write": "auth != null"
    },
    "translated_chapters": {
      ".read": true,
      ".write": "auth != null"
    },
    "keyword_maps": {
      ".read": true,
      ".write": "auth != null"
    }
  }
}
```

## Các bước tiếp theo cần thực hiện

1. **Sửa lỗi linter còn lại**:

   - Sửa các lỗi linter để ứng dụng có thể biên dịch

2. **Hoàn thiện các crawlers**:

   - Triển khai crawlers cho Qidian, JJWXC, v.v.
   - Cập nhật các phương thức crawl để lấy dữ liệu thực tế

3. **Cải thiện background tasks**:

   - Cải thiện cơ chế tải và dịch chương truyện trong background
   - Thêm thông báo tiến trình cho người dùng

4. **Thêm tính năng**:

   - Thêm tính năng tìm kiếm nâng cao (lọc theo thể loại, tác giả, etc.)
   - Thêm tính năng đồng bộ dữ liệu giữa các thiết bị

5. **Testing**:
   - Thực hiện kiểm thử toàn diện trên các thiết bị khác nhau
   - Viết unit tests và instrumented tests

## Tài nguyên tham khảo

- [Claude API Documentation](https://docs.anthropic.com/claude/reference/getting-started-with-the-api)
- [DeepL API Documentation](https://www.deepl.com/docs-api)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Android WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Jsoup Documentation](https://jsoup.org/cookbook/)
