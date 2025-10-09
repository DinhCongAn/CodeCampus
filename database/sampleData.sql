-- Dữ liệu mẫu cho user_roles
SET IDENTITY_INSERT user_roles ON;
INSERT INTO user_roles (id, name, description) VALUES
(2, N'Customer', N'Người dùng đã đăng ký là khách hàng thực tế hoặc tiềm năng.'),
(3, N'Marketing', N'Thành viên bộ phận marketing của tổ chức.'),
(4, N'Sale', N'Thành viên bộ phận bán hàng của tổ chức.'),
(5, N'Expert', N'Truy cập và chuẩn bị nội dung khóa học/bài kiểm tra theo phân công của quản trị viên.'),
(6, N'Admin', N'Người lãnh đạo/quản lý tổ chức, đóng vai trò quản trị viên hệ thống.');
SET IDENTITY_INSERT user_roles OFF;

-- Dữ liệu mẫu cho users
SET IDENTITY_INSERT users ON;
INSERT INTO users (id, email, password_hash, full_name, gender, mobile, role_id, avatar, status, created_at, updated_at) VALUES
(1, 'admin@olls.com', '$2a$10$PFQ5Y.nk3qelXcFXGK2vYe.6sP2HOMfmBX./y71eC.dnQi06BSzei', N'Admin User', N'Male', '0901234567', 6, N'https://picsum.photos/id/1005/200/300', N'active', GETDATE(), GETDATE()),
(2, 'expert1@olls.com', '$2a$10$PFQ5Y.nk3qelXcFXGK2vYe.6sP2HOMfmBX./y71eC.dnQi06BSzei', N'Expert One', N'Female', '0902345678', 5, N'https://picsum.photos/id/1011/200/300', N'active', GETDATE(), GETDATE()),
(3, 'expert2@olls.com', '$2a$10$PFQ5Y.nk3qelXcFXGK2vYe.6sP2HOMfmBX./y71eC.dnQi06BSzei', N'Expert Two', N'Male', '0903456789', 5, N'https://picsum.photos/id/1012/200/300', N'active', GETDATE(), GETDATE()),
(4, 'customer1@example.com', '$2a$10$PFQ5Y.nk3qelXcFXGK2vYe.6sP2HOMfmBX./y71eC.dnQi06BSzei', N'Customer One', N'Female', '0911223344', 2, N'https://picsum.photos/id/1025/200/300', N'active', GETDATE(), GETDATE()),
(5, 'customer2@example.com', '$2a$10$PFQ5Y.nk3qelXcFXGK2vYe.6sP2HOMfmBX./y71eC.dnQi06BSzei', N'Customer Two', N'Male', '0912334455', 2, N'https://picsum.photos/id/1026/200/300', N'pending', GETDATE(), GETDATE()),
(6, 'marketing1@olls.com', '$2a$10$PFQ5Y.nk3qelXcFXGK2vYe.6sP2HOMfmBX./y71eC.dnQi06BSzei', N'Marketing Staff', N'Female', '0921234567', 3, N'https://picsum.photos/id/1027/200/300', N'active', GETDATE(), GETDATE()),
(7, 'sale1@olls.com', '$2a$10$PFQ5Y.nk3qelXcFXGK2vYe.6sP2HOMfmBX./y71eC.dnQi06BSzei', N'Sales Staff', N'Male', '0931234567', 4, N'https://picsum.photos/id/1035/200/300', N'active', GETDATE(), GETDATE());
SET IDENTITY_INSERT users OFF;


-- Dữ liệu mẫu cho settings

SET IDENTITY_INSERT settings ON;
INSERT INTO settings (id, [type], [value], order_num, [status], setting_key, setting_value, [description]) VALUES
(1, N'System', N'Language', 1, 'inactive', 'default_language', 'en_US', 'Default language for the application'),
(2, N'Email', N'SMTP_Host', 3, 'inactive', 'smtp.gmail.com', 'smtp.gmail.com', 'SMTP host for sending emails'),
(3, N'Email', N'SMTP_Port', 4, 'inactive', '587', '587', 'SMTP port for email configuration');
SET IDENTITY_INSERT settings OFF;

-- Dữ liệu mẫu cho blog_categories
SET IDENTITY_INSERT blog_categories ON;
INSERT INTO blog_categories (id, name, is_active) VALUES
(1, N'Tin tức giáo dục', 1),
(2, N'Mẹo học tập', 1),
(3, N'Công nghệ trong học tập', 1);
SET IDENTITY_INSERT blog_categories OFF;

-- Dữ liệu mẫu cho blogs
SET IDENTITY_INSERT blogs ON;
INSERT INTO blogs (id, title, content, blog_category_id, author_id, thumbnail_url, status, published_at, created_at, updated_at) VALUES
(1, N'Tương lai của giáo dục trực tuyến', N'Cái nhìn toàn diện về cách giáo dục trực tuyến đang phát triển.', 1, 1, N'https://picsum.photos/id/201/600/400', N'published', GETDATE(), GETDATE(), GETDATE()),
(2, N'Làm chủ quản lý thời gian cho các khóa học trực tuyến', N'Các chiến lược thực tế giúp sinh viên quản lý thời gian hiệu quả.', 2, 2, N'https://picsum.photos/id/202/600/400', N'published', GETDATE(), GETDATE(), GETDATE()),
(3, N'AI trong học tập: Cơ hội và thách thức', N'Khám phá tác động của Trí tuệ nhân tạo đối với việc học cá nhân hóa.', 3, 1, N'https://picsum.photos/id/203/600/400', N'draft', NULL, GETDATE(), GETDATE());
SET IDENTITY_INSERT blogs OFF;

-- Dữ liệu mẫu cho sliders
delete from sliders
SET IDENTITY_INSERT sliders ON;
INSERT INTO sliders (id, image_url, link_url, title, description, status, order_number) VALUES
(1, N'https://cafefcdn.com/thumb_w/640/pr/2022/photo1669101163120-1669101163214142105193-63804790993386.jpg', N'https://www.viaenglish.edu.vn/vi/via-english-academy-cung-ban-khai-phong-tiem-nang-ngon-ngu', N'Khai phá tiềm năng của bạn', N'Khám phá các khóa học ngôn ngữ hàng đầu của chúng tôi.', N'active', 1),
(2, N'https://giaviet.edu.vn/public/upload/images/thumb_baiviet/tong-hop-cac-chuong-trinh-uu-dai-cac-khoa-he-2024-tai-anh-ngu-gia-viet-301717753569.png', N'https://tienganhthieunhi.edu.vn/tin-tuc/uu-dai.html', N'Ưu đãi có thời hạn', N'Giảm 20% tất cả các khóa học ngôn ngữ cao cấp!', N'active', 2),
(3, N'https://synnexfpt.com/wp-content/uploads/2023/03/thumb.jpg', N'https://synnexfpt.com/20-loi-ich-tuyet-voi-cua-viec-hoc-ngon-ngu-thu-hai-phan-1/', N'Lợi ích của việc học ngôn ngữ', N'Khám phá cách học ngoại ngữ giúp cải thiện trí não và sự nghiệp.', N'active', 3),
(4, N'https://newsky.edu.vn/wp-content/uploads/trung-tam-tieng-anh-co-hoc-phi-tot-tai-tan-phu-600x300.png', N'https://speaknow.vn/phu-kim-quy-chia-se-cau-chuyen-thanh-cong-khi-hoc-tieng-anh/', N'Câu chuyện thành công', N'Nghe chia sẻ từ những học viên đã chinh phục ngoại ngữ.', N'active', 4),
(5, N'https://file.unica.vn/storage/fc2ee9cfd8f54fd092257c83fa8d328ec9fbcefa/meo-chon-chu-de-khoa-hoc.png', N'https://edubit.vn/blog/nhung-meo-chon-chu-de-khoa-hoc-phu-hop-va-thu-hut-hoc-vien', N'Làm thế nào chọn khóa học phù hợp', N'Các mẹo giúp bạn chọn khóa học phù hợp với mục tiêu cá nhân.', N'active', 5);
SET IDENTITY_INSERT sliders OFF;

-- Dữ liệu mẫu cho course_categories (đã điều chỉnh)
SET IDENTITY_INSERT course_categories ON;
INSERT INTO course_categories (id, name, description, is_active) VALUES
(1, N'Tiếng Anh', N'Các khóa học liên quan đến ngôn ngữ Anh.', 1),
(2, N'Tiếng Nhật', N'Các khóa học liên quan đến ngôn ngữ Nhật.', 1),
(3, N'Tiếng Hàn', N'Các khóa học liên quan đến ngôn ngữ Hàn.', 1),
(4, N'Tiếng Trung', N'Các khóa học liên quan đến ngôn ngữ Trung.', 1);
SET IDENTITY_INSERT course_categories OFF;

-- Dữ liệu mẫu cho courses (đã điều chỉnh)
SET IDENTITY_INSERT courses ON;
INSERT INTO courses (id, name, category_id, description, status, is_featured, owner_id, thumbnail_url, created_at, updated_at) VALUES
(1, N'Tiếng Anh cho người mới bắt đầu (A1)', 1, N'Học các kiến thức cơ bản về tiếng Anh cho người mới bắt đầu.', N'active', 1, 2, N'https://picsum.photos/id/221/400/250', GETDATE(), GETDATE()),
(2, N'Tiếng Nhật giao tiếp cơ bản (N5)', 2, N'Học giao tiếp cơ bản và ngữ pháp N5 tiếng Nhật.', N'active', 0, 3, N'https://picsum.photos/id/222/400/250', GETDATE(), GETDATE()),
(3, N'Luyện thi TOEIC', 1, N'Khóa học toàn diện để chuẩn bị cho kỳ thi TOEIC.', N'active', 1, 2, N'https://picsum.photos/id/223/400/250', GETDATE(), GETDATE()),
(4, N'Tiếng Hàn trung cấp (Topic II)', 3, N'Nâng cao kỹ năng tiếng Hàn cho trình độ trung cấp và luyện thi Topic II.', N'draft', 0, 3, N'https://picsum.photos/id/224/400/250', GETDATE(), GETDATE()),
(5, N'Tiếng Trung thương mại', 4, N'Khóa học tiếng Trung tập trung vào các tình huống kinh doanh.', N'active', 0, 2, N'https://picsum.photos/id/225/400/250', GETDATE(), GETDATE());
SET IDENTITY_INSERT courses OFF;

-- Dữ liệu mẫu cho price_packages
SET IDENTITY_INSERT price_packages ON;
INSERT INTO price_packages (id, course_id, name, duration_months, list_price, sale_price, status, description) VALUES
(1, 1, N'Gói cơ bản tiếng Anh A1', 3, 99.99, 79.99, N'active', N'3 tháng truy cập khóa tiếng Anh A1.'),
(2, 1, N'Gói cao cấp tiếng Anh A1', 12, 299.99, 249.99, N'active', N'12 tháng truy cập khóa tiếng Anh A1 với hỗ trợ cao cấp.'),
(3, 2, N'Gói tiêu chuẩn tiếng Nhật N5', 6, 149.99, 129.99, N'active', N'6 tháng truy cập khóa tiếng Nhật N5.'),
(4, 3, N'Gói luyện thi TOEIC', 6, 199.99, 179.99, N'active', N'6 tháng truy cập khóa luyện thi TOEIC.'),
(5, 4, N'Gói tiêu chuẩn  tiếng Hàn', 6, 199.99, 179.99, N'active', N'6 tháng truy cập khóa tiếng Hàn trung cấp.'),
(6, 5, N'Gói tiêu chuẩn  tiếng Trung', 6, 199.99, 179.99, N'active', N'6 tháng truy cập khóa tiếng Trung thương mại.');
SET IDENTITY_INSERT price_packages OFF;

-- Dữ liệu mẫu cho lesson_types
SET IDENTITY_INSERT lesson_types ON;
INSERT INTO lesson_types (id, name, description) VALUES
(1, N'Video', N'Nội dung bài học chủ yếu qua video.'),
(2, N'Text', N'Nội dung bài học chủ yếu qua văn bản/HTML.'),
(3, N'Quiz', N'Bài học là một bài kiểm tra hoặc đánh giá.');
SET IDENTITY_INSERT lesson_types OFF;

-- Dữ liệu mẫu cho test_types
SET IDENTITY_INSERT test_types ON;
INSERT INTO test_types (id, name, description) VALUES
(1, N'Bài kiểm tra thực hành', N'Để tự đánh giá và luyện tập.'),
(2, N'Bài kiểm tra giữa kỳ', N'Đánh giá sự hiểu biết về một phần của khóa học.'),
(3, N'Bài kiểm tra cuối kỳ', N'Đánh giá toàn diện khóa học.');
SET IDENTITY_INSERT test_types OFF;

-- Dữ liệu mẫu cho question_levels
SET IDENTITY_INSERT question_levels ON;
INSERT INTO question_levels (id, name, description) VALUES
(1, N'Dễ', N'Các câu hỏi hiểu biết cơ bản.'),
(2, N'Trung bình', N'Các câu hỏi hiểu biết và ứng dụng trung cấp.'),
(3, N'Khó', N'Các câu hỏi giải quyết vấn đề nâng cao.');
SET IDENTITY_INSERT question_levels OFF;

-- Dữ liệu mẫu cho quizzes
SET IDENTITY_INSERT quizzes ON;
INSERT INTO quizzes (id, course_id, test_type_id, name, exam_level_id, duration_minutes, pass_rate_percentage, description) VALUES
(1, 1, 1, N'Bài kiểm tra tiếng Anh cơ bản 1', 1, 30, 70.00, N'Một bài kiểm tra ngắn về các kiến thức cơ bản tiếng Anh.'),
(2, 1, 2, N'Bài kiểm tra giữa kỳ tiếng Anh', 2, 60, 75.00, N'Đánh giá giữa kỳ cho khóa giới thiệu tiếng Anh.'),
(3, 3, 1, N'Bài tập ngữ pháp TOEIC', 2, 45, 60.00, N'Bài kiểm tra thực hành về các cấu trúc ngữ pháp TOEIC.'),
(4, 2, 1, N'Bài kiểm tra tổng hợp N5', 1, 40, 70.00, N'Bài kiểm tra này đánh giá tổng hợp kiến thức đầu vào trình độ N5, gồm ngữ pháp, từ vựng và đọc hiểu cơ bản.'),
(5, 4, 2, N'Bài kiểm tra giữa kỳ Topic II', 2, 45, 75.00, N'Bài kiểm tra đánh giá kỹ năng ngữ pháp và đọc hiểu tiếng Hàn trung cấp, giúp học viên xác định mức độ tiến bộ.'),
(6, 5, 1, N'Bài kiểm tra kiến thức cơ bản thương mại', 1, 30, 70.00, N'Bài kiểm tra giúp đánh giá kiến thức nền tảng về tiếng Trung thương mại, bao gồm từ vựng, mẫu câu giao tiếp trong kinh doanh.');
SET IDENTITY_INSERT quizzes OFF;

-- Dữ liệu mẫu cho lessons
SET IDENTITY_INSERT lessons ON;
INSERT INTO lessons (id, course_id, lesson_type_id, name, topic, order_number, video_url, html_content, quiz_id, status, package_id) VALUES
(1, 1, 1, N'Giới thiệu về tiếng Anh', N'Bảng chữ cái và cách phát âm', 1, N'https://www.youtube.com/embed/dQw4w9WgXcQ', NULL, NULL, N'active', 1),
(2, 1, 2, N'Chào hỏi và giới thiệu bản thân', N'Các câu chào hỏi cơ bản', 2, NULL, N'<p>Bài học này bao gồm các cách chào hỏi và giới thiệu bản thân...</p>', NULL, N'active', 1),
(3, 1, 3, N'Bài kiểm tra tiếng Anh cơ bản', N'Kiểm tra các bài 1-2', 3, NULL, NULL, 1, N'active', 2),
(4, 3, 1, N'Tổng quan về kỳ thi TOEIC', N'Cấu trúc và định dạng bài thi', 1, N'https://www.youtube.com/embed/dQw4w9WgXcQ', NULL, NULL, N'active', 4),
(5, 3, 2, N'Ngữ pháp TOEIC: Thì', N'Các thì trong tiếng Anh và cách dùng trong TOEIC', 2, NULL, N'<p>Hiểu các thì trong tiếng Anh và cách áp dụng trong TOEIC...</p>', NULL, N'active', 4),
(6, 3, 3, N'Bài tập ngữ pháp TOEIC', N'Kiểm tra ngữ pháp TOEIC', 3, NULL, NULL, 3, N'active', 4),
(7, 1, 3, N'Bài kiểm tra giữa kỳ tiếng Anh', N'Đánh giá giữa kỳ', 4, NULL, NULL, 2, N'active', 1),
(8, 2, 1, N'Giới thiệu về tiếng Nhật N5', N'Giới thiệu về bảng chữ cái Hiragana, Katakana', 1, N'https://www.youtube.com/embed/JapaneseN5', NULL, NULL, N'active', 3),
(9, 2, 3, N'Bài kiểm tra tổng hợp N5', N'Kiểm tra kiến thức cơ bản N5', 2, NULL, NULL, 4, N'active', 3),
(10, 4, 1, N'Giới thiệu tiếng Hàn trung cấp', N'Ngữ pháp trung cấp, mẫu câu giao tiếp', 1, N'https://www.youtube.com/embed/KoreanTopic2', NULL, NULL, N'active', 5),
(11, 4, 3, N'Bài kiểm tra giữa kỳ Topic II', N'Kiểm tra tổng hợp kiến thức trung cấp', 2, NULL, NULL, 5, N'active', 5),
(12, 5, 1, N'Giới thiệu tiếng Trung thương mại', N'Từ vựng chuyên ngành kinh doanh', 1, N'https://www.youtube.com/embed/ChineseBusiness', NULL, NULL, N'active', 6),
(13, 5, 3, N'Bài kiểm tra kiến thức cơ bản', N'Kiểm tra từ vựng và ngữ pháp thương mại', 2, NULL, NULL, 6, N'active', 6);
SET IDENTITY_INSERT lessons OFF;

-- Dữ liệu mẫu cho questions
SET IDENTITY_INSERT questions ON;
INSERT INTO questions (id, course_id, lesson_id, question_level_id, status, content, media_url, explanation) VALUES
(1, 1, 3, 1, N'active', N'Chọn câu trả lời đúng cho "How are you?"', NULL, N'Câu trả lời phổ biến cho "How are you?" là "I am fine, thank you."'),
(2, 1, 3, 1, N'active', N'Từ nào sau đây là danh từ số ít?', NULL, N'Apple là danh từ số ít, trong khi apples là số nhiều.'),
(3, 3, 6, 2, N'active', N'Chọn câu có lỗi ngữ pháp:', NULL, N'Câu đúng phải là "He goes to school every day."'),
(4, 3, 6, 2, N'active', N'Từ đồng nghĩa với "diligent" là gì?', NULL, N'Từ đồng nghĩa với "diligent" là "hard-working".'),
(5, 1, 3, 1, N'active', N'She ___ a doctor.', NULL, N'Sử dụng "is" với chủ ngữ ngôi thứ ba số ít.'),
(6, 1, 3, 1, N'active', N'What is this? ___ is an apple.', NULL, N'Sử dụng "It" để chỉ vật.'),
(7, 1, 3, 1, N'active', N'I have two ___ .', NULL, N'Danh từ số nhiều của "book" là "books".'),
(8, 1, 3, 1, N'active', N'They ___ from Vietnam.', NULL, N'Sử dụng "are" với chủ ngữ "They".'),
(9, 1, 3, 1, N'active', N'___ your name?', NULL, N'Cấu trúc hỏi tên: What is your name?'),
(10, 1, 3, 1, N'active', N'I wake up ___ 7 AM every morning.', NULL, N'Sử dụng giới từ "at" với thời gian cụ thể.'),
(11, 1, 3, 1, N'active', N'Can you ___ English?', NULL, N'Động từ "can" đi với động từ nguyên mẫu.'),
(12, 1, 3, 1, N'active', N'My favorite color is ___ .', NULL, N'Câu trả lời hợp lý cho màu sắc.'),
(13, 1, 3, 1, N'active', N'He ___ to school by bus.', NULL, N'Thì hiện tại đơn với chủ ngữ "He".'),
(14, 1, 3, 1, N'active', N'Chọn từ khác loại.', NULL, N'"Happy" là tính từ, các từ còn lại là danh từ chỉ đồ vật.'),
(15, 1, 3, 1, N'active', N'How many days are there in a week?', NULL, N'Một tuần có 7 ngày.'),
(16, 1, 3, 1, N'active', N'Which month comes after May?', NULL, N'Tháng 6 (June) đến sau tháng 5 (May).'),
(17, 1, 3, 1, N'active', N'What do you do? - I am a ___ .', NULL, N'"Student" là một nghề nghiệp.'),
(18, 1, 3, 1, N'active', N'The opposite of "hot" is ___ .', NULL, N'Trái nghĩa với "nóng" là "lạnh".'),
(19, 1, 3, 1, N'active', N'I would like ___ water, please.', NULL, N'Some được dùng trong câu yêu cầu.'),
(20, 3, 6, 2, N'active', N'Mr. Tanaka will be unavailable ___ he is attending a conference.', NULL, N'"because" được dùng để chỉ nguyên nhân.'),
(21, 3, 6, 2, N'active', N'The report must be submitted ___ Friday at 5 PM.', NULL, N'"by" có nghĩa là "trước hoặc vào lúc".'),
(22, 3, 6, 2, N'active', N'All employees are required to attend the ___ safety training.', NULL, N'Cần một tính từ (mandatory) để bổ nghĩa cho danh từ (training).'),
(23, 3, 6, 2, N'active', N'The new software is much more ___ than the previous version.', NULL, N'Cần một tính từ sau "more" trong cấu trúc so sánh hơn.'),
(24, 3, 6, 2, N'active', N'Please complete the form and return it to the ___ department.', NULL, N'Cần một danh từ (personnel) làm tính từ bổ nghĩa cho "department".'),
(25, 3, 6, 2, N'active', N'___ the heavy rain, the outdoor event was cancelled.', NULL, N'"Due to" được dùng với một cụm danh từ.'),
(26, 3, 6, 2, N'active', N'The company has experienced significant ___ in the last quarter.', NULL, N'Cần một danh từ (growth) sau tính từ (significant).'),
(27, 3, 6, 2, N'active', N'She is responsible for ___ all the marketing campaigns.', NULL, N'Sau giới từ "for", ta dùng danh động từ (V-ing).'),
(28, 3, 6, 3, N'active', N'Not only ___ the project on time, but he also exceeded expectations.', NULL, N'Trong cấu trúc đảo ngữ với "Not only", trợ động từ đứng trước chủ ngữ.'),
(29, 3, 6, 2, N'active', N'The manager was impressed by the ___ of the new intern.', NULL, N'Cần một danh từ (professionalism) sau mạo từ "the".'),
(30, 3, 6, 2, N'active', N'Customers who ___ a product within 30 days are eligible for a full refund.', NULL, N'Mệnh đề quan hệ cần một động từ chia ở thì hiện tại đơn cho chủ ngữ số nhiều (customers).'),
(31, 3, 6, 2, N'active', N'The new office building is ___ located near public transportation.', NULL, N'Cần một trạng từ (conveniently) để bổ nghĩa cho động từ "located".'),
(32, 3, 6, 3, N'active', N'___ having years of experience, she was not hired for the position.', NULL, N'"Despite" được dùng trước một danh động từ (V-ing).'),
(33, 3, 6, 2, N'active', N'The team worked ___ to meet the deadline.', NULL, N'Cần một trạng từ (diligently) để bổ nghĩa cho động từ "worked".'),
(34, 3, 6, 2, N'active', N'The success of our project depends largely ___ teamwork.', NULL, N'Động từ "depend" đi với giới từ "on".'),
(35, 2, 9, 1, N'active', N'Cách đọc đúng của chữ "あ" là gì?', NULL, N'"あ" phát âm là "a", là chữ cái đầu tiên trong bảng Hiragana. Đây là âm cơ bản nhất mà mọi học viên N5 cần nắm vững.'),
(36, 2, 9, 1, N'active', N'Chọn nghĩa đúng của từ "水 (みず)"', NULL, N'Từ "水" đọc là "mizu", nghĩa là "nước". Đây là một từ vựng quen thuộc thường xuất hiện trong giao tiếp hàng ngày.'),
(37, 2, 9, 2, N'active', N'Chọn cấu trúc giới thiệu bản thân đúng:', NULL, N'Ở trình độ N5, câu giới thiệu bản thân cơ bản là "わたしは [Tên] です" nghĩa là "Tôi là [Tên]". "です" dùng để kết thúc câu với ý nghĩa trang trọng.'),
(38, 4, 11, 2, N'active', N'Từ nào sau đây là trạng từ?', NULL, N'Trạng từ trong tiếng Hàn thường kết thúc bằng đuôi "-게". Trong câu này, "빠르게" là trạng từ, có nghĩa là "nhanh".'),
(39, 4, 11, 2, N'active', N'Chọn mẫu câu diễn đạt mong muốn đúng:', NULL, N'Diễn đạt mong muốn trong tiếng Hàn thường dùng cấu trúc "고 싶다", ví dụ: "배우고 싶어요" nghĩa là "Tôi muốn học".'),
(40, 5, 13, 1, N'active', N'Chọn nghĩa đúng của từ "合同 (hétóng)"', NULL, N'"合同" đọc là "hétóng" trong tiếng Trung, nghĩa là "hợp đồng". Đây là thuật ngữ cơ bản thường dùng trong giao dịch kinh doanh.'),
(41, 5, 13, 1, N'active', N'Mẫu câu nào dùng để chào hỏi đối tác trong kinh doanh?', NULL, N'Khi chào hỏi đối tác, mẫu câu phổ biến là "您好！" (nín hǎo) thể hiện sự lịch sự, tôn trọng trong môi trường làm việc.'),
(42, 1, 3, 1, N'active', N'Chọn từ còn thiếu: She ___ a teacher.', NULL, N'Với chủ ngữ "She", dùng động từ "is" để chỉ nghề nghiệp hoặc trạng thái.'),
(43, 1, 3, 1, N'active', N'Which word is a noun?', NULL, N'Danh từ (noun) chỉ người, vật, sự việc. "Table" là danh từ, còn "run" là động từ, "blue" là tính từ.'),
(44, 1, 3, 1, N'active', N'Fill in: I ___ to school every day.', NULL, N'Ở thì hiện tại đơn với "I", dùng động từ nguyên mẫu "go".'),
(45, 1, 3, 1, N'active', N'Choose the correct question: ___ do you live?', NULL, N'Dùng từ hỏi "Where" để hỏi về địa điểm sống.'),
(46, 1, 3, 1, N'active', N'The plural of "bus" is ___ .', NULL, N'Quy tắc thêm "es" cho danh từ tận cùng là "s": "buses".'),
(47, 1, 3, 1, N'active', N'What is the color of the sky on a clear day?', NULL, N'Bầu trời thường có màu xanh (blue) vào ngày nắng đẹp.'),
(48, 1, 3, 1, N'active', N'How many legs does a dog have?', NULL, N'Một con chó bình thường có 4 chân.'),
(49, 1, 3, 1, N'active', N'Choose the correct preposition: The cat is ___ the table.', NULL, N'Dùng "on" để chỉ vị trí ở trên bề mặt.'),
(50, 1, 3, 1, N'active', N'Which is a correct greeting?', NULL, N'"Good morning" là lời chào phổ biến vào buổi sáng.'),
(51, 1, 3, 1, N'active', N'What is the antonym of "young"?', NULL, N'Từ trái nghĩa với "young" là "old".'),
(52, 1, 3, 1, N'active', N'Which sentence is correct?', NULL, N'Ở thì hiện tại đơn với "He", động từ phải thêm "s": "He likes apples."'),
(53, 1, 3, 1, N'active', N'Choose the word that is NOT a fruit.', NULL, N'"Car" là phương tiện, không phải trái cây.'),
(54, 1, 3, 1, N'active', N'Fill in: I ___ coffee.', NULL, N'Dùng "like" cho sở thích cá nhân.'),
(55, 1, 3, 1, N'active', N'Which word is an adjective?', NULL, N'"Beautiful" là tính từ, còn "run" là động từ, "book" là danh từ.'),
(56, 1, 3, 1, N'active', N'What is the capital of Vietnam?', NULL, N'Thủ đô Việt Nam là Hà Nội.'),
(57, 1, 3, 1, N'active', N'Find the correct negative form: She ___ like fish.', NULL, N'Phủ định ở thì hiện tại đơn với "She": "does not/doesn’t like".'),
(58, 1, 3, 1, N'active', N'Fill in: It ___ raining now.', NULL, N'Thì hiện tại tiếp diễn: "is raining".'),
(59, 1, 3, 1, N'active', N'Which is a question word?', NULL, N'"Who" là đại từ nghi vấn.'),
(60, 1, 3, 1, N'active', N'Choose the correct answer: What ___ you do?', NULL, N'Câu hỏi nghề nghiệp: "What do you do?"'),
(61, 1, 3, 1, N'active', N'Which sentence is in the past tense?', NULL, N'"I visited my grandmother yesterday." là quá khứ.'),
(62, 1, 3, 1, N'active', N'Choose the word that means the same as "happy".', NULL, N'"Glad" cùng nghĩa với "happy".'),
(63, 1, 3, 1, N'active', N'Which is a correct response to "Thank you"?', NULL, N'"You’re welcome." là câu trả lời lịch sự.'),
(64, 1, 3, 1, N'active', N'Fill in: There ___ two books on the table.', NULL, N'Chủ ngữ số nhiều "two books" dùng "are".'),
(65, 1, 3, 1, N'active', N'Find the odd one out.', NULL, N'"Red, green, yellow" là màu sắc, "dog" là con vật.'),
(66, 1, 3, 1, N'active', N'Which word is a pronoun?', NULL, N'"They" là đại từ.'),
(67, 1, 3, 1, N'active', N'Fill in: I ___ born in 2000.', NULL, N'Dùng "was" cho chủ ngữ "I" ở quá khứ.'),
(68, 1, 3, 1, N'active', N'What is the past tense of "go"?', NULL, N'Quá khứ bất quy tắc của "go" là "went".'),
(69, 1, 3, 1, N'active', N'Choose the correct comparative: "tall" -> ___', NULL, N'So sánh hơn của "tall" là "taller".'),
(70, 1, 3, 1, N'active', N'Which word is a day of the week?', NULL, N'"Sunday" là một ngày trong tuần.'),
(71, 1, 3, 1, N'active', N'Fill in: My mother ___ a doctor.', NULL, N'"is" dùng với chủ ngữ số ít "my mother".'),
(72, 1, 3, 1, N'active', N'What color is a banana?', NULL, N'Một quả chuối thường có màu vàng (yellow).'),
(73, 1, 3, 1, N'active', N'Choose the correct form: He ___ TV every evening.', NULL, N'"Watches" là dạng đúng cho "He" ở thì hiện tại đơn.'),
(74, 1, 3, 1, N'active', N'Fill in: ___ your favorite subject?', NULL, N'Câu hỏi chủ đề yêu thích: "What is your favorite subject?"'),
(75, 1, 3, 1, N'active', N'Which is a means of transportation?', NULL, N'"Bicycle" là phương tiện giao thông.'),
(76, 1, 3, 1, N'active', N'Find the synonym of "big".', NULL, N'"Large" là từ đồng nghĩa với "big".'),
(77, 1, 3, 1, N'active', N'Fill in: The opposite of "difficult" is ___ .', NULL, N'Từ trái nghĩa với "difficult" là "easy".'),
(78, 1, 3, 1, N'active', N'Which word is NOT a color?', NULL, N'"Pen" là đồ vật, không phải màu sắc.'),
(79, 1, 3, 1, N'active', N'Choose the correct spelling.', NULL, N'"Beautiful" là cách viết đúng.'),
(80, 1, 3, 1, N'active', N'Fill in: I ___ milk for breakfast.', NULL, N'"drink" là động từ đúng.'),
(81, 1, 3, 1, N'active', N'Which is a farm animal?', NULL, N'"Cow" là động vật nuôi trong trang trại.'),
(100, 1, 7, 2, N'active', N'Which sentence uses the present perfect tense?', NULL, N'The present perfect uses "have/has" + V3.'),
(101, 1, 7, 2, N'active', N'Fill in: If it ___ tomorrow, we will stay at home.', NULL, N'Chia thì hiện tại đơn trong mệnh đề điều kiện loại 1.'),
(102, 1, 7, 2, N'active', N'Choose the correct passive form: The cake ___ by Mary.', NULL, N'Dạng bị động: be + V3.'),
(103, 1, 7, 2, N'active', N'Find the synonym of "quick".', NULL, N'"Fast" đồng nghĩa với "quick".'),
(104, 1, 7, 2, N'active', N'Which is a countable noun?', NULL, N'"Apple" là danh từ đếm được.'),
(105, 1, 7, 2, N'active', N'Fill in: She has lived here ___ 2010.', NULL, N'"Since" + mốc thời gian.'),
(106, 1, 7, 2, N'active', N'What is the comparative form of "good"?', NULL, N'So sánh hơn bất quy tắc của "good" là "better".'),
(107, 1, 7, 2, N'active', N'Choose the correct reported speech: He said, "I am fine."', NULL, N'Tường thuật: am -> was.'),
(108, 1, 7, 2, N'active', N'Which one is a modal verb?', NULL, N'"Can" là động từ khuyết thiếu.'),
(109, 1, 7, 2, N'active', N'Choose the correct answer: I am looking forward ___ from you.', NULL, N'sau "look forward to" dùng V-ing.'),
(110, 1, 7, 2, N'active', N'What is the superlative of "small"?', NULL, N'"The smallest" là so sánh nhất của "small".'),
(111, 1, 7, 2, N'active', N'Choose the sentence with correct word order.', NULL, N'Dạng đúng: S + V + O + trạng từ.'),
(112, 1, 7, 2, N'active', N'Fill in: He ___ to the market yesterday.', NULL, N'Thì quá khứ đơn: went.'),
(113, 1, 7, 2, N'active', N'Which suffix can form an adjective?', NULL, N'"-ful" là hậu tố của tính từ: "careful".'),
(114, 1, 7, 2, N'active', N'Choose the correct preposition: I am interested ___ music.', NULL, N'"Interested in" là cụm cố định.'),
(115, 1, 7, 2, N'active', N'What is the plural form of "child"?', NULL, N'"Children" là số nhiều bất quy tắc.'),
(116, 1, 7, 2, N'active', N'Choose the correct conditional sentence type 2.', NULL, N'If + V2, would + V.'),
(117, 1, 7, 2, N'active', N'What does "rarely" mean?', NULL, N'"Rarely" có nghĩa là "hiếm khi".'),
(118, 1, 7, 2, N'active', N'Which is a synonym for "difficult"?', NULL, N'"Hard" là đồng nghĩa với "difficult".'),
(119, 1, 7, 2, N'active', N'Choose the correct question tag: You are coming, ___?', NULL, N'Khẳng định + aren’t you?'),
(120, 1, 7, 2, N'active', N'Which sentence is correct?', NULL, N'Có đủ chủ ngữ, động từ, tân ngữ.'),
(121, 1, 7, 2, N'active', N'Fill in: The book ___ on the table.', NULL, N'"Is" cho chủ ngữ số ít.'),
(122, 1, 7, 2, N'active', N'What is the past participle of "write"?', NULL, N'"Written" là V3 của "write".'),
(123, 1, 7, 2, N'active', N'Which is a preposition?', NULL, N'"Under" là giới từ.'),
(124, 1, 7, 2, N'active', N'Choose the correct answer: She ___ lunch at 12.', NULL, N'Thì hiện tại đơn với "She": eats.'),
(125, 1, 7, 2, N'active', N'What is the synonym of "beautiful"?', NULL, N'"Pretty" là đồng nghĩa.'),
(126, 1, 7, 2, N'active', N'Fill in: Tom and Jerry ___ good friends.', NULL, N'"Are" cho chủ ngữ số nhiều.'),
(127, 1, 7, 2, N'active', N'Which is a question word?', NULL, N'"When" là từ hỏi.'),
(128, 1, 7, 2, N'active', N'Fill in: There ___ a lot of people at the party.', NULL, N'"Were" là quá khứ của "are".'),
(129, 1, 7, 2, N'active', N'Choose the antonym of "cheap".', NULL, N'"Expensive" là trái nghĩa.'),
(130, 1, 7, 2, N'active', N'Fill in: ___ you ever been to London?', NULL, N'Have you ever... là thì hiện tại hoàn thành.'),
(131, 1, 7, 2, N'active', N'Choose the correct answer: I ___ TV when she called.', NULL, N'Quá khứ tiếp diễn: was watching.'),
(132, 1, 7, 2, N'active', N'Which is a means of communication?', NULL, N'"Email" là phương tiện liên lạc.'),
(133, 1, 7, 2, N'active', N'Fill in: My sister ___ English very well.', NULL, N'"Speaks" cho chủ ngữ số ít.'),
(134, 1, 7, 2, N'active', N'Choose the correct spelling.', NULL, N'"Necessary" là đúng.'),
(135, 1, 7, 2, N'active', N'Which is a farm animal?', NULL, N'"Sheep" là vật nuôi.'),
(136, 1, 7, 2, N'active', N'Choose the correct article: ___ honest man', NULL, N'"An" trước nguyên âm câm h.'),
(137, 1, 7, 2, N'active', N'Fill in: She ___ to bed at 10 PM.', NULL, N'"Goes" cho chủ ngữ số ít.'),
(138, 1, 7, 2, N'active', N'Which is a synonym for "start"?', NULL, N'"Begin" là đồng nghĩa.'),
(200, 3, 6, 2, N'active', N'Choose the correct form: The meeting ___ at 9 a.m. every Monday.', NULL, N'Thói quen, dùng thì hiện tại đơn, chủ ngữ số ít "meeting" -> "starts".'),
(201, 3, 6, 2, N'active', N'What is the passive voice of: "They deliver the package every day"?', NULL, N'Bị động: be + V3, với "the package" là chủ ngữ: "The package is delivered every day."'),
(202, 3, 6, 2, N'active', N'Which word is an adverb?', NULL, N'"Quickly" là trạng từ, bổ nghĩa cho động từ.'),
(203, 3, 6, 2, N'active', N'Fill in: She ___ finished her report yet.', NULL, N'Phủ định hiện tại hoàn thành: "hasn’t finished".'),
(204, 3, 6, 2, N'active', N'Choose the correct preposition: He is responsible ___ the project.', NULL, N'"Responsible for" là cụm cố định.'),
(205, 3, 6, 2, N'active', N'Find the synonym of "require".', NULL, N'"Need" là từ đồng nghĩa.'),
(206, 3, 6, 2, N'active', N'Which is a conditional type 1 sentence?', NULL, N'"If + hiện tại đơn, will + V".'),
(207, 3, 6, 2, N'active', N'What is the superlative form of "early"?', NULL, N'"The earliest" là so sánh nhất.'),
(208, 3, 6, 2, N'active', N'Choose the correct sentence: ___ you ever been to the US?', NULL, N'Hiện tại hoàn thành: "Have you ever been...".'),
(209, 3, 6, 2, N'active', N'Which is a formal closing for a business email?', NULL, N'"Best regards" thường dùng kết thư trang trọng.'),
(210, 3, 6, 2, N'active', N'Fill in: The report ___ by the manager yesterday.', NULL, N'Bị động quá khứ: "was reviewed".'),
(211, 3, 6, 2, N'active', N'Which word is a conjunction?', NULL, N'"Although" là liên từ.'),
(212, 3, 6, 2, N'active', N'Choose the correct tense: By next year, I ___ here for 5 years.', NULL, N'Hiện tại hoàn thành tiếp diễn: "will have been working".'),
(213, 3, 6, 2, N'active', N'Fill in: We look forward ___ from you soon.', NULL, N'"Look forward to" + V-ing.'),
(214, 3, 6, 2, N'active', N'Choose the correct phrasal verb: She ___ up the phone.', NULL, N'"Pick up" là cụm động từ đúng.'),
(215, 3, 6, 2, N'active', N'Find the antonym of "increase".', NULL, N'"Decrease" là trái nghĩa.'),
(216, 3, 6, 2, N'active', N'Fill in: The presentation ___ by the team last week.', NULL, N'Quá khứ bị động: "was prepared".'),
(217, 3, 6, 2, N'active', N'Choose the correct word: The ___ of the contract is tomorrow.', NULL, N'"Expiration" là danh từ chỉ ngày hết hạn.'),
(218, 3, 6, 2, N'active', N'Which is a modal verb?', NULL, N'"Should" là động từ khuyết thiếu.'),
(219, 3, 6, 2, N'active', N'Fill in: If I ___ enough money, I would buy a car.', NULL, N'Điều kiện loại 2: "had".'),
(220, 3, 6, 2, N'active', N'Choose the correct tense: When I arrived, the meeting ___ already started.', NULL, N'Quá khứ hoàn thành: "had".'),
(221, 3, 6, 2, N'active', N'Which is a synonym of "purchase"?', NULL, N'"Buy" là đồng nghĩa.'),
(222, 3, 6, 2, N'active', N'Which sentence is correct?', NULL, N'Câu đúng: "The documents have been sent."'),
(223, 3, 6, 2, N'active', N'Fill in: She is ___ than her sister.', NULL, N'So sánh hơn: "taller".'),
(224, 3, 6, 2, N'active', N'What is the plural of "analysis"?', NULL, N'"Analyses" là số nhiều.'),
(225, 3, 6, 2, N'active', N'Choose the correct word: I am writing ___ response to your email.', NULL, N'"In response" là cụm đúng.'),
(226, 3, 6, 2, N'active', N'Which is a business idiom meaning "immediately"?', NULL, N'"Right off the bat" nghĩa là ngay lập tức.'),
(227, 3, 6, 2, N'active', N'Fill in: The ___ of the company met yesterday.', NULL, N'"Board" là ban lãnh đạo.'),
(228, 3, 6, 2, N'active', N'Choose the correct word: Please ___ your seatbelt.', NULL, N'"Fasten" là đúng.'),
(229, 3, 6, 2, N'active', N'Which is an uncountable noun?', NULL, N'"Information" không đếm được.'),
(230, 3, 6, 2, N'active', N'Choose the correct answer: Who ___ to the meeting?', NULL, N'"Came" là quá khứ của "come".'),
(231, 3, 6, 2, N'active', N'Fill in: ___ you like some coffee?', NULL, N'"Would you like..." là lịch sự.'),
(232, 3, 6, 2, N'active', N'Which word is a synonym for "improve"?', NULL, N'"Enhance" là đồng nghĩa.'),
(233, 3, 6, 2, N'active', N'Choose the correct preposition: She succeeded ___ solving the problem.', NULL, N'"In" là giới từ đi với "succeed".'),
(234, 3, 6, 2, N'active', N'Fill in: I am not used ___ up early.', NULL, N'"To getting" là cấu trúc đúng.'),
(235, 3, 6, 2, N'active', N'Choose the correct voice: The project ___ completed by the team.', NULL, N'Bị động hiện tại hoàn thành: "has been completed".'),
(236, 3, 6, 2, N'active', N'Which is a correct business phone phrase?', NULL, N'"May I speak to Mr. Smith?" là lịch sự.'),
(237, 3, 6, 2, N'active', N'Fill in: The results ___ announced tomorrow.', NULL, N'Tương lai bị động: "will be announced".'),
(238, 3, 6, 2, N'active', N'Which is a synonym for "assist"?', NULL, N'"Help" là đồng nghĩa.'),
(300, 2, 9, 1, N'active', N'Chữ Hiragana nào sau đây đọc là "ka"?', NULL, N'"か" là chữ "ka" trong bảng Hiragana.'),
(301, 2, 9, 1, N'active', N'Từ "水" nghĩa là gì?', NULL, N'"水" đọc là "mizu", nghĩa là "nước".'),
(302, 2, 9, 1, N'active', N'Chọn động từ đúng cho câu: 私は毎日___。', NULL, N'Dùng động từ chỉ hành động hằng ngày, ví dụ "勉強します" (học).'),
(303, 2, 9, 1, N'active', N'Số 7 trong tiếng Nhật là?', NULL, N'Bảy là "七" đọc là "nana" hoặc "shichi".'),
(304, 2, 9, 1, N'active', N'Câu nào đúng với ngữ pháp N5?', NULL, N'Chủ ngữ + は + danh từ + です là cấu trúc cơ bản.'),
(305, 2, 9, 1, N'active', N'Từ nào là tính từ?', NULL, N'"高い" là tính từ nghĩa là "cao, đắt".'),
(306, 2, 9, 1, N'active', N'Chọn giới từ đúng: いすの___に犬がいます。', NULL, N'"下" nghĩa là "dưới".'),
(307, 2, 9, 1, N'active', N'Câu chào hỏi buổi sáng là gì?', NULL, N'"おはようございます" là chào buổi sáng.'),
(308, 2, 9, 1, N'active', N'Từ "先生" có nghĩa là?', NULL, N'"Sensei" nghĩa là "thầy/cô giáo".'),
(309, 2, 9, 1, N'active', N'Số "mười" trong tiếng Nhật là gì?', NULL, N'"十" đọc là "juu".'),
(310, 2, 9, 1, N'active', N'Chọn đáp án đúng: わたしは___がすきです。', NULL, N'Chủ ngữ + は + danh từ + が + すきです.'),
(311, 2, 9, 1, N'active', N'Từ "大きい" nghĩa là gì?', NULL, N'"Okii" nghĩa là "to/lớn".'),
(312, 2, 9, 1, N'active', N'Đâu là một ngày trong tuần?', NULL, N'"火曜日" là thứ Ba.'),
(313, 2, 9, 1, N'active', N'Chọn trợ từ đúng: みず___のみます。', NULL, N'Trợ từ "を" chỉ tân ngữ.'),
(314, 2, 9, 1, N'active', N'Chọn câu phủ định đúng:', NULL, N'"じゃありません" là dạng phủ định của "です".'),
(315, 2, 9, 1, N'active', N'Từ nào là danh từ?', NULL, N'"りんご" là quả táo, danh từ.'),
(316, 2, 9, 1, N'active', N'Chọn số đúng: "năm"', NULL, N'"五" là số 5.'),
(317, 2, 9, 1, N'active', N'Từ "友達" nghĩa là gì?', NULL, N'"Tomodachi" là "bạn bè".'),
(318, 2, 9, 1, N'active', N'Đâu là động từ?', NULL, N'"食べます" nghĩa là "ăn".'),
(319, 2, 9, 1, N'active', N'Chọn mẫu câu hỏi đúng:', NULL, N'Đảo ngữ thêm "か" cuối câu để hỏi.'),
(320, 2, 9, 1, N'active', N'Số 4 trong tiếng Nhật là gì?', NULL, N'"四" đọc là "yon" hoặc "shi".'),
(321, 2, 9, 1, N'active', N'Từ "日本" nghĩa là gì?', NULL, N'"Nihon" nghĩa là "Nhật Bản".'),
(322, 2, 9, 1, N'active', N'Chọn trạng từ diễn tả tần suất:', NULL, N'"よく" nghĩa là "thường xuyên".'),
(323, 2, 9, 1, N'active', N'Chọn giới từ chỉ vị trí "trước":', NULL, N'"前" là "trước".'),
(324, 2, 9, 1, N'active', N'Đâu là động từ phủ định?', NULL, N'"しません" là phủ định của "します".'),
(325, 2, 9, 1, N'active', N'Chọn đáp án: きのうは___でしたか。', NULL, N'Dùng "何曜日" để hỏi thứ gì.'),
(326, 2, 9, 1, N'active', N'Chọn trợ từ chỉ chủ đề:', NULL, N'"は" dùng chỉ chủ đề.'),
(327, 2, 9, 1, N'active', N'Đâu là cách nói cảm ơn?', NULL, N'"ありがとう" là cảm ơn.'),
(328, 2, 9, 1, N'active', N'Chọn từ đúng về thời tiết:', NULL, N'"雨" là mưa.'),
(329, 2, 9, 1, N'active', N'Chọn câu đúng về gia đình:', NULL, N'"お父さん" là bố.'),
(330, 2, 9, 1, N'active', N'Từ "駅" nghĩa là gì?', NULL, N'"Eki" là ga tàu.'),
(331, 2, 9, 1, N'active', N'Đâu là màu sắc?', NULL, N'"赤" là màu đỏ.'),
(332, 2, 9, 1, N'active', N'Chọn từ chỉ phương tiện giao thông:', NULL, N'"電車" là tàu điện.'),
(333, 2, 9, 1, N'active', N'Chọn mẫu câu xin lỗi:', NULL, N'"すみません" là xin lỗi.'),
(334, 2, 9, 1, N'active', N'Chọn số đúng: "sáu"', NULL, N'"六" là số 6.'),
(335, 2, 9, 1, N'active', N'Đâu là động từ thể hiện sở thích?', NULL, N'"好きです" là thích.'),
(336, 2, 9, 1, N'active', N'Chọn đáp án: "Tôi là sinh viên."', NULL, N'"私は学生です" là câu đúng.'),
(337, 2, 9, 1, N'active', N'Từ nào là ngày trong tuần?', NULL, N'"日曜日" là chủ nhật.'),
(338, 2, 9, 1, N'active', N'Từ "安い" nghĩa là gì?', NULL, N'"Yasui" nghĩa là "rẻ".'),
(339, 2, 9, 1, N'active', N'Chọn từ chỉ thời gian "bây giờ":', NULL, N'"今" nghĩa là "bây giờ".'),
(400, 4, 11, 2, N'active', N'Chọn nghĩa đúng của "학교":', NULL, N'"학교" nghĩa là "trường học" trong tiếng Hàn.'),
(401, 4, 11, 2, N'active', N'Từ nào là trạng từ?', NULL, N'"빨리" nghĩa là "nhanh", là trạng từ.'),
(402, 4, 11, 2, N'active', N'Chọn câu hỏi đúng về nghề nghiệp:', NULL, N'"무슨 일을 하세요?" là câu hỏi nghề nghiệp lịch sự.'),
(403, 4, 11, 2, N'active', N'Động từ quá khứ của "하다" là gì?', NULL, N'"했다" là quá khứ của "하다" (làm).'),
(404, 4, 11, 2, N'active', N'Chọn mẫu câu đảo ngữ đúng:', NULL, N'Đảo ngữ thường dùng trong câu nhấn mạnh: "저도 공부했어요".'),
(405, 4, 11, 2, N'active', N'Câu nào có nghĩa phủ định?', NULL, N'"가지 않습니다" là phủ định của "가다".'),
(406, 4, 11, 2, N'active', N'Từ nào là tính từ?', NULL, N'"예쁘다" nghĩa là "xinh đẹp".'),
(407, 4, 11, 2, N'active', N'Chọn câu chào đúng vào buổi tối:', NULL, N'"안녕하세요" dùng mọi thời điểm, "안녕히 주무세요" là chúc ngủ ngon.'),
(408, 4, 11, 2, N'active', N'Từ "친구" nghĩa là gì?', NULL, N'"친구" là bạn bè.'),
(409, 4, 11, 2, N'active', N'Chọn đuôi câu lịch sự:', NULL, N'"-습니다" là đuôi câu trang trọng.'),
(410, 4, 11, 2, N'active', N'Từ nào là danh từ?', NULL, N'"책" nghĩa là "sách".'),
(411, 4, 11, 2, N'active', N'Chọn từ chỉ thời gian:', NULL, N'"지금" nghĩa là "bây giờ".'),
(412, 4, 11, 2, N'active', N'Đâu là mẫu câu xin lỗi?', NULL, N'"죄송합니다" là xin lỗi trang trọng.'),
(413, 4, 11, 2, N'active', N'Từ "여름" nghĩa là gì?', NULL, N'"여름" là "mùa hè".'),
(414, 4, 11, 2, N'active', N'Chọn giới từ chỉ vị trí "trước":', NULL, N'"앞" nghĩa là "trước".'),
(415, 4, 11, 2, N'active', N'Từ nào là động từ phủ định?', NULL, N'"안 먹어요" là "không ăn".'),
(416, 4, 11, 2, N'active', N'Đâu là cách nói cảm ơn?', NULL, N'"감사합니다" là cảm ơn trang trọng.'),
(417, 4, 11, 2, N'active', N'Từ "시험" nghĩa là gì?', NULL, N'"시험" là "bài kiểm tra".'),
(418, 4, 11, 2, N'active', N'Chọn trạng từ chỉ tần suất:', NULL, N'"자주" nghĩa là "thường xuyên".'),
(419, 4, 11, 2, N'active', N'Đâu là ngày trong tuần?', NULL, N'"월요일" là thứ hai.'),
(420, 4, 11, 2, N'active', N'Từ "도서관" nghĩa là gì?', NULL, N'"도서관" là "thư viện".'),
(421, 4, 11, 2, N'active', N'Chọn mẫu câu mời lịch sự:', NULL, N'"드시겠어요?" là mời dùng bữa lịch sự.'),
(422, 4, 11, 2, N'active', N'Đâu là đuôi câu thân mật?', NULL, N'"-아/어" là thân mật.'),
(423, 4, 11, 2, N'active', N'Từ "음악" nghĩa là gì?', NULL, N'"음악" là "âm nhạc".'),
(424, 4, 11, 2, N'active', N'Chọn mẫu câu quá khứ:', NULL, N'Quá khứ: "먹었어요" là "đã ăn".'),
(425, 4, 11, 2, N'active', N'Chọn số đúng: "năm"', NULL, N'"오" là số 5.'),
(426, 4, 11, 2, N'active', N'Từ "의사" nghĩa là gì?', NULL, N'"의사" là "bác sĩ".'),
(427, 4, 11, 2, N'active', N'Chọn mẫu câu hỏi bằng tiếng Hàn:', NULL, N'Kết thúc bằng "까?" để hỏi.'),
(428, 4, 11, 2, N'active', N'Chọn mẫu câu phủ định đúng:', NULL, N'"안 + động từ" hoặc "지 않다".'),
(429, 4, 11, 2, N'active', N'Từ "학교" là gì?', NULL, N'"Trường học".'),
(430, 4, 11, 2, N'active', N'Chọn mẫu câu đề nghị:', NULL, N'"-(으)ㅂ시다" là đề nghị cùng làm.'),
(431, 4, 11, 2, N'active', N'Chọn câu dùng kính ngữ:', NULL, N'Dùng đuôi "-십니다" hoặc từ kính ngữ.'),
(432, 4, 11, 2, N'active', N'Đâu là màu sắc?', NULL, N'"파란색" là màu xanh dương.'),
(433, 4, 11, 2, N'active', N'Từ "집" nghĩa là gì?', NULL, N'"집" là "nhà".'),
(434, 4, 11, 2, N'active', N'Đâu là phương tiện giao thông?', NULL, N'"버스" là xe buýt.'),
(435, 4, 11, 2, N'active', N'Chọn mẫu câu xin phép:', NULL, N'"해도 돼요?" là xin phép làm gì.'),
(436, 4, 11, 2, N'active', N'Chọn từ mang nghĩa "rẻ":', NULL, N'"싸다" nghĩa là rẻ.'),
(437, 4, 11, 2, N'active', N'Chọn mẫu câu chỉ ý định:', NULL, N'"-(으)려고 하다" là định làm gì.'),
(438, 4, 11, 2, N'active', N'Đâu là từ chỉ thời gian "hôm nay"?', NULL, N'"오늘" là hôm nay.'),
(500, 5, 13, 1, N'active', N'Từ "公司" nghĩa là gì?', NULL, N'"公司" đọc là "gōngsī", nghĩa là "công ty".'),
(501, 5, 13, 1, N'active', N'Chọn đáp án đúng: "客户" là gì?', NULL, N'"Khách hàng".'),
(502, 5, 13, 1, N'active', N'Từ "合同" dùng để chỉ?', NULL, N'"Hợp đồng".'),
(503, 5, 13, 1, N'active', N'Chọn từ đúng cho "giám đốc":', NULL, N'"经理" là "giám đốc".'),
(504, 5, 13, 1, N'active', N'Chọn mẫu câu hỏi lịch sự:', NULL, N'"请问..." là mở đầu câu hỏi lịch sự.'),
(505, 5, 13, 1, N'active', N'Điền từ: "___会议" nghĩa là họp thường kỳ.', NULL, N'"定期会议" là "cuộc họp định kỳ".'),
(506, 5, 13, 1, N'active', N'Đâu là động từ thương mại?', NULL, N'"签署" là "ký (hợp đồng)".'),
(507, 5, 13, 1, N'active', N'Từ "报价" nghĩa là gì?', NULL, N'"Báo giá".'),
(508, 5, 13, 1, N'active', N'Chọn đáp án đúng: "发票" là gì?', NULL, N'"Hóa đơn".'),
(509, 5, 13, 1, N'active', N'Từ "产品" nghĩa là gì?', NULL, N'"Sản phẩm".'),
(510, 5, 13, 1, N'active', N'Chọn chức danh đúng: "会计" là?', NULL, N'"Kế toán".'),
(511, 5, 13, 1, N'active', N'Chọn mẫu câu chào hỏi đối tác:', NULL, N'"您好！" là chào trang trọng.'),
(512, 5, 13, 1, N'active', N'Chọn từ chỉ phương tiện vận chuyển:', NULL, N'"货车" là "xe tải".'),
(513, 5, 13, 1, N'active', N'Từ "订单" nghĩa là?', NULL, N'"Đơn đặt hàng".'),
(514, 5, 13, 1, N'active', N'Từ "会议" dùng cho?', NULL, N'"Cuộc họp".'),
(515, 5, 13, 1, N'active', N'Từ "折扣" nghĩa là gì?', NULL, N'"Chiết khấu/giảm giá".'),
(516, 5, 13, 1, N'active', N'Từ "付款" nghĩa là gì?', NULL, N'"Thanh toán".'),
(517, 5, 13, 1, N'active', N'Chọn số đúng: "năm"', NULL, N'"五" là số 5.'),
(518, 5, 13, 1, N'active', N'Chọn số đúng: "mười"', NULL, N'"十" là số 10.'),
(519, 5, 13, 1, N'active', N'Chọn mẫu câu yêu cầu gửi báo giá:', NULL, N'"请报价" là "xin báo giá".'),
(520, 5, 13, 1, N'active', N'Chọn từ chỉ bên bán:', NULL, N'"卖方".'),
(521, 5, 13, 1, N'active', N'Chọn từ chỉ bên mua:', NULL, N'"买方".'),
(522, 5, 13, 1, N'active', N'Chọn từ chỉ "nhà cung cấp":', NULL, N'"供应商".'),
(523, 5, 13, 1, N'active', N'Chọn từ chỉ "hàng tồn kho":', NULL, N'"库存".'),
(524, 5, 13, 1, N'active', N'Từ "合同" dịch là?', NULL, N'"Hợp đồng".'),
(525, 5, 13, 1, N'active', N'Điền từ đúng: "___付款" nghĩa là trả trước.', NULL, N'"预付款" là "trả trước".'),
(526, 5, 13, 1, N'active', N'Chọn đáp án đúng: "市场" là gì?', NULL, N'"Thị trường".'),
(527, 5, 13, 1, N'active', N'Từ "价格" nghĩa là?', NULL, N'"Giá cả".'),
(528, 5, 13, 1, N'active', N'Chọn đáp án đúng: "签名" là gì?', NULL, N'"Ký tên".'),
(529, 5, 13, 1, N'active', N'Chọn đáp án đúng: "电话" là gì?', NULL, N'"Điện thoại".'),
(530, 5, 13, 1, N'active', N'Chọn từ chỉ "tài khoản":', NULL, N'"账户".'),
(531, 5, 13, 1, N'active', N'Chọn đáp án: "办公室" là?', NULL, N'"Văn phòng".'),
(532, 5, 13, 1, N'active', N'Chọn đáp án: "经理助理" là?', NULL, N'"Trợ lý giám đốc".'),
(533, 5, 13, 1, N'active', N'Chọn đáp án: "会议纪要" là?', NULL, N'"Biên bản cuộc họp".'),
(534, 5, 13, 1, N'active', N'Chọn đáp án: "邮件" là?', NULL, N'"Thư điện tử".'),
(535, 5, 13, 1, N'active', N'Chọn đáp án: "出差" là?', NULL, N'"Đi công tác".'),
(536, 5, 13, 1, N'active', N'Chọn từ chỉ phòng họp:', NULL, N'"会议室".'),
(537, 5, 13, 1, N'active', N'Chọn đáp án: "项目" là?', NULL, N'"Dự án".'),
(538, 5, 13, 1, N'active', N'Chọn đáp án: "签订合同" là?', NULL, N'"Ký kết hợp đồng".'),
(539, 5, 13, 1, N'active', N'Chọn đáp án: "收据" là?', NULL, N'"Biên lai".');
SET IDENTITY_INSERT questions OFF;

--Dữ liệu mẫu cho answer_options(part1)
SET IDENTITY_INSERT answer_options ON;
INSERT INTO answer_options (id, question_id, content, is_correct, order_number) VALUES
-- Question 1
(1, 1, N'I am fine, thank you.', 1, 1), (2, 1, N'I fine am, thank you.', 0, 2), (3, 1, N'Thank you, I am fine.', 0, 3), (4, 1, N'I fine, thank you.', 0, 4),
-- Question 2
(5, 2, N'Apples', 0, 1), (6, 2, N'Books', 0, 2), (7, 2, N'Apple', 1, 3), (8, 2, N'Appleses', 0, 4),
-- Question 3
(9, 3, N'She is a doctor.', 0, 1), (10, 3, N'He go to school every day.', 1, 2), (11, 3, N'They are playing football.', 0, 3), (12, 3, N'He goes to school every day.', 0, 4),
-- Question 4
(13, 4, N'Lazy', 0, 1), (14, 4, N'Careless', 0, 2), (15, 4, N'Hard-working', 1, 3), (16, 4, N'Diligently', 0, 4),
-- Question 5
(17, 5, N'is', 1, 1), (18, 5, N'are', 0, 2), (19, 5, N'am', 0, 3), (20, 5, N'be', 0, 4),
-- Question 6
(21, 6, N'He', 0, 1), (22, 6, N'It', 1, 2), (23, 6, N'She', 0, 3), (24, 6, N'They', 0, 4),
-- Question 7
(25, 7, N'book', 0, 1), (26, 7, N'books', 1, 2), (27, 7, N'booking', 0, 3), (28, 7, N'bookies', 0, 4),
-- Question 8
(29, 8, N'is', 0, 1), (30, 8, N'are', 1, 2), (31, 8, N'am', 0, 3), (32, 8, N'be', 0, 4),
-- Question 9
(33, 9, N'How', 0, 1), (34, 9, N'What', 1, 2), (35, 9, N'Who', 0, 3), (36, 9, N'Where', 0, 4),
-- Question 10
(37, 10, N'in', 0, 1), (38, 10, N'on', 0, 2), (39, 10, N'at', 1, 3), (40, 10, N'by', 0, 4),
-- Question 11
(41, 11, N'speak', 1, 1), (42, 11, N'speaks', 0, 2), (43, 11, N'speaking', 0, 3), (44, 11, N'spoke', 0, 4),
-- Question 12
(45, 12, N'blue', 1, 1), (46, 12, N'chair', 0, 2), (47, 12, N'run', 0, 3), (48, 12, N'desk', 0, 4),
-- Question 13
(49, 13, N'go', 0, 1), (50, 13, N'goes', 1, 2), (51, 13, N'went', 0, 3), (52, 13, N'going', 0, 4),
-- Question 14
(53, 14, N'Table', 0, 1), (54, 14, N'Chair', 0, 2), (55, 14, N'Happy', 1, 3), (56, 14, N'Desk', 0, 4),
-- Question 15
(57, 15, N'Five', 0, 1), (58, 15, N'Seven', 1, 2), (59, 15, N'Ten', 0, 3), (60, 15, N'Six', 0, 4),
-- Question 16
(61, 16, N'April', 0, 1), (62, 16, N'July', 0, 2), (63, 16, N'June', 1, 3), (64, 16, N'May', 0, 4),
-- Question 17
(65, 17, N'doctor', 0, 1), (66, 17, N'student', 1, 2), (67, 17, N'engineer', 0, 3), (68, 17, N'teacher', 0, 4),
-- Question 18
(69, 18, N'cold', 1, 1), (70, 18, N'warm', 0, 2), (71, 18, N'cool', 0, 3), (72, 18, N'hot', 0, 4),
-- Question 19
(73, 19, N'some', 1, 1), (74, 19, N'any', 0, 2), (75, 19, N'a', 0, 3), (76, 19, N'the', 0, 4),
-- Question 20
(77, 20, N'so', 0, 1), (78, 20, N'because', 1, 2), (79, 20, N'although', 0, 3), (80, 20, N'therefore', 0, 4),
-- Question 21
(81, 21, N'in', 0, 1), (82, 21, N'at', 0, 2), (83, 21, N'by', 1, 3), (84, 21, N'on', 0, 4),
-- Question 22
(85, 22, N'mandatorily', 0, 1), (86, 22, N'mandatory', 1, 2), (87, 22, N'mandate', 0, 3), (88, 22, N'mandated', 0, 4),
-- Question 23
(89, 23, N'rely', 0, 1), (90, 23, N'reliable', 1, 2), (91, 23, N'reliability', 0, 3), (92, 23, N'reliably', 0, 4),
-- Question 24
(93, 24, N'personnel', 1, 1), (94, 24, N'personal', 0, 2), (95, 24, N'personally', 0, 3), (96, 24, N'person', 0, 4),
-- Question 25
(97, 25, N'Although', 0, 1), (98, 25, N'Because', 0, 2), (99, 25, N'Due to', 1, 3), (100, 25, N'However', 0, 4),
-- Question 26
(101, 26, N'grow', 0, 1), (102, 26, N'grown', 0, 2), (103, 26, N'growing', 0, 3), (104, 26, N'growth', 1, 4),
-- Question 27
(105, 27, N'manage', 0, 1), (106, 27, N'managing', 1, 2), (107, 27, N'managed', 0, 3), (108, 27, N'management', 0, 4),
-- Question 28
(109, 28, N'he did finish', 0, 1), (110, 28, N'did he finish', 1, 2), (111, 28, N'he finished', 0, 3), (112, 28, N'finished he', 0, 4),
-- Question 29
(113, 29, N'professional', 0, 1), (114, 29, N'professionally', 0, 2), (115, 29, N'professionalism', 1, 3), (116, 29, N'profess', 0, 4),
-- Question 30
(117, 30, N'return', 1, 1), (118, 30, N'returns', 0, 2), (119, 30, N'returning', 0, 3), (120, 30, N'returned', 0, 4),
-- Question 31
(121, 31, N'convenience', 0, 1), (122, 31, N'convenient', 0, 2), (123, 31, N'conveniently', 1, 3), (124, 31, N'convene', 0, 4),
-- Question 32
(125, 32, N'Although', 0, 1), (126, 32, N'Despite', 1, 2), (127, 32, N'However', 0, 3), (128, 32, N'Because', 0, 4),
-- Question 33
(129, 33, N'diligent', 0, 1), (130, 33, N'diligence', 0, 2), (131, 33, N'diligently', 1, 3), (132, 33, N'more diligent', 0, 4),
-- Question 34
(133, 34, N'in', 0, 1), (134, 34, N'on', 1, 2), (135, 34, N'with', 0, 3), (136, 34, N'at', 0, 4),
-- Question 35
(137, 35, N'a', 1, 1), (138, 35, N'i', 0, 2), (139, 35, N'u', 0, 3), (140, 35, N'e', 0, 4),
-- Question 36
(141, 36, N'Nước', 1, 1), (142, 36, N'Gạo', 0, 2), (143, 36, N'Lửa', 0, 3), (144, 36, N'Gió', 0, 4),
-- Question 37
(145, 37, N'私は田中です', 1, 1), (146, 37, N'田中はわたし', 0, 2), (147, 37, N'わたしが田中です', 0, 3), (148, 37, N'田中ですわたし', 0, 4),
-- Question 38
(149, 38, N'빠르게', 1, 1), (150, 38, N'빠른', 0, 2), (151, 38, N'빠르다', 0, 3), (152, 38, N'빠름', 0, 4),
-- Question 39
(153, 39, N'배우고 싶어요', 1, 1), (154, 39, N'배우다 싶어요', 0, 2), (155, 39, N'배우어 싶어요', 0, 3), (156, 39, N'배우고 싶다요', 0, 4),
-- Question 40
(157, 40, N'Hợp đồng', 1, 1), (158, 40, N'Hóa đơn', 0, 2), (159, 40, N'Đơn hàng', 0, 3), (160, 40, N'Thanh toán', 0, 4),
-- Question 41
(161, 41, N'您好！', 1, 1), (162, 41, N'你好吗？', 0, 2), (163, 41, N'再见！', 0, 3), (164, 41, N'请问', 0, 4),
-- Question 42
(165, 42, N'is', 1, 1), (166, 42, N'are', 0, 2), (167, 42, N'am', 0, 3), (168, 42, N'be', 0, 4),
-- Question 43
(169, 43, N'table', 1, 1), (170, 43, N'run', 0, 2), (171, 43, N'blue', 0, 3), (172, 43, N'quickly', 0, 4),
-- Question 44
(173, 44, N'go', 1, 1), (174, 44, N'goes', 0, 2), (175, 44, N'going', 0, 3), (176, 44, N'gone', 0, 4),
-- Question 45
(177, 45, N'Where', 1, 1), (178, 45, N'What', 0, 2), (179, 45, N'How', 0, 3), (180, 45, N'Who', 0, 4),
-- Question 46
(181, 46, N'buses', 1, 1), (182, 46, N'bus', 0, 2), (183, 46, N'buss', 0, 3), (184, 46, N'buse', 0, 4),
-- Question 47
(185, 47, N'Blue', 1, 1), (186, 47, N'Green', 0, 2), (187, 47, N'Red', 0, 3), (188, 47, N'Yellow', 0, 4),
-- Question 48
(189, 48, N'4', 1, 1), (190, 48, N'2', 0, 2), (191, 48, N'3', 0, 3), (192, 48, N'5', 0, 4),
-- Question 49
(193, 49, N'on', 1, 1), (194, 49, N'under', 0, 2), (195, 49, N'next', 0, 3), (196, 49, N'between', 0, 4),
-- Question 50
(197, 50, N'Good morning', 1, 1), (198, 50, N'Good night', 0, 2), (199, 50, N'Goodbye', 0, 3), (200, 50, N'See you', 0, 4),
-- Question 51
(201, 51, N'old', 1, 1), (202, 51, N'tall', 0, 2), (203, 51, N'small', 0, 3), (204, 51, N'big', 0, 4),
-- Question 52
(205, 52, N'He like apples.', 0, 1), (206, 52, N'He likes apples.', 1, 2), (207, 52, N'He liking apples.', 0, 3), (208, 52, N'He liked apples.', 0, 4),
-- Question 53
(209, 53, N'Car', 1, 1), (210, 53, N'Apple', 0, 2), (211, 53, N'Banana', 0, 3), (212, 53, N'Mango', 0, 4),
-- Question 54
(213, 54, N'like', 1, 1), (214, 54, N'likes', 0, 2), (215, 54, N'liked', 0, 3), (216, 54, N'liking', 0, 4),
-- Question 55
(217, 55, N'Beautiful', 1, 1), (218, 55, N'Book', 0, 2), (219, 55, N'Run', 0, 3), (220, 55, N'Chair', 0, 4),
-- Question 56
(221, 56, N'Hà Nội', 1, 1), (222, 56, N'Sài Gòn', 0, 2), (223, 56, N'Đà Nẵng', 0, 3), (224, 56, N'Hải Phòng', 0, 4),
-- Question 57
(225, 57, N'does not', 1, 1), (226, 57, N'do not', 0, 2), (227, 57, N'is not', 0, 3), (228, 57, N'are not', 0, 4),
-- Question 58
(229, 58, N'is', 1, 1), (230, 58, N'am', 0, 2), (231, 58, N'are', 0, 3), (232, 58, N'was', 0, 4),
-- Question 59
(233, 59, N'Who', 1, 1), (234, 59, N'Car', 0, 2), (235, 59, N'Chair', 0, 3), (236, 59, N'Walk', 0, 4),
-- Question 60
(237, 60, N'do', 1, 1), (238, 60, N'does', 0, 2), (239, 60, N'did', 0, 3), (240, 60, N'done', 0, 4),
-- Question 61
(241, 61, N'I visited my grandmother yesterday.', 1, 1), (242, 61, N'I visit my grandmother.', 0, 2), (243, 61, N'I am visiting my grandmother.', 0, 3), (244, 61, N'I will visit my grandmother.', 0, 4),
-- Question 62
(245, 62, N'Glad', 1, 1), (246, 62, N'Sad', 0, 2), (247, 62, N'Angry', 0, 3), (248, 62, N'Tired', 0, 4),
-- Question 63
(249, 63, N'You’re welcome.', 1, 1), (250, 63, N'No, thanks.', 0, 2), (251, 63, N'See you.', 0, 3), (252, 63, N'Bye.', 0, 4),
-- Question 64
(253, 64, N'are', 1, 1), (254, 64, N'is', 0, 2), (255, 64, N'was', 0, 3), (256, 64, N'were', 0, 4),
-- Question 65
(257, 65, N'dog', 1, 1), (258, 65, N'red', 0, 2), (259, 65, N'green', 0, 3), (260, 65, N'yellow', 0, 4),
-- Question 66
(261, 66, N'they', 1, 1), (262, 66, N'run', 0, 2), (263, 66, N'chair', 0, 3), (264, 66, N'house', 0, 4),
-- Question 67
(265, 67, N'was', 1, 1), (266, 67, N'am', 0, 2), (267, 67, N'were', 0, 3), (268, 67, N'is', 0, 4),
-- Question 68
(269, 68, N'went', 1, 1), (270, 68, N'goed', 0, 2), (271, 68, N'goes', 0, 3), (272, 68, N'go', 0, 4),
-- Question 69
(273, 69, N'taller', 1, 1), (274, 69, N'tallest', 0, 2), (275, 69, N'more tall', 0, 3), (276, 69, N'tall', 0, 4),
-- Question 70
(277, 70, N'Sunday', 1, 1), (278, 70, N'January', 0, 2), (279, 70, N'Winter', 0, 3), (280, 70, N'Blue', 0, 4),
-- Question 71
(281, 71, N'is', 1, 1), (282, 71, N'are', 0, 2), (283, 71, N'was', 0, 3), (284, 71, N'be', 0, 4),
-- Question 72
(285, 72, N'yellow', 1, 1), (286, 72, N'green', 0, 2), (287, 72, N'blue', 0, 3), (288, 72, N'red', 0, 4),
-- Question 73
(289, 73, N'watches', 1, 1), (290, 73, N'watch', 0, 2), (291, 73, N'watched', 0, 3), (292, 73, N'watching', 0, 4),
-- Question 74
(293, 74, N'What is', 1, 1), (294, 74, N'Who is', 0, 2), (295, 74, N'Where is', 0, 3), (296, 74, N'Which is', 0, 4),
-- Question 75
(297, 75, N'Bicycle', 1, 1), (298, 75, N'Book', 0, 2), (299, 75, N'Pen', 0, 3), (300, 75, N'Chair', 0, 4),
-- Question 76
(301, 76, N'Large', 1, 1), (302, 76, N'Small', 0, 2), (303, 76, N'Tiny', 0, 3), (304, 76, N'Short', 0, 4),
-- Question 77
(305, 77, N'easy', 1, 1), (306, 77, N'bad', 0, 2), (307, 77, N'hard', 0, 3), (308, 77, N'long', 0, 4),
-- Question 78
(309, 78, N'Pen', 1, 1), (310, 78, N'Blue', 0, 2), (311, 78, N'Yellow', 0, 3), (312, 78, N'Red', 0, 4),
-- Question 79
(313, 79, N'Beautiful', 1, 1), (314, 79, N'Beutiful', 0, 2), (315, 79, N'Beautifull', 0, 3), (316, 79, N'Beautful', 0, 4),
-- Question 80
(317, 80, N'drink', 1, 1), (318, 80, N'eat', 0, 2), (319, 80, N'play', 0, 3), (320, 80, N'write', 0, 4),
-- Question 81
(321, 81, N'Cow', 1, 1), (322, 81, N'Tiger', 0, 2), (323, 81, N'Dog', 0, 3), (324, 81, N'Cat', 0, 4),
-- Question 100
(325, 100, N'I have finished my homework.', 1, 1), (326, 100, N'I finish my homework.', 0, 2), (327, 100, N'I am finishing my homework.', 0, 3), (328, 100, N'I finished my homework.', 0, 4),
-- Question 101
(329, 101, N'rains', 1, 1), (330, 101, N'will rain', 0, 2), (331, 101, N'raining', 0, 3), (332, 101, N'rain', 0, 4),
-- Question 102
(333, 102, N'was made', 1, 1), (334, 102, N'made', 0, 2), (335, 102, N'makes', 0, 3), (336, 102, N'make', 0, 4),
-- Question 103
(337, 103, N'fast', 1, 1), (338, 103, N'slow', 0, 2), (339, 103, N'lazy', 0, 3), (340, 103, N'happy', 0, 4),
-- Question 104
(341, 104, N'apple', 1, 1), (342, 104, N'milk', 0, 2), (343, 104, N'sugar', 0, 3), (344, 104, N'water', 0, 4),
-- Question 105
(345, 105, N'since', 1, 1), (346, 105, N'for', 0, 2), (347, 105, N'at', 0, 3), (348, 105, N'in', 0, 4),
-- Question 106
(349, 106, N'better', 1, 1), (350, 106, N'more good', 0, 2), (351, 106, N'gooder', 0, 3), (352, 106, N'best', 0, 4),
-- Question 107
(353, 107, N'He said he was fine.', 1, 1), (354, 107, N'He said he is fine.', 0, 2), (355, 107, N'He said I am fine.', 0, 3), (356, 107, N'He says he was fine.', 0, 4),
-- Question 108
(357, 108, N'can', 1, 1), (358, 108, N'do', 0, 2), (359, 108, N'did', 0, 3), (360, 108, N'are', 0, 4),
-- Question 109
(361, 109, N'hearing', 1, 1), (362, 109, N'hear', 0, 2), (363, 109, N'heard', 0, 3), (364, 109, N'being heard', 0, 4),
-- Question 110
(365, 110, N'the smallest', 1, 1), (366, 110, N'smaller', 0, 2), (367, 110, N'small', 0, 3), (368, 110, N'more small', 0, 4),
-- Question 111
(369, 111, N'She always goes to school early.', 1, 1), (370, 111, N'Always she goes to school early.', 0, 2), (371, 111, N'She goes always to school early.', 0, 3), (372, 111, N'She early goes to school always.', 0, 4),
-- Question 112
(373, 112, N'went', 1, 1), (374, 112, N'go', 0, 2), (375, 112, N'goes', 0, 3), (376, 112, N'gone', 0, 4),
-- Question 113
(377, 113, N'-ful', 1, 1), (378, 113, N'-ness', 0, 2), (379, 113, N'-ly', 0, 3), (380, 113, N'-ment', 0, 4),
-- Question 114
(381, 114, N'in', 1, 1), (382, 114, N'at', 0, 2), (383, 114, N'by', 0, 3), (384, 114, N'with', 0, 4),
-- Question 115
(385, 115, N'children', 1, 1), (386, 115, N'childs', 0, 2), (387, 115, N'childes', 0, 3), (388, 115, N'child', 0, 4),
-- Question 116
(389, 116, N'If I were you, I would study hard.', 1, 1), (390, 116, N'If it will rain, we will stay at home.', 0, 2), (391, 116, N'If she had known, she would come.', 0, 3), (392, 116, N'If I study, I will pass.', 0, 4),
-- Question 117
(393, 117, N'hiếm khi', 1, 1), (394, 117, N'thường xuyên', 0, 2), (395, 117, N'luôn luôn', 0, 3), (396, 117, N'thỉnh thoảng', 0, 4),
-- Question 118
(397, 118, N'hard', 1, 1), (398, 118, N'short', 0, 2), (399, 118, N'funny', 0, 3), (400, 118, N'soft', 0, 4),
-- Question 119
(401, 119, N'aren''t you', 1, 1), (402, 119, N'do you', 0, 2), (403, 119, N'will you', 0, 3), (404, 119, N'shall you', 0, 4),
-- Question 120
(405, 120, N'She studies English every [day].', 1, 1), (406, 120, N'She every day studies English.', 0, 2), (407, 120, N'English studies she every day.', 0, 3), (408, 120, N'Studies she English every day.', 0, 4),
-- Question 121
(409, 121, N'is', 1, 1), (410, 121, N'are', 0, 2), (411, 121, N'was', 0, 3), (412, 121, N'were', 0, 4),
-- Question 122
(413, 122, N'written', 1, 1), (414, 122, N'wrote', 0, 2), (415, 122, N'writing', 0, 3), (416, 122, N'writes', 0, 4),
-- Question 123
(417, 123, N'under', 1, 1), (418, 123, N'blue', 0, 2), (419, 123, N'tree', 0, 3), (420, 123, N'fast', 0, 4),
-- Question 124
(421, 124, N'eats', 1, 1), (422, 124, N'eat', 0, 2), (423, 124, N'eating', 0, 3), (424, 124, N'ate', 0, 4),
-- Question 125
(425, 125, N'pretty', 1, 1), (426, 125, N'strong', 0, 2), (427, 125, N'weak', 0, 3), (428, 125, N'cold', 0, 4),
-- Question 126
(429, 126, N'are', 1, 1), (430, 126, N'is', 0, 2), (431, 126, N'was', 0, 3), (432, 126, N'be', 0, 4),
-- Question 127
(433, 127, N'[When]', 1, 1), (434, 127, N'Car', 0, 2), (435, 127, N'Pen', 0, 3), (436, 127, N'Chair', 0, 4),
-- Question 128
(437, 128, N'were', 1, 1), (438, 128, N'was', 0, 2), (439, 128, N'are', 0, 3), (440, 128, N'is', 0, 4),
-- Question 129
(441, 129, N'expensive', 1, 1), (442, 129, N'short', 0, 2), (443, 129, N'young', 0, 3), (444, 129, N'long', 0, 4),
-- Question 130
(445, 130, N'Have', 1, 1), (446, 130, N'Has', 0, 2), (447, 130, N'Do', 0, 3), (448, 130, N'Are', 0, 4),
-- Question 131
(449, 131, N'was watching', 1, 1), (450, 131, N'watched', 0, 2), (451, 131, N'watch', 0, 3), (452, 131, N'watches', 0, 4),
-- Question 132
(453, 132, N'Email', 1, 1), (454, 132, N'Apple', 0, 2), (455, 132, N'Chair', 0, 3), (456, 132, N'Book', 0, 4),
-- Question 133
(457, 133, N'speaks', 1, 1), (458, 133, N'speak', 0, 2), (459, 133, N'speaking', 0, 3), (460, 133, N'spoke', 0, 4),
-- Question 134
(461, 134, N'necessary', 1, 1), (462, 134, N'neccessary', 0, 2), (463, 134, N'necesary', 0, 3), (464, 134, N'neccessery', 0, 4),
-- Question 135
(465, 135, N'sheep', 1, 1), (466, 135, N'tiger', 0, 2), (467, 135, N'lion', 0, 3), (468, 135, N'dog', 0, 4),
-- Question 136
(469, 136, N'an', 1, 1), (470, 136, N'a', 0, 2), (471, 136, N'the', 0, 3), (472, 136, N'one', 0, 4),
-- Question 137
(473, 137, N'goes', 1, 1), (474, 137, N'go', 0, 2), (475, 137, N'going', 0, 3), (476, 137, N'gone', 0, 4),
-- Question 138
(477, 138, N'begin', 1, 1), (478, 138, N'end', 0, 2), (479, 138, N'close', 0, 3), (480, 138, N'finish', 0, 4),
-- Question 200
(481, 200, N'starts', 1, 1), (482, 200, N'start', 0, 2), (483, 200, N'started', 0, 3), (484, 200, N'starting', 0, 4),
-- Question 201
(485, 201, N'The package is delivered every day.', 1, 1), (486, 201, N'Deliver the package every day.', 0, 2), (487, 201, N'The package delivers every day.', 0, 3), (488, 201, N'Every day is delivered the package.', 0, 4),
-- Question 202
(489, 202, N'quickly', 1, 1), (490, 202, N'quick', 0, 2), (491, 202, N'quicker', 0, 3), (492, 202, N'quickest', 0, 4),
-- Question 203
(493, 203, N'hasn’t', 1, 1), (494, 203, N'doesn’t', 0, 2), (495, 203, N'haven’t', 0, 3), (496, 203, N'isn’t', 0, 4),
-- Question 204
(497, 204, N'for', 1, 1), (498, 204, N'on', 0, 2), (499, 204, N'with', 0, 3), (500, 204, N'to', 0, 4),
-- Question 205
(501, 205, N'need', 1, 1), (502, 205, N'give', 0, 2), (503, 205, N'send', 0, 3), (504, 205, N'read', 0, 4),
-- Question 206
(505, 206, N'If it rains, I will stay at home.', 1, 1), (506, 206, N'If I were you, I would stay at home.', 0, 2), (507, 206, N'If she had known, she would have stayed.', 0, 3), (508, 206, N'If I stay, I would be happy.', 0, 4),
-- Question 207
(509, 207, N'the earliest', 1, 1), (510, 207, N'earliest', 0, 2), (511, 207, N'more early', 0, 3), (512, 207, N'most early', 0, 4),
-- Question 208
(513, 208, N'Have', 1, 1), (514, 208, N'Has', 0, 2), (515, 208, N'Did', 0, 3), (516, 208, N'Are', 0, 4),
-- Question 209
(517, 209, N'Best regards', 1, 1), (518, 209, N'Bye bye', 0, 2), (519, 209, N'See you', 0, 3), (520, 209, N'Good luck', 0, 4),
-- Question 210
(521, 210, N'was reviewed', 1, 1), (522, 210, N'were reviewed', 0, 2), (523, 210, N'is reviewed', 0, 3), (524, 210, N'has reviewed', 0, 4),
-- Question 211
(525, 211, N'although', 1, 1), (526, 211, N'happy', 0, 2), (527, 211, N'careful', 0, 3), (528, 211, N'office', 0, 4),
-- Question 212
(529, 212, N'will have been working', 1, 1), (530, 212, N'will work', 0, 2), (531, 212, N'worked', 0, 3), (532, 212, N'am working', 0, 4),
-- Question 213
(533, 213, N'hearing', 1, 1), (534, 213, N'hear', 0, 2), (535, 213, N'heard', 0, 3), (536, 213, N'be heard', 0, 4),
-- Question 214
(537, 214, N'picked', 0, 1), (538, 214, N'pick up', 1, 2), (539, 214, N'pick', 0, 3), (540, 214, N'picking', 0, 4),
-- Question 215
(541, 215, N'decrease', 1, 1), (542, 215, N'improve', 0, 2), (543, 215, N'grow', 0, 3), (544, 215, N'rise', 0, 4),
-- Question 216
(545, 216, N'was prepared', 1, 1), (546, 216, N'were prepared', 0, 2), (547, 216, N'is prepared', 0, 3), (548, 216, N'has prepared', 0, 4),
-- Question 217
(549, 217, N'expiration', 1, 1), (550, 217, N'expire', 0, 2), (551, 217, N'expired', 0, 3), (552, 217, N'expiring', 0, 4),
-- Question 218
(553, 218, N'should', 1, 1), (554, 218, N'see', 0, 2), (555, 218, N'was', 0, 3), (556, 218, N'give', 0, 4),
-- Question 219
(557, 219, N'had', 1, 1), (558, 219, N'have', 0, 2), (559, 219, N'has', 0, 3), (560, 219, N'will have', 0, 4),
-- Question 220
(561, 220, N'had', 1, 1), (562, 220, N'was', 0, 2), (563, 220, N'is', 0, 3), (564, 220, N'were', 0, 4),
-- Question 221
(565, 221, N'buy', 1, 1), (566, 221, N'sell', 0, 2), (567, 221, N'give', 0, 3), (568, 221, N'start', 0, 4),
-- Question 222
(569, 222, N'The documents have been sent.', 1, 1), (570, 222, N'The documents has been sent.', 0, 2), (571, 222, N'The documents is sent.', 0, 3), (572, 222, N'The documents will sends.', 0, 4),
-- Question 223
(573, 223, N'taller', 1, 1), (574, 223, N'tall', 0, 2), (575, 223, N'tallest', 0, 3), (576, 223, N'more tall', 0, 4),
-- Question 224
(577, 224, N'analyses', 1, 1), (578, 224, N'analysis', 0, 2), (579, 224, N'analysises', 0, 3), (580, 224, N'analys', 0, 4),
-- Question 225
(581, 225, N'in', 1, 1), (582, 225, N'on', 0, 2), (583, 225, N'at', 0, 3), (584, 225, N'for', 0, 4),
-- Question 226
(585, 226, N'right off the bat', 1, 1), (586, 226, N'under the weather', 0, 2), (587, 226, N'call it a day', 0, 3), (588, 226, N'up in the air', 0, 4),
-- Question 227
(589, 227, N'board', 1, 1), (590, 227, N'room', 0, 2), (591, 227, N'meeting', 0, 3), (592, 227, N'project', 0, 4),
-- Question 228
(593, 228, N'fasten', 1, 1), (594, 228, N'open', 0, 2), (595, 228, N'close', 0, 3), (596, 228, N'push', 0, 4),
-- Question 229
(597, 229, N'information', 1, 1), (598, 229, N'apple', 0, 2), (599, 229, N'pen', 0, 3), (600, 229, N'book', 0, 4),
-- Question 230
(601, 230, N'came', 1, 1), (602, 230, N'comes', 0, 2), (603, 230, N'coming', 0, 3), (604, 230, N'come', 0, 4),
-- Question 231
(605, 231, N'Would', 1, 1), (606, 231, N'Will', 0, 2), (607, 231, N'Do', 0, 3), (608, 231, N'Have', 0, 4),
-- Question 232
(609, 232, N'enhance', 1, 1), (610, 232, N'worsen', 0, 2), (611, 232, N'forget', 0, 3), (612, 232, N'leave', 0, 4),
-- Question 233
(613, 233, N'in', 1, 1), (614, 233, N'on', 0, 2), (615, 233, N'at', 0, 3), (616, 233, N'for', 0, 4),
-- Question 234
(617, 234, N'getting', 1, 1), (618, 234, N'get', 0, 2), (619, 234, N'got', 0, 3), (620, 234, N'gets', 0, 4),
-- Question 235
(621, 235, N'has been completed', 1, 1), (622, 235, N'was completed', 0, 2), (623, 235, N'is completing', 0, 3), (624, 235, N'will complete', 0, 4),
-- Question 236
(625, 236, N'May I speak to Mr. Smith?', 1, 1), (626, 236, N'Give me Smith.', 0, 2), (627, 236, N'Smith phone now.', 0, 3), (628, 236, N'Where is Smith?', 0, 4),
-- Question 237
(629, 237, N'will be announced', 1, 1), (630, 237, N'has been announced', 0, 2), (631, 237, N'is announced', 0, 3), (632, 237, N'was announced', 0, 4),
-- Question 238
(633, 238, N'help', 1, 1), (634, 238, N'play', 0, 2), (635, 238, N'work', 0, 3), (636, 238, N'run', 0, 4),
-- Question 300
(637, 300, N'か', 1, 1), (638, 300, N'き', 0, 2), (639, 300, N'く', 0, 3), (640, 300, N'け', 0, 4),
-- Question 301
(641, 301, N'Nước', 1, 1), (642, 301, N'Gạo', 0, 2), (643, 301, N'Lửa', 0, 3), (644, 301, N'Cá', 0, 4),
-- Question 302
(645, 302, N'勉強します', 1, 1), (646, 302, N'先生です', 0, 2), (647, 302, N'大きいです', 0, 3), (648, 302, N'七', 0, 4),
-- Question 303
(649, 303, N'七', 1, 1), (650, 303, N'五', 0, 2), (651, 303, N'四', 0, 3), (652, 303, N'三', 0, 4),
-- Question 304
(653, 304, N'私は学生です', 1, 1), (654, 304, N'学生は私です', 0, 2), (655, 304, N'学生です私は', 0, 3), (656, 304, N'私学生です', 0, 4),
-- Question 305
(657, 305, N'高い', 1, 1), (658, 305, N'先生', 0, 2), (659, 305, N'犬', 0, 3), (660, 305, N'水', 0, 4),
-- Question 306
(661, 306, N'下', 1, 1), (662, 306, N'上', 0, 2), (663, 306, N'前', 0, 3), (664, 306, N'後ろ', 0, 4),
-- Question 307
(665, 307, N'おはようございます', 1, 1), (666, 307, N'こんばんは', 0, 2), (667, 307, N'こんにちは', 0, 3), (668, 307, N'さようなら', 0, 4),
-- Question 308
(669, 308, N'Giáo viên', 1, 1), (670, 308, N'Học sinh', 0, 2), (671, 308, N'Nhân viên', 0, 3), (672, 308, N'Bác sĩ', 0, 4),
-- Question 309
(673, 309, N'十', 1, 1), (674, 309, N'一', 0, 2), (675, 309, N'九', 0, 3), (676, 309, N'八', 0, 4),
-- Question 310
(677, 310, N'りんご', 1, 1), (678, 310, N'犬', 0, 2), (679, 310, N'水', 0, 3), (680, 310, N'本', 0, 4),
-- Question 311
(681, 311, N'Lớn', 1, 1), (682, 311, N'Nhỏ', 0, 2), (683, 311, N'Đẹp', 0, 3), (684, 311, N'Già', 0, 4),
-- Question 312
(685, 312, N'火曜日', 1, 1), (686, 312, N'六月', 0, 2), (687, 312, N'先生', 0, 3), (688, 312, N'山', 0, 4),
-- Question 313
(689, 313, N'を', 1, 1), (690, 313, N'に', 0, 2), (691, 313, N'で', 0, 3), (692, 313, N'から', 0, 4),
-- Question 314
(693, 314, N'じゃありません', 1, 1), (694, 314, N'です', 0, 2), (695, 314, N'ました', 0, 3), (696, 314, N'ましょう', 0, 4),
-- Question 315
(697, 315, N'りんご', 1, 1), (698, 315, N'大きい', 0, 2), (699, 315, N'食べます', 0, 3), (700, 315, N'赤い', 0, 4),
-- Question 316
(701, 316, N'五', 1, 1), (702, 316, N'三', 0, 2), (703, 316, N'八', 0, 3), (704, 316, N'七', 0, 4),
-- Question 317
(705, 317, N'Bạn bè', 1, 1), (706, 317, N'Mẹ', 0, 2), (707, 317, N'Công việc', 0, 3), (708, 317, N'Nhà', 0, 4),
-- Question 318
(709, 318, N'食べます', 1, 1), (710, 318, N'高い', 0, 2), (711, 318, N'先生', 0, 3), (712, 318, N'水', 0, 4),
-- Question 319
(713, 319, N'あなたは学生ですか', 1, 1), (714, 319, N'学生あなたはですか', 0, 2), (715, 319, N'あなた学生ですか', 0, 3), (716, 319, N'学生かあなたはです', 0, 4),
-- Question 320
(717, 320, N'四', 1, 1), (718, 320, N'三', 0, 2), (719, 320, N'五', 0, 3), (720, 320, N'六', 0, 4),
-- Question 321
(721, 321, N'Nhật Bản', 1, 1), (722, 321, N'Mỹ', 0, 2), (723, 321, N'Việt Nam', 0, 3), (724, 321, N'Pháp', 0, 4),
-- Question 322
(725, 322, N'よく', 1, 1), (726, 322, N'犬', 0, 2), (727, 322, N'先生', 0, 3), (728, 322, N'水', 0, 4),
-- Question 323
(729, 323, N'前', 1, 1), (730, 323, N'後ろ', 0, 2), (731, 323, N'上', 0, 3), (732, 323, N'下', 0, 4),
-- Question 324
(733, 324, N'しません', 1, 1), (734, 324, N'します', 0, 2), (735, 324, N'した', 0, 3), (736, 324, N'しますか', 0, 4),
-- Question 325
(737, 325, N'何曜日', 1, 1), (738, 325, N'何時', 0, 2), (739, 325, N'何人', 0, 3), (740, 325, N'何年', 0, 4),
-- Question 326
(741, 326, N'は', 1, 1), (742, 326, N'を', 0, 2), (743, 326, N'で', 0, 3), (744, 326, N'から', 0, 4),
-- Question 327
(745, 327, N'ありがとう', 1, 1), (746, 327, N'さようなら', 0, 2), (747, 327, N'こんにちは', 0, 3), (748, 327, N'こんばんは', 0, 4),
-- Question 328
(749, 328, N'雨', 1, 1), (750, 328, N'火', 0, 2), (751, 328, N'犬', 0, 3), (752, 328, N'先生', 0, 4),
-- Question 329
(753, 329, N'お父さん', 1, 1), (754, 329, N'山', 0, 2), (755, 329, N'猫', 0, 3), (756, 329, N'川', 0, 4),
-- Question 330
(757, 330, N'Ga tàu', 1, 1), (758, 330, N'Nhà', 0, 2), (759, 330, N'Thư viện', 0, 3), (760, 330, N'Quán ăn', 0, 4),
-- Question 331
(761, 331, N'赤', 1, 1), (762, 331, N'先生', 0, 2), (763, 331, N'犬', 0, 3), (764, 331, N'山', 0, 4),
-- Question 332
(765, 332, N'電車', 1, 1), (766, 332, N'水', 0, 2), (767, 332, N'先生', 0, 3), (768, 332, N'赤い', 0, 4),
-- Question 333
(769, 333, N'すみません', 1, 1), (770, 333, N'おめでとう', 0, 2), (771, 333, N'ありがとう', 0, 3), (772, 333, N'こんばんは', 0, 4),
-- Question 334
(773, 334, N'六', 1, 1), (774, 334, N'七', 0, 2), (775, 334, N'八', 0, 3), (776, 334, N'九', 0, 4),
-- Question 335
(777, 335, N'好きです', 1, 1), (778, 335, N'高い', 0, 2), (779, 335, N'赤い', 0, 3), (780, 335, N'水', 0, 4),
-- Question 336
(781, 336, N'私は学生です', 1, 1), (782, 336, N'学生私はです', 0, 2), (783, 336, N'です私は学生', 0, 3), (784, 336, N'学生です私は', 0, 4),
-- Question 337
(785, 337, N'日曜日', 1, 1), (786, 337, N'山', 0, 2), (787, 337, N'水', 0, 3), (788, 337, N'先生', 0, 4),
-- Question 338
(789, 338, N'Rẻ', 1, 1), (790, 338, N'Cao', 0, 2), (791, 338, N'Đẹp', 0, 3), (792, 338, N'Lớn', 0, 4),
-- Question 339
(793, 339, N'今', 1, 1), (794, 339, N'昨日', 0, 2), (795, 339, N'明日', 0, 3), (796, 339, N'今日', 0, 4),
-- Question 400
(797, 400, N'학교', 1, 1), (798, 400, N'음악', 0, 2), (799, 400, N'도서관', 0, 3), (800, 400, N'친구', 0, 4),
-- Question 401
(801, 401, N'빨리', 1, 1), (802, 401, N'책', 0, 2), (803, 401, N'예쁘다', 0, 3), (804, 401, N'학교', 0, 4),
-- Question 402
(805, 402, N'무슨 일을 하세요?', 1, 1), (806, 402, N'어디에 가요?', 0, 2), (807, 402, N'무슨 색이에요?', 0, 3), (808, 402, N'몇 시에요?', 0, 4),
-- Question 403
(809, 403, N'했다', 1, 1), (810, 403, N'한다', 0, 2), (811, 403, N'합니다', 0, 3), (812, 403, N'하고', 0, 4),
-- Question 404
(813, 404, N'저도 공부했어요', 1, 1), (814, 404, N'공부 저도 했어요', 0, 2), (815, 404, N'저 공부도 했어요', 0, 3), (816, 404, N'공부했어요 저도', 0, 4),
-- Question 405
(817, 405, N'가지 않습니다', 1, 1), (818, 405, N'갑니다', 0, 2), (819, 405, N'갔어요', 0, 3), (820, 405, N'가요', 0, 4),
-- Question 406
(821, 406, N'예쁘다', 1, 1), (822, 406, N'책', 0, 2), (823, 406, N'학교', 0, 3), (824, 406, N'음악', 0, 4),
-- Question 407
(825, 407, N'안녕히 주무세요', 1, 1), (826, 407, N'안녕하세요', 0, 2), (827, 407, N'감사합니다', 0, 3), (828, 407, N'죄송합니다', 0, 4),
-- Question 408
(829, 408, N'Bạn bè', 1, 1), (830, 408, N'Trường học', 0, 2), (831, 408, N'Thư viện', 0, 3), (832, 408, N'Giáo viên', 0, 4),
-- Question 409
(833, 409, N'습니다', 1, 1), (834, 409, N'아요', 0, 2), (835, 409, N'야', 0, 3), (836, 409, N'지마', 0, 4),
-- Question 410
(837, 410, N'책', 1, 1), (838, 410, N'예쁘다', 0, 2), (839, 410, N'빨리', 0, 3), (840, 410, N'달리다', 0, 4),
-- Question 411
(841, 411, N'지금', 1, 1), (842, 411, N'책', 0, 2), (843, 411, N'도서관', 0, 3), (844, 411, N'음악', 0, 4),
-- Question 412
(845, 412, N'죄송합니다', 1, 1), (846, 412, N'감사합니다', 0, 2), (847, 412, N'안녕하세요', 0, 3), (848, 412, N'빨리', 0, 4),
-- Question 413
(849, 413, N'Mùa hè', 1, 1), (850, 413, N'Mùa đông', 0, 2), (851, 413, N'Mùa xuân', 0, 3), (852, 413, N'Mùa thu', 0, 4),
-- Question 414
(853, 414, N'앞', 1, 1), (854, 414, N'옆', 0, 2), (855, 414, N'위', 0, 3), (856, 414, N'아래', 0, 4),
-- Question 415
(857, 415, N'안 먹어요', 1, 1), (858, 415, N'먹어요', 0, 2), (859, 415, N'먹었어요', 0, 3), (860, 415, N'먹겠습니다', 0, 4),
-- Question 416
(861, 416, N'감사합니다', 1, 1), (862, 416, N'죄송합니다', 0, 2), (863, 416, N'안녕하세요', 0, 3), (864, 416, N'잘가요', 0, 4),
-- Question 417
(865, 417, N'Bài kiểm tra', 1, 1), (866, 417, N'Bác sĩ', 0, 2), (867, 417, N'Bạn bè', 0, 3), (868, 417, N'Nhà', 0, 4),
-- Question 418
(869, 418, N'자주', 1, 1), (870, 418, N'빨리', 0, 2), (871, 418, N'학교', 0, 3), (872, 418, N'의사', 0, 4),
-- Question 419
(873, 419, N'월요일', 1, 1), (874, 419, N'여름', 0, 2), (875, 419, N'음악', 0, 3), (876, 419, N'감사합니다', 0, 4),
-- Question 420
(877, 420, N'Thư viện', 1, 1), (878, 420, N'Nhà', 0, 2), (879, 420, N'Giáo viên', 0, 3), (880, 420, N'Bạn bè', 0, 4),
-- Question 421
(881, 421, N'드시겠어요?', 1, 1), (882, 421, N'먹어요?', 0, 2), (883, 421, N'가요?', 0, 3), (884, 421, N'돼요?', 0, 4),
-- Question 422
(885, 422, N'아/어', 1, 1), (886, 422, N'습니다', 0, 2), (887, 422, N'겠어요', 0, 3), (888, 422, N'십니다', 0, 4),
-- Question 423
(889, 423, N'Âm nhạc', 1, 1), (890, 423, N'Bạn bè', 0, 2), (891, 423, N'Nhà', 0, 3), (892, 423, N'Bác sĩ', 0, 4),
-- Question 424
(893, 424, N'먹었어요', 1, 1), (894, 424, N'먹어요', 0, 2), (895, 424, N'먹을 거예요', 0, 3), (896, 424, N'먹고 있어요', 0, 4),
-- Question 425
(897, 425, N'오', 1, 1), (898, 425, N'일', 0, 2), (899, 425, N'칠', 0, 3), (900, 425, N'육', 0, 4),
-- Question 426
(901, 426, N'Bác sĩ', 1, 1), (902, 426, N'Giáo viên', 0, 2), (903, 426, N'Bạn bè', 0, 3), (904, 426, N'Nhà', 0, 4),
-- Question 427
(905, 427, N'합니까?', 1, 1), (906, 427, N'합니다', 0, 2), (907, 427, N'하세요', 0, 3), (908, 427, N'해요', 0, 4),
-- Question 428
(909, 428, N'안 갑니다', 1, 1), (910, 428, N'갑니다', 0, 2), (911, 428, N'갔습니다', 0, 3), (912, 428, N'갈 거예요', 0, 4),
-- Question 429
(913, 429, N'Trường học', 1, 1), (914, 429, N'Bạn bè', 0, 2), (915, 429, N'Nhà', 0, 3), (916, 429, N'Bác sĩ', 0, 4),
-- Question 430
(917, 430, N'갑시다', 1, 1), (918, 430, N'가요', 0, 2), (919, 430, N'갑니다', 0, 3), (920, 430, N'갈 거예요', 0, 4),
-- Question 431
(921, 431, N'하십니다', 1, 1), (922, 431, N'합니다', 0, 2), (923, 431, N'해요', 0, 3), (924, 431, N'한다', 0, 4),
-- Question 432
(925, 432, N'파란색', 1, 1), (926, 432, N'책', 0, 2), (927, 432, N'의사', 0, 3), (928, 432, N'음악', 0, 4),
-- Question 433
(929, 433, N'Nhà', 1, 1), (930, 433, N'Thư viện', 0, 2), (931, 433, N'Bạn bè', 0, 3), (932, 433, N'Bác sĩ', 0, 4),
-- Question 434
(933, 434, N'버스', 1, 1), (934, 434, N'책', 0, 2), (935, 434, N'빨리', 0, 3), (936, 434, N'노래', 0, 4),
-- Question 435
(937, 435, N'해도 돼요?', 1, 1), (938, 435, N'갑시다', 0, 2), (939, 435, N'합니다', 0, 3), (940, 435, N'하시겠어요?', 0, 4),
-- Question 436
(941, 436, N'싸다', 1, 1), (942, 436, N'비싸다', 0, 2), (943, 436, N'예쁘다', 0, 3), (944, 436, N'친절하다', 0, 4),
-- Question 437
(945, 437, N'하려고 해요', 1, 1), (946, 437, N'합니다', 0, 2), (947, 437, N'했어요', 0, 3), (948, 437, N'할 거예요', 0, 4),
-- Question 438
(949, 438, N'오늘', 1, 1), (950, 438, N'어제', 0, 2), (951, 438, N'내일', 0, 3), (952, 438, N'지금', 0, 4),
-- Question 500
(953, 500, N'Công ty', 1, 1), (954, 500, N'Khách hàng', 0, 2), (955, 500, N'Sản phẩm', 0, 3), (956, 500, N'Hóa đơn', 0, 4),
-- Question 501
(957, 501, N'Khách hàng', 1, 1), (958, 501, N'Công ty', 0, 2), (959, 501, N'Hợp đồng', 0, 3), (960, 501, N'Nhà cung cấp', 0, 4),
-- Question 502
(961, 502, N'Hợp đồng', 1, 1), (962, 502, N'Khách hàng', 0, 2), (963, 502, N'Nhà cung cấp', 0, 3), (964, 502, N'Thị trường', 0, 4),
-- Question 503
(965, 503, N'经理', 1, 1), (966, 503, N'客户', 0, 2), (967, 503, N'会计', 0, 3), (968, 503, N'员工', 0, 4),
-- Question 504
(969, 504, N'请问', 1, 1), (970, 504, N'你好', 0, 2), (971, 504, N'谢谢', 0, 3), (972, 504, N'再见', 0, 4),
-- Question 505
(973, 505, N'定期', 1, 1), (974, 505, N'临时', 0, 2), (975, 505, N'取消', 0, 3), (976, 505, N'增加', 0, 4),
-- Question 506
(977, 506, N'签署', 1, 1), (978, 506, N'产品', 0, 2), (979, 506, N'会计', 0, 3), (980, 506, N'经理', 0, 4),
-- Question 507
(981, 507, N'Báo giá', 1, 1), (982, 507, N'Chiết khấu', 0, 2), (983, 507, N'Hóa đơn', 0, 3), (984, 507, N'Ký tên', 0, 4),
-- Question 508
(985, 508, N'Hóa đơn', 1, 1), (986, 508, N'Sản phẩm', 0, 2), (987, 508, N'Hợp đồng', 0, 3), (988, 508, N'Công ty', 0, 4),
-- Question 509
(989, 509, N'Sản phẩm', 1, 1), (990, 509, N'Hóa đơn', 0, 2), (991, 509, N'Khách hàng', 0, 3), (992, 509, N'Nhà cung cấp', 0, 4),
-- Question 510
(993, 510, N'Kế toán', 1, 1), (994, 510, N'Khách hàng', 0, 2), (995, 510, N'Nhà cung cấp', 0, 3), (996, 510, N'Giám đốc', 0, 4),
-- Question 511
(997, 511, N'您好！', 1, 1), (998, 511, N'早上好', 0, 2), (999, 511, N'谢谢', 0, 3), (1000, 511, N'再见', 0, 4);
SET IDENTITY_INSERT answer_options OFF;

-- Dữ liệu mẫu cho answer_options(part2)
SET IDENTITY_INSERT answer_options ON;
INSERT INTO answer_options (id, question_id, content, is_correct, order_number) VALUES
-- Question 512
(1001, 512, N'货车', 1, 1), (1002, 512, N'经理', 0, 2), (1003, 512, N'办公室', 0, 3), (1004, 512, N'客户', 0, 4),
-- Question 513
(1005, 513, N'Đơn đặt hàng', 1, 1), (1006, 513, N'Hóa đơn', 0, 2), (1007, 513, N'Sản phẩm', 0, 3), (1008, 513, N'Thị trường', 0, 4),
-- Question 514
(1009, 514, N'Cuộc họp', 1, 1), (1010, 514, N'Nhà cung cấp', 0, 2), (1011, 514, N'Khách hàng', 0, 3), (1012, 514, N'Hợp đồng', 0, 4),
-- Question 515
(1013, 515, N'Chiết khấu', 1, 1), (1014, 515, N'Thanh toán', 0, 2), (1015, 515, N'Báo giá', 0, 3), (1016, 515, N'Sản phẩm', 0, 4),
-- Question 516
(1017, 516, N'Thanh toán', 1, 1), (1018, 516, N'Hóa đơn', 0, 2), (1019, 516, N'Ký tên', 0, 3), (1020, 516, N'Nhà cung cấp', 0, 4),
-- Question 517
(1021, 517, N'五', 1, 1), (1022, 517, N'三', 0, 2), (1023, 517, N'六', 0, 3), (1024, 517, N'十', 0, 4),
-- Question 518
(1025, 518, N'十', 1, 1), (1026, 518, N'五', 0, 2), (1027, 518, N'六', 0, 3), (1028, 518, N'八', 0, 4),
-- Question 519
(1029, 519, N'请报价', 1, 1), (1030, 519, N'请发票', 0, 2), (1031, 519, N'请付款', 0, 3), (1032, 519, N'请签约', 0, 4),
-- Question 520
(1033, 520, N'卖方', 1, 1), (1034, 520, N'买方', 0, 2), (1035, 520, N'供应商', 0, 3), (1036, 520, N'客户', 0, 4),
-- Question 521
(1037, 521, N'买方', 1, 1), (1038, 521, N'卖方', 0, 2), (1039, 521, N'客户', 0, 3), (1040, 521, N'经理', 0, 4),
-- Question 522
(1041, 522, N'供应商', 1, 1), (1042, 522, N'客户', 0, 2), (1043, 522, N'买方', 0, 3), (1044, 522, N'卖方', 0, 4),
-- Question 523
(1045, 523, N'库存', 1, 1), (1046, 523, N'订单', 0, 2), (1047, 523, N'发票', 0, 3), (1048, 523, N'产品', 0, 4),
-- Question 524
(1049, 524, N'Hợp đồng', 1, 1), (1050, 524, N'Khách hàng', 0, 2), (1051, 524, N'Nhà cung cấp', 0, 3), (1052, 524, N'Hóa đơn', 0, 4),
-- Question 525
(1053, 525, N'预', 1, 1), (1054, 525, N'后', 0, 2), (1055, 525, N'定', 0, 3), (1056, 525, N'收', 0, 4),
-- Question 526
(1057, 526, N'Thị trường', 1, 1), (1058, 526, N'Sản phẩm', 0, 2), (1059, 526, N'Hóa đơn', 0, 3), (1060, 526, N'Ký tên', 0, 4),
-- Question 527
(1061, 527, N'Giá cả', 1, 1), (1062, 527, N'Thanh toán', 0, 2), (1063, 527, N'Hóa đơn', 0, 3), (1064, 527, N'Ký tên', 0, 4),
-- Question 528
(1065, 528, N'Ký tên', 1, 1), (1066, 528, N'Thanh toán', 0, 2), (1067, 528, N'Hóa đơn', 0, 3), (1068, 528, N'Báo giá', 0, 4),
-- Question 529
(1069, 529, N'Điện thoại', 1, 1), (1070, 529, N'Thư điện tử', 0, 2), (1071, 529, N'Văn phòng', 0, 3), (1072, 529, N'Ký tên', 0, 4),
-- Question 530
(1073, 530, N'账户', 1, 1), (1074, 530, N'产品', 0, 2), (1075, 530, N'客户', 0, 3), (1076, 530, N'经理', 0, 4),
-- Question 531
(1077, 531, N'Văn phòng', 1, 1), (1078, 531, N'Khách hàng', 0, 2), (1079, 531, N'Hợp đồng', 0, 3), (1080, 531, N'Sản phẩm', 0, 4),
-- Question 532
(1081, 532, N'Trợ lý giám đốc', 1, 1), (1082, 532, N'Kế toán', 0, 2), (1083, 532, N'Khách hàng', 0, 3), (1084, 532, N'Nhà cung cấp', 0, 4),
-- Question 533
(1085, 533, N'Biên bản cuộc họp', 1, 1), (1086, 533, N'Hóa đơn', 0, 2), (1087, 533, N'Báo giá', 0, 3), (1088, 533, N'Văn phòng', 0, 4),
-- Question 534
(1089, 534, N'Thư điện tử', 1, 1), (1090, 534, N'Ký tên', 0, 2), (1091, 534, N'Thanh toán', 0, 3), (1092, 534, N'Văn phòng', 0, 4),
-- Question 535
(1093, 535, N'Đi công tác', 1, 1), (1094, 535, N'Ký tên', 0, 2), (1095, 535, N'Thanh toán', 0, 3), (1096, 535, N'Giám đốc', 0, 4),
-- Question 536
(1097, 536, N'会议室', 1, 1), (1098, 536, N'办公室', 0, 2), (1099, 536, N'客户', 0, 3), (1100, 536, N'会计', 0, 4),
-- Question 537
(1101, 537, N'Dự án', 1, 1), (1102, 537, N'Sản phẩm', 0, 2), (1103, 537, N'Hóa đơn', 0, 3), (1104, 537, N'Khách hàng', 0, 4),
-- Question 538
(1105, 538, N'Ký kết hợp đồng', 1, 1), (1106, 538, N'Báo giá', 0, 2), (1107, 538, N'Ký tên', 0, 3), (1108, 538, N'Thanh toán', 0, 4),
-- Question 539
(1109, 539, N'Biên lai', 1, 1), (1110, 539, N'Hóa đơn', 0, 2), (1111, 539, N'Báo giá', 0, 3), (1112, 539, N'Sản phẩm', 0, 4);
SET IDENTITY_INSERT answer_options OFF;


-- Dữ liệu mẫu cho registrations
SET IDENTITY_INSERT registrations ON;
INSERT INTO registrations 
(id, user_id, course_id, package_id, order_code, registration_time, total_cost, status, valid_from, valid_to, notes) VALUES
(1, 4, 1, 1, 'ORD3F8B2C6A7', GETDATE(), 79.99, N'active', GETDATE(), DATEADD(month, 3, GETDATE()), N'Khách hàng 1 đăng ký khóa tiếng Anh cơ bản.'),
(2, 5, 2, 3, 'ORD8E1A4F2D9', GETDATE(), 129.99, N'pending', GETDATE(), DATEADD(month, 6, GETDATE()), N'Khách hàng 2 đang chờ thanh toán cho khóa tiếng Nhật.'),
(3, 4, 3, 4, 'ORDC7B6E1F5A', GETDATE(), 179.99, N'active', GETDATE(), DATEADD(month, 6, GETDATE()), N'Khách hàng 1 đăng ký khóa luyện thi TOEIC.');
SET IDENTITY_INSERT registrations OFF;

-- Dữ liệu mẫu cho quiz_attempts
SET IDENTITY_INSERT quiz_attempts ON;
INSERT INTO quiz_attempts (id, user_id, quiz_id, start_time, end_time, score, status, result) VALUES
(1, 4, 1, GETDATE(), DATEADD(minute, 20, GETDATE()), 85.00, N'completed', N'pass'),
(2, 4, 3, GETDATE(), DATEADD(minute, 30, GETDATE()), 70.00, N'completed', N'pass'),
(3, 5, 1, GETDATE(), NULL, NULL, N'in_progress', NULL);
SET IDENTITY_INSERT quiz_attempts OFF;

-- Dữ liệu mẫu cho quiz_attempt_answers
SET IDENTITY_INSERT quiz_attempt_answers ON;
INSERT INTO quiz_attempt_answers (id, attempt_id, question_id, selected_answer_option_id, time_taken_seconds, marked_for_review, is_correct) VALUES
(1, 1, 1, 1, 10, 0, 1),
(2, 1, 2, 6, 15, 0, 1),
(3, 2, 3, 8, 20, 1, 1),
(4, 2, 4, 12, 12, 0, 1),
(5, 3, 1, 2, 5, 1, 0); -- Câu trả lời sai cho lần thử đang tiến hành
SET IDENTITY_INSERT quiz_attempt_answers OFF;

SET IDENTITY_INSERT my_courses ON;
INSERT INTO my_courses (id, user_id, course_id, progress_percent, last_lesson_id, last_accessed, status) VALUES
(1, 4, 1, 50.00, 2, GETDATE(), 'in_progress'),   -- Customer One đang học Tiếng Anh A1, đến bài 2, tiến độ 50%
(2, 4, 3, 100.00, 6, DATEADD(day, -2, GETDATE()), 'completed');  -- Customer One đã hoàn thành luyện thi TOEIC
SET IDENTITY_INSERT my_courses OFF;

INSERT INTO quiz_settings (quiz_id, total_questions, question_type)
VALUES (1, 10, 'Chọn 1');  -- giả sử quizzes.id = 1

-- Thêm dữ liệu vào question_group
SET IDENTITY_INSERT question_group ON;
INSERT INTO question_group (id, name, quiz_setting_id) VALUES
(1, N'Tên nhóm chọn 1', 1),
(2, N'Tên nhóm chọn 2', 1),
(3, N'Tên nhóm chọn 3', 1);
SET IDENTITY_INSERT question_group OFF;

-- Dữ liệu cho bảng trung gian quiz_questions
-- Được suy ra từ mối quan hệ gián tiếp trong dữ liệu mẫu

INSERT INTO quiz_questions (quiz_id, question_id) VALUES
-- Quiz 1 (Tiếng Anh cơ bản 1) lấy các câu hỏi từ Lesson 3
(1, 1), (1, 2), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18), (1, 19), (1, 42), (1, 43), (1, 44), (1, 45), (1, 46), (1, 47), (1, 48), (1, 49), (1, 50), (1, 51), (1, 52), (1, 53), (1, 54), (1, 55), (1, 56), (1, 57), (1, 58), (1, 59), (1, 60), (1, 61), (1, 62), (1, 63), (1, 64), (1, 65), (1, 66), (1, 67), (1, 68), (1, 69), (1, 70), (1, 71), (1, 72), (1, 73), (1, 74), (1, 75), (1, 76), (1, 77), (1, 78), (1, 79), (1, 80), (1, 81),

-- Quiz 2 (Giữa kỳ tiếng Anh) lấy các câu hỏi từ Lesson 7
(2, 100), (2, 101), (2, 102), (2, 103), (2, 104), (2, 105), (2, 106), (2, 107), (2, 108), (2, 109), (2, 110), (2, 111), (2, 112), (2, 113), (2, 114), (2, 115), (2, 116), (2, 117), (2, 118), (2, 119), (2, 120), (2, 121), (2, 122), (2, 123), (2, 124), (2, 125), (2, 126), (2, 127), (2, 128), (2, 129), (2, 130), (2, 131), (2, 132), (2, 133), (2, 134), (2, 135), (2, 136), (2, 137), (2, 138),

-- Quiz 3 (Ngữ pháp TOEIC) lấy các câu hỏi từ Lesson 6
(3, 3), (3, 4), (3, 20), (3, 21), (3, 22), (3, 23), (3, 24), (3, 25), (3, 26), (3, 27), (3, 28), (3, 29), (3, 30), (3, 31), (3, 32), (3, 33), (3, 34), (3, 200), (3, 201), (3, 202), (3, 203), (3, 204), (3, 205), (3, 206), (3, 207), (3, 208), (3, 209), (3, 210), (3, 211), (3, 212), (3, 213), (3, 214), (3, 215), (3, 216), (3, 217), (3, 218), (3, 219), (3, 220), (3, 221), (3, 222), (3, 223), (3, 224), (3, 225), (3, 226), (3, 227), (3, 228), (3, 229), (3, 230), (3, 231), (3, 232), (3, 233), (3, 234), (3, 235), (3, 236), (3, 237), (3, 238),

-- Quiz 4 (Tổng hợp N5) lấy các câu hỏi từ Lesson 9
(4, 35), (4, 36), (4, 37), (4, 300), (4, 301), (4, 302), (4, 303), (4, 304), (4, 305), (4, 306), (4, 307), (4, 308), (4, 309), (4, 310), (4, 311), (4, 312), (4, 313), (4, 314), (4, 315), (4, 316), (4, 317), (4, 318), (4, 319), (4, 320), (4, 321), (4, 322), (4, 323), (4, 324), (4, 325), (4, 326), (4, 327), (4, 328), (4, 329), (4, 330), (4, 331), (4, 332), (4, 333), (4, 334), (4, 335), (4, 336), (4, 337), (4, 338), (4, 339),

-- Quiz 5 (Giữa kỳ Topic II) lấy các câu hỏi từ Lesson 11
(5, 38), (5, 39), (5, 400), (5, 401), (5, 402), (5, 403), (5, 404), (5, 405), (5, 406), (5, 407), (5, 408), (5, 409), (5, 410), (5, 411), (5, 412), (5, 413), (5, 414), (5, 415), (5, 416), (5, 417), (5, 418), (5, 419), (5, 420), (5, 421), (5, 422), (5, 423), (5, 424), (5, 425), (5, 426), (5, 427), (5, 428), (5, 429), (5, 430), (5, 431), (5, 432), (5, 433), (5, 434), (5, 435), (5, 436), (5, 437), (5, 438),

-- Quiz 6 (Tiếng Trung thương mại) lấy các câu hỏi từ Lesson 13
(6, 40), (6, 41), (6, 500), (6, 501), (6, 502), (6, 503), (6, 504), (6, 505), (6, 506), (6, 507), (6, 508), (6, 509), (6, 510), (6, 511), (6, 512), (6, 513), (6, 514), (6, 515), (6, 516), (6, 517), (6, 518), (6, 519), (6, 520), (6, 521), (6, 522), (6, 523), (6, 524), (6, 525), (6, 526), (6, 527), (6, 528), (6, 529), (6, 530), (6, 531), (6, 532), (6, 533), (6, 534), (6, 535), (6, 536), (6, 537), (6, 538), (6, 539);

UPDATE quiz_attempt_answers SET is_correct = 0 WHERE is_correct IS NULL;

UPDATE price_packages
SET sale = CASE
    WHEN name = 'Gói cao cấp tiếng Anh A1' THEN 50.00
    WHEN name = 'Gói cơ bản tiếng Anh A1' THEN 20.00
    WHEN name = 'Gói tiêu chuẩn tiếng Nhật N5' THEN 20.00
    WHEN name = 'Gói luyện thi TOEIC' THEN 20.00
    ELSE sale -- Giu nguyen gia tri neu khong khop
END;
GO