import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main extends JPanel{
    public Main() throws IOException {
        JFrame frame = new JFrame();
        Board board = new Board("board.png");
        board.setPreferredSize(new Dimension(524, 524));

        //set piece example
        String content = Files.readString(Path.of("fen.txt"), StandardCharsets.UTF_8);
        Board updateBoard = InitUtil.init(content, board);

        frame.add(updateBoard);
        frame.getContentPane().setBackground(java.awt.Color.WHITE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Chess");
        frame.pack();
        frame.setLocationRelativeTo(null); // Center window
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        new Main();
    }
}
