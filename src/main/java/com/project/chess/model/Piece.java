package com.project.chess.model;

import java.util.ArrayList;

public interface Piece {
    ArrayList<Position> getMoves();
    ArrayList<Position> getCaptureMoves();
    Color getPieceColor();
    Position getPosition();
    void setPosition(Position pos);
    String getImagePath();
    int getValue();
    String getType();
}
