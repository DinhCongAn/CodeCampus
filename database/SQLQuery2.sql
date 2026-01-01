/*
================================================================
    KỊCH BẢN CHÈN DỮ LIỆU MẪU FULL (15+ ROWS) CHO CODECAMPUS_DB
    Mật khẩu cho tất cả user: 123456
================================================================
*/
USE codecampus_db;
GO

/*
================================================================
    PHẦN 1: XÓA DỮ LIỆU CŨ (THEO THỨ TỰ KHÓA NGOẠI ĐẢO NGƯỢC)
================================================================
*/

-- Tắt kiểm tra khóa ngoại để xóa nhanh
EXEC sp_msforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all";
GO

-- Xóa các bảng tham chiếu (Transactional data)
DELETE FROM lab_ai_interactions;
DELETE FROM feedback_attachments;
DELETE FROM question_media;
DELETE FROM quiz_attempt_answers;
DELETE FROM quiz_questions;
DELETE FROM question_group;
DELETE FROM notes;
DELETE FROM my_courses;
DELETE FROM registrations;
DELETE FROM feedbacks;
DELETE FROM answer_options;
DELETE FROM quiz_settings;
DELETE FROM verification_tokens;
DELETE FROM quiz_attempts;
DELETE FROM lab_attempts;

-- Xóa các bảng nội dung chính (Content data)
DELETE FROM lessons;
DELETE FROM questions;
DELETE FROM price_packages;
DELETE FROM quizzes;
DELETE FROM labs;
DELETE FROM blogs;
DELETE FROM courses;

-- Xóa bảng người dùng (Core data)
DELETE FROM users;

-- Xóa các bảng danh mục (Independent data)
DELETE FROM user_roles;
DELETE FROM blog_categories;
DELETE FROM course_categories;
DELETE FROM lesson_types;
DELETE FROM test_types;
DELETE FROM question_levels;
DELETE FROM settings;
DELETE FROM sliders;
GO

/*
================================================================
    PHẦN 2: RESET BỘ ĐẾM IDENTITY (DBCC CHECKIDENT)
================================================================
*/
DBCC CHECKIDENT ('lab_ai_interactions', RESEED, 0);
DBCC CHECKIDENT ('feedback_attachments', RESEED, 0);
DBCC CHECKIDENT ('question_media', RESEED, 0);
DBCC CHECKIDENT ('quiz_attempt_answers', RESEED, 0);
DBCC CHECKIDENT ('question_group', RESEED, 0);
DBCC CHECKIDENT ('notes', RESEED, 0);
DBCC CHECKIDENT ('my_courses', RESEED, 0);
DBCC CHECKIDENT ('registrations', RESEED, 0);
DBCC CHECKIDENT ('feedbacks', RESEED, 0);
DBCC CHECKIDENT ('answer_options', RESEED, 0);
DBCC CHECKIDENT ('quiz_settings', RESEED, 0);
DBCC CHECKIDENT ('verification_tokens', RESEED, 0);
DBCC CHECKIDENT ('quiz_attempts', RESEED, 0);
DBCC CHECKIDENT ('lab_attempts', RESEED, 0);
DBCC CHECKIDENT ('lessons', RESEED, 0);
DBCC CHECKIDENT ('questions', RESEED, 0);
DBCC CHECKIDENT ('price_packages', RESEED, 0);
DBCC CHECKIDENT ('quizzes', RESEED, 0);
DBCC CHECKIDENT ('labs', RESEED, 0);
DBCC CHECKIDENT ('blogs', RESEED, 0);
DBCC CHECKIDENT ('courses', RESEED, 0);
DBCC CHECKIDENT ('users', RESEED, 0);
DBCC CHECKIDENT ('user_roles', RESEED, 0);
DBCC CHECKIDENT ('blog_categories', RESEED, 0);
DBCC CHECKIDENT ('course_categories', RESEED, 0);
DBCC CHECKIDENT ('lesson_types', RESEED, 0);
DBCC CHECKIDENT ('test_types', RESEED, 0);
DBCC CHECKIDENT ('question_levels', RESEED, 0);
DBCC CHECKIDENT ('settings', RESEED, 0);
DBCC CHECKIDENT ('sliders', RESEED, 0);
GO

/*
================================================================
    PHẦN 3: CHÈN DỮ LIỆU MỚI (ĐÃ MỞ RỘNG ~15 ROWS)
================================================================
*/

-- 1. Bảng user_roles
INSERT INTO user_roles (name, description) VALUES
(N'ADMIN', N'Quản trị viên hệ thống'),
(N'TEACHER', N'Giảng viên, người tạo khóa học'),
(N'STUDENT', N'Học viên'),
(N'CONTENT_MANAGER', N'Quản lý nội dung blog'),
(N'SALE', N'Nhân viên kinh doanh'),
(N'SUPPORT', N'Nhân viên hỗ trợ'),
(N'MARKETING', N'Nhân viên Marketing'),
(N'GUEST', N'Khách vãng lai'),
(N'VIP_STUDENT', N'Học viên VIP'),
(N'ASSISTANT', N'Trợ giảng');
GO

-- 2. Bảng blog_categories
INSERT INTO blog_categories (name, is_active) VALUES
(N'Lập trình Web', 1), (N'Lập trình Di động', 1), (N'Khoa học Dữ liệu', 1),
(N'Trí tuệ Nhân tạo', 1), (N'Lập trình Game', 1), (N'DevOps', 1),
(N'Tin tức Công nghệ', 1), (N'Học lập trình', 1), (N'Thủ thuật Lập trình', 1), (N'Sự kiện', 1),
(N'Blockchain', 1), (N'IoT', 1), (N'UI/UX Design', 1), (N'Tuyển dụng', 1), (N'Khác', 1);
GO

-- 3. Bảng course_categories
INSERT INTO course_categories (name, description, is_active) VALUES
(N'Front-end', N'Giao diện Web', 1),
(N'Back-end', N'Hệ thống Server', 1),
(N'Fullstack', N'Toàn diện Web', 1),
(N'Di động (Mobile)', N'iOS & Android', 1),
(N'Data Science', N'Dữ liệu', 1),
(N'AI & Machine Learning', N'Trí tuệ nhân tạo', 1),
(N'DevOps', N'Vận hành hệ thống', 1),
(N'Kiểm thử (Testing)', N'Tester & QA', 1),
(N'Lập trình Nhúng', N'IoT & Hardware', 1),
(N'Ngôn ngữ Lập trình', N'C++, Java, Python...', 1),
(N'Blockchain', N'Web3 & Smart Contract', 1),
(N'Game Development', N'Unity & Unreal', 1),
(N'Cyber Security', N'An ninh mạng', 1),
(N'UI/UX Design', N'Thiết kế giao diện', 1),
(N'Kỹ năng mềm', N'Soft skills', 1);
GO

-- 4. Bảng lesson_types
INSERT INTO lesson_types (name, description) VALUES
(N'Video', N'Bài giảng video'),
(N'Reading', N'Tài liệu đọc'),
(N'Quiz', N'Trắc nghiệm'),
(N'Lab', N'Thực hành Coding/AI'),
(N'Project', N'Dự án lớn');
GO

-- 5. Bảng test_types
INSERT INTO test_types (name, description) VALUES
(N'Tự luyện (Practice)', N'Không tính điểm'),
(N'Giữa kỳ (Midterm)', N'Hệ số 1'),
(N'Cuối kỳ (Final)', N'Hệ số 2'),
(N'Đầu vào (Entry)', N'Test trình độ'),
(N'Trắc nghiệm nhanh (Quick)', N'Sau bài học');
GO

-- 6. Bảng question_levels
INSERT INTO question_levels (name, description) VALUES
(N'Dễ (Easy)', N'Nhận biết'),
(N'Trung bình (Medium)', N'Thông hiểu'),
(N'Khó (Hard)', N'Vận dụng'),
(N'Rất khó (Expert)', N'Vận dụng cao');
GO

-- 7. Bảng sliders (Đã mở rộng lên 5 cái)
INSERT INTO sliders (image_url, link_url, title, status, order_number) VALUES
(N'https://loremflickr.com/1200/400/web,code?random=1', N'/courses/1', N'Khóa học Mới: Lập trình ReactJS 2025', N'active', 1),
(N'https://loremflickr.com/1200/400/data,science?random=2', N'/courses/2', N'Data Science với Python', N'active', 2),
(N'https://loremflickr.com/1200/400/mobile,app?random=3', N'/courses/4', N'Flutter cho Người mới bắt đầu', N'active', 3),
(N'https://loremflickr.com/1200/400/cloud,server?random=4', N'/courses/7', N'AWS Cloud Foundation', N'active', 4),
(N'https://loremflickr.com/1200/400/technology?random=5', N'/blog/1', N'Top 10 Thư viện JavaScript 2025', N'active', 5);
GO

-- 8. Bảng users (Mở rộng lên 15 users)
DECLARE @passwordHash VARCHAR(255) = '$2a$10$v60kNSroGckGc.E.jEeOpeMhN11ZWR.xId1PM.aF3.G.yOKb/1Ew.'; -- Pass: 123456
INSERT INTO users (email, password_hash, full_name, gender, mobile, role_id, avatar, [address], status) VALUES
('andche186895@fpt.edu.vn', @passwordHash, N'Admin Quản Trị', N'Nam', '0987654321', 1, N'https://i.pravatar.cc/150?img=1', N'HCM', 'active'),
('teacher1@codecampus.vn', @passwordHash, N'Nguyễn Thầy Vũ', N'Nam', '0912345678', 2, N'https://i.pravatar.cc/150?img=2', N'Hà Nội', 'active'),
('teacher2@codecampus.vn', @passwordHash, N'Trần Cô Hoa', N'Nữ', '0912345679', 2, N'https://i.pravatar.cc/150?img=3', N'Đà Nẵng', 'active'),
('student1@gmail.com', @passwordHash, N'Nguyễn Văn An', N'Nam', '0905111222', 3, N'https://i.pravatar.cc/150?img=4', N'Cần Thơ', 'active'),
('student2@gmail.com', @passwordHash, N'Trần Thị Bình', N'Nữ', '0905333444', 3, N'https://i.pravatar.cc/150?img=5', N'Hải Phòng', 'active'),
('student3@gmail.com', @passwordHash, N'Lê Văn Cường', N'Nam', '0905555666', 3, N'https://i.pravatar.cc/150?img=6', N'HCM', 'active'),
('content@codecampus.vn', @passwordHash, N'Lê Thị Nội Dung', N'Nữ', '0905777888', 4, N'https://i.pravatar.cc/150?img=7', N'Hà Nội', 'active'),
('sale@codecampus.vn', @passwordHash, N'Phạm Văn Sale', N'Nam', '0905999000', 5, N'https://i.pravatar.cc/150?img=8', N'HCM', 'active'),
('support@codecampus.vn', @passwordHash, N'Hoàng Thị Hỗ Trợ', N'Nữ', '0905123123', 6, N'https://i.pravatar.cc/150?img=9', N'Đà Nẵng', 'active'),
('vip@codecampus.vn', @passwordHash, N'Trần Văn VIP', N'Nam', '0905456456', 9, N'https://i.pravatar.cc/150?img=10', N'HCM', 'active'),
-- Thêm 5 user mới cho đủ 15
('student4@gmail.com', @passwordHash, N'Phạm Thị Duyên', N'Nữ', '0905111333', 3, N'https://i.pravatar.cc/150?img=11', N'Vũng Tàu', 'active'),
('student5@gmail.com', @passwordHash, N'Đỗ Văn Hùng', N'Nam', '0905111444', 3, N'https://i.pravatar.cc/150?img=12', N'Nha Trang', 'active'),
('teacher3@codecampus.vn', @passwordHash, N'Lý Thầy Minh', N'Nam', '0912345688', 2, N'https://i.pravatar.cc/150?img=13', N'Huế', 'active'),
('manager@codecampus.vn', @passwordHash, N'Ngô Quản Lý', N'Nữ', '0912345699', 4, N'https://i.pravatar.cc/150?img=14', N'Hà Nội', 'active'),
('student6@gmail.com', @passwordHash, N'Bùi Văn Tí', N'Nam', '0905111555', 3, N'https://i.pravatar.cc/150?img=15', N'Cà Mau', 'active');
GO

-- 9. Bảng courses (Mở rộng lên 15 courses - Tất cả đều PUBLISHED/ACTIVE)
INSERT INTO courses (name, category_id, description, status, is_featured, owner_id, thumbnail_url) VALUES
(N'HTML CSS Zero to Hero', 1, N'Học HTML CSS từ con số 0.', N'published', 1, 2, N'https://loremflickr.com/640/480/html,css?random=1'),
(N'JavaScript Nâng cao 2025', 1, N'Nắm vững ES13+, Async/Await.', N'published', 1, 2, N'https://loremflickr.com/640/480/javascript?random=2'),
(N'ReactJS & Redux Toolkit', 1, N'Xây dựng ứng dụng web hiện đại.', N'published', 1, 3, N'https://loremflickr.com/640/480/react?random=3'),
(N'Node.js & Express API', 2, N'Xây dựng RESTful API mạnh mẽ.', N'published', 1, 2, N'https://loremflickr.com/640/480/nodejs?random=4'),
(N'Spring Boot 3 Microservices', 2, N'Kiến trúc Microservices Java.', N'published', 1, 3, N'https://loremflickr.com/640/480/java,spring?random=5'),
(N'Lập trình Python từ A-Z', 10, N'Python cho người mới bắt đầu.', N'published', 1, 2, N'https://loremflickr.com/640/480/python?random=6'),
(N'Flutter (iOS & Android)', 4, N'Xây dựng ứng dụng di động.', N'published', 0, 3, N'https://loremflickr.com/640/480/flutter,mobile?random=7'),
(N'Nhập môn AI & Machine Learning', 6, N'Hiểu về Trí tuệ Nhân tạo.', N'published', 0, 13, N'https://loremflickr.com/640/480/ai,robot?random=8'),
(N'Docker & Kubernetes DevOps', 7, N'Học DevOps từ cơ bản.', N'published', 0, 3, N'https://loremflickr.com/640/480/devops,docker?random=9'),
(N'SQL Masterclass', 2, N'Thành thạo SQL Server, PostgreSQL.', N'published', 1, 2, N'https://loremflickr.com/640/480/sql,database?random=10'),
-- Thêm 5 khóa học mới
(N'Lập trình C++ Cơ bản', 10, N'Cấu trúc dữ liệu và giải thuật C++.', N'published', 0, 13, N'https://loremflickr.com/640/480/cplusplus?random=11'),
(N'Lập trình C# .NET Core', 2, N'Xây dựng ứng dụng Enterprise.', N'published', 1, 13, N'https://loremflickr.com/640/480/csharp?random=12'),
(N'Golang Backend Master', 2, N'Hiệu năng cao với Go.', N'published', 0, 2, N'https://loremflickr.com/640/480/golang?random=13'),
(N'Cyber Security Fundamentals', 13, N'Bảo mật an toàn thông tin.', N'published', 1, 3, N'https://loremflickr.com/640/480/hacker?random=14'),
(N'Blockchain & Smart Contracts', 11, N'Lập trình Solidity.', N'published', 0, 2, N'https://loremflickr.com/640/480/blockchain?random=15');
GO

-- 10. Bảng blogs (Mở rộng lên 15 blogs)
INSERT INTO blogs (title, content, blog_category_id, author_id, thumbnail_url, status, published_at) VALUES
(N'10 Lỗi JavaScript thường gặp', N'<p>Nội dung bài viết...</p>', 8, 2, N'https://loremflickr.com/640/480/tech?random=1', N'published', GETDATE()),
(N'So sánh React, Vue, Angular', N'<p>Nội dung bài viết...</p>', 1, 3, N'https://loremflickr.com/640/480/tech?random=2', N'published', GETDATE()),
(N'Top 5 extension VS Code', N'<p>Nội dung bài viết...</p>', 9, 3, N'https://loremflickr.com/640/480/tech?random=3', N'published', GETDATE()),
(N'Lộ trình học DevOps 2025', N'<p>Nội dung bài viết...</p>', 6, 13, N'https://loremflickr.com/640/480/tech?random=4', N'published', GETDATE()),
(N'Tại sao nên học Python?', N'<p>Nội dung bài viết...</p>', 3, 2, N'https://loremflickr.com/640/480/tech?random=5', N'published', GETDATE()),
(N'AI sẽ thay thế lập trình viên?', N'<p>Nội dung bài viết...</p>', 4, 2, N'https://loremflickr.com/640/480/tech?random=6', N'published', GETDATE()),
(N'Review sách Clean Code', N'<p>Nội dung bài viết...</p>', 8, 3, N'https://loremflickr.com/640/480/tech?random=7', N'published', GETDATE()),
(N'Hướng dẫn học tiếng Anh IT', N'<p>Nội dung bài viết...</p>', 8, 7, N'https://loremflickr.com/640/480/tech?random=8', N'published', GETDATE()),
(N'Sự kiện Tech Day 2025', N'<p>Nội dung bài viết...</p>', 10, 7, N'https://loremflickr.com/640/480/tech?random=9', N'published', GETDATE()),
(N'Tuyển dụng Developer lương cao', N'<p>Nội dung bài viết...</p>', 14, 8, N'https://loremflickr.com/640/480/tech?random=10', N'published', GETDATE()),
(N'Blockchain là gì?', N'<p>Nội dung bài viết...</p>', 11, 2, N'https://loremflickr.com/640/480/tech?random=11', N'published', GETDATE()),
(N'IoT trong nông nghiệp', N'<p>Nội dung bài viết...</p>', 12, 13, N'https://loremflickr.com/640/480/tech?random=12', N'published', GETDATE()),
(N'Xu hướng UI Design 2025', N'<p>Nội dung bài viết...</p>', 13, 7, N'https://loremflickr.com/640/480/tech?random=13', N'published', GETDATE()),
(N'Cách viết CV chuẩn IT', N'<p>Nội dung bài viết...</p>', 8, 7, N'https://loremflickr.com/640/480/tech?random=14', N'published', GETDATE()),
(N'Học lập trình bắt đầu từ đâu?', N'<p>Nội dung bài viết...</p>', 8, 2, N'https://loremflickr.com/640/480/tech?random=15', N'published', GETDATE());
GO

-- 11. Bảng price_packages (15 gói cho 15 khóa)
INSERT INTO price_packages (course_id, name, duration_months, list_price, sale_price, status, sale) VALUES
(1, N'Gói trọn đời HTML', 99, 10000, 5000, N'active', 50),
(2, N'Gói trọn đời JS', 99, 12000, 6000, N'active', 50),
(3, N'Gói trọn đời React', 99, 14000, 7000, N'active', 50),
(4, N'Gói trọn đời Node', 99, 16000, 8000, N'active', 50),
(5, N'Gói trọn đời Spring', 99, 20000, 10000, N'active', 50),
(6, N'Gói trọn đời Python', 99, 10000, 5000, N'active', 50),
(7, N'Gói trọn đời Flutter', 99, 18000, 9000, N'active', 50),
(8, N'Gói trọn đời AI', 99, 22000, 11000, N'active', 50),
(9, N'Gói trọn đời DevOps', 99, 20000, 10000, N'active', 50),
(10, N'Gói trọn đời SQL', 99, 12000, 6000, N'active', 50),
(11, N'Gói trọn đời C++', 99, 10000, 5000, N'active', 50),
(12, N'Gói trọn đời C#', 99, 15000, 7500, N'active', 50),
(13, N'Gói trọn đời Go', 99, 16000, 8000, N'active', 50),
(14, N'Gói trọn đời Security', 99, 25000, 12500, N'active', 50),
(15, N'Gói trọn đời Blockchain', 99, 30000, 15000, N'active', 50);
GO

-- 12. Bảng labs (Mở rộng lên 15 labs)
INSERT INTO labs (name, description, lab_type, evaluation_criteria) VALUES
(N'Lab 1: Hello JS', N'Hàm sum(a,b)', N'coding', N'Check sum function'),
(N'Lab 2: SQL Select', N'Select * from users', N'sql', N'Check SELECT'),
(N'Lab 3: React Component', N'Tạo component Button', N'coding', N'Check component render'),
(N'Lab 4: Java Class', N'Tạo class Student', N'coding', N'Check class structure'),
(N'Lab 5: Python Loop', N'Vòng lặp in 1-10', N'coding', N'Check output 1..10'),
(N'Lab 6: HTML Form', N'Tạo form login', N'coding', N'Check input tags'),
(N'Lab 7: CSS Flexbox', N'Căn giữa div', N'coding', N'Check justify-content'),
(N'Lab 8: C++ Pointer', N'Thao tác con trỏ', N'coding', N'Check memory address'),
(N'Lab 9: Go Routine', N'Tạo go routine cơ bản', N'coding', N'Check concurrency'),
(N'Lab 10: Dockerfile', N'Viết Dockerfile cho Node', N'coding', N'Check FROM node'),
(N'Lab 11: SQL Join', N'Join Users và Orders', N'sql', N'Check INNER JOIN'),
(N'Lab 12: C# LINQ', N'Lọc danh sách số chẵn', N'coding', N'Check .Where()'),
(N'Lab 13: Blockchain Hash', N'Tạo hàm hash SHA256', N'coding', N'Check hash result'),
(N'Lab 14: Security XSS', N'Sửa lỗi XSS trong code', N'coding', N'Check input sanitization'),
(N'Lab 15: Flutter Widget', N'Tạo Container màu đỏ', N'coding', N'Check Container color');
GO

-- 13. Bảng quizzes (Mở rộng lên 15 quizzes - mỗi course 1 quiz)
INSERT INTO quizzes (course_id, test_type_id, name, exam_level_id, duration_minutes, pass_rate_percentage) VALUES
(1, 5, N'Quiz 1: HTML Basics', 1, 15, 80.00),
(1, 3, N'Quiz 2: HTML Final', 2, 60, 75.00),
(2, 5, N'Quiz 3: JS ES6', 2, 20, 80.00),
(2, 3, N'Quiz 4: JS Final', 3, 90, 70.00),
(3, 5, N'Quiz 5: React Hooks', 2, 25, 80.00),
(4, 5, N'Quiz 6: Node.js Basics', 2, 30, 70.00),
(5, 5, N'Quiz 7: Spring Boot Config', 3, 45, 70.00),
(6, 5, N'Quiz 8: Python Types', 1, 15, 80.00),
(7, 5, N'Quiz 9: Flutter Widgets', 2, 30, 75.00),
(8, 5, N'Quiz 10: AI Concepts', 3, 45, 60.00),
(9, 5, N'Quiz 11: Docker Basics', 2, 30, 70.00),
(10, 5, N'Quiz 12: SQL Syntax', 2, 45, 80.00),
(11, 5, N'Quiz 13: C++ Memory', 3, 45, 65.00),
(12, 5, N'Quiz 14: C# OOP', 2, 40, 70.00),
(13, 5, N'Quiz 15: Go Syntax', 2, 30, 75.00);
GO

-- 14. Bảng questions (Tạo đủ câu hỏi cho các quiz mới)
-- Đã có 8 câu. Thêm từ câu 9 -> 22 (để map vào các quiz mới)
INSERT INTO questions (course_id, lesson_id, question_level_id, status, content, explanation) VALUES
-- Existing 1-8
(1, NULL, 1, N'published', N'HTML là viết tắt của gì?', N'HTML là HyperText Markup Language.'), -- 1
(1, NULL, 1, N'published', N'Thẻ nào dùng để tạo một đoạn văn bản?', N'Thẻ <p>.'), -- 2
(1, NULL, 1, N'published', N'CSS là viết tắt của gì?', N'Cascading Style Sheets.'), -- 3
(1, NULL, 2, N'published', N'Thuộc tính CSS đổi màu chữ?', N'color.'), -- 4
(2, NULL, 2, N'published', N'Từ khóa khai báo hằng số JS?', N'const.'), -- 5
(2, NULL, 2, N'published', N'Arrow function có this riêng?', N'Không.'), -- 6
(3, NULL, 3, N'published', N'Đâu là React Hook?', N'useState.'), -- 7
(6, NULL, 1, N'published', N'Hàm in ra màn hình Python?', N'print().'), -- 8
-- New Questions
(4, NULL, 2, N'published', N'Node.js chạy trên Engine nào?', N'V8 Engine.'), -- 9
(5, NULL, 3, N'published', N'Annotation đánh dấu Service trong Spring?', N'@Service.'), -- 10
(7, NULL, 2, N'published', N'Ngôn ngữ dùng cho Flutter?', N'Dart.'), -- 11
(8, NULL, 3, N'published', N'AI là viết tắt của?', N'Artificial Intelligence.'), -- 12
(9, NULL, 2, N'published', N'Lệnh tạo container từ image?', N'docker run.'), -- 13
(10, NULL, 2, N'published', N'Lệnh lấy dữ liệu trong SQL?', N'SELECT.'), -- 14
(11, NULL, 3, N'published', N'Toán tử lấy địa chỉ trong C++?', N'&.'), -- 15
(12, NULL, 2, N'published', N'Class cha của mọi class trong .NET?', N'Object.'), -- 16
(13, NULL, 2, N'published', N'Go có hỗ trợ class không?', N'Không, dùng Struct.'), -- 17
(14, NULL, 3, N'published', N'XSS là tấn công vào đâu?', N'Client-side script.'), -- 18
(15, NULL, 3, N'published', N'Nền tảng Smart Contract phổ biến?', N'Ethereum.'), -- 19
(1, NULL, 1, N'published', N'Thẻ tạo link?', N'<a>.'), -- 20
(2, NULL, 1, N'published', N'DOM là gì?', N'Document Object Model.'), -- 21
(3, NULL, 2, N'published', N'JSX là gì?', N'Cú pháp mở rộng JS.'); -- 22
GO

-- 15. Bảng answer_options (Đáp án cho câu 9 -> 22)
-- Câu 9 (Node)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (9, N'V8', 1, 1), (9, N'SpiderMonkey', 0, 2);
-- Câu 10 (Spring)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (10, N'@Component', 0, 1), (10, N'@Service', 1, 2);
-- Câu 11 (Flutter)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (11, N'Java', 0, 1), (11, N'Dart', 1, 2);
-- Câu 12 (AI)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (12, N'Auto Intel', 0, 1), (12, N'Artificial Intelligence', 1, 2);
-- Câu 13 (Docker)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (13, N'docker start', 0, 1), (13, N'docker run', 1, 2);
-- Câu 14 (SQL)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (14, N'GET', 0, 1), (14, N'SELECT', 1, 2);
-- Câu 15 (C++)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (15, N'*', 0, 1), (15, N'&', 1, 2);
-- Câu 16 (C#)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (16, N'System', 0, 1), (16, N'Object', 1, 2);
-- Câu 17 (Go)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (17, N'Có', 0, 1), (17, N'Không', 1, 2);
-- Câu 18 (Security)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (18, N'Server', 0, 1), (18, N'Browser', 1, 2);
-- Câu 19 (Blockchain)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (19, N'Bitcoin', 0, 1), (19, N'Ethereum', 1, 2);
-- Câu 20 (Link)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (20, N'<a>', 1, 1), (20, N'<link>', 0, 2);
-- Câu 21 (DOM)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (21, N'Data Object', 0, 1), (21, N'Document Object Model', 1, 2);
-- Câu 22 (JSX)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES (22, N'Java XML', 0, 1), (22, N'JS XML', 1, 2);
GO

-- 16. Bảng lessons (Mỗi Course có ít nhất 1 lesson, có link quiz/lab)
INSERT INTO lessons (course_id, lesson_type_id, name, topic, order_number, video_url, html_content, quiz_id, lab_id, status) VALUES
-- Course 1
(1, 1, N'Giới thiệu HTML', N'Chương 1', 1, N'https://youtube.com/1', NULL, NULL, NULL, N'active'),
(1, 2, N'Các thẻ cơ bản', N'Chương 1', 2, NULL, N'<p>Content</p>', NULL, 6, N'active'),
(1, 3, N'Quiz HTML', N'Chương 1', 3, NULL, NULL, 1, NULL, N'active'),
-- Course 2
(2, 1, N'JS Variable', N'Biến', 1, N'https://youtube.com/2', NULL, NULL, NULL, N'active'),
(2, 4, N'Lab thực hành JS', N'Biến', 2, NULL, NULL, NULL, 1, N'active'),
(2, 3, N'Quiz JS', N'Biến', 3, NULL, NULL, 3, NULL, N'active'),
-- Course 3 (React)
(3, 1, N'React Intro', N'Mở đầu', 1, N'https://youtube.com/3', NULL, NULL, NULL, N'active'),
(3, 4, N'Lab React', N'Lab', 2, NULL, NULL, NULL, 3, N'active'),
-- Course 4 (Node)
(4, 1, N'Node Intro', N'Node', 1, N'https://youtube.com/4', NULL, 6, NULL, N'active'), -- Link Quiz 6
-- Course 5 (Spring)
(5, 1, N'Spring Intro', N'Java', 1, N'https://youtube.com/5', NULL, 7, NULL, N'active'), -- Link Quiz 7
-- Course 6 (Python)
(6, 1, N'Python Intro', N'Py', 1, N'https://youtube.com/6', NULL, 8, 5, N'active'),
-- Course 7 (Flutter)
(7, 1, N'Dart Basics', N'Dart', 1, NULL, N'<p>Dart</p>', 9, 15, N'active'),
-- Course 8 (AI)
(8, 2, N'AI Concepts', N'Theory', 1, NULL, N'<p>AI</p>', 10, NULL, N'active'),
-- Course 9 (DevOps)
(9, 1, N'Docker Install', N'Docker', 1, NULL, NULL, 11, 10, N'active'),
-- Course 10 (SQL)
(10, 1, N'SQL Select', N'Query', 1, NULL, NULL, 12, 2, N'active'),
-- Course 11 (C++)
(11, 1, N'Pointer', N'Memory', 1, NULL, NULL, 13, 8, N'active'),
-- Course 12 (C#)
(12, 1, N'C# Class', N'OOP', 1, NULL, NULL, 14, 12, N'active'),
-- Course 13 (Go)
(13, 1, N'Go Routine', N'Conc', 1, NULL, NULL, 15, 9, N'active'),
-- Course 14 (Security)
(14, 2, N'OWASP Top 10', N'Sec', 1, NULL, N'<p>XSS</p>', NULL, 14, N'active'),
-- Course 15 (Blockchain)
(15, 2, N'Smart Contract', N'Web3', 1, NULL, N'<p>Solidity</p>', NULL, 13, N'active');
GO

-- 17. Bảng quiz_questions (Liên kết câu hỏi mới vào Quiz mới)
INSERT INTO quiz_questions (quiz_id, question_id) VALUES
(1, 1), (1, 2), (1, 20), -- Quiz 1 (HTML)
(2, 3), (2, 4), -- Quiz 2 (HTML Final)
(3, 5), (3, 6), -- Quiz 3 (JS)
(4, 21), -- Quiz 4 (JS Final)
(5, 7), (5, 22), -- Quiz 5 (React)
(6, 9), -- Quiz 6 (Node)
(7, 10), -- Quiz 7 (Spring)
(8, 8), -- Quiz 8 (Python)
(9, 11), -- Quiz 9 (Flutter)
(10, 12), -- Quiz 10 (AI)
(11, 13), -- Quiz 11 (Docker)
(12, 14), -- Quiz 12 (SQL)
(13, 15), -- Quiz 13 (C++)
(14, 16), -- Quiz 14 (C#)
(15, 17); -- Quiz 15 (Go)
GO

-- 18. Bảng registrations (Chỉ chèn mẫu vài dòng cần thiết như yêu cầu của bạn, không tạo rác)
INSERT INTO registrations (user_id, course_id, package_id, order_code, total_cost, status, valid_from, valid_to) VALUES
(4, 1, 1, N'ORD001', 5000, N'COMPLETED', GETDATE(), DATEADD(year, 10, GETDATE())),
(4, 2, 2, N'ORD002', 6000, N'COMPLETED', GETDATE(), DATEADD(year, 10, GETDATE())),
(5, 3, 3, N'ORD003', 7000, N'COMPLETED', GETDATE(), DATEADD(year, 10, GETDATE())),
(5, 15, 15, N'ORD004', 15000, N'PENDING', GETDATE(), NULL);
GO

-- 19. Bảng feedbacks
INSERT INTO feedbacks (course_id, user_id, rating, comment) VALUES
(1, 4, 5, N'Tuyệt vời!'),
(2, 5, 4, N'Hơi khó hiểu đoạn Async'),
(3, 4, 5, N'React hay quá');
GO

/*
================================================================
    PHẦN 4: KÍCH HOẠT LẠI KHÓA NGOẠI
================================================================
*/
EXEC sp_msforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all";
GO

PRINT N'=== HOÀN TẤT CHÈN DỮ LIỆU FULL 15+ ROWS ===';