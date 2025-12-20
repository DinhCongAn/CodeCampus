package com.codecampus.service;

import com.codecampus.dto.QuestionReviewDto;
import com.codecampus.dto.QuizReviewDto;
import com.codecampus.dto.SaveAnswerRequest;
import com.codecampus.entity.*;
import com.codecampus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(QuizAttemptService.class);

    @Autowired private QuizAttemptRepository attemptRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private QuizAttemptAnswerRepository attemptAnswerRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public List<QuizAttempt> findAttemptsByUserAndQuiz(Integer userId, Integer quizId) {
        return attemptRepository.findByUserIdAndQuizIdOrderByStartTimeDesc(userId, quizId);
    }

    @Transactional
    public QuizAttempt createNewAttempt(Integer userId, Integer quizId) {
        User user = userRepository.getReferenceById(userId);
        Quiz quiz = quizRepository.getReferenceById(quizId);

        QuizAttempt newAttempt = new QuizAttempt();
        newAttempt.setUser(user);
        newAttempt.setQuiz(quiz);
        return attemptRepository.save(newAttempt);
    }

    @Transactional(readOnly = true)
    public QuizAttempt getLiveAttempt(Integer attemptId, Integer userId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lần làm bài."));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền truy cập.");
        }

        if (!"in_progress".equals(attempt.getStatus())) {
            throw new RuntimeException("Bài làm này đã kết thúc.");
        }

        return attempt;
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptAnswer> getSavedAnswers(Integer attemptId) {
        return attemptAnswerRepository.findByAttempt_Id(attemptId);
    }

    @Transactional
    public void saveAnswer(SaveAnswerRequest request, Integer userId) {
        QuizAttempt attempt = getLiveAttempt(request.getAttemptId(), userId);

        QuizAttemptAnswer answer = attemptAnswerRepository
                .findByAttempt_IdAndQuestion_Id(request.getAttemptId(), request.getQuestionId())
                .orElse(new QuizAttemptAnswer());

        answer.setAttempt(attempt);
        answer.setQuestion(questionRepository.getReferenceById(request.getQuestionId()));
        answer.setSelectedAnswerOption(answerOptionRepository.getReferenceById(request.getAnswerId()));

        attemptAnswerRepository.save(answer);
    }

    @Transactional
    public void incrementHintCount(Integer attemptId, Integer userId) {
        QuizAttempt attempt = getLiveAttempt(attemptId, userId);
        attempt.setAiHintCount(attempt.getAiHintCount() + 1);
        attemptRepository.save(attempt);
    }

    @Transactional
    public QuizAttempt submitAndGradeQuiz(Integer attemptId, Integer userId) {

        QuizAttempt attempt = getLiveAttempt(attemptId, userId);
        Quiz quiz = attempt.getQuiz();

        // === LẤY DANH SÁCH CÂU HỎI ACTIVE TẠI THỜI ĐIỂM CHẤM ===
        List<Question> activeQuestions =
                questionRepository.findActiveQuestionsByQuizIdWithOptions(quiz.getId());

        int totalQuestions = activeQuestions.size();

        // Lấy câu trả lời user
        List<QuizAttemptAnswer> userAnswers =
                attemptAnswerRepository.findByAttempt_Id(attemptId);

        Set<Integer> correctAnswerIds =
                answerOptionRepository.findCorrectAnswerIdsByQuizId(quiz.getId());

        int correctCount = 0;

        for (QuizAttemptAnswer userAnswer : userAnswers) {
            if (userAnswer.getSelectedAnswerOption() != null
                    && correctAnswerIds.contains(userAnswer.getSelectedAnswerOption().getId())) {
                correctCount++;
            }
        }

        BigDecimal score = BigDecimal.ZERO;
        if (totalQuestions > 0) {
            score = BigDecimal.valueOf(correctCount)
                    .divide(BigDecimal.valueOf(totalQuestions), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        attempt.setScore(score.setScale(2, RoundingMode.HALF_UP));
        attempt.setResult(
                score.compareTo(quiz.getPassRatePercentage()) >= 0 ? "Pass" : "Fail"
        );
        attempt.setStatus("completed");
        attempt.setEndTime(LocalDateTime.now());

        return attemptRepository.save(attempt);
    }

    @Transactional(readOnly = true)
    public QuizReviewDto getQuizReview(Integer attemptId, Integer userId) {

        QuizAttempt attempt = attemptRepository.findCompletedAttemptById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài làm đã hoàn thành."));

        // === CHECK QUYỀN (GIỮ NGUYÊN) ===
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));
        String userRole = (currentUser.getRole() != null)
                ? currentUser.getRole().getName()
                : "STUDENT";

        if (!attempt.getUser().getId().equals(userId) && !userRole.equals("ADMIN")) {
            throw new RuntimeException("Không có quyền xem kết quả này.");
        }

        // === NGUỒN DỮ LIỆU CHÍNH: QuizAttemptAnswer ===
        List<QuizAttemptAnswer> userAnswersList =
                attemptAnswerRepository.findByAttempt_Id(attemptId);

        // Map để tra cứu nhanh
        Map<Integer, QuizAttemptAnswer> userAnswerMap = userAnswersList.stream()
                .collect(Collectors.toMap(
                        ans -> ans.getQuestion().getId(),
                        ans -> ans
                ));

        // Lấy danh sách Question từ attempt (KHÔNG QUAN TÂM ACTIVE/INACTIVE)
        List<Question> questionsInAttempt = userAnswersList.stream()
                .map(QuizAttemptAnswer::getQuestion)
                .distinct()
                .collect(Collectors.toList());

        // === BUILD DTO ===
        Quiz quiz = attempt.getQuiz();

        QuizReviewDto reviewDto = new QuizReviewDto();
        reviewDto.setAttemptId(attemptId);
        reviewDto.setQuizId(quiz.getId());
        reviewDto.setQuizName(quiz.getName());
        reviewDto.setScore(attempt.getScore());
        reviewDto.setResult(attempt.getResult());
        reviewDto.setTotalQuestions(questionsInAttempt.size());

        int correctCount = 0;
        List<QuestionReviewDto> questionDtos = new ArrayList<>();

        for (Question q : questionsInAttempt) {

            QuestionReviewDto qDto = new QuestionReviewDto();
            qDto.setQuestionId(q.getId());
            qDto.setContent(q.getContent());
            qDto.setExplanation(q.getExplanation());

            AnswerOption correctAnswer = q.getAnswerOptions().stream()
                    .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                    .findFirst()
                    .orElse(null);

            QuizAttemptAnswer userAnswer = userAnswerMap.get(q.getId());
            AnswerOption selectedOption =
                    (userAnswer != null) ? userAnswer.getSelectedAnswerOption() : null;

            qDto.setCorrectAnswerContent(
                    correctAnswer != null ? correctAnswer.getContent() : "Không có đáp án đúng"
            );
            qDto.setUserAnswerContent(
                    selectedOption != null ? selectedOption.getContent() : "(Bỏ qua)"
            );

            boolean isCorrect =
                    selectedOption != null &&
                            correctAnswer != null &&
                            selectedOption.getId().equals(correctAnswer.getId());

            qDto.setCorrect(isCorrect);

            if (isCorrect) correctCount++;

            questionDtos.add(qDto);
        }

        reviewDto.setCorrectCount(correctCount);
        reviewDto.setQuestions(questionDtos);

        return reviewDto;
    }
}