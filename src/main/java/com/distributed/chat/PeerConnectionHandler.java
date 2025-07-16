package com.distributed.chat;

import java.io.*;
import java.net.Socket;

public class PeerConnectionHandler implements Runnable {
    private Socket peerSocket;
    private ChatServer chatServer;
    private BufferedReader reader;
    private PrintWriter writer;
    private String peerAddress;
    private volatile boolean running = true;
    
    public PeerConnectionHandler(Socket peerSocket, ChatServer chatServer) {
        this.peerSocket = peerSocket;
        this.chatServer = chatServer;
        this.peerAddress = peerSocket.getRemoteSocketAddress().toString();
        
        try {
            this.reader = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
            this.writer = new PrintWriter(peerSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up peer connection: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        System.out.println("Peer connection established with: " + peerAddress);
        
        try {
            String message;
            while (running && (message = reader.readLine()) != null) {
                chatServer.handlePeerMessage(message, this);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Peer connection lost: " + peerAddress + " - " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }
    
    public void sendMessage(String message) {
        if (writer != null && !peerSocket.isClosed()) {
            writer.println(message);
        }
    }
    
    public void close() {
        running = false;
        cleanup();
    }
    
    private void cleanup() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (peerSocket != null && !peerSocket.isClosed()) {
                peerSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing peer connection: " + e.getMessage());
        }
        
        chatServer.removePeerConnection(this);
        System.out.println("Peer connection closed: " + peerAddress);
    }
    
    public String getPeerAddress() {
        return peerAddress;
    }
}