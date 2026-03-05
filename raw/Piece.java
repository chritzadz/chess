package raw;

import java.util.ArrayList;

public interface Piece {
    ArrayList<String> getMoves();
    ArrayList<String> getCaptureMoves();
    Color getPieceColor();
    Position getPosition();
    void setPosition(Position pos);
    String getImagePath();
    int getValue();
}
