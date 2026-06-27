package cz.auderis.corusco.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoruscoDataModelTest {

    @Test
    void rangeCountAndPageValidateInvariants() {
        assertThatThrownBy(() -> new CoruscoDataRange(-1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("offset");
        assertThatThrownBy(() -> new CoruscoDataCount(CoruscoDataCount.Kind.UNKNOWN, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown");
        assertThatThrownBy(() -> new CoruscoDataPage<>(List.of("a", "b"), new CoruscoDataRange(0, 1), CoruscoDataCount.exact(2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit");

        CoruscoDataPage<String> page = new CoruscoDataPage<>(
                new ArrayList<>(List.of("a")),
                new CoruscoDataRange(5, 10),
                CoruscoDataCount.estimate(100)
        );

        assertThat(page.rows()).containsExactly("a");
        assertThatThrownBy(() -> page.rows().add("b")).isInstanceOf(UnsupportedOperationException.class);
        assertThat(page.totalCount().hasValue()).isTrue();
    }

    @Test
    void queryNormalizesSortPriorityAndCopiesCollections() {
        List<CoruscoDataFilter> filters = new ArrayList<>();
        filters.add(new CoruscoDataFilter("status", CoruscoDataFilterOperator.EQUALS, List.of("ACTIVE")));
        List<CoruscoDataSort> sort = new ArrayList<>();
        sort.add(new CoruscoDataSort("name", CoruscoDataSort.Direction.ASCENDING, 5));
        sort.add(new CoruscoDataSort("id", CoruscoDataSort.Direction.DESCENDING, 2));

        CoruscoDataQuery query = new CoruscoDataQuery(filters, sort);
        filters.clear();
        sort.clear();

        assertThat(query.filters()).hasSize(1);
        assertThat(query.sort()).containsExactly(
                new CoruscoDataSort("id", CoruscoDataSort.Direction.DESCENDING, 0),
                new CoruscoDataSort("name", CoruscoDataSort.Direction.ASCENDING, 1)
        );
    }

    @Test
    void rowIdentityExtractsTypedKey() {
        CoruscoRowIdentity<Row, Long> identity = CoruscoRowIdentity.of(Row.class, Long.class, Row::id);

        assertThat(identity.keyOf(new Row(10, "alpha"))).isEqualTo(10L);
        assertThat(identity.rowType()).isEqualTo(Row.class);
        assertThat(identity.keyType()).isEqualTo(Long.class);
    }

    @Test
    void selectionSupportsExplicitAndAllMatchingModesWithoutMaterializingAllKeys() {
        CoruscoRowSelection<Long> explicit = CoruscoRowSelection.explicit(Set.of(1L, 2L, 3L), 2L);
        CoruscoRowSelection<Long> retained = explicit.retainVisibleKeys(Set.of(2L, 4L));

        assertThat(explicit.contains(1L)).isTrue();
        assertThat(explicit.contains(9L)).isFalse();
        assertThat(retained.includedKeys()).containsExactly(2L);
        assertThat(retained.leadKey()).isEqualTo(2L);

        CoruscoRowSelection<Long> all = CoruscoRowSelection.allMatching(
                CoruscoDataQuery.EMPTY,
                Set.of(9L),
                5L
        );

        assertThat(all.contains(5L)).isTrue();
        assertThat(all.contains(9L)).isFalse();
        assertThat(all.includedKeys()).isEmpty();
        assertThat(all.retainVisibleKeys(Set.of(1L, 2L)).leadKey()).isNull();
    }

    @Test
    void selectionRejectsInconsistentState() {
        assertThatThrownBy(() -> CoruscoRowSelection.explicit(Set.of(1L), 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lead");
        assertThatThrownBy(() -> new CoruscoRowSelection<>(
                CoruscoRowSelectionMode.ALL_MATCHING_QUERY,
                null,
                Set.of(),
                Set.of(),
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("query");
    }

    private record Row(long id, String name) {
    }
}
