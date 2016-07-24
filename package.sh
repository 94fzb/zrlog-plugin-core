#!/usr/bin/env bash
mvn clean install
cd server
mvn clean compile assembly:single
