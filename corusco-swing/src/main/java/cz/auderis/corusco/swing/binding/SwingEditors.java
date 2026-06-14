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
 * Utilities for flushing active Swing editor widgets before a form is submitted.
 *
 * <p>Swing components such as {@link JFormattedTextField}, {@link JSpinner},
 * and {@link JTable} cell editors can hold a value that has not yet reached the
 * presentation model. Dialog controllers use this helper before creating an
 * accepted result so the committed domain value reflects what the user is
 * currently editing, not just the last focus-lost update.</p>
 *
 * <p>The helpers are EDT-bound and do not own any component. They inspect only
 * editors inside the supplied root, commit the focused formatted or spinner
 * editor, and also stop actively editing tables below the root. A return value
 * of {@code false} means at least one editor rejected the commit; callers
 * should keep the dialog open and leave focus/error handling to the component
 * or binding that rejected the value.</p>
 */
public final class SwingEditors {

    private SwingEditors() {
    }

    /**
     * Attempts to commit the active editor inside a root component.
     *
     * <p>The root is retained only for the duration of the call. The method
     * mutates Swing editor state by committing or stopping active editing, but
     * it does not install listeners or change component ownership.</p>
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
