/*
 * SearchFrame.java
 *
 * Created on 19. Juni 2008, 11:19
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Desktop;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

/**
 *
 * @author  Jejkal
 */
public class DSWorkbenchSearchFrame extends javax.swing.JFrame implements SearchListener {

    private static Logger logger = Logger.getLogger("Search");
    private String sLastPlayerValue = null;
    private SearchThread mSearchThread = null;
    private static DSWorkbenchSearchFrame SINGLETON = null;

    public static synchronized DSWorkbenchSearchFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchSearchFrame();
        }
        return SINGLETON;
    }

    /** Creates new form SearchFrame */
    DSWorkbenchSearchFrame() {
        initComponents();
        getContentPane().setBackground(Constants.DS_BACK);
        // frameControlPanel1.setupPanel(this, true, true);
        jCenterInGameButton.setIcon(new ImageIcon("./graphics/icons/center.png"));
        jSendResButton.setIcon(new ImageIcon("./graphics/icons/booty.png"));
        jSendDefButton.setIcon(new ImageIcon("./graphics/icons/def.png"));
        try {
            jSearchFrameAlwaysOnTop.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("search.frame.alwaysOnTop")));
            setAlwaysOnTop(jSearchFrameAlwaysOnTop.isSelected());
        } catch (Exception e) {
            //setting not available
        }

        //check desktop support
        if (!Desktop.isDesktopSupported()) {
            jCenterInGameButton.setEnabled(false);
            jSendDefButton.setEnabled(false);
            jSendResButton.setEnabled(false);
        }
        mSearchThread = new SearchThread("", this);
        mSearchThread.setDaemon(true);
        mSearchThread.start();
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.search_tool", GlobalOptions.getHelpBroker().getHelpSet());
    // </editor-fold>
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPlayerSearch = new javax.swing.JPanel();
        jSearchTerm = new javax.swing.JTextField();
        jSearchTermLabel = new javax.swing.JLabel();
        jTribesList = new javax.swing.JComboBox();
        jTribesLabel = new javax.swing.JLabel();
        jMarkAllyButton = new javax.swing.JButton();
        jAllyList = new javax.swing.JComboBox();
        jAlliesLabel = new javax.swing.JLabel();
        jMarkTribeButton = new javax.swing.JButton();
        jVillagesLabel = new javax.swing.JLabel();
        jCenterButton = new javax.swing.JButton();
        jVillageList = new javax.swing.JComboBox();
        jCenterInGameButton = new javax.swing.JButton();
        jSendResButton = new javax.swing.JButton();
        jSendDefButton = new javax.swing.JButton();
        jInGameOptionsLabel = new javax.swing.JLabel();
        jSearchFrameAlwaysOnTop = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        setTitle(bundle.getString("DSWorkbenchSearchFrame.title")); // NOI18N

        jPlayerSearch.setBackground(new java.awt.Color(239, 235, 223));

        jSearchTerm.setMaximumSize(new java.awt.Dimension(200, 20));
        jSearchTerm.setMinimumSize(new java.awt.Dimension(200, 20));
        jSearchTerm.setPreferredSize(new java.awt.Dimension(200, 20));
        jSearchTerm.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireValueChangedEvent(evt);
            }
        });

        jSearchTermLabel.setText(bundle.getString("DSWorkbenchSearchFrame.jSearchTermLabel.text")); // NOI18N

        jTribesList.setMaximumSize(new java.awt.Dimension(200, 20));
        jTribesList.setMinimumSize(new java.awt.Dimension(200, 20));
        jTribesList.setPreferredSize(new java.awt.Dimension(200, 20));
        jTribesList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireTribeSelectionChangedEvent(evt);
            }
        });

        jTribesLabel.setText(bundle.getString("DSWorkbenchSearchFrame.jTribesLabel.text")); // NOI18N

        jMarkAllyButton.setBackground(new java.awt.Color(239, 235, 223));
        jMarkAllyButton.setText(bundle.getString("DSWorkbenchSearchFrame.jMarkAllyButton.text")); // NOI18N
        jMarkAllyButton.setToolTipText(bundle.getString("DSWorkbenchSearchFrame.jMarkAllyButton.toolTipText")); // NOI18N
        jMarkAllyButton.setMaximumSize(new java.awt.Dimension(100, 23));
        jMarkAllyButton.setMinimumSize(new java.awt.Dimension(100, 23));
        jMarkAllyButton.setPreferredSize(new java.awt.Dimension(100, 23));
        jMarkAllyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddMarkerEvent(evt);
            }
        });

        jAllyList.setMaximumSize(new java.awt.Dimension(200, 20));
        jAllyList.setMinimumSize(new java.awt.Dimension(200, 20));
        jAllyList.setPreferredSize(new java.awt.Dimension(200, 20));
        jAllyList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAllySelectionChangedEvent(evt);
            }
        });

        jAlliesLabel.setText(bundle.getString("DSWorkbenchSearchFrame.jAlliesLabel.text")); // NOI18N

        jMarkTribeButton.setBackground(new java.awt.Color(239, 235, 223));
        jMarkTribeButton.setText(bundle.getString("DSWorkbenchSearchFrame.jMarkTribeButton.text")); // NOI18N
        jMarkTribeButton.setToolTipText(bundle.getString("DSWorkbenchSearchFrame.jMarkTribeButton.toolTipText")); // NOI18N
        jMarkTribeButton.setMaximumSize(new java.awt.Dimension(100, 23));
        jMarkTribeButton.setMinimumSize(new java.awt.Dimension(100, 23));
        jMarkTribeButton.setPreferredSize(new java.awt.Dimension(100, 23));
        jMarkTribeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddMarkerEvent(evt);
            }
        });

        jVillagesLabel.setText(bundle.getString("DSWorkbenchSearchFrame.jVillagesLabel.text")); // NOI18N

        jCenterButton.setBackground(new java.awt.Color(239, 235, 223));
        jCenterButton.setText(bundle.getString("DSWorkbenchSearchFrame.jCenterButton.text")); // NOI18N
        jCenterButton.setToolTipText(bundle.getString("DSWorkbenchSearchFrame.jCenterButton.toolTipText")); // NOI18N
        jCenterButton.setMaximumSize(new java.awt.Dimension(100, 23));
        jCenterButton.setMinimumSize(new java.awt.Dimension(100, 23));
        jCenterButton.setPreferredSize(new java.awt.Dimension(100, 23));
        jCenterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCenterMapEvent(evt);
            }
        });

        jCenterInGameButton.setBackground(new java.awt.Color(239, 235, 223));
        jCenterInGameButton.setToolTipText(bundle.getString("DSWorkbenchSearchFrame.jCenterInGameButton.toolTipText")); // NOI18N
        jCenterInGameButton.setMaximumSize(new java.awt.Dimension(31, 31));
        jCenterInGameButton.setMinimumSize(new java.awt.Dimension(31, 31));
        jCenterInGameButton.setPreferredSize(new java.awt.Dimension(31, 31));
        jCenterInGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCenterMapInGameEvent(evt);
            }
        });

        jSendResButton.setBackground(new java.awt.Color(239, 235, 223));
        jSendResButton.setToolTipText(bundle.getString("DSWorkbenchSearchFrame.jSendResButton.toolTipText")); // NOI18N
        jSendResButton.setMaximumSize(new java.awt.Dimension(31, 31));
        jSendResButton.setMinimumSize(new java.awt.Dimension(31, 31));
        jSendResButton.setPreferredSize(new java.awt.Dimension(31, 31));
        jSendResButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSendResEvent(evt);
            }
        });

        jSendDefButton.setBackground(new java.awt.Color(239, 235, 223));
        jSendDefButton.setToolTipText(bundle.getString("DSWorkbenchSearchFrame.jSendDefButton.toolTipText")); // NOI18N
        jSendDefButton.setMaximumSize(new java.awt.Dimension(31, 31));
        jSendDefButton.setMinimumSize(new java.awt.Dimension(31, 31));
        jSendDefButton.setPreferredSize(new java.awt.Dimension(31, 31));
        jSendDefButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSendDefEvent(evt);
            }
        });

        jInGameOptionsLabel.setText(bundle.getString("DSWorkbenchSearchFrame.jInGameOptionsLabel.text")); // NOI18N

        javax.swing.GroupLayout jPlayerSearchLayout = new javax.swing.GroupLayout(jPlayerSearch);
        jPlayerSearch.setLayout(jPlayerSearchLayout);
        jPlayerSearchLayout.setHorizontalGroup(
            jPlayerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPlayerSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPlayerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jInGameOptionsLabel)
                    .addComponent(jSearchTermLabel)
                    .addComponent(jVillagesLabel)
                    .addComponent(jTribesLabel)
                    .addComponent(jAlliesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPlayerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAllyList, 0, 386, Short.MAX_VALUE)
                    .addComponent(jTribesList, 0, 0, Short.MAX_VALUE)
                    .addComponent(jSearchTerm, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                    .addComponent(jVillageList, javax.swing.GroupLayout.Alignment.TRAILING, 0, 386, Short.MAX_VALUE)
                    .addGroup(jPlayerSearchLayout.createSequentialGroup()
                        .addComponent(jCenterInGameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSendDefButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSendResButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPlayerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jMarkTribeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMarkAllyButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCenterButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPlayerSearchLayout.setVerticalGroup(
            jPlayerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPlayerSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPlayerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSearchTermLabel)
                    .addComponent(jSearchTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPlayerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jMarkAllyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jAllyList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jAlliesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPlayerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jMarkTribeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTribesLabel)
                    .addComponent(jTribesList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPlayerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jVillagesLabel)
                    .addComponent(jCenterButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jVillageList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPlayerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPlayerSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jCenterInGameButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jInGameOptionsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jSendResButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSendDefButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSearchFrameAlwaysOnTop.setText(bundle.getString("DSWorkbenchSearchFrame.jSearchFrameAlwaysOnTop.text")); // NOI18N
        jSearchFrameAlwaysOnTop.setOpaque(false);
        jSearchFrameAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireSearchFrameAlwaysOnTopEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSearchFrameAlwaysOnTop)
                    .addComponent(jPlayerSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPlayerSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSearchFrameAlwaysOnTop)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireValueChangedEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireValueChangedEvent
    String currentValue = jSearchTerm.getText();
    if (currentValue.equals(sLastPlayerValue)) {
        //no change
        return;
    }
    sLastPlayerValue = currentValue;
    mSearchThread.setSearchTerm(currentValue);
}//GEN-LAST:event_fireValueChangedEvent

private void fireTribeSelectionChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireTribeSelectionChangedEvent
    try {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            Village[] v = ((Tribe) evt.getItem()).getVillageList().toArray(new Village[0]);
            Arrays.sort(v);
            DefaultComboBoxModel model = new DefaultComboBoxModel(v);
            jVillageList.setModel(model);
        }
    } catch (Exception e) {
        //produced if 0-element in combobox is selected
    }
}//GEN-LAST:event_fireTribeSelectionChangedEvent

private void fireAllySelectionChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAllySelectionChangedEvent
    try {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            Tribe[] tl = ((Ally) evt.getItem()).getTribes().toArray(new Tribe[0]);
            Arrays.sort(tl, Tribe.CASE_INSENSITIVE_ORDER);
            DefaultComboBoxModel model = new DefaultComboBoxModel(tl);
            jTribesList.setModel(model);
            jTribesList.setSelectedIndex(0);
            Tribe t = (Tribe) jTribesList.getItemAt(0);
            model = new DefaultComboBoxModel(t.getVillageList().toArray(new Village[0]));
            jVillageList.setModel(model);
        }
    } catch (Exception e) {
        //produced if 0-element in combobox is selected
    }
}//GEN-LAST:event_fireAllySelectionChangedEvent

private void fireAddMarkerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddMarkerEvent
    try {
        if (evt.getSource() == jMarkAllyButton) {
            MarkerAddFrame f = new MarkerAddFrame();
            Object selection = jAllyList.getSelectedItem();
            if (selection instanceof String) {
                //no ally selected
            } else {
                f.setVillage(((Ally) selection).getTribes().get(0).getVillageList().get(0));
                f.setAllyOnly();
                f.setVisible(true);
            }
        } else {
            MarkerAddFrame f = new MarkerAddFrame();
            Object selection = jTribesList.getSelectedItem();
            if (selection instanceof String) {
                //no tribe selected
            } else {
                f.setVillage(((Tribe) selection).getVillageList().get(0));
                f.setTribeOnly();
                f.setVisible(true);
            }
        }
    } catch (Exception e) {
        logger.warn("Failed to add marker", e);
    }
}//GEN-LAST:event_fireAddMarkerEvent

private void fireCenterMapEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterMapEvent
    DSWorkbenchMainFrame.getSingleton().centerVillage(((Village) jVillageList.getSelectedItem()));
}//GEN-LAST:event_fireCenterMapEvent

private void fireCenterMapInGameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterMapInGameEvent
    if (!jCenterInGameButton.isEnabled()) {
        return;
    }
    Village v = (Village) jVillageList.getSelectedItem();
    if (v != null) {
        BrowserCommandSender.centerVillage(v);
    }
}//GEN-LAST:event_fireCenterMapInGameEvent

private void fireSendDefEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSendDefEvent
    if (!jSendDefButton.isEnabled()) {
        return;
    }
    Village target = (Village) jVillageList.getSelectedItem();
    Village source = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
    if ((source != null) && (target != null)) {
        BrowserCommandSender.sendTroops(source, target);
    }
}//GEN-LAST:event_fireSendDefEvent

private void fireSendResEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSendResEvent
    if (!jSendResButton.isEnabled()) {
        return;
    }
    Village target = (Village) jVillageList.getSelectedItem();
    Village source = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
    if ((source != null) && (target != null)) {
        BrowserCommandSender.sendRes(source, target);
    }
}//GEN-LAST:event_fireSendResEvent

private void fireSearchFrameAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireSearchFrameAlwaysOnTopEvent
    setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_fireSearchFrameAlwaysOnTopEvent

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jAlliesLabel;
    private javax.swing.JComboBox jAllyList;
    private javax.swing.JButton jCenterButton;
    private javax.swing.JButton jCenterInGameButton;
    private javax.swing.JLabel jInGameOptionsLabel;
    private javax.swing.JButton jMarkAllyButton;
    private javax.swing.JButton jMarkTribeButton;
    private javax.swing.JPanel jPlayerSearch;
    private javax.swing.JCheckBox jSearchFrameAlwaysOnTop;
    private javax.swing.JTextField jSearchTerm;
    private javax.swing.JLabel jSearchTermLabel;
    private javax.swing.JButton jSendDefButton;
    private javax.swing.JButton jSendResButton;
    private javax.swing.JLabel jTribesLabel;
    private javax.swing.JComboBox jTribesList;
    private javax.swing.JComboBox jVillageList;
    private javax.swing.JLabel jVillagesLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireTribesFoundEvent(Tribe[] t) {
        Arrays.sort(t, Tribe.CASE_INSENSITIVE_ORDER);
        jTribesList.setModel(new DefaultComboBoxModel(t));
        //remove villages
        jVillageList.setModel(new DefaultComboBoxModel());
        try {
            String result = t.length + " Spieler gefunden";
            ((DefaultComboBoxModel) jTribesList.getModel()).insertElementAt(result, 0);
            jTribesList.setSelectedIndex(0);
        } catch (Exception e) {
        }
    }

    @Override
    public void fireAlliesFoundEvent(Ally[] a) {
        Arrays.sort(a, Ally.CASE_INSENSITIVE_ORDER);
        jAllyList.setModel(new DefaultComboBoxModel(a));

        try {
            String result = a.length + ((a.length == 1) ? " Stamm " : " Stämme ") + "gefunden";

            ((DefaultComboBoxModel) jAllyList.getModel()).insertElementAt(result, 0);
            jAllyList.setSelectedIndex(0);
        } catch (Exception e) {
        }
    }
}

interface SearchListener {

    public void fireTribesFoundEvent(Tribe[] t);

    public void fireAlliesFoundEvent(Ally[] a);
}

class SearchThread extends Thread {

    private boolean running = true;
    private boolean restart = false;
    private boolean searchDone = false;
    private String sSearchTerm = null;
    private SearchListener mListener;

    public SearchThread(String pSearchTerm, SearchListener pListener) {
        sSearchTerm = pSearchTerm;
        mListener = pListener;
    }

    public void setSearchTerm(String pSearchTerm) {
        if (pSearchTerm != null) {
            if (!sSearchTerm.equals(pSearchTerm)) {
                sSearchTerm = pSearchTerm;
                restart = true;
                searchDone = false;
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            if (!searchDone) {
                if (sSearchTerm.length() >= 1) {
                    List<Tribe> tribeList = new LinkedList<Tribe>();
                    List<Ally> allyList = new LinkedList<Ally>();
                    Enumeration<Integer> tribes = DataHolder.getSingleton().getTribes().keys();
                    while (tribes.hasMoreElements()) {
                        Tribe t = DataHolder.getSingleton().getTribes().get(tribes.nextElement());
                        if (t.getName().toLowerCase().startsWith(sSearchTerm.toLowerCase())) {
                            if (!tribeList.contains(t)) {
                                tribeList.add(t);
                            }
                        }
                        Ally a = t.getAlly();
                        if (a != null) {
                            if ((a.getName().toLowerCase().startsWith(sSearchTerm.toLowerCase())) || (a.getTag().toLowerCase().startsWith(sSearchTerm.toLowerCase()))) {
                                if (!allyList.contains(a)) {
                                    allyList.add(a);
                                }
                            }
                        }
                        if (restart) {
                            break;
                        }
                    }
                    if (!restart) {
                        searchDone = true;
                        mListener.fireTribesFoundEvent(tribeList.toArray(new Tribe[0]));
                        mListener.fireAlliesFoundEvent(allyList.toArray(new Ally[0]));
                    } else {
                        restart = false;
                    }

                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }

    public void restartSearch() {
        restart = true;
    }

    public void stopRunning() {
        running = false;
    }
}
