# java-basic-blockchain

Proof-of-concept block chain implementation in Java

This project is a java POC implementation of a [python basic block chain implementation](https://hackernoon.com/learn-blockchains-by-building-one-117428612f46)

# Usage

Build the block chain jar file using make.ps1 (on Windows) or make.sh (on Unix), this will create the jar file
[basic-blockchain.jar](basic-blockchain.jar)

Now run the following command:

```bash
java -jar basic-blockchain.jar
```

This will start the block chain node at http://localhost:3088

Now to run a second basic block chain node:

```bash
java -jar basic-blockchain.jar http://localhost:3088
```

This will start the second block chain node at http://localhost:3089 and uses the node at http://localhost:3088 as the
seed node to broadcast its ip address.






