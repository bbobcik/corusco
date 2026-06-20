package cz.auderis.corusco.processor;

import cz.auderis.corusco.processor.source.BasicStructuredFragment;
import cz.auderis.corusco.processor.source.SimpleMutableFragment;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Writes generated command/action companions.
 */
final class ActionSourceWriter {

    private final Elements elements;
    private final Filer filer;
    private final Messager messager;

    ActionSourceWriter(Elements elements, Filer filer, Messager messager) {
        this.elements = elements;
        this.filer = filer;
        this.messager = messager;
    }

    void writeActionsClass(TypeElement ownerType, List<ActionSpec> actions) {
        String packageName = elements.getPackageOf(ownerType).getQualifiedName().toString();
        String ownerName = ownerType.getSimpleName().toString();
        String generatedType = ownerName + "Actions";
        String qualifiedName = packageName.isEmpty() ? generatedType : packageName + "." + generatedType;
        BasicStructuredFragment source = ProcessorSourceTemplates.structuredClass(
                ActionSourceWriter.class,
                "action/class.javafragment",
                packageName,
                Map.of(
                        "OWNER_TYPE", ownerName,
                        "GENERATED_TYPE", generatedType
                )
        );
        source.addFragment(ProcessorSourceTemplates.privateConstructorSource(ActionSourceWriter.class, generatedType));
        for (ActionSpec action : actions) {
            source.addFragment(actionSource(action));
        }
        source.addFragment(actionDescriptorListSource(actions));
        source.addFragment(commandFactorySource(ownerName, actions));
        writeSource(ownerType, qualifiedName, source.asString(), "Could not write generated action descriptors");
    }

    private String actionSource(ActionSpec action) {
        BasicStructuredFragment source = new BasicStructuredFragment();
        try {
            source.loadResource(ActionSourceWriter.class, "templates/action/action.javafragment");
        } catch (IOException e) {
            throw new IllegalStateException("Could not read generated source template action/action.javafragment", e);
        }
        source.replaceLocal(Map.of(
                "ACTION_ID", action.id,
                "CONSTANT_NAME", action.constantName,
                "ACTION_ID_LITERAL", stringLiteralOrNull(action.id),
                "TEXT_ID", action.textId,
                "TEXT_ID_LITERAL", stringLiteralOrNull(action.textId)
        ));
        if (action.tooltipId != null) {
            source.addFragment(ProcessorSourceTemplates.fragment(ActionSourceWriter.class, "action/tooltip.javafragment", Map.of(
                    "TOOLTIP_ID", action.tooltipId,
                    "CONSTANT_NAME", action.constantName,
                    "TOOLTIP_ID_LITERAL", stringLiteralOrNull(action.tooltipId)
            )));
        }
        source.replaceLocal("ACTION_DESCRIPTOR", "%s", actionDescriptorExpression(action));
        return source.asString();
    }

    private String actionDescriptorListSource(List<ActionSpec> actions) {
        SimpleMutableFragment entries = new SimpleMutableFragment();
        for (int i = 0; i < actions.size(); i++) {
            String suffix = i == actions.size() - 1 ? "\n" : ",\n";
            entries.appendFormatted("                %s%s", actions.get(i).constantName, suffix);
        }
        return ProcessorSourceTemplates.fragment(ActionSourceWriter.class, "action/descriptor-list.javafragment", Map.of(
                "DESCRIPTOR_ENTRIES", entries.asString()
        )).asString();
    }

    private String commandFactorySource(String ownerName, List<ActionSpec> actions) {
        SimpleMutableFragment source = new SimpleMutableFragment();
        for (ActionSpec action : actions) {
            source.append(singleCommandFactorySource(ownerName, action));
        }
        SimpleMutableFragment entries = new SimpleMutableFragment();
        for (int i = 0; i < actions.size(); i++) {
            String suffix = i == actions.size() - 1 ? "\n" : ",\n";
            entries.appendFormatted("                %s(owner)%s", factoryMethodName(actions.get(i)), suffix);
        }
        source.append(ProcessorSourceTemplates.fragment(ActionSourceWriter.class, "action/command-set.javafragment", Map.of(
                "OWNER_TYPE", ownerName,
                "COMMAND_ENTRIES", entries.asString()
        )));
        return source.asString();
    }

    private String singleCommandFactorySource(String ownerName, ActionSpec action) {
        String factory = action.selectable ? "toggle(" + action.constantName + ", false, command -> owner."
                : "command(" + action.constantName + ", command -> owner.";
        return ProcessorSourceTemplates.fragment(ActionSourceWriter.class, "action/command-factory.javafragment", Map.of(
                "OWNER_TYPE", ownerName,
                "METHOD_NAME", action.methodName,
                "FACTORY_METHOD", factoryMethodName(action),
                "FACTORY_EXPRESSION", factory + action.methodName + "())"
        )).asString();
    }

    private String actionDescriptorExpression(ActionSpec action) {
        String expression = action.selectable
                ? "ActionDescriptor.toggle(" + action.constantName + "_KEY, " + action.constantName + "_TEXT)"
                : "ActionDescriptor.action(" + action.constantName + "_KEY, " + action.constantName + "_TEXT)";
        if (action.tooltipId != null) {
            expression += ".withTooltip(" + action.constantName + "_TOOLTIP)";
        }
        if (action.mnemonic != 0) {
            expression += ".withMnemonic(" + action.mnemonic + ")";
        }
        if (action.acceleratorKey != 0) {
            expression += ".withAccelerator(AcceleratorDescriptor.of("
                    + action.acceleratorKey + ", " + action.acceleratorModifiers + "))";
        }
        return expression;
    }

    private static String factoryMethodName(ActionSpec action) {
        return action.methodName + "Command";
    }

    private void writeSource(TypeElement originatingType, String qualifiedName, String source, String errorPrefix) {
        try {
            JavaFileObject sourceFile = filer.createSourceFile(qualifiedName, originatingType);
            try (Writer writer = sourceFile.openWriter()) {
                writer.write(source);
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, errorPrefix + ": " + e.getMessage(), originatingType);
        }
    }

    private static String stringLiteralOrNull(String value) {
        return value == null ? "null" : "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
