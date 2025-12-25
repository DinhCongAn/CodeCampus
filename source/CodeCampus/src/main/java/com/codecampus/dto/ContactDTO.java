package com.codecampus.dto;

public class ContactDTO {
    private String fullName;
    private String email;
    private String subject;
    private String message;

    // Getter & Setter (Bắt buộc phải có)
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}