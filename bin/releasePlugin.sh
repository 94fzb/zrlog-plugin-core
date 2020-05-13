outFolder=${1}
pluginJarName="plugin-core-${2}.jar"
./mvnw clean install -f "plugin-common/pom.xml"
./mvnw clean compile assembly:single -f "plugin-core/pom.xml"
mkdir -p ${1}
cp plugin-core/target/plugin-core.jar ${1}/$pluginJarName
cp plugin-core/target/plugin-core.jar ${1}/plugin-core.jar