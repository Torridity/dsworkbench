/*
 * TagCellRenderer.java
 *
 * Created on 10. Oktober 2008, 15:33
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Tag;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author  Jejkal
 */
public class TagCellRenderer extends javax.swing.JPanel implements TableCellRenderer {

    private Tag mCurrentTag = null;

    // protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
    /** Creates new form TagCellRenderer */
    public TagCellRenderer() {
        initComponents();
    }

    public void setValue(Tag pTag) {
        mCurrentTag = pTag;
        jLabel1.setText(pTag.getName());
        jCheckBox1.setSelected(pTag.isShowOnMap());
    }

    public Tag getValue() {
        mCurrentTag.setName(jLabel1.getText());
        mCurrentTag.setShowOnMap(jCheckBox1.isSelected());
        return mCurrentTag;
    }

    /*  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    try {
    Tag t = (Tag) value;
    jLabel1.setText(t.getName());
    jLabel1.setIcon(new ImageIcon(t.getTagIcon()));
    jCheckBox1.setSelected(t.isShowOnMap());
    } catch (Exception e) {
    jLabel1.setText(value.toString());
    // defaultRenderer.setIcon(new ImageIcon(this.getClass().getResource("/res/forbidden.gif")));
    }
    if (isSelected) {
    jLabel1.setBackground(de.tor.tribes.util.Constants.DS_BACK);
    } else {
    jLabel1.setBackground(de.tor.tribes.util.Constants.DS_BACK_LIGHT);
    }
    return this;
    }*/
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        try {
            Tag t = (Tag) value;
            jLabel1.setText(t.getName());
            //jTextField1.setIcon(new ImageIcon(t.getTagIcon()));
            jCheckBox1.setSelected(t.isShowOnMap());
        } catch (Exception e) {
            jLabel1.setText(value.toString());
            // defaultRenderer.setIcon(new ImageIcon(this.getClass().getResource("/res/forbidden.gif")));
        }
        if (isSelected) {
            setBackground(de.tor.tribes.util.Constants.DS_BACK_LIGHT);
        } else {
            setBackground(de.tor.tribes.util.Constants.DS_BACK);
        }
        return this;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();

        jCheckBox1.setOpaque(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
