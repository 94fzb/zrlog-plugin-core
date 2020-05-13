#!/usr/bin/env bash
./mvnw clean install -pl '!plugin-core,!plugin-freemarker-render'
./mvnw clean compile assembly:single -f 'plugin-core/pom.xml'
