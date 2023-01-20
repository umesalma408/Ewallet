package com.example.majorproject;

import lombok.*;

import javax.persistence.Entity;

//Dto
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserRequest {
    private String userName;
    private String name;
    private int age;
    private String email;
}
