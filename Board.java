import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Board extends JPanel {
    private final Image boardImage;
    private ArrayList<Piece> pieces;
    private ArrayList<Position> highlightedMoves = new ArrayList<>();
    private ArrayList<Position> highlightedCaptureMoves = new ArrayList<>();
    private Position selectedSquare = null;
    private long zobristKey = 0L;
    private boolean whiteToMove = true;

    public Board(String imagePath) {
        this.boardImage = new ImageIcon(imagePath).getImage();
        this.pieces = new ArrayList<>();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectedSquare != null){
                    int squareWidth = getWidth() / 8;
                    int squareHeight = getHeight() / 8;
                    int col = (e.getX() - 3) / squareWidth;
                    int row = (e.getY() - 3) / squareHeight;
                    Position clickedPos = Position.getPosition(row, col);
                    
                    if (highlightedMoves.contains(clickedPos) || highlightedCaptureMoves.contains(clickedPos)) {
                        Piece pieceToMove = getPiece(selectedSquare);
                        if (pieceToMove != null) {
                            move(pieceToMove, clickedPos);
                        }
                        return; // exit after move
                    }
                }

                int squareWidth = getWidth() / 8;
                int squareHeight = getHeight() / 8;
                int col = (e.getX() - 3) / squareWidth;
                int row = (e.getY() - 3) / squareHeight;

                selectedSquare = Position.getPosition(row, col);
                highlightedMoves.clear();
                highlightedCaptureMoves.clear();
                for (Piece piece : pieces) {
                    Position pos = piece.getPosition();
                    int[] idx = Position.getOrdinal(pos);
                    int prow = idx[0];
                    int pcol = idx[1];
                    if (prow == row && pcol == col) {
                        Color currentTurn = whiteToMove ? Color.WHITE : Color.BLACK;
                        if (piece.getPieceColor() != currentTurn) {
                            break; //skip, not your turn
                        }
                        ArrayList<String> moves = piece.getMoves();
                        ArrayList<String> captureMoves = piece.getCaptureMoves();
                        for (String move : moves) {
                            try {
                                highlightedMoves.add(Position.valueOf(move));
                            } catch (IllegalArgumentException ex) {
                                // ignore invalid
                            }
                        }
                        for (String move : captureMoves) {
                            try {
                                highlightedCaptureMoves.add(Position.valueOf(move));
                            } catch (IllegalArgumentException ex) {
                                // ignore invalid
                            }
                        }
                        break;
                    }
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(boardImage, 0, 0, getWidth(), getHeight(), this);
        int squareWidth = getWidth() / 8;
        int squareHeight = getHeight() / 8;
        // Draw selected square highlight
        if (selectedSquare != null) {
            g.setColor(new java.awt.Color(255, 255, 0, 128));
            int[] idx = Position.getOrdinal(selectedSquare);
            int row = idx[0];
            int col = idx[1];
            int x = col * squareWidth + 3;
            int y = row * squareHeight + 3;
            g.fillRect(x, y, squareWidth, squareHeight);
        }
        // Draw normal moves in green
        g.setColor(new java.awt.Color(0, 255, 0, 128));
        for (Position pos : highlightedMoves) {
            if (highlightedCaptureMoves.contains(pos)) continue; // skip if it's a capture
            int[] idx = Position.getOrdinal(pos);
            int row = idx[0];
            int col = idx[1];
            int x = col * squareWidth + 3;
            int y = row * squareHeight + 3;
            g.fillOval(x + squareWidth/4, y + squareHeight/4, squareWidth/2, squareHeight/2);
        }
        // Draw capture moves in red
        g.setColor(new java.awt.Color(255, 0, 0, 128));
        for (Position pos : highlightedCaptureMoves) {
            int[] idx = Position.getOrdinal(pos);
            int row = idx[0];
            int col = idx[1];
            int x = col * squareWidth + 3;
            int y = row * squareHeight + 3;
            g.fillOval(x + squareWidth/4, y + squareHeight/4, squareWidth/2, squareHeight/2);
        }
        for (Piece piece : pieces) {
            drawPiece(g, piece);
        }
    }

    public void addPiece(Piece piece) {
        pieces.add(piece);
        zobristKey ^= Zobrist.getPieceSquareHash(piece, piece.getPosition());
        repaint();
    }

    private void drawPiece(Graphics g, Piece piece) {
        String imgPath = piece.getImagePath();
        Image pieceImage = new ImageIcon(imgPath).getImage();
        Position pos = piece.getPosition();
        int[] idx = Position.getOrdinal(pos);
        int row = idx[0];
        int col = idx[1];

        int squareWidth = getWidth() / 8;
        int squareHeight = getHeight() / 8;
        int x = col * squareWidth + 3;
        int y = row * squareHeight + 3;

        g.drawImage(pieceImage, x, y, squareWidth, squareHeight, this);
    }

    public Piece getPiece(Position pos){
        for (Piece p : pieces){
            if (p.getPosition().equals(pos)){
                return p;
            }
        }
        return null;
    }

    public void move(Piece piece, Position newPosition){
        Position oldPosition = piece.getPosition();
        
        //XOR old square
        zobristKey ^= Zobrist.getPieceSquareHash(piece, oldPosition);
        
        Piece captured = getPiece(newPosition);
        if (captured != null) {
            // XOR captured piece
            zobristKey ^= Zobrist.getPieceSquareHash(captured, newPosition);
            pieces.remove(captured);
        }
        
        // updated piece position
        piece.setPosition(newPosition);
        
        // XOR new piece in
        zobristKey ^= Zobrist.getPieceSquareHash(piece, newPosition);
        
        // toggling
        zobristKey ^= Zobrist.getSideToMoveHash();
        whiteToMove = !whiteToMove;
        
        selectedSquare = null;
        highlightedMoves.clear();
        highlightedCaptureMoves.clear();

        evaluate();

        repaint();
    }
    
    public long getZobristKey() {
        return zobristKey;
    }
    
    public void recalculateZobristKey() {
        zobristKey = Zobrist.computeHash(pieces, whiteToMove);
    }
    
    public boolean isWhiteToMove() {
        return whiteToMove;
    }
    
    static public boolean inBound(){
        return true;
    }
}