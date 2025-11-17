package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lessons")
@Getter
@Setter
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "order_number")
    private Integer orderNumber;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "html_content", columnDefinition = "NVARCHAR(MAX)")
    private String htmlContent;

    // === SỬA Ở ĐÂY ===
    // Thay vì private Integer quizId;
    /**
     * Định nghĩa quan hệ: Một Lesson có thể liên kết đến một Quiz.
     * 'lessons' là bên sở hữu (owning side) vì nó giữ khóa ngoại 'quiz_id'.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id") // Cột khóa ngoại trong bảng 'lessons'
    private Quiz quiz; // (Bạn sẽ cần tạo Entity 'Quiz.java')

    // === SỬA Ở ĐÂY ===
    // Thay vì private Integer labId;
    /**
     * Định nghĩa quan hệ: Một Lesson có thể liên kết đến một Lab.
     * 'lessons' là bên sở hữu (owning side) vì nó giữ khóa ngoại 'lab_id'.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id") // Cột khóa ngoại trong bảng 'lessons'
    private Lab lab; // Đây chính là thuộc tính "lab" mà 'Lab.java' đang tìm kiếm

    // --- (Phần còn lại giữ nguyên) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_type_id")
    private LessonType lessonType;
}