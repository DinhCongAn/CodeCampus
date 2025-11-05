/*
================================================================
    KỊCH BẢN CHÈN DỮ LIỆU MẪU CHO CODECAMPUS_DB
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

-- Xóa các bảng nội dung chính (Content data)
DELETE FROM lessons;
DELETE FROM questions;
DELETE FROM price_packages;
DELETE FROM quizzes;
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
    Đặt lại ID về 0 để bắt đầu chèn từ 1
================================================================
*/
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
DBCC CHECKIDENT ('lessons', RESEED, 0);
DBCC CHECKIDENT ('questions', RESEED, 0);
DBCC CHECKIDENT ('price_packages', RESEED, 0);
DBCC CHECKIDENT ('quizzes', RESEED, 0);
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
    PHẦN 3: CHÈN DỮ LIỆU MỚI (THEO THỨ TỰ KHÓA NGOẠI)
================================================================
*/

-- 1. Bảng user_roles (Bảng độc lập)
INSERT INTO user_roles (name, description) VALUES
(N'ADMIN', N'Quản trị viên hệ thống'),
(N'TEACHER', N'Giảng viên, người tạo khóa học'),
(N'STUDENT', N'Học viên');
GO
-- Chèn thêm 7 vai trò ví dụ (nếu cần)
INSERT INTO user_roles (name, description) VALUES
(N'CONTENT_MANAGER', N'Quản lý nội dung blog'),
(N'SALE', N'Nhân viên kinh doanh'),
(N'SUPPORT', N'Nhân viên hỗ trợ'),
(N'MARKETING', N'Nhân viên Marketing'),
(N'GUEST', N'Khách vãng lai'),
(N'VIP_STUDENT', N'Học viên VIP'),
(N'ASSISTANT', N'Trợ giảng');
GO

-- 2. Bảng blog_categories (Bảng độc lập)
INSERT INTO blog_categories (name, is_active) VALUES
(N'Lập trình Web', 1),
(N'Lập trình Di động', 1),
(N'Khoa học Dữ liệu', 1),
(N'Trí tuệ Nhân tạo', 1),
(N'Lập trình Game', 1),
(N'DevOps', 1),
(N'Tin tức Công nghệ', 1),
(N'Học lập trình', 1),
(N'Thủ thuật Lập trình', 1),
(N'Sự kiện', 1);
GO

-- 3. Bảng course_categories (Bảng độc lập)
INSERT INTO course_categories (name, description, is_active) VALUES
(N'Front-end', N'Các khóa học về giao diện người dùng web', 1),
(N'Back-end', N'Các khóa học về logic máy chủ và cơ sở dữ liệu', 1),
(N'Fullstack', N'Các khóa học bao gồm cả Front-end và Back-end', 1),
(N'Di động (Mobile)', N'Phát triển ứng dụng cho iOS và Android', 1),
(N'Data Science', N'Phân tích và trực quan hóa dữ liệu', 1),
(N'AI & Machine Learning', N'Học máy và trí tuệ nhân tạo', 1),
(N'DevOps', N'Triển khai và vận hành hệ thống', 1),
(N'Kiểm thử (Testing)', N'Đảm bảo chất lượng phần mềm', 1),
(N'Lập trình Nhúng', N'Lập trình cho vi điều khiển và IoT', 1),
(N'Ngôn ngữ Lập trình', N'Học sâu về một ngôn ngữ cụ thể (Python, Java, C#)', 1);
GO

-- 4. Bảng lesson_types (Bảng độc lập)
INSERT INTO lesson_types (name, description) VALUES
(N'Video', N'Bài học dạng video bài giảng'),
(N'Reading', N'Bài học dạng văn bản, tài liệu đọc'),
(N'Quiz', N'Bài kiểm tra trắc nghiệm'),
(N'Assignment', N'Bài tập thực hành (nộp code/file)'),
(N'Code', N'Bài thực hành code trực tiếp');
GO
-- Chèn thêm 5 loại (nếu cần)
INSERT INTO lesson_types (name, description) VALUES
(N'Live Stream', N'Buổi học trực tiếp với giảng viên'),
(N'Webinar', N'Hội thảo trực tuyến'),
(N'Project', N'Dự án cuối khóa'),
(N'External Link', N'Liên kết đến tài liệu bên ngoài'),
(N'Survey', N'Khảo sát ý kiến');
GO


-- 5. Bảng test_types (Bảng độc lập)
INSERT INTO test_types (name, description) VALUES
(N'Tự luyện (Practice)', N'Quiz luyện tập không tính điểm'),
(N'Giữa kỳ (Midterm)', N'Bài kiểm tra giữa khóa học'),
(N'Cuối kỳ (Final)', N'Bài thi cuối khóa'),
(N'Đầu vào (Entry)', N'Bài kiểm tra đánh giá đầu vào'),
(N'Trắc nghiệm nhanh (Quick Quiz)', N'Quiz ngắn sau mỗi bài học');
GO
-- Chèn thêm 5 loại (nếu cần)
INSERT INTO test_types (name, description) VALUES
(N'Thi thử', N'Bài thi mô phỏng'),
(N'Đánh giá năng lực', N'Bài kiểm tra tổng hợp'),
(N'Trắc nghiệm hình ảnh', N'Quiz sử dụng hình ảnh'),
(N'Trắc nghiệm code', N'Quiz điền vào chỗ trống (code)'),
(N'Bài tập lớn', N'Bài kiểm tra dạng dự án nhỏ');
GO


-- 6. Bảng question_levels (Bảng độc lập)
INSERT INTO question_levels (name, description) VALUES
(N'Dễ (Easy)', N'Câu hỏi mức độ nhận biết'),
(N'Trung bình (Medium)', N'Câu hỏi mức độ thông hiểu'),
(N'Khó (Hard)', N'Câu hỏi mức độ vận dụng'),
(N'Rất khó (Expert)', N'Câu hỏi mức độ vận dụng cao, sáng tạo');
GO
-- Chèn thêm 6 loại (nếu cần)
INSERT INTO question_levels (name, description) VALUES
(N'Cơ bản (Basic)', N'Câu hỏi lý thuyết cơ bản'),
(N'Nâng cao (Advanced)', N'Câu hỏi lý thuyết nâng cao'),
(N'Thực hành (Practical)', N'Câu hỏi áp dụng thực tế'),
(N'Phân tích (Analysis)', N'Câu hỏi đòi hỏi phân tích'),
(N'Tổng hợp (Synthesis)', N'Câu hỏi tổng hợp nhiều kiến thức'),
(N'Đánh giá (Evaluation)', N'Câu hỏi yêu cầu đánh giá, đưa ra quyết định');
GO

-- 7. Bảng sliders (Bảng độc lập)
INSERT INTO sliders (image_url, link_url, title, description, status, order_number) VALUES
(N'https://loremflickr.com/1200/400/web,development?random=1', N'/courses/1', N'Khóa học Mới: Lập trình ReactJS 2025', N'Học ReactJS từ cơ bản đến nâng cao với dự án thực tế.', N'active', 1),
(N'https://loremflickr.com/1200/400/programming,python?random=2', N'/courses/2', N'Data Science với Python', N'Nắm vững Pandas, NumPy và Matplotlib.', N'active', 2),
(N'https://loremflickr.com/1200/400/mobile,app?random=3', N'/courses/4', N'Flutter cho Người mới bắt đầu', N'Xây dựng ứng dụng iOS và Android từ một codebase.', N'active', 3),
(N'https://loremflickr.com/1200/400/devops,cloud?random=4', N'/courses/7', N'AWS Cloud Foundation', N'Lấy chứng chỉ AWS Cloud Practitioner.', N'active', 4),
(N'https://loremflickr.com/1200/400/web,abstract?random=5', N'/blog/1', N'Top 10 Thư viện JavaScript 2025', N'Đừng bỏ lỡ những thư viện này!', N'active', 5),
(N'https://loremflickr.com/1200/400/coding,office?random=6', N'/home', N'CodeCampus là gì?', N'Nền tảng học lập trình hàng đầu.', N'active', 6),
(N'https://loremflickr.com/1200/400/java,backend?random=7', N'/courses/2', N'Spring Boot 3 và Microservices', N'Xây dựng hệ thống backend mạnh mẽ.', N'active', 7),
(N'https://loremflickr.com/1200/400/ai,robot?random=8', N'/courses/6', N'Giới thiệu về AI', N'Hiểu rõ về Trí tuệ Nhân tạo.', N'inactive', 8),
(N'https://loremflickr.com/1200/400/database,sql?random=9', N'/blog/2', N'SQL vs NoSQL: Khi nào dùng gì?', N'Phân tích ưu nhược điểm.', N'active', 9),
(N'https://loremflickr.com/1200/400/security,cyber?random=10', N'/courses/1', N'Cyber Security cho Developers', N'Bảo mật ứng dụng web của bạn.', N'active', 10);
GO

-- 8. Bảng users (Phụ thuộc: user_roles)
-- Mật khẩu cho tất cả là '123456'
-- Hash (BCrypt): $2a$10$f/3.a16eE.E2j1.Pj.Q63uSgct.e.N8r.HXVgGsxSg5f8.N3f.S.q (Đây là hash ví dụ, hash thật từ code của bạn)
-- Tôi sẽ dùng hash thật cho '123456' để bạn test đăng nhập:
-- $2a$10$v60kNSroGckGc.E.jEeOpeMhN11ZWR.xId1PM.aF3.G.yOKb/1Ew.
DECLARE @passwordHash VARCHAR(255) = '$2a$10$v60kNSroGckGc.E.jEeOpeMhN11ZWR.xId1PM.aF3.G.yOKb/1Ew.';
INSERT INTO users (email, password_hash, full_name, gender, mobile, role_id, avatar, [address], status) VALUES
('admin@codecampus.vn', @passwordHash, N'Admin Quản Trị', N'Nam', '0987654321', 1, N'https://i.pravatar.cc/150?img=1', N'123 Đường Admin, TP. HCM', 'active'),
('teacher1@codecampus.vn', @passwordHash, N'Giảng viên Vũ', N'Nam', '0912345678', 2, N'https://i.pravatar.cc/150?img=2', N'456 Đường GV, Hà Nội', 'active'),
('teacher2@codecampus.vn', @passwordHash, N'Giảng viên Hoa', N'Nữ', '0912345679', 2, N'https://i.pravatar.cc/150?img=3', N'789 Đường GV, Đà Nẵng', 'active'),
('student1@gmail.com', @passwordHash, N'Nguyễn Văn An', N'Nam', '0905111222', 3, N'https://i.pravatar.cc/150?img=4', N'11 Đường Học Viên, Cần Thơ', 'active'),
('student2@gmail.com', @passwordHash, N'Trần Thị Bình', N'Nữ', '0905333444', 3, N'https://i.pravatar.cc/150?img=5', N'22 Đường Học Viên, Hải Phòng', 'active'),
('student3@gmail.com', @passwordHash, N'Lê Văn Cường', N'Nam', '0905555666', 3, N'https://i.pravatar.cc/150?img=6', N'33 Đường Học Viên, TP. HCM', 'pending'),
('contentmanager@codecampus.vn', @passwordHash, N'Quản lý Nội dung', N'Nữ', '0905777888', 4, N'https://i.pravatar.cc/150?img=7', N'44 Đường Nội dung, Hà Nội', 'active'),
('sale@codecampus.vn', @passwordHash, N'Nhân viên Sale', N'Nam', '0905999000', 5, N'https://i.pravatar.cc/150?img=8', N'55 Đường Sale, TP. HCM', 'active'),
('support@codecampus.vn', @passwordHash, N'Nhân viên Hỗ trợ', N'Nữ', '0905123123', 6, N'https://i.pravatar.cc/150?img=9', N'66 Đường Hỗ trợ, Đà Nẵng', 'active'),
('student_vip@gmail.com', @passwordHash, N'Học viên VIP', N'Nam', '0905456456', 9, N'https://i.pravatar.cc/150?img=10', N'77 Đường VIP, TP. HCM', 'active');
GO

-- 9. Bảng courses (Phụ thuộc: course_categories, users)
INSERT INTO courses (name, category_id, description, status, is_featured, owner_id, thumbnail_url) VALUES
(N'HTML CSS Zero to Hero', 1, N'Học HTML CSS từ con số 0, xây dựng website đầu tiên.', N'published', 1, 2, N'https://loremflickr.com/640/480/html,css?random=1'),
(N'JavaScript Nâng cao 2025', 1, N'Nắm vững ES13+, Async/Await, và các khái niệm cốt lõi.', N'published', 1, 2, N'https://loremflickr.com/640/480/javascript?random=2'),
(N'ReactJS & Redux Toolkit', 1, N'Xây dựng ứng dụng web hiện đại với React và Redux.', N'published', 1, 3, N'https://loremflickr.com/640/480/react?random=3'),
(N'Node.js & Express API', 2, N'Xây dựng RESTful API mạnh mẽ với Node.js và Express.', N'published', 1, 2, N'https://loremflickr.com/640/480/nodejs?random=4'),
(N'Spring Boot 3 Microservices', 2, N'Kiến trúc Microservices với Spring Boot, Eureka, và Spring Cloud.', N'published', 1, 3, N'https://loremflickr.com/640/480/java,spring?random=5'),
(N'Lập trình Python từ A-Z', 10, N'Khóa học Python cho người mới bắt đầu, từ cơ bản đến OOP.', N'published', 1, 2, N'https://loremflickr.com/640/480/python?random=6'),
(N'Flutter (iOS & Android)', 4, N'Xây dựng ứng dụng di động cho cả hai hệ điều hành.', N'published', 0, 3, N'https://loremflickr.com/640/480/flutter,mobile?random=7'),
(N'Nhập môn AI', 6, N'Hiểu về Trí tuệ Nhân tạo và các ứng dụng.', N'draft', 0, 2, N'https://loremflickr.com/640/480/ai,robot?random=8'),
(N'Docker & Kubernetes', 7, N'Học DevOps từ cơ bản đến nâng cao.', N'published', 0, 3, N'https://loremflickr.com/640/480/devops,docker?random=9'),
(N'SQL Masterclass', 2, N'Thành thạo SQL Server, PostgreSQL và MySQL.', N'published', 1, 2, N'https://loremflickr.com/640/480/sql,database?random=10');
GO

-- 10. Bảng blogs (Phụ thuộc: blog_categories, users)
INSERT INTO blogs (title, content, blog_category_id, author_id, thumbnail_url, status, published_at) VALUES
(N'10 Lỗi JavaScript người mới thường gặp', N'<h2>Lỗi 1: Sử dụng == thay vì ===</h2><p>Đây là lỗi phổ biến nhất. Luôn dùng === để so sánh cả giá trị và kiểu dữ liệu.</p>', 8, 2, N'https://loremflickr.com/640/480/javascript,error?random=1', N'published', GETDATE()),
(N'So sánh React, Vue, và Angular 2025', N'<h2>React</h2><p>Thư viện linh hoạt, hệ sinh thái lớn.</p><h2>Vue</h2><p>Dễ học, tài liệu tốt.</p>', 1, 3, N'https://loremflickr.com/640/480/react,vue?random=2', N'published', GETDATE()),
(N'Microservices là gì?', N'<p>Kiến trúc Microservices chia nhỏ ứng dụng thành các dịch vụ độc lập...</p>', 7, 2, N'https://loremflickr.com/640/480/microservices?random=3', N'published', GETDATE()),
(N'Top 5 extension VS Code cho lập trình viên', N'<p>1. Prettier - Code formatter</p><p>2. ESLint</p>', 9, 3, N'https://loremflickr.com/640/480/vscode?random=4', N'published', GETDATE()),
(N'Cách Python thống trị mảng Khoa học Dữ liệu', N'<p>Nhờ vào thư viện Pandas, NumPy, và Scikit-learn...</p>', 3, 2, N'https://loremflickr.com/640/480/python,data?random=5', N'published', GETDATE()),
(N'Flutter 3 đã ra mắt!', N'<h2>Những tính năng mới</h2><p>Hỗ trợ macOS và Linux tốt hơn...</p>', 2, 3, N'https://loremflickr.com/640/480/flutter?random=6', N'published', GETDATE()),
(N'Lộ trình học DevOps 2025', N'<h2>Giai đoạn 1: Linux</h2><p>Bạn phải thành thạo Linux...</p>', 6, 2, N'https://loremflickr.com/640/480/devops?random=7', N'published', GETDATE()),
(N'ChatGPT 5 sắp ra mắt?', N'<p>Tin đồn về mô hình AI mới của OpenAI...</p>', 4, 7, N'https://loremflickr.com/640/480/chatgpt,ai?random=8', N'published', GETDATE()),
(N'Học lập trình Game với Unity', N'<p>Ngôn ngữ C# là bắt buộc...</p>', 5, 2, N'https://loremflickr.com/640/480/unity,game?random=9', N'draft', NULL),
(N'Sự kiện CodeCampus Tech Day 2025', N'<p>Diễn ra vào tháng 12 tại TP. HCM...</p>', 10, 7, N'https://loremflickr.com/640/480/event,tech?random=10', N'published', GETDATE());
GO

-- 11. Bảng price_packages (Phụ thuộc: courses)
INSERT INTO price_packages (course_id, name, duration_months, list_price, sale_price, status, sale) VALUES
(1, N'Gói 6 tháng', 6, 1200000, 600000, N'active', 50),
(1, N'Gói trọn đời', 99, 3000000, 1500000, N'active', 50),
(2, N'Gói 6 tháng', 6, 1500000, 750000, N'active', 50),
(2, N'Gói trọn đời', 99, 3500000, 1750000, N'active', 50),
(3, N'Gói 12 tháng', 12, 2500000, 1250000, N'active', 50),
(3, N'Gói trọn đời', 99, 5000000, 2500000, N'active', 50),
(4, N'Gói trọn đời', 99, 4000000, 2000000, N'active', 50),
(5, N'Gói trọn đời', 99, 6000000, 3000000, N'active', 50),
(6, N'Gói 6 tháng', 6, 1000000, 500000, N'active', 50),
(7, N'Gói trọn đời', 99, 4500000, 2250000, N'active', 50);
GO

-- 12. Bảng quizzes (Phụ thuộc: courses, test_types, question_levels)
INSERT INTO quizzes (course_id, test_type_id, name, exam_level_id, duration_minutes, pass_rate_percentage) VALUES
(1, 5, N'Kiểm tra HTML cơ bản', 1, 15, 80.00),
(1, 3, N'Thi cuối khóa HTML/CSS', 2, 60, 75.00),
(2, 5, N'Kiểm tra JavaScript (ES6+)', 2, 20, 80.00),
(2, 3, N'Thi cuối khóa JavaScript', 3, 90, 70.00),
(3, 5, N'Kiểm tra React Hooks', 2, 25, 80.00),
(4, 5, N'Kiểm tra Node.js API', 2, 30, 80.00),
(5, 5, N'Kiểm tra Spring Boot Core', 2, 30, 75.00),
(6, 5, N'Kiểm tra Python Căn bản', 1, 15, 80.00),
(7, 3, N'Thi cuối khóa Flutter', 3, 60, 70.00),
(10, 5, N'Kiểm tra SQL (JOIN)', 2, 20, 80.00);
GO

-- 13. Bảng lessons (Phụ thuộc: courses, lesson_types, quizzes)
INSERT INTO lessons (course_id, lesson_type_id, name, topic, order_number, video_url, html_content, quiz_id, status) VALUES
(1, 1, N'Giới thiệu HTML', N'HTML', 1, N'https://www.youtube.com/watch?v=VIDEO_ID', NULL, NULL, N'active'),
(1, 2, N'Các thẻ HTML cơ bản', N'HTML', 2, NULL, N'<p>Các thẻ: p, h1, div, span...</p>', NULL, N'active'),
(1, 3, N'Bài 1: Kiểm tra HTML', N'HTML', 3, NULL, NULL, 1, N'active'),
(1, 1, N'Giới thiệu CSS', N'CSS', 4, N'https://www.youtube.com/watch?v=VIDEO_ID', NULL, NULL, N'active'),
(1, 2, N'CSS Flexbox', N'CSS', 5, NULL, N'<p>Flexbox giúp căn chỉnh layout...</p>', NULL, N'active'),
(1, 3, N'Bài 2: Thi cuối khóa', N'CSS', 6, NULL, NULL, 2, N'active'),
(2, 1, N'Biến và Kiểu dữ liệu (JS)', N'JavaScript', 1, N'https://www.youtube.com/watch?v=VIDEO_ID', NULL, NULL, N'active'),
(2, 2, N'ES6+ (Arrow Function, Let/Const)', N'JavaScript', 2, NULL, N'<h2>Arrow Function</h2>', NULL, N'active'),
(2, 3, N'Bài 1: Kiểm tra ES6+', N'JavaScript', 3, NULL, NULL, 3, N'active'),
(6, 1, N'Cài đặt Python', N'Python', 1, N'https://www.youtube.com/watch?v=VIDEO_ID', NULL, NULL, N'active');
GO

-- 14. Bảng questions (Phụ thuộc: courses, lessons, question_levels)
INSERT INTO questions (course_id, lesson_id, question_level_id, status, content, explanation) VALUES
(1, 1, 1, N'published', N'HTML là viết tắt của gì?', N'HTML là HyperText Markup Language.'),
(1, 1, 1, N'published', N'Thẻ nào dùng để tạo một đoạn văn bản?', N'Thẻ <p> dùng để tạo đoạn văn bản.'),
(1, 4, 1, N'published', N'CSS là viết tắt của gì?', N'CSS là Cascading Style Sheets.'),
(1, 4, 2, N'published', N'Thuộc tính CSS nào dùng để đổi màu chữ?', N'Thuộc tính "color" dùng để đổi màu chữ.'),
(2, 7, 2, N'published', N'Từ khóa nào trong ES6 dùng để khai báo biến không thể gán lại?', N'"const" dùng để khai báo hằng số.'),
(2, 7, 2, N'published', N'Arrow function có "this" context của riêng nó không?', N'Không, arrow function mượn "this" của scope cha.'),
(6, 10, 1, N'published', N'Python là ngôn ngữ thông dịch hay biên dịch?', N'Python là ngôn ngữ thông dịch.'),
(6, 10, 1, N'published', N'Hàm nào dùng để in ra màn hình trong Python?', N'Hàm print().'),
(10, NULL, 2, N'published', N'Câu lệnh SQL nào dùng để chọn dữ liệu từ bảng?', N'SELECT'),
(10, NULL, 2, N'published', N'Loại JOIN nào trả về tất cả bản ghi từ bảng trái, và các bản ghi khớp từ bảng phải?', N'LEFT JOIN');
GO

-- 15. Bảng answer_options (Phụ thuộc: questions)
-- 4 câu trả lời cho 10 câu hỏi
-- Câu 1 (Đúng: 2)
INSERT INTO answer_options (question_id, content, is_correct) VALUES
(1, N'Hyperlinks and Text Markup Language', 0),
(1, N'HyperText Markup Language', 1),
(1, N'Home Tool Markup Language', 0),
(1, N'HyperText Main Language', 0);
-- Câu 2 (Đúng: 7)
INSERT INTO answer_options (question_id, content, is_correct) VALUES
(2, N'<div>', 0),
(2, N'<span>', 0),
(2, N'<p>', 1),
(2, N'<h1>', 0);
-- Câu 3 (Đúng: 10)
INSERT INTO answer_options (question_id, content, is_correct) VALUES
(3, N'Colorful Style Sheets', 0),
(3, N'Cascading Style Sheets', 1),
(3, N'Creative Style Sheets', 0),
(3, N'Computer Style Sheets', 0);
-- Câu 4 (Đúng: 15)
INSERT INTO answer_options (question_id, content, is_correct) VALUES
(4, N'font-color', 0),
(4, N'text-color', 0),
(4, N'color', 1),
(4, N'background-color', 0);
-- Câu 5 (Đúng: 17)
INSERT INTO answer_options (question_id, content, is_correct) VALUES
(5, N'const', 1),
(5, N'let', 0),
(5, N'var', 0),
(5, N'static', 0);
-- Câu 6 (Đúng: 22)
INSERT INTO answer_options (question_id, content, is_correct) VALUES
(6, N'Có', 0),
(6, N'Không', 1);
-- Câu 7 (Đúng: 25)
INSERT INTO answer_options (question_id, content, is_correct) VALUES
(7, N'Biên dịch', 0),
(7, N'Thông dịch', 1);
-- Câu 8 (Đúng: 28)
INSERT INTO answer_options (question_id, content, is_correct) VALUES
(8, N'console.log()', 0),
(8, N'print()', 1),
(8, N'System.out.println()', 0),
(8, N'echo()', 0);
-- Câu 9 (Đúng: 30)
INSERT INTO answer_options (question_id, content, is_correct) VALUES
(9, N'GET', 0),
(9, N'SELECT', 1),
(9, N'FETCH', 0),
(9, N'FROM', 0);
-- Câu 10 (Đúng: 34)
INSERT INTO answer_options (question_id, content, is_correct) VALUES
(10, N'INNER JOIN', 0),
(10, N'RIGHT JOIN', 0),
(10, N'FULL JOIN', 0),
(10, N'LEFT JOIN', 1);
GO

-- 16. Bảng registrations (Phụ thuộc: users, courses, price_packages)
-- (Giả sử Học viên (ID 4, 5, 10) đăng ký các khóa học)
INSERT INTO registrations (user_id, course_id, package_id, order_code, total_cost, status, valid_from, valid_to) VALUES
(4, 1, 2, N'ORD001', 1500000, N'paid', GETDATE(), DATEADD(month, 999, GETDATE())),
(4, 2, 4, N'ORD002', 1750000, N'paid', GETDATE(), DATEADD(month, 999, GETDATE())),
(5, 1, 1, N'ORD003', 600000, N'paid', GETDATE(), DATEADD(month, 6, GETDATE())),
(5, 3, 6, N'ORD004', 2500000, N'paid', GETDATE(), DATEADD(month, 999, GETDATE())),
(5, 6, 9, N'ORD005', 500000, N'pending', GETDATE(), NULL),
(10, 1, 2, N'ORD006', 1500000, N'paid', GETDATE(), DATEADD(month, 999, GETDATE())),
(10, 2, 4, N'ORD007', 1750000, N'paid', GETDATE(), DATEADD(month, 999, GETDATE())),
(10, 3, 6, N'ORD008', 2500000, N'paid', GETDATE(), DATEADD(month, 999, GETDATE())),
(10, 4, 7, N'ORD009', 2000000, N'paid', GETDATE(), DATEADD(month, 999, GETDATE())),
(10, 5, 8, N'ORD010', 3000000, N'paid', GETDATE(), DATEADD(month, 999, GETDATE()));
GO

-- 17. Bảng feedbacks (Phụ thuộc: courses, users)
INSERT INTO feedbacks (course_id, user_id, rating, comment) VALUES
(1, 4, 5, N'Khóa học HTML/CSS rất hay và chi tiết!'),
(1, 5, 4, N'Giảng viên giải thích dễ hiểu, nhưng cần thêm bài tập.'),
(2, 4, 5, N'Khóa JS Nâng cao thực sự tuyệt vời!'),
(3, 5, 5, N'Học React xong em đã tự tin đi làm. Cảm ơn thầy!'),
(4, 10, 5, N'Khóa Node.js rất chất lượng.'),
(5, 10, 5, N'Spring Boot Microservices là khóa học hay nhất em từng học.'),
(1, 10, 4, N'Nội dung cơ bản tốt.'),
(2, 5, 4, N'Bài giảng JS hơi nhanh ở phần cuối.'),
(6, 4, 5, N'Khóa Python dễ hiểu cho người mới.'),
(10, 10, 5, N'Rất thích khóa SQL Masterclass!');
GO


/*
================================================================
    PHẦN 4: KÍCH HOẠT LẠI KHÓA NGOẠI
================================================================
*/
EXEC sp_msforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all";
GO

PRINT N'=== HOÀN TẤT CHÈN DỮ LIỆU MẪU ===';