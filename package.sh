#!/usr/bin/env bash
cd common
mvn clean install
cd ../server
mvn clean compile assembly:single
