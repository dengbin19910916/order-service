package io.xxx.orderservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.rocketmq")
public class RocketMQProperties {

    private String producerGroup;

    private String consumerGroup;

    private String namesrvAddr;
}
