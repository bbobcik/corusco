package cz.auderis.corusco.examples.showcase;

import java.util.Objects;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

final class SimpleDocumentListener implements DocumentListener {

    private final Runnable callback;

    SimpleDocumentListener(Runnable callback) {
        this.callback = Objects.requireNonNull(callback, "callback");
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        callback.run();
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        callback.run();
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        callback.run();
    }
}
