package cz.auderis.corusco.examples.book;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Deterministic screenshot helper for book figures.
 */
public final class BookScreenshotHarness {

    private BookScreenshotHarness() {
        throw new AssertionError("No instances");
    }

    public static Path capture(JComponent component, Dimension size, Path output) throws IOException {
        BookExampleSupport.requireEdt();
        component.setSize(size);
        layoutTree(component);

        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            component.printAll(graphics);
        } finally {
            graphics.dispose();
        }

        Files.createDirectories(output.getParent());
        ImageIO.write(image, "png", output.toFile());
        return output;
    }

    private static void layoutTree(Container container) {
        container.doLayout();
        for (java.awt.Component child : container.getComponents()) {
            if (child instanceof Container childContainer) {
                layoutTree(childContainer);
            }
        }
    }

    public static void captureOnEdt(JComponent component, Dimension size, Path output)
            throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                capture(component, size, output);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
