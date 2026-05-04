package com.root.config;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class JdaConfig {

    @Value("${discord.bot.token}")
    private String token;

    @Bean(destroyMethod = "")
    public JDA jda() throws Exception {
        JDA jda = JDABuilder.createLight(token)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,           // Channel access
                        GatewayIntent.MESSAGE_CONTENT,         // Message content (privileged)
                        GatewayIntent.GUILD_MESSAGE_REACTIONS  // Reactions if needed
                        // NO GUILD_MESSAGE_THREADS - doesn't exist!
                )
                .setActivity(Activity.watching("Thread Extraction"))
                .build()
                .awaitReady();

        log.info("JDA Ready: {}", jda.getSelfUser().getName());
        return jda;
    }
}
