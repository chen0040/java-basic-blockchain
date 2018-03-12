package com.github.chen0040.blockchain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.blockchain.utils.IpTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.port;

public class BlockChainServer {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainServer.class);
    private static BlockChain chain;

    public static void main(String[] args) {

        int chainPort = IpTools.getAvailablePort(3088);

        chain = new BlockChain(chainPort);
        port(chainPort);

        get("/kill", (req, res) -> {
            new Thread(()->{
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }).start();

            return "block-chain node will be killed in 100 milliseconds";
        });

        get("/ping", (req, res) -> "block-chain-node");

        get("/chain", (req, res) -> {
            res.header("Content-Type", "application/json");
            res.status(200);
           return JSON.toJSONString(chain.getChain(), SerializerFeature.BrowserCompatible);
        });

        post("/nodes/register", (req, res) -> {
            List<String> nodes = JSON.parseArray(req.body(), String.class);
            int total_nodes = chain.register(nodes);
            res.status(201);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "New nodes have been added");
            result.put("total_nodes", total_nodes);
            res.header("Content-Type", "application/json");
            return JSON.toJSONString(result, SerializerFeature.BrowserCompatible);
        });

        get("/nodes/resolve", (req, res) -> {
           boolean replaced = chain.resolveConflicts();

           Map<String, Object> result = new HashMap<>();

           if(replaced) {
               result.put("message", "Our chain was replaced");
               result.put("new_chain", chain.getChain());
           } else {
               result.put("message", "Our chain is authoritative");
               result.put("chain", chain.getChain());
           }
           res.status(200);
            res.header("Content-Type", "application/json");
           return JSON.toJSONString(result, SerializerFeature.BrowserCompatible);
        });

        get("/mine", (req, res) -> {
           res.status(200);
            res.header("Content-Type", "application/json");
           return JSON.toJSONString(chain.mine(), SerializerFeature.BrowserCompatible);
        });

        post("/transactions/new", (req, res) -> {
            try {
                Transaction newTransaction = JSON.parseObject(req.body(), Transaction.class);
                long index = chain.newTransaction(newTransaction.getSender(), newTransaction.getRecipient(), newTransaction.getAmount());
                Map<String, String> result = new HashMap<>();
                result.put("message", "Transaction will be added to block " + index);
                res.status(201);
                res.header("Content-Type", "application/json");
                return JSON.toJSONString(result, SerializerFeature.BrowserCompatible);
            } catch(Exception ex) {
                res.status(400);
                Map<String, String> result = new HashMap<>();
                result.put("message", ex.toString());
                res.header("Content-Type", "application/json");
                return JSON.toJSONString(result, SerializerFeature.BrowserCompatible);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(Spark::stop));
    }
}
