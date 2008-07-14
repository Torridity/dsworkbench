/*
 * IRCFrame.java
 *
 * Created on 14. Juli 2008, 11:09
 */
package de.tor.tribes.ui;

import de.tor.tribes.util.irc.IRCHandler;
import de.tor.tribes.util.irc.IRCHandlerListener;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import jerklib.Channel;
import jerklib.Profile;
import jerklib.events.AwayEvent;
import jerklib.events.ChannelListEvent;
import jerklib.events.ConnectionCompleteEvent;
import jerklib.events.ConnectionLostEvent;
import jerklib.events.IRCEvent;
import jerklib.events.InviteEvent;
import jerklib.events.JoinCompleteEvent;
import jerklib.events.KickEvent;
import jerklib.events.MessageEvent;
import jerklib.events.MotdEvent;
import jerklib.events.NickChangeEvent;
import jerklib.events.NickInUseEvent;
import jerklib.events.NickListEvent;
import jerklib.events.NoticeEvent;
import jerklib.events.PartEvent;
import jerklib.events.QuitEvent;
import jerklib.events.ServerInformationEvent;
import jerklib.events.ServerVersionEvent;
import jerklib.events.TopicEvent;
import jerklib.events.WhoEvent;
import jerklib.events.WhoisEvent;
import jerklib.events.WhowasEvent;
import jerklib.events.modes.ModeEvent;

/**
 *
 * @author  Jejkal
 */
public class IRCFrame extends javax.swing.JFrame implements IRCHandlerListener {

    private Hashtable<String, Integer> mTabs = null;
    private IRCHandler mHandler = null;
    private IRCOutputPanel mSystemPanel = null;
    private Profile mProfile = null;

    public IRCFrame() {
        this(null);
    }

    /** Creates new form IRCFrame */
    public IRCFrame(Profile pProfile) {
        initComponents();
        mSystemPanel = new IRCOutputPanel(null);
        jOutputTabs.add(mSystemPanel, 0);
        jOutputTabs.setTitleAt(0, "System");
        mTabs = new Hashtable<String, Integer>();
        mTabs.put("System", 0);
        if (pProfile == null) {
            mProfile = new Profile("Torri123");
        } else {
            mProfile = pProfile;
        }
        mHandler = new IRCHandler(mProfile, this);
    }

    public IRCOutputPanel getPanel(String pName) {
        Integer idx = mTabs.get(pName);
        if (idx > jOutputTabs.getTabCount() - 1) {
            return (IRCOutputPanel) jOutputTabs.getComponentAt(0);
        } else {
            return (IRCOutputPanel) jOutputTabs.getComponentAt(idx);
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

        jScrollPane2 = new javax.swing.JScrollPane();
        jUserList = new javax.swing.JList();
        jTextInput = new javax.swing.JTextField();
        jOutputTabs = new javax.swing.JTabbedPane();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane2.setViewportView(jUserList);

        jTextInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireSendInputEvent(evt);
            }
        });

        jOutputTabs.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Benutzer");
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jButton1.setText("jButton1");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireConnectEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextInput, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                    .addComponent(jOutputTabs, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButton1)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE))
                    .addComponent(jOutputTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireConnectEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireConnectEvent
    mHandler.connect("irc.quakenet.org");
    System.out.println(mHandler.isConnected());
}//GEN-LAST:event_fireConnectEvent

private void fireSendInputEvent(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fireSendInputEvent
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        String text = jTextInput.getText();
        IRCOutputPanel currentPanel = ((IRCOutputPanel) jOutputTabs.getSelectedComponent());
        if (currentPanel != mSystemPanel) {
            Channel current = currentPanel.getChannel();
            currentPanel.insertText(mProfile.getActualNick() + ": " + text);
            current.say(text);
        } else {
            mHandler.sayRaw(text);
        }
    }
}//GEN-LAST:event_fireSendInputEvent

private void sendSystemOutput(String pString){
    
}

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new IRCFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTabbedPane jOutputTabs;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextInput;
    private javax.swing.JList jUserList;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireConnectedEvent(ConnectionCompleteEvent event) {
        getPanel("System").insertText("Verbunden mit " + event.getActualHostName());
    }

    @Override
    public void fireChannelMessageEvent(MessageEvent event) {
        IRCOutputPanel channelPanel = getPanel(event.getChannel().getName());
        if (channelPanel == null) {
            return;
        } else {
            channelPanel.insertText(event.getNick() + ": " + event.getMessage());
        }
    }

    @Override
    public void fireChannelJoinEvent(JoinCompleteEvent event) {
        System.out.println("Joined");
        String chanName = event.getChannel().getName();
        jOutputTabs.addTab(chanName, new IRCOutputPanel(event.getChannel()));
        mTabs.put(chanName, jOutputTabs.getTabCount() - 1);
    }

    @Override
    public void fireAwayEvent(AwayEvent event) {
    }

    @Override
    public void fireChannelListEvent(ChannelListEvent event) {
    }

    @Override
    public void fireConnectionLostEvent(ConnectionLostEvent event) {
    }

    @Override
    public void fireInviteEvent(InviteEvent event) {
    }

    @Override
    public void fireKickEvent(KickEvent event) {
    }

    @Override
    public void fireModeEvent(ModeEvent event) {
    }

    @Override
    public void fireNickChangeEvent(NickChangeEvent event) {
    }

    @Override
    public void fireMotdEvent(MotdEvent event) {
    }

    @Override
    public void fireNickInUseEvent(NickInUseEvent event) {
    }

    @Override
    public void fireNickListEvent(NickListEvent event) {
    }

    @Override
    public void fireNoticeEvent(NoticeEvent event) {
    }

    @Override
    public void firePartEvent(PartEvent event) {
    }

    @Override
    public void firePrivateMessageEvent(MessageEvent event) {
    }

    @Override
    public void fireQuitEvent(QuitEvent event) {
    }

    @Override
    public void fireServerInformationEvent(ServerInformationEvent event) {
    }

    @Override
    public void fireServerVersionEvent(ServerVersionEvent event) {
    }

    @Override
    public void fireTopicEvent(TopicEvent event) {
    }

    @Override
    public void fireWhoisEvent(WhoisEvent event) {
    }

    @Override
    public void fireWhowasEvent(WhowasEvent event) {
    }

    @Override
    public void fireWhoEvent(WhoEvent event) {
    }

    @Override
    public void fireIRCEvent(IRCEvent event) {
    }
}
