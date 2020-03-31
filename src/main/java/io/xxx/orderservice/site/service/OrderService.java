package io.xxx.orderservice.site.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.xxx.orderservice.domain.Order;
import io.xxx.orderservice.domain.OrderMessage;
import io.xxx.orderservice.util.IdWorker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    private static final String TOPIC_ORDER_CREATED = "ORDER_CREATED";

    private final IdWorker idWorker = new IdWorker(0, 0);
    private StringRedisTemplate redisTemplate;
    private RedisScript<Boolean> createOrderScript;
    private ObjectMapper objectMapper;
    private DefaultMQProducer producer;

    @SneakyThrows
    public OrderMessage create(Order order) {
        initOrder(order);

        try {
            boolean result = deductAndSave(order);
            if (result) {
                OrderMessage orderMessage = new OrderMessage(order);
                String content = JSON.toJSONString(orderMessage);
                Message message = new Message(TOPIC_ORDER_CREATED,
                        "CREATED", order.getId().toString(),
                        content.getBytes(RemotingHelper.DEFAULT_CHARSET));
                SendResult sendResult = producer.send(message, (mqs, msg, arg) -> {
                    Order o = (Order) arg;
                    long index = o.getBuyerId() % mqs.size();
                    return mqs.get((int) index);
                }, order);
                if (sendResult == null) {
                    throw new RuntimeException(String.format("Create order [%s] failed.",
                            JSON.toJSONString(order)));
                }
                return orderMessage;
            }
        } catch (Exception e) {
            String errMsg = String.format("Create order failed, %s", JSON.toJSONString(order));
            log.error(errMsg, e);
            throw e;
        }
        throw new RuntimeException(String.format("Create order [%s] failed.",
                JSON.toJSONString(order)));
    }

    public void initOrder(Order order) {
        Long orderId = idWorker.nextId();
        order.setId(orderId);
        LocalDateTime now = LocalDateTime.now();
        order.setCreated(now);
        order.setModified(now);
    }

    /**
     * 扣减库存并且保存订单信息。
     *
     * @param order 订单信息
     * @return true - 成功，false - 失败
     */
    private Boolean deductAndSave(Order order) throws JsonProcessingException {
        String argv = objectMapper.writeValueAsString(order);
        try {
            // Redis Cluster需要key
            List<String> keys = new ArrayList<>();
            keys.add("orders:" + order.getId());
            for (Order.Item item : order.getItems()) {
                keys.add("product:" + item.getPid());
            }

            Boolean result = redisTemplate.execute(createOrderScript, keys, argv);
            if (result == null || !result) {
                return false;
            }
        } catch (Exception e) {
            String msg = String.format("Commodity %s failed to deduct inventory or save order.", argv);
            log.error(msg, e);
            throw e;
        }
        return true;
    }

    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setInventoryBookScript(RedisScript<Boolean> createOrderScript) {
        this.createOrderScript = createOrderScript;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }

}
