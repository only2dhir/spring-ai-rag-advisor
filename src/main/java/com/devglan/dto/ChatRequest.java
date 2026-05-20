package com.devglan.dto;

import lombok.Data;

@Data
public class ChatRequest {

    private String query;
    private String conversationId;
}
