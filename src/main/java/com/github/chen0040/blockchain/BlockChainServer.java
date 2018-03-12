package com.github.chen0040.blockchain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.blockchain.utils.HttpClient;
import com.github.chen0040.blockchain.utils.IpTools;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.port;

public class BlockChainServer {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainServer.class);
    private static BlockChain chain;

    private static ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    public static void main(String[] args) {

        for(String arg: args) {
            logger.info("arg: {}", arg);
        }

        String seed = "http://localhost:3088";
        if(args.length >= 1) {
            seed = args[0];
        }

        logger.info("block chain seed node: {}", seed);

        int chainPort = IpTools.getAvailablePort(3088);

        chain = new BlockChain(chainPort);
        port(chainPort);

        logger.info("Starting block chain node at {}", chain.getId());

        final String seedIp = seed;
        executor.submit(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("broad this node {} ...", chain.getId());
            chain.broadCast(seedIp);

        });


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
            logger.info("nodes registration invoked at {}", chain.getId());
            List<String> nodes = JSON.parseArray(req.body(), String.class);
            int total_nodes = chain.register(nodes);
            res.status(201);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "New nodes have been added");
            result.put("total_nodes", total_nodes);
            res.header("Content-Type", "application/json");
            return JSON.toJSONString(result, SerializerFeature.BrowserCompatible);
        });

        post("/nodes/broadcast_ip", (req, res) -> {
            logger.info("broadcast api invoked at {}", chain.getId());
           List<String> nodes = JSON.parseArray(req.body(), String.class);
           int total_nodes = chain.register(nodes);
           executor.submit(() -> {
               for (String node : chain.getNodes()) {
                   HttpClient.postArray(node + "/nodes/register", nodes);
               }
           });
            Map<String, Object> result = new HashMap<>();
            result.put("message", "New nodes have been added and broadcasted");
            result.put("total_nodes", total_nodes);

            res.status(201);
            res.header("Content-Type", "application/json");
            return JSON.toJSONString(result, SerializerFeature.BrowserCompatible);
        });

        get("/nodes", (req, res) -> {
            res.header("Content-Type", "application/json");
            return JSON.toJSONString(chain.getNodes());
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
