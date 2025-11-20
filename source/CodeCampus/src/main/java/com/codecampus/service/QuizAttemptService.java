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
        List<QuizAttemptAnswer> userAnswers = getSavedAnswers(attemptId);

        Set<Integer> correctAnswers = answerOptionRepository
                .findCorrectAnswerIdsByQuizId(quiz.getId());

        int totalQuestions = quiz.getQuestions().size();
        int correctCount = 0;

        for (QuizAttemptAnswer userAnswer : userAnswers) {
            if (userAnswer.getSelectedAnswerOption() != null &&
                    correctAnswers.contains(userAnswer.getSelectedAnswerOption().getId())) {
                correctCount++;
            }
        }

        BigDecimal score = BigDecimal.ZERO;
        if (totalQuestions > 0) {
            score = new BigDecimal(correctCount)
                    .divide(new BigDecimal(totalQuestions), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }

        String result = (score.compareTo(quiz.getPassRatePercentage()) >= 0) ? "Pass" : "Fail";

        attempt.setStatus("completed");
        attempt.setEndTime(LocalDateTime.now());
        attempt.setScore(score.setScale(2, RoundingMode.HALF_UP));
        attempt.setResult(result);

        return attemptRepository.save(attempt);
    }

    /**
     * SỬA LẠI LOGIC CỦA HÀM NÀY (Đã sửa lỗi 999)
     */
    @Transactional(readOnly = true)
    public QuizReviewDto getQuizReview(Integer attemptId, Integer userId) {
        // 1. Lấy attempt
        QuizAttempt attempt = attemptRepository.findCompletedAttemptById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài làm đã hoàn thành."));

        // 2. Xác thực (Đây là dòng 183 gây lỗi)
        logger.info("[DEBUG] getQuizReview: Checking user... (Request: {}, Owner: {})", userId, attempt.getUser().getId());

        // Lấy vai trò (Giả sử bạn có UserRole.java và liên kết getRole())
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng đang xem."));
        String userRole = (currentUser.getRole() != null) ? currentUser.getRole().getName() : "STUDENT";

        if (!attempt.getUser().getId().equals(userId) && !userRole.equals("ADMIN")) {
            logger.error("BẢO MẬT THẤT BẠI: User {} (Role: {}) đang cố xem bài của User {}",
                    userId, userRole, attempt.getUser().getId());
            throw new RuntimeException("Không có quyền xem kết quả này.");
        }

        logger.info("Bảo mật OK. Bắt đầu lấy dữ liệu review...");

        // 3. Lấy dữ liệu
        Quiz quiz = attempt.getQuiz();

        // === SỬA LOGIC CỐT LÕI TẠI ĐÂY ===
        // Nguồn dữ liệu chính: Lấy các câu hỏi CHÍNH THỨC của Quiz
        List<Question> questionsInQuiz = questionRepository.findQuestionsByQuizIdWithOptions(quiz.getId());

        // Nguồn dữ liệu phụ: Lấy các câu trả lời user đã LƯU
        List<QuizAttemptAnswer> userAnswersList = attemptAnswerRepository.findByAttempt_Id(attemptId);

        // Tạo Map (bộ tra cứu) từ danh sách phụ
        Map<Integer, QuizAttemptAnswer> userAnswerMap = userAnswersList.stream()
                .collect(Collectors.toMap(
                        ans -> ans.getQuestion().getId(), // Key là Question ID
                        ans -> ans                      // Value là toàn bộ object Answer
                ));

        // 4. Xây dựng DTO
        QuizReviewDto reviewDto = new QuizReviewDto();
        reviewDto.setAttemptId(attemptId);
        reviewDto.setQuizName(quiz.getName());
        reviewDto.setScore(attempt.getScore());
        reviewDto.setResult(attempt.getResult());
        reviewDto.setTotalQuestions(questionsInQuiz.size());

        reviewDto.setQuizId(quiz.getId());
        reviewDto.setLessonId(quiz.getId());

        int correctCount = 0;
        List<QuestionReviewDto> questionReviewDtos = new ArrayList<>();

        // === LUÔN LẶP QUA DANH SÁCH 'questionsInQuiz' (CHÍNH THỨC) ===
        // Vòng lặp này sẽ KHÔNG BAO GIỜ chạy câu 999 (dữ liệu rác)
        for (Question q : questionsInQuiz) {
            QuestionReviewDto qDto = new QuestionReviewDto();
            qDto.setQuestionId(q.getId()); // ID này sẽ luôn đúng
            qDto.setContent(q.getContent());
            qDto.setExplanation(q.getExplanation());

            // Tìm đáp án đúng từ 'q'
            AnswerOption correctAnswer = q.getAnswerOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .findFirst()
                    .orElse(null);

            // Tra cứu đáp án của user từ Map
            QuizAttemptAnswer userAnswer = userAnswerMap.get(q.getId());
            AnswerOption selectedOption = (userAnswer != null) ? userAnswer.getSelectedAnswerOption() : null;

            // Gán nội dung
            qDto.setCorrectAnswerContent(correctAnswer != null ? correctAnswer.getContent() : "Không có đáp án đúng");
            qDto.setUserAnswerContent(selectedOption != null ? selectedOption.getContent() : "(Bỏ qua)");

            // So sánh
            if (selectedOption != null && correctAnswer != null && selectedOption.getId().equals(correctAnswer.getId())) {
                qDto.setCorrect(true);
                correctCount++;
            } else {
                qDto.setCorrect(false);
            }
            questionReviewDtos.add(qDto);
        }
        // === KẾT THÚC SỬA LOGIC ===

        reviewDto.setCorrectCount(correctCount);
        reviewDto.setQuestions(questionReviewDtos);

        return reviewDto;
    }
}