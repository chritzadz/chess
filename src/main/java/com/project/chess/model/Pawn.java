package com.project.chess.model;


import java.util.ArrayList;

public class Pawn implements Piece {
    private Color color;
    private Position currentPosition;
    private String imagePath;
    private ArrayList<Position> moves = new ArrayList<>();
    private ArrayList<Position> captureMoves = new ArrayList<>();
    private boolean hasMove = false;
    private GameEngine engine;

    public Pawn(Color color, Position currentPosition, GameEngine engine) {
        this.color = color;
        this.currentPosition = currentPosition;
        this.engine = engine;
        this.imagePath = (color == Color.WHITE) ? "/images/P.png" : "/images/p_.png";
    }

    @Override
    public ArrayList<Position> getMoves() {
        moves.clear();
        captureMoves.clear();

        int[] idx = Position.getOrdinal(currentPosition);
        int row = idx[0];
        int col = idx[1];

        if (color == Color.WHITE) {
            int forward = row - 1;
            int doubleForward = row - 2;

            if (forward >= 0) {
                Position forwardMove = Position.getPosition(forward, col);
                if (forwardMove != null && engine.getPiece(forwardMove) == null) {
                    moves.add(forwardMove);
                    if (!hasMove && doubleForward >= 0) {
                        Position doubleForwardMove = Position.getPosition(doubleForward, col);
                        if (doubleForwardMove != null && engine.getPiece(doubleForwardMove) == null) {
                            moves.add(doubleForwardMove);
                        }
                    }
                }
                // Captures
                for (int dc : new int[]{-1, 1}) {
                    int newCol = col + dc;
                    if (newCol >= 0 && newCol <= 7) {
                        Position capture = Position.getPosition(forward, newCol);
                        if (capture != null) {
                            Piece p = engine.getPiece(capture);
                            if (p != null && p.getPieceColor() != this.color) {
                                captureMoves.add(capture);
                            }
                        }
                    }
                }
            }
        } else {
            int forward = row + 1;
            int doubleForward = row + 2;

            if (forward <= 7) {
                Position forwardMove = Position.getPosition(forward, col);
                if (forwardMove != null && engine.getPiece(forwardMove) == null) {
                    moves.add(forwardMove);
                    if (!hasMove && doubleForward <= 7) {
                        Position doubleForwardMove = Position.getPosition(doubleForward, col);
                        if (doubleForwardMove != null && engine.getPiece(doubleForwardMove) == null) {
                            moves.add(doubleForwardMove);
                        }
                    }
                }
                // Captures
                for (int dc : new int[]{-1, 1}) {
                    int newCol = col + dc;
                    if (newCol >= 0 && newCol <= 7) {
                        Position capture = Position.getPosition(forward, newCol);
                        if (capture != null) {
                            Piece p = engine.getPiece(capture);
                            if (p != null && p.getPieceColor() != this.color) {
                                captureMoves.add(capture);
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    public void updateHasMove() { this.hasMove = true; }

    @Override public ArrayList<Position> getCaptureMoves() { return captureMoves; }
    @Override public Color getPieceColor() { return color; }
    @Override public Position getPosition() { return currentPosition; }
    @Override public void setPosition(Position pos) { this.currentPosition = pos; }
    @Override public String getImagePath() { return imagePath; }
    @Override public int getValue() { return 1; }
    @Override public String getType() { return "Pawn"; }
}
