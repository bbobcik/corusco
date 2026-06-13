package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.resource.Resources;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates typed resource lookup with generated resource keys.
 */
public final class ResourcesExample {

    private ResourcesExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Resolves generated table resource keys through a core resource provider.
     *
     * @return resource diagnostics
     */
    public static List<String> runScenario() {
        Resources resources = Resources.of(Map.of(
                GeneratedCustomerRowTableResources.NAME_HEADER.id(), "Customer",
                GeneratedCustomerRowTableResources.NAME_TOOLTIP.id(), "Customer display name"
        ));

        // Generated companions provide typed ResourceKey instances; the
        // resource provider stores only stable ids at the boundary.
        String header = resources.require(GeneratedCustomerRowTableResources.NAME_HEADER);

        // Optional and fallback lookup let tooltip composition decide whether
        // missing generated resources should disappear or use default text.
        String tooltip = resources.resolve(GeneratedCustomerRowTableResources.NAME_TOOLTIP, "no tooltip");
        String missing = resources.resolve(
                ResourceKey.of("generated-customer-table/orders/help", String.class),
                "orders fallback"
        );

        return List.of(header, tooltip, missing);
    }
}
