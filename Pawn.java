import java.util.ArrayList;

public class Pawn implements Piece {
    private Color color;
    private Position currentPosition;
    private String imagePath;
    private ArrayList<String> moves = new ArrayList<>();
    private ArrayList<String> captureMoves = new ArrayList<>();
    private boolean hasMove;
    private Board board;

    public Pawn(Color color, Position currentPosition, Board board){
        this.color = color;
        this.currentPosition = currentPosition;
        this.board = board;
        if (color == Color.WHITE){
            imagePath = "asset/P.png";
        } else if (color == Color.BLACK){
            imagePath = "asset/p_.png";
        } else {
            imagePath = "";
        }

        hasMove = false;
    }

    @Override
    public ArrayList<String> getMoves(){
        moves.clear();

        
        int[] idx = Position.getOrdinal(currentPosition);
        int row = idx[0];
        int col = idx[1];

        if (color == Color.WHITE) {
            int forward = row - 1;
            int doubleForward = row - 2;

            // FORWARD MOVE
            if (forward >= 0) {
                Position forwardMove = Position.getPosition(forward, col);
                moves.add(forwardMove.name());
            }

            //DOUBLE FORWARD MOVE
            if (!hasMove && doubleForward >= 0) {
                Position doubleForwardMove = Position.getPosition(doubleForward, col);
                moves.add(doubleForwardMove.name());
                hasMove = true;
            }
        } else if (color == Color.BLACK) {
            int forward = row + 1;
            int doubleForward = row + 2;

            // FORWARD MOVE
            if (forward <= 7) {
                Position forwardMove = Position.getPosition(forward, col);
                moves.add(forwardMove.name());
            }

            //DOUBLE FORWARD MOVE
            if (!hasMove && doubleForward <= 7) {
                Position doubleForwardMove = Position.getPosition(doubleForward, col);
                moves.add(doubleForwardMove.name());
                hasMove = true;
            }
        }
        return moves;
    }

    @Override
    public ArrayList<String> getCaptureMoves() {
        return captureMoves;
    }

    @Override
    public Color getPieceColor() {
        return color;
    }

    @Override
    public Position getPosition() {
        return currentPosition;
    }

    @Override
    public String getImagePath() {
        return imagePath;
    }
}
