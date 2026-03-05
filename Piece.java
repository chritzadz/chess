import java.util.ArrayList;

public interface Piece {
    ArrayList<String> getMoves();
    ArrayList<String> getCaptureMoves();
    Color getPieceColor();
    Position getPosition();
    String getImagePath();
}
