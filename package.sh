#!/usr/bin/env bash
cd plugin-common
mvn clean install
cd ../plugin-core
mvn clean compile assembly:single
