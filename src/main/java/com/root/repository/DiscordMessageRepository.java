package com.root.repository;

import com.root.entity.DiscordMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DiscordMessageRepository extends JpaRepository<DiscordMessage, Long> {
    List<DiscordMessage> findByThreadId(String threadId);
    long countByThreadId(String threadId);
}
