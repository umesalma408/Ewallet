package com.example.majorproject;



    public class UserNotFoundException extends Exception{
        UserNotFoundException(){
            super("user not found");
        }
    }
