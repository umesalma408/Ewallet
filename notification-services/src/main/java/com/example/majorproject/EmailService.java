package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    SimpleMailMessage simpleMailMessage;
    @KafkaListener(topics = "create_notification",groupId = "createNotification")
    public void createNotification(String msg) throws JsonProcessingException {
        JSONObject jsonObject=objectMapper.readValue(msg,JSONObject.class);
        String email= (String) jsonObject.get("email");
        String message= (String) jsonObject.get("message");
        System.out.println("Email from NS "+email);
        System.out.println("Message from NS "+message);
        simpleMailMessage.setFrom("avengersbackendbank@gmail.com");
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("Transaction Email");
        simpleMailMessage.setText(message);
        javaMailSender.send(simpleMailMessage);
    }
}
