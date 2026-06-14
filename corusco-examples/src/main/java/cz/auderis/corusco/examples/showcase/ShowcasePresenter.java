package cz.auderis.corusco.examples.showcase;

import cz.auderis.corusco.annotations.command.UiAction;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Annotated presenter methods used to generate showcase command metadata.
 */
public final class ShowcasePresenter {

    private Runnable save = () -> { };
    private Runnable reset = () -> { };
    private Runnable reload = () -> { };
    private Runnable addEvent = () -> { };
    private Runnable toggleOptimized = () -> { };
    private Runnable openDocs = () -> { };

    void configure(
            Runnable save,
            Runnable reset,
            Runnable reload,
            Runnable addEvent,
            Runnable toggleOptimized,
            Runnable openDocs
    ) {
        this.save = save;
        this.reset = reset;
        this.reload = reload;
        this.addEvent = addEvent;
        this.toggleOptimized = toggleOptimized;
        this.openDocs = openDocs;
    }

    @UiAction(
            id = "showcase/save",
            tooltip = "showcase/save/tooltip",
            acceleratorKey = KeyEvent.VK_S,
            acceleratorModifiers = InputEvent.CTRL_DOWN_MASK
    )
    void save() {
        save.run();
    }

    @UiAction(
            id = "showcase/reset",
            tooltip = "showcase/reset/tooltip",
            acceleratorKey = KeyEvent.VK_R,
            acceleratorModifiers = InputEvent.CTRL_DOWN_MASK
    )
    void reset() {
        reset.run();
    }

    @UiAction(
            id = "showcase/reload",
            tooltip = "showcase/reload/tooltip",
            acceleratorKey = KeyEvent.VK_L,
            acceleratorModifiers = InputEvent.CTRL_DOWN_MASK
    )
    void reload() {
        reload.run();
    }

    @UiAction(
            id = "showcase/add-event",
            tooltip = "showcase/add-event/tooltip",
            acceleratorKey = KeyEvent.VK_E,
            acceleratorModifiers = InputEvent.CTRL_DOWN_MASK
    )
    void addEvent() {
        addEvent.run();
    }

    @UiAction(
            id = "showcase/optimized-renderers",
            text = "showcase/optimized-renderers/text",
            tooltip = "showcase/optimized-renderers/tooltip",
            selectable = true
    )
    void toggleOptimizedRenderers() {
        toggleOptimized.run();
    }

    @UiAction(
            id = "showcase/docs",
            tooltip = "showcase/docs/tooltip",
            acceleratorKey = KeyEvent.VK_D,
            acceleratorModifiers = InputEvent.CTRL_DOWN_MASK
    )
    void openDocs() {
        openDocs.run();
    }
}
