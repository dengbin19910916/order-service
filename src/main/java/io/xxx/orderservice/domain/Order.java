package io.xxx.orderservice.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("t_order")
public class Order {

    @Id
    @TableId
    private Long id;

    private Long buyerId;

    private LocalDateTime created;

    private LocalDateTime modified;

    @NotEmpty
    @TableField(exist = false)
    private List<OrderItem> items;
}
