import java.util.ArrayList;

public class Knight implements Piece {
    private Color color;
    private Position currentPosition;
    private String imagePath;
    private ArrayList<String> moves = new ArrayList<>();
    private ArrayList<String> captureMoves = new ArrayList<>();
    private Board board;

    public Knight(Color color, Position currentPosition, Board board){
        this.color = color;
        this.currentPosition = currentPosition;
        this.board = board;
        if (color == Color.WHITE){
            imagePath = "asset/N.png";
        } else if (color == Color.BLACK){
            imagePath = "asset/n_.png";
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

        // All 8 possible knight moves
        int[][] deltas = {
            {-2, -1}, {-2, 1},
            {-1, -2}, {-1, 2},
            {1, -2}, {1, 2},
            {2, -1}, {2, 1}
        };
        for (int[] d : deltas) {
            int newRow = row + d[0];
            int newCol = col + d[1];
            if (newRow >= 0 && newRow <= 7 && newCol >= 0 && newCol <= 7) {
                Piece p = board.getPiece(Position.getPosition(newRow, newCol));
                if (p == null) {
                    moves.add(Position.getPosition(newRow, newCol).name());
                } else {
                    if (p.getPieceColor() != this.color) {
                        moves.add(Position.getPosition(newRow, newCol).name());
                        captureMoves.add(Position.getPosition(newRow, newCol).name());
                    }
                }
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
