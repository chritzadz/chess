package com.project.chess.model;

import java.util.ArrayList;

public class Queen implements Piece {
    private Color color;
    private Position currentPosition;
    private String imagePath;
    private ArrayList<Position> moves = new ArrayList<>();
    private ArrayList<Position> captureMoves = new ArrayList<>();
    private GameEngine engine;

    public Queen(Color color, Position currentPosition, GameEngine engine) {
        this.color = color;
        this.currentPosition = currentPosition;
        this.engine = engine;
        this.imagePath = (color == Color.WHITE) ? "/images/Q.png" : "/images/q_.png";
    }

    @Override
    public ArrayList<Position> getMoves() {
        moves.clear();
        captureMoves.clear();

        int[] idx = Position.getOrdinal(currentPosition);
        int row = idx[0];
        int col = idx[1];

        // Queen = Rook + Bishop directions
        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}};
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            while (r >= 0 && r <= 7 && c >= 0 && c <= 7) {
                Position pos = Position.getPosition(r, c);
                Piece p = engine.getPiece(pos);
                if (p == null) {
                    moves.add(pos);
                } else {
                    if (p.getPieceColor() != this.color) {
                        captureMoves.add(pos);
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
        ArrayList<Position> allMoves = new ArrayList<>(moves);
        allMoves.addAll(captureMoves);
        return allMoves;
    }

    @Override public ArrayList<Position> getCaptureMoves() { return captureMoves; }
    @Override public Color getPieceColor() { return color; }
    @Override public Position getPosition() { return currentPosition; }
    @Override public void setPosition(Position pos) { this.currentPosition = pos; }
    @Override public String getImagePath() { return imagePath; }
    @Override public int getValue() { return 9; }
    @Override public String getType() { return "Queen"; }
}
