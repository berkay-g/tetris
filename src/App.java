import javax.swing.*;

public class App {
    static int windowWidth = 360;
    static int windowHeight = 330;

    public static void main(String[] args) {

        JFrame frame = new JFrame("Tetris");
        frame.setVisible(true);
        frame.setSize(windowWidth, windowHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int startLevel = 0;
        if (args.length > 0)
            startLevel = Integer.parseInt(args[0]);

        Tetris game = new Tetris(windowWidth, windowHeight, Math.clamp(startLevel, 0, 29));
        frame.add(game);
        frame.pack();
        game.requestFocus();
        game.launchGame();
    }
}
