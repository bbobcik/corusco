package cz.auderis.corusco.processor.source;

import java.io.IOException;

public interface SourceFragment {

    void render(Appendable target) throws IOException;

    default String asString() {
        final StringBuilder result = new StringBuilder(1024);
        try {
            render(result);
        } catch (IOException e) {
            throw new IllegalStateException("Could not render source fragment", e);
        }
        return result.toString();
    }

    default Object getInternalBuffer() {
        return null;
    }

}
