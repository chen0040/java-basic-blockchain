package com.github.chen0040.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;


import static spark.Spark.get;
import static spark.Spark.port;

public class BlockChainServer {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainServer.class);

    public static void main(String[] args) {
        port(3088);

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

        Runtime.getRuntime().addShutdownHook(new Thread(Spark::stop));
    }
}
