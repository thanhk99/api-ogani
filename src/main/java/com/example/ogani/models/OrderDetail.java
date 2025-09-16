package com.example.ogani.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_detail")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private long price;

    private int quantity;

    private long subTotal;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
