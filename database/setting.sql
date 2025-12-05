-- Đảm bảo bảng đã được tạo theo script của bạn trước đó
-- Xóa dữ liệu cũ (nếu muốn làm sạch)
TRUNCATE TABLE settings;

-- Thêm dữ liệu mẫu
INSERT INTO settings (type, value, order_num, status, setting_key, setting_value, description) 
VALUES 
-- 1. Cấu hình Chung (General)
('GENERAL', 'CodeCampus', 1, 'active', 'site_name', 'CodeCampus E-Learning', N'Tên hiển thị của website'),
('GENERAL', 'Support Email', 2, 'active', 'site_email', 'contact@codecampus.vn', N'Email liên hệ hiển thị ở footer'),
('GENERAL', 'Hotline', 3, 'active', 'site_hotline', '1900 8888', N'Số điện thoại đường dây nóng'),
('GENERAL', 'VND', 4, 'active', 'currency_unit', '₫', N'Đơn vị tiền tệ hiển thị'),

-- 2. Cấu hình Hệ thống (System) - Phân trang
('SYSTEM', '10', 1, 'active', 'page_size_course', '10', N'Số lượng khóa học hiển thị trên 1 trang'),
('SYSTEM', '5', 2, 'active', 'page_size_blog', '5', N'Số lượng bài viết hiển thị trên 1 trang'),
('SYSTEM', 'Session', 3, 'active', 'session_timeout', '1800', N'Thời gian hết hạn phiên đăng nhập (giây)'),

-- 3. Cấu hình Upload
('UPLOAD', 'Max Size', 1, 'active', 'max_file_size', '10485760', N'Dung lượng file tối đa (bytes) - 10MB'),
('UPLOAD', 'Allowed Types', 2, 'active', 'allowed_file_extensions', 'jpg,png,jpeg,pdf,docx', N'Các đuôi file cho phép tải lên'),

-- 4. Cấu hình Vai trò & Người dùng
('USER', 'Default Role', 1, 'active', 'default_role', 'ROLE_STUDENT', N'Quyền mặc định khi user mới đăng ký'),
('USER', 'Verify Email', 2, 'inactive', 'require_email_verification', 'false', N'Yêu cầu xác thực email mới cho đăng nhập (true/false)'),

-- 5. Cấu hình Mạng xã hội (Social)
('SOCIAL', 'Facebook', 1, 'active', 'social_facebook', 'https://facebook.com/codecampus', N'Link Fanpage Facebook'),
('SOCIAL', 'Youtube', 2, 'active', 'social_youtube', 'https://youtube.com/c/codecampus', N'Link kênh Youtube');

GO