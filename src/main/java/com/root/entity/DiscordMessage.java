package com.root.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Table(name = "discord_messages", indexes = {@Index(columnList = "threadId"), @Index(columnList = "timestamp")})
@Data
public class DiscordMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageId;

    @Column(nullable = false)
    private String threadId;

    @Column(nullable = false)
    private String channelId;

    @Column(nullable = false)
    private String guildId;

    @Column(nullable = false)
    private String authorId;

    @Column(nullable = false)
    private String authorName;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Boolean isBot;

    @Column(nullable = false)
    private Instant timestamp;
}
