package com.jpmc.midascore.entity;

import jakarta.persistence.*;

@Entity
public class TransactionRecord {
    
    @Id
    @GeneratedValue()
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender")
    private UserRecord sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient")
    private UserRecord recipient;

    @Column(nullable = false)
    private float amount;

    @Column()
    private float incentive;

    public void setSender(UserRecord sender) {
        this.sender = sender;
    }

    public UserRecord getSender() {
        return sender;
    }

    public void setRecipient(UserRecord recipient) {
        this.recipient = recipient;
    }

    public UserRecord getRecipient() {
        return recipient;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getAmount() {
        return amount;
    }

    public void setIncentive(float incentive) {
        this.incentive = incentive;
    }

    public float getIncentive() {
        return incentive;
    }

}
