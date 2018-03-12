package com.github.chen0040.blockchain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MineResult {
    private String message;
    private long index;
    private List<Transaction> transactions = new ArrayList<>();
    private long proof;
    private String prevHash;
}
