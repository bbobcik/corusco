package cz.auderis.corusco.processor;

import cz.auderis.corusco.processor.source.BasicStructuredFragment;
import cz.auderis.corusco.processor.source.SimpleMutableFragment;
import java.io.IOException;
import java.util.Map;

/**
 * Loads generated-source templates bundled with the processor.
 */
final class ProcessorSourceTemplates {

    private static final String TEMPLATE_ROOT = "templates/";

    static BasicStructuredFragment structuredClass(
            Class<?> anchor,
            String resourceName,
            String packageName,
            Map<String, String> values
    ) {
        final BasicStructuredFragment source = new BasicStructuredFragment();
        try {
            source.loadResource(anchor, TEMPLATE_ROOT + resourceName);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read generated source template " + resourceName, e);
        }
        source.replaceLocal("PACKAGE_DECLARATION", packageDeclaration(packageName));
        source.replaceLocal(values);
        return source;
    }

    static SimpleMutableFragment fragment(Class<?> anchor, String resourceName, Map<String, String> values) {
        final SimpleMutableFragment fragment = new SimpleMutableFragment();
        try {
            fragment.appendResource(anchor, TEMPLATE_ROOT + resourceName);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read generated source template " + resourceName, e);
        }
        fragment.replaceLocal(values);
        return fragment;
    }

    static String privateConstructorSource(Class<?> anchor, String generatedType) {
        return fragment(anchor, "common/private-constructor.javafragment", Map.of(
                "GENERATED_TYPE", generatedType
        )).asString();
    }

    static String packageDeclaration(String packageName) {
        if (packageName.isEmpty()) {
            return "";
        }
        return "package " + packageName + ";" + System.lineSeparator() + System.lineSeparator();
    }

    private ProcessorSourceTemplates() {
    }

}
