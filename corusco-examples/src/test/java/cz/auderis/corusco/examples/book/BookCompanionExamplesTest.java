package cz.auderis.corusco.examples.book;

import cz.auderis.corusco.examples.bookapp.BookWorkspaceExample;
import cz.auderis.corusco.examples.components.ComponentContractExample;
import cz.auderis.corusco.examples.corusco_core.CoreValuesExample;
import cz.auderis.corusco.examples.corusco_swing.BindingBehaviorBookExample;
import cz.auderis.corusco.examples.large_data.LargeDataTableExample;
import cz.auderis.corusco.examples.miglayout.MigLayoutFormExample;
import cz.auderis.corusco.examples.modern_java.ModernJavaSwingExample;
import cz.auderis.corusco.examples.practices.PracticeComparisonExample;
import cz.auderis.corusco.examples.refresh.RefreshShellExample;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookCompanionExamplesTest {

    @Test
    void allVisualBookExamplesConstructOnEdt() throws Exception {
        assertWindow(RefreshShellExample::createWindow, "Swing refresh");
        assertWindow(ComponentContractExample::createWindow, "Component contracts");
        assertWindow(MigLayoutFormExample::createWindow, "MigLayout form");
        assertWindow(PracticeComparisonExample::createWindow, "Practice comparison");
        assertWindow(LargeDataTableExample::createWindow, "Large data");
        assertWindow(ModernJavaSwingExample::createWindow, "Modern Java");
        assertWindow(BindingBehaviorBookExample::createWindow, "Corusco Swing binding");
        assertWindow(BookWorkspaceExample::createWindow, "Book workspace");
    }

    @Test
    void coreValueScenarioPublishesExpectedAudit() {
        assertThat(CoreValuesExample.runScenario())
                .containsExactly(
                        "USER: Ada Lovelace -> Grace Hopper",
                        "MODEL: Grace Hopper -> Katherine Johnson");
    }

    private static void assertWindow(WindowFactory factory, String title) throws Exception {
        AtomicReference<JInternalFrame> frame = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> frame.set(factory.create()));
        assertThat(frame.get().getTitle()).isEqualTo(title);
        assertThat(frame.get().isVisible()).isFalse();
    }

    @FunctionalInterface
    private interface WindowFactory {
        JInternalFrame create();
    }
}
