outFolder=${1}
pluginJarName="plugin-core-${2}.jar"
./mvnw -U clean package
mkdir -p ${1}
cp zrlog-plugin-core/target/plugin-core.jar ${1}/$pluginJarName
cp zrlog-plugin-core/target/plugin-core.jar ${1}/plugin-core.jar