package cz.auderis.corusco.examples.generated;

import cz.auderis.corusco.core.value.ChangeOrigin;
import java.math.BigDecimal;
import java.util.List;

/**
 * Demonstrates a mixed handwritten/generated abstract form source.
 *
 * <p>The scenario starts with an abstract domain-facing form type, lets the
 * processor supply the immutable implementation and form model, and then uses
 * handwritten methods on the committed result. This is the non-record variant
 * of the generated form workflow.</p>
 */
public final class AbstractGeneratedFormExample {

    private AbstractGeneratedFormExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Edits a generated model for an abstract form source.
     *
     * @return generated implementation and handwritten-policy details
     */
    public static List<String> runScenario() {
        AbstractCustomerProfile original = new GeneratedAbstractCustomerProfile(
                "Acme",
                new BigDecimal("75000.00"),
                GeneratedCustomerType.BUSINESS,
                true
        );
        AbstractCustomerProfileFormModel model = new AbstractCustomerProfileFormModel(original);

        model.name.setRawText("Acme Industrial", ChangeOrigin.USER);
        model.creditLimit.setRawText("150000.00", ChangeOrigin.USER);
        model.active.setValue(false, ChangeOrigin.USER);
        AbstractCustomerProfile committed = model.toResult();

        return List.of(
                committed.getClass().getSimpleName(),
                committed.displayName(),
                committed.reviewBadge(),
                Boolean.toString(committed instanceof AbstractCustomerProfile),
                Boolean.toString(new GeneratedAbstractCustomerProfile(
                        "Acme Industrial",
                        new BigDecimal("150000.00"),
                        GeneratedCustomerType.BUSINESS,
                        false
                ).equals(committed)),
                AbstractCustomerProfileFields.NAME.id(),
                AbstractCustomerProfileDescriptors.CREDIT_LIMIT.kind().name()
        );
    }
}
