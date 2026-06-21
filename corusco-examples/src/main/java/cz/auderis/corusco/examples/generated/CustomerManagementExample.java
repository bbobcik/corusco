package cz.auderis.corusco.examples.generated;

import cz.auderis.corusco.core.collection.ObservableArrayList;
import cz.auderis.corusco.core.command.ActionDescriptor;
import cz.auderis.corusco.core.command.CommandFactory;
import cz.auderis.corusco.core.command.MutableCommand;
import cz.auderis.corusco.core.dialog.DialogResult;
import cz.auderis.corusco.core.form.FormModel;
import cz.auderis.corusco.core.key.ActionKey;
import cz.auderis.corusco.core.key.FieldKey;
import cz.auderis.corusco.core.key.ResourceKey;
import cz.auderis.corusco.core.problem.Problem;
import cz.auderis.corusco.core.problem.ProblemCode;
import cz.auderis.corusco.core.problem.ProblemSet;
import cz.auderis.corusco.core.problem.ProblemSeverity;
import cz.auderis.corusco.core.problem.ProblemTarget;
import cz.auderis.corusco.core.table.Column;
import cz.auderis.corusco.core.table.ColumnCapabilities;
import cz.auderis.corusco.core.table.ColumnDefaults;
import cz.auderis.corusco.core.table.ColumnDescriptor;
import cz.auderis.corusco.core.table.ColumnKey;
import cz.auderis.corusco.core.table.InMemoryTableStateStore;
import cz.auderis.corusco.core.table.TableDescriptor;
import cz.auderis.corusco.core.table.TableKey;
import cz.auderis.corusco.core.table.TableStateStore;
import cz.auderis.corusco.core.task.TaskService;
import cz.auderis.corusco.core.validation.AsyncFieldValidation;
import cz.auderis.corusco.core.value.StandardChangeOrigin;
import cz.auderis.corusco.core.value.SimpleValue;
import cz.auderis.corusco.swing.binding.BindingScope;
import cz.auderis.corusco.swing.binding.SwingEdt;
import cz.auderis.corusco.swing.dialog.FormDialog;
import cz.auderis.corusco.swing.dialog.FormDialogValidationBinding;
import cz.auderis.corusco.swing.table.ObservableTableModel;
import cz.auderis.corusco.swing.table.TableStateController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * Demonstrates a headless miniature customer-management flow.
 *
 * <p>The example combines generated-style metadata, observable values,
 * validation, table state, commands, and resources in one small scenario. It is
 * not a full application shell; it exists to show how the core abstractions fit
 * together when editing and listing domain records.</p>
 */
public final class CustomerManagementExample {

    private static final ActionKey RESET = ActionKey.of("customer/reset");
    private static final ResourceKey<String> RESET_TEXT = ResourceKey.of("customer/reset/text", String.class);
    private static final FieldKey<VatCheck, String> VAT_ID =
            FieldKey.of("customer/vat-id", VatCheck.class, String.class);
    private static final ProblemCode VAT_INVALID = ProblemCode.of("customer/vat/invalid");

    private CustomerManagementExample() {
        throw new AssertionError("No instances");
    }

    /**
     * Runs a composed customer-management scenario without opening a native
     * window.
     *
     * @return diagnostics covering the composed MVR features
     */
    public static List<String> runScenario() {
        List<String> result = new ArrayList<>();
        SwingEdt.runAndWait(() -> {
            TableStateStore tableStateStore = new InMemoryTableStateStore();
            try (BindingScope scope = new BindingScope()) {
                addCustomerSearchTableDiagnostics(result, scope, tableStateStore);
                addDialogDiagnostics(result);
                addInvoiceTableDiagnostics(result, scope);
            }
        });
        result.addAll(asyncVatDiagnostics());
        return List.copyOf(result);
    }

    private static void addCustomerSearchTableDiagnostics(
            List<String> result,
            BindingScope scope,
            TableStateStore tableStateStore
    ) {
        ObservableArrayList<GeneratedCustomerRow> customers = ObservableArrayList.of(List.of(
                new GeneratedCustomerRow("Alice Retail", 3),
                new GeneratedCustomerRow("Bob Wholesale", 5)
        ));
        JTable table = new JTable();
        ObservableTableModel<GeneratedCustomerRow> model =
                GeneratedCustomerRowTableBindings.installModel(table, customers, scope);
        TableStateController<GeneratedCustomerRow> stateController = scope.add(
                TableStateController.install(table, model, tableStateStore)
        );

        // The search table uses generated table metadata and persists mutable
        // JTable column state by stable generated persistence ids.
        table.getColumnModel().moveColumn(1, 0);
        stateController.saveNow();
        result.add("searchRows=" + model.getRowCount());
        result.add("firstStoredColumn=" + tableStateStore.load(GeneratedCustomerRowColumns.TABLE.id())
                .orElseThrow()
                .columns()
                .getFirst()
                .id());

        // Generated help and tooltip metadata travels with descriptors, ready
        // for help/tooltip behaviors without runtime annotation scanning.
        result.add("nameHelp=" + GeneratedCustomerRowColumns.NAME_DESCRIPTOR.helpTopic().id());
        result.add("nameTooltip=" + GeneratedCustomerRowTableResources.NAME_TOOLTIP.id());
    }

    private static void addDialogDiagnostics(List<String> result) {
        GeneratedCustomerEditFormModel form = new GeneratedCustomerEditFormModel(new GeneratedCustomerEdit(
                "Alice",
                new BigDecimal("10.00"),
                30,
                LocalDate.parse("2026-01-01"),
                GeneratedCustomerType.RETAIL,
                true
        ));
        FormDialog<GeneratedCustomerEditFormModel, GeneratedCustomerEdit> dialog =
                new FormDialog<>(form, new JPanel(), form.name::isDirty, () -> true);
        JLabel summary = new JLabel();

        try (FormDialogValidationBinding validation = FormDialogValidationBinding.install(dialog, summary)) {
            // Validation summary reads the generated form model's current
            // problems; generated validators produce stable problem ids.
            form.name.setRawText("", StandardChangeOrigin.USER);
            validation.refresh();
            result.add("summary=" + summary.getText());

            MutableCommand reset = CommandFactory.command(
                    ActionDescriptor.action(RESET, RESET_TEXT),
                    command -> form.reset()
            );
            reset.execute();
            result.add("afterReset=" + form.name.value());

            // OK/save follows the dialog controller path: active editors,
            // committability, immutable result creation, and baseline accept.
            form.name.setRawText("Alicia", StandardChangeOrigin.USER);
            dialog.refreshCommandState();
            dialog.okCommand().execute();
            DialogResult<GeneratedCustomerEdit> saved = dialog.result();
            result.add("saved=" + saved.acceptedValue().orElseThrow().name());
        }

        AddressForm addressForm = new AddressForm("Prague", "Main Street");
        FormDialog<AddressForm, Address> addressDialog = new FormDialog<>(addressForm, new JPanel());
        addressDialog.applyCommand().execute();
        result.add("addressApplied=" + addressDialog.lastAppliedResult().orElseThrow().city());

        FormDialog<AddressForm, Address> cancelDialog = new FormDialog<>(new AddressForm("Brno", "Side Street"), new JPanel());
        cancelDialog.cancelCommand().execute();
        result.add("cancelled=" + cancelDialog.result().isAccepted());
    }

    private static void addInvoiceTableDiagnostics(List<String> result, BindingScope scope) {
        ObservableArrayList<InvoiceLine> lines = ObservableArrayList.of(List.of(
                new InvoiceLine("Support", 2),
                new InvoiceLine("License", 1)
        ));
        ObservableTableModel<InvoiceLine> model = ObservableTableModel.of(lines, invoiceTable());
        scope.add(model);

        // Invoice lines use the same descriptor-backed table model as generated
        // tables. Editing creates replacement immutable rows through updaters.
        model.setValueAt(3, 0, 1);
        result.add("invoiceQty=" + lines.get(0).quantity());
        result.add("invoiceColumns=" + model.getColumnCount());
    }

    private static List<String> asyncVatDiagnostics() {
        SimpleValue<String> vatId = SimpleValue.of("CZ000");
        CountDownLatch started = new CountDownLatch(1);
        try (TaskService taskService = TaskService.virtualThreads(Runnable::run)) {
            AsyncFieldValidation<VatCheck, String> validation = AsyncFieldValidation.bind(
                    VAT_ID,
                    vatId,
                    taskService,
                    (key, value, cancellation) -> {
                        started.countDown();
                        if (!"CZ12345678".equals(value)) {
                            return ProblemSet.of(Problem.validation(
                                    VAT_INVALID,
                                    ProblemSeverity.ERROR,
                                    ProblemTarget.field(key),
                                    "VAT id is not registered"
                            ));
                        }
                        return ProblemSet.empty();
                    }
            );
            await(started);
            awaitNotBusy(validation);
            List<String> result = List.of(
                    "vatProblems=" + validation.problems().value().size(),
                    "vatBusy=" + validation.busy().value()
            );
            validation.close();
            return result;
        }
    }

    private static TableDescriptor<InvoiceLine> invoiceTable() {
        Column<InvoiceLine, String> description = Column.readOnly(
                new ColumnDescriptor<>(
                        ColumnKey.of("invoice/description", InvoiceLine.class, String.class),
                        ResourceKey.of("invoice/description/header", String.class),
                        null,
                        ColumnDefaults.visible(180, 0),
                        ColumnCapabilities.readOnly()
                ),
                InvoiceLine::description
        );
        Column<InvoiceLine, Integer> quantity = Column.editable(
                new ColumnDescriptor<>(
                        ColumnKey.of("invoice/quantity", InvoiceLine.class, Integer.class),
                        ResourceKey.of("invoice/quantity/header", String.class),
                        null,
                        ColumnDefaults.visible(80, 1),
                        ColumnCapabilities.editableColumn()
                ),
                InvoiceLine::quantity,
                (line, value) -> new InvoiceLine(line.description(), value)
        );
        return new TableDescriptor<>(TableKey.of("invoice/lines", InvoiceLine.class), List.of(description, quantity));
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for async validation");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for async validation", e);
        }
    }

    private static void awaitNotBusy(AsyncFieldValidation<?, ?> validation) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            if (!validation.busy().value()) {
                return;
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for async validation", e);
            }
        }
        throw new IllegalStateException("Timed out waiting for async validation");
    }

    private record InvoiceLine(String description, int quantity) {
    }

    private record VatCheck(String vatId) {
    }

    private record Address(String city, String street) {
    }

    private static final class AddressForm implements FormModel<Address> {

        private final Address address;

        private AddressForm(String city, String street) {
            this.address = new Address(city, street);
        }

        @Override
        public ProblemSet problems() {
            return ProblemSet.empty();
        }

        @Override
        public boolean isCommittable() {
            return true;
        }

        @Override
        public void reset() {
        }

        @Override
        public void acceptCurrentValues() {
        }

        @Override
        public Address toResult() {
            return address;
        }
    }
}
