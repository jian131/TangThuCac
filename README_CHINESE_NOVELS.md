# Hướng dẫn sử dụng tính năng tìm kiếm và dịch truyện Trung Quốc

Ứng dụng TangThuCac giờ đây đã được tích hợp khả năng tìm kiếm truyện từ các nguồn Trung Quốc và tự động dịch sang tiếng Việt. Đây là hướng dẫn chi tiết về cách sử dụng tính năng này.

## Mục lục

1. [Thiết lập ban đầu](#thiết-lập-ban-đầu)
2. [Tìm kiếm truyện](#tìm-kiếm-truyện)
3. [Tải xuống truyện](#tải-xuống-truyện)
4. [Đọc truyện đã dịch](#đọc-truyện-đã-dịch)
5. [Quản lý API key](#quản-lý-api-key)
6. [Xử lý lỗi thường gặp](#xử-lý-lỗi-thường-gặp)

## Thiết lập ban đầu

Để sử dụng tính năng dịch thuật, bạn cần cấu hình API keys cho Claude và/hoặc DeepL:

1. Đăng nhập vào ứng dụng TangThuCac
2. Mở tab "Tài khoản" (nút cuối cùng ở thanh điều hướng dưới cùng)
3. Bấm vào biểu tượng menu (3 chấm) ở góc trên bên phải
4. Chọn "Cài đặt API"
5. Nhập API key của Claude và DeepL (bạn có thể đăng ký miễn phí tại [trang web của Anthropic](https://www.anthropic.com) và [trang web của DeepL](https://www.deepl.com/pro-api))
6. Bấm "Lưu"

> **Lưu ý**: Nếu không cấu hình API keys, ứng dụng vẫn có thể tìm kiếm truyện nhưng sẽ không thể dịch nội dung.

## Tìm kiếm truyện

Để tìm kiếm truyện từ các nguồn Trung Quốc:

1. Mở tab "Tài khoản"
2. Bấm vào biểu tượng menu (3 chấm) ở góc trên bên phải
3. Chọn "Tìm kiếm truyện Trung Quốc"
4. Nhập từ khóa tìm kiếm (có thể bằng tiếng Việt hoặc tiếng Trung)
5. Chọn nguồn tìm kiếm (Webnovel, Qidian, JJWXC)
6. Bấm nút "Tìm kiếm"

Ứng dụng sẽ tự động:

- Phát hiện ngôn ngữ bạn nhập
- Nếu là tiếng Việt, sẽ dịch sang tiếng Trung để tìm kiếm
- Hiển thị kết quả tìm kiếm với thông tin cơ bản như tiêu đề, tác giả, thể loại...

## Tải xuống truyện

Để tải xuống truyện và dịch tự động:

1. Từ kết quả tìm kiếm, bấm vào nút "Tải xuống" trên truyện mà bạn muốn đọc
2. Ứng dụng sẽ bắt đầu tải xuống thông tin truyện và các chương
3. Quá trình này có thể mất thời gian tùy thuộc vào độ dài của truyện
4. Sau khi tải xong, truyện sẽ được thêm vào thư viện của bạn

## Đọc truyện đã dịch

Để đọc truyện đã tải xuống:

1. Mở tab "Thư viện"
2. Tìm truyện đã tải xuống
3. Bấm vào truyện để xem danh sách chương
4. Bấm vào chương để đọc

Mỗi chương sẽ hiển thị cả phiên bản tiếng Trung và tiếng Việt. Bạn có thể:

- Chuyển đổi giữa hai phiên bản bằng nút ở góc trên bên phải
- Điều chỉnh kích thước chữ, màu nền... bằng cách bấm vào biểu tượng cài đặt

## Quản lý API key

Để quản lý API keys:

1. Mở tab "Tài khoản"
2. Bấm vào biểu tượng menu (3 chấm) ở góc trên bên phải
3. Chọn "Cài đặt API"
4. Tại đây, bạn có thể cập nhật hoặc xóa API keys

## Xử lý lỗi thường gặp

### Không tìm thấy kết quả

- Hãy thử tìm kiếm bằng từ khóa khác, ngắn gọn hơn
- Thử tìm kiếm từ nguồn khác (Webnovel, Qidian, JJWXC)

### Lỗi khi dịch

- Kiểm tra lại API keys
- Đảm bảo bạn vẫn còn quota dịch thuật (nếu sử dụng tài khoản miễn phí)
- Thử dùng API dịch thuật thay thế

### Không thể tải xuống truyện

- Kiểm tra kết nối internet
- Đảm bảo bạn có đủ bộ nhớ trên thiết bị
- Một số truyện có thể bị giới hạn quyền truy cập, hãy thử truyện khác

---

Cảm ơn bạn đã sử dụng ứng dụng TangThuCac. Nếu có bất kỳ câu hỏi hoặc gặp vấn đề nào, vui lòng liên hệ chúng tôi qua mục "Phản hồi" trong ứng dụng.

# Hướng dẫn import dữ liệu truyện Trung Quốc vào Firebase

Tài liệu này hướng dẫn cách import dữ liệu truyện Trung Quốc từ file `tangthucac-firebase-import.json` vào Firebase Realtime Database.

## 1. Chuẩn bị

### Yêu cầu:

- Đã tạo project Firebase và thiết lập Realtime Database
- Đã đăng nhập vào Firebase Console
- Đã tải file `tangthucac-firebase-import.json`

## 2. Import dữ liệu vào Firebase

### Cách 1: Import qua Firebase Console

1. Truy cập Firebase Console: https://console.firebase.google.com/
2. Chọn project của bạn
3. Trong menu bên trái, chọn "Realtime Database"
4. Nhấp vào "⋮" (ba chấm) ở góc phải và chọn "Import JSON"
5. Chọn file `tangthucac-firebase-import.json` và nhấp "Import"

### Cách 2: Sử dụng Firebase Admin SDK (Node.js)

1. Cài đặt Firebase Admin SDK:

```bash
npm install firebase-admin
```

2. Tạo file `import.js` với nội dung sau:

```javascript
const admin = require("firebase-admin");
const fs = require("fs");

// Khởi tạo ứng dụng với thông tin service account
const serviceAccount = require("./path-to-your-service-account.json");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://your-project-id-default-rtdb.firebaseio.com",
});

// Đọc file JSON
const data = JSON.parse(
  fs.readFileSync("./tangthucac-firebase-import.json", "utf8")
);

// Import dữ liệu vào database
const db = admin.database();
const ref = db.ref("/");

ref
  .set(data)
  .then(() => {
    console.log("Dữ liệu đã được import thành công!");
    process.exit(0);
  })
  .catch((error) => {
    console.error("Lỗi khi import dữ liệu:", error);
    process.exit(1);
  });
```

3. Thay thế `path-to-your-service-account.json` và `your-project-id` với thông tin từ project của bạn
4. Chạy script:

```bash
node import.js
```

## 3. Cấu trúc dữ liệu

File JSON import chứa các node chính sau:

- `Users`: Thông tin người dùng
- `chinese_novels`: Danh sách truyện Trung Quốc
- `chinese_novel_chapters`: Nội dung các chương
- `chinese_genres`: Danh sách thể loại
- `chinese_novels_by_genre`: Phân loại truyện theo thể loại
- `chinese_novels_by_author`: Phân loại truyện theo tác giả
- `featured_chinese_novels`: Danh sách truyện nổi bật
- `ai_chat_histories`: Lịch sử trò chuyện với AI

## 4. Hướng dẫn sử dụng trong ứng dụng

Sau khi import dữ liệu, ứng dụng Android sẽ truy cập dữ liệu thông qua các đường dẫn tương ứng. Cấu trúc đường dẫn được định nghĩa trong class `ChineseNovelStructure.java`.

Ví dụ:

- Lấy danh sách truyện: `chinese_novels`
- Lấy truyện theo ID: `chinese_novels/{novelId}`
- Lấy chương theo ID: `chinese_novel_chapters/{chapterId}`

## 5. Lưu ý quan trọng

- Đảm bảo thiết lập quy tắc bảo mật (Security Rules) phù hợp cho database
- Nên sao lưu dữ liệu hiện có trước khi import dữ liệu mới
- File JSON import đã được tối ưu cho cấu trúc mới, tập trung vào truyện Trung Quốc và AI chat

## 6. Xử lý lỗi

Nếu gặp lỗi khi import, kiểm tra:

- Kích thước file (Firebase có giới hạn import 256MB)
- Định dạng JSON hợp lệ
- Quyền truy cập vào database

Nếu cần hỗ trợ thêm, vui lòng tham khảo tài liệu Firebase: https://firebase.google.com/docs/database

---

© Tầng Thư Các - Ứng dụng đọc truyện Trung Quốc
