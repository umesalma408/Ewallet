package com.example.majorproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/user")
    public void addUser(@RequestBody UserRequest userRequest){

        userService.addUser(userRequest);
    }
    @GetMapping("/user")
    public User getUser(@RequestParam("userName") String  userName) throws Exception{
        return userService.getUser(userName);
    }
}
