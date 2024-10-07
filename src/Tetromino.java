public class Tetromino {

    public final int TETROMINOS[][][] = {
        { { 1, 0 }, { 1, 1 }, { 1, 2 }, { 1, 3 } }, // I
        { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 1, 1 } }, // O
        { { 0, 1 }, { 1, 0 }, { 1, 1 }, { 1, 2 } }, // T
        { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 1, 2 } }, // J
        { { 0, 2 }, { 1, 0 }, { 1, 1 }, { 1, 2 } }, // L
        { { 0, 1 }, { 0, 2 }, { 1, 0 }, { 1, 1 } }, // S
        { { 0, 0 }, { 0, 1 }, { 1, 1 }, { 1, 2 } }, // Z
    };
    
    private int tetromino[][];
    public int offsetR, offsetC;
    public int currentTetromino;

    public Tetromino(int type)
    {
        currentTetromino = type;
        tetromino = TETROMINOS[currentTetromino];
        offsetC = 5 - tetromino[0].length / 2;
    }

    public void reset(int type) {
        offsetC = 5 - tetromino[0].length / 2;
        offsetR = 0;
        currentTetromino = type;
        tetromino = TETROMINOS[currentTetromino];
    }

    public void rotateCW() {
        int n = tetromino.length;

        // Extract ys and xs
        int[] ys = new int[n];
        int[] xs = new int[n];
        for (int i = 0; i < n; i++) {
            ys[i] = tetromino[i][0]; // row
            xs[i] = tetromino[i][1]; // column
        }

        // Calculate size of the bounding box
        int maxY = ys[0], minY = ys[0];
        int maxX = xs[0], minX = xs[0];
        for (int i = 1; i < n; i++) {
            if (ys[i] > maxY) maxY = ys[i];
            if (ys[i] < minY) minY = ys[i];
            if (xs[i] > maxX) maxX = xs[i];
            if (xs[i] < minX) minX = xs[i];
        }
        int size = Math.max(maxY - minY, maxX - minX);

        // Create rotated tetromino
        int[][] rotatedTetromino = new int[n][2];
        for (int i = 0; i < n; i++) {
            rotatedTetromino[i][0] = xs[i];             // new x (original y)
            rotatedTetromino[i][1] = size - ys[i];      // new y (size - original x)
        }
        
        tetromino = rotatedTetromino;
    }

    public void move(int r, int c)
    {
        offsetC += c;
        offsetR += r;
    }

    public int[][] getPosition() {
        int[][] newPosition = new int[tetromino.length][2]; // Create a new array for the position
    
        for (int i = 0; i < tetromino.length; i++) {
            newPosition[i][0] = tetromino[i][0] + offsetR; // Adjust row
            newPosition[i][1] = tetromino[i][1] + offsetC; // Adjust column
        }
    
        return newPosition; // Return the new position array
    }
}
