package com.project.chess.model;

import java.util.ArrayList;

public class King implements Piece {
    private Color color;
    private Position currentPosition;
    private String imagePath;
    private ArrayList<Position> moves = new ArrayList<>();
    private ArrayList<Position> captureMoves = new ArrayList<>();
    private GameEngine engine;
    private boolean hasMove = false;

    public King(Color color, Position currentPosition, GameEngine engine) {
        this.color = color;
        this.currentPosition = currentPosition;
        this.engine = engine;
        this.imagePath = (color == Color.WHITE) ? "/images/K.png" : "/images/k_.png";
    }

    @Override
    public ArrayList<Position> getMoves() {
        moves.clear();
        captureMoves.clear();

        int[] idx = Position.getOrdinal(currentPosition);
        int row = idx[0];
        int col = idx[1];

        int[][] deltas = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        for (int[] d : deltas) {
            int newRow = row + d[0];
            int newCol = col + d[1];
            if (newRow >= 0 && newRow <= 7 && newCol >= 0 && newCol <= 7) {
                Position pos = Position.getPosition(newRow, newCol);
                Piece p = engine.getPiece(pos);
                
                // King can't move to attacked squares
                Color enemyColor = (this.color == Color.WHITE) ? Color.BLACK : Color.WHITE;
                if (engine.isSquareAttackedBy(pos, enemyColor)) {
                    continue;
                }
                
                if (p == null) {
                    moves.add(pos);
                } else if (p.getPieceColor() != this.color) {
                    captureMoves.add(pos);
                    moves.add(pos);
                }
            }
        }

        //castle
        if (!hasMove) {
            //King side castling
            int kingRow = (color == Color.WHITE) ? 7 : 0;
            int kingCol = 4;
            boolean canCastleKingside = true;

            if (this.hasMove){
                canCastleKingside = false;
            }

            for (int c = kingCol + 1; c <= kingCol + 2; c++) {
                Position pos = Position.getPosition(kingRow, c);
                if (engine.getPiece(pos) != null || engine.isSquareAttackedBy(pos, (color == Color.WHITE) ? Color.BLACK : Color.WHITE)) {
                    canCastleKingside = false;
                    break;
                }
            }

            Position rookKingsidePos = Position.getPosition(kingRow, 7);
            Piece rookKingside = engine.getPiece(rookKingsidePos);
            if (canCastleKingside && rookKingside != null && rookKingside.getType().equals("Rook") && !(((Rook) rookKingside).hasMoved())) {
                Position kingStart = Position.getPosition(kingRow, kingCol);
                if (!engine.isSquareAttackedBy(kingStart, (color == Color.WHITE) ? Color.BLACK : Color.WHITE)) {
                    moves.add(Position.getPosition(kingRow, kingCol + 2)); // Castling move
                }
            }

            // Queenside castling
            boolean canCastleQueenside = true;

            if (this.hasMove){
                canCastleQueenside = false;
            }

            for (int c = kingCol - 1; c >= kingCol - 3; c--) {
                Position pos = Position.getPosition(kingRow, c);
                if (engine.getPiece(pos) != null || engine.isSquareAttackedBy(pos, (color == Color.WHITE) ? Color.BLACK : Color.WHITE)) {
                    canCastleQueenside = false;
                    break;
                }
            }
            // Check rook presence and it hasn't moved
            Position rookQueensidePos = Position.getPosition(kingRow, 0);
            Piece rookQueenside = engine.getPiece(rookQueensidePos);
            if (canCastleQueenside && rookQueenside != null && rookQueenside.getType().equals("Rook") && !((Rook) rookQueenside).hasMoved()) {
                Position kingStart = Position.getPosition(kingRow, kingCol);
                if (!engine.isSquareAttackedBy(kingStart, (color == Color.WHITE) ? Color.BLACK : Color.WHITE)) {
                    moves.add(Position.getPosition(kingRow, kingCol - 2)); // Castling move
                }
            }
        }
        return moves;
    }

    @Override public ArrayList<Position> getCaptureMoves() { return captureMoves; }
    @Override public Color getPieceColor() { return color; }
    @Override public Position getPosition() { return currentPosition; }
    @Override public void setPosition(Position pos) { this.currentPosition = pos; }
    @Override public String getImagePath() { return imagePath; }
    @Override public int getValue() { return 0; }
    @Override public String getType() { return "King"; }
    public void updateHasMove() { hasMove = true; }
}
