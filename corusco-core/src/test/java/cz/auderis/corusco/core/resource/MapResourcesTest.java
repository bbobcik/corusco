package cz.auderis.corusco.core.resource;

import cz.auderis.corusco.core.key.ResourceKey;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapResourcesTest {

    private static final ResourceKey<String> LABEL = ResourceKey.of("customer/name/label", String.class);
    private static final ResourceKey<String> TOOLTIP = ResourceKey.of("customer/name/tooltip", String.class);
    private static final ResourceKey<Integer> WIDTH = ResourceKey.of("customer/name/width", Integer.class);

    @Test
    void findsTypedResourceValues() {
        Resources resources = Resources.of(Map.of(
                LABEL.id(), "Name",
                WIDTH.id(), 160
        ));

        assertThat(resources.find(LABEL)).contains("Name");
        assertThat(resources.find(WIDTH)).contains(160);
    }

    @Test
    void returnsEmptyAndFallbackForMissingValues() {
        Resources resources = Resources.empty();

        assertThat(resources.find(TOOLTIP)).isEmpty();
        assertThat(resources.resolve(TOOLTIP, "fallback")).isEqualTo("fallback");
    }

    @Test
    void requiredMissingResourceFailsClearly() {
        Resources resources = Resources.empty();

        assertThatThrownBy(() -> resources.require(LABEL))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("Missing resource")
                .hasMessageContaining(LABEL.id());
    }

    @Test
    void wrongRuntimeTypeFailsClearly() {
        Resources resources = Resources.of(Map.of(LABEL.id(), 42));

        assertThatThrownBy(() -> resources.find(LABEL))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining(LABEL.id())
                .hasMessageContaining(String.class.getName());
    }

    @Test
    void mapResourcesTakeImmutableSnapshot() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(LABEL.id(), "Name");
        Resources resources = MapResources.of(values);

        values.put(LABEL.id(), "Changed");

        assertThat(resources.require(LABEL)).isEqualTo("Name");
    }

    @Test
    void rejectsNullIdsAndValues() {
        assertThatThrownBy(() -> MapResources.of(Map.of("valid", "value", "bad", null)))
                .isInstanceOf(NullPointerException.class);
    }
}
