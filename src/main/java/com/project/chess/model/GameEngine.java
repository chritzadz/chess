package com.project.chess.model;


import java.util.ArrayList;

public class GameEngine {
    private ArrayList<Piece> pieces = new ArrayList<>();
    private boolean whiteToMove = true;
    private boolean gameOver = false;
    private String winner = null;
    private long zobristKey = 0L;
    private Color playerColor = Color.WHITE; // The human player's color

    public GameEngine() {
        initializeBoard();
    }

    public void initializeBoard() {
        initializeBoard(Color.WHITE);
    }

    public void initializeBoard(Color playerColor) {
        pieces.clear();
        whiteToMove = true;
        gameOver = false;
        winner = null;
        zobristKey = 0L;
        this.playerColor = playerColor;
        
        // Always use standard starting position
        // Visual flipping is handled in the frontend
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        initFromFen(fen);
        
        // Calculate initial Zobrist hash
        recalculateZobristKey();
    }

    public String processMove(String move){
        move = move.trim();
        if (move.length() == 4) {
            String fromStr = move.substring(0, 2);
            String toStr = move.substring(2, 4);
            Position from = Position.fromAlgebraic(fromStr);
            Position to = Position.fromAlgebraic(toStr);
            boolean success = move(from, to);
            if (success) {
                return "OK";
            } else {
                return "ILLEGAL";
            }
        }
        return "INVALID";
    }

    private void initFromFen(String fenString) {
        String[] rows = fenString.split("/");
        int rowIndex = 0;
        for (String row : rows) {
            int colIndex = 0;
            for (int i = 0; i < row.length(); i++) {
                char c = row.charAt(i);
                if (c >= '1' && c <= '8') {
                    colIndex += c - '0';
                    continue;
                }
                Position position = Position.getPosition(rowIndex, colIndex);
                Color color = Character.isUpperCase(c) ? Color.WHITE : Color.BLACK;
                switch (Character.toLowerCase(c)) {
                    case 'r' -> pieces.add(new Rook(color, position, this));
                    case 'n' -> pieces.add(new Knight(color, position, this));
                    case 'b' -> pieces.add(new Bishop(color, position, this));
                    case 'q' -> pieces.add(new Queen(color, position, this));
                    case 'k' -> pieces.add(new King(color, position, this));
                    case 'p' -> pieces.add(new Pawn(color, position, this));
                }
                colIndex++;
            }
            rowIndex++;
        }
    }

    public Piece getPiece(Position pos) {
        for (Piece p : pieces) {
            if (p.getPosition() == pos) return p;
        }
        return null;
    }

    public ArrayList<Piece> getPieces() {
        return pieces;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getWinner() {
        return winner;
    }

    public boolean move(Position from, Position to) {
        if (gameOver) return false;

        Piece piece = getPiece(from);
        if (piece == null) return false;

        Color currentTurn = whiteToMove ? Color.WHITE : Color.BLACK;
        if (piece.getPieceColor() != currentTurn) return false;

        ArrayList<Position> legalMoves = getLegalMovesFor(piece);
        if (!legalMoves.contains(to)) return false;

        // XOR out the piece from old position
        zobristKey ^= Zobrist.getPieceSquareHash(piece, from);

        Piece captured = getPiece(to);
        if (captured != null) {
            // XOR out the captured piece
            zobristKey ^= Zobrist.getPieceSquareHash(captured, to);
            pieces.remove(captured);
            if (captured instanceof King) {
                gameOver = true;
                winner = currentTurn.name();
            }
        }

        // Move the piece
        piece.setPosition(to);

        // XOR in the piece at new position
        zobristKey ^= Zobrist.getPieceSquareHash(piece, to);

        // Pawn special handling
        if (piece instanceof Pawn) {
            ((Pawn) piece).updateHasMove();
            // Auto-promote to queen
            int[] idx = Position.getOrdinal(to);
            if ((piece.getPieceColor() == Color.WHITE && idx[0] == 0) ||
                (piece.getPieceColor() == Color.BLACK && idx[0] == 7)) {
                // XOR out the pawn
                zobristKey ^= Zobrist.getPieceSquareHash(piece, to);
                pieces.remove(piece);
                Queen promotedQueen = new Queen(piece.getPieceColor(), to, this);
                pieces.add(promotedQueen);
                // XOR in the queen
                zobristKey ^= Zobrist.getPieceSquareHash(promotedQueen, to);
            }
        }

        // Toggle side to move
        zobristKey ^= Zobrist.getSideToMoveHash();
        whiteToMove = !whiteToMove;

        // Check for checkmate/stalemate
        if (!gameOver) {
            Color nextColor = whiteToMove ? Color.WHITE : Color.BLACK;
            if (!hasLegalMoves(nextColor)) {
                gameOver = true;
                if (isInCheck(nextColor)) {
                    winner = currentTurn.name(); // Checkmate
                } else {
                    winner = "DRAW"; // Stalemate
                }
            }
        }

        return true;
    }

    public ArrayList<Position> getLegalMovesFor(Piece piece) {
        ArrayList<Position> allMoves = new ArrayList<>(piece.getMoves());
        allMoves.addAll(piece.getCaptureMoves());
        
        ArrayList<Position> legalMoves = new ArrayList<>();
        for (Position targetPos : allMoves) {
            if (isLegalMove(piece, targetPos)) {
                legalMoves.add(targetPos);
            }
        }
        return legalMoves;
    }

    public boolean isLegalMove(Piece piece, Position targetPos) {
        Position originalPos = piece.getPosition();
        Piece captured = getPiece(targetPos);

        // Simulate move
        piece.setPosition(targetPos);
        if (captured != null) pieces.remove(captured);

        boolean legal = !isInCheck(piece.getPieceColor());

        // Revert
        piece.setPosition(originalPos);
        if (captured != null) pieces.add(captured);

        return legal;
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
        return isSquareAttackedBy(kingPos, enemyColor);
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

            // Pawn attacks (diagonal only)
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

            // Other pieces - check their moves
            p.getMoves();
            ArrayList<Position> attacks = p.getCaptureMoves();
            for (Position atk : attacks) {
                if (atk == square) return true;
            }
            ArrayList<Position> moves = p.getMoves();
            for (Position mv : moves) {
                if (mv == square) return true;
            }
        }
        return false;
    }

    public boolean hasLegalMoves(Color color) {
        for (Piece p : pieces) {
            if (p.getPieceColor() != color) continue;
            if (!getLegalMovesFor(p).isEmpty()) return true;
        }
        return false;
    }

    public long getZobristKey() {
        return zobristKey;
    }

    public void recalculateZobristKey() {
        zobristKey = Zobrist.computeHash(pieces, whiteToMove);
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public void reset() {
        initializeBoard(playerColor);
    }

    public void reset(Color playerColor) {
        initializeBoard(playerColor);
    }

    public String getGameStateString() {
        StringBuilder sb = new StringBuilder();
        String[][] board = new String[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board[r][c] = ".";
            }
        }
        for (Piece p : pieces) {
            int[] idx = Position.getOrdinal(p.getPosition());
            String symbol = p.getClass().getSimpleName().substring(0,1);
            if (p.getPieceColor() == Color.WHITE) symbol = symbol.toUpperCase();
            else symbol = symbol.toLowerCase();
            board[idx[0]][idx[1]] = symbol;
        }
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                sb.append(board[r][c]).append(" ");
            }
            sb.append("  ").append(8 - r).append("\n");
        }
        sb.append("a b c d e f g h\n");
        sb.append("Turn: ").append(whiteToMove ? "White" : "Black").append("\n");
        if (gameOver) {
            sb.append("Game Over! Winner: ").append(winner).append("\n");
        }
        return sb.toString();
    }

    public String generateFEN() {
    StringBuilder fen = new StringBuilder();
    for (int row = 0; row < 8; row++) {
        int emptyCount = 0;
        for (int col = 0; col < 8; col++) {
            Position pos = Position.getPosition(row, col);
            Piece piece = null;
            for (Piece p : pieces) {
                if (p.getPosition().equals(pos)) {
                    piece = p;
                    break;
                }
            }
            if (piece == null) {
                emptyCount++;
            } else {
                if (emptyCount > 0) {
                    fen.append(emptyCount);
                    emptyCount = 0;
                }
                String symbol = piece.getType().substring(0,1);
                symbol = piece.getPieceColor() == Color.WHITE ? symbol.toUpperCase() : symbol.toLowerCase();
                fen.append(symbol);
            }
        }
        if (emptyCount > 0) fen.append(emptyCount);
        if (row < 7) fen.append("/");
    }
    return fen.toString();
}
}
