java -version
./mvnw clean install
./mvnw -Pnative -Dagent exec:exec@java-agent -U
./mvnw -Pnative package
basePath=/tmp/download/plugin/core
mkdir ${basePath}
mv target/plugin-core ${basePath}/plugin-core-$(uname -s)-$(uname -m).bin