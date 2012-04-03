package tetris.client;

import java.util.Random;

import pulpcore.animation.Color;
import pulpcore.animation.Easing;
import pulpcore.image.Colors;
import pulpcore.image.CoreGraphics;
import pulpcore.sprite.FilledSprite;
import pulpcore.sprite.Group;
import pulpcore.sprite.Sprite;

public abstract class BoardSprite extends Group {

    public static final int EMPTY = Colors.BLACK;

    public static final int FIXED = Colors.LIGHTGRAY;

    /**
     * The square size in pixels. This value is updated when the component size
     * is changed, i.e. when the <code>size</code> variable is modified.
     */
    private int squareSize;

    private Random rnd;

    /**
     * The square board color matrix. This matrix (or grid) contains a color
     * entry for each square in the board. The matrix is indexed by the
     * vertical, and then the horizontal coordinate.
     */
    private SquareSprite[][] matrix;

    private int bWidth, bHeight;

    private static int borderSize = 2;

    private boolean gameOver;

    private FilledSprite background;

    protected BoardSprite(int x, int y, int bWidth, int bHeight, int squareSize) {
        super(-bWidth * squareSize, y, bWidth * squareSize + borderSize * 2,
                bHeight * squareSize + borderSize * 2);

        this.bWidth = bWidth;
        this.bHeight = bHeight;
        this.squareSize = squareSize;

        background = new FilledSprite(0, 0, width.getAsInt(),
                height.getAsInt(), Colors.BLACK);
        background.borderColor.set(Colors.LIGHTGRAY);
        background.setBorderSize(borderSize);
        add(background);

        this.x.animateTo(x, 500);

        this.rnd = new Random();
    }

    public void gameStart() {
        this.matrix = new SquareSprite[bHeight][bWidth];
        // initialize empty matrix
        for (int i = 0; i < bHeight; i++) {
            for (int j = 0; j < bWidth; j++) {
                matrix[i][j] = new SquareSprite(j * squareSize + borderSize, i
                        * squareSize + borderSize, EMPTY);
                add(matrix[i][j]);
            }
        }
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isSquareEmpty(int x, int y) {
        if (x < 0 || x >= bWidth || y < 0 || y >= bHeight) {
            return x >= 0 && x < bWidth && y < 0;
        }
        else {
            synchronized (matrix) {
                return matrix[y][x].getColor() == EMPTY;
            }
        }
    }

    public void setSquareColor(int x, int y, int color) {
        if (x < 0 || x >= bWidth || y < 0 || y >= bHeight) {
            return;
        }

        synchronized (matrix) {
            matrix[y][x].setColor(color);
        }
    }

    public void fix(int x, int y) {
        if (x < 0 || x >= bWidth || y < 0 || y >= bHeight) {
            return;
        }

        synchronized (matrix) {
            matrix[y][x].fix();
        }
    }

    protected void fill(int color) {
        for (int y = 0; y < bHeight; y++) {
            for (int x = 0; x < bWidth; x++) {
                setSquareColor(x, y, color);
            }
        }
    }

    public void clear() {
        fill(EMPTY);
    }

    /**
     * Checks if a specified line is full, i.e. only contains no empty squares.
     * If the line is outside the board, true will always be returned.
     * 
     * @param y
     *            the vertical position (0 <= y < height)
     * 
     * @return true if the whole line is full, or false otherwise
     */
    private boolean isLineFull(int y) {
        if (y < 0 || y >= bHeight) {
            return true;
        }
        synchronized (matrix) {
            for (int x = 0; x < bWidth; x++) {
                if (!matrix[y][x].isFixed()) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Removes all full lines. All lines above a removed line will be moved
     * downward one step, and a new empty line will be added at the top. After
     * removing all full lines, the component will be repainted.
     * 
     * @return the number of lines removed
     */
    public int removeFullLines() {
        int removedLines = 0;

        synchronized (matrix) {
            // Remove full lines
            for (int y = bHeight - 1; y >= 0; y--) {
                if (isLineFull(y)) {
                    removeLine(y);
                    removedLines++;
                    y++;
                }
            }
        }

        return removedLines;
    }

    /**
     * Removes a single line. All lines above are moved down one step, and a new
     * empty line is added at the top.
     * 
     * @param y
     *            the vertical position (0 <= y < height)
     */
    private void removeLine(int y) {
        if (y < 0 || y >= bHeight) {
            return;
        }
        synchronized (matrix) {
            for (; y > 0; y--) {
                for (int x = 0; x < bWidth; x++) {
                    // push down except OCCUPIED
                    if ((matrix[y - 1][x].isFixed() || matrix[y - 1][x]
                            .getColor() == EMPTY)
                            && (matrix[y][x].isFixed() || matrix[y][x]
                                    .getColor() == EMPTY)) {
                        matrix[y][x].copy(matrix[y - 1][x]);
                    }
                }
            }

            // empty first row
            for (int x = 0; x < bWidth; x++) {
                if (matrix[0][x].isFixed()) {
                    matrix[0][x].setColor(EMPTY);
                    matrix[0][x].setFixed(false);
                }
            }
        }
    }

    public void pushLines(int lines) {
        synchronized (matrix) {
            for (int l = 0; l < lines; l++) {
                for (int y = 0; y < bHeight - 1; y++) {
                    for (int x = 0; x < bWidth; x++) {
                        // subo todo /lines/ lineas
                        if ((matrix[y + 1][x].isFixed() || matrix[y + 1][x]
                                .getColor() == EMPTY)
                                && (matrix[y][x].isFixed() || matrix[y][x]
                                        .getColor() == EMPTY)) {
                            matrix[y][x].copy(matrix[y + 1][x]);
                        }
                    }
                }
            }

            // fill last /lines/ rows with random
            for (int l = 1; l <= lines; l++) {
                for (int x = 0; x < bWidth; x++) {
                    boolean fixed = Math.random() > 0.6;
                    matrix[bHeight - l][x].setColor(fixed ? FIXED : EMPTY);
                    matrix[bHeight - l][x].setFixed(fixed);
                }
            }
        }
    }

    private class SquareSprite extends Sprite {

        private Color color;

        private boolean fixed;

        public SquareSprite(int x, int y, int color) {
            super(x, y, squareSize, squareSize);
            this.color = new Color(color);
            this.fixed = false;
        }

        private void copy(SquareSprite other) {
            setFixed(other.isFixed());
            setColor(other.getColor());
            color.setBehavior(other.color.getBehavior());
        }

        public int getColor() {
            return color.get();
        }

        public void setColor(int color) {
            this.color.set(color);

            setDirty(true);
        }

        @Override
        public void update(int elapsedTime) {
            super.update(elapsedTime);

            if (gameOver && !color.isAnimating()) {
                color.animateTo(0xFF000000 | rnd.nextInt(), 900 + rnd
                        .nextInt(300), Easing.REGULAR_IN_OUT);
            }

            int old = color.get();
            color.update(elapsedTime);

            if (color.get() != old) {
                setDirty(true);
            }
        }

        public void fix() {
            fixed = true;
            setDirty(true);
            // TODO efectito
        }

        public boolean isFixed() {
            return fixed;
        }

        public void setFixed(boolean fixed) {
            this.fixed = fixed;
        }

        @Override
        protected void drawSprite(CoreGraphics g) {
            int w = width.getAsInt();
            int h = height.getAsInt();

            int xMax = w - 1;
            int yMax = h - 1;

            // Fill with base color
            g.setColor(color.get());
            g.fillRect(0, 0, w, h);

            if (color.get() != EMPTY) {
                toString();
            }

            if (!isFixed()) {
                // Draw brighter lines
                g.setColor(Colors.brighter(color.get()));
                for (int i = 0; i < squareSize / 10; i++) {
                    g.drawLine(i, i, xMax - i, i);
                    g.drawLine(i, i, i, yMax - i);
                }

                // Draw darker lines
                g.setColor(Colors.darker(color.get()));
                for (int i = 0; i < squareSize / 10; i++) {
                    g.drawLine(xMax - i, i, xMax - i, yMax - i);
                    g.drawLine(i, yMax - i, xMax - i, yMax - i);
                }
            }
        }

        @Override
        public String toString() {
            return "(" + x.getAsInt() / squareSize + ", " + y.getAsInt()
                    / squareSize + ", " + fixed + ")";
        }
    }

    public int getBoardWidth() {
        return bWidth;
    }

    public int getBoardHeight() {
        return bHeight;
    }

    public byte[] getState() {
        byte[] ret = new byte[bHeight];

        synchronized (matrix) {
            for (int y = 0; y < bHeight; y++) {

                ret[y] = 0;

                for (int x = 0; x < bWidth; x++) {
                    if (matrix[y][x].isFixed()) {
                        ret[y] += 1 << x;
                    }
                }
            }
        }

        return ret;
    }

    public void setState(byte[] state) {
        synchronized (matrix) {
            for (int y = 0; y < state.length; y++) {
                byte b = state[y];

                for (int x = 0; x < bWidth; x++) {
                    if ((b >> x & 1) != 0) {
                        matrix[y][x].setColor(FIXED);
                        matrix[y][x].setFixed(true);
                    }
                    else {
                        matrix[y][x].setColor(EMPTY);
                        matrix[y][x].setFixed(false);
                    }
                }
            }
        }
    }
}