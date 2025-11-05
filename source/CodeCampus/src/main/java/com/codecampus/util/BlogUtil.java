package com.codecampus.util;

import org.springframework.stereotype.Component;

@Component
public class BlogUtil {

    private static final int BRIEF_INFO_LENGTH = 150; // Độ dài tóm tắt (150 ký tự)

    /**
     * Tạo "brief-info": Xóa thẻ HTML và cắt ngắn nội dung
     */
    public static String getBriefInfo(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }

        // 1. Xóa tất cả các thẻ HTML
        String textOnly = htmlContent.replaceAll("<[^>]*>", "");

        // 2. Cắt ngắn
        if (textOnly.length() <= BRIEF_INFO_LENGTH) {
            return textOnly;
        } else {
            return textOnly.substring(0, BRIEF_INFO_LENGTH) + "...";
        }
    }
}