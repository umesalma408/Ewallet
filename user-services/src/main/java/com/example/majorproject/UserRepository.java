package com.example.majorproject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    User findByUserName(String userName);
    List<User> findByNameAndAge(String name,int age);
    boolean existsByUserName(String userName);
}
