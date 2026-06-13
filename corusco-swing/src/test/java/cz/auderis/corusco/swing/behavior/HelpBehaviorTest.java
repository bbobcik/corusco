package cz.auderis.corusco.swing.behavior;

import cz.auderis.corusco.core.help.DefaultHelpService;
import cz.auderis.corusco.core.help.HelpRequest;
import cz.auderis.corusco.core.key.HelpTopic;
import cz.auderis.corusco.swing.binding.SwingEdt;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HelpBehaviorTest {

    private static final HelpTopic NAME_HELP = HelpTopic.of("customer/name");

    @Test
    void f1DispatchesHelpRequestThroughScopeService() {
        SwingEdt.runAndWait(() -> {
            List<HelpRequest> requests = new ArrayList<>();
            DefaultHelpService helpService = new DefaultHelpService(requests::add);
            BehaviorScope scope = new BehaviorScope(helpService);
            JTextField field = new JTextField();

            scope.install(field, List.of(StandardBehaviors.helpOnF1(NAME_HELP)));
            triggerF1(field);

            HelpRequest request = requests.getFirst();
            assertThat(request.topic()).isEqualTo(NAME_HELP);
            assertThat(request.sourceOptional()).containsSame(field);
            assertThat(request.contextOptional()).contains("F1");
            assertThat(helpService.lastRequest()).contains(request);
            scope.close();
        });
    }

    @Test
    void installingWithoutHelpServiceFailsClearly() {
        SwingEdt.runAndWait(() -> {
            BehaviorScope scope = new BehaviorScope();

            assertThatThrownBy(() -> scope.install(new JTextField(), List.of(StandardBehaviors.helpOnF1(NAME_HELP))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("HelpService");
        });
    }

    @Test
    void closeRemovesInstalledF1Binding() {
        SwingEdt.runAndWait(() -> {
            DefaultHelpService helpService = new DefaultHelpService(request -> { });
            BehaviorScope scope = new BehaviorScope(helpService);
            JTextField field = new JTextField();
            KeyStroke f1 = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0);

            scope.install(field, List.of(StandardBehaviors.helpOnF1(NAME_HELP)));
            assertThat(field.getInputMap(JComponent.WHEN_FOCUSED).get(f1)).isNotNull();

            scope.close();

            assertThat(field.getInputMap(JComponent.WHEN_FOCUSED).get(f1)).isNull();
        });
    }

    @Test
    void closeRestoresPreviousF1Binding() {
        SwingEdt.runAndWait(() -> {
            DefaultHelpService helpService = new DefaultHelpService(request -> { });
            BehaviorScope scope = new BehaviorScope(helpService);
            JTextField field = new JTextField();
            KeyStroke f1 = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0);
            Action previousAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                }
            };
            field.getInputMap(JComponent.WHEN_FOCUSED).put(f1, "previous-help");
            field.getActionMap().put("previous-help", previousAction);

            scope.install(field, List.of(StandardBehaviors.helpOnF1(NAME_HELP)));
            scope.close();

            assertThat(field.getInputMap(JComponent.WHEN_FOCUSED).get(f1)).isEqualTo("previous-help");
            assertThat(field.getActionMap().get("previous-help")).isSameAs(previousAction);
        });
    }

    private static void triggerF1(JTextField field) {
        KeyStroke f1 = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0);
        Object actionKey = field.getInputMap(JComponent.WHEN_FOCUSED).get(f1);
        Action action = field.getActionMap().get(actionKey);
        action.actionPerformed(new ActionEvent(field, ActionEvent.ACTION_PERFORMED, "help"));
    }
}
