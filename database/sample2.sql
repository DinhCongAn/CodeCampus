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
DELETE FROM lab_attempts; -- BỔ SUNG MỚI (Lab)

-- Xóa các bảng nội dung chính (Content data)
DELETE FROM lessons;
DELETE FROM questions;
DELETE FROM price_packages;
DELETE FROM quizzes;
DELETE FROM labs; -- BỔ SUNG MỚI (Lab)
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
DBCC CHECKIDENT ('lab_attempts', RESEED, 0); -- BỔ SUNG MỚI
DBCC CHECKIDENT ('lessons', RESEED, 0);
DBCC CHECKIDENT ('questions', RESEED, 0);
DBCC CHECKIDENT ('price_packages', RESEED, 0);
DBCC CHECKIDENT ('quizzes', RESEED, 0);
DBCC CHECKIDENT ('labs', RESEED, 0); -- BỔ SUNG MỚI
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
    PHẦN 3: CHÈN DỮ LIỆU MỚI
================================================================
*/

-- 1. Bảng user_roles
INSERT INTO user_roles (name, description) VALUES
(N'ADMIN', N'Quản trị viên hệ thống'),
(N'TEACHER', N'Giảng viên, người tạo khóa học'),
(N'STUDENT', N'Học viên');
GO
-- (Chèn thêm 7 vai trò khác)
INSERT INTO user_roles (name, description) VALUES
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
(N'Tin tức Công nghệ', 1), (N'Học lập trình', 1), (N'Thủ thuật Lập trình', 1), (N'Sự kiện', 1);
GO

-- 3. Bảng course_categories
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

-- 4. Bảng lesson_types
INSERT INTO lesson_types (name, description) VALUES
(N'Video', N'Bài học dạng video bài giảng'),
(N'Reading', N'Bài học dạng văn bản, tài liệu đọc'),
(N'Quiz', N'Bài kiểm tra trắc nghiệm'),
(N'Lab', N'Bài thực hành (code/lab) có AI chấm'), -- BỔ SUNG MỚI
(N'Project', N'Dự án cuối khóa');
GO

-- 5. Bảng test_types
INSERT INTO test_types (name, description) VALUES
(N'Tự luyện (Practice)', N'Quiz luyện tập không tính điểm'),
(N'Giữa kỳ (Midterm)', N'Bài kiểm tra giữa khóa học'),
(N'Cuối kỳ (Final)', N'Bài thi cuối khóa'),
(N'Đầu vào (Entry)', N'Bài kiểm tra đánh giá đầu vào'),
(N'Trắc nghiệm nhanh (Quick Quiz)', N'Quiz ngắn sau mỗi bài học');
GO

-- 6. Bảng question_levels
INSERT INTO question_levels (name, description) VALUES
(N'Dễ (Easy)', N'Câu hỏi mức độ nhận biết'),
(N'Trung bình (Medium)', N'Câu hỏi mức độ thông hiểu'),
(N'Khó (Hard)', N'Câu hỏi mức độ vận dụng'),
(N'Rất khó (Expert)', N'Câu hỏi mức độ vận dụng cao, sáng tạo');
GO

-- 7. Bảng sliders
INSERT INTO sliders (image_url, link_url, title, status, order_number) VALUES
(N'https://loremflickr.com/1200/400/web,development?random=1', N'/courses/1', N'Khóa học Mới: Lập trình ReactJS 2025', N'active', 1),
(N'https://loremflickr.com/1200/400/programming,python?random=2', N'/courses/2', N'Data Science với Python', N'active', 2),
(N'https://loremflickr.com/1200/400/mobile,app?random=3', N'/courses/4', N'Flutter cho Người mới bắt đầu', N'active', 3),
(N'https://loremflickr.com/1200/400/devops,cloud?random=4', N'/courses/7', N'AWS Cloud Foundation', N'active', 4),
(N'https://loremflickr.com/1200/400/web,abstract?random=5', N'/blog/1', N'Top 10 Thư viện JavaScript 2025', N'active', 5);
GO

-- 8. Bảng users (Mật khẩu: 123456)
DECLARE @passwordHash VARCHAR(255) = '$2a$10$v60kNSroGckGc.E.jEeOpeMhN11ZWR.xId1PM.aF3.G.yOKb/1Ew.';
INSERT INTO users (email, password_hash, full_name, gender, mobile, role_id, avatar, [address], status) VALUES
('andche186895@fpt.edu.vn', @passwordHash, N'Admin Quản Trị', N'Nam', '0987654321', 1, N'https://i.pravatar.cc/150?img=1', N'123 Đường Admin, TP. HCM', 'active'),
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

-- 9. Bảng courses
INSERT INTO courses (name, category_id, description, status, is_featured, owner_id, thumbnail_url) VALUES
(N'Khóa học 1: HTML CSS Zero to Hero', 1, N'Học HTML CSS từ con số 0.', N'published', 1, 2, N'https://loremflickr.com/640/480/html,css?random=1'),
(N'Khóa học 2: JavaScript Nâng cao 2025', 1, N'Nắm vững ES13+, Async/Await.', N'published', 1, 2, N'https://loremflickr.com/640/480/javascript?random=2'),
(N'Khóa học 3: ReactJS & Redux Toolkit', 1, N'Xây dựng ứng dụng web hiện đại.', N'published', 1, 3, N'https://loremflickr.com/640/480/react?random=3'),
(N'Khóa học 4: Node.js & Express API', 2, N'Xây dựng RESTful API mạnh mẽ.', N'published', 1, 2, N'https://loremflickr.com/640/480/nodejs?random=4'),
(N'Khóa học 5: Spring Boot 3 Microservices', 2, N'Kiến trúc Microservices.', N'published', 1, 3, N'https://loremflickr.com/640/480/java,spring?random=5'),
(N'Khóa học 6: Lập trình Python từ A-Z', 10, N'Python cho người mới bắt đầu.', N'published', 1, 2, N'https://loremflickr.com/640/480/python?random=6'),
(N'Khóa học 7: Flutter (iOS & Android)', 4, N'Xây dựng ứng dụng di động.', N'published', 0, 3, N'https://loremflickr.com/640/480/flutter,mobile?random=7'),
(N'Khóa học 8: Nhập môn AI', 6, N'Hiểu về Trí tuệ Nhân tạo.', N'draft', 0, 2, N'https://loremflickr.com/640/480/ai,robot?random=8'),
(N'Khóa học 9: Docker & Kubernetes', 7, N'Học DevOps từ cơ bản.', N'published', 0, 3, N'https://loremflickr.com/640/480/devops,docker?random=9'),
(N'Khóa học 10: SQL Masterclass', 2, N'Thành thạo SQL Server, PostgreSQL.', N'published', 1, 2, N'https://loremflickr.com/640/480/sql,database?random=10');
GO

-- 10. Bảng blogs
INSERT INTO blogs (title, content, blog_category_id, author_id, thumbnail_url, status, published_at) VALUES
(N'10 Lỗi JavaScript người mới thường gặp', N'<h2>Lỗi 1: Sử dụng == thay vì ===</h2>', 8, 2, N'https://loremflickr.com/640/480/javascript,error?random=1', N'published', GETDATE()),
(N'So sánh React, Vue, và Angular 2025', N'<h2>React</h2><p>Thư viện linh hoạt.</p>', 1, 3, N'https://loremflickr.com/640/480/react,vue?random=2', N'published', GETDATE()),
(N'Top 5 extension VS Code', N'<p>1. Prettier - Code formatter</p>', 9, 3, N'https://loremflickr.com/640/480/vscode?random=4', N'published', GETDATE());
GO

-- 11. Bảng price_packages (giá demo)
INSERT INTO price_packages (course_id, name, duration_months, list_price, sale_price, status, sale) VALUES
(1, N'Gói 6 tháng (Course 1)', 6, 10000, 5000, N'active', 50),
(1, N'Gói trọn đời (Course 1)', 99, 15000, 7500, N'active', 50),
(2, N'Gói 6 tháng (Course 2)', 6, 12000, 6000, N'active', 50),
(2, N'Gói trọn đời (Course 2)', 99, 18000, 9000, N'active', 50),
(3, N'Gói 12 tháng (Course 3)', 12, 14000, 7000, N'active', 50),
(3, N'Gói trọn đời (Course 3)', 99, 20000, 10000, N'active', 50),
(4, N'Gói trọn đời (Course 4)', 99, 16000, 8000, N'active', 50),
(5, N'Gói trọn đời (Course 5)', 99, 20000, 10000, N'active', 50),
(6, N'Gói 6 tháng (Course 6)', 6, 10000, 5000, N'active', 50),
(7, N'Gói trọn đời (Course 7)', 99, 18000, 9000, N'active', 50);
GO


-- 12. Bảng quizzes
INSERT INTO quizzes (course_id, test_type_id, name, exam_level_id, duration_minutes, pass_rate_percentage) VALUES
(1, 5, N'Quiz 1: Kiểm tra HTML cơ bản', 1, 15, 80.00),
(1, 3, N'Quiz 2: Thi cuối khóa HTML/CSS', 2, 60, 75.00),
(2, 5, N'Quiz 3: Kiểm tra JavaScript (ES6+)', 2, 20, 80.00),
(2, 3, N'Quiz 4: Thi cuối khóa JavaScript', 3, 90, 70.00),
(3, 5, N'Quiz 5: Kiểm tra React Hooks', 2, 25, 80.00);
GO

-- 13. Bảng questions
INSERT INTO questions (course_id, lesson_id, question_level_id, status, content, explanation) VALUES
(1, NULL, 1, N'published', N'HTML là viết tắt của gì?', N'HTML là HyperText Markup Language.'),
(1, NULL, 1, N'published', N'Thẻ nào dùng để tạo một đoạn văn bản?', N'Thẻ <p> dùng để tạo đoạn văn bản.'),
(1, NULL, 1, N'published', N'CSS là viết tắt của gì?', N'CSS là Cascading Style Sheets.'),
(1, NULL, 2, N'published', N'Thuộc tính CSS nào dùng để đổi màu chữ?', N'Thuộc tính "color" dùng để đổi màu chữ.'),
(2, NULL, 2, N'published', N'Từ khóa nào trong ES6 dùng để khai báo biến không thể gán lại?', N'"const" dùng để khai báo hằng số.'),
(2, NULL, 2, N'published', N'Arrow function có "this" context của riêng nó không?', N'Không, arrow function mượn "this" của scope cha.'),
(2, NULL, 3, N'published', N'Đâu là một React Hook?', N'useState là một Hook cơ bản.'),
(6, NULL, 1, N'published', N'Hàm nào dùng để in ra màn hình trong Python?', N'Hàm print().');
GO

-- 14. Bảng answer_options
-- Câu 1 (Đúng: 2)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES
(1, N'Hyperlinks and Text Markup Language', 0, 1),
(1, N'HyperText Markup Language', 1, 2),
(1, N'Home Tool Markup Language', 0, 3),
(1, N'HyperText Main Language', 0, 4);
-- Câu 2 (Đúng: 7)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES
(2, N'<div>', 0, 1),
(2, N'<span>', 0, 2),
(2, N'<p>', 1, 3),
(2, N'<h1>', 0, 4);
-- Câu 3 (Đúng: 10)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES
(3, N'Colorful Style Sheets', 0, 1),
(3, N'Cascading Style Sheets', 1, 2),
(3, N'Creative Style Sheets', 0, 3),
(3, N'Computer Style Sheets', 0, 4);
-- Câu 4 (Đúng: 15)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES
(4, N'font-color', 0, 1),
(4, N'text-color', 0, 2),
(4, N'color', 1, 3),
(4, N'background-color', 0, 4);
-- Câu 5 (Đúng: 17)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES
(5, N'const', 1, 1),
(5, N'let', 0, 2),
(5, N'var', 0, 3),
(5, N'static', 0, 4);
-- Câu 6 (Đúng: 22)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES
(6, N'Có, nó có "this" riêng', 0, 1),
(6, N'Không, nó mượn "this" của scope cha', 1, 2);
-- Câu 7 (Đúng: 23)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES
(7, N'useState', 1, 1),
(7, N'React.Component', 0, 2),
(7, N'render()', 0, 3),
(7, N'this.state', 0, 4);
-- Câu 8 (Đúng: 28)
INSERT INTO answer_options (question_id, content, is_correct, order_number) VALUES
(8, N'console.log()', 0, 1),
(8, N'print()', 1, 2),
(8, N'System.out.println()', 0, 3),
(8, N'echo()', 0, 4);
GO


-- 15. Bảng labs (BỔ SUNG MỚI)
INSERT INTO labs (name, description, lab_type, evaluation_criteria) VALUES
(N'Lab 1: Hàm JavaScript đầu tiên',
 N'<h2>Yêu cầu</h2><p>Viết một hàm JavaScript tên là <code>sum</code> nhận vào 2 tham số <code>a</code> và <code>b</code>, và trả về tổng của chúng.</p><h3>Ví dụ:</h3><pre><code>sum(5, 10) // Phải trả về 15</code></pre>',
 N'coding',
 N'Tiêu chí chấm (cho AI): 1. Phải có hàm tên là "sum". 2. Hàm phải nhận 2 tham số. 3. Hàm phải trả về (return) tổng 2 tham số. 4. Code phải sạch, không có lỗi cú pháp.');
GO
INSERT INTO labs (name, description, lab_type, evaluation_criteria) VALUES
(N'Lab 2: Truy vấn SQL cơ bản',
 N'<h2>Yêu cầu</h2><p>Viết một câu lệnh SQL <code>SELECT</code> để lấy <code>name</code> và <code>email</code> từ bảng <code>users</code> nơi <code>status</code> là ''active''.</p>',
 N'sql',
 N'Tiêu chí chấm (cho AI): 1. Phải dùng SELECT. 2. Phải chọn đúng 2 cột "name", "email". 3. Phải FROM đúng bảng "users". 4. Phải WHERE "status" = ''active''.');
GO


-- 16. Bảng lessons (BỔ SUNG: link tới Quiz và Lab)
INSERT INTO lessons (course_id, lesson_type_id, name, topic, order_number, video_url, html_content, quiz_id, lab_id, status) VALUES
(1, 1, N'Giới thiệu HTML', N'HTML', 1, N'https://www.youtube.com/embed/okqEVeNqBhc', NULL, NULL, NULL, N'active'),
(1, 2, N'Các thẻ HTML cơ bản', N'HTML', 2, NULL, N'<h2>Thẻ P</h2><p>Thẻ p dùng để tạo một đoạn văn bản.</p><h2>Thẻ H</h2><p>Các thẻ h1 đến h6 dùng cho tiêu đề.</p>', NULL, NULL, N'active'),
(1, 3, N'Bài 1: Kiểm tra HTML (Quiz)', N'HTML', 3, NULL, NULL, 1, NULL, N'active'), -- Link Quiz 1
(1, 1, N'Giới thiệu CSS', N'CSS', 4, N'https://www.youtube.com/embed/OEV8gHsKqFs', NULL, NULL, NULL, N'active'),
(1, 2, N'CSS Flexbox', N'CSS', 5, NULL, N'<h2>Flexbox là gì?</h2><p>Flexbox là một mô hình layout trong CSS...</p>', NULL, NULL, N'active'),
(1, 3, N'Bài 2: Thi cuối khóa (Quiz)', N'CSS', 6, NULL, NULL, 2, NULL, N'active'), -- Link Quiz 2
(2, 1, N'Biến và Kiểu dữ liệu (JS)', N'JavaScript', 1, N'https://www.youtube.com/embed/1K5vqmrDQ8s?si=m20x0EZf2QBJ-H8C', NULL, NULL, NULL, N'active'),
(2, 2, N'ES6+ (Arrow Function, Let/Const)', N'JavaScript', 2, NULL, N'<h2>Arrow Function</h2><p>Cú pháp ngắn gọn hơn...</p>', NULL, NULL, N'active'),
(2, 4, N'Bài 3: Thực hành (Lab)', N'JavaScript', 3, NULL, NULL, NULL, 1, N'active'), -- Link Lab 1
(2, 3, N'Bài 4: Kiểm tra JS (Quiz)', N'JavaScript', 4, NULL, NULL, 3, NULL, N'active'), -- Link Quiz 3
(6, 1, N'Cài đặt Python', N'Python', 1, N'https://www.youtube.com/embed/q-Y0bnx6Ndw', NULL, NULL, NULL, N'active');
GO

-- 17. Bảng quiz_questions (BỔ SUNG MỚI - Rất quan trọng)
-- Link Quiz với Câu hỏi (M-M)
INSERT INTO quiz_questions (quiz_id, question_id) VALUES
(1, 1), -- Quiz 1 (HTML) có câu 1 (HTML là gì?)
(1, 2), -- Quiz 1 (HTML) có câu 2 (Thẻ <p>?)
(2, 3), -- Quiz 2 (CSS) có câu 3 (CSS là gì?)
(2, 4), -- Quiz 2 (CSS) có câu 4 (Thuộc tính color?)
(3, 5), -- Quiz 3 (JS) có câu 5 (const?)
(3, 6); -- Quiz 3 (JS) có câu 6 (Arrow function?)
GO

-- 18. Bảng registrations (Cập nhật: Thêm 1 PENDING)
INSERT INTO registrations (user_id, course_id, package_id, order_code, total_cost, status, valid_from, valid_to) VALUES
(4, 1, 2, N'ORD001', 1500000, N'COMPLETED', GETDATE(), DATEADD(month, 999, GETDATE())),
(4, 2, 4, N'ORD002', 1750000, N'COMPLETED', GETDATE(), DATEADD(month, 999, GETDATE())),
(5, 1, 1, N'ORD003', 600000, N'COMPLETED', GETDATE(), DATEADD(month, 6, GETDATE())),
(5, 3, 6, N'ORD004', 2500000, N'COMPLETED', GETDATE(), DATEADD(month, 999, GETDATE())),
(5, 6, 9, N'ORD005', 500000, N'PENDING', GETDATE(), NULL), -- Đây là đơn PENDING để test
(10, 1, 2, N'ORD006', 1500000, N'COMPLETED', GETDATE(), DATEADD(month, 999, GETDATE())),
(10, 5, 8, N'ORD010', 3000000, N'COMPLETED', GETDATE(), DATEADD(month, 999, GETDATE()));
GO

-- 19. Bảng feedbacks
INSERT INTO feedbacks (course_id, user_id, rating, comment) VALUES
(1, 4, 5, N'Khóa học HTML/CSS rất hay và chi tiết!'),
(1, 5, 4, N'Giảng viên giải thích dễ hiểu, nhưng cần thêm bài tập.'),
(2, 4, 5, N'Khóa JS Nâng cao thực sự tuyệt vời!');
GO

-- 20. Bảng quiz_attempts (BỔ SUNG MỚI)
-- Tạo 1 lượt làm bài (COMPLETED) cho student1 (ID 4) làm Quiz 1 (ID 1)
-- Giả sử Quiz 1 có 2 câu (Q1, Q2)
INSERT INTO quiz_attempts (user_id, quiz_id, start_time, end_time, score, status, result, ai_hint_count) VALUES
(4, 1, DATEADD(hour, -1, GETDATE()), GETDATE(), 50.00, 'COMPLETED', 'Fail', 1);
GO
-- Tạo 1 lượt làm bài (IN_PROGRESS) cho student2 (ID 5) làm Quiz 3 (ID 3)
INSERT INTO quiz_attempts (user_id, quiz_id, start_time, status, ai_hint_count) VALUES
(5, 3, GETDATE(), 'in_progress', 0);
GO


-- 21. Bảng quiz_attempt_answers (BỔ SUNG MỚI)
-- Thêm câu trả lời cho lượt làm bài (Attempt ID 1) của (User 4)
-- Giả sử Attempt ID là 1
-- Câu 1 (Hỏi HTML là gì?), User 4 chọn Answer 1 (Sai). Đáp án đúng là 2.
INSERT INTO quiz_attempt_answers (attempt_id, question_id, selected_answer_option_id, is_correct) VALUES
(1, 1, 1, 0);
-- Câu 2 (Hỏi thẻ <p>?), User 4 chọn Answer 7 (Đúng).
INSERT INTO quiz_attempt_answers (attempt_id, question_id, selected_answer_option_id, is_correct) VALUES
(1, 2, 7, 1);
GO


/*
================================================================
    PHẦN 4: KÍCH HOẠT LẠI KHÓA NGOẠI
================================================================
*/
EXEC sp_msforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all";
GO

PRINT N'=== HOÀN TẤT CHÈN DỮ LIỆU MẪU (Bao gồm Quiz & Lab) ===';