package cz.auderis.corusco.examples.generated;

import cz.auderis.corusco.core.value.ChangeOrigin;
import java.math.BigDecimal;
import java.util.List;

/**
 * Demonstrates a mixed handwritten/generated abstract form source.
 *
 * <p>The scenario starts with an abstract source contract, lets the processor
 * supply the form model and immutable semantic result, and keeps source-side
 * helper methods separate from the committed result. This is the non-record
 * variant of the generated form workflow.</p>
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
        AbstractCustomerProfile original = new SourceProfile(
                "Acme",
                new BigDecimal("75000.00"),
                GeneratedCustomerType.BUSINESS,
                true
        );
        AbstractCustomerProfileFormModel model = new AbstractCustomerProfileFormModel(original);

        model.name.setRawText("Acme Industrial", ChangeOrigin.USER);
        model.creditLimit.setRawText("150000.00", ChangeOrigin.USER);
        model.active.setValue(false, ChangeOrigin.USER);
        GeneratedAbstractCustomerProfile committed = model.toResult();

        return List.of(
                committed.getClass().getSimpleName(),
                displayName(committed),
                reviewBadge(committed),
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

    private static String displayName(GeneratedAbstractCustomerProfile profile) {
        return profile.active() ? profile.name() : profile.name() + " (inactive)";
    }

    private static String reviewBadge(GeneratedAbstractCustomerProfile profile) {
        BigDecimal limit = profile.creditLimit();
        boolean requiresReview = (GeneratedCustomerType.BUSINESS == profile.type())
                && (limit != null)
                && (limit.compareTo(new BigDecimal("100000.00")) >= 0);
        return requiresReview ? "review-required" : "standard";
    }

    private static final class SourceProfile extends AbstractCustomerProfile {

        private final String name;
        private final BigDecimal creditLimit;
        private final GeneratedCustomerType type;
        private final boolean active;

        private SourceProfile(String name, BigDecimal creditLimit, GeneratedCustomerType type, boolean active) {
            this.name = name;
            this.creditLimit = creditLimit;
            this.type = type;
            this.active = active;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public BigDecimal creditLimit() {
            return creditLimit;
        }

        @Override
        public GeneratedCustomerType type() {
            return type;
        }

        @Override
        public boolean active() {
            return active;
        }
    }
}
