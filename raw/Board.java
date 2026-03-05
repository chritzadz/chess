package raw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Board extends JPanel {
    private static final int BORDER_OFFSET = 0; // Adjust this to match your board image border
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
                    int col = (e.getX() - BORDER_OFFSET) / squareWidth;
                    int row = (e.getY() - BORDER_OFFSET) / squareHeight;
                    Position clickedPos = Position.getPosition(row, col);
                    
                    if (highlightedMoves.contains(clickedPos) || highlightedCaptureMoves.contains(clickedPos)) {
                        Piece pieceToMove = getPiece(selectedSquare);
                        if (pieceToMove != null) {
                            move(pieceToMove, clickedPos);
                        }
                        return; // exit after move
                    }

                    selectedSquare = null;
                    highlightedMoves.clear();
                    highlightedCaptureMoves.clear();
                }

                int squareWidth = getWidth() / 8;
                int squareHeight = getHeight() / 8;
                int col = (e.getX() - BORDER_OFFSET) / squareWidth;
                int row = (e.getY() - BORDER_OFFSET) / squareHeight;

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
                        
                        // Only add moves that are legal (don't leave king in check)
                        for (String move : moves) {
                            try {
                                Position targetPos = Position.valueOf(move);
                                if (isLegalMove(piece, targetPos)) {
                                    highlightedMoves.add(targetPos);
                                }
                            } catch (IllegalArgumentException ex) {
                                // ignore invalid
                            }
                        }
                        for (String move : captureMoves) {
                            try {
                                Position targetPos = Position.valueOf(move);
                                if (isLegalMove(piece, targetPos)) {
                                    highlightedCaptureMoves.add(targetPos);
                                }
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
            int x = col * squareWidth + BORDER_OFFSET;
            int y = row * squareHeight + BORDER_OFFSET;
            g.fillRect(x, y, squareWidth, squareHeight);
        }
        // Draw normal moves in green
        g.setColor(new java.awt.Color(0, 255, 0, 128));
        for (Position pos : highlightedMoves) {
            if (highlightedCaptureMoves.contains(pos)) continue; // skip if it's a capture
            int[] idx = Position.getOrdinal(pos);
            int row = idx[0];
            int col = idx[1];
            int x = col * squareWidth + BORDER_OFFSET;
            int y = row * squareHeight + BORDER_OFFSET;
            g.fillOval(x + squareWidth/4, y + squareHeight/4, squareWidth/2, squareHeight/2);
        }
        // Draw capture moves in red
        g.setColor(new java.awt.Color(255, 0, 0, 128));
        for (Position pos : highlightedCaptureMoves) {
            int[] idx = Position.getOrdinal(pos);
            int row = idx[0];
            int col = idx[1];
            int x = col * squareWidth + BORDER_OFFSET;
            int y = row * squareHeight + BORDER_OFFSET;
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
        int x = col * squareWidth + BORDER_OFFSET;
        int y = row * squareHeight + BORDER_OFFSET;

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

        update(piece);
        evaluate();
        repaint();
    }

    private void evaluate(){
        int whiteScoreByMaterials = 0;
        int blackScoreByMaterials = 0;
        for (Piece p : pieces){
            if (p.getPieceColor() == Color.WHITE){
                whiteScoreByMaterials += p.getValue();
            } else {
                blackScoreByMaterials += p.getValue();
            }
        }
        System.out.println("WHITE MATERIALS: " + whiteScoreByMaterials);
        System.out.println("BLACK MATERIALS: " + blackScoreByMaterials);
        
        // Check for checkmate or stalemate
        Color currentTurn = whiteToMove ? Color.WHITE : Color.BLACK;
        System.out.println("Checking game end for: " + currentTurn);
        boolean inCheck = isInCheck(currentTurn);
        System.out.println("In check: " + inCheck);
        boolean hasLegal = hasLegalMoves(currentTurn);
        System.out.println("Has legal moves: " + hasLegal);
        
        if (!hasLegal) {
            if (inCheck) {
                String winner = (currentTurn == Color.WHITE) ? "Black" : "White";
                System.out.println("CHECKMATE! " + winner + " wins!");
                JOptionPane.showMessageDialog(this, "Checkmate! " + winner + " wins!");
            } else {
                System.out.println("STALEMATE! Draw.");
                JOptionPane.showMessageDialog(this, "Stalemate! It's a draw.");
            }
        }
    }

    private void update(Piece piece){
        //remove double forward
        if (piece instanceof Pawn){
            ((Pawn) piece).updateHasMove();
        }
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

    public boolean isSquareAttackedBy(Position square, Color attackerColor) {
        int[] squareIdx = Position.getOrdinal(square);
        int sqRow = squareIdx[0];
        int sqCol = squareIdx[1];
        
        for (Piece p : pieces) {
            if (p.getPieceColor() != attackerColor) continue;
            
            // King attacks (1 square distance)
            if (p instanceof King) {
                int[] kingIdx = Position.getOrdinal(p.getPosition());
                int rowDiff = Math.abs(kingIdx[0] - sqRow);
                int colDiff = Math.abs(kingIdx[1] - sqCol);
                if (rowDiff <= 1 && colDiff <= 1 && !(rowDiff == 0 && colDiff == 0)) {
                    return true;
                }
                continue;
            }
            
            // Pawn attacks exception to block for checks ot even captures
            if (p instanceof Pawn) {
                int[] pawnIdx = Position.getOrdinal(p.getPosition());
                int pawnRow = pawnIdx[0];
                int pawnCol = pawnIdx[1];

                int attackRow = (attackerColor == Color.WHITE) ? pawnRow - 1 : pawnRow + 1;
                
                if (sqRow == attackRow && (sqCol == pawnCol - 1 || sqCol == pawnCol + 1)) {
                    return true;
                }
                continue;
            }
            
            // check moves and captures
            p.getMoves();
            ArrayList<String> attacks = p.getCaptureMoves();
            for (String atk : attacks) {
                try {
                    if (Position.valueOf(atk) == square) {
                        return true;
                    }
                } catch (IllegalArgumentException ex) {
                    // ignore
                }
            }
            
            // For sliding pieces, check regular moves too
            ArrayList<String> moves = p.getMoves();
            for (String mv : moves) {
                try {
                    if (Position.valueOf(mv) == square) {
                        return true;
                    }
                } catch (IllegalArgumentException ex) {
                    // ignore
                }
            }
        }
        return false;
    }

    public boolean isInCheck(Color kingColor) {
        Position kingPos = null;
        for (Piece p : pieces) {
            if (p instanceof King && p.getPieceColor() == kingColor) {
                kingPos = p.getPosition();
                break;
            }
        }
        if (kingPos == null) return false;
        
        Color enemyColor = (kingColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
        boolean inCheck = isSquareAttackedBy(kingPos, enemyColor);
        return inCheck;
    }

    public boolean hasLegalMoves(Color color) {
        for (Piece p : pieces) {
            if (p.getPieceColor() != color) continue;
            
            // deep copy.
            ArrayList<String> allMoves = new ArrayList<>(p.getMoves());
            allMoves.addAll(p.getCaptureMoves());
            
            for (String moveStr : allMoves) {
                try {
                    Position targetPos = Position.valueOf(moveStr);
                    if (isLegalMove(p, targetPos)) {
                        System.out.println("Legal move found: " + p.getClass().getSimpleName() + " at " + p.getPosition() + " can move to " + targetPos);
                        return true;
                    }
                } catch (IllegalArgumentException ex) {
                    // ignore
                }
            }
        }
        return false;
    }

    public boolean isLegalMove(Piece piece, Position targetPos) {
        Position originalPos = piece.getPosition();
        Piece captured = getPiece(targetPos);
        
        //simulate temporary move
        piece.setPosition(targetPos);
        if (captured != null) {
            pieces.remove(captured);
        }
        
        // if own king still in check if move, then keep being in check
        boolean legal = !isInCheck(piece.getPieceColor());
        
        //revert
        piece.setPosition(originalPos);
        if (captured != null) {
            pieces.add(captured);
        }
        
        return legal;
    }
}