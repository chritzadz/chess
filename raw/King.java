package raw;

import java.util.ArrayList;

public class King implements Piece {
    private Color color;
    private Position currentPosition;
    private String imagePath;
    private ArrayList<String> moves = new ArrayList<>();
    private ArrayList<String> captureMoves = new ArrayList<>();
    private Board board;

    public King(Color color, Position currentPosition, Board board){
        this.color = color;
        this.currentPosition = currentPosition;
        this.board = board;
        if (color == Color.WHITE){
            imagePath = "asset/K.png";
        } else if (color == Color.BLACK){
            imagePath = "asset/k_.png";
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

        int[][] deltas = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
        };
        for (int[] d : deltas) {
            int newRow = row + d[0];
            int newCol = col + d[1];
            if (newRow >= 0 && newRow <= 7 && newCol >= 0 && newCol <= 7) {
                Position pos = Position.getPosition(newRow, newCol);
                Piece p = board.getPiece(pos);
                
                // since King cannot kill it self, prevent King to go to an "attacked" square
                Color enemyColor = (this.color == Color.WHITE) ? Color.BLACK : Color.WHITE;
                boolean isAttacked = board.isSquareAttackedBy(pos, enemyColor);
                
                //empty square
                if (p == null) {
                    if (!isAttacked) {
                        moves.add(pos.name());
                    }
                //enemy color
                } else if (p.getPieceColor() != this.color) {
                    // allow capture only if it is undefended
                    if (!isAttacked) {
                        captureMoves.add(pos.name());
                        moves.add(pos.name());
                    }
                }
            }
        }

        ArrayList<String> allMoves = new ArrayList<>(moves);
        allMoves.addAll(captureMoves);
        return allMoves;
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
        return 0;
    }
}
