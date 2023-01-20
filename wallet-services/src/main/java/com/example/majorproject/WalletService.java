package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
    @Autowired
    WalletRepository walletRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;
    //kafka consumer
    //this method is called while creating user by user kafka producer
    @KafkaListener(topics = {"create_wallet"},groupId = "createWallet")
    public Wallet createWallet(String msg) throws JsonProcessingException {
        //converting String to jsonObj
        JSONObject jsonObject=objectMapper.readValue(msg, JSONObject.class);
        String userName= (String) jsonObject.get("userName");
        //creating wallet for the user
        Wallet wallet=Wallet.builder()
                .amount(5000)
                .userName(userName)
                .build();
        //saving in wallet DB
        walletRepository.save(wallet);
        return wallet;
    }
    //kafka consumer
    //this method is called while creating transaction by transaction kafka producer
    @KafkaListener(topics = {"create_transaction"},groupId = "createTransaction")
    public void createTransaction(String msg) throws JsonProcessingException {
        //String to jsonObj
        JSONObject jsonObject=objectMapper.readValue(msg, JSONObject.class);
        String fromUser= (String) jsonObject.get("fromUser");
        String toUser= (String) jsonObject.get("toUser");
        String transactionId= (String) jsonObject.get("transactionId");
        System.out.println("transactionId in WS"+transactionId);//null
        int amount= (int) jsonObject.get("amount");
        Wallet sender=walletRepository.findByUserName(fromUser);
        int balance=sender.getAmount();
        JSONObject transactionObj=new JSONObject();
        if(balance>=amount){
            //decrement
            sender.setAmount(balance-amount);
            walletRepository.save(sender);
            //increment
            Wallet receiver=walletRepository.findByUserName(toUser);
            receiver.setAmount(receiver.getAmount()+amount);
            walletRepository.save(receiver);
            transactionObj.put("status","SUCCESS");
            transactionObj.put("transactionId",transactionId);
        }else{
            transactionObj.put("status","FAILED");
            transactionObj.put("transactionId",transactionId);
        }
        //kafka producer returning acknowledgment to transaction service
        kafkaTemplate.send("update_transaction",transactionObj.toString());
    }

}
//    private final WalletRepository walletRepository;
//
//    public WalletService(WalletRepository walletRepository) {
//        this.walletRepository = walletRepository;
//    }
//
//    public void createWallet(String userName){
//        Wallet wallet = Wallet.builder().
//                userName(userName).
//                amount(0).
//                build();
//
//        walletRepository.save(wallet);
//    }
//
//    Wallet incrementWallet(String userName, int amount){
//
//        Wallet oldWallet = walletRepository.findByUserName(userName);
//        int newAmount = oldWallet.getAmount() + amount;
//
//        Wallet newWallet = Wallet.builder().userName(userName).amount(newAmount).id(oldWallet.getId()).build();
//
//        walletRepository.save(newWallet);
//        return newWallet;
//    }
/////Method 2: write the query
// //   int success = walletRepository.updateWallet(userName, amount);
//    Wallet decrementWallet(String userName, int amount){
//
//        Wallet oldWallet = walletRepository.findByUserName(userName);
//        int newAmount = oldWallet.getAmount() - amount;
//
//        Wallet newWallet = Wallet.builder().userName(userName).amount(newAmount).id(oldWallet.getId()).build();
//
//        walletRepository.save(newWallet);
//
//            return newWallet;
//    }
//    }
