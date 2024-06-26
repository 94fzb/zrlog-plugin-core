#!/usr/bin/env bash

java -version
sh bin/build-info.sh
./mvnw clean package
./mvnw -Pnative -Dagent exec:exec@java-agent -U
./mvnw -Pnative package
basePath=/tmp/download/plugin/core
mkdir -p ${basePath}
binName="plugin-core"
if [ -f "target/${binName}.exe" ];
then
  echo "window"
  mv "target/${binName}.exe" "${basePath}/${binName}-Windows-$(uname -m).exe"
  exit 0;
fi
if [ -f "target/${binName}" ];
then
  echo "unix"
  mv target/${binName} ${basePath}/${binName}-$(uname -s)-$(uname -m).bin
fi