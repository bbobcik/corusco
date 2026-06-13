package cz.auderis.corusco.core.problem;

import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.key.FieldKey;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProblemModelTest {

    private static final FieldKey<CustomerEdit, String> NAME =
            FieldKey.of("customer/name", CustomerEdit.class, String.class);
    private static final FieldKey<CustomerEdit, BigDecimal> CREDIT_LIMIT =
            FieldKey.of("customer/credit-limit", CustomerEdit.class, BigDecimal.class);
    private static final ComponentKey<ViewTextField> NAME_FIELD =
            ComponentKey.of("customer/name-field", ViewTextField.class);
    private static final ProblemCode REQUIRED = ProblemCode.of("required");
    private static final ProblemCode RANGE = ProblemCode.of("range");
    private static final ProblemCode DUPLICATE = ProblemCode.of("duplicate");

    @Test
    void problemTargetsCoverFormFieldRowCellAndComponent() {
        ProblemTarget.Form form = ProblemTarget.form();
        ProblemTarget.Field<CustomerEdit, String> field = ProblemTarget.field(NAME);
        ProblemTarget.Row<String> row = ProblemTarget.row("row-1");
        ProblemTarget.Cell<String, String> cell = ProblemTarget.cell("row-1", "amount");
        ProblemTarget.Component<ViewTextField> component = ProblemTarget.component(NAME_FIELD);

        assertThat(form).isInstanceOf(ProblemTarget.Form.class);
        assertThat(field.key()).isEqualTo(NAME);
        assertThat(row.row()).isEqualTo("row-1");
        assertThat(cell.column()).isEqualTo("amount");
        assertThat(component.key()).isEqualTo(NAME_FIELD);
    }

    @Test
    void invalidIdentityIdsAreRejected() {
        assertThatThrownBy(() -> ProblemCode.of(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must not be blank");
        assertThatThrownBy(() -> ProblemSource.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must not be blank");
    }

    @Test
    void severityOrderingIsLeastToMostSevere() {
        assertThat(ProblemSeverity.INFO)
                .isLessThan(ProblemSeverity.WARNING)
                .isLessThan(ProblemSeverity.ERROR);
    }

    @Test
    void problemSetIsImmutableAndPreservesInsertionOrder() {
        Problem first = Problem.validation(REQUIRED, ProblemSeverity.ERROR, ProblemTarget.field(NAME), "Name required");
        Problem second = Problem.validation(RANGE, ProblemSeverity.WARNING, ProblemTarget.field(CREDIT_LIMIT), "Review limit");
        ProblemSet empty = ProblemSet.empty();

        ProblemSet one = empty.add(first);
        ProblemSet two = one.add(second);

        assertThat(empty.problems()).isEmpty();
        assertThat(one.problems()).containsExactly(first);
        assertThat(two.problems()).containsExactly(first, second);
        assertThat(two.hasErrors()).isTrue();
    }

    @Test
    void filteringCoversSupportedDimensions() {
        Problem name = Problem.validation(REQUIRED, ProblemSeverity.ERROR, ProblemTarget.field(NAME), "Name required");
        Problem credit = Problem.validation(RANGE, ProblemSeverity.WARNING, ProblemTarget.field(CREDIT_LIMIT), "Review limit");
        Problem form = Problem.validation(DUPLICATE, ProblemSeverity.ERROR, ProblemTarget.form(), "Duplicate customer");
        Problem row = Problem.validation(REQUIRED, ProblemSeverity.ERROR, ProblemTarget.row("row-1"), "Row invalid");
        Problem cell = Problem.validation(RANGE, ProblemSeverity.ERROR, ProblemTarget.cell("row-1", "amount"), "Bad amount");
        Problem component = Problem.validation(REQUIRED, ProblemSeverity.INFO, ProblemTarget.component(NAME_FIELD), "Focus hint");
        Problem server = new Problem(DUPLICATE, ProblemSeverity.ERROR, ProblemTarget.form(), ProblemSource.SERVER, "Server");
        ProblemSet set = ProblemSet.of(name, credit, form, row, cell, component, server);

        assertThat(set.filter(ProblemFilter.field(NAME)).problems()).containsExactly(name);
        assertThat(set.filter(ProblemFilter.form()).problems()).containsExactly(form, server);
        assertThat(set.filter(ProblemFilter.severity(ProblemSeverity.WARNING)).problems()).containsExactly(credit);
        assertThat(set.filter(ProblemFilter.severityAtLeast(ProblemSeverity.WARNING)).problems())
                .containsExactly(name, credit, form, row, cell, server);
        assertThat(set.filter(ProblemFilter.row("row-1")).problems()).containsExactly(row);
        assertThat(set.filter(ProblemFilter.cell("row-1", "amount")).problems()).containsExactly(cell);
        assertThat(set.filter(ProblemFilter.component(NAME_FIELD)).problems()).containsExactly(component);
        assertThat(set.filter(ProblemFilter.source(ProblemSource.SERVER)).problems()).containsExactly(server);
    }

    @Test
    void filtersCanBeComposed() {
        Problem warning = Problem.validation(RANGE, ProblemSeverity.WARNING, ProblemTarget.field(CREDIT_LIMIT), "Review");
        Problem error = Problem.validation(REQUIRED, ProblemSeverity.ERROR, ProblemTarget.field(NAME), "Required");
        ProblemSet set = ProblemSet.of(warning, error);

        ProblemFilter fieldErrors = ProblemFilter.field(NAME)
                .and(ProblemFilter.severityAtLeast(ProblemSeverity.ERROR));

        assertThat(set.filter(fieldErrors).problems()).containsExactly(error);
    }

    @Test
    void severityViewIsDeterministic() {
        Problem info = Problem.validation(REQUIRED, ProblemSeverity.INFO, ProblemTarget.component(NAME_FIELD), "Hint");
        Problem warning = Problem.validation(RANGE, ProblemSeverity.WARNING, ProblemTarget.field(CREDIT_LIMIT), "Review");
        Problem error = Problem.validation(REQUIRED, ProblemSeverity.ERROR, ProblemTarget.field(NAME), "Required");

        assertThat(ProblemSet.of(info, warning, error).bySeverityDescending())
                .containsExactly(error, warning, info);
    }

    private record CustomerEdit(String name, BigDecimal creditLimit) {
    }

    private static final class ViewTextField {
    }
}
