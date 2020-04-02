package io.xxx.orderservice.site.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.xxx.orderservice.config.RocketMQProperties;
import io.xxx.orderservice.domain.Order;
import io.xxx.orderservice.domain.OrderItem;
import io.xxx.orderservice.domain.OrderMessage;
import io.xxx.orderservice.site.data.OrderItemMapper;
import io.xxx.orderservice.site.data.OrderMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderService implements InitializingBean {

    private static final String TOPIC_ORDER_CREATED = "OMNI_ORDER";

    private StringRedisTemplate redisTemplate;
    private RedisScript<Boolean> createOrderScript;
    private ObjectMapper objectMapper;
    private DefaultMQProducer producer;
    private final RocketMQProperties properties;
    private final OrderMapper orderMapper;
    private final OrderItemMapper itemMapper;

    public OrderService(StringRedisTemplate redisTemplate,
                        RedisScript<Boolean> createOrderScript,
                        ObjectMapper objectMapper,
                        DefaultMQProducer producer,
                        RocketMQProperties properties,
                        OrderMapper orderMapper,
                        OrderItemMapper itemMapper) {
        this.redisTemplate = redisTemplate;
        this.createOrderScript = createOrderScript;
        this.objectMapper = objectMapper;
        this.producer = producer;
        this.properties = properties;
        this.orderMapper = orderMapper;
        this.itemMapper = itemMapper;
    }

    @SneakyThrows
    public OrderMessage create(Order order) {
        initOrder(order);

        try {
            boolean result = deductAndSave(order);
            if (result) {
                OrderMessage orderMessage = new OrderMessage(order);
                String content = JSON.toJSONString(order);
                Message message = new Message(TOPIC_ORDER_CREATED, null,
                        order.getId().toString(), content.getBytes(StandardCharsets.UTF_8));
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
        Long orderId = IdWorker.getId();
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
            for (OrderItem item : order.getItems()) {
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

    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(properties.getConsumerGroup());
        consumer.setNamesrvAddr(properties.getNamesrvAddr());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(TOPIC_ORDER_CREATED, "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                String body = new String(msg.getBody(), StandardCharsets.UTF_8);
                Order order = JSON.parseObject(body, Order.class);
                save(order);

                if (log.isDebugEnabled()) {
                    log.debug("Order saved, {}.", body);
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
    }

    @Transactional
    public void save(Order order) {
        orderMapper.insert(order);
        for (OrderItem item : order.getItems()) {
            item.setId(IdWorker.getId());
            item.setCreated(order.getCreated());
            item.setModified(order.getModified());
            item.setOrderId(order.getId());
            itemMapper.insert(item);
        }
    }
}
