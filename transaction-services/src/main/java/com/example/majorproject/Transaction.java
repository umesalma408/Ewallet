package com.example.majorproject;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString
public class Transaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private String transactionId= UUID.randomUUID().toString();
    private String fromUser;
    private String toUser;
    private int amount;
    private TransactionStatus status;
    private String transactionTime;
}

