package com.project.chess.model;

import java.util.ArrayList;
import java.util.Random;

public class Zobrist {
    // 12 piece types (6 white + 6 black) * 64 squares
    private static final long[][] PIECE_SQUARE = new long[12][64];
    // Side to move (XOR when its black turn)
    private static final long SIDE_TO_MOVE;
    // Castling rights (4 possibilities: KQkq)
    private static final long[] CASTLING = new long[4];
    // En passant file (8 files)
    private static final long[] EN_PASSANT = new long[8];

    static {
        Random rand = new Random(0xDEADBEEF); // fixed seed
        
        for (int piece = 0; piece < 12; piece++) {
            for (int square = 0; square < 64; square++) {
                PIECE_SQUARE[piece][square] = rand.nextLong();
            }
        }
        
        SIDE_TO_MOVE = rand.nextLong();
        
        for (int i = 0; i < 4; i++) {
            CASTLING[i] = rand.nextLong();
        }
        
        for (int i = 0; i < 8; i++) {
            EN_PASSANT[i] = rand.nextLong();
        }
    }

    /**
     * Get piece index for Zobrist table.
     * White pieces: 0-5 (Pawn, Knight, Bishop, Rook, Queen, King)
     * Black pieces: 6-11
     */
    public static int getPieceIndex(Piece piece) {
        int base = (piece.getPieceColor() == Color.WHITE) ? 0 : 6;
        
        if (piece instanceof Pawn)   return base + 0;
        if (piece instanceof Knight) return base + 1;
        if (piece instanceof Bishop) return base + 2;
        if (piece instanceof Rook)   return base + 3;
        if (piece instanceof Queen)  return base + 4;
        if (piece instanceof King)   return base + 5;
        
        return -1; // Should never happen
    }

    /**
     * Get the Zobrist value for a piece on a square. 
     */
    public static long getPieceSquareHash(Piece piece, Position pos) {
        int pieceIdx = getPieceIndex(piece);
        int squareIdx = pos.ordinal();
        return PIECE_SQUARE[pieceIdx][squareIdx];
    }

    /**
     * Get the side-to-move hash (XOR this when switching sides).
     */
    public static long getSideToMoveHash() {
        return SIDE_TO_MOVE;
    }

    /**
     * Get castling hash.
     * index: 0=white kingside, 1=white queenside, 2=black kingside, 3=black queenside
     */
    public static long getCastlingHash(int index) {
        return CASTLING[index];
    }

    /**
     * Get en passant hash for a file (0-7 = a-h).
     */
    public static long getEnPassantHash(int file) {
        return EN_PASSANT[file];
    }

    /**
     * Compute the full Zobrist hash for a board position.
     */
    public static long computeHash(ArrayList<Piece> pieces, boolean whiteToMove) {
        long hash = 0L;
        
        for (Piece piece : pieces) {
            hash ^= getPieceSquareHash(piece, piece.getPosition());
        }
        
        if (!whiteToMove) {
            hash ^= SIDE_TO_MOVE;
        }
        
        return hash;
    }
}
