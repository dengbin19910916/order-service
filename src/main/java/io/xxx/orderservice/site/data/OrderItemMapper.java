package io.xxx.orderservice.site.data;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.xxx.orderservice.domain.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
