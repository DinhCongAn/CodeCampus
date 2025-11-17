package com.codecampus.repository;

import com.codecampus.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserIdAndLessonId(Integer userId, Long lessonId);
    Optional<Note> findByIdAndUserId(Long id, Integer userId);
}