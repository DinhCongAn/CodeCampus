﻿USE master;
GO

-- Xóa database nếu đã tồn tại, đảm bảo rollback các kết nối
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'olls')
BEGIN
    ALTER DATABASE olls SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE olls;
END
GO

CREATE DATABASE olls;
GO

USE olls;

CREATE TABLE user_roles (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(MAX)
);

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


CREATE TABLE blog_categories (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    is_active BIT DEFAULT 1
);

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

CREATE TABLE sliders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    image_url NVARCHAR(255) NOT NULL,
    link_url NVARCHAR(255),
    title NVARCHAR(255),
    description NVARCHAR(MAX),
    status NVARCHAR(50) DEFAULT 'active',
    order_number INT DEFAULT 0
);

CREATE TABLE course_categories (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(MAX),
    is_active BIT DEFAULT 1
);

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

CREATE TABLE lesson_types (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(MAX)
);

CREATE TABLE test_types (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(MAX)
);

CREATE TABLE question_levels (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(MAX)
);

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

CREATE TABLE answer_options (
    id INT IDENTITY(1,1) PRIMARY KEY,
    question_id INT,
    content NVARCHAR(MAX) NOT NULL,
    is_correct BIT DEFAULT 0,
    order_number INT DEFAULT 0,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

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

CREATE TABLE quiz_settings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    quiz_id INT NOT NULL,
    total_questions INT,
    question_type VARCHAR(50),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);
GO

CREATE TABLE question_group (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255),
	questions_number INT,
    quiz_setting_id BIGINT NOT NULL,
    FOREIGN KEY (quiz_setting_id) REFERENCES quiz_settings(id)
);
GO

CREATE TABLE quiz_questions (
    quiz_id INT NOT NULL,
    question_id INT NOT NULL,
    PRIMARY KEY (quiz_id, question_id),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);
GO


-- Feedback table
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

-- Feedback attachments table
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

-- Trigger for auto-updating updated_at
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

-- Add verification_tokens table
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

-- Trigger cập nhật updated_at khi UPDATE
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



