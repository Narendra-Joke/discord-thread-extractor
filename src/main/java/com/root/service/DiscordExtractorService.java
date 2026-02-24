package com.root.service;

import com.root.dto.ExtractRequest;

public interface DiscordExtractorService {
    String extractThreadMessages(ExtractRequest request);

//    String extractFromChannel(String channelId, int limit

    long getExtractedCount(String threadId);
}
