package cz.auderis.corusco.examples.forms;

import cz.auderis.corusco.examples.generated.*;

import cz.auderis.corusco.core.value.ChangeOrigin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Demonstrates a generated form model for an annotated record.
 *
 * <p>The scenario uses annotation-processor output to create typed field
 * models and commit a record result. It helps readers connect source
 * annotations, generated metadata, validation, and the core form contract.</p>
 */
public final class GeneratedFormModelExample {

    private GeneratedFormModelExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Edits a generated form model and commits it back to an immutable record.
     *
     * @return committed and validation details
     */
    public static List<String> runScenario() {
        GeneratedCustomerEdit original = new GeneratedCustomerEdit(
                "Alice",
                new BigDecimal("10.00"),
                30,
                LocalDate.parse("2026-01-01"),
                GeneratedCustomerType.RETAIL,
                true
        );
        GeneratedCustomerEditFormModel model = new GeneratedCustomerEditFormModel(original);

        // Generated models expose ordinary field members. Text fields keep raw
        // text separate from semantic values, so invalid input can be reported
        // without destroying the previous valid value.
        model.name.setRawText("", ChangeOrigin.USER);
        boolean blockedByRequiredName = !model.isCommittable();

        model.name.setRawText("Bob", ChangeOrigin.USER);
        model.creditLimit.setRawText("25.50", ChangeOrigin.USER);
        model.age.setRawText("45", ChangeOrigin.USER);
        model.active.setValue(false, ChangeOrigin.USER);
        GeneratedCustomerEdit committed = model.toResult();

        model.reset();
        return List.of(
                Boolean.toString(blockedByRequiredName),
                committed.name(),
                committed.creditLimit().toPlainString(),
                committed.age().toString(),
                Boolean.toString(committed.active()),
                model.name.value()
        );
    }
}
