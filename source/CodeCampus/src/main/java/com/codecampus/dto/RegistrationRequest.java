
package com.codecampus.dto;
import lombok.Data;

@Data // Tự động tạo Getters, Setters, required-args constructor, toString, equals, hashCode
public class RegistrationRequest {
    private Integer courseId;
    private Integer packageId;
    private String fullName;
    private String email;
    private String mobile;
    private String gender;
}