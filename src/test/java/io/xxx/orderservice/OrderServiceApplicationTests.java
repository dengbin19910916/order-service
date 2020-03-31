package io.xxx.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class OrderServiceApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        for (int i = 1; i <= 1_0; i++) {
            redisTemplate.opsForValue().set("product:" + i, "1000000");
        }
        for (int i = 1; i <= 1_0; i++) {
            redisTemplate.opsForValue().set("{wos}:product:" + i, "1000000");
        }
    }

}
