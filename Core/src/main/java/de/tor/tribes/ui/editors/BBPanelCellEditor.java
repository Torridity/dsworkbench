/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.ui.panels.BBPanel;
import de.tor.tribes.ui.views.DSWorkbenchNotepad;
import de.tor.tribes.util.interfaces.BBChangeListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Torridity
 */
public class BBPanelCellEditor extends AbstractCellEditor implements TableCellEditor {

    private BBPanel editor = null;
    private JDialog dlg = null;

    public BBPanelCellEditor(JTextField pF) {
        editor = new BBPanel(new BBChangeListener() {
            @Override
            public void fireBBChangedEvent() {
            }
        });
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return !(anEvent instanceof MouseEvent) || ((MouseEvent) anEvent).getClickCount() >= 2;
    }

    @Override
    public Object getCellEditorValue() {
        return editor.getBBCode();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value == null) {
            return editor;
        }
        editor.setBBCode((String) value);
        editor.setEditMode(true);
        editor.setSize(360, 250);
        editor.setPreferredSize(new Dimension(360, 250));
        editor.setMinimumSize(new Dimension(360, 250));
        showEditor();

        return new JLabel("Bearbeite...");
    }

    private void showEditor() {
        dlg = new JDialog(DSWorkbenchNotepad.getSingleton(), false);
        Point pos = MouseInfo.getPointerInfo().getLocation();
        pos.translate(-50, -50);
        dlg.setLocation(pos);
        dlg.setResizable(false);
        dlg.setUndecorated(true);
        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().add(editor, BorderLayout.CENTER);
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        final JButton ok = new JButton("Speichern (STRG+S)");
        ok.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                dlg.setVisible(false);
                fireEditingStopped();
            }
        });
        p.add(ok, BorderLayout.CENTER);
        
        //register save shortcut
        KeyStroke save = KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK, false);
        ok.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlg.setVisible(false);
                fireEditingStopped();
            }
        }, "Save", save, JComponent.WHEN_IN_FOCUSED_WINDOW);

        dlg.getContentPane().add(p, BorderLayout.SOUTH);
        dlg.pack();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dlg.setVisible(true);
            }
        });
    }
}
