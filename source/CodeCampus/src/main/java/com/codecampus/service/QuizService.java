package com.codecampus.service;

import com.codecampus.entity.Quiz;
import com.codecampus.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Transactional(readOnly = true)
    public Quiz findQuizById(Integer quizId) {
        // .orElseThrow() sẽ tự ném 500 nếu không tìm thấy
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Quiz với ID: " + quizId));
    }
}