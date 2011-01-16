/*
 * TestFrame.java
 *
 * Created on 18. Juni 2008, 19:12
 */
package de.tor.tribes.ui;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import javax.swing.JDesktopPane;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author  Charon
 */
public class TestFrame extends javax.swing.JFrame {

    /** Creates new form TestFrame */
    public TestFrame() {
        initComponents();


        /*  GroupableTableModel model = new GroupableTableModel();
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        jTable1.setModel(model);
        jTable1.setRowSorter(sorter);
         */

        /*   DockBar dockBar = new DockBar();
        // jTable1.setModel(new GroupableTableModel());
        /*  DockBar dockBar = new DockBar();
        for (int i = 0; i < 10; ++i) {
        URL url = DockBar.class.getResource("/" + icons[i % icons.length]);
        Image img = Toolkit.getDefaultToolkit().getImage(url);
        ImageIcon imageIcon = new ImageIcon(img);
        Image image = imageIcon.getImage();
        BufferedImage b = new BufferedImage(image.getHeight(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        b = new BufferedImage(image.getHeight(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        g = b.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        dockBar.addApplication("App " + i, b, null);
        }

        jPanel1.add(dockBar);
        dockBar.setVisible(true);
        dockBar.setFrameParent();*/
    }

    public String buildUnitTable() {
        String res = "<table width=\"100%\" style=\"border: solid 1px black; padding: 4px;background-color:#EFEBDF;\">";
        /*res += "<tr>";
        res += "<td width=\"100\">&nbsp;</td>";
        for (int i = 0; i < 13; i++) {
        res += "<td><div align=\"center\"><img src=\"" + this.getClass().getResource("/res/ui/spear.png") + "\"/></div></td>";
        }
        res += "</tr>";*/
        res += "<tr>";
        // res += "<td width=\"100\"><div align=\"center\"><b>Im Dorf:</b></div></td>";
        for (int i = 0; i < 13; i++) {
            if (i % 2 == 0) {
                res += "<td style=\"background-color:#FFFFFF;font-size:95%;font-family:Verdana\"><div align=\"center\">";
            } else {
                res += "<td style=\"font-size:95%;font-family:Verdana\"><div align=\"center\">";
            }

            res += "<img src=\"" + this.getClass().getResource("/res/ui/spear.png") + "\"/>";
            res += "<BR/>";
            res += "<font style=\"color:#AAAAAA;\">2000</font>";
            res += "<BR/>";
            res += "200";
            res += "<BR/>";
            res += "<b>100</b>";
            res += "<BR/>";
            res += "<i>12:23:43</i>";
            res += "</div>";
            res += "</td>";
        }
        res += "</tr>";
        /* res += "<tr>";
        res += "<td width=\"100\"><div align=\"center\"><b>Ausserhalb:</b></div></td>";
        for (int i = 0; i < 13; i++) {
        res += "<td><div align=\"center\">2000</div></td>";
        }
        res += "</tr>";
        res += "<td width=\"100\"><div align=\"center\"><b>Unterwegs:</b></div></td>";
        for (int i = 0; i < 13; i++) {
        res += "<td><div align=\"center\">0</div></td>";
        }
        res += "</tr>";
        res += "<td width=\"100\"><div align=\"center\"><b>Laufzeit:</b></div></td>";
        for (int i = 0; i < 13; i++) {
        res += "<td><div align=\"center\">12:23:43</div></td>";
        }
        res += "</tr>";*/
        res += "</table>";
        return res;
    }

    public String buildLuckBar(double pLuck) {
        String res = "<table cellspacing=\"0\" cellpadding=\"0\" style=\"border: solid 1px black; padding: 0px;\">";
        res += "<tr>";
        if (pLuck == 0) {
            res += "<td width=\"" + 50 + "\" height=\"12\"></td>";
            res += "<td width=\"" + 0 + "\" style=\"background-color:#FF0000;\"></td>";
            res += "<td width=\"2\" style=\"background-color:rgb(0, 0, 0)\"></td>";
            res += "<td width=\"0\" style=\"background-color:#009300\"></td>";
            res += "<td width=\"50\"></td>";
        } else if (pLuck < 0) {
            double luck = Math.abs(pLuck);
            double filled = luck / 25 * 50;
            double notFilled = 50 - filled;
            res += "<td width=\"" + notFilled + "\" height=\"12\"></td>";
            res += "<td width=\"" + filled + "\" style=\"background-color:#FF0000;\"></td>";
            res += "<td width=\"2\" style=\"background-color:rgb(0, 0, 0)\"></td>";
            res += "<td width=\"0\" style=\"background-color:#009300\"></td>";
            res += "<td width=\"50\"></td>";
        } else {
            double filled = pLuck / 25 * 50;
            double notFilled = 50 - filled;
            res += "<td width=\"50\" height=\"12\"></td>";
            res += "<td width=\"0\" style=\"background-color:#F00;\"></td>";
            res += "<td width=\"2\" style=\"background-color:rgb(0, 0, 0)\"></td>";
            res += "<td width=\"" + filled + "\" style=\"background-color:#009300\"></td>";
            res += "<td width=\"" + notFilled + "\"></td>";
        }

        res += "</tr>";
        res += "</table>";
        return res;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem2 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem3 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem4 = new javax.swing.JCheckBoxMenuItem();
        jTextField1 = new javax.swing.JTextField();

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");
        jCheckBoxMenuItem1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChange(evt);
            }
        });
        jPopupMenu1.add(jCheckBoxMenuItem1);

        jCheckBoxMenuItem2.setText("jCheckBoxMenuItem2");
        jCheckBoxMenuItem2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChange(evt);
            }
        });
        jPopupMenu1.add(jCheckBoxMenuItem2);

        jCheckBoxMenuItem3.setSelected(true);
        jCheckBoxMenuItem3.setText("jCheckBoxMenuItem3");
        jCheckBoxMenuItem3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChange(evt);
            }
        });
        jPopupMenu1.add(jCheckBoxMenuItem3);

        jCheckBoxMenuItem4.setText("jCheckBoxMenuItem4");
        jCheckBoxMenuItem4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChange(evt);
            }
        });
        jPopupMenu1.add(jCheckBoxMenuItem4);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextField1.setText("jTextField1");
        jTextField1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                update(evt);
            }
        });
        jTextField1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                posChange(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
            }
        });
        jTextField1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                propChange(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(188, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(267, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireChange(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChange
    }//GEN-LAST:event_fireChange

    private void update(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_update
        System.out.println("Update " + jTextField1.getText());
    }//GEN-LAST:event_update

    private void posChange(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_posChange
        System.out.println("Pos");
    }//GEN-LAST:event_posChange

    private void propChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_propChange
        System.out.println("Prop " + evt.getPropertyName());
    }//GEN-LAST:event_propChange

    public static void main(String args[]) {

        TestFrame f = new TestFrame();
        f.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        f.setSize(1024, 800);
        f.setVisible(true);


        //System.out.println(System.getProperty("user.dir"));

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem2;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem3;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem4;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}


