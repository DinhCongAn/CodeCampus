package com.codecampus.service;

import com.codecampus.dto.AiQuizResponse;
import com.codecampus.dto.QuestionForm;
import com.codecampus.entity.*;
import com.codecampus.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private QuestionLevelRepository levelRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private AiLearningService aiLearningService;

    // =========================================================================
    // 1. LẤY DANH SÁCH (ADMIN PAGE)
    // =========================================================================
    public Page<Question> getQuestionsAdmin(String keyword, Integer courseId, Integer quizId, Integer levelId, int page, int size) {
        return questionRepository.findQuestionsAdmin(keyword, courseId, quizId, levelId, PageRequest.of(page, size));
    }

    // =========================================================================
    // 2. LẤY CHI TIẾT ĐỂ SỬA (CHO MODAL)
    // =========================================================================
    public QuestionForm getQuestionDetail(Integer id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi ID: " + id));

        QuestionForm form = new QuestionForm();
        form.setId(q.getId());
        form.setContent(q.getContent());
        form.setExplanation(q.getExplanation());

        if (q.getCourse() != null) form.setCourseId(q.getCourse().getId().intValue());
        if (q.getQuestionLevel() != null) form.setLevelId(q.getQuestionLevel().getId());

        // Nếu câu hỏi thuộc về các Quiz, lấy Quiz đầu tiên để hiển thị (nếu có)
        if (q.getQuizzes() != null && !q.getQuizzes().isEmpty()) {
            form.setQuizId(q.getQuizzes().get(0).getId());
        }

        // Map danh sách đáp án
        List<QuestionForm.AnswerForm> ansForms = q.getAnswerOptions().stream().map(a -> {
            QuestionForm.AnswerForm af = new QuestionForm.AnswerForm();
            af.setContent(a.getContent());
            af.setCorrect(a.isCorrect());
            return af;
        }).collect(Collectors.toList());
        form.setAnswers(ansForms);

        return form;
    }

    // =========================================================================
    // 3. LƯU THỦ CÔNG (THÊM MỚI / CẬP NHẬT)
    // =========================================================================
    @Transactional
    public void saveQuestionManual(QuestionForm form) {
        Question q;
        // A. Kiểm tra Sửa hay Thêm
        if (form.getId() != null) {
            q = questionRepository.findById(form.getId()).orElse(new Question());
            // Nếu là sửa, xóa đáp án cũ đi để lưu lại bộ mới
            answerOptionRepository.deleteByQuestionId(q.getId());
            q.getAnswerOptions().clear();
        } else {
            q = new Question();
            q.setAnswerOptions(new ArrayList<>());
        }

        // B. XỬ LÝ LOGIC COURSE VÀ QUIZ (QUAN TRỌNG)
        Course course = null;
        Quiz quiz = null;

        // Ưu tiên 1: Nếu có QuizId -> Lấy Course từ Quiz
        if (form.getQuizId() != null) {
            quiz = quizRepository.findById(form.getQuizId()).orElse(null);
            if (quiz != null) {
                course = quiz.getCourse(); // Tự động lấy môn của Quiz
            }
        }

        // Ưu tiên 2: Nếu không có Quiz, lấy từ CourseId form gửi lên
        if (course == null && form.getCourseId() != null) {
            course = courseRepository.findById(Long.valueOf(form.getCourseId()))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học!"));
        }

        if (course == null) {
            throw new RuntimeException("Bắt buộc phải có Môn học hoặc Bài Quiz!");
        }

        // C. Gán dữ liệu vào Entity
        q.setContent(form.getContent());
        q.setExplanation(form.getExplanation());
        q.setStatus("active");
        q.setCourse(course);
        q.setQuestionLevel(levelRepository.findById(form.getLevelId()).orElseThrow());

        // D. Lưu Câu hỏi
        Question savedQ = questionRepository.save(q);

        // E. Lưu Đáp án
        if (form.getAnswers() != null) {
            List<AnswerOption> options = new ArrayList<>();
            for (QuestionForm.AnswerForm ansForm : form.getAnswers()) {
                AnswerOption ans = new AnswerOption();
                ans.setContent(ansForm.getContent());
                ans.setCorrect(ansForm.isCorrect());
                ans.setQuestion(savedQ);
                options.add(ans);
            }
            answerOptionRepository.saveAll(options);
        }

        // F. Gán câu hỏi vào Quiz (Nếu có)
        if (quiz != null) {
            assignToQuiz(savedQ, quiz);
        }
    }

    // =========================================================================
    // 4. TẠO TỰ ĐỘNG BẰNG AI
    // =========================================================================
    @Transactional
    public void generateQuestionsAi(Integer courseId, Integer quizId, Integer levelId, String topic, int number) {
        // 1. Xác định Course & Quiz
        Quiz quiz = null;
        Course course;

        if (quizId != null) {
            quiz = quizRepository.findById(quizId).orElse(null);
        }

        // Nếu có Quiz, ưu tiên lấy Course của Quiz. Nếu không, lấy từ courseId truyền vào
        if (quiz != null) {
            course = quiz.getCourse();
        } else {
            course = courseRepository.findById(Long.valueOf(courseId))
                    .orElseThrow(() -> new RuntimeException("Môn học không tồn tại"));
        }

        QuestionLevel level = levelRepository.findById(levelId).orElseThrow();

        // 2. Gọi AI Service
        String promptContext = "Chủ đề: " + topic + ". Môn học: " + course.getName();
        String jsonResponse = aiLearningService.generateQuizJson(promptContext, number, level.getName());

        // 3. Parse JSON và Lưu
        try {
            ObjectMapper mapper = new ObjectMapper();
            AiQuizResponse response = mapper.readValue(jsonResponse, AiQuizResponse.class);

            if (response.getQuestions() == null) return;

            for (AiQuizResponse.AiQuestion aiQ : response.getQuestions()) {
                // Lưu câu hỏi và đáp án
                Question savedQ = createAndSaveQuestion(aiQ.getContent(), aiQ.getExplanation(), course, level, aiQ.getAnswers());

                // Gán vào Quiz
                if (quiz != null) {
                    assignToQuiz(savedQ, quiz);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý dữ liệu từ AI: " + e.getMessage());
        }
    }

    // =========================================================================
    // 5. IMPORT TỪ EXCEL
    // =========================================================================
    // =========================================================================
    // 5. IMPORT TỪ EXCEL (CHỈ CẦN QUIZ ID)
    // =========================================================================
    @Transactional
    public void importFromExcel(MultipartFile file, Integer quizId) throws IOException {
        // 1. Kiểm tra Quiz có tồn tại không
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Bài kiểm tra (Quiz) với ID: " + quizId));

        // 2. Lấy Môn học từ Quiz (Vì câu hỏi phải thuộc về môn của Quiz đó)
        Course course = quiz.getCourse();
        if (course == null) {
            throw new RuntimeException("Bài kiểm tra này chưa được gán Môn học nào. Vui lòng kiểm tra lại cấu hình Quiz.");
        }

        // Mặc định Level là Medium (ID=2) nếu file excel không có cột Level (hoặc bạn có thể thêm logic đọc cột Level)
        QuestionLevel defaultLevel = levelRepository.findById(2).orElse(null);

        // 3. Đọc file Excel
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Duyệt từ dòng 1 (Bỏ dòng Header)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Lấy nội dung câu hỏi
                String content = getCellValue(row, 0);
                if (content.isEmpty()) continue; // Bỏ qua dòng trống

                String explanation = getCellValue(row, 1);

                // Lấy vị trí đáp án đúng (1, 2, 3, 4)
                String correctStr = getCellValue(row, 6);
                int correctIdx = 1;
                try {
                    // Xử lý trường hợp người dùng nhập 1.0 hoặc "1"
                    correctIdx = (int) Double.parseDouble(correctStr.isEmpty() ? "1" : correctStr);
                } catch (Exception e) {
                    correctIdx = 1; // Mặc định là 1 nếu lỗi format
                }

                // Tạo danh sách 4 đáp án
                List<AiQuizResponse.AiAnswer> answers = new ArrayList<>();
                for (int j = 1; j <= 4; j++) {
                    String ansText = getCellValue(row, j + 1); // Cột 2, 3, 4, 5
                    AiQuizResponse.AiAnswer ans = new AiQuizResponse.AiAnswer();
                    ans.setContent(ansText);
                    ans.setCorrect(j == correctIdx);
                    answers.add(ans);
                }

                // 4. Lưu Câu hỏi vào DB (Helper method đã có ở bài trước)
                Question savedQ = createAndSaveQuestion(content, explanation, course, defaultLevel, answers);

                // 5. Gán Câu hỏi đó vào Quiz ngay lập tức
                assignToQuiz(savedQ, quiz);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
        }
    }

    // =========================================================================
    // 6. TẠO FILE EXCEL MẪU
    // =========================================================================
    public ByteArrayInputStream generateExcelTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Mau_Nhap_Cau_Hoi");

            // Style cho Header
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Nội dung câu hỏi (Bắt buộc)",
                    "Giải thích (Tùy chọn)",
                    "Đáp án 1", "Đáp án 2", "Đáp án 3", "Đáp án 4",
                    "Vị trí đúng (1-4)"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, i < 2 ? 8000 : 4000); // Cột nội dung rộng hơn
            }

            // Dữ liệu mẫu
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Java là ngôn ngữ lập trình hướng đối tượng đúng không?");
            sample.createCell(1).setCellValue("Java hỗ trợ OOP mạnh mẽ.");
            sample.createCell(2).setCellValue("Đúng");
            sample.createCell(3).setCellValue("Sai");
            sample.createCell(4).setCellValue("Không biết");
            sample.createCell(5).setCellValue("Có thể");
            sample.createCell(6).setCellValue(1); // Đáp án 1 là đúng

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi tạo file mẫu: " + e.getMessage());
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    // Helper: Tạo và lưu Question + AnswerOptions (Dùng chung cho AI và Excel)
    private Question createAndSaveQuestion(String content, String explain, Course c, QuestionLevel l, List<AiQuizResponse.AiAnswer> answers) {
        Question q = new Question();
        q.setContent(content);
        q.setExplanation(explain);
        q.setCourse(c);
        q.setQuestionLevel(l);
        q.setStatus("active");

        Question savedQ = questionRepository.save(q);

        List<AnswerOption> options = new ArrayList<>();
        for (AiQuizResponse.AiAnswer a : answers) {
            AnswerOption ao = new AnswerOption();
            ao.setContent(a.getContent());
            ao.setCorrect(a.isCorrect());
            ao.setQuestion(savedQ);
            options.add(ao);
        }
        answerOptionRepository.saveAll(options);
        return savedQ;
    }

    // Helper: Gán Question vào Quiz
    private void assignToQuiz(Question q, Quiz quiz) {
        // Kiểm tra xem câu hỏi đã có trong Quiz chưa để tránh trùng lặp
        if (!quiz.getQuestions().contains(q)) {
            quiz.getQuestions().add(q);
            quizRepository.save(quiz); // Lưu lại Quiz để cập nhật bảng trung gian
        }
    }

    // Helper: Đọc Cell Excel an toàn
    private String getCellValue(Row row, int idx) {
        Cell c = row.getCell(idx);
        if (c == null) return "";
        DataFormatter df = new DataFormatter();
        return df.formatCellValue(c).trim();
    }
}