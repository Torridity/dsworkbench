/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchComClientFrame.java
 *
 * Created on 01.03.2009, 17:41:30
 */
package de.tor.tribes.ui;

import de.tor.tribes.net.ComClient;
import de.tor.tribes.net.ComClientListener;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author Charon
 */
public class DSWorkbenchComClientFrame extends AbstractDSWorkbenchFrame implements ComClientListener {

    private ComClient mClient = null;

    /** Creates new form DSWorkbenchComClientFrame */
    public DSWorkbenchComClientFrame() {
        initComponents();
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
        jServerName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jServerPort = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jOutputPane = new javax.swing.JTextPane();
        jLabel3 = new javax.swing.JLabel();
        jInputText = new javax.swing.JTextField();
        jDisconnectButton = new javax.swing.JButton();
        jConnectButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jNameField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jUserList = new javax.swing.JList();
        jToggleButton1 = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Client");
        setAlwaysOnTop(true);

        jLabel1.setText("Servername");

        jServerName.setText("localhost");

        jLabel2.setText("Serverport");

        jServerPort.setText("6666");
        jServerPort.setMaximumSize(new java.awt.Dimension(80, 20));
        jServerPort.setMinimumSize(new java.awt.Dimension(80, 20));
        jServerPort.setPreferredSize(new java.awt.Dimension(80, 20));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2), "Ausgabe"));
        jPanel1.setOpaque(false);

        jScrollPane1.setAutoscrolls(true);

        jOutputPane.setEditable(false);
        jScrollPane1.setViewportView(jOutputPane);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel3.setText("Eingabe");

        jInputText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireInputKeyReleasedEvent(evt);
            }
        });

        jDisconnectButton.setText("Trennen");
        jDisconnectButton.setEnabled(false);
        jDisconnectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDisconnectEvent(evt);
            }
        });

        jConnectButton.setText("Verbinden");
        jConnectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireConnectEvent(evt);
            }
        });

        jLabel4.setText("Name");

        jNameField.setText("Torri");
        jNameField.setMaximumSize(new java.awt.Dimension(120, 20));
        jNameField.setMinimumSize(new java.awt.Dimension(120, 20));
        jNameField.setPreferredSize(new java.awt.Dimension(120, 20));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2), "Online"));
        jPanel2.setOpaque(false);

        jScrollPane2.setViewportView(jUserList);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
        );

        jToggleButton1.setText("jToggleButton1");
        jToggleButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRandomInputEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jInputText))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jConnectButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jDisconnectButton))
                            .addComponent(jServerName))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToggleButton1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jServerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jConnectButton)
                    .addComponent(jDisconnectButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jInputText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireConnectEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireConnectEvent
        if (!jConnectButton.isEnabled()) {
            return;
        }
        String server = jServerName.getText();

        try {
            int port = Integer.parseInt(jServerPort.getText());
            mClient = new ComClient(server, port, jNameField.getText());
            mClient.addComClientListener(this);
            mClient.sendHello();
            jConnectButton.setEnabled(false);
            jDisconnectButton.setEnabled(true);
        } catch (Exception e) {
        }
    }//GEN-LAST:event_fireConnectEvent

    private void fireDisconnectEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDisconnectEvent
        if (!jDisconnectButton.isEnabled()) {
            return;
        }
        performDisconnect();
    }//GEN-LAST:event_fireDisconnectEvent

    private void performDisconnect() {
        mClient.removeComClientListener(this);
        mClient.disconnect();
        mClient = null;
        jConnectButton.setEnabled(true);
        jUserList.setModel(new DefaultListModel());
        jDisconnectButton.setEnabled(false);
    }

    private void fireInputKeyReleasedEvent(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fireInputKeyReleasedEvent
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            String text = jInputText.getText();
            if (text.length() == 0) {
                //ignore no text
                return;
            }
            mClient.sendChat(text);

            try {
                DefaultStyledDocument doc = (DefaultStyledDocument) jOutputPane.getDocument();
                SimpleAttributeSet att = new SimpleAttributeSet();
                StyleConstants.setForeground(att, Color.BLUE);
                jOutputPane.setCaretPosition(doc.getLength());
                doc.insertString(doc.getLength(), mClient.getNick() + ": ", att);
                att = new SimpleAttributeSet();
                jOutputPane.setCaretPosition(doc.getLength());
                doc.insertString(doc.getLength(), text + "\n", att);
                jInputText.setText("");
            } catch (Exception e) {
            }
        }
    }//GEN-LAST:event_fireInputKeyReleasedEvent

    private void fireRandomInputEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRandomInputEvent
        if (jToggleButton1.isSelected()) {
            new Thread(new Runnable() {

                public void run() {
                    while (true) {
                        jInputText.setText(Double.toString(Math.random()));
                        String text = jInputText.getText();
                        mClient.sendChat(text);

                        try {
                            DefaultStyledDocument doc = (DefaultStyledDocument) jOutputPane.getDocument();
                            SimpleAttributeSet att = new SimpleAttributeSet();
                            StyleConstants.setForeground(att, Color.BLUE);
                            jOutputPane.setCaretPosition(doc.getLength());
                            doc.insertString(doc.getLength(), mClient.getNick() + ": ", att);
                            att = new SimpleAttributeSet();
                            jOutputPane.setCaretPosition(doc.getLength());
                            doc.insertString(doc.getLength(), text + "\n", att);
                            jInputText.setText("");
                        } catch (Exception e) {
                        }
                        try {
                            Thread.sleep(20);
                        } catch (Exception e) {
                        }
                    }
                }
            }).start();
        }
    }//GEN-LAST:event_fireRandomInputEvent

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchComClientFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jConnectButton;
    private javax.swing.JButton jDisconnectButton;
    private javax.swing.JTextField jInputText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jNameField;
    private javax.swing.JTextPane jOutputPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jServerName;
    private javax.swing.JTextField jServerPort;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JList jUserList;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireHelloEvent(String pUser) {
        try {
            DefaultStyledDocument doc = (DefaultStyledDocument) jOutputPane.getDocument();
            SimpleAttributeSet att = new SimpleAttributeSet();
            StyleConstants.setForeground(att, Color.LIGHT_GRAY);
            StyleConstants.setItalic(att, true);
            jOutputPane.setCaretPosition(doc.getLength());
            doc.insertString(doc.getLength(), pUser + " ist eingetreten\n", att);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fireChatEvent(String pUser, String pText) {
        try {
            DefaultStyledDocument doc = (DefaultStyledDocument) jOutputPane.getDocument();
            SimpleAttributeSet att = new SimpleAttributeSet();
            StyleConstants.setForeground(att, Color.RED);
            jOutputPane.setCaretPosition(doc.getLength());
            doc.insertString(doc.getLength(), pUser + ": ", att);
            att = new SimpleAttributeSet();
            jOutputPane.setCaretPosition(doc.getLength());
            doc.insertString(doc.getLength(), pText + "\n", att);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fireUserListEvent(String[] pUsers) {
        DefaultListModel model = new DefaultListModel();
        Arrays.sort(pUsers, String.CASE_INSENSITIVE_ORDER);
        for (String user : pUsers) {
            model.addElement(user);
        }
        jUserList.setModel(model);
    }

    @Override
    public void fireByeEvent(String pUser) {
        try {
            DefaultStyledDocument doc = (DefaultStyledDocument) jOutputPane.getDocument();
            SimpleAttributeSet att = new SimpleAttributeSet();
            StyleConstants.setForeground(att, Color.LIGHT_GRAY);
            StyleConstants.setItalic(att, true);
            jOutputPane.setCaretPosition(doc.getLength());
            doc.insertString(doc.getLength(), pUser + " hat uns verlassen\n", att);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fireDisconnectEvent() {
        try {
            DefaultStyledDocument doc = (DefaultStyledDocument) jOutputPane.getDocument();
            SimpleAttributeSet att = new SimpleAttributeSet();
            StyleConstants.setForeground(att, Color.RED);
            StyleConstants.setItalic(att, true);
            jOutputPane.setCaretPosition(doc.getLength());
            doc.insertString(doc.getLength(), "Der Server wurde beendet\n", att);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            performDisconnect();
        }
    }

    @Override
    public void fireKickEvent() {
        try {
            DefaultStyledDocument doc = (DefaultStyledDocument) jOutputPane.getDocument();
            SimpleAttributeSet att = new SimpleAttributeSet();
            StyleConstants.setForeground(att, Color.RED);
            StyleConstants.setItalic(att, true);
            jOutputPane.setCaretPosition(doc.getLength());
            doc.insertString(doc.getLength(), "Du wurdest vom Server verwiesen\n", att);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            performDisconnect();
        }
    }

    @Override
    public void fireBanEvent() {
        try {
            DefaultStyledDocument doc = (DefaultStyledDocument) jOutputPane.getDocument();
            SimpleAttributeSet att = new SimpleAttributeSet();
            StyleConstants.setForeground(att, Color.RED);
            StyleConstants.setItalic(att, true);
            jOutputPane.setCaretPosition(doc.getLength());
            doc.insertString(doc.getLength(), "Du wurdest dauerhaft vom Server verwiesen\n", att);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fireDisconnectEvent(null);
        }
    }
}
