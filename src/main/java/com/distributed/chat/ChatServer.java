package com.distributed.chat;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer extends WebSocketServer {
    private String serverId;
    private int peerPort;
    private List<String> peerAddresses;
    private Set<PeerConnectionHandler> peerConnections;
    private ExecutorService executorService;
    private ServerSocket peerServerSocket;
    
    public ChatServer(int websocketPort, int peerPort, String serverId, List<String> peerAddresses) {
        super(new InetSocketAddress(websocketPort));
        this.serverId = serverId;
        this.peerPort = peerPort;
        this.peerAddresses = new ArrayList<>(peerAddresses);
        this.peerConnections = ConcurrentHashMap.newKeySet();
        this.executorService = Executors.newCachedThreadPool();
        
        System.out.println("ChatServer " + serverId + " initialized:");
        System.out.println("  WebSocket port: " + websocketPort);
        System.out.println("  Peer port: " + peerPort);
        System.out.println("  Peer addresses: " + peerAddresses);
    }
    
    public void startServer() {
        // Start WebSocket server
        this.start();
        System.out.println("WebSocket server started on port " + this.getPort());
        
        // Start peer listener
        startPeerListener();
        
        // Connect to existing peers
        connectToPeers();
    }
    
    private void startPeerListener() {
        executorService.submit(() -> {
            try {
                peerServerSocket = new ServerSocket(peerPort);
                System.out.println("Peer listener started on port " + peerPort);
                
                while (!peerServerSocket.isClosed()) {
                    Socket peerSocket = peerServerSocket.accept();
                    PeerConnectionHandler handler = new PeerConnectionHandler(peerSocket, this);
                    peerConnections.add(handler);
                    executorService.submit(handler);
                }
            } catch (IOException e) {
                if (!peerServerSocket.isClosed()) {
                    System.err.println("Error in peer listener: " + e.getMessage());
                }
            }
        });
    }
    
    private void connectToPeers() {
        for (String peerAddress : peerAddresses) {
            executorService.submit(() -> connectToPeer(peerAddress));
        }
    }
    
    private void connectToPeer(String peerAddress) {
        try {
            String[] parts = peerAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            // Add a small delay to avoid connection race conditions
            Thread.sleep(1000);
            
            Socket peerSocket = new Socket(host, port);
            PeerConnectionHandler handler = new PeerConnectionHandler(peerSocket, this);
            peerConnections.add(handler);
            executorService.submit(handler);
            
            System.out.println("Connected to peer: " + peerAddress);
        } catch (Exception e) {
            System.err.println("Failed to connect to peer " + peerAddress + ": " + e.getMessage());
        }
    }  
  
    // WebSocket event handlers
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New client connected: " + conn.getRemoteSocketAddress());
        conn.send("Welcome to ChatServer " + serverId + "!");
    }
    
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
    }
    
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received from client: " + message);
        handleClientMessage(message, conn);
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }
    
    @Override
    public void onStart() {
        System.out.println("WebSocket server started successfully");
    }
    
    // Message handling
    public void handleClientMessage(String message, WebSocket sender) {
        try {
            // Create chat message with this server as origin
            String senderName = "User@" + sender.getRemoteSocketAddress().toString();
            ChatMessage chatMessage = new ChatMessage(senderName, message, serverId);
            
            // Broadcast to all local clients
            broadcastToClients(chatMessage.toJson());
            
            // Forward to all peer servers
            forwardToPeers(chatMessage.toJson(), null);
            
        } catch (Exception e) {
            System.err.println("Error handling client message: " + e.getMessage());
        }
    }
    
    public void handlePeerMessage(String messageJson, PeerConnectionHandler sender) {
        try {
            ChatMessage chatMessage = ChatMessage.fromJson(messageJson);
            
            // Prevent infinite loops - don't process messages that originated from this server
            if (chatMessage.getOriginServerId().equals(serverId)) {
                return;
            }
            
            System.out.println("Received from peer: " + chatMessage);
            
            // Broadcast to all local clients
            broadcastToClients(messageJson);
            
            // Forward to other peers (excluding the sender)
            forwardToPeers(messageJson, sender);
            
        } catch (Exception e) {
            System.err.println("Error handling peer message: " + e.getMessage());
        }
    }
    
    private void broadcastToClients(String message) {
        for (WebSocket client : getConnections()) {
            client.send(message);
        }
    }
    
    private void forwardToPeers(String message, PeerConnectionHandler excludePeer) {
        for (PeerConnectionHandler peer : peerConnections) {
            if (peer != excludePeer) {
                peer.sendMessage(message);
            }
        }
    }
    
    public void removePeerConnection(PeerConnectionHandler handler) {
        peerConnections.remove(handler);
    }
    
    public void shutdown() {
        try {
            // Close all peer connections
            for (PeerConnectionHandler peer : peerConnections) {
                peer.close();
            }
            
            // Close peer server socket
            if (peerServerSocket != null && !peerServerSocket.isClosed()) {
                peerServerSocket.close();
            }
            
            // Shutdown executor service
            executorService.shutdown();
            
            // Stop WebSocket server
            this.stop();
            
            System.out.println("ChatServer " + serverId + " shutdown complete");
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java ChatServer <websocket-port> <peer-port> <server-id> [peer-address1] [peer-address2] ...");
            System.out.println("Example: java ChatServer 8080 9080 ServerA localhost:9081 localhost:9082");
            return;
        }
        
        int websocketPort = Integer.parseInt(args[0]);
        int peerPort = Integer.parseInt(args[1]);
        String serverId = args[2];
        
        List<String> peerAddresses = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            peerAddresses.add(args[i]);
        }
        
        ChatServer server = new ChatServer(websocketPort, peerPort, serverId, peerAddresses);
        server.startServer();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        
        System.out.println("ChatServer " + serverId + " is running. Press Ctrl+C to stop.");
    }
}