package cz.auderis.corusco.examples.book;

import cz.auderis.corusco.swing.task.BusyOverlayLayerUI;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.text.DefaultHighlighter;
import net.miginfocom.swing.MigLayout;

/**
 * Deterministic Swing panels rendered as book screenshots.
 */
final class BookVisualFigures {

    private static final Color INK = new Color(0x27323a);
    private static final Color MUTED = new Color(0x63717c);
    private static final Color LINE = new Color(0xb8c2cc);
    private static final Color PANEL = new Color(0xf7f8fa);
    private static final Color BLUE = new Color(0x2f6f9f);
    private static final Color GREEN = new Color(0x4f8f6b);
    private static final Color RED = new Color(0xb94a48);
    private static final Color AMBER = new Color(0xb9822b);

    private BookVisualFigures() {
        throw new AssertionError("No instances");
    }

    static JPanel refreshResponsibilities() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Swing screen responsibilities",
                "[grow,fill][grow,fill][grow,fill]", "[][grow][]");
        panel.add(responsibility("Component tree",
                "JPanel, JToolBar, JTable, fields",
                "owns containment and geometry"), "grow");
        panel.add(responsibility("Models",
                "TableModel, Document, selection",
                "own state and change events"), "grow");
        panel.add(responsibility("Actions",
                "Refresh, Save, Help",
                "own command identity"), "grow, wrap");
        panel.add(note("Lifecycle question",
                "Every listener, binding, timer, task, and model subscription needs an owner that can close it."),
                "span 3, growx");
        return panel;
    }

    static JPanel refreshContractLoop() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("The small Swing contract loop",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setColor(new Color(0xe7edf3));
            g.fillRoundRect(20, 18, c.getWidth() - 40, c.getHeight() - 36, 18, 18);
            g.setColor(LINE);
            g.drawRoundRect(20, 18, c.getWidth() - 40, c.getHeight() - 36, 18, 18);

            Rectangle event = new Rectangle(42, 48, 118, 54);
            Rectangle model = new Rectangle(204, 48, 128, 54);
            Rectangle command = new Rectangle(376, 48, 128, 54);
            Rectangle component = new Rectangle(204, 162, 128, 54);
            Rectangle worker = new Rectangle(42, 162, 118, 54);
            Rectangle scope = new Rectangle(376, 162, 128, 54);

            drawFlowBox(g, event, "EDT event", "click / edit");
            drawFlowBox(g, model, "model fact", "value changed");
            drawFlowBox(g, command, "command state", "enabled / busy");
            drawFlowBox(g, component, "component", "display + repaint");
            drawFlowBox(g, worker, "worker", "slow work");
            drawFlowBox(g, scope, "scope", "cleanup owner");

            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2.2f));
            drawArrow(g, 160, 75, 204, 75);
            drawArrow(g, 332, 75, 376, 75);
            drawArrow(g, 440, 102, 330, 162);
            drawArrow(g, 204, 188, 160, 188);
            drawArrow(g, 100, 162, 100, 104);
            g.setColor(AMBER);
            drawArrow(g, 332, 188, 376, 188);
            g.setColor(MUTED);
            g.drawString("EDT boundary: Swing mutations return here", 116, 136);
        });
        canvas.setPreferredSize(new Dimension(500, 250));
        panel.add(canvas, "grow");
        panel.add(bulletList("Read the loop",
                "events enter on the EDT",
                "models own facts",
                "commands publish policy",
                "workers return small results",
                "scopes close relationships"), "grow, wrap");
        return panel;
    }

    static JPanel edtEventQueueBoundary() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("EventQueue and EDT ownership boundary",
                "[grow,fill][220!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());

            g.setColor(new Color(0xf0f4f8));
            g.fillRoundRect(18, 18, c.getWidth() - 36, c.getHeight() - 36, 18, 18);
            g.setColor(LINE);
            g.drawRoundRect(18, 18, c.getWidth() - 36, c.getHeight() - 36, 18, 18);

            Rectangle input = new Rectangle(36, 34, 124, 50);
            Rectangle timer = new Rectangle(36, 104, 124, 50);
            Rectangle worker = new Rectangle(36, 174, 124, 50);
            Rectangle queue = new Rectangle(202, 70, 128, 76);
            Rectangle edt = new Rectangle(366, 70, 94, 76);
            Rectangle graph = new Rectangle(254, 174, 206, 50);

            drawFlowBox(g, input, "native input", "mouse / key");
            drawFlowBox(g, timer, "timer event", "coalesced");
            drawFlowBox(g, worker, "worker result", "snapshot");
            drawFlowBox(g, queue, "EventQueue", "ordered dispatch");
            drawFlowBox(g, edt, "EDT", "single owner");
            drawFlowBox(g, graph, "live Swing graph", "components + models");

            g.setStroke(new java.awt.BasicStroke(2.2f));
            g.setColor(BLUE);
            drawArrow(g, input.x + input.width, input.y + 25, queue.x, queue.y + 18);
            drawArrow(g, timer.x + timer.width, timer.y + 25, queue.x, queue.y + 38);
            drawArrow(g, worker.x + worker.width, worker.y + 25, queue.x, queue.y + 58);
            drawArrow(g, queue.x + queue.width, queue.y + 38, edt.x, edt.y + 38);
            drawArrow(g, edt.x + 46, edt.y + edt.height, graph.x + graph.width - 46, graph.y);

            g.setColor(AMBER);
            java.awt.Stroke oldStroke = g.getStroke();
            g.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_BUTT,
                    java.awt.BasicStroke.JOIN_MITER, 10f, new float[] {6f, 5f}, 0f));
            g.drawLine(184, 28, 184, c.getHeight() - 28);
            g.setStroke(oldStroke);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
            g.drawString("handoff boundary", 196, 32);

            g.setColor(MUTED);
            g.setFont(g.getFont().deriveFont(11f));
            g.drawString("Nested loops may dispatch before the caller resumes.", 198, 238);
        });
        canvas.setPreferredSize(new Dimension(500, 270));
        panel.add(canvas, "grow");
        panel.add(bulletList("Review the boundary",
                "delivery, not transactions",
                "models live in the graph",
                "workers carry snapshots",
                "callbacks check lifecycle",
                "streams are batched"), "grow, wrap");
        return panel;
    }

    static JPanel componentPaintContract() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Custom component contract",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setColor(new Color(0xe9eef3));
            for (int x = 0; x < c.getWidth(); x += 32) {
                g.drawLine(x, 0, x, c.getHeight());
            }
            for (int y = 0; y < c.getHeight(); y += 32) {
                g.drawLine(0, y, c.getWidth(), y);
            }
            Rectangle preferred = new Rectangle(24, 20, 260, 150);
            Rectangle clip = new Rectangle(82, 52, 142, 76);
            g.setColor(new Color(0xc9def4));
            g.fill(preferred);
            g.setColor(BLUE);
            g.draw(preferred);
            g.setColor(new Color(0xe7b2b0));
            g.fill(clip);
            g.setColor(RED);
            g.draw(clip);
            g.setColor(INK);
            g.drawString("preferred size", 38, 44);
            g.drawString("current clip", 96, 92);
            g.setColor(new Color(0x385f71));
            g.fillOval(184, 104, 20, 20);
        });
        canvas.setPreferredSize(new Dimension(380, 210));
        panel.add(canvas, "grow");
        panel.add(bulletList("Contracts",
                "paint only inside clip",
                "preferred size is geometry",
                "input uses local coordinates",
                "Scrollable reports increments"), "grow, wrap");
        return panel;
    }

    static JPanel componentSizeNegotiation() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Component size negotiation",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setFont(g.getFont().deriveFont(12f));

            Rectangle parent = new Rectangle(34, 42, c.getWidth() - 88, c.getHeight() - 88);
            Rectangle minimum = new Rectangle(parent.x + 34, parent.y + 118, 92, 46);
            Rectangle preferred = new Rectangle(parent.x + 150, parent.y + 76, 178, 86);
            Rectangle assigned = new Rectangle(parent.x + 260, parent.y + 44, 250, 136);

            g.setColor(new Color(0xf4f6f8));
            g.fillRoundRect(parent.x, parent.y, parent.width, parent.height, 20, 20);
            g.setColor(LINE);
            g.setStroke(new java.awt.BasicStroke(1.4f));
            g.drawRoundRect(parent.x, parent.y, parent.width, parent.height, 20, 20);
            g.setColor(INK);
            g.drawString("parent container after insets", parent.x + 18, parent.y + 24);

            g.setColor(new Color(0xf8e6ce));
            g.fill(minimum);
            g.setColor(AMBER);
            g.draw(minimum);
            drawCentered(g, "minimum", minimum);

            g.setColor(new Color(0xd9e8f7));
            g.fill(preferred);
            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2f));
            g.draw(preferred);
            drawCentered(g, "preferred", preferred);

            g.setColor(new Color(0xdff0e4));
            g.fill(assigned);
            g.setColor(GREEN);
            g.setStroke(new java.awt.BasicStroke(2.4f));
            g.draw(assigned);
            drawCentered(g, "assigned bounds", assigned);

            g.setColor(MUTED);
            g.drawString("child reports", minimum.x, minimum.y - 18);
            drawDoubleArrow(g, minimum.x, minimum.y - 8, preferred.x + preferred.width, preferred.y - 8);
            g.setColor(RED);
            g.drawString("layout constraints and window size choose final geometry", parent.x + 130, parent.y + parent.height + 26);
        });
        canvas.setPreferredSize(new Dimension(470, 250));
        panel.add(canvas, "grow");
        panel.add(bulletList("Size facts",
                "minimum: useful lower bound",
                "preferred: natural size",
                "maximum: growth policy",
                "assigned: parent decision",
                "revalidate when facts change"), "grow, wrap");
        return panel;
    }

    static JPanel componentScrollableViewport() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Viewport extent and view world are different",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setFont(g.getFont().deriveFont(12f));

            Rectangle world = new Rectangle(40, 42, c.getWidth() - 122, c.getHeight() - 92);
            Rectangle viewport = new Rectangle(world.x + 128, world.y + 62, 230, 116);
            Point viewPosition = new Point(128, 62);

            g.setColor(new Color(0xf7f8fa));
            g.fill(world);
            g.setColor(new Color(0xdce3ea));
            for (int x = world.x; x <= world.x + world.width; x += 34) {
                g.drawLine(x, world.y, x, world.y + world.height);
            }
            for (int y = world.y; y <= world.y + world.height; y += 28) {
                g.drawLine(world.x, y, world.x + world.width, y);
            }
            g.setColor(LINE);
            g.draw(world);
            g.setColor(INK);
            g.drawString("view preferred size: the scrollable world", world.x + 12, world.y - 14);

            g.setColor(new Color(0xcfe5f7, false));
            Graphics2D overlay = (Graphics2D) g.create();
            try {
                overlay.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.36f));
                overlay.fill(viewport);
            } finally {
                overlay.dispose();
            }
            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2.6f));
            g.draw(viewport);
            g.setColor(INK);
            g.drawString("viewport extent", viewport.x + 52, viewport.y + 28);
            g.drawString("visibleRect in view coordinates", viewport.x + 30, viewport.y + 52);

            g.setColor(RED);
            g.fillOval(world.x + viewPosition.x - 4, world.y + viewPosition.y - 4, 8, 8);
            g.drawString("viewPosition = (" + viewPosition.x + ", " + viewPosition.y + ")",
                    world.x + viewPosition.x - 4, world.y + viewPosition.y - 14);

            g.setColor(AMBER);
            int barY = world.y + world.height + 18;
            g.drawLine(world.x, barY, world.x + world.width, barY);
            g.fillRoundRect(viewport.x, barY - 5, viewport.width, 10, 8, 8);
            int barX = world.x + world.width + 18;
            g.drawLine(barX, world.y, barX, world.y + world.height);
            g.fillRoundRect(barX - 5, viewport.y, 10, viewport.height, 8, 8);
        });
        canvas.setPreferredSize(new Dimension(470, 250));
        panel.add(canvas, "grow");
        panel.add(bulletList("Scroll facts",
                "viewport extent is visible",
                "view size is the world",
                "viewPosition moves the window",
                "visibleRect stays in view coords",
                "Scrollable controls increments"), "grow, wrap");
        return panel;
    }

    static JPanel migLayoutVocabulary() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("MigLayout vocabulary before notation",
                "[grow,fill][250!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setFont(g.getFont().deriveFont(12f));

            int x0 = 30;
            int y0 = 48;
            int labelWidth = 96;
            int editorWidth = Math.max(180, c.getWidth() - 300);
            int commandWidth = 78;
            int rowHeight = 52;
            int gap = 12;
            int x1 = x0 + labelWidth + gap;
            int x2 = x1 + editorWidth + gap;
            int y1 = y0 + rowHeight + gap;
            int y2 = y1 + rowHeight + gap;

            g.setColor(new Color(0xf0f4f8));
            g.fillRect(x0, y0, labelWidth, rowHeight);
            g.fillRect(x1, y0, editorWidth, rowHeight);
            g.fillRect(x2, y0, commandWidth, rowHeight);
            g.fillRect(x0, y1, labelWidth, rowHeight);
            g.fillRect(x1, y1, editorWidth, rowHeight);
            g.fillRect(x2, y1, commandWidth, rowHeight);
            g.setColor(new Color(0xeaf5ec));
            g.fillRect(x1, y2, editorWidth + gap + commandWidth, rowHeight);

            g.setColor(LINE);
            g.setStroke(new java.awt.BasicStroke(1.2f));
            for (Rectangle cell : List.of(
                    new Rectangle(x0, y0, labelWidth, rowHeight),
                    new Rectangle(x1, y0, editorWidth, rowHeight),
                    new Rectangle(x2, y0, commandWidth, rowHeight),
                    new Rectangle(x0, y1, labelWidth, rowHeight),
                    new Rectangle(x1, y1, editorWidth, rowHeight),
                    new Rectangle(x2, y1, commandWidth, rowHeight),
                    new Rectangle(x0, y2, labelWidth, rowHeight),
                    new Rectangle(x1, y2, editorWidth + gap + commandWidth, rowHeight))) {
                g.draw(cell);
            }

            g.setColor(INK);
            drawCentered(g, "label", new Rectangle(x0, y0, labelWidth, rowHeight));
            drawCentered(g, "field grows", new Rectangle(x1, y0, editorWidth, rowHeight));
            drawCentered(g, "button", new Rectangle(x2, y0, commandWidth, rowHeight));
            drawCentered(g, "cell", new Rectangle(x0, y1, labelWidth, rowHeight));
            drawCentered(g, "fill uses cell", new Rectangle(x1, y1, editorWidth, rowHeight));
            drawCentered(g, "wrap", new Rectangle(x2, y1, commandWidth, rowHeight));
            drawCentered(g, "span 2", new Rectangle(x1, y2, editorWidth + gap + commandWidth, rowHeight));

            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2.0f));
            drawDoubleArrow(g, x1 + 18, y0 - 18, x1 + editorWidth - 18, y0 - 18);
            g.drawString("[grow,fill] column", x1 + 62, y0 - 24);

            g.setColor(AMBER);
            g.drawLine(x0 + labelWidth, y0 - 6, x1, y0 - 6);
            g.drawLine(x0 + labelWidth, y0 - 10, x0 + labelWidth, y0 - 2);
            g.drawLine(x1, y0 - 10, x1, y0 - 2);
            g.drawString("gap", x0 + labelWidth + 2, y0 - 12);

            g.setColor(GREEN);
            drawDoubleArrow(g, x0, y2 + rowHeight + 20, x2 + commandWidth, y2 + rowHeight + 20);
            g.drawString("push gives unclaimed space to the chosen region", x0 + 58, y2 + rowHeight + 38);

            g.setColor(RED);
            int arrowX = x2 + commandWidth + 18;
            int arrowY = y1 + rowHeight / 2;
            g.drawLine(arrowX, arrowY, arrowX + 26, arrowY);
            g.drawLine(arrowX + 26, arrowY, arrowX + 26, y2 + rowHeight / 2);
            g.fillPolygon(new int[] {arrowX + 26, arrowX + 20, arrowX + 32},
                    new int[] {y2 + rowHeight / 2, y2 + rowHeight / 2 - 8, y2 + rowHeight / 2 - 8},
                    3);
            g.drawString("next row", arrowX - 4, y2 + rowHeight / 2 + 18);
        });
        canvas.setPreferredSize(new Dimension(460, 250));
        panel.add(canvas, "grow");
        panel.add(bulletList("Read the grid",
                "cell: starting slot",
                "span: occupies several slots",
                "grow: receives extra space",
                "fill: uses received space",
                "gap: visual grammar",
                "wrap: next row",
                "push: claims slack"), "grow, wrap");
        return panel;
    }

    static JPanel migLayoutDenseScreen() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("MigLayout dense search screen",
                "[][grow,fill][][grow,fill][]", "[][][][grow]");
        panel.add(new JLabel("Customer"));
        panel.add(new JTextField("lovelace"), "growx");
        panel.add(new JLabel("State"));
        panel.add(new JComboBox<>(new String[] {"Any", "Active", "Review"}), "growx");
        panel.add(new JButton("Search"), "wrap");
        panel.add(new JLabel("Opened after"));
        panel.add(new JTextField("2026-01-01"), "growx");
        panel.add(new JLabel("Owner"));
        panel.add(new JTextField("Operations"), "growx");
        panel.add(new JButton("Reset"), "wrap");
        panel.add(note("Layout policy",
                "Related fields use compact gaps; command buttons stay aligned; the result table takes remaining height."),
                "span 5, growx, wrap");
        panel.add(new JScrollPane(sampleTable()), "span 5, grow");
        return panel;
    }

    static JPanel practiceListenerSprawl() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("From listener sprawl to owned state",
                "[grow,fill][grow,fill]", "[][grow]");
        panel.add(codeCard("Local listener pile",
                "document listener updates label\nbutton listener reads field\ntable listener changes command\nfocus listener rewrites tooltip"),
                "grow");
        panel.add(codeCard("Named Corusco facts",
                "WritableValue<String> name\nDerivedValue<Boolean> canSave\nCommand save\nBindingScope owns cleanup"),
                "grow, wrap");
        panel.add(note("Review move",
                "The code becomes longer in vocabulary and shorter in hidden assumptions."),
                "span 2, growx");
        return panel;
    }

    static JPanel paintingDirtyRegions() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Dirty regions are promises",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            for (int x = 20; x < c.getWidth(); x += 42) {
                for (int y = 20; y < c.getHeight(); y += 34) {
                    g.setColor(new Color(0xeef2f5));
                    g.fillRect(x, y, 28, 20);
                    g.setColor(new Color(0xd2d9df));
                    g.drawRect(x, y, 28, 20);
                }
            }
            Rectangle old = new Rectangle(62, 56, 116, 54);
            Rectangle now = new Rectangle(150, 94, 116, 54);
            Rectangle union = old.union(now);
            g.setColor(new Color(0xf6d6d5));
            g.fill(union);
            g.setColor(RED);
            g.draw(union);
            g.setColor(BLUE);
            g.draw(old);
            g.draw(now);
            g.setColor(INK);
            g.drawString("old bounds", old.x + 8, old.y + 22);
            g.drawString("new bounds", now.x + 8, now.y + 22);
            g.drawString("repaint union", union.x + 8, union.y + union.height - 10);
        });
        canvas.setPreferredSize(new Dimension(400, 230));
        panel.add(canvas, "grow");
        panel.add(bulletList("Audit",
                "semantic mutation first",
                "old visual bounds",
                "new visual bounds",
                "repaint the union"), "grow, wrap");
        return panel;
    }

    static JPanel paintingOpacity() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Opacity must be truthful",
                "[grow,fill][grow,fill]", "[][grow]");
        panel.add(opacityPanel("Truthful opaque", true), "grow");
        panel.add(opacityPanel("Transparent decorator", false), "grow, wrap");
        panel.add(note("Painting rule",
                "An opaque component must fill every pixel in its bounds; a non-opaque component lets ancestors show through."),
                "span 2, growx");
        return panel;
    }

    static JPanel paintingCompositeSrcOver() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("AlphaComposite.SRC_OVER keeps the destination visible",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            drawCheckerboard(g, 0, 0, c.getWidth(), c.getHeight(), 18);
            g.setColor(new Color(0xd9e8f7));
            g.fillRoundRect(46, 42, 250, 138, 18, 18);
            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2.4f));
            g.drawRoundRect(46, 42, 250, 138, 18, 18);
            g.setColor(INK);
            g.drawString("destination already drawn", 70, 74);

            Graphics2D overlay = (Graphics2D) g.create();
            try {
                overlay.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.46f));
                overlay.setColor(RED);
                overlay.fill(new Ellipse2D.Double(168, 80, 170, 110));
            } finally {
                overlay.dispose();
            }
            g.setColor(RED);
            g.draw(new Ellipse2D.Double(168, 80, 170, 110));
            g.setColor(INK);
            g.drawString("source over at alpha .46", 208, 202);
        });
        canvas.setPreferredSize(new Dimension(470, 230));
        panel.add(canvas, "grow");
        panel.add(bulletList("SrcOver rule",
                "draw destination first",
                "apply alpha to source",
                "destination still contributes",
                "restore composite after use",
                "dirty bounds include both"), "grow, wrap");
        return panel;
    }

    static JPanel paintingCompositeGroupOpacity() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Object alpha and group alpha are different",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            drawCheckerboard(g, 18, 34, 196, 166, 16);
            drawCheckerboard(g, 256, 34, 196, 166, 16);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            g.setColor(INK);
            g.drawString("each object alpha .45", 34, 26);
            g.drawString("whole group alpha .45", 276, 26);

            Graphics2D left = (Graphics2D) g.create();
            try {
                left.translate(18, 34);
                left.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
                paintCompositeFlower(left);
            } finally {
                left.dispose();
            }

            BufferedImage group = new BufferedImage(196, 166, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gg = group.createGraphics();
            try {
                gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                paintCompositeFlower(gg);
            } finally {
                gg.dispose();
            }
            Graphics2D right = (Graphics2D) g.create();
            try {
                right.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
                right.drawImage(group, 256, 34, null);
            } finally {
                right.dispose();
            }
            g.setColor(RED);
            g.drawString("overlaps accumulate", 48, 218);
            g.setColor(BLUE);
            g.drawString("internal overlaps stay opaque", 286, 218);
        });
        canvas.setPreferredSize(new Dimension(470, 260));
        panel.add(canvas, "grow");
        panel.add(bulletList("Group opacity",
                "render group offscreen",
                "composite group once",
                "avoid darker overlaps",
                "mind image allocation",
                "cache only with invalidation"), "grow, wrap");
        return panel;
    }

    static JPanel paintingCompositeClearMask() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("CLEAR belongs on an offscreen layer",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setColor(new Color(0xe8edf3));
            g.fillRoundRect(42, 42, 310, 154, 18, 18);
            g.setColor(BLUE);
            g.fillRoundRect(74, 80, 132, 42, 10, 10);
            g.setColor(Color.WHITE);
            g.drawString("Apply", 120, 106);
            g.setColor(INK);
            g.drawString("form behind overlay", 80, 154);

            BufferedImage veil = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D vg = veil.createGraphics();
            try {
                vg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                vg.setComposite(AlphaComposite.SrcOver);
                vg.setColor(new Color(0x202833));
                vg.fillRect(0, 0, c.getWidth(), c.getHeight());
                vg.setComposite(AlphaComposite.Clear);
                vg.fill(new Ellipse2D.Double(58, 66, 168, 86));
            } finally {
                vg.dispose();
            }

            Graphics2D over = (Graphics2D) g.create();
            try {
                over.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.62f));
                over.drawImage(veil, 0, 0, null);
            } finally {
                over.dispose();
            }
            g.setColor(AMBER);
            g.setStroke(new java.awt.BasicStroke(2.4f));
            g.draw(new Ellipse2D.Double(58, 66, 168, 86));
            g.setColor(INK);
            g.drawString("punched with CLEAR on the layer", 222, 74);
        });
        canvas.setPreferredSize(new Dimension(470, 230));
        panel.add(canvas, "grow");
        panel.add(bulletList("Cutout rule",
                "never clear live ancestors",
                "clear an ARGB layer",
                "draw layer back with SrcOver",
                "repaint full visual effect",
                "use for spotlights and masks"), "grow, wrap");
        return panel;
    }

    static JPanel paintingCompositeRuleComparison() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Composite rules answer different coverage questions",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            drawCompositeRule(g, "SrcOver", AlphaComposite.SRC_OVER, 22, 26);
            drawCompositeRule(g, "SrcIn", AlphaComposite.SRC_IN, 248, 26);
            drawCompositeRule(g, "SrcOut", AlphaComposite.SRC_OUT, 22, 154);
            drawCompositeRule(g, "Xor", AlphaComposite.XOR, 248, 154);
        });
        canvas.setPreferredSize(new Dimension(470, 300));
        panel.add(canvas, "grow");
        panel.add(bulletList("Choose deliberately",
                "SrcOver for normal overlays",
                "SrcIn for clipped source",
                "SrcOut for outside-only marks",
                "Xor for separated coverage",
                "test with transparent pixels"), "grow, wrap");
        return panel;
    }

    static JPanel java2dVectorPrimitives() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Java2D primitives survive scaling",
                "[grow,fill][220!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setStroke(new java.awt.BasicStroke(2.0f));
            g.setColor(BLUE);
            g.draw(new Line2D.Double(24, 170, 120, 36));
            g.setColor(GREEN);
            g.fillRoundRect(138, 42, 132, 92, 18, 18);
            g.setColor(INK);
            g.drawString("shape", 176, 92);
            g.setColor(AMBER);
            g.fillOval(292, 62, 86, 86);
            g.setColor(INK);
            g.drawString("text follows font metrics", 118, 190);
        });
        canvas.setPreferredSize(new Dimension(420, 230));
        panel.add(canvas, "grow");
        panel.add(bulletList("Prefer",
                "shapes over bitmap hacks",
                "font metrics over constants",
                "scale-aware strokes",
                "theme colors"), "grow, wrap");
        return panel;
    }

    static JPanel java2dScaleGrid() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("HiDPI grid check",
                "[grow,fill][grow,fill]", "[][grow]");
        panel.add(scaleGrid("Logical 1x", 1), "grow");
        panel.add(scaleGrid("Scaled 2x preview", 2), "grow, wrap");
        panel.add(note("Review point",
                "Draw from logical coordinates and let Graphics2D carry the device transform."),
                "span 2, growx");
        return panel;
    }

    static JPanel java2dTextBaseline() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Text metrics: baseline, ink, and advance",
                "[grow,fill][220!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());

            Font baseFont = UIManager.getFont("Label.font");
            if (baseFont == null) {
                baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
            }
            Font font = baseFont.deriveFont(Font.BOLD, 42f);
            TextLayout layout = new TextLayout("Swing", font, g.getFontRenderContext());
            float x = 70f;
            float baseline = 155f;
            float advance = layout.getAdvance();
            Rectangle2D visualBounds = layout.getBounds();
            Shape ink = AffineTransform.getTranslateInstance(x, baseline).createTransformedShape(visualBounds);

            g.setColor(new Color(0xe9f1fa));
            g.fill(new Rectangle2D.Double(x, baseline - layout.getAscent(), advance,
                    layout.getAscent() + layout.getDescent()));
            g.setColor(new Color(0xb8c2cc));
            g.draw(new Rectangle2D.Double(x, baseline - layout.getAscent(), advance,
                    layout.getAscent() + layout.getDescent()));

            g.setColor(new Color(0xfff0d9));
            g.fill(ink);
            g.setColor(AMBER);
            g.draw(ink);

            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2f));
            g.draw(new Line2D.Double(42, baseline, c.getWidth() - 34, baseline));
            g.setFont(baseFont.deriveFont(Font.BOLD, 11f));
            g.drawString("baseline", 42, baseline - 6);

            g.setColor(GREEN);
            drawDoubleArrow(g, Math.round(x), Math.round(baseline + 56),
                    Math.round(x + advance), Math.round(baseline + 56));
            g.drawString("advance", Math.round(x + advance / 2f - 24f), Math.round(baseline + 74f));

            g.setColor(MUTED);
            g.drawString("logical bounds", Math.round(x + advance + 18f), Math.round(baseline - layout.getAscent() + 16f));
            g.drawString("ink bounds", Math.round(x + advance + 18f), Math.round(baseline - 20f));

            g.setColor(INK);
            layout.draw(g, x, baseline);
        });
        canvas.setPreferredSize(new Dimension(500, 260));
        panel.add(canvas, "grow");
        panel.add(bulletList("Measure the question",
                "baseline aligns text",
                "advance positions next text",
                "ink bounds repaint pixels",
                "FRC depends on hints",
                "cache by font and scale"), "grow, wrap");
        return panel;
    }

    static JPanel tableCoordinateSystems() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("JTable coordinate systems",
                "[grow,fill][260!]", "[][grow]");
        JTable table = sampleTable();
        TableRowSorter<?> sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        table.setRowSorter(sorter);
        table.setRowSelectionInterval(1, 1);
        panel.add(new JScrollPane(table), "grow");
        panel.add(bulletList("Selected row",
                "viewRow = 1",
                "modelRow = convertRowIndexToModel(viewRow)",
                "row identity = CustomerId",
                "column identity survives movement"), "grow, wrap");
        return panel;
    }

    static JPanel tableRefreshIdentity() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Refresh preserves user context by identity",
                "[grow,fill][235!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle capture = new Rectangle(26, 36, 124, 56);
            Rectangle load = new Rectangle(190, 36, 124, 56);
            Rectangle diff = new Rectangle(354, 36, 124, 56);
            Rectangle events = new Rectangle(354, 150, 124, 56);
            Rectangle restore = new Rectangle(190, 150, 124, 56);
            Rectangle repaint = new Rectangle(26, 150, 124, 56);

            drawFlowBox(g, capture, "capture", "ids + viewport");
            drawFlowBox(g, load, "background", "new rows");
            drawFlowBox(g, diff, "diff by id", "insert/update");
            drawFlowBox(g, events, "table events", "precise ranges");
            drawFlowBox(g, restore, "restore", "selection by id");
            drawFlowBox(g, repaint, "viewport", "small repaint");

            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2.2f));
            drawArrow(g, capture.x + capture.width, capture.y + 28, load.x, load.y + 28);
            drawArrow(g, load.x + load.width, load.y + 28, diff.x, diff.y + 28);
            drawArrow(g, diff.x + 62, diff.y + diff.height, events.x + 62, events.y);
            drawArrow(g, events.x, events.y + 28, restore.x + restore.width, restore.y + 28);
            drawArrow(g, restore.x, restore.y + 28, repaint.x + repaint.width, repaint.y + 28);
            g.setColor(MUTED);
            g.drawString("view rows are temporary; ids survive sorting, filtering, and reloads", 32, 250);
        });
        canvas.setPreferredSize(new Dimension(500, 270));
        panel.add(canvas, "grow");
        panel.add(bulletList("Refresh audit",
                "capture before mutation",
                "load away from EDT",
                "apply one transaction",
                "fire precise events",
                "restore by identity"), "grow, wrap");
        return panel;
    }

    static JPanel treePathIdentity() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("JTree paths preserve navigation intent",
                "[grow,fill][260!]", "[][grow]");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Workspace");
        DefaultMutableTreeNode customers = new DefaultMutableTreeNode("Customers");
        DefaultMutableTreeNode orders = new DefaultMutableTreeNode("Orders");
        DefaultMutableTreeNode loading = new DefaultMutableTreeNode("Invoices (loading...)");
        customers.add(new DefaultMutableTreeNode("Ada Lovelace"));
        customers.add(new DefaultMutableTreeNode("Grace Hopper"));
        customers.add(new DefaultMutableTreeNode("Katherine Johnson"));
        orders.add(new DefaultMutableTreeNode("2026-Q1"));
        orders.add(new DefaultMutableTreeNode("2026-Q2"));
        root.add(customers);
        root.add(orders);
        root.add(loading);
        JTree tree = new JTree(root);
        tree.setRowHeight(24);
        tree.setVisibleRowCount(7);
        tree.expandRow(0);
        tree.expandRow(1);
        tree.expandRow(5);
        tree.setSelectionPath(new TreePath(new Object[] {
                root, customers, customers.getChildAt(1)
        }));
        panel.add(new JScrollPane(tree), "grow");
        panel.add(bulletList("Review",
                "visible row is temporary",
                "TreePath is current route",
                "NodeId restores expansion",
                "load from expansion events"), "grow, wrap");
        return panel;
    }

    static JPanel actionsCommandSurfaces() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("One command, several Swing surfaces",
                "[grow,fill][grow,fill][grow,fill]", "[][grow][]");
        panel.add(commandSurface("Toolbar", "Save", "icon + tooltip"), "grow");
        panel.add(commandSurface("Menu item", "Save", "text + shortcut"), "grow");
        panel.add(commandSurface("Key binding", "Ctrl+S", "same command"), "grow, wrap");
        panel.add(note("Command rule",
                "The command owns identity, enabled state, and handler; each Swing surface is an adapter."),
                "span 3, growx");
        return panel;
    }

    static JPanel actionsFocusTraversal() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Focus order is a user workflow",
                "[][grow,fill]", "[][][][]");
        addFocusRow(panel, "1", "Search field", true);
        addFocusRow(panel, "2", "Result table", false);
        addFocusRow(panel, "3", "Detail form", false);
        addFocusRow(panel, "4", "Save command", false);
        return panel;
    }

    static JPanel actionsCommandRouting() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Command routing resolves context before execution",
                "[grow,fill][230!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());

            Rectangle focus = new Rectangle(24, 34, 118, 54);
            Rectangle provider = new Rectangle(170, 34, 128, 54);
            Rectangle registry = new Rectangle(326, 34, 124, 54);
            Rectangle command = new Rectangle(170, 150, 128, 54);
            Rectangle presenter = new Rectangle(326, 150, 124, 54);

            drawFlowBox(g, focus, "focus owner", "editor/table");
            drawFlowBox(g, provider, "context", "active provider");
            drawFlowBox(g, registry, "registry", "command id");
            drawFlowBox(g, command, "command", "state + handler");
            drawFlowBox(g, presenter, "presenters", "menu/key/button");

            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2.2f));
            drawArrow(g, focus.x + focus.width, focus.y + 27, provider.x, provider.y + 27);
            drawArrow(g, provider.x + provider.width, provider.y + 27, registry.x, registry.y + 27);
            drawArrow(g, registry.x + 68, registry.y + registry.height, command.x + 68, command.y);
            drawArrow(g, command.x + command.width, command.y + 27, presenter.x, presenter.y + 27);

            g.setColor(GREEN);
            drawArrow(g, presenter.x + 68, presenter.y, registry.x + 68, registry.y + registry.height);
            g.setColor(MUTED);
            g.drawString("enabled state and disabled reason flow back to every surface", 46, 248);
        });
        canvas.setPreferredSize(new Dimension(460, 260));
        panel.add(canvas, "grow");
        panel.add(bulletList("Routing checks",
                "focus is not semantic owner",
                "provider lifetime is scoped",
                "command id is stable",
                "presenters share state",
                "tests query the command"), "grow, wrap");
        return panel;
    }

    static JPanel layersBusyOverlay() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Busy overlay as input policy",
                "[grow,fill][220!]", "[][grow]");
        JPanel form = compactForm();
        JLayer<JComponent> layer = new JLayer<>(form);
        BusyOverlayLayerUI ui = new BusyOverlayLayerUI(new Color(0x202833), 0.34f);
        layer.setUI(ui);
        ui.setBusy(layer, true);
        panel.add(layer, "grow");
        panel.add(bulletList("While busy",
                "paint cover",
                "wait cursor",
                "consume incompatible input",
                "status remains visible"), "grow, wrap");
        return panel;
    }

    static JPanel layersValidationOverlay() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Validation overlay on a form subtree",
                "[grow,fill][220!]", "[][grow]");
        JPanel form = compactForm();
        ((JComponent) form.getComponent(1)).putClientProperty("validation.error", Boolean.TRUE);
        JLayer<JComponent> layer = new JLayer<>(form);
        layer.setUI(new ValidationMarksLayerUI());
        panel.add(layer, "grow");
        panel.add(bulletList("Overlay owns",
                "coordinate conversion",
                "visual mark policy",
                "local repaint",
                "no field subclassing"), "grow, wrap");
        return panel;
    }

    static JPanel layersScopeCoordinate() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Overlay scope decides coordinates",
                "[grow,fill][230!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            Rectangle root = new Rectangle(28, 22, c.getWidth() - 56, c.getHeight() - 44);
            Rectangle form = new Rectangle(58, 58, 210, 132);
            Rectangle target = new Rectangle(96, 104, 92, 32);
            Rectangle popup = new Rectangle(304, 64, 120, 74);
            g.setColor(new Color(0xe9eef5));
            g.fillRoundRect(root.x, root.y, root.width, root.height, 18, 18);
            g.setColor(LINE);
            g.drawRoundRect(root.x, root.y, root.width, root.height, 18, 18);
            g.setColor(new Color(0xd8e8f7));
            g.fillRoundRect(form.x, form.y, form.width, form.height, 14, 14);
            g.setColor(BLUE);
            g.drawRoundRect(form.x, form.y, form.width, form.height, 14, 14);
            g.setColor(new Color(0xf7efdd));
            g.fillRect(target.x, target.y, target.width, target.height);
            g.setColor(AMBER);
            g.drawRect(target.x, target.y, target.width, target.height);
            g.setColor(new Color(0xebf2e7));
            g.fillRoundRect(popup.x, popup.y, popup.width, popup.height, 12, 12);
            g.setColor(GREEN);
            g.drawRoundRect(popup.x, popup.y, popup.width, popup.height, 12, 12);
            g.setColor(INK);
            g.drawString("root / glass pane", root.x + 16, root.y + 24);
            g.drawString("JLayer subtree", form.x + 16, form.y + 26);
            g.drawString("target", target.x + 22, target.y + 21);
            g.drawString("popup", popup.x + 34, popup.y + 42);
            g.setColor(RED);
            g.setStroke(new java.awt.BasicStroke(2.2f));
            drawArrow(g, target.x + target.width, target.y + 8, root.x + root.width - 64, root.y + root.height - 44);
            g.drawString("convert bounds", root.x + root.width - 148, root.y + root.height - 30);
        });
        canvas.setPreferredSize(new Dimension(500, 245));
        panel.add(canvas, "grow");
        panel.add(bulletList("Choose by scope",
                "JLayer: subtree",
                "glass: root pane",
                "layered pane: live child",
                "popup: separate surface",
                "convert coordinates"), "grow, wrap");
        return panel;
    }

    static JPanel lafComponents() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Look-and-Feel supplies component vocabulary",
                "[grow,fill][grow,fill]", "[][grow][]");
        panel.add(lafSample("Enabled", true), "grow");
        panel.add(lafSample("Disabled", false), "grow, wrap");
        panel.add(colorSwatches(), "span 2, growx");
        return panel;
    }

    static JPanel lafDensity() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Operational density",
                "[grow,fill][320!]", "[][grow]");
        panel.add(new JScrollPane(sampleTable()), "grow");
        panel.add(compactForm(), "grow, wrap");
        panel.add(note("LAF review",
                "Spacing, focus rings, disabled state, and table metrics must work together under the chosen Look-and-Feel."),
                "span 2, growx");
        return panel;
    }

    static JPanel lafStateMatrix() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Visual state has several owners",
                "[grow,fill][230!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = 22;
            int y = 38;
            int rowHeight = 43;
            int[] widths = {86, 112, 112, 116};
            String[] headers = {"state", "model fact", "UI default", "delegate output"};
            String[][] rows = {
                    {"enabled", "command can run", "Button.foreground", "normal contrast"},
                    {"focused", "focus owner", "Component.focus", "visible ring"},
                    {"selected", "selection model", "Table.selection", "theme colors"},
                    {"invalid", "ProblemSet", "App.error", "border + help"}
            };
            g.setFont(g.getFont().deriveFont(Font.BOLD, 12f));
            int xx = x;
            for (int i = 0; i < headers.length; i++) {
                g.setColor(INK);
                g.drawString(headers[i], xx + 8, y - 12);
                xx += widths[i] + 10;
            }
            g.setFont(g.getFont().deriveFont(11f));
            for (int r = 0; r < rows.length; r++) {
                xx = x;
                for (int col = 0; col < rows[r].length; col++) {
                    Rectangle cell = new Rectangle(xx, y + r * rowHeight, widths[col], 31);
                    Color fill = col == 0 ? new Color(0xf4f6f8)
                            : col == 1 ? new Color(0xdff0e4)
                            : col == 2 ? new Color(0xd9e8f7)
                            : new Color(0xf8e6ce);
                    g.setColor(fill);
                    g.fillRoundRect(cell.x, cell.y, cell.width, cell.height, 10, 10);
                    g.setColor(LINE);
                    g.drawRoundRect(cell.x, cell.y, cell.width, cell.height, 10, 10);
                    g.setColor(col == 0 ? INK : MUTED);
                    g.drawString(rows[r][col], cell.x + 8, cell.y + 20);
                    xx += widths[col] + 10;
                }
            }
            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2f));
            drawArrow(g, 130, 230, 230, 230);
            drawArrow(g, 258, 230, 358, 230);
            drawArrow(g, 386, 230, 478, 230);
            g.setColor(MUTED);
            g.drawString("facts become hints; hints meet defaults; delegates paint states", 30, 258);
        });
        canvas.setPreferredSize(new Dimension(500, 270));
        panel.add(canvas, "grow");
        panel.add(bulletList("Review owners",
                "model owns truth",
                "properties carry hints",
                "defaults carry policy",
                "delegate paints state",
                "tests capture states"), "grow, wrap");
        return panel;
    }

    static JPanel textDocumentModel() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Text component and document model",
                "[grow,fill][240!]", "[][grow]");
        JTextArea area = new JTextArea("Ada Lovelace\nGrace Hopper\nKatherine Johnson");
        area.setRows(7);
        panel.add(new JScrollPane(area), "grow");
        panel.add(bulletList("Document facts",
                "text lives in Document",
                "edits fire document events",
                "view maps model positions",
                "undo listens to edits"), "grow, wrap");
        return panel;
    }

    static JPanel textValidationState() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Raw text, parse state, and feedback",
                "[][grow,fill]", "[][][][]");
        panel.add(new JLabel("Credit limit"));
        JTextField field = new JTextField("12x.00");
        field.setBorder(BorderFactory.createLineBorder(RED, 2));
        panel.add(field, "growx, wrap");
        panel.add(new JLabel("Raw text"));
        panel.add(new JLabel("12x.00"), "wrap");
        panel.add(new JLabel("Parse state"));
        panel.add(tag("Failed: expected decimal", RED), "wrap");
        panel.add(new JLabel("Semantic value"));
        panel.add(new JLabel("last valid value remains 120.00"), "wrap");
        return panel;
    }

    static JPanel textMutationPipeline() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Text mutation has several owners",
                "[grow,fill][250!]", "[][grow]");
        JTextArea area = new JTextArea("""
                customer.name = "Ada Lovelace"
                credit.limit = "12x.00"
                note = "Call before renewal"
                """);
        area.setRows(6);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setCaretPosition(38);
        area.select(17, 29);
        try {
            area.getHighlighter().addHighlight(49, 55,
                    new DefaultHighlighter.DefaultHighlightPainter(new Color(0xffef9f)));
        } catch (javax.swing.text.BadLocationException e) {
            throw new IllegalStateException(e);
        }
        JPanel left = new JPanel(new MigLayout("fill, insets 0", "[grow,fill]", "[grow]"));
        left.add(new JScrollPane(area), "grow");

        PaintPanel flow = new PaintPanel((g, c) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            Rectangle filter = new Rectangle(22, 24, 150, 44);
            Rectangle document = new Rectangle(22, 98, 150, 44);
            Rectangle observers = new Rectangle(22, 172, 150, 44);
            drawFlowBox(g, filter, "DocumentFilter", "before mutation");
            drawFlowBox(g, document, "Document", "text + positions");
            drawFlowBox(g, observers, "observers", "events after");
            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2f));
            drawArrow(g, 97, 68, 97, 98);
            drawArrow(g, 97, 142, 97, 172);
            g.setColor(GREEN);
            g.drawString("Highlighter: transient ranges", 10, 242);
            g.setColor(AMBER);
            g.drawString("UndoManager: user edits", 10, 262);
        });
        flow.setPreferredSize(new Dimension(200, 260));

        panel.add(left, "grow");
        panel.add(flow, "grow, wrap");
        panel.add(note("Review rule",
                "Filters run before mutation; listeners, highlighters, undo, and field models observe named results."),
                "span 2, growx");
        return panel;
    }

    static JPanel geomShapeVocabulary() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("java.awt.geom shape vocabulary",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setStroke(new java.awt.BasicStroke(2.2f));
            Rectangle2D rect = new Rectangle2D.Double(34, 38, 110, 76);
            Ellipse2D ellipse = new Ellipse2D.Double(88, 92, 124, 74);
            Path2D path = new Path2D.Double();
            path.moveTo(250, 48);
            path.curveTo(310, 20, 342, 94, 286, 126);
            path.lineTo(358, 162);
            Shape tiltedPath = AffineTransform.getRotateInstance(
                    Math.toRadians(-10), 300, 92).createTransformedShape(path);
            Area overlap = new Area(rect);
            overlap.intersect(new Area(ellipse));
            g.setColor(new Color(0xd9e8f7));
            g.fill(rect);
            g.setColor(BLUE);
            g.draw(rect);
            g.setColor(new Color(0xf4ded5));
            g.fill(ellipse);
            g.setColor(AMBER);
            g.draw(ellipse);
            g.setColor(new Color(0x7aa664));
            g.fill(overlap);
            g.setColor(new Color(0xd7becf));
            g.draw(tiltedPath);
            g.setColor(RED);
            g.draw(path);
            g.setColor(INK);
            g.drawString("Rectangle2D", 42, 32);
            g.drawString("Ellipse2D", 114, 184);
            g.drawString("Path2D + transform", 248, 38);
        });
        canvas.setPreferredSize(new Dimension(430, 230));
        panel.add(canvas, "grow");
        panel.add(bulletList("Shape facts",
                "bounds are cheap filters",
                "contains answers hit tests",
                "intersects guides repaint",
                "PathIterator exposes segments",
                "Area performs boolean algebra"), "grow, wrap");
        return panel;
    }

    static JPanel geomTransformAlgebra() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Affine transforms are composed maps",
                "[grow,fill][70!][grow,fill][70!][grow,fill]", "[][grow][]");
        panel.add(behaviorTile("Model", "meters, page, diagram"), "grow");
        panel.add(arrowPanel("M->W"), "grow");
        panel.add(behaviorTile("View", "pan, zoom, rotation"), "grow");
        panel.add(arrowPanel("W->D"), "grow");
        panel.add(behaviorTile("Device", "HiDPI pixels"), "grow, wrap");
        panel.add(note("Algebra rule",
                "Use the same transform chain for painting, hit testing, selection, tooltips, printing, and screenshots."),
                "span 5, growx");
        return panel;
    }

    static JPanel geomAffineVariants() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Affine transformations preserve lines, not all visual facts",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            Shape local = localHouseShape();
            drawTransformCell(g, "translate", 22, 24,
                    AffineTransform.getTranslateInstance(36, 18), local, BLUE);
            AffineTransform scale = new AffineTransform();
            scale.translate(188, 44);
            scale.scale(1.34, 0.72);
            drawTransformCell(g, "scale", 246, 24, scale, local, GREEN);
            AffineTransform rotate = new AffineTransform();
            rotate.translate(82, 166);
            rotate.rotate(Math.toRadians(28), 42, 42);
            drawTransformCell(g, "rotate", 22, 152, rotate, local, AMBER);
            AffineTransform shear = new AffineTransform();
            shear.translate(256, 166);
            shear.shear(0.42, 0.0);
            drawTransformCell(g, "shear", 246, 152, shear, local, RED);
        });
        canvas.setPreferredSize(new Dimension(470, 300));
        panel.add(canvas, "grow");
        panel.add(bulletList("Review the space",
                "translation moves origins",
                "scale changes units",
                "rotation changes axes",
                "shear keeps parallelism",
                "inverse maps input back"), "grow, wrap");
        return panel;
    }

    static JPanel geomTransformOrder() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Composition order changes the promise",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            Shape arrow = localArrowShape();
            AffineTransform first = new AffineTransform();
            first.translate(118, 116);
            first.rotate(Math.toRadians(34));
            AffineTransform second = new AffineTransform();
            second.rotate(Math.toRadians(34));
            second.translate(118, 116);
            drawOrderScene(g, "translate, then rotate", 18, 32, first, arrow, BLUE);
            drawOrderScene(g, "rotate, then translate", 244, 32, second, arrow, RED);
        });
        canvas.setPreferredSize(new Dimension(470, 230));
        panel.add(canvas, "grow");
        panel.add(bulletList("Test with points",
                "name source and target spaces",
                "map two known points",
                "store inverse with forward",
                "use deltaTransform for drags",
                "keep graphics transform scoped"), "grow, wrap");
        return panel;
    }

    static JPanel geomStrokedShape() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("createStrokedShape turns a line into hit geometry",
                "[grow,fill][250!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            Path2D route = new Path2D.Double();
            route.moveTo(42, 144);
            route.curveTo(110, 36, 210, 56, 250, 126);
            route.curveTo(288, 190, 356, 164, 386, 82);
            Shape hit = new java.awt.BasicStroke(22f,
                    java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND).createStrokedShape(route);
            g.setColor(new Color(0xdbeadf));
            g.fill(hit);
            g.setColor(new Color(0x8ebf9b));
            g.draw(hit);
            g.setStroke(new java.awt.BasicStroke(3f,
                    java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            g.setColor(BLUE);
            g.draw(route);
            g.setColor(RED);
            g.fill(new Ellipse2D.Double(196, 83, 12, 12));
            g.setColor(INK);
            g.drawString("visual centerline", 48, 32);
            g.drawString("wide hit shape", 248, 190);
        });
        canvas.setPreferredSize(new Dimension(430, 230));
        panel.add(canvas, "grow");
        panel.add(bulletList("Uses",
                "fat finger hit testing",
                "road or river selection",
                "lasso tolerance",
                "outline-to-fill conversion",
                "painted selection halos"), "grow, wrap");
        return panel;
    }

    static JPanel geomAreaOperations() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Area operations are exact shape arithmetic",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            drawAreaOperation(g, "add", 24, 24, "union");
            drawAreaOperation(g, "subtract", 248, 24, "A - B");
            drawAreaOperation(g, "intersect", 24, 154, "overlap");
            drawAreaOperation(g, "xor", 248, 154, "exclusive");
        });
        canvas.setPreferredSize(new Dimension(470, 300));
        panel.add(canvas, "grow");
        panel.add(bulletList("Use after filtering",
                "selection regions",
                "vector subtraction",
                "polygon overlap",
                "clip construction",
                "export conversion"), "grow, wrap");
        return panel;
    }

    static JPanel geomSpatialIndex() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Clip first, then query visible geometry",
                "[grow,fill][250!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setColor(new Color(0xe4e9ef));
            for (int x = 28; x < c.getWidth(); x += 54) {
                g.drawLine(x, 20, x, c.getHeight() - 24);
            }
            for (int y = 28; y < c.getHeight(); y += 42) {
                g.drawLine(20, y, c.getWidth() - 20, y);
            }
            Rectangle2D clip = new Rectangle2D.Double(108, 62, 178, 112);
            g.setColor(new Color(0xd9e8f7));
            g.fill(clip);
            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2.4f));
            g.draw(clip);
            for (int i = 0; i < 24; i++) {
                double x = 34 + (i * 47) % 344;
                double y = 38 + (i * 31) % 150;
                Shape item = new Ellipse2D.Double(x, y, 16, 16);
                g.setColor(item.intersects(clip.getBounds2D()) ? GREEN : MUTED);
                g.fill(item);
            }
            g.setColor(INK);
            g.drawString("viewport clip", 126, 58);
        });
        canvas.setPreferredSize(new Dimension(430, 230));
        panel.add(canvas, "grow");
        panel.add(bulletList("Performance path",
                "transform clip to model space",
                "query spatial index",
                "paint only candidates",
                "reuse paths where safe",
                "measure allocation in JFR"), "grow, wrap");
        return panel;
    }

    static JPanel geomFontMetrics() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Font metrics are geometry, not decoration",
                "[grow,fill][250!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            Font font = new Font(Font.SERIF, Font.PLAIN, 44);
            FontRenderContext frc = g.getFontRenderContext();
            TextLayout layout = new TextLayout("Geometry", font, frc);
            float x = 42f;
            float baseline = 126f;
            g.setFont(font);
            g.setColor(INK);
            layout.draw(g, x, baseline);
            g.setStroke(new java.awt.BasicStroke(1.4f));
            g.setColor(BLUE);
            g.draw(new Line2D.Double(24, baseline, c.getWidth() - 28, baseline));
            g.setColor(GREEN);
            g.draw(new Line2D.Double(24, baseline - layout.getAscent(), c.getWidth() - 28,
                    baseline - layout.getAscent()));
            g.setColor(AMBER);
            g.draw(new Line2D.Double(24, baseline + layout.getDescent(), c.getWidth() - 28,
                    baseline + layout.getDescent()));
            g.setColor(RED);
            Rectangle2D bounds = layout.getBounds();
            g.draw(new Rectangle2D.Double(x + bounds.getX(), baseline + bounds.getY(),
                    bounds.getWidth(), bounds.getHeight()));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(BLUE);
            g.drawString("baseline", 310, (int) baseline - 4);
            g.setColor(GREEN);
            g.drawString("ascent", 310, (int) (baseline - layout.getAscent()) - 4);
            g.setColor(AMBER);
            g.drawString("descent", 310, (int) (baseline + layout.getDescent()) + 14);
            g.setColor(RED);
            g.drawString("visual bounds", 88, 178);
        });
        canvas.setPreferredSize(new Dimension(430, 230));
        panel.add(canvas, "grow");
        panel.add(bulletList("Font questions",
                "advance is not outline bounds",
                "baseline anchors layout",
                "FRC affects measurement",
                "glyphs may overhang",
                "fallback changes metrics"), "grow, wrap");
        return panel;
    }

    static JPanel geomGlyphOutline() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Glyph vectors expose text as shapes",
                "[grow,fill][250!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            Font font = new Font(Font.SERIF, Font.BOLD, 76);
            GlyphVector vector = font.createGlyphVector(g.getFontRenderContext(), "Aa");
            Shape outline = vector.getOutline(64f, 140f);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(0xd9e8f7));
            g.fill(outline);
            g.setStroke(new java.awt.BasicStroke(2.0f));
            g.setColor(BLUE);
            g.draw(outline);
            Rectangle2D visual = outline.getBounds2D();
            g.setColor(RED);
            g.draw(visual);
            for (int i = 0; i < vector.getNumGlyphs(); i++) {
                Point2D pos = vector.getGlyphPosition(i);
                g.setColor(AMBER);
                g.fill(new Ellipse2D.Double(64 + pos.getX() - 3, 140 + pos.getY() - 3, 6, 6));
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(INK);
            g.drawString("outline can be filled, stroked, hit-tested, and transformed", 36, 198);
        });
        canvas.setPreferredSize(new Dimension(430, 230));
        panel.add(canvas, "grow");
        panel.add(bulletList("Advanced uses",
                "text along paths",
                "logo-like outlines",
                "collision geometry",
                "print/export shapes",
                "custom selection"), "grow, wrap");
        return panel;
    }

    static JPanel geomTextTransformBounds() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Text has local bounds before the canvas transform",
                "[grow,fill][240!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            Font font = new Font(Font.SERIF, Font.BOLD, 38);
            FontRenderContext frc = g.getFontRenderContext();
            TextLayout layout = new TextLayout("North Gate", font, frc);
            Rectangle2D localBounds = layout.getBounds();
            AffineTransform textTransform = new AffineTransform();
            textTransform.translate(88, 166);
            textTransform.rotate(Math.toRadians(-18));

            Graphics2D textGraphics = (Graphics2D) g.create();
            try {
                textGraphics.transform(textTransform);
                textGraphics.setColor(new Color(0xd9e8f7));
                textGraphics.fill(new Rectangle2D.Double(localBounds.getX() - 4,
                        localBounds.getY() - 4, localBounds.getWidth() + 8,
                        localBounds.getHeight() + 8));
                textGraphics.setColor(INK);
                layout.draw(textGraphics, 0f, 0f);
                textGraphics.setStroke(new java.awt.BasicStroke(1.4f));
                textGraphics.setColor(BLUE);
                textGraphics.draw(new Line2D.Double(-10, 0, layout.getAdvance() + 12, 0));
                textGraphics.setColor(RED);
                textGraphics.draw(localBounds);
            } finally {
                textGraphics.dispose();
            }

            Shape transformedBounds = textTransform.createTransformedShape(localBounds);
            g.setColor(AMBER);
            g.setStroke(new java.awt.BasicStroke(2.0f));
            g.draw(transformedBounds.getBounds2D());
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(BLUE);
            g.drawString("baseline in local text space", 50, 64);
            g.setColor(RED);
            g.drawString("visual bounds before transform", 50, 82);
            g.setColor(AMBER);
            g.drawString("axis-aligned repaint bounds after transform", 240, 216);
        });
        canvas.setPreferredSize(new Dimension(470, 230));
        panel.add(canvas, "grow");
        panel.add(bulletList("Font geometry",
                "baseline is the anchor",
                "visual bounds are local",
                "transformed bounds repaint",
                "FRC belongs in caches",
                "text remains semantic state"), "grow, wrap");
        return panel;
    }

    static JPanel transferDropTarget() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("TransferHandler separates data from gesture",
                "[grow,fill][80!][grow,fill]", "[][grow]");
        panel.add(new JScrollPane(new JList<>(new String[] {"Ada.csv", "Grace.csv", "Orders.csv"})), "grow");
        panel.add(arrowPanel("drop"), "grow");
        JTextArea target = new JTextArea("Accepted flavors:\n- file list\n- text/csv\n\nDrop preview appears here.");
        panel.add(new JScrollPane(target), "grow, wrap");
        return panel;
    }

    static JPanel transferImportProgress() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Import feedback after transfer",
                "[][grow,fill]", "[][][][]");
        panel.add(new JLabel("Source"));
        panel.add(new JLabel("orders-2026.csv"), "wrap");
        panel.add(new JLabel("Rows"));
        panel.add(new JLabel("3,200 accepted, 12 rejected"), "wrap");
        panel.add(new JLabel("Progress"));
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(72);
        panel.add(progress, "growx, wrap");
        panel.add(new JLabel("Status"));
        panel.add(tag("Importing batch 8 of 11", BLUE), "wrap");
        return panel;
    }

    static JPanel transferFlavorWorkflow() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Transfer is gesture, negotiation, and workflow",
                "[grow,fill][230!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle gesture = new Rectangle(24, 28, 108, 54);
            Rectangle transferable = new Rectangle(168, 28, 124, 54);
            Rectangle flavor = new Rectangle(332, 28, 124, 54);
            Rectangle request = new Rectangle(168, 132, 124, 54);
            Rectangle task = new Rectangle(332, 132, 124, 54);
            Rectangle model = new Rectangle(168, 204, 124, 54);

            drawFlowBox(g, gesture, "gesture", "copy / drop");
            drawFlowBox(g, transferable, "Transferable", "snapshot");
            drawFlowBox(g, flavor, "DataFlavor", "best fit");
            drawFlowBox(g, request, "import request", "validated");
            drawFlowBox(g, task, "task service", "slow work");
            drawFlowBox(g, model, "model state", "batch + problems");

            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2.1f));
            drawArrow(g, gesture.x + gesture.width, gesture.y + 29, transferable.x, transferable.y + 29);
            drawArrow(g, transferable.x + transferable.width, transferable.y + 29, flavor.x, flavor.y + 29);
            drawArrow(g, flavor.x + 66, flavor.y + flavor.height, task.x + 66, task.y);
            drawArrow(g, transferable.x + 66, transferable.y + transferable.height, request.x + 66, request.y);
            drawArrow(g, request.x + request.width, request.y + 29, task.x, task.y + 29);
            drawArrow(g, task.x, task.y + 48, model.x + model.width, model.y + 18);
            g.setColor(MUTED);
            g.drawString("canImport: cheap feedback", 24, 112);
            g.drawString("importData: request", 276, 112);
            g.drawString("the final mutation belongs to domain code, not mouse code", 24, 194);
        });
        canvas.setPreferredSize(new Dimension(500, 260));
        panel.add(canvas, "grow");
        panel.add(bulletList("Review boundaries",
                "payload is a snapshot",
                "flavors are a contract",
                "location becomes a request",
                "slow work leaves the EDT",
                "results publish precisely"), "grow, wrap");
        return panel;
    }

    static JPanel accessibilityLabels() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Accessible names from stable field facts",
                "[][grow,fill][grow,fill]", "[][][]");
        JTextField name = new JTextField("Ada Lovelace");
        name.getAccessibleContext().setAccessibleName("Customer name");
        name.getAccessibleContext().setAccessibleDescription("Enter the customer display name");
        panel.add(new JLabel("Name"));
        panel.add(name, "growx");
        panel.add(tag("accessibleName = Customer name", BLUE), "growx, wrap");
        JTextField limit = new JTextField("1200.00");
        limit.getAccessibleContext().setAccessibleName("Credit limit");
        panel.add(new JLabel("Credit limit"));
        panel.add(limit, "growx");
        panel.add(tag("resource-backed description", GREEN), "growx, wrap");
        panel.add(note("Accessibility is not decoration",
                "Visible labels, tooltips, validation, and help must have semantic counterparts."),
                "span 3, growx");
        return panel;
    }

    static JPanel accessibilityFocusOrder() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Keyboard path through a screen",
                "[grow,fill][grow,fill][grow,fill][grow,fill]", "[][grow][]");
        panel.add(focusTile("1", "Search"), "grow");
        panel.add(focusTile("2", "Table"), "grow");
        panel.add(focusTile("3", "Detail"), "grow");
        panel.add(focusTile("4", "Save"), "grow, wrap");
        panel.add(note("Input review",
                "Tab order, mnemonics, default buttons, Escape behavior, and F1 help must describe one workflow."),
                "span 4, growx");
        return panel;
    }

    static JPanel accessibilitySemanticChannels() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("One state, several channels",
                "[grow,fill][grow,fill][grow,fill][grow,fill]", "[][grow][]");
        panel.add(bulletList("Visible", "red border", "inline text"), "grow");
        panel.add(bulletList("Keyboard", "summary action", "focus repair"), "grow");
        panel.add(bulletList("Accessible", "name and role", "problem state"), "grow");
        panel.add(bulletList("Proof", "resource key", "component test"), "grow, wrap");
        panel.add(note("Review rule",
                "A meaningful state is robust only when it survives sight, keyboard, assistive technology, and tests."),
                "span 4, growx");
        return panel;
    }

    static JPanel diagnosticsWatchdog() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("EDT watchdog and repaint diagnostics",
                "[grow,fill][260!]", "[][grow][]");
        JList<String> eventLog = new JList<>(new String[] {
                "14:08:12.210  action refreshCustomers started",
                "14:08:12.711  EDT latency 501 ms",
                "14:08:12.714  top event ActionEvent Refresh",
                "14:08:12.720  dirty area 1,248,000 px",
                "14:08:12.735  repaint burst CustomerTable"
        });
        panel.add(new JScrollPane(eventLog), "grow");
        panel.add(bulletList("Snapshot",
                "thread dump captured",
                "recent command retained",
                "repaint area sampled",
                "support bundle redacted"), "grow, wrap");
        panel.add(note("Operational rule",
                "Collect bounded evidence before the next incident, then export it only when diagnostics are requested."),
                "span 2, growx");
        return panel;
    }

    static JPanel diagnosticsComponentTree() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Component tree dump with lifecycle facts",
                "[280!,fill][grow,fill]", "[][grow][]");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("CustomerWorkspacePanel");
        DefaultMutableTreeNode toolbar = new DefaultMutableTreeNode("JToolBar name=customer.toolbar");
        toolbar.add(new DefaultMutableTreeNode("JButton name=customer.refresh"));
        toolbar.add(new DefaultMutableTreeNode("JButton name=customer.save"));
        DefaultMutableTreeNode split = new DefaultMutableTreeNode("JSplitPane name=customer.split");
        split.add(new DefaultMutableTreeNode("JTable name=customer.table rows=4"));
        split.add(new DefaultMutableTreeNode("JPanel name=customer.detail"));
        root.add(toolbar);
        root.add(split);
        JTree tree = new JTree(root);
        for (int row = 0; row < tree.getRowCount(); row++) {
            tree.expandRow(row);
        }
        panel.add(new JScrollPane(tree), "grow");
        JTextArea dump = new JTextArea("""
                displayable=true  showing=true
                layout=MigLayout  laf=FlatLaf
                focusOwner=customer.filter
                scope subscriptions=18
                active tasks=1
                last full table reset=none
                leak probe=collectible after close
                """);
        dump.setEditable(false);
        dump.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        panel.add(new JScrollPane(dump), "grow, wrap");
        panel.add(note("Dump policy",
                "Stable component names, bounds, accessibility text, scope counts, and task names make screenshots diagnosable."),
                "span 2, growx");
        return panel;
    }

    static JPanel diagnosticsLeakLifecycle() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Leak path and lifecycle cleanup",
                "[grow,fill][230!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());

            Rectangle root = new Rectangle(34, 38, 118, 54);
            Rectangle service = new Rectangle(196, 38, 118, 54);
            Rectangle listener = new Rectangle(358, 38, 118, 54);
            Rectangle controller = new Rectangle(196, 154, 118, 54);
            Rectangle panelBox = new Rectangle(358, 154, 118, 54);
            Rectangle scope = new Rectangle(34, 154, 118, 54);

            drawFlowBox(g, root, "app root", "long-lived");
            drawFlowBox(g, service, "service", "listener list");
            drawFlowBox(g, listener, "listener", "captures owner");
            drawFlowBox(g, controller, "controller", "screen owner");
            drawFlowBox(g, panelBox, "component tree", "disposed");
            drawFlowBox(g, scope, "scope.close", "removes links");

            g.setStroke(new java.awt.BasicStroke(2.2f));
            g.setColor(RED);
            drawArrow(g, root.x + root.width, root.y + 27, service.x, service.y + 27);
            drawArrow(g, service.x + service.width, service.y + 27, listener.x, listener.y + 27);
            drawArrow(g, listener.x + 58, listener.y + listener.height, panelBox.x + 58, panelBox.y);
            drawArrow(g, panelBox.x, panelBox.y + 27, controller.x + controller.width, controller.y + 27);
            g.drawString("retained path", 206, 116);

            g.setColor(GREEN);
            drawArrow(g, scope.x + scope.width, scope.y + 27, controller.x, controller.y + 27);
            drawArrow(g, scope.x + 58, scope.y, service.x + 58, service.y + service.height);
            g.drawString("cleanup owner", 44, 236);

            g.setColor(MUTED);
            g.setFont(g.getFont().deriveFont(11f));
            g.drawString("A closed screen is collectible only after long-lived references are removed.", 42, 264);
        });
        canvas.setPreferredSize(new Dimension(500, 290));
        panel.add(canvas, "grow");
        panel.add(bulletList("Leak review",
                "find long-lived roots",
                "name installed links",
                "close by screen scope",
                "probe collectability",
                "test the owner"), "grow, wrap");
        return panel;
    }

    static JPanel packagingPipeline() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Release pipeline for a packaged Swing app",
                "[grow,fill][grow,fill][grow,fill]", "[][grow][grow][]");
        panel.add(behaviorTile("Compile", "generated sources, tests"), "grow");
        panel.add(behaviorTile("Analyze", "jdeps module report"), "grow");
        panel.add(behaviorTile("Runtime", "jlink image, JDK 25"), "grow, wrap");
        panel.add(behaviorTile("Package", "jpackage app image"), "grow");
        panel.add(behaviorTile("Sign", "platform trust"), "grow");
        panel.add(behaviorTile("Smoke test", "installed launcher"), "grow, wrap");
        panel.add(note("Release evidence",
                "Record source revision, JDK build, runtime modules, package options, signing identity, and test logs."),
                "span 3, growx");
        return panel;
    }

    static JPanel packagingInstalledLayout() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Installed layout separates product files from user state",
                "[grow,fill][230!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle install = new Rectangle(28, 34, 170, 72);
            Rectangle runtime = new Rectangle(280, 34, 170, 72);
            Rectangle userData = new Rectangle(28, 162, 170, 72);
            Rectangle logs = new Rectangle(280, 162, 170, 72);
            drawFlowBox(g, install, "install image", "replaceable files");
            drawFlowBox(g, runtime, "runtime image", "jlink modules");
            drawFlowBox(g, userData, "user data", "survives updates");
            drawFlowBox(g, logs, "support area", "logs + bundle");
            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2.2f));
            drawArrow(g, install.x + install.width, install.y + 36, runtime.x, runtime.y + 36);
            drawArrow(g, install.x + 85, install.y + install.height, userData.x + 85, userData.y);
            drawArrow(g, runtime.x + 85, runtime.y + runtime.height, logs.x + 85, logs.y);
            g.setColor(MUTED);
            g.drawString("diagnostics must know both sides of the boundary", 74, 266);
        });
        canvas.setPreferredSize(new Dimension(500, 270));
        panel.add(canvas, "grow");
        panel.add(bulletList("Package audit",
                "launcher has options",
                "runtime is recorded",
                "resources are bundled",
                "user data is writable",
                "logs are discoverable"), "grow, wrap");
        return panel;
    }

    static JPanel packagingSupportDialog() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Support dialog from the installed app",
                "[][grow,fill][grow,fill]", "[][][][][][][]");
        panel.add(new JLabel("Application"));
        panel.add(new JLabel("Orders Desk 4.2.0"));
        panel.add(tag("build 8f24c21", BLUE), "growx, wrap");
        panel.add(new JLabel("Runtime"));
        panel.add(new JLabel("Temurin 25.0.2, custom image"));
        panel.add(tag("java.desktop present", GREEN), "growx, wrap");
        panel.add(new JLabel("Look-and-Feel"));
        panel.add(new JLabel("FlatLaf Light"));
        panel.add(tag("scale 150%", BLUE), "growx, wrap");
        panel.add(new JLabel("User data"));
        panel.add(new JLabel("AppData/Auderis/Orders"));
        panel.add(tag("writable", GREEN), "growx, wrap");
        panel.add(new JLabel("Diagnostics"));
        panel.add(new JLabel("tasks=1, scopes=12, locale=en-US"));
        panel.add(tag("resources ok", GREEN), "growx, wrap");
        panel.add(new JSeparator(), "span 3, growx, wrap");
        panel.add(new JButton("Copy diagnostics"), "span 3, split 3, align right");
        panel.add(new JButton("Open logs"));
        panel.add(new JButton("Close"));
        return panel;
    }

    static JPanel modernTaskBoundary() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Modern task boundary still returns to the EDT",
                "[grow,fill][70!][grow,fill][70!][grow,fill]", "[][grow][]");
        panel.add(behaviorTile("EDT gesture", "Refresh command"), "grow");
        panel.add(arrowPanel("start"), "grow");
        panel.add(behaviorTile("Virtual thread", "blocking service call"), "grow");
        panel.add(arrowPanel("publish"), "grow");
        panel.add(behaviorTile("EDT delivery", "model and components"), "grow, wrap");
        panel.add(note("Ownership rule",
                "A generation token, cancellation handle, and scope decide whether a result still belongs to the screen."),
                "span 5, growx");
        return panel;
    }

    static JPanel bindingFormState() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Binding keeps model and component synchronized",
                "[grow,fill][60!][grow,fill]", "[][grow]");
        panel.add(compactForm(), "grow");
        panel.add(arrowPanel("bind"), "grow");
        panel.add(bulletList("Core model",
                "raw text = Ada Lovelace",
                "parsed value = valid",
                "dirty = true",
                "problems = none"), "grow, wrap");
        return panel;
    }

    static JPanel modelOwnershipBoundary() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Presentation boundary ownership map",
                "[grow,fill][220!]", "[][grow]");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());

            g.setColor(new Color(0xf1f5f8));
            g.fillRoundRect(18, 18, c.getWidth() - 36, c.getHeight() - 36, 18, 18);
            g.setColor(LINE);
            g.drawRoundRect(18, 18, c.getWidth() - 36, c.getHeight() - 36, 18, 18);

            Rectangle domain = new Rectangle(34, 64, 120, 62);
            Rectangle services = new Rectangle(34, 166, 120, 62);
            Rectangle presentation = new Rectangle(196, 42, 146, 84);
            Rectangle descriptors = new Rectangle(196, 164, 146, 64);
            Rectangle components = new Rectangle(382, 52, 104, 62);
            Rectangle swingModels = new Rectangle(382, 152, 104, 76);

            drawFlowBox(g, domain, "domain model", "business facts");
            drawFlowBox(g, services, "services", "slow work");
            drawFlowBox(g, presentation, "presentation model", "screen facts");
            drawFlowBox(g, descriptors, "descriptors", "stable keys");
            drawFlowBox(g, components, "components", "visible widgets");
            drawFlowBox(g, swingModels, "Swing models", "Document / Table");

            g.setStroke(new java.awt.BasicStroke(2.2f));
            g.setColor(BLUE);
            drawArrow(g, domain.x + domain.width, domain.y + 31, presentation.x, presentation.y + 30);
            drawArrow(g, services.x + services.width, services.y + 31, presentation.x, presentation.y + 64);
            drawArrow(g, presentation.x + presentation.width, presentation.y + 30, components.x, components.y + 31);
            drawArrow(g, presentation.x + presentation.width, presentation.y + 64, swingModels.x, swingModels.y + 31);
            g.setColor(GREEN);
            drawArrow(g, descriptors.x + descriptors.width, descriptors.y + 32, swingModels.x, swingModels.y + 60);
            drawArrow(g, descriptors.x + 72, descriptors.y, presentation.x + 72, presentation.y + presentation.height);

            g.setColor(AMBER);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
            g.drawString("bindings adapt facts; scopes own cleanup", 172, 252);
            g.setColor(MUTED);
            g.setFont(g.getFont().deriveFont(11f));
            g.drawString("Tests should target the owner of the fact, not the nearest widget.", 76, 276);
        });
        canvas.setPreferredSize(new Dimension(500, 290));
        panel.add(canvas, "grow");
        panel.add(bulletList("Ownership tests",
                "domain: no Swing",
                "presentation: screen facts",
                "components: mechanics",
                "bindings: synchronization",
                "scope: cleanup"), "grow, wrap");
        return panel;
    }

    static JPanel behaviorPlan() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Generated behavior plan",
                "[grow,fill][grow,fill][grow,fill]", "[][grow][]");
        panel.add(behaviorTile("Text binding", "phase: model sync"), "grow");
        panel.add(behaviorTile("Tooltip", "phase: decoration"), "grow");
        panel.add(behaviorTile("F1 help", "phase: input"), "grow, wrap");
        panel.add(note("Scope owns cleanup",
                "The plan is inspectable Java; the scope records installed behavior keys and closes them together."),
                "span 3, growx");
        return panel;
    }

    static JPanel coruscoTableDescriptor() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Descriptor-backed table",
                "[grow,fill][250!]", "[][grow]");
        panel.add(new JScrollPane(sampleTable()), "grow");
        panel.add(bulletList("Descriptor owns",
                "table id",
                "column ids",
                "resource keys",
                "value readers",
                "persistence ids"), "grow, wrap");
        return panel;
    }

    static JPanel coruscoTableState() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Persisted table state by stable ids",
                "[grow,fill][260!]", "[][grow]");
        JTable table = sampleTable();
        table.getColumnModel().moveColumn(0, 2);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        panel.add(new JScrollPane(table), "grow");
        panel.add(bulletList("Saved state",
                "customer/name width=180",
                "customer/state visible=true",
                "customer/lastOrder sort=ASC",
                "not localized headers"), "grow, wrap");
        return panel;
    }

    static JPanel dialogValidation() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Dialog validation path",
                "[][grow,fill]", "[][][][][]");
        panel.add(new JLabel("Name"));
        panel.add(new JTextField(""), "growx, wrap");
        panel.add(new JLabel("Credit limit"));
        JTextField limit = new JTextField("-5");
        limit.setBorder(BorderFactory.createLineBorder(RED, 2));
        panel.add(limit, "growx, wrap");
        panel.add(tag("Name is required; credit limit must be non-negative", RED),
                "span 2, growx, wrap");
        panel.add(new JPanel(), "span 2, pushy, wrap");
        panel.add(new JButton("Cancel"), "span 2, split 3, tag cancel, align right");
        panel.add(new JButton("Apply"), "tag apply");
        panel.add(new JButton("OK"), "tag ok");
        return panel;
    }

    static JPanel dialogActiveEditor() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Active editor before OK",
                "[grow,fill][220!]", "[][grow]");
        JTable table = new JTable(new Object[][] {
                {"Ada", "1200.00"},
                {"Grace", "editing..."}
        }, new Object[] {"Customer", "Limit"});
        table.setRowSelectionInterval(1, 1);
        table.setColumnSelectionInterval(1, 1);
        panel.add(new JScrollPane(table), "grow");
        panel.add(bulletList("OK sequence",
                "stop active editor",
                "validate form",
                "create result",
                "close shell"), "grow, wrap");
        return panel;
    }

    static JPanel dialogDirtyCancel() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Dirty cancel is a dialog decision",
                "[grow,fill][220!]", "[][grow]");
        JPanel form = new JPanel(new MigLayout("fillx, insets 10", "[][grow,fill]", "[][][][]"));
        form.setOpaque(true);
        form.setBackground(Color.WHITE);
        form.add(new JLabel("Name"));
        form.add(new JTextField("Ada Lovelace"), "growx, wrap");
        form.add(new JLabel("Credit limit"));
        form.add(new JTextField("1500.00"), "growx, wrap");
        form.add(new JLabel("Status"));
        form.add(new JComboBox<>(new String[] {"Active", "Review"}), "growx, wrap");
        form.add(tag("Unsaved changes since last Apply", AMBER), "span 2, growx, wrap");
        form.add(new JButton("Discard"), "span 2, split 3, tag no, align right");
        form.add(new JButton("Keep editing"), "tag yes");
        form.add(new JButton("Apply"), "tag apply");
        panel.add(form, "grow");
        panel.add(bulletList("Cancel path",
                "query DirtyState",
                "ask confirmation only if dirty",
                "reset form on confirmed cancel",
                "close bypasses confirmation"), "grow, wrap");
        return panel;
    }

    static JPanel dialogLifecycle() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("One dialog activation owns temporary resources",
                "[grow,fill][grow,fill][grow,fill]", "[][grow][]");
        panel.add(lifecycleCard("1", "Controller",
                "FormDialog",
                "OK, Apply, Cancel, result"), "grow");
        panel.add(lifecycleCard("2", "Bindings",
                "fields, validation, keyboard",
                "listeners and root-pane state"), "grow");
        panel.add(lifecycleCard("3", "Services",
                "TaskService, detachables",
                "background work and caches"), "grow, wrap");
        panel.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, growx, wrap");
        panel.add(note("Close order",
                "The lifecycle closes registered resources in reverse order, then closes the dialog controller."),
                "span 3, growx");
        return panel;
    }

    static JPanel backgroundBusyTask() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Task busy state drives UI",
                "[grow,fill][220!]", "[][grow]");
        JLayer<JComponent> layer = new JLayer<>(new JScrollPane(sampleTable()));
        BusyOverlayLayerUI ui = new BusyOverlayLayerUI(new Color(0x202833), 0.30f);
        layer.setUI(ui);
        ui.setBusy(layer, true);
        panel.add(layer, "grow");
        panel.add(bulletList("Bound outputs",
                "Refresh disabled",
                "overlay active",
                "status = Loading...",
                "Cancel enabled"), "grow, wrap");
        return panel;
    }

    static JPanel backgroundStatusProgress() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Progress is modeled state",
                "[][grow,fill]", "[][][][]");
        panel.add(new JLabel("Operation"));
        panel.add(new JLabel("Load customer rows"), "wrap");
        panel.add(new JLabel("Progress"));
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(46);
        panel.add(progress, "growx, wrap");
        panel.add(new JLabel("Cancellation"));
        panel.add(new JButton("Cancel current task"), "wrap");
        panel.add(new JLabel("Status"));
        panel.add(tag("Loaded page 4 of 9", BLUE), "wrap");
        return panel;
    }

    static JPanel testingScreenshotHarness() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Screenshot harness output",
                "[grow,fill][260!]", "[][grow]");
        panel.add(lafDensity(), "grow");
        panel.add(bulletList("Harness fixes",
                "FlatLaf setup",
                "deterministic data",
                "fixed component size",
                "layout before paint",
                "PNG for LaTeX"), "grow, wrap");
        return panel;
    }

    static JPanel testingComponentKeys() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Component keys make tests stable",
                "[][grow,fill][grow,fill]", "[][][]");
        panel.add(new JLabel("Name"));
        panel.add(new JTextField("Ada Lovelace"), "growx");
        panel.add(tag("customer/name", BLUE), "growx, wrap");
        panel.add(new JLabel("Table"));
        panel.add(new JScrollPane(sampleTable()), "growx");
        panel.add(tag("customer/table", GREEN), "growx, wrap");
        panel.add(note("Test lookup",
                "Find components by typed keys, not localized text or current layout position."),
                "span 3, growx");
        return panel;
    }

    static JPanel bookappBusyDetail() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Workspace with selected detail and busy table",
                "[grow,fill][320!]", "[][grow][]");
        JLayer<JComponent> tableLayer = new JLayer<>(new JScrollPane(sampleTable()));
        BusyOverlayLayerUI ui = new BusyOverlayLayerUI(new Color(0x202833), 0.25f);
        tableLayer.setUI(ui);
        ui.setBusy(tableLayer, true);
        JPanel detail = compactForm();
        detail.add(tag("Name is currently valid", GREEN), "span 2, growx, wrap");
        panel.add(new JLabel("Customers"));
        panel.add(new JLabel("Detail"), "wrap");
        panel.add(tableLayer, "grow");
        panel.add(detail, "grow, wrap");
        panel.add(tag("Loading refreshed rows; selected customer remains visible", BLUE),
                "span 2, growx");
        return panel;
    }

    static JPanel practicalSplitButtons() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Split button command surfaces",
                "[grow,fill][230!]", "[][grow][]");

        JPanel toolbar = cardPanel();
        toolbar.setLayout(new MigLayout("fillx, insets 12", "[][][][grow]", "[][]"));
        toolbar.add(strong("Order commands"), "span 4, growx, wrap");
        toolbar.add(new JButton("Refresh"));
        toolbar.add(splitButtonPreview("Export PDF", "v"));
        toolbar.add(new JButton("Print"));
        toolbar.add(new JLabel("Action keys feed every surface"), "right, growx, wrap");

        JPanel popup = cardPanel();
        popup.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][][]"));
        popup.add(strong("Export"), "growx, wrap");
        popup.add(new JMenuItem("Export CSV"), "growx, wrap");
        popup.add(new JMenuItem("Export XLSX"), "growx, wrap");
        popup.add(new JMenuItem("Print preview"), "growx");

        JPanel commandModel = cardPanel();
        commandModel.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][][]"));
        commandModel.add(strong("Corusco metadata"), "growx, wrap");
        commandModel.add(new JLabel("ActionKey: export.pdf"), "growx, wrap");
        commandModel.add(new JLabel("ActionKey: export.csv"), "growx, wrap");
        commandModel.add(new JLabel("ResourceKey: export.more"), "growx, wrap");
        commandModel.add(new JLabel("Scope closes menu bindings"), "growx");

        panel.add(toolbar, "growx, wrap");
        panel.add(popup, "grow");
        panel.add(commandModel, "grow, wrap");
        panel.add(note("Review path",
                "Generated commands create Swing actions; the split button presents primary and secondary paths."),
                "span 2, growx");
        return panel;
    }

    static JPanel practicalBuddyAndUnitFields() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Buddy fields and integral unit entry",
                "[grow,fill][230!]", "[][grow][]");

        JPanel form = cardPanel();
        form.setLayout(new MigLayout("fillx, insets 14", "[120!][grow,fill][]", "[][][][][]"));
        form.add(strong("Shipment filter"), "span 3, growx, wrap");
        form.add(new JLabel("Customer"));
        form.add(new JTextField("Acme"));
        form.add(new JButton("Search"), "wrap");
        form.add(new JLabel("Weight"));
        form.add(unitFieldPreview("120.00", "kg"));
        form.add(new JLabel("FieldKey: shipping.weight"), "wrap");
        form.add(new JLabel("Report folder"));
        form.add(new JTextField("D:\\reports\\june"));
        form.add(new JButton("Browse"), "wrap");
        form.add(new JLabel());
        JLabel problem = new JLabel("Amount is required before shipping can be estimated.");
        problem.setForeground(RED);
        form.add(problem, "span 2, growx");

        JPanel model = cardPanel();
        model.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][][][]"));
        model.add(strong("Field model"), "growx, wrap");
        model.add(new JLabel("rawAmount"), "growx, wrap");
        model.add(new JLabel("selectedUnit"), "growx, wrap");
        model.add(new JLabel("ProblemSet"), "growx, wrap");
        model.add(new JLabel("BindingScope"), "growx");

        panel.add(form, "grow");
        panel.add(model, "grow, wrap");
        panel.add(note("Construction rule",
                "Visible Swing children stay ordinary; Corusco keys and problem codes name the logical field."),
                "span 2, growx");
        return panel;
    }

    static JPanel practicalTableHeader() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Advanced JTable header behavior",
                "[grow,fill][230!]", "[][grow][]");

        DefaultTableModel model = new DefaultTableModel(new Object[][] {
                {"Acme Manufacturing", "Active", "2026-06-22", "$12,450"},
                {"Northstar Retail", "Hold", "2026-07-04", "$8,125"},
                {"Lumen Services", "Active", "2026-07-17", "$19,980"},
                {"Cedar Logistics", "Warning", "2026-08-02", "$6,410"}
        }, new Object[] {"Customer", "Status", "Due date v", "Amount"});
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xe1e6eb));
        table.getTableHeader().setReorderingAllowed(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(210);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        JPanel tableSurface = cardPanel();
        tableSurface.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][]"));
        tableSurface.add(strong("Orders by descriptor-backed column"), "growx, wrap");
        tableSurface.add(table.getTableHeader(), "growx, wrap");
        tableSurface.add(table, "growx, wrap");
        tableSurface.add(tag("ColumnKey.dueDate owns sort, filter, help, visibility", BLUE),
                "growx");

        JPanel menu = cardPanel();
        menu.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][][][][]"));
        menu.add(strong("Header popup: Due date"), "growx, wrap");
        menu.add(menuRow("Sort ascending"), "growx, wrap");
        menu.add(menuRow("Sort descending"), "growx, wrap");
        menu.add(menuRow("Filter by range..."), "growx, wrap");
        menu.add(menuRow("Hide column"), "growx, wrap");
        menu.add(menuRow("Help for due date"), "growx");

        JPanel descriptor = cardPanel();
        descriptor.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][][][][]"));
        descriptor.add(strong("Descriptor path"), "growx, wrap");
        descriptor.add(new JLabel("view column"), "growx, wrap");
        descriptor.add(new JLabel("model column"), "growx, wrap");
        descriptor.add(new JLabel("ColumnKey.dueDate"), "growx, wrap");
        descriptor.add(new JLabel("SortKey + RowSorter"), "growx, wrap");
        descriptor.add(new JLabel("resource/help/state ids"), "growx, wrap");
        descriptor.add(tag("localized header is display only", AMBER), "growx");

        JPanel side = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[grow][grow]"));
        side.setOpaque(false);
        side.add(menu, "grow, wrap");
        side.add(descriptor, "grow");

        panel.add(tableSurface, "grow");
        panel.add(side, "grow, wrap");
        panel.add(note("Header rule",
                "Pointer location resolves to a descriptor column before sort, filter, help, or visibility commands run."),
                "span 2, growx");
        return panel;
    }

    static JPanel practicalTemporalSelection() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Temporal selection panels",
                "[grow,fill][grow,fill]", "[][grow][]");
        panel.add(calendarPreview(), "grow");
        panel.add(dateTimeSpanPreview(), "grow, wrap");
        panel.add(note("Temporal contract",
                "Draft date, draft time, quick range, zone, endpoint policy, and committed value are separate facts."),
                "span 2, growx");
        return panel;
    }

    static JPanel practicalTagCloud() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Catalog-backed tag cloud",
                "[grow,fill][250!]", "[][grow][]");

        JPanel editor = cardPanel();
        editor.setLayout(new MigLayout("fillx, insets 14", "[grow]", "[][][][][]"));
        editor.add(strong("Customer labels"), "growx, wrap");
        JPanel chips = new JPanel(new MigLayout("insets 0, gap 6", "[][][][]", "[][]"));
        chips.setOpaque(false);
        chips.add(tag("priority", RED));
        chips.add(tag("renewal", BLUE));
        chips.add(tag("finance", GREEN));
        chips.add(tag("manual review", AMBER), "wrap");
        chips.add(new JTextField("add catalog tag"), "span 3, growx");
        chips.add(new JButton("Add"));
        editor.add(chips, "growx, wrap");
        editor.add(tag("Problem: duplicate tag 'finance'", RED), "growx, wrap");
        editor.add(new JLabel("Focused chip: renewal; query: \"ris\""), "growx, wrap");
        editor.add(new JLabel("FieldKey: customer.tags"), "growx");

        JPanel suggestions = cardPanel();
        suggestions.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][][][][]"));
        suggestions.add(strong("Suggestions"), "growx, wrap");
        suggestions.add(menuRow("risk - catalog id TAG-204"), "growx, wrap");
        suggestions.add(menuRow("risk review - catalog id TAG-512"), "growx, wrap");
        suggestions.add(menuRow("region restricted - disabled"), "growx, wrap");
        suggestions.add(new JSeparator(), "growx, wrap");
        suggestions.add(new JLabel("generation 18, stale replies ignored"), "growx");

        panel.add(editor, "grow");
        panel.add(suggestions, "grow, wrap");
        panel.add(note("Model path",
                "selected ids + query + suggestions + focused chip + ProblemSet drive ordinary Swing children."),
                "span 2, growx");
        return panel;
    }

    static JPanel practicalInteractiveCells() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Interactive cells in JTable",
                "[grow,fill][240!]", "[][grow][]");

        DefaultTableModel model = new DefaultTableModel(new Object[][] {
                {"ORD-1042", "Acme Manufacturing", "Pending", "Approve", "rowId=1042"},
                {"ORD-1043", "Northstar Retail", "Blocked", "Resolve", "rowId=1043"},
                {"ORD-1044", "Lumen Services", "Ready", "Ship", "rowId=1044"},
                {"ORD-1045", "Cedar Logistics", "Review", "Open", "rowId=1045"}
        }, new Object[] {"Order", "Customer", "Status", "Action", "Identity"});
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(85);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(105);

        JPanel tableCard = cardPanel();
        tableCard.setLayout(new MigLayout("fill, insets 10", "[grow]", "[][grow][]"));
        tableCard.add(strong("Approval queue"), "growx, wrap");
        tableCard.add(new JScrollPane(table), "grow, wrap");
        tableCard.add(tag("Selected cell invokes CommandKey.order.approve through RowId + ColumnKey", BLUE),
                "growx");

        JPanel route = cardPanel();
        route.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][][][][]"));
        route.add(strong("Event route"), "growx, wrap");
        route.add(new JLabel("view row 2"), "growx, wrap");
        route.add(new JLabel("convertRowIndexToModel"), "growx, wrap");
        route.add(new JLabel("RowId ORD-1044"), "growx, wrap");
        route.add(new JLabel("ColumnKey.action"), "growx, wrap");
        route.add(tag("renderer paints; action map invokes", GREEN), "growx");

        panel.add(tableCard, "grow");
        panel.add(route, "grow, wrap");
        panel.add(note("Interaction rule",
                "Painted buttons and badges are not live children; commands are routed by table coordinates."),
                "span 2, growx");
        return panel;
    }

    static JPanel practicalBusinessWidgets() {
        BookExampleSupport.requireEdt();
        JPanel panel = figurePanel("Business widget catalog",
                "[grow,fill][grow,fill]", "[][grow][grow][]");
        panel.add(tokenFieldPreview(), "grow");
        panel.add(statusAndTriStatePreview(), "grow, wrap");
        panel.add(stepPanelPreview(), "grow");
        panel.add(commandOverflowPreview(), "grow, wrap");
        panel.add(note("Reuse rule",
                "Small widgets stay maintainable when state, commands, resources, problems, and component keys are explicit."),
                "span 2, growx");
        return panel;
    }

    private static void addHeaderCell(JPanel panel, String text) {
        addHeaderCell(panel, text, "");
    }

    private static void addHeaderCell(JPanel panel, String text, String constraints) {
        JLabel label = strong(text);
        label.setOpaque(true);
        label.setBackground(new Color(0xe9eef4));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xc6d0da)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        panel.add(label, constraints);
    }

    private static void addTableRow(JPanel panel, String customer, String status, String dueDate, String amount) {
        panel.add(tableCell(customer));
        panel.add(tableCell(status));
        panel.add(tableCell(dueDate));
        panel.add(tableCell(amount), "wrap");
    }

    private static JLabel tableCell(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xe1e6eb)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return label;
    }

    private static JLabel menuRow(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return label;
    }

    private static JPanel splitButtonPreview(String text, String arrow) {
        JPanel split = new JPanel(new MigLayout("insets 0, gap 0", "[]0[26!]", "[fill]"));
        split.setOpaque(false);
        JButton main = new JButton(text);
        JButton secondary = new JButton(arrow);
        secondary.setFocusable(false);
        split.add(main, "growy");
        split.add(secondary, "growy");
        return split;
    }

    private static JPanel unitFieldPreview(String amount, String unit) {
        JPanel field = new JPanel(new MigLayout("insets 0, gap 0", "[grow,fill]0[64!]", "[fill]"));
        field.setOpaque(true);
        field.setBackground(Color.WHITE);
        field.setBorder(UIManager.getBorder("TextField.border"));
        JTextField amountField = new JTextField(amount);
        amountField.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        JButton unitButton = new JButton(unit + " v");
        unitButton.setFocusable(false);
        field.add(amountField, "grow");
        field.add(unitButton, "growy");
        return field;
    }

    private static JPanel calendarPreview() {
        JPanel calendar = cardPanel();
        calendar.setLayout(new MigLayout("fill, insets 12", "[grow]", "[][][grow]"));
        calendar.add(strong("June 2026"), "center, wrap");
        calendar.add(new JLabel("Mon   Tue   Wed   Thu   Fri   Sat   Sun"), "center, wrap");
        calendar.add(new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            int cellWidth = c.getWidth() / 7;
            int cellHeight = c.getHeight() / 5;
            int day = 1;
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 7 && day <= 30; col++) {
                    int x = col * cellWidth + 4;
                    int y = row * cellHeight + 4;
                    g.setColor(day == 16 ? new Color(0xdff2ea) : new Color(0xf8fafc));
                    g.fillRoundRect(x, y, cellWidth - 8, cellHeight - 8, 10, 10);
                    g.setColor(day == 16 ? GREEN : LINE);
                    g.drawRoundRect(x, y, cellWidth - 8, cellHeight - 8, 10, 10);
                    g.setColor(INK);
                    drawCentered(g, Integer.toString(day), new Rectangle(x, y, cellWidth - 8, cellHeight - 8));
                    day++;
                }
            }
        }), "grow");
        return calendar;
    }

    private static JPanel dateTimeSpanPreview() {
        JPanel span = cardPanel();
        span.setLayout(new MigLayout("fillx, insets 12", "[100!][grow,fill]", "[][][][][][]"));
        span.add(strong("Date-time span"), "span 2, growx, wrap");
        span.add(new JLabel("Start"));
        span.add(new JTextField("2026-06-16 09:00"), "growx, wrap");
        span.add(new JLabel("End"));
        span.add(new JTextField("2026-06-16 17:00"), "growx, wrap");
        span.add(new JLabel("Quick"));
        span.add(splitButtons("Today", "This week", "Custom"), "growx, wrap");
        span.add(new JLabel("Zone"));
        span.add(tag("Europe/Prague, end exclusive", AMBER), "growx, wrap");
        span.add(new JButton("Apply draft"), "span 2, split 2");
        span.add(new JButton("Cancel"));
        return span;
    }

    private static JPanel tokenFieldPreview() {
        JPanel panel = cardPanel();
        panel.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][]"));
        panel.add(strong("Token field"), "growx, wrap");
        JPanel tokens = new JPanel(new MigLayout("insets 0", "[][][][grow]", "[]"));
        tokens.setOpaque(false);
        tokens.add(tag("urgent", GREEN));
        tokens.add(tag("renewal", BLUE));
        tokens.add(tag("credit review", AMBER));
        tokens.add(new JTextField("add tag"), "growx");
        panel.add(tokens, "growx, wrap");
        panel.add(new JLabel("tokens + query + focused token"), "growx");
        return panel;
    }

    private static JPanel statusAndTriStatePreview() {
        JPanel panel = cardPanel();
        panel.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][]"));
        panel.add(strong("Status and nullable facts"), "growx, wrap");
        JPanel row = new JPanel(new MigLayout("insets 0", "[][][][grow]", "[]"));
        row.setOpaque(false);
        row.add(tag("Active", GREEN));
        row.add(tag("Warning", AMBER));
        row.add(new JComboBox<>(new String[] {"Unknown", "Yes", "No"}));
        panel.add(row, "growx, wrap");
        panel.add(new JLabel("severity + accessible text + explicit tri-state"), "growx");
        return panel;
    }

    private static JPanel stepPanelPreview() {
        JPanel panel = cardPanel();
        panel.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][]"));
        panel.add(strong("Wizard step panel"), "growx, wrap");
        panel.add(splitButtons("Import", "Map", "Validate", "Finish"), "growx, wrap");
        panel.add(new JLabel("current step, visited state, problems, commands"), "growx");
        return panel;
    }

    private static JPanel commandOverflowPreview() {
        JPanel panel = cardPanel();
        panel.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][]"));
        panel.add(strong("Command bar with overflow"), "growx, wrap");
        JPanel bar = new JPanel(new MigLayout("insets 0", "[][][][grow]push[]", "[]"));
        bar.setOpaque(false);
        bar.add(new JButton("Save"));
        bar.add(new JButton("Refresh"));
        bar.add(new JButton("Export"));
        bar.add(new JLabel());
        bar.add(new JButton("..."));
        panel.add(bar, "growx, wrap");
        panel.add(new JLabel("same command keys when visible or overflowed"), "growx");
        return panel;
    }

    private static JPanel splitButtons(String... labels) {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[]4[]4[]4[]", "[]"));
        panel.setOpaque(false);
        for (String label : labels) {
            panel.add(new JButton(label));
        }
        return panel;
    }

    private static void drawCheckerboard(Graphics2D graphics, int x, int y, int width, int height, int step) {
        graphics.setColor(Color.WHITE);
        graphics.fillRect(x, y, width, height);
        for (int yy = y; yy < y + height; yy += step) {
            for (int xx = x; xx < x + width; xx += step) {
                boolean dark = ((xx - x) / step + (yy - y) / step) % 2 == 0;
                graphics.setColor(dark ? new Color(0xe4e9ef) : new Color(0xf8fafc));
                graphics.fillRect(xx, yy, Math.min(step, x + width - xx), Math.min(step, y + height - yy));
            }
        }
    }

    private static void paintCompositeFlower(Graphics2D graphics) {
        graphics.setColor(BLUE);
        graphics.fill(new Ellipse2D.Double(58, 18, 78, 118));
        graphics.setColor(GREEN);
        graphics.fill(new Ellipse2D.Double(26, 58, 118, 78));
        graphics.setColor(AMBER);
        graphics.fill(new Ellipse2D.Double(82, 58, 88, 78));
        graphics.setColor(INK);
        graphics.setStroke(new java.awt.BasicStroke(1.4f));
        graphics.draw(new Ellipse2D.Double(58, 18, 78, 118));
        graphics.draw(new Ellipse2D.Double(26, 58, 118, 78));
        graphics.draw(new Ellipse2D.Double(82, 58, 88, 78));
    }

    private static void drawCompositeRule(Graphics2D graphics, String label, int rule, int x, int y) {
        BufferedImage image = new BufferedImage(176, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = image.createGraphics();
        try {
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ig.setColor(new Color(0x2f6f9f));
            ig.fill(new Ellipse2D.Double(20, 18, 96, 64));
            ig.setComposite(AlphaComposite.getInstance(rule, 0.78f));
            ig.setColor(new Color(0xb94a48));
            ig.fill(new Rectangle2D.Double(68, 28, 88, 54));
        } finally {
            ig.dispose();
        }
        graphics.setColor(new Color(0xf8fafc));
        graphics.fillRoundRect(x, y, 190, 116, 12, 12);
        graphics.setColor(LINE);
        graphics.drawRoundRect(x, y, 190, 116, 12, 12);
        drawCheckerboard(graphics, x + 7, y + 8, image.getWidth(), image.getHeight(), 14);
        graphics.drawImage(image, x + 7, y + 8, null);
        graphics.setColor(INK);
        graphics.drawString(label, x + 12, y + 108);
    }

    private static Shape localHouseShape() {
        Path2D house = new Path2D.Double();
        house.moveTo(16, 48);
        house.lineTo(16, 86);
        house.lineTo(78, 86);
        house.lineTo(78, 48);
        house.lineTo(47, 18);
        house.closePath();
        return house;
    }

    private static Shape localArrowShape() {
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(-48, -12);
        arrow.lineTo(18, -12);
        arrow.lineTo(18, -28);
        arrow.lineTo(58, 0);
        arrow.lineTo(18, 28);
        arrow.lineTo(18, 12);
        arrow.lineTo(-48, 12);
        arrow.closePath();
        return arrow;
    }

    private static void drawTransformCell(Graphics2D graphics, String label, int x, int y,
            AffineTransform transform, Shape local, Color color) {
        graphics.setColor(new Color(0xf8fafc));
        graphics.fillRoundRect(x, y, 196, 104, 12, 12);
        graphics.setColor(LINE);
        graphics.drawRoundRect(x, y, 196, 104, 12, 12);
        graphics.setColor(new Color(0xe4e9ef));
        graphics.drawLine(x + 18, y + 86, x + 176, y + 86);
        graphics.drawLine(x + 24, y + 18, x + 24, y + 92);
        Shape transformed = transform.createTransformedShape(local);
        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 72));
        graphics.fill(transformed);
        graphics.setColor(color);
        graphics.setStroke(new java.awt.BasicStroke(2.2f));
        graphics.draw(transformed);
        graphics.setColor(INK);
        graphics.drawString(label, x + 12, y + 18);
    }

    private static void drawOrderScene(Graphics2D graphics, String label, int x, int y,
            AffineTransform transform, Shape local, Color color) {
        graphics.setColor(new Color(0xf8fafc));
        graphics.fillRoundRect(x, y, 206, 164, 12, 12);
        graphics.setColor(LINE);
        graphics.drawRoundRect(x, y, 206, 164, 12, 12);
        graphics.setColor(new Color(0xe4e9ef));
        graphics.drawLine(x + 18, y + 118, x + 188, y + 118);
        graphics.drawLine(x + 58, y + 20, x + 58, y + 146);
        Graphics2D copy = (Graphics2D) graphics.create();
        try {
            copy.translate(x, y);
            Shape transformed = transform.createTransformedShape(local);
            copy.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 86));
            copy.fill(transformed);
            copy.setColor(color);
            copy.setStroke(new java.awt.BasicStroke(2.2f));
            copy.draw(transformed);
            copy.setColor(INK);
            copy.fill(new Ellipse2D.Double(115, 113, 6, 6));
        } finally {
            copy.dispose();
        }
        graphics.setColor(INK);
        graphics.drawString(label, x + 12, y + 154);
    }

    private static void drawAreaOperation(Graphics2D graphics, String operation, int x, int y, String label) {
        Shape a = new Ellipse2D.Double(x + 20, y + 20, 92, 72);
        Shape b = new Rectangle2D.Double(x + 78, y + 38, 86, 58);
        Area result = new Area(a);
        switch (operation) {
            case "add" -> result.add(new Area(b));
            case "subtract" -> result.subtract(new Area(b));
            case "intersect" -> result.intersect(new Area(b));
            case "xor" -> result.exclusiveOr(new Area(b));
            default -> throw new IllegalArgumentException(operation);
        }
        graphics.setColor(new Color(0xf8fafc));
        graphics.fillRoundRect(x, y, 190, 112, 12, 12);
        graphics.setColor(LINE);
        graphics.drawRoundRect(x, y, 190, 112, 12, 12);
        graphics.setColor(new Color(0xd9e8f7));
        graphics.fill(a);
        graphics.setColor(new Color(0xf4ded5));
        graphics.fill(b);
        graphics.setColor(new Color(0x4f8f6b, false));
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
        graphics.fill(result);
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.setColor(BLUE);
        graphics.draw(a);
        graphics.setColor(RED);
        graphics.draw(b);
        graphics.setColor(INK);
        graphics.drawString(label, x + 12, y + 106);
    }

    private static JPanel figurePanel(String title, String columns, String rows) {
        JPanel panel = new JPanel(new MigLayout("fill, insets 14, gap 10", columns, rows));
        panel.setOpaque(true);
        panel.setBackground(PANEL);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(INK);
        panel.add(titleLabel, "span, growx, wrap");
        return panel;
    }

    private static JPanel responsibility(String title, String example, String owner) {
        JPanel card = cardPanel();
        card.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][]"));
        card.add(strong(title), "growx, wrap");
        card.add(new JLabel(example), "growx, wrap");
        card.add(tag(owner, BLUE), "growx");
        return card;
    }

    private static JPanel codeCard(String title, String text) {
        JPanel card = cardPanel();
        card.setLayout(new MigLayout("fill, insets 10", "[grow]", "[][grow]"));
        card.add(strong(title), "growx, wrap");
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        card.add(area, "grow");
        return card;
    }

    private static JPanel bulletList(String title, String... bullets) {
        JPanel card = cardPanel();
        card.setLayout(new MigLayout("fillx, insets 10", "[grow]", ""));
        card.add(strong(title), "growx, wrap");
        for (String bullet : bullets) {
            card.add(new JLabel("- " + bullet), "growx, wrap");
        }
        return card;
    }

    private static JPanel note(String title, String text) {
        JPanel panel = cardPanel();
        panel.setLayout(new MigLayout("fillx, insets 10", "[][grow]", "[]"));
        panel.add(strong(title));
        panel.add(new JLabel(text), "growx");
        return panel;
    }

    private static JPanel compactForm() {
        JPanel form = new JPanel(new MigLayout("fillx, insets 10", "[][grow,fill]", "[][][]"));
        form.setOpaque(true);
        form.setBackground(Color.WHITE);
        form.add(new JLabel("Name"));
        form.add(new JTextField("Ada Lovelace"), "wrap");
        form.add(new JLabel("Credit limit"));
        form.add(new JTextField("1200.00"), "wrap");
        form.add(new JLabel("Active"));
        form.add(new JCheckBox("", true), "wrap");
        return form;
    }

    private static JTable sampleTable() {
        DefaultTableModel model = new DefaultTableModel(new Object[][] {
                {"Ada Lovelace", "Active", LocalDate.of(2026, 2, 12)},
                {"Grace Hopper", "Review", LocalDate.of(2026, 3, 4)},
                {"Katherine Johnson", "Active", LocalDate.of(2026, 4, 18)},
                {"Dorothy Vaughan", "Active", LocalDate.of(2026, 5, 6)}
        }, new Object[] {"Name", "State", "Last order"});
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return table;
    }

    private static JPanel opacityPanel(String title, boolean opaqueChild) {
        JPanel panel = cardPanel();
        panel.setLayout(new MigLayout("fill, insets 10", "[grow]", "[][grow]"));
        panel.add(strong(title), "growx, wrap");
        PaintPanel paint = new PaintPanel((g, c) -> {
            for (int x = 0; x < c.getWidth(); x += 18) {
                for (int y = 0; y < c.getHeight(); y += 18) {
                    g.setColor(((x + y) / 18) % 2 == 0 ? new Color(0xdde3ea) : Color.WHITE);
                    g.fillRect(x, y, 18, 18);
                }
            }
            if (opaqueChild) {
                g.setColor(new Color(0xf5f7fa));
                g.fillRoundRect(28, 26, c.getWidth() - 56, c.getHeight() - 52, 16, 16);
            }
            g.setColor(opaqueChild ? BLUE : AMBER);
            g.setStroke(new java.awt.BasicStroke(3f));
            g.drawRoundRect(28, 26, c.getWidth() - 56, c.getHeight() - 52, 16, 16);
            g.setColor(INK);
            g.drawString(opaqueChild ? "fills bounds" : "ancestor shows through", 48, 72);
        });
        paint.setPreferredSize(new Dimension(260, 150));
        panel.add(paint, "grow");
        return panel;
    }

    private static JPanel scaleGrid(String title, int scale) {
        JPanel panel = cardPanel();
        panel.setLayout(new MigLayout("fill, insets 10", "[grow]", "[][grow]"));
        panel.add(strong(title), "growx, wrap");
        PaintPanel canvas = new PaintPanel((g, c) -> {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            int step = 16 * scale;
            g.setColor(new Color(0xe2e7ec));
            for (int x = 12; x < c.getWidth(); x += step) {
                g.drawLine(x, 12, x, c.getHeight() - 12);
            }
            for (int y = 12; y < c.getHeight(); y += step) {
                g.drawLine(12, y, c.getWidth() - 12, y);
            }
            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(2f * scale));
            g.drawOval(52, 34, 70 * scale, 48 * scale);
            g.setColor(INK);
            g.drawString("logical grid", 36, c.getHeight() - 18);
        });
        canvas.setPreferredSize(new Dimension(250, 150));
        panel.add(canvas, "grow");
        return panel;
    }

    private static JPanel commandSurface(String title, String command, String detail) {
        JPanel card = cardPanel();
        card.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][]"));
        card.add(strong(title), "growx, wrap");
        JButton button = new JButton(command);
        card.add(button, "growx, wrap");
        card.add(new JLabel(detail), "growx");
        return card;
    }

    private static void addFocusRow(JPanel panel, String number, String label, boolean selected) {
        panel.add(tag(number, selected ? BLUE : MUTED), "right");
        JTextField field = new JTextField(label);
        if (selected) {
            field.setBorder(BorderFactory.createLineBorder(BLUE, 2));
        }
        panel.add(field, "growx, wrap");
    }

    private static JPanel lafSample(String title, boolean enabled) {
        JPanel card = cardPanel();
        card.setLayout(new MigLayout("fillx, insets 10", "[][grow]", "[][][]"));
        card.add(strong(title), "span 2, growx, wrap");
        card.add(new JLabel("Field"));
        JTextField field = new JTextField(enabled ? "Editable" : "Disabled");
        field.setEnabled(enabled);
        card.add(field, "growx, wrap");
        card.add(new JButton("Apply"), "span 2, split 2");
        JCheckBox box = new JCheckBox("Active", enabled);
        box.setEnabled(enabled);
        card.add(box, "wrap");
        return card;
    }

    private static JPanel colorSwatches() {
        JPanel panel = cardPanel();
        panel.setLayout(new MigLayout("fillx, insets 10", "[][][][][grow]", "[]"));
        panel.add(strong("UI defaults"));
        panel.add(swatch(UIManager.getColor("Panel.background"), "Panel"));
        panel.add(swatch(UIManager.getColor("Component.focusColor"), "Focus"));
        panel.add(swatch(UIManager.getColor("Actions.Blue"), "Accent"));
        panel.add(new JLabel("Use defaults before hard-coded colors."), "growx");
        return panel;
    }

    private static JPanel swatch(Color color, String label) {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[]", "[][]"));
        JPanel square = new JPanel();
        square.setBackground(color == null ? new Color(0xd0d5da) : color);
        square.setBorder(BorderFactory.createLineBorder(LINE));
        square.setPreferredSize(new Dimension(38, 18));
        panel.add(square, "wrap");
        panel.add(new JLabel(label));
        return panel;
    }

    private static JPanel arrowPanel(String label) {
        JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[grow]"));
        PaintPanel arrow = new PaintPanel((g, c) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int y = c.getHeight() / 2;
            g.setColor(BLUE);
            g.setStroke(new java.awt.BasicStroke(3f));
            g.drawLine(12, y, c.getWidth() - 18, y);
            g.fillPolygon(
                    new int[] {c.getWidth() - 18, c.getWidth() - 30, c.getWidth() - 30},
                    new int[] {y, y - 8, y + 8},
                    3);
            g.setColor(INK);
            g.drawString(label, 14, y - 10);
        });
        arrow.setPreferredSize(new Dimension(80, 80));
        panel.add(arrow, "grow");
        return panel;
    }

    private static JPanel focusTile(String number, String label) {
        JPanel card = cardPanel();
        card.setLayout(new MigLayout("fill, insets 10", "[grow]", "[][]"));
        card.add(tag(number, BLUE), "center, wrap");
        card.add(strong(label), "center");
        return card;
    }

    private static JPanel behaviorTile(String title, String detail) {
        JPanel card = cardPanel();
        card.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][]"));
        card.add(strong(title), "growx, wrap");
        card.add(new JLabel(detail), "growx");
        return card;
    }

    private static JPanel lifecycleCard(String number, String title, String type, String detail) {
        JPanel card = cardPanel();
        card.setLayout(new MigLayout("fillx, insets 10", "[grow]", "[][][][]"));
        card.add(tag(number, BLUE), "left, wrap");
        card.add(strong(title), "growx, wrap");
        card.add(new JLabel(type), "growx, wrap");
        card.add(new JLabel(detail), "growx");
        return card;
    }

    private static void drawFlowBox(Graphics2D graphics, Rectangle bounds, String title, String detail) {
        graphics.setColor(new Color(0xf9fbfd));
        graphics.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
        graphics.setColor(LINE);
        graphics.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
        graphics.setColor(INK);
        Font oldFont = graphics.getFont();
        graphics.setFont(oldFont.deriveFont(Font.BOLD, 12f));
        graphics.drawString(title, bounds.x + 12, bounds.y + 22);
        graphics.setFont(oldFont.deriveFont(11f));
        graphics.setColor(MUTED);
        graphics.drawString(detail, bounds.x + 12, bounds.y + 41);
        graphics.setFont(oldFont);
    }

    private static void drawArrow(Graphics2D graphics, int x1, int y1, int x2, int y2) {
        graphics.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int size = 8;
        int xA = (int) Math.round(x2 - size * Math.cos(angle - Math.PI / 6));
        int yA = (int) Math.round(y2 - size * Math.sin(angle - Math.PI / 6));
        int xB = (int) Math.round(x2 - size * Math.cos(angle + Math.PI / 6));
        int yB = (int) Math.round(y2 - size * Math.sin(angle + Math.PI / 6));
        graphics.fillPolygon(new int[] {x2, xA, xB}, new int[] {y2, yA, yB}, 3);
    }

    private static void drawCentered(Graphics2D graphics, String text, Rectangle bounds) {
        java.awt.FontMetrics metrics = graphics.getFontMetrics();
        int x = bounds.x + (bounds.width - metrics.stringWidth(text)) / 2;
        int y = bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent();
        graphics.drawString(text, x, y);
    }

    private static void drawDoubleArrow(Graphics2D graphics, int x1, int y1, int x2, int y2) {
        graphics.drawLine(x1, y1, x2, y2);
        if (x1 <= x2) {
            graphics.fillPolygon(new int[] {x1, x1 + 8, x1 + 8}, new int[] {y1, y1 - 5, y1 + 5}, 3);
            graphics.fillPolygon(new int[] {x2, x2 - 8, x2 - 8}, new int[] {y2, y2 - 5, y2 + 5}, 3);
        } else {
            graphics.fillPolygon(new int[] {x1, x1 - 8, x1 - 8}, new int[] {y1, y1 - 5, y1 + 5}, 3);
            graphics.fillPolygon(new int[] {x2, x2 + 8, x2 + 8}, new int[] {y2, y2 - 5, y2 + 5}, 3);
        }
    }

    private static JLabel strong(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(INK);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private static JLabel tag(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setBackground(color == null ? MUTED : color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
        return label;
    }

    private static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        Border line = BorderFactory.createLineBorder(new Color(0xd5dbe2));
        Border padding = BorderFactory.createEmptyBorder(4, 4, 4, 4);
        panel.setBorder(BorderFactory.createCompoundBorder(line, padding));
        return panel;
    }

    @FunctionalInterface
    private interface Painter {
        void paint(Graphics2D graphics, JComponent component);
    }

    @SuppressWarnings("serial")
    private static final class PaintPanel extends JComponent {
        private final Painter painter;

        private PaintPanel(Painter painter) {
            this.painter = painter;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            try {
                copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                painter.paint(copy, this);
            } finally {
                copy.dispose();
            }
        }
    }

    private static final class ValidationMarksLayerUI extends LayerUI<JComponent> {
        private static final long serialVersionUID = 1L;

        @Override
        public void paint(Graphics graphics, JComponent component) {
            super.paint(graphics, component);
            Graphics2D copy = (Graphics2D) graphics.create();
            try {
                copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                copy.setColor(RED);
                copy.setStroke(new java.awt.BasicStroke(2f));
                if (component instanceof JLayer<?> layer) {
                    Component view = layer.getView();
                    if (view != null) {
                        paintMarks(copy, component, view);
                    }
                }
            } finally {
                copy.dispose();
            }
        }

        private void paintMarks(Graphics2D graphics, JComponent root, Component current) {
            if (current instanceof JComponent jc
                    && Boolean.TRUE.equals(jc.getClientProperty("validation.error"))
                    && current.getParent() != null) {
                Rectangle bounds = SwingUtilities.convertRectangle(current.getParent(), current.getBounds(), root);
                graphics.drawRoundRect(bounds.x - 3, bounds.y - 3, bounds.width + 5, bounds.height + 5, 8, 8);
            }
            if (current instanceof Container container) {
                for (Component child : container.getComponents()) {
                    paintMarks(graphics, root, child);
                }
            }
        }
    }
}
