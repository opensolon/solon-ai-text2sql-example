package com.wht.ai.config;

import org.noear.solon.ai.chat.ChatConfig;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.embedding.EmbeddingConfig;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

/**
 * @author by HaiTao.Wang on 2025/5/28.
 */
@Configuration
public class AIModelConfig {

    @Bean
    public ChatModel build(@Inject("${solon.ai.chat.demo}") ChatConfig config) {
        return ChatModel.of(config).build();
    }

    @Bean
    public EmbeddingModel embeddingModel(@Inject("${solon.ai.embed.demo}") EmbeddingConfig config) {
        return EmbeddingModel.of(config).build();
    }


}
