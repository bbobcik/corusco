package cz.auderis.corusco.examples;

import java.util.List;

/**
 * Demonstrates generated field metadata from annotations.
 *
 * <p>The example inspects descriptor information that would be produced for an
 * annotated record. It focuses on stable field ids, resource keys, editor
 * kinds, and validation metadata rather than on Swing component construction.</p>
 */
public final class GeneratedMetadataExample {

    private GeneratedMetadataExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Reads generated metadata constants produced during example compilation.
     *
     * @return key and descriptor details
     */
    public static List<String> runScenario() {
        // The generated classes are ordinary source artifacts. Newcomers should
        // be able to inspect them in the build directory and see direct key,
        // resource, problem, and descriptor construction.
        return List.of(
                GeneratedCustomerEditFields.NAME.id(),
                GeneratedCustomerEditResources.NAME_LABEL.id(),
                GeneratedCustomerEditProblems.NAME_REQUIRED.id(),
                GeneratedCustomerEditDescriptors.NAME.helpTopic().id(),
                GeneratedCustomerEditDescriptors.CREDIT_LIMIT.constraints().getFirst().kind().name(),
                GeneratedCustomerEditDescriptors.AGE.constraints().getFirst().kind().name(),
                GeneratedCustomerEditDescriptors.VALID_FROM.kind().name(),
                GeneratedCustomerEditDescriptors.TYPE.kind().name(),
                GeneratedCustomerEditDescriptors.ACTIVE.kind().name()
        );
    }
}
