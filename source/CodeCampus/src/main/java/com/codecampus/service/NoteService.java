package com.codecampus.service;

import com.codecampus.dto.NoteDto; // <-- THÊM IMPORT
import com.codecampus.entity.Lesson;
import com.codecampus.entity.Note;
import com.codecampus.entity.User;
import com.codecampus.exception.NoteNotFoundException;
import com.codecampus.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors; // <-- THÊM IMPORT

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private LessonService lessonService;

    // --- HÀM HELPER MỚI: Chuyển đổi Entity -> DTO ---
    private NoteDto toDto(Note note) {
        NoteDto dto = new NoteDto();
        dto.setId(note.getId());
        dto.setNote(note.getNote());
        dto.setLessonId(note.getLesson().getId());
        dto.setUserId(note.getUser().getId());
        return dto;
    }

    /**
     * Sửa lại: Trả về List<NoteDto>
     */
    public List<NoteDto> getNotesForLesson(Integer userId, Long lessonId) {
        List<Note> notes = noteRepository.findByUserIdAndLessonId(userId, lessonId);
        // Chuyển đổi List<Note> thành List<NoteDto>
        return notes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Sửa lại: Trả về NoteDto
     */
    public NoteDto saveNote(User user, Long lessonId, String content) {
        Lesson lesson = lessonService.getLessonById(lessonId.intValue());

        Note note = new Note();
        note.setUser(user);
        note.setLesson(lesson);
        note.setNote(content);

        Note savedNote = noteRepository.save(note); // Lưu Entity

        return toDto(savedNote); // Trả về DTO
    }


    // === THÊM PHƯƠNG THỨC MỚI: CẬP NHẬT ===
    @Transactional
    public NoteDto updateNote(Long noteId, String newContent, Integer userId) {
        // 1. Tìm ghi chú và xác thực chủ sở hữu
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new NoteNotFoundException("Không tìm thấy ghi chú hoặc bạn không có quyền sửa."));

        // 2. Cập nhật nội dung
        note.setNote(newContent);

        // 3. Lưu lại
        Note updatedNote = noteRepository.save(note);

        // 4. Trả về DTO
        return toDto(updatedNote);
    }

    // === THÊM PHƯƠNG THỨC MỚI: XÓA ===
    @Transactional
    public void deleteNote(Long noteId, Integer userId) {
        // 1. Tìm ghi chú và xác thực chủ sở hữu
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new NoteNotFoundException("Không tìm thấy ghi chú hoặc bạn không có quyền xóa."));

        // 2. Xóa
        noteRepository.delete(note);
    }

}