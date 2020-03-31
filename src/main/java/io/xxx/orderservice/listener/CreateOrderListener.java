package io.xxx.orderservice.listener;

import io.xxx.orderservice.config.RocketMQProperties;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CreateOrderListener implements InitializingBean {

    private static final String TOPIC_ORDER_CREATED = "ORDER_CREATED";

    private final RocketMQProperties properties;

    public CreateOrderListener(RocketMQProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(properties.getConsumerGroup());
        consumer.setNamesrvAddr(properties.getNamesrvAddr());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(TOPIC_ORDER_CREATED, "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            // todo 保存到数据库
            System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
    }
}
