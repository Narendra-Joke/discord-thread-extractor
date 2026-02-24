package com.root.dto;

import lombok.Data;

@Data
public class ExtractRequest {
    private String threadId;  // Optional, uses config if null
    private String channelId;
    private int limit = 100;  // Max messages to extract
    private boolean async = true;
}
