USE master;
GO

-- Xóa database nếu đã tồn tại, đảm bảo rollback các kết nối
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'codecampus_db')
BEGIN
    ALTER DATABASE codecampus_db SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE codecampus_db;
END
GO

CREATE DATABASE codecampus_db;
GO

USE codecampus_db;
GO

-- Bảng Vai trò Người dùng
CREATE TABLE user_roles (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(MAX)
);
GO

-- Bảng Người dùng
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name NVARCHAR(255),
    gender NVARCHAR(10),
    mobile VARCHAR(20),
    role_id INT,
    avatar NTEXT,
    [address] NTEXT,
    status NVARCHAR(50) DEFAULT 'pending',
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (role_id) REFERENCES user_roles(id)
);
GO

-- Bảng Cài đặt Hệ thống
CREATE TABLE settings (
    id INT IDENTITY(1,1) PRIMARY KEY,
    type NVARCHAR(100),
    value NVARCHAR(255),
    order_num INT,
    status VARCHAR(50),
    setting_key VARCHAR(100),
    setting_value NVARCHAR(MAX),
    description NVARCHAR(MAX)
);
GO

-- Bảng Danh mục Blog
CREATE TABLE blog_categories (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    is_active BIT DEFAULT 1
);
GO

-- Bảng Blog
CREATE TABLE blogs (
    id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    content NVARCHAR(MAX),
    blog_category_id INT,
    author_id INT,
    thumbnail_url NTEXT,
    status NVARCHAR(50) DEFAULT 'draft',
    published_at DATETIME NULL DEFAULT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (blog_category_id) REFERENCES blog_categories(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
);
GO

-- Bảng Slider
CREATE TABLE sliders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    image_url NVARCHAR(255) NOT NULL,
    link_url NVARCHAR(255),
    title NVARCHAR(255),
    description NVARCHAR(MAX),
    status NVARCHAR(50) DEFAULT 'active',
    order_number INT DEFAULT 0
);
GO

-- Bảng Danh mục Khóa học
CREATE TABLE course_categories (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(MAX),
    is_active BIT DEFAULT 1
);
GO

-- Bảng Khóa học
CREATE TABLE courses (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    category_id INT,
    description NVARCHAR(MAX),
    status NVARCHAR(50) DEFAULT 'draft',
    is_featured BIT DEFAULT 0,
    owner_id INT,
    thumbnail_url NTEXT,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (category_id) REFERENCES course_categories(id),
    FOREIGN KEY (owner_id) REFERENCES users(id)
);
GO

-- Bảng Gói giá
CREATE TABLE price_packages (
    id INT IDENTITY(1,1) PRIMARY KEY,
    course_id INT,
    name NVARCHAR(100) NOT NULL,
    duration_months INT NOT NULL CHECK (duration_months > 0),
    list_price DECIMAL(10, 2) NOT NULL CHECK (list_price >= 0),
    sale_price DECIMAL(10, 2) CHECK (sale_price >= 0),
    status NVARCHAR(50) DEFAULT 'active',
    description NVARCHAR(MAX),
    sale DECIMAL(10,2),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);
GO

-- Bảng Loại bài học
CREATE TABLE lesson_types (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(MAX)
);
GO

-- Bảng Loại bài kiểm tra
CREATE TABLE test_types (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(MAX)
);
GO

-- Bảng Cấp độ câu hỏi
CREATE TABLE question_levels (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(MAX)
);
GO

-- Bảng Bài kiểm tra (Quiz)
CREATE TABLE quizzes (
    id INT IDENTITY(1,1) PRIMARY KEY,
    course_id INT,
    test_type_id INT,
    name NVARCHAR(255) NOT NULL,
    exam_level_id INT,
    duration_minutes INT,
    pass_rate_percentage DECIMAL(5, 2),
    description NVARCHAR(MAX),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (test_type_id) REFERENCES test_types(id),
    FOREIGN KEY (exam_level_id) REFERENCES question_levels(id)
);
GO

-- Bảng Bài học
CREATE TABLE lessons (
    id INT IDENTITY(1,1) PRIMARY KEY,
    course_id INT,
    lesson_type_id INT,
    name NVARCHAR(255) NOT NULL,
    topic NVARCHAR(255),
    order_number INT DEFAULT 0,
    video_url NVARCHAR(255),
    html_content NVARCHAR(MAX),
    quiz_id INT NULL,
    status NVARCHAR(50) DEFAULT 'active',
    package_id INT,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (lesson_type_id) REFERENCES lesson_types(id),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);
GO

-- Bảng Câu hỏi
CREATE TABLE questions (
    id INT IDENTITY(1,1) PRIMARY KEY,
    course_id INT,
    lesson_id INT NULL,
    question_level_id INT,
    status NVARCHAR(50) DEFAULT 'draft',
    content NVARCHAR(MAX) NOT NULL,
    media_url NVARCHAR(255),
    explanation NVARCHAR(MAX),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (lesson_id) REFERENCES lessons(id),
    FOREIGN KEY (question_level_id) REFERENCES question_levels(id)
);
GO

-- Bảng Lựa chọn trả lời
CREATE TABLE answer_options (
    id INT IDENTITY(1,1) PRIMARY KEY,
    question_id INT,
    content NVARCHAR(MAX) NOT NULL,
    is_correct BIT DEFAULT 0,
    order_number INT DEFAULT 0,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);
GO

-- Bảng Đăng ký khóa học
CREATE TABLE registrations (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    course_id INT,
    package_id INT,
    order_code NVARCHAR(20) UNIQUE NOT NULL,
    registration_time DATETIME NOT NULL DEFAULT GETDATE(),
    total_cost DECIMAL(10, 2) NOT NULL,
    status NVARCHAR(50) DEFAULT 'pending',
    valid_from DATETIME,
    valid_to DATETIME,
    notes NVARCHAR(MAX),
    lastchange_by NVARCHAR(50),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (package_id) REFERENCES price_packages(id)
);
GO

-- Bảng Lượt làm bài quiz
CREATE TABLE quiz_attempts (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    quiz_id INT,
    start_time DATETIME NOT NULL DEFAULT GETDATE(),
    end_time DATETIME NULL DEFAULT NULL,
    score DECIMAL(5, 2) NULL,
    status NVARCHAR(50) DEFAULT 'in_progress',
    result NVARCHAR(50) NULL,
    ai_hint_count INT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);
GO

-- Bảng Chi tiết câu trả lời trong một lượt làm bài
CREATE TABLE quiz_attempt_answers (
    id INT IDENTITY(1,1) PRIMARY KEY,
    attempt_id INT,
    question_id INT,
    selected_answer_option_id INT NULL,
    time_taken_seconds INT NULL,
    marked_for_review BIT DEFAULT 0,
    is_correct BIT NOT NULL DEFAULT 0,
    FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id),
    FOREIGN KEY (question_id) REFERENCES questions(id),
    FOREIGN KEY (selected_answer_option_id) REFERENCES answer_options(id)
);
GO

-- Bảng Khóa học của tôi (theo dõi tiến độ)
CREATE TABLE my_courses (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    progress_percent DECIMAL(5,2) DEFAULT 0.0,
    last_lesson_id INT NULL,
    last_accessed DATETIME DEFAULT GETDATE(),
    status VARCHAR(50) DEFAULT 'in_progress',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (last_lesson_id) REFERENCES lessons(id)
);
GO

-- Bảng Ghi chú cá nhân
CREATE TABLE notes(
    id INT IDENTITY(1,1) PRIMARY KEY,
    [user_id] INT,
    lesson_id INT,
    note TEXT,
    image_url TEXT,
    video_url TEXT,
    FOREIGN KEY ([user_id]) REFERENCES [users]([id]),
    FOREIGN KEY ([lesson_id]) REFERENCES [lessons]([id])
);
GO

-- Bảng Cài đặt Quiz
CREATE TABLE quiz_settings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    quiz_id INT NOT NULL,
    total_questions INT,
    question_type VARCHAR(50),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);
GO

-- Bảng Nhóm câu hỏi
CREATE TABLE question_group (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255),
    questions_number INT,
    quiz_setting_id BIGINT NOT NULL,
    FOREIGN KEY (quiz_setting_id) REFERENCES quiz_settings(id)
);
GO

-- Bảng nối Quiz và Question (quan hệ nhiều-nhiều)
CREATE TABLE quiz_questions (
    quiz_id INT NOT NULL,
    question_id INT NOT NULL,
    PRIMARY KEY (quiz_id, question_id),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);
GO

-- Bảng Phản hồi/Đánh giá
CREATE TABLE feedbacks (
  id INT IDENTITY(1,1) PRIMARY KEY,
  course_id INT NOT NULL,
  user_id INT NOT NULL,
  rating INT NOT NULL,
  comment NVARCHAR(MAX),
  created_at DATETIME2 DEFAULT GETDATE(),
  updated_at DATETIME2 DEFAULT GETDATE(),
  FOREIGN KEY (course_id) REFERENCES courses(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
GO

-- Bảng File đính kèm của Phản hồi
CREATE TABLE feedback_attachments (
  id INT IDENTITY(1,1) PRIMARY KEY,
  feedback_id INT NOT NULL,
  file_name NVARCHAR(255) NOT NULL,
  file_url NVARCHAR(512) NOT NULL,
  file_type NVARCHAR(50) NOT NULL,
  created_at DATETIME2 DEFAULT GETDATE(),
  FOREIGN KEY (feedback_id) REFERENCES feedbacks(id) ON DELETE CASCADE
);
GO

-- Trigger tự động cập nhật updated_at cho feedbacks
CREATE TRIGGER tr_feedbacks_update
ON feedbacks
AFTER UPDATE
AS
BEGIN
    UPDATE feedbacks
    SET updated_at = GETDATE()
    FROM feedbacks f
    INNER JOIN inserted i ON f.id = i.id
END
GO

-- Bảng Token xác thực
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='verification_tokens' AND xtype='U')
BEGIN
    CREATE TABLE verification_tokens (
        id INT IDENTITY(1,1) PRIMARY KEY,
        token VARCHAR(255) NOT NULL UNIQUE,
        user_id INT NOT NULL,
        expiry_date DATETIME NOT NULL,
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );
END
GO

-- Bảng Media cho Câu hỏi
CREATE TABLE question_media (
    id INT IDENTITY(1,1) PRIMARY KEY,
    question_id INT NOT NULL,
    media_url NVARCHAR(MAX) NOT NULL,
    media_type NVARCHAR(20) NOT NULL,
    file_name NVARCHAR(255),
    description NVARCHAR(500),
    order_number INT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_question_media_question FOREIGN KEY (question_id)
        REFERENCES questions(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_media_type CHECK (media_type IN ('image', 'video', 'audio'))
);
GO

-- Trigger tự động cập nhật updated_at cho question_media
CREATE TRIGGER trg_question_media_update
ON question_media
AFTER UPDATE
AS
BEGIN
    UPDATE question_media
    SET updated_at = GETDATE()
    FROM inserted
    WHERE question_media.id = inserted.id;
END
GO


-- (Thêm vào cuối DBScript.sql)
GO
-- Bảng 19: Định nghĩa LAB (Đề bài)
CREATE TABLE labs (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX), -- Mô tả/Hướng dẫn lab
    lab_type NVARCHAR(50) DEFAULT 'coding', -- (ví dụ: 'coding', 'sql', 'devops')
    evaluation_criteria NVARCHAR(MAX), -- Tiêu chí chấm (cho AI)
    created_at DATETIME DEFAULT GETDATE()
);
GO
-- Bảng 20: Lượt làm LAB
CREATE TABLE lab_attempts (
    id INT IDENTITY(1,1) PRIMARY KEY,
    lab_id INT NOT NULL,
    user_id INT NOT NULL,
    status NVARCHAR(50) DEFAULT 'in_progress', -- ('in_progress', 'submitted', 'grading', 'graded')
    submitted_content NVARCHAR(MAX), -- Code/Script/File của user
    ai_grade DECIMAL(5, 2) NULL, -- Điểm AI chấm
    ai_feedback NVARCHAR(MAX) NULL, -- Nhận xét của AI
    started_at DATETIME DEFAULT GETDATE(),
    completed_at DATETIME NULL,
    FOREIGN KEY (lab_id) REFERENCES labs(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO

CREATE TABLE lab_ai_interactions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    attempt_id INT NOT NULL,
    user_prompt NVARCHAR(MAX),
    ai_response NVARCHAR(MAX),
    timestamp DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (attempt_id) REFERENCES lab_attempts(id) ON DELETE CASCADE
);
GO


-- Sửa bảng 'lessons'
ALTER TABLE lessons
ADD lab_id INT NULL;
GO
ALTER TABLE lessons
ADD CONSTRAINT fk_lessons_lab FOREIGN KEY (lab_id) REFERENCES labs(id);
GO

ALTER TABLE notes
ALTER COLUMN note NVARCHAR(MAX);

ALTER DATABASE codecampus_db COLLATE SQL_Latin1_General_CP1_CI_AS;

--bo sung bang luu tien do hoc tap--
USE codecampus_db;
GO

-- Bảng này lưu chi tiết: User A đã học xong Bài B chưa
CREATE TABLE user_lesson_progress (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    lesson_id INT NOT NULL,
    course_id INT NOT NULL,
    is_completed BIT DEFAULT 0,
    completed_at DATETIME DEFAULT GETDATE(),
    last_accessed DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (lesson_id) REFERENCES lessons(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT uq_user_lesson UNIQUE (user_id, lesson_id) -- Quan trọng: 1 user chỉ có 1 record cho 1 bài
);
GO

-- Chạy lệnh này trong SQL Server
ALTER TABLE blogs ADD summary NVARCHAR(500); -- Mô tả ngắn
ALTER TABLE blogs ADD is_featured BIT DEFAULT 0; -- Cờ nổi bật


USE codecampus_db;
GO

-- Thêm cột điểm trung bình
ALTER TABLE courses 
ADD average_rating DECIMAL(3, 1) DEFAULT 0.0;
GO

-- Thêm cột tổng số đánh giá
ALTER TABLE courses 
ADD review_count INT DEFAULT 0;
GO