package com.codecampus.service;

import com.codecampus.dto.GeneratedQuestionDTO;
import com.codecampus.dto.QuestionSaveRequest; // <<-- Bổ sung
import com.codecampus.dto.SaveResultDetail;   // <<-- Bổ sung
import com.codecampus.dto.SaveResultDetail.FailEntry; // <<-- Bổ sung
import com.codecampus.dto.SaveResultDetail.SuccessEntry; // <<-- Bổ sung
import com.codecampus.entity.*;
import com.codecampus.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation; // <<-- Bổ sung
import jakarta.validation.Validator;           // <<-- Bổ sung
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private QuestionLevelRepository questionLevelRepository;

    @Autowired
    private AiLearningService aiLearningService;

    @Autowired // <<-- BỔ SUNG
    private Validator validator;

    // --- 1. LẤY DỮ LIỆU HIỂN THỊ (GIỮ NGUYÊN) ---

    public Page<Question> getQuestionsByFilters(String keyword, Integer courseId, Integer quizId, Integer levelId, String status, int page, int size) {
        if (status != null && status.trim().isEmpty()) {
            status = null;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return questionRepository.findByFilters(keyword, courseId, quizId, levelId, status, pageable);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<QuestionLevel> getAllLevels() {
        return questionLevelRepository.findAll();
    }

    public List<Quiz> getQuizzesByCourseId(Integer courseId) {
        if (courseId == null) return new ArrayList<>();
        return quizRepository.findByCourseId(Long.valueOf(courseId));
    }

    public Optional<Question> getQuestionById(Integer id) {
        return questionRepository.findById(id);
    }



    // --- 2. LOGIC LƯU CÂU HỎI (GIỮ NGUYÊN) ---

    @Transactional
    public void saveOrUpdateQuestion(Integer id, Question dto, Integer correctIndex, Integer quizId) {
        // ... (Logic cũ giữ nguyên) ...
        Question question;
        if (id != null) {
            question = questionRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi ID: " + id));
        } else {
            question = new Question();
            question.setStatus("active");
        }

        question.setContent(dto.getContent());
        question.setExplanation(dto.getExplanation());

        if (dto.getCourse() != null && dto.getCourse().getId() != null) {
            question.setCourse(courseRepository.findById(Long.valueOf(dto.getCourse().getId())).orElse(null));
        }

        if (dto.getLesson() != null && dto.getLesson().getId() != null) {
            question.setLesson(lessonRepository.findById(dto.getLesson().getId()).orElse(null));
        } else {
            question.setLesson(null);
        }

        if (dto.getQuestionLevel() != null && dto.getQuestionLevel().getId() != null) {
            question.setQuestionLevel(questionLevelRepository.findById(dto.getQuestionLevel().getId()).orElse(null));
        }

        List<AnswerOption> incomingOptions = dto.getAnswerOptions();
        if (incomingOptions != null) {
            List<AnswerOption> currentOptions = question.getAnswerOptions();
            if (currentOptions == null) currentOptions = new ArrayList<>();

            if (id == null) currentOptions.clear();

            for (int i = 0; i < incomingOptions.size(); i++) {
                if (i >= 4) break;

                AnswerOption inOpt = incomingOptions.get(i);
                AnswerOption dbOpt;

                if (i < currentOptions.size()) {
                    dbOpt = currentOptions.get(i);
                } else {
                    dbOpt = new AnswerOption();
                    dbOpt.setQuestion(question);
                    currentOptions.add(dbOpt);
                }

                dbOpt.setContent(inOpt.getContent());
                dbOpt.setIsCorrect(i == correctIndex);
                dbOpt.setOrderNumber(i + 1);
            }
            question.setAnswerOptions(currentOptions);
        }

        question = questionRepository.save(question);

        if (quizId != null) {
            Quiz quiz = quizRepository.findById(quizId).orElse(null);
            if (quiz != null) {
                List<Question> quizQuestions = quiz.getQuestions();
                if (quizQuestions == null) quizQuestions = new ArrayList<>();

                Question finalQuestion = question;
                boolean exists = quizQuestions.stream().anyMatch(q -> q.getId().equals(finalQuestion.getId()));

                if (!exists) {
                    quizQuestions.add(question);
                    quiz.setQuestions(quizQuestions);
                    quizRepository.save(quiz);
                }
            }
        }
    }

    // --- BỔ SUNG: LOGIC LƯU AI VÀ VALIDATION (CORE) ---

    @Transactional
    public SaveResultDetail saveAiGeneratedQuestions(List<QuestionSaveRequest> requests) {

        SaveResultDetail result = SaveResultDetail.builder()
                .totalRecords(requests.size())
                .failDetails(new ArrayList<>())
                .successDetails(new ArrayList<>()) // <<-- KHỞI TẠO SUCCESS DETAILS
                .build();

        int successCount = 0;

        for (QuestionSaveRequest request : requests) {

            String sourceData = (request.getContent() != null) ?
                    request.getContent().substring(0, Math.min(request.getContent().length(), 80)) + "..." : "N/A";

            List<String> currentErrors = new ArrayList<>();

            // A. Validation Annotation (@NotBlank, @NotNull, @Size...)
            Set<ConstraintViolation<QuestionSaveRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                violations.stream()
                        .map(violation -> violation.getPropertyPath().toString() + ": " + violation.getMessage())
                        .forEach(currentErrors::add);
            }

            // B. Business Logic Validation (1 đáp án đúng)
            String businessError = validateBusinessRules(request);
            if (businessError != null) {
                currentErrors.add(businessError);
            }

            // 2. Xử lý kết quả kiểm tra
            if (currentErrors.isEmpty()) {
                try {
                    // Chuyển đổi và lưu
                    Question question = convertToEntity(request);
                    questionRepository.save(question);

                    // Xử lý Quiz Many-to-Many
                    if (request.getQuizId() != null) {
                        Quiz quiz = quizRepository.findById(Math.toIntExact(request.getQuizId())).orElse(null);
                        if (quiz != null) {
                            if (quiz.getQuestions() == null) quiz.setQuestions(new ArrayList<>());
                            boolean exists = quiz.getQuestions().stream().anyMatch(q -> q.getId() != null && q.getId().equals(question.getId()));
                            if (!exists) {
                                quiz.getQuestions().add(question);
                                quizRepository.save(quiz);
                            }
                        }
                    }

                    successCount++;

                    // <<-- THÊM VÀO SUCCESS DETAILS
                    result.getSuccessDetails().add(SuccessEntry.builder()
                            .originalRequest(request)
                            .message("Đã lưu thành công (ID: " + question.getId() + ")")
                            .build());

                } catch (Exception e) {
                    currentErrors.add("Lỗi lưu DB: " + e.getMessage());
                }
            }

            // 3. Báo cáo lỗi
            if (!currentErrors.isEmpty()) {
                String fullErrorMessage = String.join("; ", currentErrors);
                result.getFailDetails().add(FailEntry.builder()
                        .sourceData(sourceData)
                        .errorMessage(fullErrorMessage)
                        .originalRequest(request) // <<-- THÊM DỮ LIỆU GỐC LỖI
                        .build());
            }
        }

        result.setSuccessCount(successCount);
        result.setFailCount(requests.size() - successCount);
        return result;
    }

    // Hàm kiểm tra nghiệp vụ: Phải có ĐÚNG 1 đáp án đúng
    private String validateBusinessRules(QuestionSaveRequest request) {
        if (request.getAnswerOptions() == null) return null;

        long correctCount = request.getAnswerOptions().stream()
                .filter(option -> option.getIsCorrect() != null && option.getIsCorrect())
                .count();

        if (correctCount != 1) {
            return "Phải có chính xác 1 đáp án đúng (Tìm thấy: " + correctCount + ").";
        }
        return null;
    }

    // Hàm chuyển đổi DTO sang Entity
    private Question convertToEntity(QuestionSaveRequest request) {
        Question question = new Question();

        question.setCourse(courseRepository.findById(request.getCourseId()).orElse(null));
        question.setLesson(request.getLessonId() != null ? lessonRepository.findById(request.getLessonId()).orElse(null) : null);
        question.setQuestionLevel(questionLevelRepository.findById(Math.toIntExact(request.getLevelId())).orElse(null));

        question.setContent(request.getContent());
        question.setExplanation(request.getExplanation());
        question.setStatus("active");

        List<AnswerOption> options = request.getAnswerOptions().stream()
                .map(optionReq -> {
                    AnswerOption option = new AnswerOption();
                    option.setContent(optionReq.getContent());
                    option.setIsCorrect(optionReq.getIsCorrect());
                    option.setQuestion(question);
                    option.setOrderNumber(request.getAnswerOptions().indexOf(optionReq) + 1);
                    return option;
                })
                .collect(Collectors.toList());

        question.setAnswerOptions(options);
        return question;
    }


    // --- 3. LOGIC IMPORT EXCEL (GIỮ NGUYÊN) ---

    @Transactional
    public Map<String, Object> importQuestionsFromExcel(MultipartFile file, Integer courseId, Integer quizId, Integer lessonId) {
        // ... (Logic cũ giữ nguyên) ...
        List<Map<String, Object>> importDetails = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        Set<String> existingContents = new HashSet<>();
        questionRepository.findAll().forEach(q -> existingContents.add(normalizeContent(q.getContent())));

        Course uiCourse = (courseId != null) ? courseRepository.findById(Long.valueOf(courseId)).orElse(null) : null;
        Lesson uiLesson = (lessonId != null) ? lessonRepository.findById(Long.valueOf(lessonId)).orElse(null) : null;
        Quiz targetQuiz = (quizId != null) ? quizRepository.findById(quizId).orElse(null) : null;

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowIdx = 0;

            for (Row row : sheet) {
                rowIdx++;
                if (rowIdx == 1) continue;

                Map<String, Object> rowResult = new HashMap<>();
                rowResult.put("row", rowIdx);

                try {
                    String excelCourse = getCellString(row, 0);
                    String excelLesson = getCellString(row, 1);
                    String excelLevel  = getCellString(row, 2);
                    String rawContent  = getCellString(row, 3);
                    String explanation = getCellString(row, 4);
                    String mediaUrl    = getCellString(row, 5);
                    String answerA     = getCellString(row, 6);
                    String answerB     = getCellString(row, 7);
                    String answerC     = getCellString(row, 8);
                    String answerD     = getCellString(row, 9);
                    String correct     = getCellString(row, 10);

                    rowResult.put("course", (uiCourse != null) ? uiCourse.getName() : excelCourse);
                    rowResult.put("lesson", (uiLesson != null) ? uiLesson.getName() : excelLesson);
                    rowResult.put("level", excelLevel);
                    rowResult.put("content", rawContent);
                    rowResult.put("explanation", explanation);
                    rowResult.put("media", mediaUrl);
                    rowResult.put("a", answerA);
                    rowResult.put("b", answerB);
                    rowResult.put("c", answerC);
                    rowResult.put("d", answerD);
                    rowResult.put("correct", correct);

                    if (rawContent.isEmpty()) continue;

                    String normalized = normalizeContent(rawContent);
                    if (existingContents.contains(normalized)) {
                        throw new IllegalArgumentException("Đã tồn tại (Trùng lặp)");
                    }

                    if (answerA.isEmpty() || answerB.isEmpty()) throw new IllegalArgumentException("Thiếu đáp án A/B");
                    if (correct.isEmpty()) throw new IllegalArgumentException("Chưa chọn đáp án đúng");

                    Question q = new Question();
                    q.setContent(rawContent);
                    q.setExplanation(explanation);
                    q.setMediaUrl(mediaUrl);
                    q.setStatus("active");

                    if (uiCourse != null) q.setCourse(uiCourse);
                    else if (!excelCourse.isEmpty()) {
                        courseRepository.findByName(excelCourse).ifPresent(q::setCourse);
                    } else {
                        throw new IllegalArgumentException("Thiếu thông tin Khóa học");
                    }
                    if (q.getCourse() == null) throw new IllegalArgumentException("Không tìm thấy khóa học: " + excelCourse);

                    if (uiLesson != null) q.setLesson(uiLesson);

                    questionLevelRepository.findByName(excelLevel).ifPresent(q::setQuestionLevel);

                    List<AnswerOption> options = new ArrayList<>();
                    options.add(createAnswerOption(q, answerA, correct.toUpperCase().contains("A"), 1));
                    options.add(createAnswerOption(q, answerB, correct.toUpperCase().contains("B"), 2));
                    if (!answerC.isEmpty()) options.add(createAnswerOption(q, answerC, correct.toUpperCase().contains("C"), 3));
                    if (!answerD.isEmpty()) options.add(createAnswerOption(q, answerD, correct.toUpperCase().contains("D"), 4));
                    q.setAnswerOptions(options);

                    q = questionRepository.save(q);

                    if (targetQuiz != null) {
                        List<Question> qq = targetQuiz.getQuestions();
                        if (qq == null) qq = new ArrayList<>();
                        qq.add(q);
                    }

                    existingContents.add(normalized);
                    successCount++;
                    rowResult.put("status", "success");
                    rowResult.put("message", "Thành công");

                } catch (Exception e) {
                    errorCount++;
                    rowResult.put("status", "error");
                    rowResult.put("message", e.getMessage());
                }
                importDetails.add(rowResult);
            }

            if (targetQuiz != null && successCount > 0) {
                targetQuiz.setQuestions(targetQuiz.getQuestions());
                quizRepository.save(targetQuiz);
            }

            Map<String, Object> finalResult = new HashMap<>();
            finalResult.put("total", successCount + errorCount);
            finalResult.put("success", successCount);
            finalResult.put("error", errorCount);
            finalResult.put("details", importDetails);

            return finalResult;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi file: " + e.getMessage());
        }
    }


    // --- 4. HÀM HỖ TRỢ & AI (GIỮ NGUYÊN) ---

    private String normalizeContent(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase().replaceAll("[\\s\\u00A0]+", " ");
    }

    @Transactional
    public String toggleStatus(Integer id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi ID: " + id));

        if ("active".equalsIgnoreCase(question.getStatus())) {
            question.setStatus("inactive");
        } else {
            question.setStatus("active");
        }

        questionRepository.save(question);
        return question.getStatus();
    }

    public ByteArrayInputStream generateImportTemplate() throws IOException {
        // ... (Logic cũ giữ nguyên) ...
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Questions");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setWrapText(true);
            dataStyle.setVerticalAlignment(VerticalAlignment.TOP);

            Row header = sheet.createRow(0);
            String[] cols = {
                    "Khóa học (Để trống nếu chọn trên Web)",
                    "Bài học (Để trống nếu chọn trên Web)",
                    "Mức độ (Dễ/Trung bình/Khó)",
                    "Nội dung câu hỏi *",
                    "Giải thích (Optional)",
                    "Media URL (Optional)",
                    "Đáp án A *",
                    "Đáp án B *",
                    "Đáp án C",
                    "Đáp án D",
                    "Đáp án đúng (A/B/C/D) *"
            };

            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
                if(i == 3 || i == 4) sheet.setColumnWidth(i, 10000);
                else sheet.setColumnWidth(i, 4000);
            }

            List<String[]> sampleData = new ArrayList<>();
            sampleData.add(new String[]{"", "", "Dễ", "Java là ngôn ngữ lập trình kiểu gì?", "Java là ngôn ngữ định kiểu tĩnh (Statically Typed).", "", "Kiểu động", "Kiểu tĩnh", "Không có kiểu", "Cả A và B", "B"});

            int rowNum = 1;
            for (String[] data : sampleData) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < data.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(data[i]);
                    cell.setCellStyle(dataStyle);
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private String getCellString(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int) cell.getNumericCellValue());
        return cell.getStringCellValue().trim();
    }

    private AnswerOption createAnswerOption(Question q, String content, boolean correct, int order) {
        AnswerOption o = new AnswerOption();
        o.setQuestion(q);
        o.setContent(content);
        o.setIsCorrect(correct);
        o.setOrderNumber(order);
        return o;
    }

    public List<GeneratedQuestionDTO> generateQuestionsWithAi(Integer courseId, Integer quizId, Integer lessonId, Integer levelId, String description, int numberOfQuestions) {
        // ... (Logic cũ giữ nguyên) ...
        String courseName = courseRepository.findById(Long.valueOf(courseId)).map(Course::getName).orElse("Lập trình");
        String levelName = (levelId != null) ? questionLevelRepository.findById(levelId).map(QuestionLevel::getName).orElse("Trung bình") : "Trung bình";
        String lessonContext = (lessonId != null) ? lessonRepository.findById(Long.valueOf(lessonId)).map(Lesson::getName).orElse("") : "";

        String prompt = String.format("""
        Bạn là một giảng viên chuyên nghiệp về chủ đề: %s.
        Hãy soạn thảo %d câu hỏi trắc nghiệm (Multiple Choice).
        
        Bối cảnh:
        - Môn học: %s
        - Mức độ: %s
        %s
        - Yêu cầu thêm: %s
        
        Yêu cầu Output định dạng JSON Array thuần túy (không bọc trong markdown code block), với cấu trúc object như sau:
        [
          {
            "content": "Nội dung câu hỏi?",
            "explanation": "Giải thích ngắn gọn vì sao đúng",
            "answerA": "Đáp án A",
            "answerB": "Đáp án B",
            "answerC": "Đáp án C",
            "answerD": "Đáp án D",
            "correctChar": "A" (hoặc B, C, D - chỉ lấy 1 ký tự in hoa)
          }
        ]
        Đảm bảo JSON hợp lệ, không thừa dấu phẩy.
        """,
                courseName, numberOfQuestions, courseName, levelName,
                (!lessonContext.isEmpty() ? "- Bài học cụ thể: " + lessonContext : ""),
                description);

        String jsonResponse = aiLearningService.callGeminiApi(prompt, "generate_quiz");

        return parseJsonToDtoList(jsonResponse, courseId, quizId, lessonId, levelId);
    }

    private List<GeneratedQuestionDTO> parseJsonToDtoList(String json, Integer cId, Integer qId, Integer lId, Integer levId) {
        // ... (Logic cũ giữ nguyên) ...
        try {
            if (json == null || json.trim().isEmpty()) {
                throw new RuntimeException("AI không trả về dữ liệu nào.");
            }

            if (json.trim().startsWith("Lỗi")) {
                throw new RuntimeException(json);
            }

            ObjectMapper mapper = new ObjectMapper();

            int firstBracket = json.indexOf("[");
            int lastBracket = json.lastIndexOf("]");

            if (firstBracket != -1 && lastBracket != -1 && firstBracket < lastBracket) {
                json = json.substring(firstBracket, lastBracket + 1);
            } else {
                throw new RuntimeException("AI trả về định dạng không đúng (Không tìm thấy dấu [ ]).");
            }

            List<GeneratedQuestionDTO> list = mapper.readValue(json, new TypeReference<List<GeneratedQuestionDTO>>(){});

            list.forEach(dto -> {
                dto.setCourseId(cId);
                dto.setQuizId(qId);
                dto.setLessonId(lId);
                dto.setLevelId(levId);
            });
            return list;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi xử lý dữ liệu AI: " + e.getMessage());
        }
    }
}