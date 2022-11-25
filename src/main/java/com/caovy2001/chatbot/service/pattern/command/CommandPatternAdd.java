package com.caovy2001.chatbot.service.pattern.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandPatternAdd {
    private String userId;
    private String content;
    private String intentId;
}
