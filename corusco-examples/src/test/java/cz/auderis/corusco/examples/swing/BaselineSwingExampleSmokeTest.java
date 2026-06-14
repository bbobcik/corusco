package cz.auderis.corusco.examples.swing;

import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaselineSwingExampleSmokeTest {

    @Test
    void exampleWindowCanBeConstructedOnEdtWithoutShowingIt() throws Exception {
        AtomicReference<JInternalFrame> window = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> window.set(BaselineSwingExample.createWindow()));

        assertThat(window.get())
                .isNotNull()
                .extracting(JInternalFrame::isVisible)
                .isEqualTo(false);
        assertThat(window.get().getTitle())
                .isEqualTo("Corusco baseline");
    }
}
