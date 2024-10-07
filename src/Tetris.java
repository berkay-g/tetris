import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Tetris extends JPanel implements KeyListener, Runnable {

    Thread gameThread;
    double deltaTime;
    private Set<Integer> pressedKeys = new HashSet<>();

    int rows = 20, cols = 10;
    int field[][] = new int[rows][cols];
    final Color colors[] = { new Color(0xf8fcff), new Color(0xf8fcff), new Color(0xf8fcff),
            new Color(0x2131da), new Color(0x4ed0f7), new Color(0x2131da), new Color(0x4ed0f7), Color.black };

    Random rand = new Random();
    Tetromino tetromino;
    int nextTetromino;

    double fallCounter = 0;

    int score = 0;
    long updates = 0;

    int errors = 0;
    boolean paused = false;

    ArrayList<Integer> rowsToClear = new ArrayList<Integer>();
    int currentColumn = 0;

    int totalClearedLines = 0;
    int startLevel;
    int level;
    float speedsByLevel[] = { 15.974f, 14.310f, 12.646f, 10.982f, 9.318f, 7.654f, 5.990f, 4.326f, 2.662f, 1.997f,
            1.664f, 1.664f, 1.664f, 1.331f, 1.331f, 1.331f, 0.998f, 0.998f, 0.998f, 0.666f, 0.666f, 0.666f, 0.666f,
            0.666f, 0.666f, 0.666f, 0.666f, 0.666f, 0.666f, 0.333f };

    public Tetris(int width, int height, int startlevel) {
        setPreferredSize(new Dimension(width, height));
        setVisible(true);
        setFocusable(true);
        setLayout(null);
        addKeyListener(this);
        setBackground(new Color(0x747474));

        tetromino = new Tetromino(rand.nextInt(7));
        nextTetromino = rand.nextInt(7);

        startLevel = startlevel;
        level = startLevel;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long currentTime, frameDuration, sleepTime;

        final double targetFPS = 30.0;
        final double targetTime = 1e9 / targetFPS; // Target time per frame in nanoseconds

        while (gameThread != null) {
            currentTime = System.nanoTime();
            deltaTime = (currentTime - lastTime) / 1e9; // Convert to seconds
            lastTime = currentTime;

            if (!paused)
                updateGame();
            repaint();

            updates++;

            // Calculate how long to sleep to maintain the target FPS
            frameDuration = System.nanoTime() - currentTime;
            sleepTime = (long) (targetTime - frameDuration);

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / (long) 1e6, (int) (sleepTime % 1e6));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void updateGame() {

        if (!rowsToClear.isEmpty()) {
            if (updates % 3 == 0) {
                for (int i : rowsToClear) {
                    field[i][4 - currentColumn] = 8;
                    field[i][5 + currentColumn] = 8;
                }
                currentColumn += 1;
                if (currentColumn > 4) {
                    currentColumn = 0;

                    if (rowsToClear.size() == 1)
                        score += 40 * (level + 1);
                    else if (rowsToClear.size() == 2)
                        score += 100 * (level + 1);
                    else if (rowsToClear.size() == 3)
                        score += 300 * (level + 1);
                    else if (rowsToClear.size() == 4)
                        score += 1200 * (level + 1);

                    rowsToClear.clear();
                    clearRows();
                }
            }
            return;
        }

        fallCounter += deltaTime;
        while (fallCounter >= speedsByLevel[Math.min(level, 29)] / 19f) {
            fallCounter -= speedsByLevel[Math.min(level, 29)] / 19f;
            moveTetramino(1, 0);
        }

        if (!pressedKeys.isEmpty()) {
            for (Iterator<Integer> it = pressedKeys.iterator(); it.hasNext();) {
                Integer event = it.next();
                if (event == KeyEvent.VK_DOWN) {
                    moveTetramino(1, 0);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawField(g, 15, 15, 15);
        drawTetromino(g, 15, 15, 15);
        g.drawString("Score: " + score, 230, 80);
        g.drawString("Level: " + level, 230, 160);

        g.setColor(Color.ORANGE);
        g.drawString("← ↑ → ↓ R", 230, 200);
        g.drawString("Lines: " + totalClearedLines, 230, 40);
    }

    private void drawField(Graphics g, int x, int y, int size) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (field[i][j] == 0)
                    g.setColor(Color.black);
                else
                    g.setColor(colors[field[i][j] - 1]);
                g.fillRect(x + j * size, y + i * size, size, size);
            }
        }
    }

    private void drawTetromino(Graphics g, int x, int y, int size) {
        int p[][] = tetromino.getPosition();
        g.setColor(colors[tetromino.currentTetromino]);
        for (int i = 0; i < p.length; i++) {
            g.fillRect(x + p[i][1] * size, y + p[i][0] * size, size, size);
        }

        g.setColor(colors[nextTetromino]);
        for (int i = 0; i < 4; i++) {
            g.fillRect(x + (tetromino.TETROMINOS[nextTetromino][i][1] + 15) * size,
                    y + (tetromino.TETROMINOS[nextTetromino][i][0] + 5) * size, size, size);
        }
    }

    private void moveTetramino(int r, int c) {
        if (r == 1) { // Moving down
            if (canMoveDown()) {
                tetromino.move(1, 0); // Move tetromino down
            } else {
                applyTetromino(); // Apply tetromino if it cannot move down
            }
        } else { // Moving left or right
            if (!checkSideCollisions(c)) {
                tetromino.move(0, c);
            }
        }
    }

    private boolean canMoveDown() {
        int p[][] = tetromino.getPosition();
        for (int i = 0; i < p.length; i++) {
            if (p[i][0] + 1 >= rows || field[p[i][0] + 1][p[i][1]] != 0) {
                return false;
            }
        }
        return true;
    }

    private void applyTetromino() {
        if (errors > 50) {
            paused = true;
            return;
        }

        int p[][] = tetromino.getPosition();

        int[][] fieldCopy = new int[field.length][];
        for (int i = 0; i < field.length; i++)
            fieldCopy[i] = field[i].clone();

        int filledBefore = countFilledCells();

        for (int i = 0; i < p.length; i++) {
            int row = p[i][0];
            int col = p[i][1];

            // Boundary check to ensure row and column are within the field
            if (row >= 0 && row < field.length && col >= 0 && col < field[0].length) {
                if (field[row][col] != 0) {
                    System.out.println("Invalid placement: position already filled.");
                    errors++;
                }
                field[row][col] = tetromino.currentTetromino + 1;
            } else {
                System.out.println("Invalid tetromino position");
                errors++;
            }
        }

        int filledAfter = countFilledCells();
        int difference = filledAfter - filledBefore;
        if (difference > 4) {
            System.out.println(
                    "Warning: Difference in filled cells exceeds 4! Difference: " + difference);
            field = fieldCopy;
            applyTetromino();
            return;
        }

        tetromino.reset(nextTetromino);
        nextTetromino = rand.nextInt(7);

        if (rowsToClear.isEmpty())
            checkRows();
    }

    private int countFilledCells() {
        int count = 0;
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (field[i][j] != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private void checkRows() {
        for (int i = field.length - 1; i >= 0; i--) {
            if (isRowFull(field[i])) {
                rowsToClear.add(i);
            }
        }
    }

    private void clearRows() {
        int clearedRows = 0;

        // Iterate from the bottom row up to the top row
        for (int i = field.length - 1; i >= 0; i--) {
            if (isRowFull(field[i])) {
                clearedRows++; // Count cleared rows
            } else if (clearedRows > 0) {
                // Move the current row down by the number of cleared rows
                field[i + clearedRows] = field[i];
            }
        }

        // Fill the top cleared rows with zeros
        for (int i = 0; i < clearedRows; i++) {
            Arrays.fill(field[i], 0); // Reset cleared rows at the top
        }

        totalClearedLines += clearedRows;

        if (startLevel < totalClearedLines / 10)
            level = totalClearedLines / 10;
    }

    private static boolean isRowFull(int[] row) {
        for (int value : row) {
            if (value == 0) {
                return false;
            }
        }
        return true;
    }

    private void resetField() {
        field = new int[rows][cols];
        updates = 0;
        score = 0;
        currentColumn = 0;
        level = startLevel;
        totalClearedLines = 0;
        errors = 0;
    }

    private boolean checkSideCollisions(int c) {
        int p[][] = tetromino.getPosition();
        for (int i = 0; i < p.length; i++) {
            if (p[i][1] + c > cols - 1 || p[i][1] + c < 0 ||
                    field[p[i][0]][p[i][1] + c] != 0)
                return true;
        }
        return false;
    }

    private void rotateTetromino() {
        if (!rowsToClear.isEmpty())
            return;

        tetromino.rotateCW();

        int p[][] = tetromino.getPosition();
        boolean valid = true;
        for (int i = 0; i < 4; i++) {
            if (p[i][1] > cols - 1 || p[i][1] < 0
                    || p[i][0] > rows - 1
                    || p[i][0] < 0
                    || field[p[i][0]][p[i][1]] != 0) {
                valid = false;
                break;
            }
        }
        if (!valid) {
            tetromino.rotateCW();
            tetromino.rotateCW();
            tetromino.rotateCW();
        }
    }

    private void rotateTetrominoCCW() {
        if (!rowsToClear.isEmpty())
            return;

        tetromino.rotateCW();
        tetromino.rotateCW();
        tetromino.rotateCW();

        int p[][] = tetromino.getPosition();
        boolean valid = true;
        for (int i = 0; i < 4; i++) {
            if (p[i][1] > cols - 1 || p[i][1] < 0
                    || p[i][0] > rows - 1
                    || p[i][0] < 0
                    || field[p[i][0]][p[i][1]] != 0) {
                valid = false;
                break;
            }
        }
        if (!valid) {
            tetromino.rotateCW();
        }
    }

    public void launchGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!rowsToClear.isEmpty())
            return;

        pressedKeys.add(e.getKeyCode());
        if (!pressedKeys.isEmpty()) {
            for (Iterator<Integer> it = pressedKeys.iterator(); it.hasNext();) {
                Integer event = it.next();
                if (event == KeyEvent.VK_LEFT) {
                    moveTetramino(0, -1);
                } else if (event == KeyEvent.VK_RIGHT) {
                    moveTetramino(0, 1);
                } else if (event == KeyEvent.VK_UP) {
                    rotateTetromino();
                } else if (event == KeyEvent.VK_Z) {
                    rotateTetrominoCCW();
                } else if (event == KeyEvent.VK_X) {
                    rotateTetromino();
                } else if (event == KeyEvent.VK_R) {
                    resetField();
                    paused = false;
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }
}