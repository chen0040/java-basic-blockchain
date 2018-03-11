package com.github.chen0040.blockchain;

import com.alibaba.fastjson.JSON;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BlockChain {
    private List<Block> chain = new ArrayList<>();
    private List<Transaction> currentTransactions = new ArrayList<>();

    public Block newBlock(long proof) {
        return newBlock(proof, null);
    }


    public Block newBlock(long proof, String prev_hash){
        if(prev_hash == null) {
            prev_hash = hash(chain.get(chain.size()-1));
        }

        Block block = new Block();
        block.setIndex(chain.size()+1);
        block.setTimestamp(new Date().getTime());
        block.setTransactions(currentTransactions);
        block.setPrevHash(prev_hash);
        block.setProof(proof);

        currentTransactions = new ArrayList<>();

        chain.add(block);

        return block;
    }

    public long newTransaction(String sender, String recipient, double amount) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setSender(sender);
        transaction.setRecipient(recipient);

        return lastBlock().getIndex()+1; // return the index of the block that will hold this transaction
    }



    private Block lastBlock() {
        return chain.get(chain.size()-1);
    }


    private String hash(Block block) {
        String json = JSON.toJSONString(block);
        String sha256hex = Hashing.sha256()
                .hashString(json, StandardCharsets.UTF_8)
                .toString();
        return sha256hex;
    }
}
