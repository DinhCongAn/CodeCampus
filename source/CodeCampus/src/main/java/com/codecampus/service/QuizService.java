// src/main/java/com/codecampus/service/QuizService.java
package com.codecampus.service;

import com.codecampus.entity.*;
import com.codecampus.repository.AnswerOptionRepository; // BỔ SUNG
import com.codecampus.repository.QuestionRepository;
import com.codecampus.repository.QuizAttemptAnswerRepository;
import com.codecampus.repository.QuizAttemptRepository;
import com.codecampus.repository.QuizRepository;
import org.springframework.transaction.annotation.Transactional; // SỬA: Dùng @Transactional của Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired private QuizRepository quizRepository;
    @Autowired private QuizAttemptRepository quizAttemptRepository;
    @Autowired private QuizAttemptAnswerRepository quizAttemptAnswerRepository;
    @Autowired private AIService aiService; // Tiêm AI Service
    @Autowired private QuestionRepository questionRepository;

    @Autowired private AnswerOptionRepository answerOptionRepository; // BỔ SUNG

    /**
     * (Màn 16) Lấy kết quả gần nhất
     */
    @Transactional(readOnly = true) // SỬA: Dùng Spring @Transactional
    public Optional<QuizAttempt> getLastAttempt(Integer userId, Integer quizId) {
        // (Bạn cần thêm phương thức này vào QuizAttemptRepository)
        return quizAttemptRepository.findFirstByUserIdAndQuizIdOrderByStartTimeDesc(userId, quizId);
    }

    /**
     * (Màn 16) Bắt đầu Quiz
     */
    @Transactional
    public QuizAttempt startQuiz(User user, Quiz quiz) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setStatus("in_progress");
        return quizAttemptRepository.save(attempt);
    }

    /**
     * (Màn 17) Lấy thông tin bài đang làm (Kiểm tra bảo mật)
     */
    @Transactional(readOnly = true)
    public QuizAttempt getAttemptForTaking(Integer attemptId, Integer userId) {
        // SỬA: Gọi hàm JOIN FETCH đầy đủ
        return quizAttemptRepository.findInProgressAttemptByIdAndUserWithDetails(attemptId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lượt làm bài hoặc đã hết hạn."));
    }

    /**
     * (Màn 17) Lấy câu hỏi (dùng JOIN FETCH)
     */
    @Transactional(readOnly = true)
    public Quiz getQuizWithQuestions(Integer quizId) {
        // (Bạn cần thêm phương thức này (dùng @Query) vào QuizRepository)
        return quizRepository.findByIdWithQuestions(quizId).orElseThrow(
                () -> new RuntimeException("Không tìm thấy Quiz với ID: " + quizId)
        );
    }

    /**
     * (Màn 17) Lưu câu trả lời (AJAX)
     */
    @Transactional
    public void saveAnswer(Integer attemptId, Integer questionId, Integer selectedOptionId) {

        // 1. Tìm xem đã trả lời câu này chưa (để update)
        QuizAttemptAnswer answer = quizAttemptAnswerRepository
                .findByAttempt_IdAndQuestion_Id(attemptId, questionId)
                .orElse(new QuizAttemptAnswer());

        // 2. Lấy các đối tượng liên quan
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lượt làm bài"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Câu hỏi"));
        AnswerOption selectedOption = answerOptionRepository.findById(selectedOptionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lựa chọn"));

        // 3. Set thông tin
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setSelectedAnswerOption(selectedOption);

        // 4. (Tự chấm điểm ngay lúc lưu)
        boolean isCorrect = selectedOption.isCorrect();
        answer.setCorrect(isCorrect);

        quizAttemptAnswerRepository.save(answer);
    }

    /**
     * (Màn 17) Lấy Gợi ý AI
     */
    @Transactional
    public String getHint(Integer attemptId, Integer questionId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lượt làm bài"));

        // Giới hạn 3 lần gợi ý
        if (attempt.getAiHintCount() >= 3) {
            return "Bạn đã hết lượt gợi ý cho bài quiz này.";
        }

        // Tăng bộ đếm
        attempt.setAiHintCount(attempt.getAiHintCount() + 1);
        quizAttemptRepository.save(attempt);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Câu hỏi"));

        return aiService.getHintForQuestion(question.getContent());
    }

    /**
     * (Màn 17/18) Nộp bài và Chấm điểm (Data cố định)
     */
    @Transactional
    public QuizAttempt gradeQuiz(Integer attemptId, Integer userId) {
        QuizAttempt attempt = getAttemptForTaking(attemptId, userId);

        // Lấy tất cả câu trả lời của lượt này
        List<QuizAttemptAnswer> answers = quizAttemptAnswerRepository.findByAttempt_Id(attemptId);

        // Đếm số câu đúng
        int correctCount = 0;
        for (QuizAttemptAnswer answer : answers) {
            if (answer.isCorrect()) {
                correctCount++;
            }
        }

        // Lấy tổng số câu hỏi
        int totalQuestions = attempt.getQuiz().getQuestions().size();
        if (totalQuestions == 0) {
            totalQuestions = 1; // Tránh chia cho 0
        }

        // Tính điểm (thang 100)
        double score = ((double) correctCount / totalQuestions) * 100;

        attempt.setScore(BigDecimal.valueOf(score));
        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus("COMPLETED");

        // So sánh với % pass
        if (score >= attempt.getQuiz().getPassRatePercentage().doubleValue()) {
            attempt.setResult("Pass");
        } else {
            attempt.setResult("Fail");
        }

        return quizAttemptRepository.save(attempt);
    }
}