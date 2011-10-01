/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchSOSRequestAnalyzer.java
 *
 * Created on Apr 18, 2010, 2:26:56 PM
 */
package de.tor.tribes.ui.views;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.SOSRequest.TargetInformation;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.types.Village;
import de.tor.tribes.types.test.DummyProfile;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.GenericTestPanel;
import de.tor.tribes.ui.VillageSupportFrame;
import de.tor.tribes.ui.renderer.AttackTypeCellRenderer;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.ui.renderer.WallLevellCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.PropertyHelper;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.bb.SosListFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * @author Torridity
 */
public class DSWorkbenchSOSRequestAnalyzer extends AbstractDSWorkbenchFrame implements ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Copy")) {
            copySelectionToInternalClipboard();
        } else if (e.getActionCommand().equals("BBCopy")) {
            copySelectionToClipboardAsBBCode();
        } else if (e.getActionCommand().equals("Cut")) {
            cutSelectionToInternalClipboard();
        } else if (e.getActionCommand().equals("Delete")) {
            removeSelection();
        }
    }
    private static Logger logger = Logger.getLogger("SOSRequestAnalyzer");
    private static DSWorkbenchSOSRequestAnalyzer SINGLETON = null;
    private Hashtable<Tribe, SOSRequest> currentRequests = new Hashtable<Tribe, SOSRequest>();
    private GenericTestPanel centerPanel = null;
    
    public static synchronized DSWorkbenchSOSRequestAnalyzer getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchSOSRequestAnalyzer();
        }
        return SINGLETON;
    }
    
    @Override
    public void resetView() {
        currentRequests = new Hashtable<Tribe, SOSRequest>();
        updateView();
    }

    /** Creates new form DSWorkbenchSOSRequestAnalyzer */
    DSWorkbenchSOSRequestAnalyzer() {
        initComponents();
        centerPanel = new GenericTestPanel(true);
        jSOSPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jSOSInputPanel);
        buildMenu();
        capabilityInfoPanel1.addActionListener(this);
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false);
        jResultTable.registerKeyboardAction(DSWorkbenchSOSRequestAnalyzer.this, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jResultTable.registerKeyboardAction(DSWorkbenchSOSRequestAnalyzer.this, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jResultTable.registerKeyboardAction(DSWorkbenchSOSRequestAnalyzer.this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jResultTable.registerKeyboardAction(DSWorkbenchSOSRequestAnalyzer.this, "Cut", cut, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jResultTable.getActionMap().put("find", new AbstractAction() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                //ignore find
            }
        });
        
        DefaultTableModel sosTableModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Verteidiger", "Ziel", "Angreifer", "Herkunft", "Ankunft", "Angriffe", "Kampfkraft/Angriff", "Wall", "Typ"}) {
            
            Class[] types = new Class[]{
                Tribe.class, Village.class, Tribe.class, Village.class, String.class, Integer.class, Double.class, Integer.class, Integer.class
            };
            
            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
            
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        jXInputArea.setPrompt("SOS-Anfrage hier einfügen");
        
        jResultTable.setModel(sosTableModel);
        jResultTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jResultTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        
        jResultTable.getColumnExt("Wall").setCellRenderer(new WallLevellCellRenderer());
        jResultTable.getColumnExt("Typ").setCellRenderer(new AttackTypeCellRenderer());
        jResultTable.setColumnControlVisible(false);
        jResultTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jResultTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jResultTable.requestFocus();
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.sos_analyzer", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>
    }
    
    @Override
    public void toBack() {
        jAlwaysOnTopBox.setSelected(false);
        fireAlwaysOnTopEvent(null);
        super.toBack();
    }
    
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTopBox.isSelected());
        
        PropertyHelper.storeTableProperties(jResultTable, pConfig, getPropertyPrefix());
    }
    
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));
        
        try {
            jAlwaysOnTopBox.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception e) {
        }
        
        setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        
        PropertyHelper.restoreTableProperties(jResultTable, pConfig, getPropertyPrefix());
    }
    
    public String getPropertyPrefix() {
        return "sos.view";
    }
    
    private void buildMenu() {
        JXTaskPane transferPane = new JXTaskPane();
        transferPane.setTitle("Übertragen");
        JXButton toSupport = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/support_tool.png")));
        toSupport.setToolTipText("Überträgt den gewählten Angriff in das Unterstützungswerkzeug");
        toSupport.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                transferToSupportTool();
            }
        });
        
        transferPane.getContentPane().add(toSupport);
        JXButton toRetime = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/re-time.png")));
        toRetime.setToolTipText("Überträgt den gewählten Angriff in den ReTimer");
        toRetime.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                transferToRetimeTool();
            }
        });
        
        transferPane.getContentPane().add(toRetime);
        
        centerPanel.setupTaskPane(transferPane);
    }
    
    private void showInfo(String pMessage) {
        infoPanel.setCollapsed(false);
        jXInfoLabel.setBackgroundPainter(new MattePainter(getBackground()));
        jXInfoLabel.setForeground(Color.BLACK);
        jXInfoLabel.setText(pMessage);
    }
    
    private void showSuccess(String pMessage) {
        infoPanel.setCollapsed(false);
        jXInfoLabel.setBackgroundPainter(new MattePainter(Color.GREEN));
        jXInfoLabel.setForeground(Color.BLACK);
        jXInfoLabel.setText(pMessage);
    }
    
    private void showError(String pMessage) {
        infoPanel.setCollapsed(false);
        jXInfoLabel.setBackgroundPainter(new MattePainter(Color.RED));
        jXInfoLabel.setForeground(Color.WHITE);
        jXInfoLabel.setText(pMessage);
    }
    
    private void transferToSupportTool() {
        List<Attack> attacks = getSelectedAttacks();
        if (attacks.isEmpty()) {
            return;
        }
        VillageSupportFrame.getSingleton().showSupportFrame(attacks.get(0).getTarget(), attacks.get(0).getArriveTime().getTime());
    }
    
    private void transferToRetimeTool() {
        List<Attack> attacks = getSelectedAttacks();
        if (attacks.isEmpty()) {
            return;
        }
        SimpleDateFormat f = null;
        if (!ServerSettings.getSingleton().isMillisArrival()) {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format"));
        } else {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms"));
        }
        StringBuilder b = new StringBuilder();
        
        b.append(PluginManager.getSingleton().getVariableValue("sos.source")).append(" ").append(attacks.get(0).getSource().toString()).append("\n");
        b.append("Ziel: ").append(attacks.get(0).getTarget().toString()).append("\n");
        b.append(PluginManager.getSingleton().getVariableValue("attack.arrive.time")).append(" ").append(f.format(attacks.get(0).getArriveTime())).append("\n");
        DSWorkbenchReTimerFrame.getSingleton().setCustomAttack(b.toString());
        if (!DSWorkbenchReTimerFrame.getSingleton().isVisible()) {
            DSWorkbenchReTimerFrame.getSingleton().setVisible(true);
        }
    }
    
    private void copySelectionToInternalClipboard() {
        List<Attack> selection = getSelectedAttacks();
        if (selection.isEmpty()) {
            return;
        }
        
        StringBuilder b = new StringBuilder();
        int cnt = 0;
        for (Attack a : selection) {
            if (a.getUnit() == null) {
                a.setUnit(UnknownUnit.getSingleton());
            }
            b.append(Attack.toInternalRepresentation(a)).append("\n");
            cnt++;
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
            showSuccess(cnt + ((cnt == 1) ? " Angriff kopiert" : " Angriffe kopiert"));
        } catch (HeadlessException hex) {
            showError("Fehler beim Kopieren der Angriffe");
        }
    }
    
    private void copySelectionToClipboardAsBBCode() {
        if (currentRequests == null || currentRequests.isEmpty()) {
            showInfo("Keine SOS Anfragen eingelesen");
            return;
        }
        try {
            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);
            
            StringBuilder buffer = new StringBuilder();
            if (extended) {
                buffer.append("[u][size=12]SOS Anfragen[/size][/u]\n\n");
            } else {
                buffer.append("[u]SOS Anfragen[/u]\n\n");
            }
            
            List<SOSRequest> requests = new LinkedList<SOSRequest>();
            
            Enumeration<Tribe> tribeKeys = currentRequests.keys();
            while (tribeKeys.hasMoreElements()) {
                requests.add(currentRequests.get(tribeKeys.nextElement()));
            }
            buffer.append(new SosListFormatter().formatElements(requests, extended));
            
            if (extended) {
                buffer.append("\n[size=8]Erstellt am ");
                buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                buffer.append(" mit [url=\"http://www.dsworkbench.de/index.php?id=23\"]DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/url][/size]\n");
            } else {
                buffer.append("\nErstellt am ");
                buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                buffer.append(" mit [url=\"http://www.dsworkbench.de/index.php?id=23\"]DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/url]\n");
            }
            
            String b = buffer.toString();
            StringTokenizer t = new StringTokenizer(b, "[");
            int cnt = t.countTokens();
            if (cnt > 1000) {
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die momentan vorhandenen Anfragen benötigen mehr als 1000 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
            showSuccess("Daten in Zwischenablage kopiert");
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            showError("Fehler beim Kopieren in die Zwischenablage");
        }
        
    }
    
    private void cutSelectionToInternalClipboard() {
        List<Attack> selection = getSelectedAttacks();
        copySelectionToInternalClipboard();
        removeSelection(false);
        showSuccess(((selection.size() == 1) ? "Angriff" : selection.size() + " Angriffe") + " ausgeschnitten");
    }
    
    private void removeSelection() {
        removeSelection(true);
    }
    
    private void removeSelection(boolean pAsk) {
        int[] selectedRows = jResultTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length < 1) {
            showInfo("Keine Angriffe ausgewählt");
            return;
        }
        
        if (!pAsk || JOptionPaneHelper.showQuestionConfirmBox(this, "Willst du " + ((selectedRows.length == 1) ? "den gewählten Angriff " : "die gewählten Angriffe ") + "wirklich löschen?", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            DefaultTableModel model = (DefaultTableModel) jResultTable.getModel();
            int numRows = selectedRows.length;
            for (int i = 0; i < numRows; i++) {
                model.removeRow(jResultTable.convertRowIndexToModel(jResultTable.getSelectedRow()));
            }
            showSuccess(((numRows == 1) ? "Angriff" : numRows + " Angriffe") + " gelöscht");
        }
    }
    
    private List<Attack> getSelectedAttacks() {
        List<Attack> attacks = new LinkedList<Attack>();
        if (currentRequests == null) {
            showInfo("Keine SOS Anfragen vorhanden");
            return attacks;
        }
        
        int[] rows = jResultTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            showInfo("Kein Angriff gewählt");
            return attacks;
        }
        
        for (int row : rows) {
            Village target = (Village) jResultTable.getValueAt(row, 1);
            Village source = (Village) jResultTable.getValueAt(row, 3);
            String arrive = (String) jResultTable.getValueAt(row, 4);
            int type = (Integer) jResultTable.getValueAt(row, 8);
            SimpleDateFormat f = null;
            if (!ServerSettings.getSingleton().isMillisArrival()) {
                f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format"));
            } else {
                f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms"));
            }
            
            Attack a = new Attack();
            a.setSource(source);
            a.setTarget(target);
            if (type == Attack.SNOB_TYPE) {
                a.setUnit(DataHolder.getSingleton().getUnitByPlainName("snob"));
            } else if (type == Attack.FAKE_TYPE) {
                a.setUnit(DataHolder.getSingleton().getUnitByPlainName("ram"));
            }
            try {
                a.setArriveTime(f.parse(arrive));
                attacks.add(a);
            } catch (ParseException ex) {
                showError("Fehler beim Lesen der Ankunftszeit");
            }
        }
        return attacks;
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

        jSOSInputPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jXInputArea = new org.jdesktop.swingx.JXTextArea();
        jResultPanel = new org.jdesktop.swingx.JXPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXInfoLabel = new org.jdesktop.swingx.JXLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jResultTable = new org.jdesktop.swingx.JXTable();
        jSOSPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();

        jSOSInputPanel.setMinimumSize(new java.awt.Dimension(500, 400));
        jSOSInputPanel.setPreferredSize(new java.awt.Dimension(500, 400));
        jSOSInputPanel.setLayout(new java.awt.BorderLayout(0, 10));

        jScrollPane4.setMinimumSize(new java.awt.Dimension(166, 100));
        jScrollPane4.setPreferredSize(new java.awt.Dimension(166, 100));

        jXInputArea.setColumns(20);
        jXInputArea.setRows(5);
        jXInputArea.setPrompt("Bla");
        jXInputArea.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireSosRequestUpdateEvent(evt);
            }
        });
        jScrollPane4.setViewportView(jXInputArea);

        jSOSInputPanel.add(jScrollPane4, java.awt.BorderLayout.NORTH);

        jResultPanel.setPreferredSize(new java.awt.Dimension(360, 300));
        jResultPanel.setLayout(new java.awt.BorderLayout());

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXInfoLabel.setText("Keine Meldung");
        jXInfoLabel.setOpaque(true);
        jXInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jXInfoLabelfireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXInfoLabel, java.awt.BorderLayout.CENTER);

        jResultPanel.add(infoPanel, java.awt.BorderLayout.SOUTH);

        jResultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane6.setViewportView(jResultTable);

        jResultPanel.add(jScrollPane6, java.awt.BorderLayout.CENTER);

        jSOSInputPanel.add(jResultPanel, java.awt.BorderLayout.CENTER);

        setTitle("SOS Analyzer");
        setMinimumSize(new java.awt.Dimension(500, 400));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jSOSPanel.setBackground(new java.awt.Color(239, 235, 223));
        jSOSPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        jSOSPanel.setPreferredSize(new java.awt.Dimension(500, 300));
        jSOSPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jSOSPanel, gridBagConstraints);

        capabilityInfoPanel1.setPastable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAlwaysOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTopBox, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jXInfoLabelfireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXInfoLabelfireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXInfoLabelfireHideInfoEvent
    
    private void fireSosRequestUpdateEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireSosRequestUpdateEvent
        
        List<SOSRequest> requests = PluginManager.getSingleton().executeSOSParserParser(jXInputArea.getText());
        if (requests != null && !requests.isEmpty()) {
            for (SOSRequest request : requests) {
                currentRequests.put(request.getDefender(), request);
            }
        } else {
            currentRequests = new Hashtable<Tribe, SOSRequest>();
        }
        
        if (currentRequests == null || currentRequests.isEmpty()) {
            showInfo("Keine gültigen SOS Anfrage gefunden");
        } else {
            showSuccess("SOS Anfragen erfolgreich gelesen");
        }
        updateView();
        
    }//GEN-LAST:event_fireSosRequestUpdateEvent
    
private void fireAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopEvent
    setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_fireAlwaysOnTopEvent
    
    private void updateView() {
        if (currentRequests == null || currentRequests.isEmpty()) {
            return;
        }
        
        DefaultTableModel sosTableModel = (DefaultTableModel) jResultTable.getModel();
        for (int i = sosTableModel.getRowCount() - 1; i >= 0; i--) {
            sosTableModel.removeRow(i);
        }
        Enumeration<Tribe> defenders = currentRequests.keys();
        SimpleDateFormat f = null;
        if (!ServerSettings.getSingleton().isMillisArrival()) {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format"));
        } else {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms"));
        }
        while (defenders.hasMoreElements()) {

            //go through all defenders
            Tribe defender = defenders.nextElement();
            SOSRequest request = currentRequests.get(defender);
            Enumeration<Village> targets = request.getTargets();
            //go through all targets
            while (targets.hasMoreElements()) {
                
                Village target = targets.nextElement();
                TargetInformation info = request.getTargetInformation(target);
                //get general info for each target (wall and defPower)
                int wall = info.getWallLevel();
                Hashtable<UnitHolder, Integer> troopInfo = info.getTroops();
                Enumeration<UnitHolder> unitKeys = troopInfo.keys();
                double defensePower = 0;
                while (unitKeys.hasMoreElements()) {
                    UnitHolder unit = unitKeys.nextElement();
                    defensePower += unit.getDefense() * troopInfo.get(unit);
                }
                //go through all attacks on the current target
                List<SOSRequest.TimedAttack> attacks = info.getAttacks();
                int attackCount = attacks.size();
                for (SOSRequest.TimedAttack attack : attacks) {
                    //add one table row for each attack
                    int possibleType = Attack.NO_TYPE;
                    if (attack.isPossibleFake()) {
                        possibleType = Attack.FAKE_TYPE;
                    } else if (attack.isPossibleSnob()) {
                        possibleType = Attack.SNOB_TYPE;
                    }
                    
                    sosTableModel.addRow(new Object[]{defender,
                                target,
                                attack.getSource().getTribe(),
                                attack.getSource(),
                                f.format(new Date(attack.getlArriveTime())),
                                attackCount,
                                defensePower / attackCount, wall, possibleType});
                }
            }
        }
    }
    
    private void findFakes(SOSRequest pRequest) {
        Enumeration<Village> targets = pRequest.getTargets();
        List<Village> attackSources = new LinkedList<Village>();
        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            SOSRequest.TargetInformation targetInfo = pRequest.getTargetInformation(target);
            for (SOSRequest.TimedAttack attack : targetInfo.getAttacks()) {
                if (attackSources.contains(attack.getSource())) {
                    //check for possible fake
                    long sendTime = attack.getlArriveTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(attack.getSource(), target, DataHolder.getSingleton().getUnitByPlainName("ram").getSpeed()) * 1000);
                    if (sendTime < System.currentTimeMillis()) {
                        attack.setPossibleSnob(false);
                        attack.setPossibleFake(true);
                    } else {
                        attack.setPossibleSnob(true);
                        attack.setPossibleFake(false);
                    }
                } else {
                    //add unknown source
                    attackSources.add(attack.getSource());
                    attack.setPossibleFake(false);
                    attack.setPossibleSnob(false);
                }
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        GlobalOptions.setSelectedProfile(new DummyProfile());
        DataHolder.getSingleton().loadData(false);
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        
        DSWorkbenchSOSRequestAnalyzer.getSingleton().resetView();
        DSWorkbenchSOSRequestAnalyzer.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DSWorkbenchSOSRequestAnalyzer.getSingleton().setVisible(true);
        
        
        
    }
    
    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private org.jdesktop.swingx.JXPanel jResultPanel;
    private org.jdesktop.swingx.JXTable jResultTable;
    private javax.swing.JPanel jSOSInputPanel;
    private javax.swing.JPanel jSOSPanel;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private org.jdesktop.swingx.JXLabel jXInfoLabel;
    private org.jdesktop.swingx.JXTextArea jXInputArea;
    // End of variables declaration//GEN-END:variables
}
