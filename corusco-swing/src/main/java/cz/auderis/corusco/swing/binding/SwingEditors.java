package cz.auderis.corusco.swing.binding;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.text.ParseException;

/**
 * Utilities for committing active Swing editors before form submission.
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
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner == null || !SwingUtilities.isDescendingFrom(focusOwner, root)) {
            return true;
        }
        if (focusOwner instanceof JFormattedTextField formatted) {
            try {
                formatted.commitEdit();
            } catch (ParseException e) {
                return false;
            }
        }
        if (focusOwner instanceof JSpinner.DefaultEditor editor) {
            try {
                editor.getTextField().commitEdit();
            } catch (ParseException e) {
                return false;
            }
        }
        JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, focusOwner);
        if (table != null && table.isEditing()) {
            return table.getCellEditor().stopCellEditing();
        }
        return true;
    }
}
