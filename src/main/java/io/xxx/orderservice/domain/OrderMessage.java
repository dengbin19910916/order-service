package io.xxx.orderservice.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderMessage {

    private Long id;

    private Long buyerId;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modified;

    public OrderMessage(Order order) {
        this.id = order.getId();
        this.buyerId = order.getBuyerId();
        this.created = order.getCreated();
        this.modified = order.getModified();
    }
}
