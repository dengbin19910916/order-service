package io.xxx.orderservice.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
//@Document
public class Order {

    @Id
    private Long id;

    private Long buyerId;

    @NotEmpty
    private List<Item> items;

    private LocalDateTime created;

    private LocalDateTime modified;

//    @Document
    @Data
    public static class Item {

        @NotNull
        private Long pid;

        @NotNull
        private Integer num;
    }
}
