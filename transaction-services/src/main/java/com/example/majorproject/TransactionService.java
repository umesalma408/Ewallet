package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
@Component
public class TransactionService {
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;
    @Autowired
    RestTemplate restTemplate;
    public void createTransaction(TransactionRequest transactionRequest){
        Transaction transaction=Transaction.builder()
                .fromUser(transactionRequest.getFromUser())
                .toUser(transactionRequest.getToUser())
                .amount(transactionRequest.getAmount())
                .status(TransactionStatus.PENDING)
                .transactionId(String.valueOf(UUID.randomUUID()))
                .transactionTime(new Date().toString())
                .build();
        transactionRepository.save(transaction);
        System.out.println("1 Transaction in TS"+transaction);
        //kafka
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("fromUser",transaction.getFromUser());
        jsonObject.put("toUser",transaction.getToUser());
        jsonObject.put("transactionId",transaction.getTransactionId());
        jsonObject.put("amount",transaction.getAmount());
        kafkaTemplate.send("create_transaction",jsonObject.toString());
    }
    //kafka consumer
    //listening to wallet service
    @KafkaListener(topics = {"update_transaction"},groupId = "updateTransaction")
    public void updateTransaction(String msg) throws JsonProcessingException {
        JSONObject jsonObject=objectMapper.readValue(msg, JSONObject.class);
        String status= (String) jsonObject.get("status");
        String transactionId= (String) jsonObject.get("transactionId");
        Transaction transaction=transactionRepository.findByTransactionId(transactionId);

        if(Objects.equals(status, "SUCCESS")){
            transaction.setStatus(TransactionStatus.SUCCESS);
        }else{
            transaction.setStatus(TransactionStatus.FAILED);
        }
        transactionRepository.save(transaction);
        System.out.println("TransactionStatus in TS "+status);
        System.out.println("2 Transaction in TS"+transaction);
        callNotificationService(transaction);
    }
    public void callNotificationService(Transaction transaction){
        String fromUser=transaction.getFromUser();
        String toUser=transaction.getToUser();
        //localhost:8076/user
        //sender
        URI url=URI.create("http://localhost:8076/user?userName="+fromUser);
        HttpEntity httpEntity=new HttpEntity<>(new HttpHeaders());
        JSONObject sender=restTemplate.exchange(url, HttpMethod.GET,httpEntity,JSONObject.class).getBody();
        String senderName= (String) sender.get("name");
        String senderEmail= (String) sender.get("email");

        //receiver
        url=URI.create("http://localhost:8076/user?userName="+toUser);
        JSONObject receiver=restTemplate.exchange(url, HttpMethod.GET,httpEntity,JSONObject.class).getBody();
        String receiverName= (String) receiver.get("name");
        String receiverEmail= (String) receiver.get("email");
        //sender Notification
        JSONObject senderEmailRequest=new JSONObject();
        senderEmailRequest.put("email",senderEmail);
        String sendMessage=String.format("Hi %s,\nYour transaction with transactionId %s to %s of amount %d is in %s status(Debited).",senderName,transaction.getTransactionId(),receiverName,transaction.getAmount(),transaction.getStatus());
        senderEmailRequest.put("message",sendMessage);
        kafkaTemplate.send("create_notification",senderEmailRequest.toString());
        //receiver
        if(Objects.equals(transaction.getStatus(),TransactionStatus.FAILED)){
            return;
        }
        JSONObject receiverEmailRequest=new JSONObject();
        receiverEmailRequest.put("email",receiverEmail);
        String receiverMessage=String.format("Hi %s,\nYou have received %d rupees from %s, kindly check your wallet and use this money for your usefull needs, don't waste it",receiverName,transaction.getAmount(),senderName);
        receiverEmailRequest.put("message",receiverMessage);
        kafkaTemplate.send("create_notification",receiverEmailRequest.toString());

    }
    public Transaction getTransaction(String id) {
        return transactionRepository.findByTransactionId(id);
    }
}


