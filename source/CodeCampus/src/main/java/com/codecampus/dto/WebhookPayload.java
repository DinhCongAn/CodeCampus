// src/main/java/com/codecampus/dto/WebhookPayload.java
package com.codecampus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebhookPayload {

    @JsonProperty("description") // "description" hoặc "memo", "addInfo"...
    private String orderCode;

    @JsonProperty("amount")
    private Long amount;
}