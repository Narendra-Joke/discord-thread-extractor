package com.root.service;

import com.root.dto.ExtractRequest;
import com.root.entity.DiscordMessage;
import com.root.repository.DiscordMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;  // ✅ Exact import [web:25]
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordExtractorServiceImpl implements DiscordExtractorService {

    private final JDA jda;

    @Autowired
    private DiscordMessageRepository repository;

    @Value("${discord.guild.id}")
    private String defaultGuildId;

    @Override
    @Async
    @Transactional
    public String extractThreadMessages(ExtractRequest request) {
        // ✅ PRIORITY 1: Thread ID (direct extraction)
        if (request.getThreadId() != null && !request.getThreadId().isBlank()) {
            return extractFromThread(request.getThreadId(), request.getLimit());
        }

        // ✅ PRIORITY 2: Channel ID (extract ALL threads)
        if (request.getChannelId() != null && !request.getChannelId().isBlank()) {
            return extractFromChannel(request.getChannelId(), request.getLimit());
        }

        return "❌ Provide either threadId OR channelId";
    }

    private String extractFromThread(String threadId, int limit) {
        // Existing thread logic...
        Guild guild = jda.getGuildById(defaultGuildId);
        ThreadChannel thread = guild.getThreadChannelById(threadId);
        if (thread == null) return "❌ Thread not found: " + threadId;

        thread.getIterableHistory().limit(limit)
                .forEachAsync(this::saveMessage);
        return "🚀 Thread extraction started: " + threadId;
    }

//    private String extractFromChannel(String channelId, int limit) {
//        Guild guild = jda.getGuildById(defaultGuildId);
//        var channel = guild.getTextChannelById(channelId);  // Parent channel
//        if (channel == null) return "❌ Channel not found: " + channelId;
//
//        // ✅ FETCH ALL ACTIVE THREADS FROM CHANNEL
//        // ✅ CORRECT - No extra parameter needed
//        channel.getThreadChannels().forEach(thread -> {
//            log.info("📋 Extracting thread: {} ({})", thread.getName(), thread.getId());
//            thread.getIterableHistory().limit(limit)
//                    .forEachAsync(this::saveMessage);  // ✅ Only Message parameter
//        });
//
//
//        return "🚀 Channel extraction started: " + channelId + " (" +
//                channel.getThreadChannels().size() + " threads)";
//    }

    private String extractFromChannel(String channelId, int limit) {
        Guild guild = jda.getGuildById(defaultGuildId);
        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null)
            return "❌ Channel not found: " + channelId;

        log.info("🚀 Extracting ALL from channel: {} ({})", channel.getName(), channelId);

        // ✅ 1. Extract ALL MESSAGES FROM PARENT CHANNEL
        channel.getIterableHistory().limit(limit)
                .forEachAsync(this::saveMessage);

        // ✅ 2. Extract ALL MESSAGES FROM ALL THREADS
        channel.getThreadChannels().forEach(thread -> {
            log.info("📋 Thread: {} ({})", thread.getName(), thread.getId());
            thread.getIterableHistory().limit(limit)
                    .forEachAsync(this::saveMessage);
        });

        int totalThreads = channel.getThreadChannels().size();
        return String.format("🚀 Channel extraction COMPLETE: %s (%d threads, %d msg limit)",
                channel.getName(), totalThreads, limit);
    }

    private boolean saveMessage(Message message) {  // Return boolean to continue/stop pagination
        try {
            DiscordMessage entity = new DiscordMessage();
            entity.setMessageId(message.getId());

            // ✅ Use ACTUAL channel/thread ID from message
            entity.setThreadId(message.getChannel().getId());  // ThreadChannel ID
            entity.setChannelId(message.getChannel().getId()); // Same as thread ID
            entity.setGuildId(message.getGuild().getId());
            entity.setAuthorId(message.getAuthor().getId());
            entity.setAuthorName(message.getAuthor().getName());
            entity.setContent(message.getContentRaw());
            entity.setIsBot(message.getAuthor().isBot());
            entity.setTimestamp(message.getTimeCreated().toInstant());

            repository.saveAndFlush(entity);  // Flush for high volume
            return true;  // Continue pagination
        } catch (Exception e) {
            log.error("💾 Save failed for message {}: {}", message.getId(), e.getMessage());
            return true;  // Continue even on error
        }
    }

    @Override
    public long getExtractedCount(String threadId) {
        return repository.countByThreadId(threadId);
    }
}
