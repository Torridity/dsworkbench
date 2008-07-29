/*
 * DSWorkbenchSplashScreen.java
 *
 * Created on 30. Juni 2008, 14:12
 */
package de.tor.tribes.ui;

import de.tor.tribes.util.GlobalOptions;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import de.tor.tribes.io.DataHolderListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.UIManager;
import org.apache.log4j.xml.DOMConfigurator;

/**
 *
 * @author  Jejkal
 */
public class DSWorkbenchSplashScreen extends javax.swing.JFrame implements DataHolderListener {

    private static Logger logger = Logger.getLogger(DSWorkbenchSplashScreen.class);
    private final DSWorkbenchSplashScreen self = this;
    private final SplashRepaintThread t;

    /** Creates new form DSWorkbenchSplashScreen */
    public DSWorkbenchSplashScreen() {
        initComponents();
        new Timer("StartupTimer", true).schedule(new HideSplashTask(this), 3000);
        t = new SplashRepaintThread(this);
        t.setDaemon(true);
        t.start();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jStatusOutput = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        jStatusOutput.setFont(new java.awt.Font("Comic Sans MS", 0, 14));
        jStatusOutput.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jStatusOutput.setText("Lade Einstellungen...");

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/splash.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 0, 0))
            .addComponent(jStatusOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 0, 0)
                .addComponent(jStatusOutput))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    protected void hideSplash() {
        try {
            GlobalOptions.initialize();
            GlobalOptions.addDataHolderListener(this);
            GlobalOptions.addDataHolderListener(DSWorkbenchSettingsDialog.getGlobalSettingsFrame());
        } catch (Exception e) {
            logger.error("Failed to initialize global options", e);
            JOptionPane.showMessageDialog(self, "Fehler bei der Initialisierung.\nMöglicherweise ist deine DS Workbench Installation defekt.", "Fehler", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        if (!DSWorkbenchSettingsDialog.getGlobalSettingsFrame().checkSettings()) {
            logger.info("Reading user settings returned error(s)");
            DSWorkbenchSettingsDialog.getGlobalSettingsFrame().setVisible(true);
        }

        try {
            GlobalOptions.loadData(false);
            GlobalOptions.loadUserData();
        } catch (Exception e) {
            logger.error("Failed to load server data", e);
            System.exit(1);
        }

        try {
            DSWorkbenchMainFrame mainFrame = new DSWorkbenchMainFrame();
            SearchFrame.createSearchFrame(mainFrame);
            DSWorkbenchSettingsDialog.getGlobalSettingsFrame().setMainFrame(mainFrame);
            mainFrame.init();
            mainFrame.setVisible(true);
            t.stopRunning();
            setVisible(false);
        } catch (Exception e) {
            logger.error("Failed to start initialize MainFrame", e);
            JOptionPane.showMessageDialog(self, "Fehler bei der Initialisierung.\nMöglicherweise ist deine DS Workbench Installation defekt.", "Fehler", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        DOMConfigurator.configure("log4j.xml");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }



        java.awt.EventQueue.invokeLater(new  

              Runnable() {

                    
                public void run() {
                DSWorkbenchSplashScreen splash = new DSWorkbenchSplashScreen();
                splash.setLocationRelativeTo(null);
                splash.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jStatusOutput;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireDataHolderEvent(String pText) {
        jStatusOutput.setText(pText);
    }

    public void updateStatus() {
        jStatusOutput.updateUI();
    }

    @Override
    public void fireDataLoadedEvent() {
        jStatusOutput.setText("Daten geladen");
    }
}

class HideSplashTask extends TimerTask {

    private DSWorkbenchSplashScreen mParent;

    public HideSplashTask(DSWorkbenchSplashScreen pParent) {
        mParent = pParent;
    }

    public void run() {
        mParent.hideSplash();
    }
}

class SplashRepaintThread extends Thread {

    private DSWorkbenchSplashScreen mParent;
    private boolean running = true;

    public SplashRepaintThread(DSWorkbenchSplashScreen pParent) {
        mParent = pParent;
    }

    public void run() {
        while (running) {
            mParent.updateStatus();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
            }
        }
    }

    public void stopRunning() {
        running = false;
    }
}
