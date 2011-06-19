/*
 * DSWorkbenchMarkerFrame.java
 *
 * Created on 28. September 2008, 15:13
 */
package de.tor.tribes.ui.views;

import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.swing.TabEditingEvent;
import com.jidesoft.swing.TabEditingListener;
import com.jidesoft.swing.TabEditingValidator;
import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.GenericTestPanel;
import de.tor.tribes.ui.MarkerTableTab;
import java.util.List;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.MouseGestureHandler;
import de.tor.tribes.util.mark.MarkerManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

/**
 * @author  Charon
 */
public class DSWorkbenchMarkerFrame extends AbstractDSWorkbenchFrame implements GenericManagerListener, ActionListener {
//@TODO Fit size, notifiy map on change, don't paint does not work

    private static Logger logger = Logger.getLogger("MarkerView");
    private static DSWorkbenchMarkerFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        MarkerTableTab activeTab = getActiveTab();
        if (e.getActionCommand() != null && activeTab != null) {
            if (e.getActionCommand().equals("Copy")) {
                activeTab.transferSelection(MarkerTableTab.TRANSFER_TYPE.COPY_TO_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Cut")) {
                activeTab.transferSelection(MarkerTableTab.TRANSFER_TYPE.CUT_TO_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Paste")) {
                activeTab.transferSelection(MarkerTableTab.TRANSFER_TYPE.FROM_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Delete")) {
                activeTab.deleteSelection(true);
            }
        }
    }

    @Override
    public void dataChangedEvent() {
        generateMarkerTabs();
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        MarkerTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateSet();
        }
    }

    /** Creates new form DSWorkbenchMarkerFrame */
    DSWorkbenchMarkerFrame() {
        initComponents();
        centerPanel = new GenericTestPanel(false);
        jMarkersPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildPanel(jXMarkerPanel);
        try {
            jMarkerFrameAlwaysOnTop.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("marker.frame.alwaysOnTop")));
            setAlwaysOnTop(jMarkerFrameAlwaysOnTop.isSelected());
        } catch (Exception e) {
            //setting not available
        }

        jMarkerTabPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
        jMarkerTabPane.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
        jMarkerTabPane.setBoldActiveTab(true);
        jMarkerTabPane.addTabEditingListener(new TabEditingListener() {

            @Override
            public void editingStarted(TabEditingEvent tee) {
            }

            @Override
            public void editingStopped(TabEditingEvent tee) {
                MarkerManager.getSingleton().renameGroup(tee.getOldTitle(), tee.getNewTitle());
            }

            @Override
            public void editingCanceled(TabEditingEvent tee) {
            }
        });
        jMarkerTabPane.setTabEditingValidator(new TabEditingValidator() {

            @Override
            public boolean alertIfInvalid(int tabIndex, String tabText) {
                if (tabText.trim().length() == 0) {
                    JOptionPaneHelper.showWarningBox(jMarkerTabPane, "'" + tabText + "' ist ein ungültiger Name für ein Markierungsset", "Fehler");
                    return false;
                }

                if (MarkerManager.getSingleton().groupExists(tabText)) {
                    JOptionPaneHelper.showWarningBox(jMarkerTabPane, "Es existiert bereits ein Markierungsset mit dem Namen '" + tabText + "'", "Fehler");
                    return false;
                }
                return true;
            }

            @Override
            public boolean isValid(int tabIndex, String tabText) {
                if (tabText.trim().length() == 0) {
                    return false;
                }

                if (MarkerManager.getSingleton().groupExists(tabText)) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean shouldStartEdit(int tabIndex, MouseEvent event) {
                return !(tabIndex == 0 || tabIndex == 1);
            }
        });
        jMarkerTabPane.setCloseAction(new AbstractAction("closeAction") {

            public void actionPerformed(ActionEvent e) {
                MarkerTableTab tab = (MarkerTableTab) e.getSource();
                if (JOptionPaneHelper.showQuestionConfirmBox(jMarkerTabPane, "Das Markierungsset '" + tab.getMarkerSet() + "' und alle darin enthaltenen Markierungen wirklich löschen? ", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                    MarkerManager.getSingleton().removeGroup(tab.getMarkerSet());
                }
            }
        });

        jMarkerTabPane.getModel().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                MarkerTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.updateSet();
                }
            }
        });

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
//        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.markers_view", GlobalOptions.getHelpBroker().getHelpSet());
// </editor-fold>
        pack();
    }

    public static DSWorkbenchMarkerFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchMarkerFrame();
        }
        return SINGLETON;
    }

    /**Get the currently selected tab*/
    private MarkerTableTab getActiveTab() {
        try {
            if (jMarkerTabPane.getModel().getSelectedIndex() < 0) {
                return null;
            }
            return ((MarkerTableTab) jMarkerTabPane.getComponentAt(jMarkerTabPane.getModel().getSelectedIndex()));
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**Initialize and add one tab for each marker set to jTabbedPane1*/
    public void generateMarkerTabs() {
        while (jMarkerTabPane.getTabCount() > 0) {
            MarkerTableTab tab = (MarkerTableTab) jMarkerTabPane.getComponentAt(0);
            tab.deregister();
            jMarkerTabPane.removeTabAt(0);
        }

        LabelUIResource lr = new LabelUIResource();
        lr.setLayout(new BorderLayout());
        lr.add(jNewPlanPanel, BorderLayout.CENTER);
        jMarkerTabPane.setTabLeadingComponent(lr);
        String[] plans = MarkerManager.getSingleton().getGroups();

        //insert default tab to first place
        int cnt = 0;
        for (String plan : plans) {
            MarkerTableTab tab = new MarkerTableTab(plan, this);
            jMarkerTabPane.addTab(plan, tab);
            if (cnt == 0) {
                jMarkerTabPane.setTabClosableAt(0, false);
            }
            cnt++;
        }
        jMarkerTabPane.setSelectedIndex(0);
        MarkerTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateSet();
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

        jXMarkerPanel = new org.jdesktop.swingx.JXPanel();
        jMarkerTabPane = new com.jidesoft.swing.JideTabbedPane();
        jNewPlanPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jMarkerFrameAlwaysOnTop = new javax.swing.JCheckBox();
        jMarkersPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.CapabilityInfoPanel();

        jXMarkerPanel.setLayout(new java.awt.BorderLayout());

        jMarkerTabPane.setScrollSelectedTabOnWheel(true);
        jMarkerTabPane.setShowCloseButtonOnTab(true);
        jMarkerTabPane.setShowGripper(true);
        jMarkerTabPane.setTabEditingAllowed(true);
        jXMarkerPanel.add(jMarkerTabPane, java.awt.BorderLayout.CENTER);

        jNewPlanPanel.setLayout(new java.awt.BorderLayout());

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_new_24x24.png"))); // NOI18N
        jLabel3.setToolTipText("Leeren Angriffsplan erstellen");
        jLabel3.setMaximumSize(new java.awt.Dimension(40, 40));
        jLabel3.setMinimumSize(new java.awt.Dimension(40, 40));
        jLabel3.setOpaque(true);
        jLabel3.setPreferredSize(new java.awt.Dimension(40, 40));
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireEnterEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireMouseExitEvent(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCreateMarkerSetEvent(evt);
            }
        });
        jNewPlanPanel.add(jLabel3, java.awt.BorderLayout.CENTER);

        setTitle("Markierungen");
        setMinimumSize(new java.awt.Dimension(400, 300));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jMarkerFrameAlwaysOnTop.setText("Immer im Vordergrund");
        jMarkerFrameAlwaysOnTop.setOpaque(false);
        jMarkerFrameAlwaysOnTop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireMarkerFrameOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jMarkerFrameAlwaysOnTop, gridBagConstraints);

        jMarkersPanel.setBackground(new java.awt.Color(239, 235, 223));
        jMarkersPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jMarkersPanel, gridBagConstraints);

        capabilityInfoPanel1.setBbSupport(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireMarkerFrameOnTopEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireMarkerFrameOnTopEvent
    setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_fireMarkerFrameOnTopEvent

private void fireEnterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireEnterEvent
    jLabel3.setBackground(getBackground().darker());
}//GEN-LAST:event_fireEnterEvent

private void fireMouseExitEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMouseExitEvent
    jLabel3.setBackground(getBackground());
}//GEN-LAST:event_fireMouseExitEvent

private void fireCreateMarkerSetEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCreateMarkerSetEvent
    int unusedId = 1;
    while (unusedId < 1000) {
        if (MarkerManager.getSingleton().addGroup("Neues Set " + unusedId)) {
            break;
        }
        unusedId++;
    }
    if (unusedId == 1000) {
        JOptionPaneHelper.showErrorBox(DSWorkbenchMarkerFrame.this, "Du hast mehr als 1000 Markierungssets. Bitte lösche zuerst ein paar bevor du Neue erstellst.", "Fehler");
        return;
    }
}//GEN-LAST:event_fireCreateMarkerSetEvent

    /**Setup marker panel*/
    @Override
    public void resetView() {
        MarkerManager.getSingleton().addManagerListener(this);
        generateMarkerTabs();
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    public static void main(String[] args) {
        MouseGestures mMouseGestures = new MouseGestures();
        mMouseGestures.setMouseButton(MouseEvent.BUTTON3_MASK);
        mMouseGestures.addMouseGesturesListener(new MouseGestureHandler());
        mMouseGestures.start();
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        // DSWorkbenchMarkerFrame.getSingleton().setSize(800, 600);
        DSWorkbenchMarkerFrame.getSingleton().pack();
        MarkerManager.getSingleton().addGroup("test1");
        MarkerManager.getSingleton().addGroup("asd2");
        MarkerManager.getSingleton().addGroup("awe3");
        for (int i = 0; i < 5; i++) {
            Marker a = new Marker();
            a.setMarkerColor(Color.RED);
            a.setMarkerID(-1);
            a.setMarkerType(a.TRIBE_MARKER_TYPE);
            Marker a2 = new Marker();
            a2.setMarkerColor(Color.RED);
            a2.setMarkerID(-1);
            a2.setMarkerType(a.TRIBE_MARKER_TYPE);
            Marker a3 = new Marker();
            a3.setMarkerColor(Color.RED);
            a3.setMarkerID(-1);
            a3.setMarkerType(a.TRIBE_MARKER_TYPE);
            MarkerManager.getSingleton().addManagedElement(a);
            MarkerManager.getSingleton().addManagedElement("test1", a2);
            MarkerManager.getSingleton().addManagedElement("asd2", a3);
        }
        DSWorkbenchMarkerFrame.getSingleton().resetView();
        DSWorkbenchMarkerFrame.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DSWorkbenchMarkerFrame.getSingleton().setVisible(true);

    }
    // <editor-fold defaultstate="collapsed" desc="Gesture handling">

    @Override
    public void fireNextPageGestureEvent() {
        int current = jMarkerTabPane.getSelectedIndex();
        int size = jMarkerTabPane.getTabCount();
        if (current + 1 > size - 1) {
            current = 0;
        } else {
            current += 1;
        }
        jMarkerTabPane.setSelectedIndex(current);
    }

    @Override
    public void firePreviousPageGestureEvent() {
        int current = jMarkerTabPane.getSelectedIndex();
        int size = jMarkerTabPane.getTabCount();
        if (current - 1 < 0) {
            current = size - 1;
        } else {
            current -= 1;
        }
        jMarkerTabPane.setSelectedIndex(current);
    }

    @Override
    public void fireRenameGestureEvent() {
        int idx = jMarkerTabPane.getSelectedIndex();
        if (idx != 0) {
            jMarkerTabPane.editTabAt(idx);
        }
    }
// </editor-fold>
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JCheckBox jMarkerFrameAlwaysOnTop;
    private com.jidesoft.swing.JideTabbedPane jMarkerTabPane;
    private javax.swing.JPanel jMarkersPanel;
    private javax.swing.JPanel jNewPlanPanel;
    private org.jdesktop.swingx.JXPanel jXMarkerPanel;
    // End of variables declaration//GEN-END:variables
}
