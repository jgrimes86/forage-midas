package com.jpmc.midascore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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
            System.out.println("Processing transaction from sender: " + sender.getName() + " to recipient: " + recipient.getName() + " with amount: " + transaction.getAmount());
            System.out.println("Sender balance before transaction: " + sender.getBalance());
            System.out.println("Recipient balance before transaction: " + recipient.getBalance());

            sender.setBalance(sender.getBalance() - transaction.getAmount());
            recipient.setBalance(recipient.getBalance() + transaction.getAmount());

            TransactionRecord record = new TransactionRecord();
            record.setSender(sender);
            record.setRecipient(recipient);
            record.setAmount(transaction.getAmount());
            databaseConduit.saveTransaction(record);

            databaseConduit.save(sender);
            databaseConduit.save(recipient);
            
            System.out.println("Sender balance after transaction: " + sender.getBalance());
            System.out.println("Recipient balance after transaction: " + recipient.getBalance());
        }

    }
    
}
