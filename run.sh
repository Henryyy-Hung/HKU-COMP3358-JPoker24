#!/bin/bash

# Define string variables
port_number="1099"
# gf_client_path="/home/u3035782750/glassfish-6.1.0/glassfish6/glassfish/lib/gf-client.jar"
# mysql_connector_path="/home/u3035782750/java/mysql-connector-j-8.4.0.jar"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
gf_client_path="$SCRIPT_DIR/lib/glassfish-6.1.0/glassfish6/glassfish/lib/gf-client.jar"
mysql_connector_path="$SCRIPT_DIR/lib/mysql-connector-j_8.4.0-1ubuntu22.04_all/usr/share/java/mysql-connector-j-8.4.0.jar"

bin_dir="bin"
src_dir="src"
security_policy="policy/security.policy"

# Check for user input
if [ "$#" -eq 0 ]; then
    echo "Usage: $0 compile|server|client|'compile server'|'compile client'"
    exit 1
fi

# Function to compile code
compile() {
    echo "> Removing existing built classes..."
    rm -rf $bin_dir/*
    echo ""

    echo "> Compiling the Java code..."
    javac -cp ":$gf_client_path" -d $bin_dir -sourcepath $src_dir $src_dir/**/*.java
    echo ""
}

# Check availability of the port and kill the process using it if necessary
check_and_release_port() {
    echo "> Checking availability of port $port_number..."
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
    java -cp ":$bin_dir:$mysql_connector_path:$gf_client_path" -Djava.security.manager -Djava.security.policy=$security_policy server.ServerMain
    echo ""
}

# Function to start the client
start_client() {
    echo "> Starting the Client..."
    java -cp ":$bin_dir:$gf_client_path" -Djava.security.manager -Djava.security.policy=$security_policy client.ClientMain localhost
    echo ""
}

# Main command handling
command="$1"
if [ -n "$2" ]; then
    command="$command $2"
fi
case "$command" in
    "compile")
        compile
        ;;
    "server")
        start_server
        ;;
    "client")
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
    *)
        echo "Invalid argument: $command"
        echo "Usage: $0 compile|server|client|'compile server'|'compile client'"
        exit 2
        ;;
esac
