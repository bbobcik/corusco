package cz.auderis.corusco.core.table;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableStateStoreTest {

    private Preferences preferencesRoot;

    @AfterEach
    void removePreferencesRoot() throws BackingStoreException {
        if (preferencesRoot != null) {
            preferencesRoot.removeNode();
            Preferences.userRoot().flush();
        }
    }

    @Test
    void inMemoryStoreRoundTripsImmutableStateSnapshots() {
        TableStateStore store = new InMemoryTableStateStore();
        TableState state = customerState();

        store.save(state);

        Optional<TableState> loaded = store.load("customers");
        assertThat(loaded).contains(state);
        assertThat(loaded.orElseThrow().columns()).containsExactlyElementsOf(state.columns());
        store.flush();
    }

    @Test
    void inMemoryStoreRemovesStateByTableId() {
        TableStateStore store = new InMemoryTableStateStore();
        store.save(customerState());

        store.remove("customers");

        assertThat(store.load("customers")).isEmpty();
    }

    @Test
    void preferencesStoreRoundTripsAcrossStoreInstances() {
        Preferences root = preferencesRoot();
        TableState state = customerState();
        TableStateStore first = new PreferencesTableStateStore(root);

        first.save(state);
        first.flush();

        TableStateStore second = new PreferencesTableStateStore(root);
        assertThat(second.load("customers")).contains(state);
    }

    @Test
    void preferencesStoreRemovesStateByTableId() {
        Preferences root = preferencesRoot();
        TableStateStore store = new PreferencesTableStateStore(root);
        store.save(customerState());

        store.remove("customers");
        store.flush();

        assertThat(store.load("customers")).isEmpty();
    }

    @Test
    void preferencesStoreRejectsMalformedState() {
        Preferences root = preferencesRoot();
        Preferences node = root.node(encodedNodeName("customers"));
        node.putInt("version", 1);
        node.put("tableId", "customers");
        node.putInt("columns.count", 1);
        node.put("columns.0.id", "customers/name");
        node.put("columns.0.width", "not-a-number");
        node.putInt("columns.0.order", 0);
        node.putBoolean("columns.0.visible", true);
        node.putInt("sort.count", 0);

        TableStateStore store = new PreferencesTableStateStore(root);

        assertThatThrownBy(() -> store.load("customers"))
                .isInstanceOf(TableStateStoreException.class)
                .hasMessageContaining("Malformed table state for customers")
                .hasMessageContaining("invalid integer key columns.0.width");
    }

    private Preferences preferencesRoot() {
        preferencesRoot = Preferences.userRoot()
                .node("/cz/auderis/corusco/tests/table-state/" + UUID.randomUUID());
        return preferencesRoot;
    }

    private static TableState customerState() {
        return new TableState(
                "customers",
                List.of(
                        new ColumnState("customers/name", 180, 0, true),
                        new ColumnState("customers/orders", 90, 1, false)
                ),
                List.of(new SortState("customers/name", SortDirection.ASCENDING, 0))
        );
    }

    private static String encodedNodeName(String tableId) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tableId.getBytes(StandardCharsets.UTF_8));
    }
}
