package cz.auderis.corusco.examples;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FieldModelExampleTest {

    @Test
    void exampleShowsInvalidRawTextPreservingSemanticValue() {
        assertThat(FieldModelExample.singleFieldDiagnostics())
                .containsExactly("12,", "10.00", "true");
    }

    @Test
    void exampleShowsRecordBackedFormCommit() {
        assertThat(FieldModelExample.editCustomer())
                .isEqualTo(new FieldModelExample.CustomerEdit("Grace", new BigDecimal("20.00")));
    }
}
