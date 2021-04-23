outFolder=${1}
pluginJarName="plugin-core-${2}.jar"
./mvnw -U clean package
mkdir -p ${1}
cp plugin-core/target/plugin-core.jar ${1}/$pluginJarName
cp plugin-core/target/plugin-core.jar ${1}/plugin-core.jar