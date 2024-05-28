java -version
./mvnw clean package
./mvnw -Pnative -Dagent exec:exec@java-agent -U
./mvnw -Pnative package
basePath=/tmp/download/plugin/core
mkdir -p ${basePath}
mv target/plugin-core ${basePath}/plugin-core-$(uname -s)-$(uname -m).bin