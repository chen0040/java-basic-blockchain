package com.github.chen0040.blockchain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction {
    private String sender;
    private String recipient;
    private double amount;
}
