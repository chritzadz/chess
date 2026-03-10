package com.project.chess.handler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.project.chess.model.GameEngine;
import com.project.chess.model.Piece;
import com.project.chess.model.Position;

public class SocketConnectionHandler extends TextWebSocketHandler {
    // Map<gameId, Map<userId, WebSocketSession>>
    private static final Map<String, Map<String, WebSocketSession>> gameUserSessions = new HashMap<>();
    // Map<gameId, GameEngine>
    private static final Map<String, GameEngine> gameEngines = new HashMap<>();
    // Map<gameId, Boolean> (true: player1's turn, false: player2's turn)
    private static final Map<String, Boolean> gameTurns = new LinkedHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String gameId = getGameId(session);
        System.out.println(gameId);
        if (gameId == null) {
            session.close();
            return;
        }
        gameUserSessions.computeIfAbsent(gameId, k -> new LinkedHashMap<>());
        gameEngines.computeIfAbsent(gameId, k -> new GameEngine());
        gameTurns.putIfAbsent(gameId, true); // Player 1's turn by default
        System.out.println("Connection established for gameId: " + gameId + ", session: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String gameId = getGameId(session);
        if (gameId == null) return;
        String userIdToRemove = null;
        Map<String, WebSocketSession> userSessions = gameUserSessions.get(gameId);
        if (userSessions != null) {
            for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
                if (entry.getValue().getId().equals(session.getId())) {
                    userIdToRemove = entry.getKey();
                    break;
                }
            }
            if (userIdToRemove != null) {
                userSessions.remove(userIdToRemove);
                System.out.println("User disconnected: " + userIdToRemove + " from gameId: " + gameId);
            }
            // If all users disconnected, clean up game state
            if (userSessions.isEmpty()) {
                gameUserSessions.remove(gameId);
                gameEngines.remove(gameId);
                gameTurns.remove(gameId);
                System.out.println("Game state cleaned up for gameId: " + gameId);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String gameId = getGameId(session);
        if (gameId == null) {
            session.sendMessage(new TextMessage("ERROR:Game ID missing"));
            return;
        }
        Map<String, WebSocketSession> userSessions = gameUserSessions.get(gameId);
        if (userSessions == null) {
            session.sendMessage(new TextMessage("ERROR:Game not found"));
            return;
        }
        String msg = message.getPayload().trim();
        System.out.println("Raw message received for gameId " + gameId + ": " + msg);

        // Handle user registration
        if (msg.startsWith("USER:")) {
            handleUserRegistration(session, msg, gameId);
            return;
        }

        // Handle move request (REQUEST:e2)
        // if (msg.startsWith("REQUEST:")) {
        //     String square = msg.substring(8).trim();
        //     GameEngine gameEngine = gameEngines.get(gameId);
        //     Position pos = Position.fromAlgebraic(square);
        //     if (pos == null) {
        //         session.sendMessage(new TextMessage("ERROR:Invalid square"));
        //         return;
        //     }
        //     Piece piece = gameEngine.getPiece(pos);
        //     if (piece == null) {
        //         session.sendMessage(new TextMessage("ERROR:No piece at square"));
        //         return;
        //     }
        //     var moves = piece.getMoves();
        //     var captures = piece.getCaptureMoves();
        //     List<String> moveList = new ArrayList<>();
        //     for (Position p : moves) {
        //         moveList.add(Position.toAlgebraic(p));
        //     }
        //     for (Position p : captures) {
        //         moveList.add(Position.toAlgebraic(p));
        //     }
        //     session.sendMessage(new TextMessage("MOVES:" + String.join(",", moveList)));
        //     return;
        // }

        // Find userId for this session
        String userId = findUserIdBySession(session, userSessions);

        if (userId == null) {
            System.out.println("Message from unregistered session: " + session.getId());
            session.sendMessage(new TextMessage("ERROR:Please register first with USER:yourUserId"));
            return;
        }

        // Log who sent the message
        String sender = getSenderName(userId, gameId);
        System.out.println("Processing message from " + sender + ": " + msg);

        // Validate it's the correct player's turn
        if (!isCorrectPlayerTurn(userId, gameId)) {
            System.out.println("Wrong turn attempt by " + sender);
            session.sendMessage(new TextMessage("ERROR:Not your turn"));
            return;
        }

        // Process the move
        try {
            GameEngine gameEngine = gameEngines.get(gameId);
            System.out.println("Processing move for " + sender);
            String result = gameEngine.processMove(msg);
            if (result.equals("INVALID") || result.equals("ILLEGAL")){
                throw new RuntimeException(result);
            }

            String gameState = gameEngine.getGameStateString();
            System.out.println("Game state after move: " + gameState);
            String winner = gameEngine.getWinner();

            if (winner != null){
                broadcastGameState("END:"+winner, userSessions);
            }


            // Broadcast to all connected players in this game
            broadcastGameState(gameEngine.generateFEN(), userSessions);

            // Switch turns
            boolean isPlayer1Turn = gameTurns.getOrDefault(gameId, true);
            gameTurns.put(gameId, !isPlayer1Turn);
            System.out.println("Turn switched. Next turn: " + (!isPlayer1Turn ? "Player 1" : "Player 2"));

        } catch (Exception e) {
            System.out.println("Error processing move: " + e.getMessage());
            session.sendMessage(new TextMessage("ERROR:Invalid move - " + e.getMessage()));
        }
    }

    private void handleUserRegistration(WebSocketSession session, String msg, String gameId) throws Exception {
        Map<String, WebSocketSession> userSessions = gameUserSessions.get(gameId);
        if (userSessions == null) return;
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

        // Determine player1/player2 for this game
        int playerCount = userSessions.size();
        if (playerCount == 1) {
            session.sendMessage(new TextMessage("REGISTERED:White"));
            System.out.println("Registered Player 1: " + userId + " for gameId: " + gameId);
        } else if (playerCount == 2) {
            session.sendMessage(new TextMessage("REGISTERED:Black"));
            System.out.println("Registered Player 2: " + userId + " for gameId: " + gameId);
            // Game can start
            GameEngine gameEngine = gameEngines.get(gameId);
            String initialState = gameEngine.getGameStateString();
            broadcastGameState(gameEngine.generateFEN(), userSessions);
            System.out.println("Game started for gameId: " + gameId + ". Player 1's turn");
        } else {
            System.out.println("Game full, rejecting: " + userId);
            userSessions.remove(userId);
            session.sendMessage(new TextMessage("ERROR:Game is full"));
        }
    }

    private String findUserIdBySession(WebSocketSession session, Map<String, WebSocketSession> userSessions) {
        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String getSenderName(String userId, String gameId) {
        Map<String, WebSocketSession> userSessions = gameUserSessions.get(gameId);
        if (userSessions == null) return userId;
        ArrayList<String> users = new ArrayList<>(userSessions.keySet());
        if (users.size() > 0 && userId.equals(users.get(0))) {
            return "Player 1 (" + userId + ")";
        } else if (users.size() > 1 && userId.equals(users.get(1))) {
            return "Player 2 (" + userId + ")";
        }
        return userId;
    }

    private boolean isCorrectPlayerTurn(String userId, String gameId) {
        Map<String, WebSocketSession> userSessions = gameUserSessions.get(gameId);
        if (userSessions == null || userSessions.size() < 2) {
            System.out.println("Game not ready - both players must be connected");
            return false;
        }
        ArrayList<String> users = new ArrayList<>(userSessions.keySet());
        boolean isPlayer1Turn = gameTurns.getOrDefault(gameId, true);
        if (isPlayer1Turn && userId.equals(users.get(0))) {
            return true;
        }
        if (!isPlayer1Turn && users.size() > 1 && userId.equals(users.get(1))) {
            return true;
        }
        return false;
    }

    private void broadcastGameState(String gameState, Map<String, WebSocketSession> userSessions) {
        System.out.println("Broadcasting game state to " + userSessions.size() + " players");
        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            try {
                if (entry.getValue().isOpen()) {
                    entry.getValue().sendMessage(new TextMessage(gameState));
                    System.out.println("Sent game state to: " + entry.getKey());

                    // Send all pieces' moves instead for every sync position.
                    String gameId = entry.getKey();
                    GameEngine gameEngine = gameEngines.get(gameId);
                    if (gameEngine != null) {
                        List<Piece> pieces = gameEngine.getPieces();
                        List<Map<String, Object>> pieceMovesList = new ArrayList<>();
                        for (Piece piece : pieces) {
                            Map<String, Object> pieceInfo = new HashMap<>();
                            pieceInfo.put("piece", Position.toAlgebraic(piece.getPosition()));
                            List<String> moves = new ArrayList<>();
                            for (Position p : piece.getMoves()) {
                                moves.add(Position.toAlgebraic(p));
                            }
                            pieceInfo.put("moves", moves);
                            List<String> captures = new ArrayList<>();
                            for (Position p : piece.getCaptureMoves()) {
                                captures.add(Position.toAlgebraic(p));
                            }
                            pieceInfo.put("captures", captures);
                            pieceInfo.put("type", piece.getType());
                            pieceInfo.put("color", piece.getPieceColor().toString());
                            pieceMovesList.add(pieceInfo);
                        }
                        Map<String, Object> payload = new HashMap<>();
                        payload.put("pieceMoves", pieceMovesList);
                        String json;
                        try {
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            json = mapper.writeValueAsString(payload);
                        } catch (Exception e) {
                            json = "ERROR:Could not serialize moves";
                        }
                        entry.getValue().sendMessage(new TextMessage("MOVES:"+json));
                    }
                } else {
                    System.out.println("Session closed for: " + entry.getKey());
                }
            } catch (Exception e) {
                System.err.println("Error sending message to " + entry.getKey() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    // Helper to extract gameId from query string
    private String getGameId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2 && kv[0].equals("id")) {
                    return kv[1];
                }
            }
        }
        return null;
    }
}
