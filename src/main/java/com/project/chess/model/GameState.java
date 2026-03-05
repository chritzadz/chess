package com.project.chess.model;

import java.util.List;
import java.util.Map;

public class GameState {
    private List<PieceDTO> pieces;
    private boolean whiteToMove;
    private boolean inCheck;
    private boolean gameOver;
    private String winner; // "WHITE", "BLACK", "DRAW", or null
    private Map<String, List<String>> legalMoves; // position -> list of legal target squares
    private String playerColor; // "WHITE" or "BLACK" - the human player's side

    public GameState() {}

    public List<PieceDTO> getPieces() { return pieces; }
    public void setPieces(List<PieceDTO> pieces) { this.pieces = pieces; }
    
    public boolean isWhiteToMove() { return whiteToMove; }
    public void setWhiteToMove(boolean whiteToMove) { this.whiteToMove = whiteToMove; }
    
    public boolean isInCheck() { return inCheck; }
    public void setInCheck(boolean inCheck) { this.inCheck = inCheck; }
    
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    
    public Map<String, List<String>> getLegalMoves() { return legalMoves; }
    public void setLegalMoves(Map<String, List<String>> legalMoves) { this.legalMoves = legalMoves; }

    public String getPlayerColor() { return playerColor; }
    public void setPlayerColor(String playerColor) { this.playerColor = playerColor; }

    public static class PieceDTO {
        private String type; // "Pawn", "Rook", etc.
        private String color; // "WHITE" or "BLACK"
        private String position; // "e2"
        private String imagePath;

        public PieceDTO() {}

        public PieceDTO(String type, String color, String position, String imagePath) {
            this.type = type;
            this.color = color;
            this.position = position;
            this.imagePath = imagePath;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        
        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    }
}
