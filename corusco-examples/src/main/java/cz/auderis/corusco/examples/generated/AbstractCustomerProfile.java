package cz.auderis.corusco.examples.generated;

import cz.auderis.corusco.annotations.form.CheckBox;
import cz.auderis.corusco.annotations.form.ComboBox;
import cz.auderis.corusco.annotations.form.CoruscoForm;
import cz.auderis.corusco.annotations.form.TextField;
import cz.auderis.corusco.annotations.help.Help;
import cz.auderis.corusco.annotations.validation.DecimalRange;
import cz.auderis.corusco.annotations.validation.Length;
import cz.auderis.corusco.annotations.validation.Required;
import java.math.BigDecimal;

/**
 * Abstract annotated form source with handwritten domain logic.
 *
 * <p>Abstract form sources are useful when generated form infrastructure should
 * read an existing hand-maintained source shape instead of a plain record. The
 * abstract accessors below become generated fields, descriptors, a form model,
 * Swing view contracts, and a separate immutable result implementation. The
 * concrete methods remain source-side helpers; committed generated results
 * contain only semantic field values.</p>
 */
@CoruscoForm(id = "abstract-customer-profile")
public abstract class AbstractCustomerProfile {

    private static final BigDecimal REVIEW_LIMIT = new BigDecimal("100000.00");

    /**
     * Returns the customer display name.
     *
     * @return customer name
     */
    @TextField
    @Required
    @Length(max = 80)
    @Help(topic = "abstract-customer-profile/name")
    public abstract String name();

    /**
     * Returns the approved credit limit.
     *
     * @return credit limit, or {@code null} when it has not been assigned yet
     */
    @TextField
    @DecimalRange(min = "0.00", max = "1000000.00")
    public abstract BigDecimal creditLimit();

    /**
     * Returns the customer category.
     *
     * @return customer type
     */
    @ComboBox
    public abstract GeneratedCustomerType type();

    /**
     * Returns whether the profile is active.
     *
     * @return active flag
     */
    @CheckBox
    public abstract boolean active();

    /**
     * Returns whether this profile belongs to a business customer.
     *
     * @return {@code true} for business customers
     */
    public final boolean businessCustomer() {
        return GeneratedCustomerType.BUSINESS == type();
    }

    /**
     * Returns whether the profile should be reviewed before committing it to
     * downstream systems.
     *
     * @return {@code true} when business credit exceeds the review limit
     */
    public final boolean requiresCreditReview() {
        BigDecimal limit = creditLimit();
        return businessCustomer() && (limit != null) && (limit.compareTo(REVIEW_LIMIT) >= 0);
    }

    /**
     * Returns a label that combines generated state with handwritten policy.
     *
     * @return display label
     */
    public final String displayName() {
        return active() ? name() : name() + " (inactive)";
    }

    /**
     * Returns a compact risk label for lists and audit records.
     *
     * @return review label
     */
    public final String reviewBadge() {
        return requiresCreditReview() ? "review-required" : "standard";
    }
}
