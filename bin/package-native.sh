java -version
export JDK_JAVA_OPTIONS='--add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED'
./mvnw clean install
./mvnw -Pnative -Dagent exec:exec@java-agent -U
./mvnw -Pnative package
mv target/plugin-core plugin-core-$(uname -s)-$(uname -m).bin