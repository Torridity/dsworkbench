/*
 * TestFrame.java
 *
 * Created on 18. Juni 2008, 19:12
 */
package de.tor.tribes.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author  Charon
 */
public class TestFrame extends javax.swing.JFrame {

    public static String icons[] = new String[]{
        "res/ui/icon.png", "res/ui/icon.png", "res/ui/icon.png", "res/ui/icon.png"};
    private MyTableModel model = null;

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(339, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireChange(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChange
    }//GEN-LAST:event_fireChange

    public static void main(String args[]) {

        TestFrame f = new TestFrame();
        f.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        f.setSize(300, 100);
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

class MyTableModel extends AbstractTableModel {

    /** Vector of Object[], this are the datas of the table */
    Vector datas = new Vector();
    /** Indicates which columns are visible */
    boolean[] columnsVisible = new boolean[4];
    /** Column names */
    String[] columnsName = {
        "0", "1", "2", "3"
    };

    /** Constructor */
    public MyTableModel() {
        columnsVisible[0] = true;
        columnsVisible[1] = false;
        columnsVisible[2] = true;
        columnsVisible[3] = false;
    }

    public void changeVis(boolean[] newVis) {
        columnsVisible[0] = newVis[0];
        columnsVisible[1] = newVis[1];
        columnsVisible[2] = newVis[2];
        columnsVisible[3] = newVis[3];
        for (boolean b : columnsVisible) {
            System.out.println(b);
        }
        System.out.println("---");
    }

    /**
     * This functiun converts a column number in the table
     * to the right number of the datas.
     */
    protected int getNumber(int col) {
        int n = col;    // right number to return
        int i = 0;
        do {
            if (!(columnsVisible[i])) {
                n++;
            }
            i++;
        } while (i < n);
        // If we are on an invisible column,
        // we have to go one step further
        while (!(columnsVisible[n])) {
            n++;
        }
        return n;
    }

    // *** TABLE MODEL METHODS ***
    public int getColumnCount() {
        int n = 0;
        for (int i = 0; i < 4; i++) {
            if (columnsVisible[i]) {
                n++;
            }
        }
        return n;
    }

    public int getRowCount() {
        return datas.size();
    }

    public Object getValueAt(int row, int col) {
        Object[] array = (Object[]) (datas.elementAt(row));
        return array[getNumber(col)];
    }

    public String getColumnName(int col) {
        return columnsName[getNumber(col)];
    }
}
