# Tầng Thư Các - Ứng dụng đọc truyện Android

## Giới thiệu
Tầng Thư Các là ứng dụng đọc truyện cho phép người dùng:
- Đọc truyện online với nhiều thể loại
- Nghe đọc truyện với công nghệ Text-to-Speech
- Lưu truyện yêu thích vào thư viện cá nhân
- Tìm kiếm và khám phá truyện mới
- Trao đổi với AI trợ giúp

## Cách cài đặt
1. Clone repository này
2. Mở dự án trong Android Studio
3. Thêm file `google-services.json` vào thư mục app
4. Build và chạy ứng dụng

## Công nghệ sử dụng
- Firebase Authentication: Quản lý đăng nhập, đăng ký
- Firebase Realtime Database: Lưu trữ dữ liệu truyện và thông tin người dùng
- Firebase Storage: Lưu trữ hình ảnh
- Retrofit: Gọi API
- Glide: Hiển thị hình ảnh
- Android Text-to-Speech: Đọc truyện
- Material Design Components: Giao diện người dùng
- Gemini AI API: Tính năng trò chuyện AI

## Cấu trúc dự án
- `activity`: Chứa các Activity của ứng dụng
- `adapter`: Các Adapter cho RecyclerView
- `fragment`: Các Fragment cho màn hình chính
- `model`: Các lớp Model dữ liệu
- `API`: Các lớp kết nối API và Firebase

## Yêu cầu hệ thống
- Android 8.0 (API level 26) trở lên
- Kết nối internet

## Thông tin liên hệ
Liên hệ: [example@email.com](mailto:example@email.com)
