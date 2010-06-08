/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import org.apache.log4j.Logger;

/**
 *
 * @author Jejkal
 */
public class AttackTypeCellEditor extends DefaultCellEditor {

    private static Logger logger = Logger.getLogger("AttackDialog (TypeEditor)");
    private JComboBox comboComponent = null;
    private List<ImageIcon> icons = null;

    public AttackTypeCellEditor() {
        super(new JComboBox());
        setClickCountToStart(2);
        try {
            icons = new LinkedList<ImageIcon>();
            icons.add(new ImageIcon("./graphics/icons/axe.png"));
            icons.add(new ImageIcon("./graphics/icons/snob.png"));
            icons.add(new ImageIcon("./graphics/icons/def.png"));
            icons.add(new ImageIcon("./graphics/icons/fake.png"));
            icons.add(new ImageIcon("./graphics/icons/def_fake.png"));
            comboComponent = new javax.swing.JComboBox() {

                @Override
                public void processMouseEvent(MouseEvent e) {
                    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                    if (isDisplayable() && focusOwner == this && !isPopupVisible()) {
                        showPopup();
                    }
                }

                @Override
                public void processFocusEvent(FocusEvent fe) {
                }
            };
            comboComponent.setBorder(BorderFactory.createEmptyBorder());
            comboComponent.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        stopCellEditing();
                    }
                }
            });

            comboComponent.addKeyListener(new KeyListener() {

                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyPressed(KeyEvent e) {
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        fireEditingStopped();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        fireEditingCanceled();
                    }
                }
            });

            //add content
            comboComponent.setModel(new DefaultComboBoxModel(new Object[]{0, 1, 2, 3, 4, 5}));
            comboComponent.setRenderer(new ListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = new DefaultListCellRenderer().getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    Integer type = (Integer) value;
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    if ((type == null) || (type == 0)) {
                        //no icon!?
                        ((JLabel) c).setText("-");
                        ((JLabel) c).setIcon(null);
                    } else {
                        //setBackground(selectColor);
                        ((JLabel) c).setText("");
                        ((JLabel) c).setIcon(icons.get(type - 1));
                    }
                    return c;
                }
            });
        } catch (Exception e) {
            logger.warn("Failed to load attack type icons");
            icons = null;
        }
    }

    @Override
    public Object getCellEditorValue() {
        return comboComponent.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Integer type = (Integer) value;
        comboComponent.setSelectedIndex(type);
        //comboComponent.setSelectedItem(value);
        return comboComponent;
    }
}
