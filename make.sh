#!/usr/bin/env bash

mvn -f pom.xml clean package -U

cp /target/java-basic-blockchain.jar basic-blockchain.jar
