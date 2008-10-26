/*
 * DSRealStatsFrame.java
 *
 * Created on 19. Juli 2008, 22:34
 */
package de.tor.tribes.ui;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.util.GlobalOptions;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 *
 * @author  Charon
 */
public class DSRealStatsFrame extends javax.swing.JFrame {

    private Tribe mCurrentTribe = null;
    private Ally mCurrentAlly = null;

    /** Creates new form DSRealStatsFrame */
    public DSRealStatsFrame() {
        initComponents();
    }

    public void showStats(Ally pAlly, boolean pPoints, boolean pBashOff, boolean pBashDef) {
        mCurrentTribe = null;
        if (mCurrentAlly != pAlly) {
            mCurrentAlly = pAlly;
            jPointsLabel.setIcon(null);
            jBashOffLabel.setIcon(null);
            jBashDefLabel.setIcon(null);
        }
        String server = GlobalOptions.getSelectedServer();
        int id = mCurrentAlly.getId();

        if (pPoints) {
            if (jPointsLabel.getIcon() == null) {
                try {
                    String url = "http://www.dsreal.de/chart/chart.php?id=";
                    url += id;
                    url += "&world=" + server;
                    url += "&mode=ally&art=points";
                    ImageIcon i = new ImageIcon(new URL(url));
                    jPointsLabel.setIcon(i);
                } catch (Exception e) {
                    jPointsLabel.setVisible(false);
                }
            }
            jPointsLabel.setVisible(true);
        } else {
            jPointsLabel.setVisible(false);
        }

        if (pBashOff) {
            if (jBashOffLabel.getIcon() == null) {
                try {
                    String url = "http://www.dsreal.de/chart/bash_chart.php?id=";
                    url += id;
                    url += "&world=" + server;
                    url += "&mode=ally&art=off";
                    ImageIcon i = new ImageIcon(new URL(url));
                    jBashOffLabel.setIcon(i);
                } catch (Exception e) {
                }
            }
            jBashOffLabel.setVisible(true);
        } else {
            jBashOffLabel.setVisible(false);
        }

        if (pBashDef) {
            if (jBashDefLabel.getIcon() == null) {
                try {
                    String url = "http://www.dsreal.de/chart/bash_chart.php?id=";
                    url += id;
                    url += "&world=" + server;
                    url += "&mode=ally&art=def";
                    ImageIcon i = new ImageIcon(new URL(url));
                    jBashDefLabel.setIcon(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            jBashDefLabel.setVisible(true);
        } else {
            jBashDefLabel.setVisible(false);
        }
        pack();
        setVisible(true);
    }

    public void showStats(Tribe pTribe, boolean pPoints, boolean pBashOff, boolean pBashDef) {
        mCurrentAlly = null;
        if (mCurrentTribe != pTribe) {
            mCurrentTribe = pTribe;
            jPointsLabel.setIcon(null);
            jBashOffLabel.setIcon(null);
            jBashDefLabel.setIcon(null);
        }
        String mode = "player";
        String server = GlobalOptions.getSelectedServer();
        if(pTribe == null){
            return;
        }
        int id = pTribe.getId();


        if (pPoints) {
            if (jPointsLabel.getIcon() == null) {
                try {
                    String url = "http://www.dsreal.de/chart/chart.php?id=";
                    url += id;
                    url += "&world=" + server;
                    url += "&mode=player&art=points";
                    ImageIcon i = new ImageIcon(new URL(url));
                    jPointsLabel.setIcon(i);
                } catch (Exception e) {
                    jPointsLabel.setVisible(false);
                }
            }
            jPointsLabel.setVisible(true);
        } else {
            jPointsLabel.setVisible(false);
        }

        if (pBashOff) {
            if (jBashOffLabel.getIcon() == null) {
                try {
                    String url = "http://www.dsreal.de/chart/bash_chart.php?id=";
                    url += id;
                    url += "&world=" + server;
                    url += "&mode=player&art=off";
                    ImageIcon i = new ImageIcon(new URL(url));
                    jBashOffLabel.setIcon(i);
                } catch (Exception e) {
                }
            }
            jBashOffLabel.setVisible(true);
        } else {
            jBashOffLabel.setVisible(false);
        }

        if (pBashDef) {
            if (jBashDefLabel.getIcon() == null) {
                try {
                    String url = "http://www.dsreal.de/chart/bash_chart.php?id=";
                    url += id;
                    url += "&world=" + server;
                    url += "&mode=player&art=def";
                    ImageIcon i = new ImageIcon(new URL(url));
                    jBashDefLabel.setIcon(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            jBashDefLabel.setVisible(true);
        } else {
            jBashDefLabel.setVisible(false);
        }
        pack();
        setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPointsLabel = new javax.swing.JLabel();
        jBashOffLabel = new javax.swing.JLabel();
        jBashDefLabel = new javax.swing.JLabel();

        setTitle("DS Real Stats");
        setAlwaysOnTop(true);

        jBashDefLabel.setBackground(new java.awt.Color(153, 255, 153));
        jBashDefLabel.setOpaque(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPointsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jBashOffLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jBashDefLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPointsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBashOffLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBashDefLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DSRealStatsFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jBashDefLabel;
    private javax.swing.JLabel jBashOffLabel;
    private javax.swing.JLabel jPointsLabel;
    // End of variables declaration//GEN-END:variables

}
