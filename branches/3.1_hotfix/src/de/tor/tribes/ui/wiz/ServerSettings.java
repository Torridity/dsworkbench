/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ServerSettings.java
 *
 * Created on Aug 27, 2011, 7:16:58 PM
 */
package de.tor.tribes.ui.wiz;

import de.tor.tribes.db.DatabaseServerEntry;
import de.tor.tribes.php.DatabaseInterface;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanel;
import org.netbeans.spi.wizard.WizardPanelNavResult;

/**
 *
 * @author Torridity
 */
public class ServerSettings extends javax.swing.JPanel implements WizardPanel {

    private WizardController wizCtrl;
    private Map currentSettings = null;
    private boolean isError = false;

    /** Creates new form ServerSettings */
    public ServerSettings(final WizardController pWizCtrl, final Map map) {
        initComponents();
        wizCtrl = pWizCtrl;
        currentSettings = map;
        try {
            List<DatabaseServerEntry> entries = DatabaseInterface.getServerInfo(ProxyHelper.getProxyFromProperties(currentSettings));
            if (entries.isEmpty()) {
                throw new Exception();
            } else {
                Collections.sort(entries);
                DefaultComboBoxModel model = new DefaultComboBoxModel();
                model.addElement("-Bitte wählen-");
                for (DatabaseServerEntry entry : entries) {
                    model.addElement(entry);
                }
                jServerBox.setModel(model);
                DefaultComboBoxModel tribeModel = new DefaultComboBoxModel();
                tribeModel.addElement("-Bitte Server wählen-");
                jTribeBox.setModel(tribeModel);
            }
        } catch (Exception e) {
            wizCtrl.setProblem("Keine Server gefunden. Bitte versuch es später noch einmal.");
            isError = true;
        }

        jSelectServerButton.setEnabled(!isError);

        if (!isError) {
            wizCtrl.setProblem("Bitte wähle einen Server und Spielernamen.");
        } else {
            jServerBox.setModel(new DefaultComboBoxModel(new Object[]{"Nicht möglich"}));
            jTribeBox.setModel(new DefaultComboBoxModel(new Object[]{"Nicht möglich"}));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jServerBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jTribeBox = new javax.swing.JComboBox();
        jSelectServerButton = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(400, 400));
        setMinimumSize(new java.awt.Dimension(400, 400));
        setPreferredSize(new java.awt.Dimension(400, 400));
        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setMaximumSize(new java.awt.Dimension(400, 150));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(400, 150));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 150));

        jTextPane1.setContentType("text/html");
        jTextPane1.setEditable(false);
        jTextPane1.setText("<html>In diesem letzten Schritt wird der Server auf dem du spielst und dein Name im Spiel festgelegt.\nPrinzipiell kannst du DS Workbench für beliebig viele Server und mit jedem Spielernamen benutzen, einen Zugriff auf die eigentlichen DS-Accounts hast du dadurch jedoch nicht. \nVielmehr dient dies dazu, um z.B. gleichzeitig UV-Accounts in DS Workbench zu verwenden oder für einen Mitspieler Angriffe zu planen.</html>");
        jTextPane1.setMaximumSize(new java.awt.Dimension(400, 400));
        jTextPane1.setMinimumSize(new java.awt.Dimension(400, 400));
        jTextPane1.setPreferredSize(new java.awt.Dimension(400, 400));
        jScrollPane1.setViewportView(jTextPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jPanel1.setMaximumSize(new java.awt.Dimension(400, 250));
        jPanel1.setMinimumSize(new java.awt.Dimension(400, 250));
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 250));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Server");
        jLabel1.setMaximumSize(new java.awt.Dimension(100, 14));
        jLabel1.setMinimumSize(new java.awt.Dimension(100, 14));
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel1.add(jLabel1, gridBagConstraints);

        jServerBox.setMaximumSize(new java.awt.Dimension(150, 24));
        jServerBox.setMinimumSize(new java.awt.Dimension(150, 24));
        jServerBox.setPreferredSize(new java.awt.Dimension(150, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel1.add(jServerBox, gridBagConstraints);

        jLabel2.setText("Spielername");
        jLabel2.setMaximumSize(new java.awt.Dimension(100, 14));
        jLabel2.setMinimumSize(new java.awt.Dimension(100, 14));
        jLabel2.setPreferredSize(new java.awt.Dimension(100, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel1.add(jLabel2, gridBagConstraints);

        jTribeBox.setMinimumSize(new java.awt.Dimension(150, 24));
        jTribeBox.setPreferredSize(new java.awt.Dimension(150, 24));
        jTribeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireTribeChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        jPanel1.add(jTribeBox, gridBagConstraints);

        jSelectServerButton.setText("Wählen");
        jSelectServerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireSelectServerEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jSelectServerButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void fireSelectServerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectServerEvent
    if (!jSelectServerButton.isEnabled()) {
        return;
    }
    DatabaseServerEntry selection = null;
    try {
        selection = (DatabaseServerEntry) jServerBox.getSelectedItem();
    } catch (ClassCastException cce) {
    }
    if (selection == null) {
        wizCtrl.setProblem("Bitte einen Server auswählen");
        return;
    }

    String downloadURL = DatabaseInterface.getDownloadURL(selection.getServerID(), ProxyHelper.getProxyFromProperties(currentSettings));
    try {
        URL file = new URL(downloadURL + "/tribe.txt.gz");
        downloadDataFile(file, "tribe.tmp");
        BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("tribe.tmp"))));

        String line = "";
        try {
            List<Tribe> tribes = new ArrayList<Tribe>();
            while ((line = r.readLine()) != null) {
                line = line.replaceAll(",,", ", ,");
                Tribe t = Tribe.parseFromPlainData(line);
                if (t != null) {
                    tribes.add(t);
                }
            }

            if (tribes.isEmpty()) {
                wizCtrl.setProblem("Keine Spieler gefunden. Versuch es bitte später noch einmal.");
                return;
            }
            Collections.sort(tribes, Tribe.CASE_INSENSITIVE_ORDER);
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            model.addElement("-Bitte wählen-");
            for (Tribe t : tribes) {
                model.addElement(t);
            }
            jTribeBox.setModel(model);
            currentSettings.put("server", selection.getServerID());
            wizCtrl.setProblem("Bitte einen Spielernamen wählen");
        } catch (Throwable t) {
        }
        r.close();
    } catch (Exception ioe) {
        wizCtrl.setProblem("Fehler beim Herunterladen der Serverinformationen.\nBitte versuch es später nochmal.");
    }

}//GEN-LAST:event_fireSelectServerEvent

private void fireTribeChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireTribeChangedEvent
    Tribe tribe = null;
    try {
        tribe = (Tribe) jTribeBox.getSelectedItem();
    } catch (ClassCastException cce) {
    }
    if (tribe == null) {
        wizCtrl.setProblem("Bitte einen Spielernamen wählen");
        return;
    }
    wizCtrl.setProblem(null);
    currentSettings.put("tribe", tribe.getName());
}//GEN-LAST:event_fireTribeChangedEvent

    private void downloadDataFile(URL pSource, String pLocalName) throws Exception {
        URLConnection ucon = pSource.openConnection(ProxyHelper.getProxyFromProperties(currentSettings));
        ucon.setConnectTimeout(10000);
        ucon.setReadTimeout(20000);
        FileOutputStream tempWriter = new FileOutputStream(pLocalName);
        InputStream isr = ucon.getInputStream();
        int bytes = 0;
        byte[] data = new byte[1024];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int sum = 0;
        while (bytes != -1) {

            if (bytes != -1) {
                result.write(data, 0, bytes);
            }

            bytes = isr.read(data);
            sum += bytes;
            if (sum % 500 == 0) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
            }
        }

        tempWriter.write(result.toByteArray());
        tempWriter.flush();
        try {
            isr.close();
        } catch (Exception e) {
        }
        try {
            tempWriter.close();
        } catch (Exception e) {
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jSelectServerButton;
    private javax.swing.JComboBox jServerBox;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JComboBox jTribeBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String stepName, Map settings, Wizard wizard) {
        if (isError) {
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        } else {
            return WizardPanelNavResult.PROCEED;
        }
    }

    @Override
    public WizardPanelNavResult allowBack(String stepName, Map settings, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;
    }

    @Override
    public WizardPanelNavResult allowFinish(String stepName, Map settings, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;
    }
}
