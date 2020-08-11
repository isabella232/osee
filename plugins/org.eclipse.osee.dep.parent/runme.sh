#!/bin/bash
mvn clean verify -DdescriptorId=jar-with-dependencies -Declipse-ip-site=file:./../../../org.eclipse.ip/org.eclipse.ip.p2/target/repository

