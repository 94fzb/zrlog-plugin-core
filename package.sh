#!/usr/bin/env bash
./mvnw clean install -pl '!zrlog-plugin-core,!zrlog-plugin-freemarker-render'
./mvnw clean compile assembly:single -f 'zrlog-plugin-core/pom.xml'
