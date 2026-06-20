package cz.auderis.corusco.processor.source;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class TextReplacement {

    static void replace(Stream<StringBuilder> texts, String placeholder, String format, Object... args) {
        Objects.requireNonNull(texts, "texts");
        Objects.requireNonNull(placeholder, "placeholder");
        Objects.requireNonNull(format, "format");
        final Pattern placeholderPattern = Pattern.compile("\\b{g}" + Pattern.quote(placeholder) + "\\b{g}");
        final Matcher placeholderMatcher = placeholderPattern.matcher("");
        final var result = new StringBuilder(1024);
        final String replacement = Matcher.quoteReplacement(String.format(format, args));
        texts.forEach(text -> {
            result.setLength(0);
            placeholderMatcher.reset(text);
            while (placeholderMatcher.find()) {
                placeholderMatcher.appendReplacement(result, replacement);
            }
            placeholderMatcher.appendTail(result);
            text.setLength(0);
            text.append(result);
        });
    }

    static void replaceMultiple(Stream<StringBuilder> texts, Map<String, String> valueByPlaceholder) {
        Objects.requireNonNull(texts, "texts");
        Objects.requireNonNull(valueByPlaceholder, "valueByPlaceholder");
        if (valueByPlaceholder.isEmpty()) {
            return;
        }
        final StringBuilder patternBuilder = new StringBuilder(256);
        patternBuilder.append("\\b{g}(");
        String separator = "";
        final java.util.List<String> placeholders = valueByPlaceholder
                .keySet()
                .stream()
                .sorted(java.util.Comparator.comparingInt(String::length).reversed())
                .toList();
        for (String placeholder : placeholders) {
            patternBuilder.append(separator);
            patternBuilder.append(Pattern.quote(placeholder));
            separator = "|";
        }
        patternBuilder.append(")\\b{g}");
        final Pattern pattern = Pattern.compile(patternBuilder.toString());
        final Matcher placeholderMatcher = pattern.matcher("");
        final var result = new StringBuilder(1024);
        texts.forEach(text -> {
            result.setLength(0);
            placeholderMatcher.reset(text);
            while (placeholderMatcher.find()) {
                final String placeholder = placeholderMatcher.group(1);
                String replacement = valueByPlaceholder.get(placeholder);
                if (null == replacement) {
                    if (!valueByPlaceholder.containsKey(placeholder)) {
                        throw new IllegalArgumentException(String.format("placeholder '%s' not found", placeholder));
                    }
                    replacement = "";
                }
                placeholderMatcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
            placeholderMatcher.appendTail(result);
            text.setLength(0);
            text.append(result);
        });
    }

    private TextReplacement() {
    }

}
