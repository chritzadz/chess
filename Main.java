import javax.swing.*;
import java.awt.*;

public class Main extends JPanel{
    public Main() {
        JFrame frame = new JFrame();
        Board board = new Board("materials/board/MainBoard.png");
        board.setBorder(BorderFactory.createEmptyBorder(360, 360, 360, 360));
        board.setLayout(new BorderLayout());

        frame.add(board, BorderLayout.CENTER);
        frame.getContentPane().setBackground(Color.WHITE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Chess");
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new Main();
    }
}
