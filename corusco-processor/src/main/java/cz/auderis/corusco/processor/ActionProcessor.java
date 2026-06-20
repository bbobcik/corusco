package cz.auderis.corusco.processor;

import cz.auderis.corusco.annotations.command.UiAction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

/**
 * Reads {@link UiAction} declarations and delegates generated action output.
 */
final class ActionProcessor {

    private final Messager messager;
    private final ActionSourceWriter writer;

    ActionProcessor(Messager messager, ActionSourceWriter writer) {
        this.messager = messager;
        this.writer = writer;
    }

    void process(RoundEnvironment roundEnv) {
        Map<TypeElement, List<ActionSpec>> actionsByOwner = new LinkedHashMap<>();
        boolean failed = false;
        for (Element element : roundEnv.getElementsAnnotatedWith(UiAction.class)) {
            if (!(element instanceof ExecutableElement method)) {
                error(element, "@UiAction is supported only on methods");
                failed = true;
                continue;
            }
            Element owner = method.getEnclosingElement();
            if (!(owner instanceof TypeElement ownerType)) {
                error(method, "@UiAction method must be enclosed by a type");
                failed = true;
                continue;
            }
            ActionSpec action = actionSpec(method);
            if (action == null) {
                failed = true;
                continue;
            }
            List<ActionSpec> actions = actionsByOwner.computeIfAbsent(ownerType, ignored -> new ArrayList<>());
            if (actions.stream().anyMatch(existing -> existing.id.equals(action.id))) {
                error(method, "Duplicate @UiAction id in " + ownerType.getSimpleName() + ": " + action.id);
                failed = true;
                continue;
            }
            actions.add(action);
        }
        if (failed) {
            return;
        }
        for (Map.Entry<TypeElement, List<ActionSpec>> entry : actionsByOwner.entrySet()) {
            writer.writeActionsClass(entry.getKey(), entry.getValue());
        }
    }

    private ActionSpec actionSpec(ExecutableElement method) {
        UiAction annotation = method.getAnnotation(UiAction.class);
        if (!method.getParameters().isEmpty()) {
            error(method, "@UiAction methods must not declare parameters");
            return null;
        }
        if (method.getReturnType().getKind() != TypeKind.VOID) {
            error(method, "@UiAction methods must return void");
            return null;
        }
        if (annotation.id().isBlank()) {
            error(method, "@UiAction id must not be blank");
            return null;
        }
        if (!isStableId(annotation.id())) {
            error(method, "@UiAction id must contain only letters, digits, dots, underscores, dashes, or slashes");
            return null;
        }
        if (!annotation.text().isBlank() && !isStableId(annotation.text())) {
            error(method, "@UiAction text must contain only letters, digits, dots, underscores, dashes, or slashes");
            return null;
        }
        if (!annotation.tooltip().isBlank() && !isStableId(annotation.tooltip())) {
            error(method, "@UiAction tooltip must contain only letters, digits, dots, underscores, dashes, or slashes");
            return null;
        }
        if (annotation.acceleratorKey() == 0 && annotation.acceleratorModifiers() != 0) {
            error(method, "@UiAction acceleratorModifiers require acceleratorKey");
            return null;
        }
        String methodName = method.getSimpleName().toString();
        String constantName = constantName(methodName);
        String textId = annotation.text().isBlank() ? annotation.id() + "/text" : annotation.text();
        String tooltipId = annotation.tooltip().isBlank() ? null : annotation.tooltip();
        return new ActionSpec(
                constantName,
                methodName,
                annotation.id(),
                textId,
                tooltipId,
                annotation.mnemonic(),
                annotation.acceleratorKey(),
                annotation.acceleratorModifiers(),
                annotation.selectable()
        );
    }

    private void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private static boolean isStableId(String value) {
        return value.matches("[A-Za-z0-9][A-Za-z0-9._/-]*");
    }

    private static String constantName(String componentName) {
        String kebab = kebab(componentName);
        return kebab.replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private static String kebab(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (!result.isEmpty()) {
                    result.append('-');
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
