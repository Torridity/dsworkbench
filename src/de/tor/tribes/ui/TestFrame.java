/*
 * TestFrame.java
 *
 * Created on 18. Juni 2008, 19:12
 */
package de.tor.tribes.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author  Charon
 */
public class TestFrame extends javax.swing.JFrame {

    public static String icons[] = new String[]{
        "res/ui/icon.png", "res/ui/icon.png", "res/ui/icon.png", "res/ui/icon.png"};

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
        String res = "";
        try {
            BufferedReader r = new BufferedReader(new FileReader(new File("templ.tmpl")));
            String line = "";
            while ((line = r.readLine()) != null) {
                res += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        res = res.replaceAll("\\$ATTACKER_TABLE", buildUnitTable());
        res = res.replaceAll("\\$DEFENDER_TABLE", buildUnitTable());
        res = res.replaceAll("\\$LUCK_STRING", "Gl&uuml;ck (aus Sicht des Angreifers)");
        double luck = -25.0;
        res = res.replaceAll("\\$LUCK_BAR", buildLuckBar(luck));
        res = res.replaceAll("\\$LUCK_NEG", ((luck < 0) ? "<b>" + Double.toString(luck) + "</b>" : ""));
        res = res.replaceAll("\\$LUCK_POS", ((luck >= 0) ? "<b>" + Double.toString(luck) + "</b>" : ""));
        res = res.replaceAll("\\$LUCK_ICON1", "<img src=\"" + ((luck <= 0) ? this.getClass().getResource("/res/rabe.png") : this.getClass().getResource("/res/rabe_grau.png")) + "\"/>");
        res = res.replaceAll("\\$LUCK_ICON2", "<img src=\"" + ((luck >= 0) ? this.getClass().getResource("/res/klee.png") : this.getClass().getResource("/res/klee_grau.png")) + "\"/>");
        jLabel1.setToolTipText(res);
    }

    public String buildUnitTable() {
        String res = "<table width=\"100%\" style=\"border: solid 1px black; padding: 4px;background-color:#EFEBDF;\">";
        res += "<tr>";
        res += "<td width=\"100\">&nbsp;</td>";
        for (int i = 0; i < 13; i++) {
            res += "<td><img src=\"" + this.getClass().getResource("/res/ui/spear.png") + "\"</td>";
        }
        res += "</tr>";
        res += "<tr>";
        res += "<td width=\"100\"><div align=\"center\">Anzahl:</div></td>";
        for (int i = 0; i < 13; i++) {
            res += "<td>2000</td>";
        }
        res += "</tr>";
        res += "<tr>";
        res += "<td width=\"100\"><div align=\"center\">Verluste:</div></td>";
        for (int i = 0; i < 13; i++) {
            res += "<td>2000</td>";
        }
        res += "</tr>";
        res += "<td width=\"100\"><div align=\"center\">&Uuml;berlebende:</div></td>";
        for (int i = 0; i < 13; i++) {
            res += "<td>0</td>";
        }
        res += "</tr>";
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jComboBox1.setBackground(new java.awt.Color(102, 255, 153));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("jLabel1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(174, 174, 174)
                        .addComponent(jLabel1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(23, 23, 23))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
