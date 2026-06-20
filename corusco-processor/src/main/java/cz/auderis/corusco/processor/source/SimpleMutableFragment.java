package cz.auderis.corusco.processor.source;

import org.intellij.lang.annotations.PrintFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class SimpleMutableFragment implements SourceFragment, SourceTemplate {

    private final StringBuilder text;

    public SimpleMutableFragment() {
        this.text = new StringBuilder(32);
    }

    @Override
    public void render(Appendable target) throws IOException {
        target.append(text);
    }

    public void clear() {
        text.setLength(0);
    }

    public SimpleMutableFragment append(CharSequence csq) {
        if (null !=  csq) {
            text.append(csq);
        }
        return this;
    }

    public SimpleMutableFragment append(SourceFragment fragment) {
        Objects.requireNonNull(fragment, "fragment");
        try {
            fragment.render(text);
        } catch (IOException e) {
            throw new IllegalStateException("Could not append source fragment", e);
        }
        return this;
    }

    public SimpleMutableFragment appendFormatted(@PrintFormat String format, Object... args) {
        text.append(String.format(format, args));
        return this;
    }

    public SimpleMutableFragment appendResource(Class<?> anchor, String resourcePath) throws IOException {
        try (
                InputStream input = anchor.getResourceAsStream(resourcePath);
                InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(input, "input"), StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(reader, 8192)
        ) {
            final String newLine = System.lineSeparator();
            bufferedReader.lines().forEachOrdered(line -> {
                text.append(line);
                text.append(newLine);
            });
        }
        return this;
    }

    public String asString() {
        return text.toString();
    }

    @Override
    public void replaceLocal(String placeholder, String format, Object... args) {
        Objects.requireNonNull(placeholder, "placeholder");
        Objects.requireNonNull(format, "format");
        TextReplacement.replace(Stream.of(text), placeholder, format, args);
    }

    @Override
    public void replaceLocal(Map<String, String> valuesByPlaceholders) {
        Objects.requireNonNull(valuesByPlaceholders, "valuesByPlaceholders");
        TextReplacement.replaceMultiple(Stream.of(text), valuesByPlaceholders);
    }

    @Override
    public Object getInternalBuffer() {
        final java.util.List<StringBuilder> buffers = java.util.List.of(text);
        return new InternalBuffers(buffers, buffers);
    }

}
