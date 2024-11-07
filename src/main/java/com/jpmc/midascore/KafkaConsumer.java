package com.jpmc.midascore;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;

@Service
public class KafkaConsumer {

    @Autowired
    private DatabaseConduit databaseConduit;

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "test-group")
    public void listen(Transaction transaction) {
        UserRecord sender = databaseConduit.findUserById(transaction.getSenderId());
        UserRecord recipient = databaseConduit.findUserById(transaction.getRecipientId());

        if (sender != null && recipient != null && sender.getBalance() >= transaction.getAmount()) {
            // System.out.println("Processing transaction from sender: " + sender.getName() + " to recipient: " + recipient.getName() + " with amount: " + transaction.getAmount());
            // System.out.println("Sender balance before transaction: " + sender.getBalance());
            // System.out.println("Recipient balance before transaction: " + recipient.getBalance());

            sender.setBalance(sender.getBalance() - transaction.getAmount());
            recipient.setBalance(recipient.getBalance() + transaction.getAmount());

            TransactionRecord record = new TransactionRecord();
            record.setSender(sender);
            record.setRecipient(recipient);
            record.setAmount(transaction.getAmount());
            databaseConduit.saveTransaction(record);

            databaseConduit.save(sender);
            databaseConduit.save(recipient);
            
            // System.out.println("Sender balance after transaction: " + sender.getBalance());
            // System.out.println("Recipient balance after transaction: " + recipient.getBalance());
            // System.out.println("TRANSACTION DETAILS FOR RECIPIENT: " + recipient.getName());
            // System.out.println("Sender Id: " + transaction.getSenderId());
            // System.out.println("Recipient Id: " + transaction.getRecipientId());
            // System.out.println("Transaction amount: " + transaction.getAmount());
            // System.out.println("Receipt balance: " + recipient.getBalance());

            // System.out.println("Sending transaction to Incentives API: " + transaction);

            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<Transaction> request = new HttpEntity<>(transaction);

            // System.out.println("HTTP Entity: " + request);

            ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:8080/incentive", request, Map.class);

            System.out.println("Raw response from Incentives API: " + response.getBody());

            if (response.getBody() != null && response.getBody().containsKey("amount")) {
                Float incentive = Float.valueOf(response.getBody().get("amount").toString());
                System.out.println("Incentive Amount: " + incentive);
                transaction.setIncentive(incentive);
                recipient.setBalance(recipient.getBalance() + incentive);
            } else {
                System.out.println("No incentive amount found.");
            }

            System.out.println("SENDER: " + sender.getName() + "; balance: " + sender.getBalance());
            System.out.println("RECIPIENT: " + recipient.getName() + "; balance: " + recipient.getBalance());


        }

    }
    
}
