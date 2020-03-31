package io.xxx.orderservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.lang.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Configuration
public class RedisConfiguration implements InitializingBean, ApplicationContextAware {

    private final StringRedisTemplate redisTemplate;
    private ApplicationContext applicationContext;

    public RedisConfiguration(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public RedisScript<Boolean> createOrderScript() {
        Resource resource = new ClassPathResource("scripts/createOrder.lua");
        return RedisScript.of(resource, Boolean.class);
    }

    @Bean
    public RedisScript<String> limitLoadTimesScript() {
        Resource resource = new ClassPathResource("scripts/LimitLoadTimes.lua");
        return RedisScript.of(resource, String.class);
    }

    @Override
    public void afterPropertiesSet() {
        @SuppressWarnings("rawtypes")
        Map<String, RedisScript> redisScripts = applicationContext.getBeansOfType(RedisScript.class);

        Map<String, String> result = new HashMap<>();
        redisScripts.forEach((k, v) -> {
            byte[] script = v.getScriptAsString().getBytes(StandardCharsets.UTF_8);
            String sha = Objects.requireNonNull(redisTemplate.getConnectionFactory())
                    .getConnection()
                    .scriptLoad(script);
            result.put(k, sha);
        });
        log.info("Redis script {} is loaded.", result.toString());
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }
}
