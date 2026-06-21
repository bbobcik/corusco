package cz.auderis.corusco.examples.showcase;

import com.formdev.flatlaf.FlatClientProperties;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.resource.Resources;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.swing.binding.BindingFactory;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.dialog.FormDialog;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

final class CustomerWorkspacePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    final ShowcaseCustomerEditFormModel model;
    final JTextField nameField = new JTextField();
    private final JLabel formState = new JLabel("Clean baseline");

    CustomerWorkspacePanel(BindingScope scope, Resources resources, JLabel statusLabel) {
        super(new MigLayout("fill, insets 20, gap 14", "[grow][310!]", "[][grow]"));
        model = new ShowcaseCustomerEditFormModel(new ShowcaseCustomerEdit(
                "Alice Retail",
                new BigDecimal("25000.00"),
                34,
                LocalDate.parse("2026-01-01"),
                ShowcaseCustomerType.RETAIL,
                true
        ));
        JTextField creditLimit = new JTextField();
        JTextField age = new JTextField();
        JTextField validFrom = new JTextField();
        JComboBox<ShowcaseCustomerType> type = new JComboBox<>(ShowcaseCustomerType.values());
        JCheckBox active = new JCheckBox(ShowcaseUi.resolve(resources, ShowcaseCustomerEditResources.ACTIVE_LABEL));
        JLabel validation = new JLabel("No validation errors");
        validation.putClientProperty(FlatClientProperties.STYLE, "foreground:#287d3c;");
        formState.putClientProperty(FlatClientProperties.STYLE, "font: $semibold.font;");

        nameField.setToolTipText(ShowcaseUi.resolve(resources, ShowcaseCustomerEditResources.NAME_TOOLTIP));
        creditLimit.setToolTipText(ShowcaseUi.resolve(resources, ShowcaseCustomerEditResources.CREDIT_LIMIT_TOOLTIP));

        add(header(), "span, growx, wrap");
        JPanel form = new JPanel(new MigLayout("fillx, insets 16, gap 10", "[][grow]", "[][][][][][]"));
        ShowcaseUi.card(form);
        form.add(ShowcaseUi.heading("Customer Profile"), "span, growx, wrap");
        addLabeled(form, ShowcaseUi.resolve(resources, ShowcaseCustomerEditResources.NAME_LABEL), nameField);
        addLabeled(form, ShowcaseUi.resolve(resources, ShowcaseCustomerEditResources.CREDIT_LIMIT_LABEL), creditLimit);
        addLabeled(form, ShowcaseUi.resolve(resources, ShowcaseCustomerEditResources.AGE_LABEL), age);
        addLabeled(form, ShowcaseUi.resolve(resources, ShowcaseCustomerEditResources.VALID_FROM_LABEL), validFrom);
        form.add(new JLabel(ShowcaseUi.resolve(resources, ShowcaseCustomerEditResources.TYPE_LABEL)));
        form.add(type, "growx, wrap");
        form.add(active, "span, growx");
        add(form, "grow");

        JPanel side = new JPanel(new MigLayout("fillx, insets 16, gap 10", "[grow]", "[][]16[][]push[]"));
        ShowcaseUi.card(side);
        side.add(ShowcaseUi.heading("Validation"), "growx, wrap");
        side.add(validation, "growx, wrap");
        side.add(ShowcaseUi.heading("Model State"), "growx, wrap");
        side.add(formState, "growx, wrap");

        scope.add(BindingFactory.textField(nameField, model.name));
        scope.add(BindingFactory.textField(creditLimit, model.creditLimit));
        scope.add(BindingFactory.textField(age, model.age));
        scope.add(BindingFactory.textField(validFrom, model.validFrom));
        scope.add(BindingFactory.selected(active, model.active));
        scope.add(BindingFactory.validationTooltip(nameField, model.name.problemSet()));
        scope.add(BindingFactory.validationBorder(nameField, model.name.problemSet()));
        scope.add(BindingFactory.statusText(nameField, statusLabel, "Editing generated customer name."));
        scope.add(BindingFactory.statusText(creditLimit, statusLabel, "Generated decimal converter and range validation."));
        scope.add(BindingFactory.accessibleText(nameField, "Customer name", "Generated field bound to a form model."));
        var nameValueSubscription = model.name.dirty().subscribe(event ->
                refreshFormState());
        scope.add(nameValueSubscription::close);
        var nameProblemSubscription = model.name.problemSet().subscribe(event -> {
            if (event.newValue().hasErrors()) {
                validation.putClientProperty(FlatClientProperties.STYLE, "foreground:#b42318;");
                validation.setText(primaryProblemText(event.newValue()));
            } else {
                validation.putClientProperty(FlatClientProperties.STYLE, "foreground:#287d3c;");
                validation.setText("No validation errors");
            }
        });
        scope.add(nameProblemSubscription::close);
        type.setSelectedItem(model.type.value().value());
        boolean[] updatingType = new boolean[1];
        type.addActionListener(event -> {
            if (!updatingType[0]) {
                model.type.setValue((ShowcaseCustomerType) type.getSelectedItem(), StandardChangeOrigin.USER);
            }
        });
        var typeSubscription = model.type.value().subscribe(event -> {
            updatingType[0] = true;
            try {
                type.setSelectedItem(event.newValue());
            } finally {
                updatingType[0] = false;
            }
        });
        scope.add(typeSubscription::close);

        JButton dialogButton = new JButton("Open Dialog Preview");
        dialogButton.addActionListener(event -> {
            FormDialog<ShowcaseCustomerEditFormModel, ShowcaseCustomerEdit> dialog =
                    new FormDialog<>(model, new JLabel("Dialog controller preview"));
            dialog.applyCommand().execute();
            JOptionPane.showMessageDialog(
                    this,
                    "FormDialog applied: " + dialog.lastAppliedResult().orElseThrow().name(),
                    "Dialog integration",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
        side.add(dialogButton, "growx, wrap");
        side.add(ShowcaseUi.caption("Dialog apply/save uses the generated form model result."));
        add(side, "growx, growy");
    }

    void refreshFormState() {
        formState.setText(model.name.isDirty() ? "Unsaved customer changes" : "Clean baseline");
    }

    private JPanel header() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 0", "[grow][]", "[][]"));
        panel.add(ShowcaseUi.heading("Customer Workspace"), "growx");
        panel.add(ShowcaseUi.badge("Generated form"), "wrap");
        panel.add(ShowcaseUi.caption("Typed fields, converters, validation, help metadata and dialog integration."),
                "span, growx");
        return panel;
    }

    private static void addLabeled(JPanel panel, String label, JTextField field) {
        panel.add(new JLabel(label));
        panel.add(field, "growx, wrap");
    }

    private static String primaryProblemText(ProblemSet problems) {
        return problems.bySeverityDescending().stream()
                .findFirst()
                .map(problem -> problem.message())
                .orElse("No validation errors");
    }
}
