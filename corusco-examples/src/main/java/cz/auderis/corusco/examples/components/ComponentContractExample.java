package cz.auderis.corusco.examples.components;

import cz.auderis.corusco.examples.book.BookExampleSupport;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

public final class ComponentContractExample {

    private ComponentContractExample() {
        throw new AssertionError("No instances");
    }

    public static JInternalFrame createWindow() {
        return BookExampleSupport.frame("Component contracts", createContent(), new Dimension(760, 460));
    }

    public static JPanel createContent() {
        BookExampleSupport.requireEdt();
        JLabel status = new JLabel("Click or use arrow keys inside the canvas.");
        ContractCanvas canvas = new ContractCanvas(status);
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(24);

        JPanel panel = new JPanel(new MigLayout("fill, insets 16, gap 10", "[grow]", "[][grow][]"));
        panel.add(new JLabel("A custom component with preferred size, painting, input, and Scrollable behavior."),
                "growx, wrap");
        panel.add(scrollPane, "grow, wrap");
        panel.add(status, "growx");
        return panel;
    }

    public static final class ContractCanvas extends JComponent implements Scrollable {
        private static final int CELL = 48;
        private static final int COLUMNS = 18;
        private static final int ROWS = 14;

        private final JLabel status;
        private final List<Point> marks = new ArrayList<>();
        private Point cursor = new Point(2, 2);

        ContractCanvas(JLabel status) {
            this.status = status;
            setOpaque(true);
            setFocusable(true);
            setToolTipText("Scrollable custom component");
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    requestFocusInWindow();
                    cursor = cellAt(event.getPoint());
                    marks.add(new Point(cursor));
                    status.setText("Mouse selected cell " + cursor.x + ", " + cursor.y);
                    repaint(cellBounds(cursor));
                }
            });
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent event) {
                    moveCursor(event.getKeyCode());
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            Insets insets = getInsets();
            return new Dimension(COLUMNS * CELL + insets.left + insets.right,
                    ROWS * CELL + insets.top + insets.bottom);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                paintGrid(g, graphics.getClipBounds());
                paintMarks(g);
                paintCursor(g);
            } finally {
                g.dispose();
            }
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(520, 280);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return CELL / 2;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL
                    ? Math.max(CELL, visibleRect.height - CELL)
                    : Math.max(CELL, visibleRect.width - CELL);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        private void moveCursor(int keyCode) {
            Point old = new Point(cursor);
            switch (keyCode) {
                case KeyEvent.VK_LEFT -> cursor.x = Math.max(0, cursor.x - 1);
                case KeyEvent.VK_RIGHT -> cursor.x = Math.min(COLUMNS - 1, cursor.x + 1);
                case KeyEvent.VK_UP -> cursor.y = Math.max(0, cursor.y - 1);
                case KeyEvent.VK_DOWN -> cursor.y = Math.min(ROWS - 1, cursor.y + 1);
                default -> {
                    return;
                }
            }
            status.setText("Keyboard selected cell " + cursor.x + ", " + cursor.y);
            Rectangle dirty = cellBounds(old).union(cellBounds(cursor));
            dirty.grow(2, 2);
            repaint(dirty);
            scrollRectToVisible(cellBounds(cursor));
        }

        private void paintGrid(Graphics2D g, Rectangle clip) {
            if (clip == null) {
                clip = new Rectangle(0, 0, getWidth(), getHeight());
            }
            g.setColor(getBackground());
            g.fillRect(clip.x, clip.y, clip.width, clip.height);
            int firstColumn = Math.max(0, clip.x / CELL);
            int lastColumn = Math.min(COLUMNS - 1, (clip.x + clip.width) / CELL);
            int firstRow = Math.max(0, clip.y / CELL);
            int lastRow = Math.min(ROWS - 1, (clip.y + clip.height) / CELL);

            for (int row = firstRow; row <= lastRow; row++) {
                for (int column = firstColumn; column <= lastColumn; column++) {
                    Rectangle cell = new Rectangle(column * CELL, row * CELL, CELL, CELL);
                    g.setColor((row + column) % 2 == 0 ? new Color(0xf5f7fa) : new Color(0xffffff));
                    g.fill(cell);
                    g.setColor(new Color(0xd7dce2));
                    g.draw(cell);
                }
            }
        }

        private void paintMarks(Graphics2D g) {
            g.setColor(new Color(0x4477aa));
            for (Point mark : marks) {
                Rectangle cell = cellBounds(mark);
                g.fillOval(cell.x + 13, cell.y + 13, 22, 22);
            }
        }

        private void paintCursor(Graphics2D g) {
            Rectangle cell = cellBounds(cursor);
            g.setColor(new Color(0xcc3333));
            g.drawRect(cell.x + 3, cell.y + 3, cell.width - 7, cell.height - 7);
            g.drawRect(cell.x + 4, cell.y + 4, cell.width - 9, cell.height - 9);
        }

        private Point cellAt(Point point) {
            return new Point(Math.max(0, Math.min(COLUMNS - 1, point.x / CELL)),
                    Math.max(0, Math.min(ROWS - 1, point.y / CELL)));
        }

        private Rectangle cellBounds(Point cell) {
            return new Rectangle(cell.x * CELL, cell.y * CELL, CELL, CELL);
        }
    }
}
