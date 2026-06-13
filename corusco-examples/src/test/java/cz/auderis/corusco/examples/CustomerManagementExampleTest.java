package cz.auderis.corusco.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerManagementExampleTest {

    @Test
    void runsCustomerManagementScenario() {
        assertThat(CustomerManagementExample.runScenario()).containsExactly(
                "searchRows=2",
                "firstStoredColumn=generated-customer-table/orders",
                "nameHelp=generated-customer-table/name",
                "nameTooltip=generated-customer-table/name/help",
                "summary=generated-customer/name/required",
                "afterReset=Alice",
                "saved=Alicia",
                "addressApplied=Prague",
                "cancelled=false",
                "invoiceQty=3",
                "invoiceColumns=2",
                "vatProblems=1",
                "vatBusy=false"
        );
    }
}
