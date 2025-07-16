# Distributed Chat Messenger

A distributed chat application built in Java that demonstrates key distributed systems concepts including inter-process communication, message broadcasting, and fault tolerance.

##  Features

- **Multi-Server Architecture**: Run multiple chat servers that communicate with each other
- **Real-time Messaging**: WebSocket-based client communication for instant message delivery
- **Global Message Broadcasting**: Messages sent to any server are delivered to all connected clients across all servers
- **Fault Tolerance**: System remains partially available even if individual servers fail
- **Loop Prevention**: Smart message routing prevents infinite forwarding between servers
- **Concurrent Connections**: Each server handles multiple clients and peer connections simultaneously

## ️ Architecture

```
+----------------+       +----------------+       +----------------+
| ChatServer (A) | <---> | ChatServer (B) | <---> | ChatServer (C) |
+--------+-------+       +--------+-------+       +--------+-------+
         ^                        ^                        ^
         |                        |                        |
         | (WebSocket)            | (WebSocket)            | (WebSocket)
         |                        |                        |
+--------+-------+       +--------+-------+       +--------+-------+
| ChatClient (1) |       | ChatClient (2) |       | ChatClient (3) |
+----------------+       +----------------+       +----------------+
```

- **Client-Server Communication**: WebSockets for real-time bidirectional communication
- **Server-to-Server Communication**: TCP sockets for peer-to-peer message forwarding
- **Message Flow**: Messages are broadcast locally and forwarded to peer servers

## ️ Technology Stack

- **Java 21** - Core application language
- **Maven** - Build and dependency management
- **Java-WebSocket** - WebSocket server implementation
- **JSON** - Message serialization
- **HTML/JavaScript** - Web-based client interface

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- Modern web browser with WebSocket support

## 🚀 Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/distributed-chat-messenger.git
   cd distributed-chat-messenger
   ```

2. **Start the servers**
   ```bash
   ./run-servers.sh
   ```

3. **Open the client**
   - Open `client.html` in your browser
   - Connect to any server:
     - `ws://localhost:8080` (Server A)
     - `ws://localhost:8081` (Server B)
     - `ws://localhost:8082` (Server C)

4. **Test distributed messaging**
   - Open multiple browser tabs
   - Connect each tab to a different server
   - Send messages from any tab - they appear in all tabs!

## 🔧 Manual Setup

If you prefer to run servers individually:

```bash
# Build the project
mvn clean compile

# Start Server A
mvn exec:java -Dexec.mainClass="com.distributed.chat.ChatServer" \
  -Dexec.args="8080 9080 ServerA localhost:9081 localhost:9082"

# Start Server B (in another terminal)
mvn exec:java -Dexec.mainClass="com.distributed.chat.ChatServer" \
  -Dexec.args="8081 9081 ServerB localhost:9080 localhost:9082"

# Start Server C (in another terminal)
mvn exec:java -Dexec.mainClass="com.distributed.chat.ChatServer" \
  -Dexec.args="8082 9082 ServerC localhost:9080 localhost:9081"
```

##  Project Structure

```
src/
├── main/java/com/distributed/chat/
│   ├── ChatServer.java           # Main server class handling WebSocket and peer connections
│   ├── ChatMessage.java          # Message data structure with JSON serialization
│   └── PeerConnectionHandler.java # Manages individual peer-to-peer connections
client.html                       # Web-based chat client
run-servers.sh                   # Script to start all servers
pom.xml                          # Maven configuration
```

##  Key Distributed Systems Concepts

### Inter-Process Communication (IPC)
- **Client-Server**: WebSockets for real-time bidirectional communication
- **Server-to-Server**: Raw TCP sockets for direct peer communication

### Message Broadcasting
- Messages are broadcast to all local clients and forwarded to peer servers
- Each message includes origin server ID to prevent infinite loops

### Fault Tolerance
- System remains partially available if individual servers fail
- Clients can reconnect to operational servers
- No single point of failure

### Concurrency
- Each server handles multiple simultaneous client and peer connections
- Thread-safe message handling and connection management

##  Testing the System

1. **Basic Functionality**: Send messages between clients connected to the same server
2. **Distributed Messaging**: Send messages between clients connected to different servers
3. **Fault Tolerance**: Stop one server and verify other servers continue working
4. **Concurrent Users**: Connect multiple clients to test concurrent message handling

##  Learning Outcomes

This project demonstrates understanding of:
- Distributed system architecture and design
- Network programming with sockets and WebSockets
- Concurrent programming and thread safety
- Message serialization and protocol design
- Fault tolerance and system reliability
- Real-time communication systems
