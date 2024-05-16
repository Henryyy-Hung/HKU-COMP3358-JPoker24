# Compile all necessary files using the existing run.sh script
./run.sh compile

# Prepare directories for JAR packaging
mkdir -p temp_jar/client
mkdir -p temp_jar/client/META-INF
mkdir -p temp_jar/server
mkdir -p temp_jar/server/META-INF

# Copy compiled classes and resources specifically for the client
mkdir -p temp_jar/client/com
cp -r bin/com/{client,common,enums,jms,ui,utils} temp_jar/client/com
cp -r assets temp_jar/client
echo "Main-Class: com.client.ClientMain" > temp_jar/client/META-INF/MANIFEST.MF

# Copy source files specifically for the client
cp -r src/com/{client,common,enums,jms,ui,utils} temp_jar/client/com

# Package the client JAR
cd temp_jar/client
jar cvfm ../../JPoker24Game.jar META-INF/MANIFEST.MF . 
cd ../..

# Copy compiled classes and resources specifically for the server
mkdir -p temp_jar/server/com
cp -r bin/com/{common,enums,handler,jms,server,utils} temp_jar/server/com
echo "Main-Class: com.server.ServerMain" > temp_jar/server/META-INF/MANIFEST.MF

# Copy source files specifically for the server
cp -r src/com/{common,enums,handler,jms,server,utils} temp_jar/server/com

# Package the server JAR
cd temp_jar/server
jar cvfm ../../JPoker24GameServer.jar META-INF/MANIFEST.MF . 
cd ../..

# Clean up temporary directory
rm -rf temp_jar

# Output the created JAR filenames
echo "Client JAR: JPoker24Game.jar"
echo "Server JAR: JPoker24GameServer.jar"