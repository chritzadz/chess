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
            int left = col - 1;
            int right = col + 1;

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

            //CAPTURE MOVE
            if (left >= 0 && right <= 7 && forward < 7){
                Position leftCapture = Position.getPosition(forward, left);
                Position rightCapture = Position.getPosition(forward, right);

                if (board.getPiece(leftCapture) != null && board.getPiece(leftCapture).getPieceColor() != this.color) {
                    captureMoves.add(leftCapture.name());
                }
                if (board.getPiece(rightCapture) != null && board.getPiece(rightCapture).getPieceColor() != this.color) {
                    captureMoves.add(rightCapture.name());
                }
            }

            //PROMOTION
            if (forward == 7) {
                Position promotionPosition = Position.getPosition(forward, col);
                moves.add(promotionPosition.name()); //for now do nothing just add to normal move
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
    public void setPosition(Position pos) {
        this.currentPosition = pos;
    }

    @Override
    public String getImagePath() {
        return imagePath;
    }
}
