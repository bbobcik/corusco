package cz.auderis.corusco.core.table;

import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableCellProblemsTest {

    private static final ProblemCode REQUIRED = ProblemCode.of("required");
    private static final ProblemCode RANGE = ProblemCode.of("range");
    private static final ColumnKey<CustomerRow, String> NAME =
            ColumnKey.of("name", CustomerRow.class, String.class);
    private static final ColumnKey<CustomerRow, Integer> ORDERS =
            ColumnKey.of("orders", CustomerRow.class, Integer.class);

    @Test
    void createsTypedCellTargetsAndFiltersByColumnKey() {
        CustomerRow alice = new CustomerRow("Alice", 3);
        Problem name = Problem.validation(
                REQUIRED,
                ProblemSeverity.ERROR,
                TableCellProblems.target(alice, NAME),
                "Name required"
        );
        Problem orders = Problem.validation(
                RANGE,
                ProblemSeverity.WARNING,
                TableCellProblems.target(alice, ORDERS),
                "Review orders"
        );
        ProblemSet problems = ProblemSet.of(name, orders);

        assertThat(TableCellProblems.forCell(problems, alice, NAME).problems()).containsExactly(name);
        assertThat(TableCellProblems.forCell(problems, alice, ORDERS).problems()).containsExactly(orders);
        assertThat(TableCellProblems.forCell(problems, new CustomerRow("Bob", 1), NAME).isEmpty()).isTrue();
    }

    @Test
    void returnsMostSevereCellProblem() {
        CustomerRow alice = new CustomerRow("Alice", 3);
        Problem warning = Problem.validation(
                RANGE,
                ProblemSeverity.WARNING,
                TableCellProblems.target(alice, NAME),
                "Review"
        );
        Problem error = Problem.validation(
                REQUIRED,
                ProblemSeverity.ERROR,
                TableCellProblems.target(alice, NAME),
                "Required"
        );

        assertThat(TableCellProblems.mostSevere(ProblemSet.of(warning, error), alice, NAME)).isEqualTo(error);
    }

    private record CustomerRow(String name, int orders) {
    }
}
