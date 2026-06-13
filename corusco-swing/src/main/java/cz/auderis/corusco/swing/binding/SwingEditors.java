package cz.auderis.corusco.swing.binding;

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.text.ParseException;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Utilities for committing active Swing editors before form submission.
 *
 * <p>The helpers are EDT-bound. They commit the focus-owned formatted/spinner
 * editor when focus is inside the supplied root and also stop any actively
 * editing table below the root. The table pass matters for headless tests and
 * native focus edge cases where Swing may not expose the cell editor component
 * as the current focus owner.</p>
 */
public final class SwingEditors {

    private SwingEditors() {
    }

    /**
     * Attempts to commit the active editor inside a root component.
     *
     * @param root root component to inspect
     * @return {@code true} when no editor rejected the commit
     */
    public static boolean commitActiveEditor(JComponent root) {
        SwingEdt.requireEdt();
        Objects.requireNonNull(root, "root");
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return commitActiveEditor(root, focusOwner);
    }

    static boolean commitActiveEditor(JComponent root, Component focusOwner) {
        Objects.requireNonNull(root, "root");
        if (focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, root)
                && !commitFocusedEditor(focusOwner)) {
            return false;
        }
        return commitEditingTables(root);
    }

    private static boolean commitFocusedEditor(Component focusOwner) {
        if (focusOwner instanceof JFormattedTextField formatted) {
            try {
                formatted.commitEdit();
            } catch (ParseException e) {
                return false;
            }
        }
        JSpinner spinner = componentOrAncestor(JSpinner.class, focusOwner);
        if (spinner != null) {
            try {
                spinner.commitEdit();
            } catch (ParseException e) {
                return false;
            }
        }
        JTable table = componentOrAncestor(JTable.class, focusOwner);
        if (table != null && table.isEditing()) {
            return table.getCellEditor().stopCellEditing();
        }
        return true;
    }

    private static boolean commitEditingTables(Component component) {
        if (component instanceof JTable table && table.isEditing() && !table.getCellEditor().stopCellEditing()) {
            return false;
        }
        if (!(component instanceof Container container)) {
            return true;
        }
        for (Component child : container.getComponents()) {
            if (!commitEditingTables(child)) {
                return false;
            }
        }
        return true;
    }

    private static <T> T componentOrAncestor(Class<T> type, Component component) {
        Component current = component;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getParent();
        }
        return null;
    }
}
