package com.codecampus.controller;

import com.codecampus.dto.NoteDto; // <-- THÊM IMPORT
import com.codecampus.entity.Course;
import com.codecampus.entity.Lesson;
// import com.codecampus.entity.Note; // <-- Không cần nữa
import com.codecampus.entity.User;
import com.codecampus.exception.NoteNotFoundException;
import com.codecampus.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/learning")
public class LearningController {

    // (Constructor và 2 phương thức GetMapping chính giữ nguyên)
    // ... (Toàn bộ constructor và 2 hàm getLessonView, handleCourseEntry)
    private final LessonService lessonService;
    private final CourseService courseService;
    private final MyCourseService myCourseService;
    private final UserService userService; // Đây là UserService bạn cung cấp
    private final NoteService noteService;
    private final AiLearningService aiLearningService;

    @Autowired
    public LearningController(LessonService lessonService,
                              CourseService courseService,
                              MyCourseService myCourseService,
                              UserService userService,
                              NoteService noteService,
                              AiLearningService aiLearningService) {
        this.lessonService = lessonService;
        this.courseService = courseService;
        this.myCourseService = myCourseService;
        this.userService = userService;
        this.noteService = noteService;
        this.aiLearningService = aiLearningService;
    }

    @GetMapping("/{courseId}")
    public String handleCourseEntry(@PathVariable("courseId") Integer courseId,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        User currentUser = userService.findUserByEmail(principal.getName());

        if (currentUser == null) {
            return "redirect:/login?error=user_not_found";
        }

        if (!myCourseService.isUserEnrolled(currentUser.getId(), courseId)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập khóa học này.");
            return "redirect:/my-courses";
        }
        try {
            Lesson firstLesson = lessonService.getFirstLesson(courseId);
            return "redirect:/learning/" + courseId + "/" + firstLesson.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: Khóa học này chưa có bài học.");
            return "redirect:/my-courses";
        }
    }

    @GetMapping("/{courseId}/{lessonId}")
    public String getLessonView(@PathVariable("courseId") Integer courseId,
                                @PathVariable("lessonId") Integer lessonId,
                                Model model,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        User currentUser = userService.findUserByEmail(principal.getName());

        if (currentUser == null) {
            return "redirect:/login?error=user_not_found";
        }

        if (!myCourseService.isUserEnrolled(currentUser.getId(), courseId)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập khóa học này.");
            return "redirect:/my-courses";
        }

        Lesson currentLesson = lessonService.getLessonById(lessonId);
        //Cập nhật tiến độ
        myCourseService.updateProgress(currentUser.getId(), courseId, Math.toIntExact(currentLesson.getId()));

        if (currentLesson.getLab() != null) {
            return "redirect:/learning/lab/" + currentLesson.getLab().getId() + "?lessonId=" + lessonId;
        }

        if (currentLesson.getQuiz() != null) {
            return "redirect:/learning/quiz/" + currentLesson.getQuiz().getId() + "?lessonId=" + lessonId;
        }



        Course course = courseService.findCourseById(courseId);
        List<Lesson> allLessons = lessonService.getLessonsByCourseId(courseId);

        Lesson prevLesson = null;
        Lesson nextLesson = null;
        int currentIndex = -1;
        Long currentLessonId = currentLesson.getId();

        for (int i = 0; i < allLessons.size(); i++) {
            if (allLessons.get(i).getId().equals(currentLessonId)) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex > 0) prevLesson = allLessons.get(currentIndex - 1);
        if (currentIndex != -1 && currentIndex < allLessons.size() - 1) nextLesson = allLessons.get(currentIndex + 1);

        model.addAttribute("currentLesson", currentLesson);
        model.addAttribute("course", course);
        model.addAttribute("allLessons", allLessons);
        model.addAttribute("prevLesson", prevLesson);
        model.addAttribute("nextLesson", nextLesson);
        model.addAttribute("currentUserId", currentUser.getId());

        return "learning/lesson-view";
    }

    // ========== SỬA LẠI CÁC ENDPOINT API ==========

    /**
     * API: Lấy danh sách Ghi chú (Sửa lại: Trả về List<NoteDto>)
     */
    @GetMapping("/api/notes/{lessonId}")
    @ResponseBody
    public ResponseEntity<List<NoteDto>> getNotes(@PathVariable Long lessonId, Principal principal) {
        User currentUser = userService.findUserByEmail(principal.getName());
        List<NoteDto> notes = noteService.getNotesForLesson(currentUser.getId(), lessonId);
        return ResponseEntity.ok(notes);
    }

    /**
     * API: Lưu Ghi chú mới (Sửa lại: Trả về NoteDto)
     */
    @PostMapping("/api/notes/save")
    @ResponseBody
    public ResponseEntity<NoteDto> saveNote(@RequestBody Map<String, String> payload, Principal principal) {
        User currentUser = userService.findUserByEmail(principal.getName());
        Long lessonId = Long.parseLong(payload.get("lessonId"));
        String content = payload.get("content");

        NoteDto savedNoteDto = noteService.saveNote(currentUser, lessonId, content);
        return ResponseEntity.ok(savedNoteDto);
    }

    // (Các API còn lại giữ nguyên)
    @PostMapping("/api/ask-ai")
    @ResponseBody
    public ResponseEntity<Map<String, String>> askAi(@RequestBody Map<String, String> payload) {
        Long lessonId = Long.parseLong(payload.get("lessonId"));
        String question = payload.get("question");
        String answer = aiLearningService.getContextualAnswer(lessonId, question);
        return ResponseEntity.ok(Map.of("answer", answer));
    }

    @GetMapping("/api/summarize/{lessonId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getSummary(@PathVariable Long lessonId) {
        String summary = aiLearningService.summarizeLesson(lessonId);
        return ResponseEntity.ok(Map.of("summary", summary));
    }

    // === API MỚI: CẬP NHẬT GHI CHÚ ===
    @PutMapping("/api/notes/update/{noteId}")
    @ResponseBody
    public ResponseEntity<NoteDto> updateNote(@PathVariable Long noteId,
                                              @RequestBody Map<String, String> payload,
                                              Principal principal) {
        User currentUser = userService.findUserByEmail(principal.getName());
        String content = payload.get("content");

        try {
            NoteDto updatedNote = noteService.updateNote(noteId, content, currentUser.getId());
            return ResponseEntity.ok(updatedNote);
        } catch (NoteNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // === API MỚI: XÓA GHI CHÚ ===
    @DeleteMapping("/api/notes/{noteId}")
    @ResponseBody
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId, Principal principal) {
        User currentUser = userService.findUserByEmail(principal.getName());

        try {
            noteService.deleteNote(noteId, currentUser.getId());
            return ResponseEntity.ok().build(); // Trả về 200 OK (không cần nội dung)
        } catch (NoteNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}