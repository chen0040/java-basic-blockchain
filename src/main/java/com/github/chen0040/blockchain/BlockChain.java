package com.github.chen0040.blockchain;

import com.alibaba.fastjson.JSON;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Getter
@Setter
public class BlockChain {
    private List<Block> chain = new ArrayList<>();
    private List<Transaction> currentTransactions = new ArrayList<>();
    private Set<String> nodes = new HashSet<>();

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
       return hash(json);
    }

    private String hash(String text) {
        return Hashing.sha256()
                .hashString(text, StandardCharsets.UTF_8)
                .toString();
    }

    public boolean validateProof(long lastProof, long proof) {
        String text = lastProof + "" + proof;
        String hashed = hash(text);
        return hashed.startsWith("0000");
    }

    public long proofOfWork(long lastProof) {
        long proof = 0L;
        while(!validateProof(lastProof,proof)) {
            proof++;
        }
        return proof;
    }

    public void registerNode(String url) {
        nodes.add(url);
    }

    public boolean validateChain(List<Block> chain) {
        Block lastBlock = chain.get(0);
        int currentIndex = 1;
        while(currentIndex < chain.size()) {
            Block block = chain.get(currentIndex);
            if(!block.getPrevHash().equals(hash(lastBlock))) {
                return false;
            }
            lastBlock = block;
            currentIndex++;
        }

        return true;
    }

    public boolean resolveConflicts() {
        List<String> neighbors = new ArrayList<>(nodes);
        List<Block> newChain = null;

        // only looking for chains longer than that available at this node.
        int max_length = chain.size();

        for(String neighbor : neighbors) {
            List<Block> chain = queryChain(neighbor);
            if(chain == null) {
                continue;
            }
            if(chain.size() <= max_length) {
                continue;
            }
            if(!validateChain(chain)) {
                continue;
            }
            max_length = chain.size();
            newChain = chain;
        }

        if(newChain != null) {
            chain = newChain;
            return true;
        }
        return false;


    }

    private List<Block> queryChain(String url) {
        return null;
    }







}
