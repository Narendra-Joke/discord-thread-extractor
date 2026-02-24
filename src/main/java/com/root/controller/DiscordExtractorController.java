package com.root.controller;

import com.root.dto.ExtractRequest;
import com.root.service.DiscordExtractorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/extractor")
@RequiredArgsConstructor
public class DiscordExtractorController {

    @Autowired
    private DiscordExtractorService service;

    @PostMapping("/extract")
    public String extract(@RequestBody ExtractRequest request) {
        return service.extractThreadMessages(request);
    }

    @GetMapping("/count/{threadId}")
    public long getCount(@PathVariable String threadId) {
        return service.getExtractedCount(threadId);
    }
}
