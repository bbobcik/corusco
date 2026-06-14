package cz.auderis.corusco.examples.testing;

import cz.auderis.corusco.core.key.ComponentKey;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.swing.behavior.BehaviorScope;
import cz.auderis.corusco.swing.behavior.StandardBehaviorKeys;
import cz.auderis.corusco.swing.behavior.StandardBehaviors;
import cz.auderis.corusco.swing.testing.SwingComponentKeys;
import cz.auderis.corusco.swing.testing.SwingMvpTester;

import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Demonstrates tester behavior-installation assertions.
 *
 * <p>The example uses the Swing MVP tester to verify which behaviors a view
 * installs and how conflicts are reported. It is aimed at tests for generated
 * or presenter-built view plans.</p>
 */
public final class SwingMvpTesterBehaviorExample {

    private static final ComponentKey<JTextField> NAME_COMPONENT =
            ComponentKey.of("customer/name-field", JTextField.class);

    private SwingMvpTesterBehaviorExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a behavior assertion scenario.
     *
     * @return diagnostics for installed and cleaned-up behavior state
     */
    public static List<String> runScenario() {
        SwingMvpTester<CustomerView, CustomerPresenter> tester = SwingMvpTester.create(
                CustomerView::new,
                CustomerPresenter::new
        );

        // Generated view plans should install behaviors through one scope owned
        // by the presenter or view lifecycle. Tests then assert stable behavior
        // keys instead of private listener or border implementation details.
        tester.assertBehaviorInstalled(NAME_COMPONENT, (view, presenter) -> presenter.scope(),
                        StandardBehaviorKeys.SELECT_ALL_ON_FOCUS)
                .assertBehaviorInstalled(NAME_COMPONENT, (view, presenter) -> presenter.scope(),
                        StandardBehaviorKeys.VALIDATION_BORDER)
                .assertBehaviorNotInstalled(NAME_COMPONENT, (view, presenter) -> presenter.scope(),
                        StandardBehaviorKeys.HELP_ON_F1);

        String installedCount = tester.queryOnEdt((view, presenter) ->
                Integer.toString(presenter.scope().installedBehaviorKeys(view.nameField).size()));

        // Closing the behavior scope removes installed-key tracking together
        // with the actual listeners/decorators owned by the scope.
        tester.runOnEdt((view, presenter) -> presenter.scope().close())
                .assertBehaviorNotInstalled(NAME_COMPONENT, (view, presenter) -> presenter.scope(),
                        StandardBehaviorKeys.SELECT_ALL_ON_FOCUS);

        return tester.queryOnEdt((view, presenter) -> List.of(
                installedCount,
                Integer.toString(presenter.scope().installedBehaviorKeys(view.nameField).size())
        ));
    }

    private static final class CustomerView extends JPanel {

        private static final long serialVersionUID = 1L;

        private final JTextField nameField = SwingComponentKeys.mark(new JTextField(), NAME_COMPONENT);

        private CustomerView() {
            add(nameField);
        }
    }

    private static final class CustomerPresenter {

        private final BehaviorScope scope = new BehaviorScope();

        private CustomerPresenter(CustomerView view) {
            scope.install(view.nameField, List.of(
                    StandardBehaviors.validationBorder(SimpleValue.of(ProblemSet.empty())),
                    StandardBehaviors.selectAllOnFocus()
            ));
        }

        private BehaviorScope scope() {
            return scope;
        }
    }
}
