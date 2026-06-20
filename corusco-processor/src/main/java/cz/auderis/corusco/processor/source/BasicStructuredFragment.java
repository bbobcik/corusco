package cz.auderis.corusco.processor.source;

import org.intellij.lang.annotations.Language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BasicStructuredFragment implements StructuredSourceFragment, SourceTemplate {

    private static final @Language("regexp") String SEPARATOR_REGEX = """
            ^ \\s* (
                /\\* \\s* <<< \\s* SPLIT \\s* >>> \\s* ={3,} \\s* \\*/   #  Java block comment: /* <<< SPLIT >>> ===== */
                |
                // \\s* <<< \\s* SPLIT \\s* >>> \\s* ={3,}               #  Java line comment:  // <<< SPLIT >>> =====
            ) \\s* $
            """;
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile(SEPARATOR_REGEX, Pattern.COMMENTS);

    private final StringBuilder header;
    private final StringBuilder trailer;
    private final InternalSectionId defaultSectionId;
    private final Map<SectionId, FragmentGroup> fragmentGroups;

    public BasicStructuredFragment() {
        this.header = new StringBuilder(16);
        this.trailer = new StringBuilder(16);
        this.defaultSectionId = new InternalSectionId("__default");
        this.fragmentGroups = new LinkedHashMap<>(4);
        fragmentGroups.put(defaultSectionId, new FragmentGroup());
    }

    @Override
    public String headerPart() {
        return header.toString();
    }

    @Override
    public void headerPart(CharSequence headerPart) {
        Objects.requireNonNull(headerPart, "header part");
        header.setLength(0);
        header.append(headerPart);
    }

    @Override
    public String trailerPart() {
        return trailer.toString();
    }

    @Override
    public void trailerPart(CharSequence trailerPart) {
        Objects.requireNonNull(trailerPart, "trailer part");
        trailer.setLength(0);
        trailer.append(trailerPart);
    }

    public void loadResource(Class<?> anchor, String resourcePath) throws IOException {
        try (
                InputStream input = anchor.getResourceAsStream(resourcePath);
                InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(input, "input"), StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(reader, 8192)
        ) {
            final String newLine = System.lineSeparator();
            final Matcher separatorMatcher = SEPARATOR_PATTERN.matcher("");
            header.setLength(0);
            String line;
            HEADER_SCAN:
            while ((line = bufferedReader.readLine()) != null) {
                separatorMatcher.reset(line);
                if (separatorMatcher.find()) {
                    break HEADER_SCAN;
                }
                header.append(line);
                header.append(newLine);
            }
            trailer.setLength(0);
            while ((line = bufferedReader.readLine()) != null) {
                trailer.append(line);
                trailer.append(newLine);
            }
        }
    }

    @Override
    public SectionId getDefaultSection() {
        return defaultSectionId;
    }

    @Override
    public SectionId addSection(String name) {
        Objects.requireNonNull(name, "name");
        final InternalSectionId sectionId = new InternalSectionId(name);
        if (fragmentGroups.containsKey(sectionId)) {
            throw new  IllegalStateException("Section already exists: " + sectionId);
        }
        fragmentGroups.put(sectionId, new FragmentGroup());
        return sectionId;
    }

    @Override
    public void clearSection(SectionId section) {
        Objects.requireNonNull(section, "section");
        final FragmentGroup fragmentGroup = fragmentGroups.get(section);
        if (null == fragmentGroup) {
            throw new  IllegalStateException("Section does not exist: " + section);
        }
        fragmentGroup.fragments().clear();
    }

    @Override
    public void addFragment(SectionId section, SourceFragment fragment) {
        Objects.requireNonNull(section, "section");
        Objects.requireNonNull(fragment, "fragment");
        final FragmentGroup sectionGroup = fragmentGroups.get(section);
        if (null == sectionGroup) {
            throw new  IllegalStateException("Section does not exist: " + section);
        }
        sectionGroup.fragments().add(fragment);
    }

    @Override
    public void addFragment(SourceFragment fragment) {
        addFragment(defaultSectionId, fragment);
    }

    @Override
    public Object getInternalBuffer() {
        return getInternalBufferObject();
    }

    @Override
    public void replaceLocal(String placeholder, String format, Object... args) {
        final Stream<StringBuilder> localBuffers = Stream.of(header, trailer);
        TextReplacement.replace(localBuffers, placeholder, format, args);
    }

    @Override
    public void replaceLocal(Map<String, String> valuesByPlaceholders) {
        final Stream<StringBuilder> localBuffers = Stream.of(header, trailer);
        TextReplacement.replaceMultiple(localBuffers, valuesByPlaceholders);
    }

    @Override
    public void replaceAll(String placeholder, String format, Object... args) {
        final Stream<StringBuilder> allBuffers = getInternalBufferObject().allBuffersStream();
        TextReplacement.replace(allBuffers, placeholder, format, args);
    }

    @Override
    public void replaceAll(Map<String, String> valuesByPlaceholders) {
        final Stream<StringBuilder> allBuffers = getInternalBufferObject().allBuffersStream();
        TextReplacement.replaceMultiple(allBuffers, valuesByPlaceholders);
    }

    @Override
    public void render(Appendable target) throws IOException {
        target.append(header);
        for (final FragmentGroup group : fragmentGroups.values()) {
            for (final SourceFragment fragment : group.fragments()) {
                fragment.render(target);
            }
        }
        target.append(trailer);
    }

    private InternalBuffers getInternalBufferObject() {
        final java.util.List<StringBuilder> localBuffers = java.util.List.of(header, trailer);
        final java.util.List<StringBuilder> groupBuffers = fragmentGroups
                .values()
                .stream()
                .flatMap(group -> group.fragments().stream())
                .flatMap(BasicStructuredFragment::fragmentBuilders)
                .toList();
        final java.util.List<StringBuilder> allBuffers = Stream.concat(localBuffers.stream(), groupBuffers.stream())
                .toList();
        return new InternalBuffers(localBuffers, allBuffers);
    }

    private static Stream<StringBuilder> fragmentBuilders(SourceFragment fragment) {
        final Object bufferObject = fragment.getInternalBuffer();
        if (bufferObject instanceof InternalBuffers buffers) {
            return buffers.allBuffersStream();
        }
        return Stream.empty();
    }

}
