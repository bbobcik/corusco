package cz.auderis.corusco.core.data.edit;

import cz.auderis.corusco.core.data.CoruscoRowIdentity;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoruscoEditSessionTest {

    @Test
    void stagesCreateUpdateDeleteInStableKeyOrderAndPublishesDirtyState() {
        CoruscoEditSession<Row, Long> session = session();
        List<Boolean> dirtyEvents = new ArrayList<>();
        List<CoruscoEditSet<Row, Long>> editEvents = new ArrayList<>();
        session.dirty().subscribe(event -> dirtyEvents.add(event.newValue()));
        session.editSet().subscribe(event -> editEvents.add(event.newValue()));

        session.create(new Row(1, "create"), StandardChangeOrigin.USER);
        session.update(new Row(2, "update"), new CoruscoVersionToken("v2"), StandardChangeOrigin.USER);
        session.delete(1L, null, StandardChangeOrigin.USER);

        assertThat(session.dirty().value()).isTrue();
        assertThat(session.editSet().value().changes()).containsExactly(
                new CoruscoEditChange<>(CoruscoEditOperation.DELETE, 1L, null, null),
                new CoruscoEditChange<>(CoruscoEditOperation.UPDATE, 2L, new Row(2, "update"), new CoruscoVersionToken("v2"))
        );
        assertThat(dirtyEvents).containsExactly(true);
        assertThat(editEvents).hasSize(3);
    }

    @Test
    void discardAndClearPublishImmutableSnapshots() {
        CoruscoEditSession<Row, Long> session = session();
        session.create(new Row(1, "one"), StandardChangeOrigin.MODEL);
        session.create(new Row(2, "two"), StandardChangeOrigin.MODEL);

        CoruscoEditSet<Row, Long> snapshot = session.editSet().value();
        session.discard(1L, StandardChangeOrigin.MODEL);

        assertThat(snapshot.changes()).hasSize(2);
        assertThat(session.editSet().value().changes()).containsExactly(
                new CoruscoEditChange<>(CoruscoEditOperation.CREATE, 2L, new Row(2, "two"), null)
        );

        session.clear(StandardChangeOrigin.MODEL);

        assertThat(session.dirty().value()).isFalse();
        assertThat(session.editSet().value().isEmpty()).isTrue();
    }

    @Test
    void changeSaveAndConflictRecordsValidateAndCopy() {
        assertThatThrownBy(() -> new CoruscoEditChange<Row, Long>(CoruscoEditOperation.CREATE, 1L, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("row");
        assertThatThrownBy(() -> new CoruscoVersionToken(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");

        CoruscoEditChange<Row, Long> change = new CoruscoEditChange<>(
                CoruscoEditOperation.UPDATE,
                1L,
                new Row(1, "one"),
                new CoruscoVersionToken("v1")
        );
        CoruscoConflict<Row, Long> conflict = new CoruscoConflict<>(1L, change, new Row(1, "server"), ProblemSet.empty());
        CoruscoSaveResult<Row, Long> result = new CoruscoSaveResult<>(
                new ArrayList<>(List.of(new Row(1, "one"))),
                new ArrayList<>(List.of(conflict)),
                ProblemSet.empty()
        );

        assertThat(result.successful()).isFalse();
        assertThatThrownBy(() -> result.savedRows().add(new Row(2, "two")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static CoruscoEditSession<Row, Long> session() {
        return new CoruscoEditSession<>(CoruscoRowIdentity.of(Row.class, Long.class, Row::id));
    }

    private record Row(long id, String name) {
    }
}
