package com.github.chen0040.blockchain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Block {
    private long index = 0L;
    private long timestamp = 0L;
    private List<Transaction> transactions = new ArrayList<>();
    private long proof = 0L;
    private String prevHash = "";
}
