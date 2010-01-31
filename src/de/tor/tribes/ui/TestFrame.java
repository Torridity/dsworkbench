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
        String res = "<html>";
        try {
            BufferedReader r = new BufferedReader(new FileReader(new File("VillageInfo.tmpl")));
            String line = "";
            while ((line = r.readLine()) != null) {
                res += line;
            }
            res += "</html>";
        } catch (Exception e) {
            e.printStackTrace();
        }
        res = res.replaceAll("\\$VILLAGE_NAME", "Barbarendorf (123|234) K23");
        res = res.replaceAll("\\$VILLAGE_POINTS", "10.019");
        res = res.replaceAll("\\$VILLAGE_OWNER", "Rattenfutter");
        res = res.replaceAll("\\$VILLAGE_ALLY", "[KvA]");
        res = res.replaceAll("\\$VILLAGE_MORAL", "100%");
        res = res.replaceAll("\\$VILLAGE_TAGS", "Off;Fertig");
        res = res.replaceAll("\\$UNIT_TABLE", buildUnitTable());
        System.out.println(res);
        jLabel1.setToolTipText(res);
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

        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("jLabel1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(173, 173, 173)
                .addComponent(jLabel1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
