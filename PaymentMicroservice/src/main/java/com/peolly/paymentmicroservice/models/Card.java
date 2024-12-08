package com.peolly.paymentmicroservice.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;
import java.util.UUID;

@Builder
@Entity
@Table(name = "card")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    @Column
    private UUID userId;

    @Column
    private String cardNumber;

    @Column
    private String monthExpiration;

    @Column
    private String yearExpiration;

    @Column
    private int cvv;

    @Column
    private Double availableMoney;

    @Column
    private String cardType;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Card card = (Card) o;
        return getCardId() != null && Objects.equals(getCardId(), card.getCardId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "cardId = " + cardId + ", " +
                "userId = " + userId + ", " +
                "cardNumber = " + cardNumber + ", " +
                "monthExpiration = " + monthExpiration + ", " +
                "yearExpiration = " + yearExpiration + ", " +
                "cvv = " + cvv + ", " +
                "availableMoney = " + availableMoney + ", " +
                "cardType = " + cardType + ")";
    }
}
