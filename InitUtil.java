public class InitUtil {
    public static Board init(String fenString, Board board){
        System.out.println("[INIT] ");
        String[] rows = fenString.split("/");
        int rowIndex = 0;
        for (String row : rows) {
            int colIndex = 0;
            for (int i = 0; i < row.length(); i++) {
                char c = row.charAt(i);
                Position position = Position.values()[rowIndex * 8 + colIndex];
                if (c >= '1' && c <= '8') {
                    colIndex += c - '0';
                    continue;
                }
                Color color = Character.isUpperCase(c) ? Color.WHITE : Color.BLACK;
                switch (Character.toLowerCase(c)) {
                    case 'r':
                        System.out.println("ADD ROOK");
                        board.addPiece(new Rook(color, position, board));
                        break;
                    case 'n':
                        System.out.println("ADD KNIGHT");
                        board.addPiece(new Knight(color, position, board));
                        break;
                    case 'b':
                        System.out.println("ADD BISHOP");
                        board.addPiece(new Bishop(color, position, board));
                        break;
                    case 'q':
                        System.out.println("ADD QUEEN");
                        board.addPiece(new Queen(color, position, board));
                        break;
                    case 'k':
                        System.out.println("ADD KING");
                        board.addPiece(new King(color, position, board));
                        break;
                    case 'p':
                        System.out.println("ADD PAWN");
                        board.addPiece(new Pawn(color, position, board));
                        break;
                }
                colIndex++;
            }
            rowIndex++;
        }
        return board;
    }
}
