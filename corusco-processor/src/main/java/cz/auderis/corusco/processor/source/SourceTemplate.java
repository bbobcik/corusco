package cz.auderis.corusco.processor.source;

import org.intellij.lang.annotations.PrintFormat;

import java.util.Map;

public interface SourceTemplate {

    void replaceLocal(String placeholder, @PrintFormat String format, Object... args);
    void replaceLocal(Map<String, String> valuesByPlaceholders);

    default void replaceAll(String placeholder, @PrintFormat String format, Object... args) {
        replaceLocal(placeholder, format, args);
    }

    default void replaceAll(Map<String, String> valuesByPlaceholders) {
        replaceLocal(valuesByPlaceholders);
    }

}
