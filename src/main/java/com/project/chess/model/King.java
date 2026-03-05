package com.project.chess.model;

import java.util.ArrayList;

public class King implements Piece {
    private Color color;
    private Position currentPosition;
    private String imagePath;
    private ArrayList<Position> moves = new ArrayList<>();
    private ArrayList<Position> captureMoves = new ArrayList<>();
    private GameEngine engine;

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
        return moves;
    }

    @Override public ArrayList<Position> getCaptureMoves() { return captureMoves; }
    @Override public Color getPieceColor() { return color; }
    @Override public Position getPosition() { return currentPosition; }
    @Override public void setPosition(Position pos) { this.currentPosition = pos; }
    @Override public String getImagePath() { return imagePath; }
    @Override public int getValue() { return 0; }
    @Override public String getType() { return "King"; }
}
