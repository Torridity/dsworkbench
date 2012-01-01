/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CapabilityInfoPanel.java
 *
 * Created on Apr 23, 2011, 11:04:01 PM
 */
package de.tor.tribes.ui.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Torridity
 */
public class CapabilityInfoPanel extends javax.swing.JPanel {

    private boolean searchable = true;
    private boolean copyable = true;
    private boolean pastable = true;
    private boolean deletable = true;
    private boolean bbSupport = true;
    private ActionListener actionListener = null;
    private Component source = null;

    /** Creates new form CapabilityInfoPanel */
    public CapabilityInfoPanel() {
        initComponents();
    }

    public void addActionListener(ActionListener pActionListener) {
        addActionListener(pActionListener, null);
    }

    public void addActionListener(ActionListener pActionListener, Component pSource) {
        actionListener = pActionListener;
        source = pSource;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCopy = new javax.swing.JLabel();
        jPaste = new javax.swing.JLabel();
        jBBSupport = new javax.swing.JLabel();
        jDelete = new javax.swing.JLabel();
        jFind = new javax.swing.JLabel();

        setOpaque(false);
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_copy.png"))); // NOI18N
        jCopy.setToolTipText("<html><b>Kopieren/Ausschneiden von Eintr&auml;gen per STRG+C bzw. STRG+X</b><br/>In einigen Ansichten werden Eintr&auml;ge so kopiert,<br/>dass sie in genau diese oder eine vergleichbare Ansicht wieder eingef&uuml;gt werden k&ouml;nnen.<br/>\nIn anderen Ansichten werden Dorfkoordinaten kopiert,<br/>die an verschiedenen anderen Stellen aus der Zwischenablage gelesen werden k&ouml;nnen</html>");
        jCopy.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCopyAction(evt);
            }
        });
        add(jCopy);

        jPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/clipboard_empty.png"))); // NOI18N
        jPaste.setToolTipText("<html><b>Einf&uuml;gen von Eintr&auml;gen per STRG+V</b><br/>In eigenen Ansichten k&ouml;nnen nur spezielle Eintr&auml;ge eingef&uuml;gt werden,<br/>die vorher aus derselben Ansicht kopiert wurden.<br/>In anderen Ansichten k&ouml;nnen Dorfkoordinaten aus beliebigen Quellen eingef&uuml;gt werden</html>");
        jPaste.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                firePasteAction(evt);
            }
        });
        add(jPaste);

        jBBSupport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/bb_to_clipboard.png"))); // NOI18N
        jBBSupport.setToolTipText("<html><b>Kopieren von Eintr&auml;gen als BB-Codes per STRG+B</b><br/>In dieser Ansicht k&ouml;nnen Eintr&auml;ge als BB-Codes in die Zwischenablage kopiert werden.<br/>Von dort aus kann man sie dann per STRG+V im Spiel in Notizen, IGMs oder das Forum einf&uuml;gen.</html>");
        jBBSupport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireBBCopyAction(evt);
            }
        });
        add(jBBSupport);

        jDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_delete.png"))); // NOI18N
        jDelete.setToolTipText("<html><b>L&ouml;schen von Eintr&auml;gen per ENTF</b></html>");
        jDelete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireDeleteAction(evt);
            }
        });
        add(jDelete);

        jFind.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_find.png"))); // NOI18N
        jFind.setToolTipText("<html><b>Suchen nach Eintr&auml;gen per STRG+F</b></html>");
        jFind.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireFindAction(evt);
            }
        });
        add(jFind);
    }// </editor-fold>//GEN-END:initComponents

    private void fireCopyAction(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyAction
        fireEvent("Copy");
    }//GEN-LAST:event_fireCopyAction

    private void firePasteAction(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_firePasteAction
        fireEvent("Paste");
    }//GEN-LAST:event_firePasteAction

    private void fireBBCopyAction(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireBBCopyAction
        fireEvent("BBCopy");
    }//GEN-LAST:event_fireBBCopyAction

    private void fireDeleteAction(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDeleteAction
        fireEvent("Delete");
    }//GEN-LAST:event_fireDeleteAction

    private void fireFindAction(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireFindAction
        fireEvent("Find");
    }//GEN-LAST:event_fireFindAction

    private void fireEvent(String pEvent) {
        if (actionListener != null) {
            if (source != null) {
                actionListener.actionPerformed(new ActionEvent(source, 0, pEvent));
            } else {
                actionListener.actionPerformed(new ActionEvent(this, 0, pEvent));
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jBBSupport;
    private javax.swing.JLabel jCopy;
    private javax.swing.JLabel jDelete;
    private javax.swing.JLabel jFind;
    private javax.swing.JLabel jPaste;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the searchable
     */
    public boolean isSearchable() {
        return searchable;
    }

    /**
     * @param searchable the searchable to set
     */
    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
        jFind.setVisible(searchable);
    }

    /**
     * @return the copyable
     */
    public boolean isCopyable() {
        return copyable;
    }

    /**
     * @param copyable the copyable to set
     */
    public void setCopyable(boolean copyable) {
        this.copyable = copyable;
        jCopy.setVisible(copyable);
    }

    /**
     * @return the deletable
     */
    public boolean isDeletable() {
        return deletable;
    }

    /**
     * @param deletable the deletable to set
     */
    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
        jDelete.setVisible(deletable);
    }

    /**
     * @return the pastable
     */
    public boolean isPastable() {
        return pastable;
    }

    /**
     * @param pastable the pastable to set
     */
    public void setPastable(boolean pastable) {
        this.pastable = pastable;
        jPaste.setVisible(pastable);
    }

    /**
     * @return the bbSupport
     */
    public boolean isBbSupport() {
        return bbSupport;
    }

    /**
     * @param bbSupport the bbSupport to set
     */
    public void setBbSupport(boolean bbSupport) {
        this.bbSupport = bbSupport;
        jBBSupport.setVisible(bbSupport);
    }
}
