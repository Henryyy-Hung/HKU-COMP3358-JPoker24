#!/bin/bash

# Define string variables
port_number="1099"

server_jar="JPoker24GameServer.jar"
client_jar="JPoker24Game.jar"

bin_dir="bin"
lib_dir="lib"
src_dir="src"
build_dir="build"

security_policy_path="policy/security.policy"
gf_client_path="$lib_dir/glassfish-6.1.0/glassfish6/glassfish/lib/gf-client.jar"
mysql_connector_path="$lib_dir/mysql-connector-j_8.4.0-1ubuntu22.04_all/usr/share/java/mysql-connector-j-8.4.0.jar"
server_jar_path="$build_dir/$server_jar"
client_jar_path="$build_dir/$client_jar"

# Check for user input
if [ "$#" -eq 0 ]; then
    echo "Usage: $0 compile|server|client|'compile server'|'compile client'"
    exit 1
fi

# Function to compile code
compile() {
    echo "> Removing existing built classes..."
    mkdir -p $bin_dir
    rm -rf $bin_dir/*
    echo ""

    echo "> Compiling the Java code..."
    javac -cp ":$gf_client_path" -d $bin_dir -sourcepath $src_dir $src_dir/com/**/*.java
    echo ""
}

# Check availability of the port and kill the process using it if necessary
check_and_release_port() {
    echo "> Checking availability of port $port_number..."
    echo "Enter password to check for occupied ports and release them if necessary."
    # Filter for TCP connections on the specified port
    local netstat_output=$(sudo netstat -tulpn | grep "tcp.*:$port_number ")
    if [ ! -z "$netstat_output" ]; then
        local pid=$(echo "$netstat_output" | awk '{print $7}' | cut -d'/' -f1)
        if [ ! -z "$pid" ]; then
            echo "Port $port_number is occupied by PID $pid. Attempting to release the port..."
            sudo kill -9 "$pid"
            if [ $? -eq 0 ]; then
                echo "Port $port_number released."
            else
                echo "Failed to release port $port_number."
            fi
        else
            echo "Failed to extract PID. Port $port_number might still be occupied."
        fi
    else
        echo "Port $port_number is available."
    fi
    echo ""
}

# Function to start the server
start_server() {
    # Check and potentially release the port
    check_and_release_port
    echo "> Starting the Server..."
    java -cp ":$bin_dir:$mysql_connector_path:$gf_client_path" -Djava.security.manager -Djava.security.policy=$security_policy_path com.server.ServerMain
    echo ""
}

# Function to start the client
start_client() {
    echo "> Starting the Client..."
    java -cp ":$bin_dir:$gf_client_path" -Djava.security.manager -Djava.security.policy=$security_policy_path com.client.ClientMain localhost
    echo ""
}

# Function to start the server in jar
start_server_jar() {
    # Check and potentially release the port
    check_and_release_port
    echo "> Starting the $server_jar..."
    java -cp "$server_jar_path:$mysql_connector_path:$gf_client_path" -Djava.security.manager -Djava.security.policy=$security_policy_path com.server.ServerMain
    echo ""
}

# Function to start the client in jar
start_client_jar() {
    echo "> Starting the $client_jar..."
    java -cp "$client_jar_path:$gf_client_path" -Djava.security.manager -Djava.security.policy=$security_policy_path com.client.ClientMain localhost
    echo ""
}

# Function to build JAR files
build() {
    # Compile all necessary files
    compile

    echo "> Building JAR files..."

    # Prepare directories for JAR packaging
    mkdir -p temp_jar/client/META-INF
    mkdir -p temp_jar/client/com
    mkdir -p temp_jar/server/META-INF
    mkdir -p temp_jar/server/com
    mkdir -p build

    # Copy compiled classes and resources specifically for the client
    cp -r bin/com/{client,common,enums,jms,ui,utils} temp_jar/client/com
    cp -r assets temp_jar/client
    {
        echo "Main-Class: com.client.ClientMain"
        echo "Class-Path: $mysql_connector_path $gf_client_path"
    } > temp_jar/client/META-INF/MANIFEST.MF

    # Copy source files specifically for the client
    cp -r src/com/{client,common,enums,jms,ui,utils} temp_jar/client/com

    # Package the client JAR
    (cd temp_jar/client && jar cvfm $client_jar META-INF/MANIFEST.MF .)

    # Copy compiled classes and resources specifically for the server
    cp -r bin/com/{common,enums,handler,jms,server,utils} temp_jar/server/com
    {
        echo "Main-Class: com.server.ServerMain"
        echo "Class-Path: $mysql_connector_path $gf_client_path"
    } > temp_jar/server/META-INF/MANIFEST.MF

    # Copy source files specifically for the server
    cp -r src/com/{common,enums,handler,jms,server,utils} temp_jar/server/com

    # Package the server JAR
    (cd temp_jar/server && jar cvfm $server_jar META-INF/MANIFEST.MF .)

    # Move the JAR files to the build directory
    mv temp_jar/client/$client_jar build
    mv temp_jar/server/$server_jar build

    # Clean up temporary directory
    rm -rf temp_jar

    echo ""
}

# Main command handling
command="$1"
if [ -n "$2" ]; then
    command="$command $2"
fi
if [ -n "$3" ]; then
    command="$command $3"
fi
case "$command" in
    "release port")
        check_and_release_port
        ;;
    "compile")
        compile
        ;;
    "java server")
        start_server
        ;;
    "java client")
        start_client
        ;;
    "compile server")
        compile
        start_server
        ;;
    "compile client")
        compile
        start_client
        ;;
    "build")
        build
        ;;
    "jar server")
        start_server_jar
        ;;
    "jar client")
        start_client_jar
        ;;
    "build server")
        build
        start_server_jar
        ;;
    "build client")
        build
        start_client_jar
        ;;
    *)
        echo "Invalid argument: $command"
        echo "Usage: $0 compile|server|client|'compile server'|'compile client'|'build server'|'build client'"
        exit 2
        ;;
esac
