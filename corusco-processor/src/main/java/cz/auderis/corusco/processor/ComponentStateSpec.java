package cz.auderis.corusco.processor;

/**
 * Normalized generated component-state member.
 */
final class ComponentStateSpec {

    final String constantName;
    final String componentName;
    final boolean resultAccessor;
    final java.util.List<DependencySpec> dependencies;

    ComponentStateSpec(String constantName, String componentName) {
        this(constantName, componentName, false);
    }

    ComponentStateSpec(String constantName, String componentName, boolean resultAccessor) {
        this(constantName, componentName, resultAccessor, java.util.List.of());
    }

    ComponentStateSpec(
            String constantName,
            String componentName,
            boolean resultAccessor,
            java.util.List<DependencySpec> dependencies
    ) {
        this.constantName = constantName;
        this.componentName = componentName;
        this.resultAccessor = resultAccessor;
        this.dependencies = java.util.List.copyOf(dependencies);
    }
}
