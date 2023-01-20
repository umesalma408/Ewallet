package com.example.majorproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransactionController {
    @Autowired
    TransactionService transactionService;
    @PostMapping("/transaction")
    private void createTransaction(@RequestBody()TransactionRequest transactionRequest){
        transactionService.createTransaction(transactionRequest);
    }
    @GetMapping("/transaction/{transactionId}")
    private Transaction getTransaction(@PathVariable("transactionId")String id){
        return transactionService.getTransaction(id);
    }
}
