import java.util.ArrayList;

public class Bishop implements Piece {
    private Color color;
    private Position currentPosition;
    private String imagePath;
    private ArrayList<String> moves = new ArrayList<>();
    private ArrayList<String> captureMoves = new ArrayList<>();
    private Board board;

    public Bishop(Color color, Position currentPosition, Board board){
        this.color = color;
        this.currentPosition = currentPosition;
        this.board = board;
        if (color == Color.WHITE){
            imagePath = "asset/B.png";
        } else if (color == Color.BLACK){
            imagePath = "asset/b_.png";
        } else {
            imagePath = "";
        }
    }

    @Override
    public ArrayList<String> getMoves(){
        moves.clear();
        captureMoves.clear();

        int[] idx = Position.getOrdinal(currentPosition);
        int row = idx[0];
        int col = idx[1];

        //DIAGONAL MOVE
        for (int r = row - 1, c = col - 1; r >= 0 && c >= 0; r--, c--) {
            //PIECE BLOCKING
            Piece p = board.getPiece(Position.getPosition(r, c));
            if (p != null){
                if (p.getPieceColor() != this.color){
                    captureMoves.add(Position.getPosition(r, c).name());
                }
                break;
            } else {
                moves.add(Position.getPosition(r, c).name());
            }
        }

        for (int r = row - 1, c = col + 1; r >= 0 && c <= 7; r--, c++) {
            //PIECE BLOCKING
            Piece p = board.getPiece(Position.getPosition(r, c));
            if (p != null){
                if (p.getPieceColor() != this.color){
                    captureMoves.add(Position.getPosition(r, c).name());
                }
                break;
            } else {
                moves.add(Position.getPosition(r, c).name());
            }
        }

        for (int r = row + 1, c = col - 1; r <= 7 && c >= 0; r++, c--) {
            //PIECE BLOCKING
            Piece p = board.getPiece(Position.getPosition(r, c));
            if (p != null){
                if (p.getPieceColor() != this.color){
                    captureMoves.add(Position.getPosition(r, c).name());
                }
                break;
            } else {
                moves.add(Position.getPosition(r, c).name());
            }
        }

        for (int r = row + 1, c = col + 1; r <= 7 && c <= 7; r++, c++) {
            //PIECE BLOCKING
            Piece p = board.getPiece(Position.getPosition(r, c));
            if (p != null){
                if (p.getPieceColor() != this.color){
                    captureMoves.add(Position.getPosition(r, c).name());
                }
                break;
            } else {
                moves.add(Position.getPosition(r, c).name());
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

    @Override
    public int getValue() {
        return 3;
    }
}
