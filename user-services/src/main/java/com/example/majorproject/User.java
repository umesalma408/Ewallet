package com.example.majorproject;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

///entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(unique = true)
    private String userName;
    private String name;
    private int age;
    private String email;
}
