java -version
sh bin/build-info.sh
./mvnw clean package
./mvnw -Pnative -Dagent exec:exec@java-agent -U
./mvnw -Pnative package
basePath=/tmp/download/plugin/core
mkdir -p ${basePath}
if [ -f 'target/plugin-core' ];
then
  mv target/plugin-core ${basePath}/plugin-core-$(uname -s)-$(uname -m).bin
fi
if [ -f 'target/plugin-core.exe' ];
then
  mv target/plugin-core.exe ${basePath}/plugin-core-Windows-$(uname -m).bin
fi