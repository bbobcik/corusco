package cz.auderis.corusco.examples.book;

import cz.auderis.corusco.examples.components.ComponentContractExample;
import cz.auderis.corusco.examples.large_data.LargeDataTableExample;
import cz.auderis.corusco.examples.miglayout.MigLayoutFormExample;
import cz.auderis.corusco.examples.modern_java.ModernJavaSwingExample;
import cz.auderis.corusco.examples.practices.PracticeComparisonExample;
import cz.auderis.corusco.examples.refresh.RefreshShellExample;
import cz.auderis.corusco.examples.bookapp.BookWorkspaceExample;
import java.awt.Dimension;
import java.nio.file.Path;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Generates deterministic PNG figures consumed by the LaTeX book.
 */
public final class BookFigureGenerator {

    private BookFigureGenerator() {
        throw new AssertionError("No instances");
    }

    public static void main(String[] args) throws Exception {
        Path outputDir = args.length == 0
                ? Path.of("docs", "book", "figures")
                : Path.of(args[0]);
        SwingUtilities.invokeAndWait(() -> {
            try {
                BookExampleSupport.installLookAndFeel();
                BookScreenshotHarness.capture(RefreshShellExample.createContent(), new Dimension(720, 420),
                        outputDir.resolve("refresh-shell.png"));
                BookScreenshotHarness.capture(BookVisualFigures.refreshResponsibilities(), new Dimension(720, 300),
                        outputDir.resolve("refresh-responsibilities.png"));
                BookScreenshotHarness.capture(BookVisualFigures.refreshContractLoop(), new Dimension(760, 340),
                        outputDir.resolve("refresh-contract-loop.png"));
                BookScreenshotHarness.capture(BookVisualFigures.edtEventQueueBoundary(), new Dimension(760, 360),
                        outputDir.resolve("edt-eventqueue-boundary.png"));
                JComponent componentContent = ComponentContractExample.createContent();
                BookScreenshotHarness.capture(componentContent, new Dimension(760, 460),
                        outputDir.resolve("components-contract.png"));
                BookScreenshotHarness.capture(BookVisualFigures.componentPaintContract(), new Dimension(720, 330),
                        outputDir.resolve("components-paint-contract.png"));
                BookScreenshotHarness.capture(BookVisualFigures.componentSizeNegotiation(), new Dimension(760, 360),
                        outputDir.resolve("components-size-negotiation.png"));
                BookScreenshotHarness.capture(BookVisualFigures.componentScrollableViewport(), new Dimension(760, 360),
                        outputDir.resolve("components-scrollable-viewport.png"));
                BookScreenshotHarness.capture(MigLayoutFormExample.createContent(), new Dimension(520, 260),
                        outputDir.resolve("miglayout-form.png"));
                BookScreenshotHarness.capture(BookVisualFigures.migLayoutVocabulary(), new Dimension(760, 360),
                        outputDir.resolve("miglayout-vocabulary.png"));
                BookScreenshotHarness.capture(BookVisualFigures.migLayoutDenseScreen(), new Dimension(760, 390),
                        outputDir.resolve("miglayout-dense-screen.png"));
                BookScreenshotHarness.capture(PracticeComparisonExample.createContent(), new Dimension(560, 220),
                        outputDir.resolve("practice-comparison.png"));
                BookScreenshotHarness.capture(BookVisualFigures.practiceListenerSprawl(), new Dimension(720, 300),
                        outputDir.resolve("practice-listener-sprawl.png"));
                BookScreenshotHarness.capture(BookVisualFigures.paintingDirtyRegions(), new Dimension(720, 340),
                        outputDir.resolve("painting-dirty-regions.png"));
                BookScreenshotHarness.capture(BookVisualFigures.paintingOpacity(), new Dimension(720, 330),
                        outputDir.resolve("painting-opacity.png"));
                BookScreenshotHarness.capture(BookVisualFigures.paintingCompositeSrcOver(), new Dimension(760, 360),
                        outputDir.resolve("painting-composite-src-over.png"));
                BookScreenshotHarness.capture(BookVisualFigures.paintingCompositeGroupOpacity(), new Dimension(760, 390),
                        outputDir.resolve("painting-composite-group-opacity.png"));
                BookScreenshotHarness.capture(BookVisualFigures.paintingCompositeClearMask(), new Dimension(760, 360),
                        outputDir.resolve("painting-composite-clear-mask.png"));
                BookScreenshotHarness.capture(BookVisualFigures.paintingCompositeRuleComparison(), new Dimension(760, 430),
                        outputDir.resolve("painting-composite-rule-comparison.png"));
                BookScreenshotHarness.capture(BookVisualFigures.java2dVectorPrimitives(), new Dimension(720, 330),
                        outputDir.resolve("java2d-vector-primitives.png"));
                BookScreenshotHarness.capture(BookVisualFigures.java2dScaleGrid(), new Dimension(720, 330),
                        outputDir.resolve("java2d-scale-grid.png"));
                BookScreenshotHarness.capture(BookVisualFigures.java2dTextBaseline(), new Dimension(760, 360),
                        outputDir.resolve("java2d-text-baseline.png"));
                BookScreenshotHarness.capture(LargeDataTableExample.createContent(), new Dimension(680, 420),
                        outputDir.resolve("large-data-table.png"));
                BookScreenshotHarness.capture(BookVisualFigures.tableCoordinateSystems(), new Dimension(720, 330),
                        outputDir.resolve("table-coordinate-systems.png"));
                BookScreenshotHarness.capture(BookVisualFigures.tableRefreshIdentity(), new Dimension(760, 340),
                        outputDir.resolve("table-refresh-identity.png"));
                BookScreenshotHarness.capture(BookVisualFigures.treePathIdentity(), new Dimension(720, 330),
                        outputDir.resolve("tree-path-identity.png"));
                BookScreenshotHarness.capture(BookVisualFigures.actionsCommandSurfaces(), new Dimension(720, 300),
                        outputDir.resolve("actions-command-surfaces.png"));
                BookScreenshotHarness.capture(BookVisualFigures.actionsFocusTraversal(), new Dimension(640, 260),
                        outputDir.resolve("actions-focus-traversal.png"));
                BookScreenshotHarness.capture(BookVisualFigures.actionsCommandRouting(), new Dimension(760, 340),
                        outputDir.resolve("actions-command-routing.png"));
                BookScreenshotHarness.capture(BookVisualFigures.layersBusyOverlay(), new Dimension(720, 330),
                        outputDir.resolve("layers-busy-overlay.png"));
                BookScreenshotHarness.capture(BookVisualFigures.layersValidationOverlay(), new Dimension(720, 330),
                        outputDir.resolve("layers-validation-overlay.png"));
                BookScreenshotHarness.capture(BookVisualFigures.layersScopeCoordinate(), new Dimension(760, 340),
                        outputDir.resolve("layers-scope-coordinate.png"));
                BookScreenshotHarness.capture(BookVisualFigures.lafComponents(), new Dimension(720, 340),
                        outputDir.resolve("laf-components.png"));
                BookScreenshotHarness.capture(BookVisualFigures.lafDensity(), new Dimension(760, 380),
                        outputDir.resolve("laf-density.png"));
                BookScreenshotHarness.capture(BookVisualFigures.lafStateMatrix(), new Dimension(760, 340),
                        outputDir.resolve("laf-state-matrix.png"));
                BookScreenshotHarness.capture(BookVisualFigures.textDocumentModel(), new Dimension(720, 330),
                        outputDir.resolve("text-document-model.png"));
                BookScreenshotHarness.capture(BookVisualFigures.textValidationState(), new Dimension(640, 250),
                        outputDir.resolve("text-validation-state.png"));
                BookScreenshotHarness.capture(BookVisualFigures.textMutationPipeline(), new Dimension(760, 340),
                        outputDir.resolve("text-mutation-pipeline.png"));
                BookScreenshotHarness.capture(BookVisualFigures.geomShapeVocabulary(), new Dimension(720, 330),
                        outputDir.resolve("geom-shape-vocabulary.png"));
                BookScreenshotHarness.capture(BookVisualFigures.geomTransformAlgebra(), new Dimension(760, 280),
                        outputDir.resolve("geom-transform-algebra.png"));
                BookScreenshotHarness.capture(BookVisualFigures.geomAffineVariants(), new Dimension(760, 430),
                        outputDir.resolve("geom-affine-variants.png"));
                BookScreenshotHarness.capture(BookVisualFigures.geomTransformOrder(), new Dimension(760, 360),
                        outputDir.resolve("geom-transform-order.png"));
                BookScreenshotHarness.capture(BookVisualFigures.geomStrokedShape(), new Dimension(720, 330),
                        outputDir.resolve("geom-stroked-shape.png"));
                BookScreenshotHarness.capture(BookVisualFigures.geomAreaOperations(), new Dimension(760, 430),
                        outputDir.resolve("geom-area-operations.png"));
                BookScreenshotHarness.capture(BookVisualFigures.geomSpatialIndex(), new Dimension(720, 330),
                        outputDir.resolve("geom-spatial-index.png"));
                BookScreenshotHarness.capture(BookVisualFigures.geomFontMetrics(), new Dimension(720, 330),
                        outputDir.resolve("geom-font-metrics.png"));
                BookScreenshotHarness.capture(BookVisualFigures.geomGlyphOutline(), new Dimension(720, 330),
                        outputDir.resolve("geom-glyph-outline.png"));
                BookScreenshotHarness.capture(BookVisualFigures.geomTextTransformBounds(), new Dimension(760, 360),
                        outputDir.resolve("geom-text-transform-bounds.png"));
                BookScreenshotHarness.capture(BookVisualFigures.transferDropTarget(), new Dimension(720, 330),
                        outputDir.resolve("transfer-drop-target.png"));
                BookScreenshotHarness.capture(BookVisualFigures.transferImportProgress(), new Dimension(640, 260),
                        outputDir.resolve("transfer-import-progress.png"));
                BookScreenshotHarness.capture(BookVisualFigures.transferFlavorWorkflow(), new Dimension(760, 340),
                        outputDir.resolve("transfer-flavor-workflow.png"));
                BookScreenshotHarness.capture(BookVisualFigures.accessibilityLabels(), new Dimension(720, 300),
                        outputDir.resolve("accessibility-labels.png"));
                BookScreenshotHarness.capture(BookVisualFigures.accessibilitySemanticChannels(), new Dimension(760, 330),
                        outputDir.resolve("accessibility-semantic-channels.png"));
                BookScreenshotHarness.capture(BookVisualFigures.accessibilityFocusOrder(), new Dimension(720, 270),
                        outputDir.resolve("accessibility-focus-order.png"));
                BookScreenshotHarness.capture(BookVisualFigures.diagnosticsWatchdog(), new Dimension(720, 300),
                        outputDir.resolve("diagnostics-watchdog.png"));
                BookScreenshotHarness.capture(BookVisualFigures.diagnosticsComponentTree(), new Dimension(720, 330),
                        outputDir.resolve("diagnostics-component-tree.png"));
                BookScreenshotHarness.capture(BookVisualFigures.diagnosticsLeakLifecycle(), new Dimension(760, 360),
                        outputDir.resolve("diagnostics-leak-lifecycle.png"));
                BookScreenshotHarness.capture(BookVisualFigures.packagingPipeline(), new Dimension(720, 330),
                        outputDir.resolve("packaging-pipeline.png"));
                BookScreenshotHarness.capture(BookVisualFigures.packagingInstalledLayout(), new Dimension(760, 340),
                        outputDir.resolve("packaging-installed-layout.png"));
                BookScreenshotHarness.capture(BookVisualFigures.packagingSupportDialog(), new Dimension(720, 300),
                        outputDir.resolve("packaging-support-dialog.png"));
                BookScreenshotHarness.capture(ModernJavaSwingExample.createContent(
                                new ModernJavaSwingExample.LoadState.Loading("customers")),
                        new Dimension(420, 160), outputDir.resolve("modern-loading-state.png"));
                BookScreenshotHarness.capture(ModernJavaSwingExample.createContent(
                                new ModernJavaSwingExample.LoadState.Loaded(200)),
                        new Dimension(420, 160), outputDir.resolve("modern-loaded-state.png"));
                BookScreenshotHarness.capture(BookVisualFigures.modernTaskBoundary(), new Dimension(760, 280),
                        outputDir.resolve("modern-task-boundary.png"));
                BookScreenshotHarness.capture(BookVisualFigures.bindingFormState(), new Dimension(720, 330),
                        outputDir.resolve("binding-form-state.png"));
                BookScreenshotHarness.capture(BookVisualFigures.modelOwnershipBoundary(), new Dimension(760, 360),
                        outputDir.resolve("models-ownership-boundary.png"));
                BookScreenshotHarness.capture(BookVisualFigures.behaviorPlan(), new Dimension(720, 300),
                        outputDir.resolve("behavior-plan.png"));
                BookScreenshotHarness.capture(BookVisualFigures.coruscoTableDescriptor(), new Dimension(720, 330),
                        outputDir.resolve("corusco-table-descriptor.png"));
                BookScreenshotHarness.capture(BookVisualFigures.coruscoTableState(), new Dimension(720, 330),
                        outputDir.resolve("corusco-table-state.png"));
                BookScreenshotHarness.capture(BookVisualFigures.dialogValidation(), new Dimension(600, 330),
                        outputDir.resolve("dialog-validation.png"));
                BookScreenshotHarness.capture(BookVisualFigures.dialogActiveEditor(), new Dimension(720, 330),
                        outputDir.resolve("dialog-active-editor.png"));
                BookScreenshotHarness.capture(BookVisualFigures.dialogDirtyCancel(), new Dimension(680, 330),
                        outputDir.resolve("dialog-dirty-cancel.png"));
                BookScreenshotHarness.capture(BookVisualFigures.dialogLifecycle(), new Dimension(760, 360),
                        outputDir.resolve("dialog-lifecycle.png"));
                BookScreenshotHarness.capture(BookVisualFigures.backgroundBusyTask(), new Dimension(720, 330),
                        outputDir.resolve("background-busy-task.png"));
                BookScreenshotHarness.capture(BookVisualFigures.backgroundStatusProgress(), new Dimension(640, 260),
                        outputDir.resolve("background-status-progress.png"));
                BookScreenshotHarness.capture(BookVisualFigures.testingScreenshotHarness(), new Dimension(780, 430),
                        outputDir.resolve("testing-screenshot-harness.png"));
                BookScreenshotHarness.capture(BookVisualFigures.testingComponentKeys(), new Dimension(720, 330),
                        outputDir.resolve("testing-component-keys.png"));
                BookScreenshotHarness.capture(BookWorkspaceExample.createContent(), new Dimension(820, 500),
                        outputDir.resolve("bookapp-workspace.png"));
                BookScreenshotHarness.capture(BookVisualFigures.bookappBusyDetail(), new Dimension(820, 500),
                        outputDir.resolve("bookapp-busy-detail.png"));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
