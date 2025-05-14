package com.peolly.ordermicroservice.models;

import com.peolly.ordermicroservice.external.ItemDto;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@RedisHash(value = "OrderElement")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderElement implements Serializable {

    @Id
    private Long orderId;

    private ItemDto itemDto;

    private UUID userId;

    private LocalDateTime createdAt;

    private int quantity;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        OrderElement orderElement = (OrderElement) o;
        return getOrderId() != null && Objects.equals(getOrderId(), orderElement.getOrderId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + orderId + ", " +
                "productDto = " + itemDto + ", " +
                "userId = " + userId + ", " +
                "createdAt = " + createdAt + ")";
    }
}