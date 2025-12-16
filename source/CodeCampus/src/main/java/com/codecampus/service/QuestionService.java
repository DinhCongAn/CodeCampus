package com.codecampus.service;

import com.codecampus.dto.GeneratedQuestionDTO;
import com.codecampus.entity.*;
import com.codecampus.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository; // Bắt buộc để lưu vào bảng trung gian quiz_questions

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository; // Bắt buộc để lưu lesson_id

    @Autowired
    private QuestionLevelRepository questionLevelRepository;

    @Autowired
    private AiLearningService aiLearningService;

    // --- 1. LẤY DỮ LIỆU HIỂN THỊ ---

    // 1. Hàm lọc chính
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

    // Lấy Quiz theo Course để đổ vào Dropdown
    public List<Quiz> getQuizzesByCourseId(Integer courseId) {
        if (courseId == null) return new ArrayList<>();
        return quizRepository.findByCourseId(Long.valueOf(courseId));
    }

    public Optional<Question> getQuestionById(Integer id) {
        return questionRepository.findById(id);
    }



    // --- 2. LOGIC LƯU CÂU HỎI (QUAN TRỌNG NHẤT) ---

    @Transactional
    public void saveOrUpdateQuestion(Integer id, Question dto, Integer correctIndex, Integer quizId) {
        // BƯỚC 1: Khởi tạo/Lấy Question
        Question question;
        if (id != null) {
            question = questionRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi ID: " + id));
        } else {
            question = new Question();
            question.setStatus("active");
        }

        // BƯỚC 2: Map thông tin cơ bản
        question.setContent(dto.getContent());
        question.setExplanation(dto.getExplanation());
        // Nếu có mediaUrl
        // question.setMediaUrl(dto.getMediaUrl());

        // BƯỚC 3: Map Khóa ngoại (Course, Lesson, Level)
        if (dto.getCourse() != null && dto.getCourse().getId() != null) {
            question.setCourse(courseRepository.findById(Long.valueOf(dto.getCourse().getId())).orElse(null));
        }

        // FIX: Map Lesson ID (để không bị NULL trong DB)
        if (dto.getLesson() != null && dto.getLesson().getId() != null) {
            question.setLesson(lessonRepository.findById(dto.getLesson().getId()).orElse(null));
        } else {
            question.setLesson(null); // Nếu user chọn "-- Chọn Bài học --"
        }

        if (dto.getQuestionLevel() != null && dto.getQuestionLevel().getId() != null) {
            question.setQuestionLevel(questionLevelRepository.findById(dto.getQuestionLevel().getId()).orElse(null));
        }

        // BƯỚC 4: Map Đáp án (Answer Options)
        List<AnswerOption> incomingOptions = dto.getAnswerOptions();
        if (incomingOptions != null) {
            List<AnswerOption> currentOptions = question.getAnswerOptions();
            if (currentOptions == null) currentOptions = new ArrayList<>();

            // Nếu là tạo mới, clear list cũ để đảm bảo sạch
            if (id == null) currentOptions.clear();

            for (int i = 0; i < incomingOptions.size(); i++) {
                // Chỉ lấy tối đa 4 đáp án từ form
                if (i >= 4) break;

                AnswerOption inOpt = incomingOptions.get(i);
                AnswerOption dbOpt;

                // Logic Update hoặc Insert vào list
                if (i < currentOptions.size()) {
                    dbOpt = currentOptions.get(i);
                } else {
                    dbOpt = new AnswerOption();
                    dbOpt.setQuestion(question); // Gán khóa ngoại ngược lại
                    currentOptions.add(dbOpt);
                }

                dbOpt.setContent(inOpt.getContent());
                dbOpt.setIsCorrect(i == correctIndex); // Set đúng/sai theo radio button
                dbOpt.setOrderNumber(i + 1);
            }
            question.setAnswerOptions(currentOptions);
        }

        // BƯỚC 5: LƯU QUESTION (Phải lưu trước để sinh ID)
        question = questionRepository.save(question);

        // BƯỚC 6: FIX LỖI QUIZ (Lưu vào bảng trung gian quiz_questions)
        if (quizId != null) {
            Quiz quiz = quizRepository.findById(quizId).orElse(null);
            if (quiz != null) {
                // Lấy danh sách câu hỏi hiện tại của Quiz
                List<Question> quizQuestions = quiz.getQuestions();
                if (quizQuestions == null) quizQuestions = new ArrayList<>();

                // Kiểm tra xem đã có câu hỏi này chưa (tránh trùng lặp)
                Question finalQuestion = question;
                boolean exists = quizQuestions.stream().anyMatch(q -> q.getId().equals(finalQuestion.getId()));

                if (!exists) {
                    quizQuestions.add(question); // Thêm câu hỏi vào Quiz
                    quiz.setQuestions(quizQuestions);
                    quizRepository.save(quiz); // <--- QUAN TRỌNG: Hibernate sẽ insert vào bảng 'quiz_questions'
                }
            }
        }
    }

    // --- 3. LOGIC IMPORT EXCEL ---

    @Transactional
    public Map<String, Object> importQuestionsFromExcel(MultipartFile file, Integer courseId, Integer quizId, Integer lessonId) {
        List<Map<String, Object>> importDetails = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        Set<String> existingContents = new HashSet<>();
        questionRepository.findAll().forEach(q -> existingContents.add(normalizeContent(q.getContent())));

        // Lấy Entity từ UI (nếu có)
        Course uiCourse = (courseId != null) ? courseRepository.findById(Long.valueOf(courseId)).orElse(null) : null;
        Lesson uiLesson = (lessonId != null) ? lessonRepository.findById(Long.valueOf(lessonId)).orElse(null) : null;
        Quiz targetQuiz = (quizId != null) ? quizRepository.findById(quizId).orElse(null) : null;

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowIdx = 0;

            for (Row row : sheet) {
                rowIdx++;
                if (rowIdx == 1) continue; // Bỏ qua Header

                // Map kết quả dòng hiện tại
                Map<String, Object> rowResult = new HashMap<>();
                rowResult.put("row", rowIdx);

                try {
                    // --- 1. ĐỌC TOÀN BỘ CỘT TỪ EXCEL TRƯỚC (Để hiển thị full thông tin) ---
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

                    // Đưa vào Map kết quả NGAY LẬP TỨC
                    // Logic hiển thị: Nếu UI chọn Course/Lesson thì ưu tiên hiển thị cái của UI, nếu không thì lấy Excel
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

                    // --- 2. VALIDATE ---
                    if (rawContent.isEmpty()) continue; // Bỏ qua dòng trống

                    // Check trùng lặp
                    String normalized = normalizeContent(rawContent);
                    if (existingContents.contains(normalized)) {
                        throw new IllegalArgumentException("Đã tồn tại (Trùng lặp)");
                    }

                    // Check đáp án
                    if (answerA.isEmpty() || answerB.isEmpty()) throw new IllegalArgumentException("Thiếu đáp án A/B");
                    if (correct.isEmpty()) throw new IllegalArgumentException("Chưa chọn đáp án đúng");

                    // --- 3. TẠO ENTITY ---
                    Question q = new Question();
                    q.setContent(rawContent);
                    q.setExplanation(explanation);
                    q.setMediaUrl(mediaUrl);
                    q.setStatus("active");

                    // Gán Course
                    if (uiCourse != null) q.setCourse(uiCourse);
                    else if (!excelCourse.isEmpty()) {
                        // Tìm course theo tên Excel (Logic đơn giản hóa)
                        courseRepository.findByName(excelCourse).ifPresent(q::setCourse);
                    } else {
                        throw new IllegalArgumentException("Thiếu thông tin Khóa học");
                    }
                    if (q.getCourse() == null) throw new IllegalArgumentException("Không tìm thấy khóa học: " + excelCourse);

                    // Gán Lesson
                    if (uiLesson != null) q.setLesson(uiLesson);
                    // else logic tìm lesson...

                    // Gán Level
                    questionLevelRepository.findByName(excelLevel).ifPresent(q::setQuestionLevel);

                    // Gán Đáp án
                    List<AnswerOption> options = new ArrayList<>();
                    options.add(createAnswerOption(q, answerA, correct.toUpperCase().contains("A"), 1));
                    options.add(createAnswerOption(q, answerB, correct.toUpperCase().contains("B"), 2));
                    if (!answerC.isEmpty()) options.add(createAnswerOption(q, answerC, correct.toUpperCase().contains("C"), 3));
                    if (!answerD.isEmpty()) options.add(createAnswerOption(q, answerD, correct.toUpperCase().contains("D"), 4));
                    q.setAnswerOptions(options);

                    // Lưu DB
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

    // --- HÀM HỖ TRỢ (HELPER) ---

    // Hàm chuẩn hóa chuỗi để so sánh chính xác (tránh lỗi dấu cách ảo)
    private String normalizeContent(String input) {
        if (input == null) return "";
        // 1. .trim(): Xóa khoảng trắng 2 đầu
        // 2. .toLowerCase(): Chuyển về chữ thường
        // 3. .replaceAll(...): Thay thế nhiều dấu cách liên tiếp hoặc ký tự lạ (\u00A0) thành 1 dấu cách duy nhất
        return input.trim().toLowerCase().replaceAll("[\\s\\u00A0]+", " ");
    }

    @Transactional
    public String toggleStatus(Integer id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi ID: " + id));

        // Logic: Nếu đang active -> inactive, ngược lại -> active
        if ("active".equalsIgnoreCase(question.getStatus())) {
            question.setStatus("inactive");
        } else {
            question.setStatus("active");
        }

        questionRepository.save(question);
        return question.getStatus(); // Trả về trạng thái mới
    }

    // --- 4. EXCEL TEMPLATE & UTILS ---

    // --- 4. TẢI TEMPLATE KÈM DỮ LIỆU MẪU (TEST DATA) ---
    public ByteArrayInputStream generateImportTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Questions");

            // --- A. TẠO STYLE ---
            // 1. Style cho Header (In đậm, nền xám nhẹ, căn giữa)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 2. Style cho Dữ liệu (Wrap text - tự xuống dòng nếu dài)
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setWrapText(true);
            dataStyle.setVerticalAlignment(VerticalAlignment.TOP);

            // --- B. TẠO HEADER ---
            Row header = sheet.createRow(0);
            String[] cols = {
                    "Khóa học (Để trống nếu chọn trên Web)", // Col 0
                    "Bài học (Để trống nếu chọn trên Web)",  // Col 1
                    "Mức độ (Dễ/Trung bình/Khó)",            // Col 2
                    "Nội dung câu hỏi *",                    // Col 3
                    "Giải thích (Optional)",                 // Col 4
                    "Media URL (Optional)",                  // Col 5
                    "Đáp án A *",                            // Col 6
                    "Đáp án B *",                            // Col 7
                    "Đáp án C",                              // Col 8
                    "Đáp án D",                              // Col 9
                    "Đáp án đúng (A/B/C/D) *"                // Col 10
            };

            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
                // Set độ rộng cột tương đối
                if(i == 3 || i == 4) sheet.setColumnWidth(i, 10000); // Cột Nội dung & Giải thích rộng hơn
                else sheet.setColumnWidth(i, 4000);
            }

            // --- C. TẠO DỮ LIỆU MẪU (5 CÂU) ---
            List<String[]> sampleData = new ArrayList<>();

            // Mẫu 1: Câu hỏi cơ bản
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

    // 1. Hàm sinh câu hỏi (Chỉ trả về data, KHÔNG lưu DB)
    public List<GeneratedQuestionDTO> generateQuestionsWithAi(Integer courseId, Integer quizId, Integer lessonId, Integer levelId, String description, int numberOfQuestions) {

        // Lấy tên các thực thể để đưa vào Prompt cho chính xác
        String courseName = courseRepository.findById(Long.valueOf(courseId)).map(Course::getName).orElse("Lập trình");
        String levelName = (levelId != null) ? questionLevelRepository.findById(levelId).map(QuestionLevel::getName).orElse("Trung bình") : "Trung bình";
        String lessonContext = (lessonId != null) ? lessonRepository.findById(Long.valueOf(lessonId)).map(Lesson::getName).orElse("") : "";

        // Xây dựng Prompt (Kỹ thuật Prompt Engineering)
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

        // Gọi AI (Giả sử aiLearningService trả về String JSON raw)
        String jsonResponse = aiLearningService.callGeminiApi(prompt, "generate_quiz");

        // Parse JSON String sang List<GeneratedQuestionDTO>
        // (Bạn có thể dùng Jackson ObjectMapper hoặc Gson tại đây)
        return parseJsonToDtoList(jsonResponse, courseId, quizId, lessonId, levelId);
    }

    // Hàm phụ trợ parse JSON (Ví dụ dùng Jackson)
    private List<GeneratedQuestionDTO> parseJsonToDtoList(String json, Integer cId, Integer qId, Integer lId, Integer levId) {
        try {
            if (json == null || json.trim().isEmpty()) {
                throw new RuntimeException("AI không trả về dữ liệu nào.");
            }

            // Kiểm tra lỗi quota/server từ AI
            if (json.trim().startsWith("Lỗi")) {
                throw new RuntimeException(json);
            }

            ObjectMapper mapper = new ObjectMapper();

            // --- 🔥 SỬA ĐOẠN LÀM SẠCH JSON TẠI ĐÂY (Mạnh hơn) ---
            // Tìm vị trí dấu mở ngoặc vuông đầu tiên và dấu đóng ngoặc vuông cuối cùng
            int firstBracket = json.indexOf("[");
            int lastBracket = json.lastIndexOf("]");

            if (firstBracket != -1 && lastBracket != -1 && firstBracket < lastBracket) {
                // Cắt lấy đúng đoạn JSON Array, vứt bỏ mọi thứ rác ở đầu/cuối (như ```json, json, text...)
                json = json.substring(firstBracket, lastBracket + 1);
            } else {
                throw new RuntimeException("AI trả về định dạng không đúng (Không tìm thấy dấu [ ]).");
            }
            // --- 🟢 HẾT ĐOẠN SỬA ---

            List<GeneratedQuestionDTO> list = mapper.readValue(json, new TypeReference<List<GeneratedQuestionDTO>>(){});

            // Gán lại ID
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

    // 2. Hàm lưu hàng loạt (Batch Save) - Sau khi user bấm "Lưu"
    @Transactional
    public void saveGeneratedQuestions(List<GeneratedQuestionDTO> dtos) {
        for (GeneratedQuestionDTO dto : dtos) {
            // Tái sử dụng hàm import logic hoặc map thủ công
            Question q = new Question();
            q.setContent(dto.getContent());
            q.setExplanation(dto.getExplanation());
            q.setStatus("active");

            // Map Foreign Keys
            if(dto.getCourseId() != null) q.setCourse(courseRepository.findById(Long.valueOf(dto.getCourseId())).orElse(null));
            if(dto.getLessonId() != null) q.setLesson(lessonRepository.findById(Long.valueOf(dto.getLessonId())).orElse(null));
            if(dto.getLevelId() != null) q.setQuestionLevel(questionLevelRepository.findById(dto.getLevelId()).orElse(null));

            // Map Answers
            List<AnswerOption> options = new ArrayList<>();
            options.add(createAnswerOption(q, dto.getAnswerA(), "A".equalsIgnoreCase(dto.getCorrectChar()), 1));
            options.add(createAnswerOption(q, dto.getAnswerB(), "B".equalsIgnoreCase(dto.getCorrectChar()), 2));
            options.add(createAnswerOption(q, dto.getAnswerC(), "C".equalsIgnoreCase(dto.getCorrectChar()), 3));
            options.add(createAnswerOption(q, dto.getAnswerD(), "D".equalsIgnoreCase(dto.getCorrectChar()), 4));
            q.setAnswerOptions(options);

            Question savedQ = questionRepository.save(q);

            // Map Quiz (Many-to-Many)
            if (dto.getQuizId() != null) {
                Quiz quiz = quizRepository.findById(dto.getQuizId()).orElse(null);
                if (quiz != null) {
                    quiz.getQuestions().add(savedQ);
                    quizRepository.save(quiz);
                }
            }
        }
    }
}