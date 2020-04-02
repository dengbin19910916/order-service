package io.xxx.orderservice.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@TableName("t_order_item")
public class OrderItem {

    @Id
    @TableId
    private Long id;

    private Long orderId;

    @NotNull
    private Long pid;

    @NotNull
    private Integer num;

    private LocalDateTime created;

    private LocalDateTime modified;
}
