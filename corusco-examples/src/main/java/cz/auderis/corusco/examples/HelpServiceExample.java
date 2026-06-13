package cz.auderis.corusco.examples;

import cz.auderis.corusco.core.help.DefaultHelpService;
import cz.auderis.corusco.core.help.HelpRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates toolkit-neutral help topic dispatch.
 */
public final class HelpServiceExample {

    private HelpServiceExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Opens generated descriptor help through a core service.
     *
     * @return help diagnostics
     */
    public static List<String> runScenario() {
        List<HelpRequest> requests = new ArrayList<>();
        DefaultHelpService help = new DefaultHelpService(requests::add);

        // Generated descriptors carry stable HelpTopic values. The core
        // service dispatches the topic without knowing whether a later UI
        // layer opens a browser, modal dialog, or embedded help panel.
        help.open(
                GeneratedCustomerRowColumns.NAME_DESCRIPTOR.helpTopic(),
                GeneratedCustomerRowColumns.NAME_DESCRIPTOR,
                "table-header"
        );

        HelpRequest request = help.lastRequest().orElseThrow();
        return List.of(
                request.topic().id(),
                request.contextOptional().orElse("no-context"),
                Integer.toString(requests.size())
        );
    }
}
