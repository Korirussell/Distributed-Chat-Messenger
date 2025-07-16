#!/bin/bash

# Set JAVA_HOME for macOS
export JAVA_HOME=$(/usr/libexec/java_home)
echo "Using JAVA_HOME: $JAVA_HOME"

# Build the project first
echo "Building project..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Starting distributed chat servers..."

# Start Server A (WebSocket: 8080, Peer: 9080)
echo "Starting Server A..."
mvn exec:java -Dexec.mainClass="com.distributed.chat.ChatServer" -Dexec.args="8080 9080 ServerA localhost:9081 localhost:9082" &
SERVER_A_PID=$!

# Wait a bit for Server A to start
sleep 2

# Start Server B (WebSocket: 8081, Peer: 9081)
echo "Starting Server B..."
mvn exec:java -Dexec.mainClass="com.distributed.chat.ChatServer" -Dexec.args="8081 9081 ServerB localhost:9080 localhost:9082" &
SERVER_B_PID=$!

# Wait a bit for Server B to start
sleep 2

# Start Server C (WebSocket: 8082, Peer: 9082)
echo "Starting Server C..."
mvn exec:java -Dexec.mainClass="com.distributed.chat.ChatServer" -Dexec.args="8082 9082 ServerC localhost:9080 localhost:9081" &
SERVER_C_PID=$!

echo ""
echo "All servers started!"
echo "Server A: WebSocket on port 8080, Peer on port 9080"
echo "Server B: WebSocket on port 8081, Peer on port 9081" 
echo "Server C: WebSocket on port 8082, Peer on port 9082"
echo ""
echo "Open client.html in your browser and connect to:"
echo "  ws://localhost:8080 (Server A)"
echo "  ws://localhost:8081 (Server B)"
echo "  ws://localhost:8082 (Server C)"
echo ""
echo "Press Ctrl+C to stop all servers"

# Function to cleanup on exit
cleanup() {
    echo ""
    echo "Stopping all servers..."
    kill $SERVER_A_PID $SERVER_B_PID $SERVER_C_PID 2>/dev/null
    wait
    echo "All servers stopped."
}

# Set trap to cleanup on script exit
trap cleanup EXIT

# Wait for user to stop
wait