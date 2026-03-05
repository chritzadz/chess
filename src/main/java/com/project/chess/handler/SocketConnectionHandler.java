package com.project.chess.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.HashMap;
import java.util.Map;
import com.project.chess.model.GameEngine;

public class SocketConnectionHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> userSessions = new HashMap<>();
    private String player1UserId = null;
    private String player2UserId = null;
    private GameEngine gameEngine = new GameEngine();
    private boolean isPlayer1Turn = true;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userIdToRemove = null;
        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                userIdToRemove = entry.getKey();
                break;
            }
        }
        if (userIdToRemove != null) {
            userSessions.remove(userIdToRemove);
            System.out.println("User disconnected: " + userIdToRemove);
            if (userIdToRemove.equals(player1UserId)) {
                player1UserId = null;
                System.out.println("Player 1 disconnected");
            }
            if (userIdToRemove.equals(player2UserId)) {
                player2UserId = null;
                System.out.println("Player 2 disconnected");
            }
        }
        // Reset game state if a player disconnects
        gameEngine = new GameEngine();
        isPlayer1Turn = true;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String msg = message.getPayload().trim();
        System.out.println("Raw message received: " + msg);
        
        // Handle user registration
        if (msg.startsWith("USER:")) {
            handleUserRegistration(session, msg);
            return;
        }
        
        // Find userId for this session
        String userId = findUserIdBySession(session);
        
        if (userId == null) {
            System.out.println("Message from unregistered session: " + session.getId());
            session.sendMessage(new TextMessage("ERROR:Please register first with USER:yourUserId"));
            return;
        }
        
        // Log who sent the message
        String sender = getSenderName(userId);
        System.out.println("Processing message from " + sender + ": " + msg);
        
        // Validate it's the correct player's turn
        if (!isCorrectPlayerTurn(userId)) {
            System.out.println("Wrong turn attempt by " + sender);
            session.sendMessage(new TextMessage("ERROR:Not your turn"));
            return;
        }
        
        // Process the move
        try {
            System.out.println("Processing move for " + sender);
            gameEngine.processMove(msg);
            
            String gameState = gameEngine.getGameStateString();
            System.out.println("Game state after move: " + gameState);
            
            // Broadcast to all connected players
            broadcastGameState(gameState);
            
            // Switch turns
            isPlayer1Turn = !isPlayer1Turn;
            System.out.println("Turn switched. Next turn: " + (isPlayer1Turn ? "Player 1" : "Player 2"));
            
        } catch (Exception e) {
            System.err.println("Error processing move: " + e.getMessage());
            e.printStackTrace();
            session.sendMessage(new TextMessage("ERROR:Invalid move - " + e.getMessage()));
        }
    }
    
    private void handleUserRegistration(WebSocketSession session, String msg) throws Exception {
        String userId = msg.substring(5).trim();
        
        if (userId.isEmpty()) {
            session.sendMessage(new TextMessage("ERROR:User ID cannot be empty"));
            return;
        }
        
        // Check if user is already registered
        if (userSessions.containsKey(userId)) {
            System.out.println("Duplicate registration attempt: " + userId);
            session.sendMessage(new TextMessage("ERROR:User ID already registered"));
            return;
        }
        
        userSessions.put(userId, session);
        
        if (player1UserId == null) {
            player1UserId = userId;
            System.out.println("Registered Player 1: " + userId);
            session.sendMessage(new TextMessage("REGISTERED:Player1"));
        } else if (player2UserId == null) {
            player2UserId = userId;
            System.out.println("Registered Player 2: " + userId);
            session.sendMessage(new TextMessage("REGISTERED:Player2"));
            
            // Game can start
            String initialState = gameEngine.getGameStateString();
            System.out.println("Initial game state: " + initialState);
            broadcastGameState(initialState);
            System.out.println("Game started! Player 1's turn");
        } else {
            System.out.println("Game full, rejecting: " + userId);
            userSessions.remove(userId);
            session.sendMessage(new TextMessage("ERROR:Game is full"));
        }
    }
    
    private String findUserIdBySession(WebSocketSession session) {
        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private String getSenderName(String userId) {
        if (userId.equals(player1UserId)) {
            return "Player 1 (" + userId + ")";
        } else if (userId.equals(player2UserId)) {
            return "Player 2 (" + userId + ")";
        }
        return userId;
    }
    
    private boolean isCorrectPlayerTurn(String userId) {
        if (player1UserId == null || player2UserId == null) {
            System.out.println("Game not ready - both players must be connected");
            return false;
        }
        
        if (isPlayer1Turn && userId.equals(player1UserId)) {
            return true;
        }
        
        if (!isPlayer1Turn && userId.equals(player2UserId)) {
            return true;
        }
        
        return false;
    }
    
    private void broadcastGameState(String gameState) {
        System.out.println("Broadcasting game state to " + userSessions.size() + " players");
        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            try {
                if (entry.getValue().isOpen()) {
                    entry.getValue().sendMessage(new TextMessage(gameState));
                    System.out.println("Sent game state to: " + entry.getKey());
                } else {
                    System.out.println("Session closed for: " + entry.getKey());
                }
            } catch (Exception e) {
                System.err.println("Error sending message to " + entry.getKey() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}