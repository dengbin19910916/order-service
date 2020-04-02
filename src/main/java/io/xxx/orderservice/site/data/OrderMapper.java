package io.xxx.orderservice.site.data;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.xxx.orderservice.domain.Order;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OrderMapper extends BaseMapper<Order> {
}
