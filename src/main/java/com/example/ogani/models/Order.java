package com.example.ogani.models;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String firstname;

    private String lastname;

    private String country;

    private String address;

    private String town;

    private String state;

    private long postCode;

    private String email;

    private String phone;

    private String note;

    private long totalPrice;

    @Column(name = "pay_datetime")
    private LocalDateTime payDateTime ;

    @Column(name = "date_order")
    private LocalDateTime dateOrder; 
    
    @Column(name = "order_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order")
    @JsonBackReference
    private Set<OrderDetail> orderdetails;

    @Column(name = "pay_method" , nullable = false)
    private String payMethod;
    public enum OrderStatus {
        PENDING,        
        CONFIRMED,      
        PAID,         
        SHIPPING,       
        COMPLETED,     
        CANCELLED     
    }
}
