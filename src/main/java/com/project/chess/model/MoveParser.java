package com.project.chess.model;

public class MoveParser {
    public static Move parse(String notation) {
        // Basic parser for algebraic notation (e.g., Nxe5, e4, Qh4, O-O)
        // You may want to expand this for full chess notation support
        notation = notation.trim();
        if (notation.equals("O-O")) {
            return new Move("K", true, false, false, false, null, null);
        }
        if (notation.equals("O-O-O")) {
            return new Move("K", false, true, false, false, null, null);
        }
        String piece = "P";
        int idx = 0;
        if (notation.length() > 0 && Character.isUpperCase(notation.charAt(0)) && notation.charAt(0) != 'O') {
            piece = String.valueOf(notation.charAt(0));
            idx++;
        }
        boolean isCapture = notation.contains("x");
        String dest = notation.substring(notation.length() - 2);
        // You can add more parsing for disambiguation, promotion, check, etc.
        return new Move(piece, false, false, isCapture, false, null, dest);
    }

    public static class Move {
        public String piece;
        public boolean kingsideCastle;
        public boolean queensideCastle;
        public boolean capture;
        public boolean promotion;
        public String from;
        public String to;
        public Move(String piece, boolean kingsideCastle, boolean queensideCastle, boolean capture, boolean promotion, String from, String to) {
            this.piece = piece;
            this.kingsideCastle = kingsideCastle;
            this.queensideCastle = queensideCastle;
            this.capture = capture;
            this.promotion = promotion;
            this.from = from;
            this.to = to;
        }
    }
}
