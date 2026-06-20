package cz.auderis.corusco.examples.generated;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractGeneratedFormExampleTest {

    @Test
    void abstractGeneratedFormPreservesHandwrittenDomainLogic() {
        assertThat(AbstractGeneratedFormExample.runScenario()).containsExactly(
                "GeneratedAbstractCustomerProfile",
                "Acme Industrial (inactive)",
                "review-required",
                "true",
                "abstract-customer-profile/name",
                "TEXT"
        );
    }
}
