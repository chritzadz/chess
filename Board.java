import javax.swing.*;
import java.awt.*;

public class Board extends JPanel {
    private final Image boardImage;

    public Board(String imagePath) {
        this.boardImage = new ImageIcon(imagePath).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the image to fill the entire panel
        g.drawImage(boardImage, 0, 0, getWidth(), getHeight(), this);
    }
}