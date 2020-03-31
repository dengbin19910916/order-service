package io.xxx.orderservice.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
//@Document
public class MessageSendResult {

    @Id
    private Long id;
    private String brokerName;
    private String topic;
    private int queueId;
    private String regionId;
    private String msgId;
    private String transactionId;
    private String offsetMsgId;
    private long queueOffset;
    private String sendStatus;

}
