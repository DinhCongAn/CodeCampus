-- Đảm bảo sử dụng đúng database
USE codecampus_db;
GO

-- 1. XÓA DỮ LIỆU CŨ ĐỂ TRÁNH TRÙNG LẶP (Chạy từ bảng phụ thuộc nhiều nhất)
DELETE FROM feedback_attachments;
DELETE FROM quiz_attempt_answers;
DELETE FROM quiz_attempts;
DELETE FROM question_group;
DELETE FROM quiz_settings;
DELETE FROM quiz_questions;
DELETE FROM question_media;
DELETE FROM answer_options;
DELETE FROM questions;
DELETE FROM notes;
DELETE FROM my_courses;
DELETE FROM registrations;
DELETE FROM price_packages;
DELETE FROM feedbacks;
DELETE FROM lessons;
DELETE FROM quizzes;
DELETE FROM blogs;
DELETE FROM courses;
DELETE FROM sliders;
DELETE FROM verification_tokens;
DELETE FROM users;
DELETE FROM user_roles;
DELETE FROM course_categories;
DELETE FROM blog_categories;
DELETE FROM lesson_types;
DELETE FROM test_types;
DELETE FROM question_levels;
DELETE FROM settings;
GO

-- 2. THÊM DỮ LIỆU MẪU CHO CÁC BẢNG

-- Các bảng gốc (không có hoặc ít phụ thuộc)
INSERT INTO user_roles (name, description) VALUES
(N'ADMIN', N'Quản trị viên, có toàn quyền hệ thống'),
(N'INSTRUCTOR', N'Giảng viên, người tạo và quản lý khóa học'),
(N'STUDENT', N'Học viên, người tham gia các khóa học');
GO

INSERT INTO course_categories (name, description, is_active) VALUES
(N'Lập trình Web', N'Các khóa học về lập trình front-end và back-end cho website.', 1),
(N'Lập trình Di động', N'Các khóa học về lập trình ứng dụng cho iOS và Android.', 1),
(N'Khoa học Dữ liệu', N'Các khóa học về Phân tích dữ liệu, Học máy và AI.', 1);
GO

INSERT INTO blog_categories (name, is_active) VALUES (N'Tin tức Công nghệ', 1), (N'Hướng dẫn Lập trình', 1);
GO
INSERT INTO lesson_types (name, description) VALUES ('VIDEO', N'Bài học dạng video'), ('TEXT', N'Bài học dạng bài viết');
GO
INSERT INTO test_types (name, description) VALUES ('QUIZ', N'Kiểm tra nhanh'), ('FINAL_EXAM', N'Kiểm tra cuối khóa');
GO
INSERT INTO question_levels (name, description) VALUES ('EASY', N'Mức độ dễ'), ('MEDIUM', N'Mức độ trung bình');
GO

INSERT INTO settings (type, value, setting_key, setting_value, description) VALUES
(N'General', N'CodeCampus', N'SITE_NAME', N'CodeCampus - Học để đi làm', N'Tên của trang web'),
(N'Contact', N'contact@codecampus.vn', N'SITE_EMAIL', N'contact@codecampus.vn', N'Email liên hệ chính');
GO

INSERT INTO sliders (image_url, link_url, title, status) VALUES
('https://i.imgur.com/example1.png', '/courses', N'Khóa học Mới Nhất', 'active'),
('https://i.imgur.com/example2.png', '/promotion', N'Ưu đãi Tháng 10', 'active');
GO

-- Bảng users
-- Mật khẩu cho tất cả: 'password123' (đã được mã hóa bằng BCrypt)
INSERT INTO users (email, password_hash, full_name, role_id, status) VALUES
('admin@codecampus.vn', '$2a$10$3gA5.l5b.n3gB8fH.3.e.O0oQ2oF8c3iB4aG6jH9kF1lJ0oR4l7e', N'Admin Quản Trị', 1, 'active'),
('instructor@codecampus.vn', '$2a$10$3gA5.l5b.n3gB8fH.3.e.O0oQ2oF8c3iB4aG6jH9kF1lJ0oR4l7e', N'Giảng Viên A', 2, 'active'),
('student1@codecampus.vn', '$2a$10$3gA5.l5b.n3gB8fH.3.e.O0oQ2oF8c3iB4aG6jH9kF1lJ0oR4l7e', N'Học Viên Nguyễn Văn B', 3, 'active'),
('student2@codecampus.vn', '$2a$10$3gA5.l5b.n3gB8fH.3.e.O0oQ2oF8c3iB4aG6jH9kF1lJ0oR4l7e', N'Học Viên Trần Thị C', 3, 'active');
GO

-- Bảng verification_tokens (tạo token mẫu cho student2)
INSERT INTO verification_tokens (token, user_id, expiry_date) VALUES ('abc-123-xyz-789', 4, DATEADD(day, 1, GETDATE()));
GO

-- Bảng courses
INSERT INTO courses (name, category_id, description, status, is_featured, owner_id, thumbnail_url) VALUES
(N'Spring Boot Toàn tập - Từ Zero đến Hero', 1, N'Khóa học nền tảng về Spring Boot.', 'published', 1, 2, 'https://i.imgur.com/course1.png'),
(N'ReactJS cho người mới bắt đầu', 1, N'Học cách xây dựng ứng dụng front-end hiện đại.', 'published', 1, 2, 'https://i.imgur.com/course2.png');
GO

-- Bảng blogs
INSERT INTO blogs (title, content, blog_category_id, author_id, status, published_at) VALUES
(N'Top 5 Framework JavaScript đáng học 2025', N'Nội dung chi tiết...', 2, 2, 'published', GETDATE());
GO

-- Bảng price_packages cho khóa học Spring Boot (course_id = 1)
INSERT INTO price_packages (course_id, name, duration_months, list_price, sale_price) VALUES
(1, N'Gói 6 tháng', 6, 1200000, 599000);
GO

-- Bảng quizzes cho khóa học Spring Boot (course_id = 1)
INSERT INTO quizzes (course_id, test_type_id, name, exam_level_id, duration_minutes, pass_rate_percentage) VALUES
(1, 1, N'Kiểm tra kiến thức Chương 1', 1, 15, 80.00);
GO

-- Bảng quiz_settings (cài đặt nâng cao cho quiz_id = 1)
INSERT INTO quiz_settings (quiz_id, total_questions, question_type) VALUES
(1, 2, 'Multiple Choice');
GO

-- Bảng question_group (nhóm câu hỏi cho quiz_settings_id = 1)
INSERT INTO question_group(name, questions_number, quiz_setting_id) VALUES
(N'Nhóm câu hỏi cơ bản', 2, 1);
GO

-- Bảng lessons cho khóa học Spring Boot (course_id = 1)
INSERT INTO lessons (course_id, lesson_type_id, name, topic, order_number, video_url, quiz_id) VALUES
(1, 1, N'Bài 1: Giới thiệu Spring Framework', N'Chương 1: Nhập môn', 1, 'https://youtube.com/example/spring1', NULL),
(1, 1, N'Bài 2: Cài đặt môi trường', N'Chương 1: Nhập môn', 2, 'https://youtube.com/example/spring2', 1),
(1, 2, N'Bài 3: Inversion of Control (IoC)', N'Chương 2: Kiến trúc Core', 3, NULL, NULL);
GO

-- Bảng questions cho khóa học Spring Boot (course_id = 1)
INSERT INTO questions (course_id, lesson_id, question_level_id, status, content) VALUES
(1, 2, 1, 'published', N'Spring Boot là framework dựa trên ngôn ngữ nào?'),
(1, 2, 2, 'published', N'Annotation nào dùng để đánh dấu một REST Controller?');
GO

-- Bảng question_media (thêm ảnh cho question_id = 2)
INSERT INTO question_media (question_id, media_url, media_type) VALUES
(2, 'https://i.imgur.com/question_media.png', 'image');
GO

-- Bảng quiz_questions (nối quiz_id=1 với question_id=1,2)
INSERT INTO quiz_questions (quiz_id, question_id) VALUES
(1, 1),
(1, 2);
GO

-- Bảng answer_options
INSERT INTO answer_options (question_id, content, is_correct) VALUES
(1, N'Python', 0), (1, N'Java', 1), (1, N'C#', 0),
(2, N'@Controller', 0), (2, N'@Service', 0), (2, N'@RestController', 1);
GO

-- Bảng registrations (Học viên 3 đăng ký gói 1 của khóa 1)
INSERT INTO registrations (user_id, course_id, package_id, order_code, total_cost, status, valid_from, valid_to) VALUES
(3, 1, 1, 'ORDER001', 599000, 'completed', GETDATE(), DATEADD(month, 6, GETDATE()));
GO

-- Bảng my_courses (Tiến độ của học viên 3)
INSERT INTO my_courses (user_id, course_id, progress_percent, last_lesson_id, status) VALUES
(3, 1, 33.33, 1, 'in_progress');
GO

-- Bảng quiz_attempts (Học viên 3 làm bài quiz 1)
INSERT INTO quiz_attempts(user_id, quiz_id, end_time, score, status, result) VALUES
(3, 1, GETDATE(), 50.00, 'completed', 'failed');
GO

-- Bảng quiz_attempt_answers (Chi tiết bài làm của học viên 3)
INSERT INTO quiz_attempt_answers (attempt_id, question_id, selected_answer_option_id, is_correct) VALUES
(1, 1, 2, 1), -- Câu 1 trả lời đúng (selected_answer_option_id = 2)
(1, 2, 4, 0); -- Câu 2 trả lời sai (selected_answer_option_id = 4)
GO

-- Bảng feedbacks (Học viên 3 review khóa 1)
INSERT INTO feedbacks (course_id, user_id, rating, comment) VALUES
(1, 3, 5, N'Khóa học rất hay và chi tiết.');
GO

-- Bảng feedback_attachments (File đính kèm cho feedback_id = 1)
INSERT INTO feedback_attachments (feedback_id, file_name, file_url, file_type) VALUES
(1, 'screenshot.png', 'https://i.imgur.com/feedback.png', 'image');
GO

-- Bảng notes (Học viên 3 ghi chú ở bài học 3)
INSERT INTO notes (user_id, lesson_id, note) VALUES
(3, 3, N'IoC là một nguyên lý quan trọng, cần xem lại kỹ.');
GO

PRINT 'Tạo dữ liệu mẫu hoàn chỉnh thành công!';