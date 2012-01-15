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

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.*;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.panels.TroopSelectionPanel;
import de.tor.tribes.ui.models.DefenseToolModel;
import de.tor.tribes.ui.models.SupportsModel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.DefenseStatusTableCellRenderer;
import de.tor.tribes.ui.renderer.LossRatioTableCellRenderer;
import de.tor.tribes.ui.renderer.TendencyTableCellRenderer;
import de.tor.tribes.ui.windows.VillageSupportFrame;
import de.tor.tribes.ui.wiz.dep.DefenseAnalysePanel;
import de.tor.tribes.ui.wiz.dep.DefensePlanerWizard;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.DefenseAnalyzer;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.PropertyHelper;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.TableHelper;
import de.tor.tribes.util.UIHelper;
import de.tor.tribes.util.sos.SOSManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.time.DateUtils;
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
            // copySelectionToInternalClipboard();
        } else if (e.getActionCommand().equals("BBCopy")) {
            // copySelectionToClipboardAsBBCode();
        } else if (e.getActionCommand().equals("Cut")) {
            // cutSelectionToInternalClipboard();
        } else if (e.getActionCommand().equals("Delete")) {
            removeSelection();
        }
    }
    private static Logger logger = Logger.getLogger("SOSRequestAnalyzer");
    private static DSWorkbenchSOSRequestAnalyzer SINGLETON = null;
    private GenericTestPanel centerPanel = null;
    private TroopSelectionPanel defensePanel = null;
    private TroopSelectionPanel offensePanel = null;
    private DefenseAnalyzer a = null;

    public static synchronized DSWorkbenchSOSRequestAnalyzer getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchSOSRequestAnalyzer();
        }
        return SINGLETON;
    }

    @Override
    public void resetView() {
        updateView();
    }

    /**
     * Creates new form DSWorkbenchSOSRequestAnalyzer
     */
    DSWorkbenchSOSRequestAnalyzer() {
        initComponents();
        centerPanel = new GenericTestPanel(true);
        jSOSPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jSOSInputPanel);
        buildMenu();
        jButton1.setIcon(new ImageIcon("./graphics/big/find.png"));
        capabilityInfoPanel1.addActionListener(this);
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false);
        jAttacksTable.registerKeyboardAction(DSWorkbenchSOSRequestAnalyzer.this, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jAttacksTable.registerKeyboardAction(DSWorkbenchSOSRequestAnalyzer.this, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jAttacksTable.registerKeyboardAction(DSWorkbenchSOSRequestAnalyzer.this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jAttacksTable.registerKeyboardAction(DSWorkbenchSOSRequestAnalyzer.this, "Cut", cut, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jAttacksTable.getActionMap().put("find", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //ignore find
            }
        });

        jAttacksTable.setModel(new DefenseToolModel());
        jAttacksTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jAttacksTable.getColumnExt("Tendenz").setCellRenderer(new TendencyTableCellRenderer());
        jAttacksTable.getColumnExt("Status").setCellRenderer(new DefenseStatusTableCellRenderer());
        //jAttacksTable.getColumnExt("Wall").setCellRenderer(new WallLevellCellRenderer());
        jAttacksTable.getColumnExt("Verlustrate").setCellRenderer(new LossRatioTableCellRenderer());
        jAttacksTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jAttacksTable.setColumnControlVisible(false);
        jAttacksTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jAttacksTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jAttacksTable.requestFocus();

        jSupportsTable.setModel(new SupportsModel());
        jSupportsTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jSupportsTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jSupportsTable.setDefaultRenderer(Date.class, new DateCellRenderer());

        offensePanel = new TroopSelectionPanel();
        offensePanel.setupOffense(true);
        Hashtable<de.tor.tribes.io.UnitHolder, Integer> offAmounts = new Hashtable<de.tor.tribes.io.UnitHolder, Integer>();
        offAmounts.put(DataHolder.getSingleton().getUnitByPlainName("axe"), 7000);
        offAmounts.put(DataHolder.getSingleton().getUnitByPlainName("light"), 2300);
        offAmounts.put(DataHolder.getSingleton().getUnitByPlainName("ram"), 250);
        offensePanel.setAmounts(offAmounts);
        jPanel1.add(offensePanel, BorderLayout.CENTER);

        defensePanel = new TroopSelectionPanel();
        defensePanel.setupDefense(true);
        Hashtable<UnitHolder, Integer> defAmounts = new Hashtable<UnitHolder, Integer>();
        defAmounts.put(DataHolder.getSingleton().getUnitByPlainName("spear"), 500);
        defAmounts.put(DataHolder.getSingleton().getUnitByPlainName("sword"), 500);
        defAmounts.put(DataHolder.getSingleton().getUnitByPlainName("heavy"), 100);
        jPanel2.add(defensePanel, BorderLayout.CENTER);
        defensePanel.setAmounts(defAmounts);
        offensePanel.setEnabled(false);
        defensePanel.setEnabled(false);
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

    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTopBox.isSelected());

        PropertyHelper.storeTableProperties(jAttacksTable, pConfig, getPropertyPrefix());
    }

    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));

        try {
            jAlwaysOnTopBox.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception e) {
        }

        setAlwaysOnTop(jAlwaysOnTopBox.isSelected());

        PropertyHelper.restoreTableProperties(jAttacksTable, pConfig, getPropertyPrefix());
    }

    @Override
    public String getPropertyPrefix() {
        return "sos.view";
    }

    private void buildMenu() {

        JXTaskPane viewPane = new JXTaskPane();
        viewPane.setTitle("Ansicht");
        JXButton toSosView = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/axe24.png")));
        toSosView.setToolTipText("Eingelesene SOS-Anfragen anzeigen");
        toSosView.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                jScrollPane6.setViewportView(jAttacksTable);
            }
        });
        viewPane.getContentPane().add(toSosView);

        JXButton toSupportView = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/sword24.png")));
        toSupportView.setToolTipText("Errechnete Unterstützungen anzeigen");
        toSupportView.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                jScrollPane6.setViewportView(jSupportsTable);
            }
        });

        viewPane.getContentPane().add(toSupportView);

        JXTaskPane transferPane = new JXTaskPane();
        transferPane.setTitle("Übertragen");

        JXButton toDefensePlanner = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/def_auto_small.png")));
        toDefensePlanner.setToolTipText("Überträgt die gewählten SOS-Anfragen in den automatischen Verteidigungsplaner");
        toDefensePlanner.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                List<DefenseInformation> selection = getSelectedRows();
                DefenseAnalysePanel.getSingleton().setData(selection);
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        DefensePlanerWizard.show();
                    }
                });
            }
        });
        transferPane.getContentPane().add(toDefensePlanner);

        JXButton toSupport = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/support_tool.png")));
        toSupport.setToolTipText("Überträgt den ersten Angriff der gewählten SOS-Anfrage in das Unterstützungswerkzeug");
        toSupport.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                transferToSupportTool();
            }
        });

        transferPane.getContentPane().add(toSupport);

        JXButton toRetime = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/re-time.png")));
        toRetime.setToolTipText("Überträgt den ersten Angriff der gewählten SOS-Anfrage in den ReTimer");
        toRetime.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                transferToRetimeTool();
            }
        });

        transferPane.getContentPane().add(toRetime);


        JXTaskPane miscPane = new JXTaskPane();
        miscPane.setTitle("Sonstiges");

        JXButton analyzeSettings = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/sos_settings.png")));
        analyzeSettings.setToolTipText("Öffnet die Einstellungen für die Analyse von SOS-Anfragen");
        analyzeSettings.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                jDefenseSettingsFrame.pack();
                jDefenseSettingsFrame.setLocationRelativeTo(DSWorkbenchSOSRequestAnalyzer.this);
                jDefenseSettingsFrame.setVisible(true);
            }
        });
        miscPane.getContentPane().add(analyzeSettings);

        centerPanel.setupTaskPane(viewPane, transferPane, miscPane);
    }

    public DefenseToolModel getModel() {
        return TableHelper.getTableModel(jAttacksTable);
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
        List<DefenseInformation> attacks = getSelectedRows();
        if (attacks.isEmpty()) {
            return;
        }
        VillageSupportFrame.getSingleton().showSupportFrame(attacks.get(0).getTarget(), attacks.get(0).getFirstAttack().getTime());
    }

    private void transferToRetimeTool() {
        List<DefenseInformation> attacks = getSelectedRows();
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

        b.append(PluginManager.getSingleton().getVariableValue("sos.source")).append(" ").
                append(attacks.get(0).getTargetInformation().getFirstTimedAttack().getSource().toString()).append("\n");
        b.append("Ziel: ").append(attacks.get(0).getTarget().toString()).append("\n");
        b.append(PluginManager.getSingleton().getVariableValue("attack.arrive.time")).append(" ").
                append(f.format(attacks.get(0).getFirstAttack())).append("\n");
        DSWorkbenchReTimerFrame.getSingleton().setCustomAttack(b.toString());
        if (!DSWorkbenchReTimerFrame.getSingleton().isVisible()) {
            DSWorkbenchReTimerFrame.getSingleton().setVisible(true);
        }
    }

    /*
     * private void copySelectionToInternalClipboard() { List<Attack> selection = getSelectedAttacks(); if (selection.isEmpty()) { return; }
     *
     * StringBuilder b = new StringBuilder(); int cnt = 0; for (Attack a : selection) { if (a.getUnit() == null) {
     * a.setUnit(UnknownUnit.getSingleton()); } b.append(Attack.toInternalRepresentation(a)).append("\n"); cnt++; } try {
     * Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null); showSuccess(cnt + ((cnt == 1)
     * ? " Angriff kopiert" : " Angriffe kopiert")); } catch (HeadlessException hex) { showError("Fehler beim Kopieren der Angriffe"); } }
     *//*
     * private void copySelectionToClipboardAsBBCode() { if (currentRequests == null || currentRequests.isEmpty()) { showInfo("Keine SOS
     * Anfragen eingelesen"); return; } try { boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes
     * verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);
     *
     * StringBuilder buffer = new StringBuilder(); if (extended) { buffer.append("[u][size=12]SOS Anfragen[/size][/u]\n\n"); } else {
     * buffer.append("[u]SOS Anfragen[/u]\n\n"); }
     *
     * List<SOSRequest> requests = new LinkedList<SOSRequest>();
     *
     * Enumeration<Tribe> tribeKeys = currentRequests.keys(); while (tribeKeys.hasMoreElements()) {
     * requests.add(currentRequests.get(tribeKeys.nextElement())); } buffer.append(new SosListFormatter().formatElements(requests,
     * extended));
     *
     * if (extended) { buffer.append("\n[size=8]Erstellt am "); buffer.append(new SimpleDateFormat("dd.MM.yy 'um'
     * HH:mm:ss").format(Calendar.getInstance().getTime())); buffer.append(" mit [url=\"http://www.dsworkbench.de/index.php?id=23\"]DS
     * Workbench "); buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/url][/size]\n"); } else {
     * buffer.append("\nErstellt am "); buffer.append(new SimpleDateFormat("dd.MM.yy 'um'
     * HH:mm:ss").format(Calendar.getInstance().getTime())); buffer.append(" mit [url=\"http://www.dsworkbench.de/index.php?id=23\"]DS
     * Workbench "); buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/url]\n"); }
     *
     * String b = buffer.toString(); StringTokenizer t = new StringTokenizer(b, "["); int cnt = t.countTokens(); if (cnt > 1000) { if
     * (JOptionPaneHelper.showQuestionConfirmBox(this, "Die momentan vorhandenen Anfragen benötigen mehr als 1000 BB-Codes\n" + "und können
     * daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja")
     * == JOptionPane.NO_OPTION) { return; } }
     *
     * Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null); showSuccess("Daten in Zwischenablage
     * kopiert"); } catch (Exception e) { logger.error("Failed to copy data to clipboard", e); showError("Fehler beim Kopieren in die
     * Zwischenablage"); }
     *
     * }
     *
     * private void cutSelectionToInternalClipboard() { List<Attack> selection = getSelectedAttacks(); copySelectionToInternalClipboard();
     * removeSelection(false); showSuccess(((selection.size() == 1) ? "Angriff" : selection.size() + " Angriffe") + " ausgeschnitten"); }
     */

    public de.tor.tribes.io.UnitHolder getSlowestUnit() {
        Hashtable<UnitHolder, Integer> units = defensePanel.getAmounts();
        Enumeration<UnitHolder> keys = units.keys();
        UnitHolder slowest = null;
        while (keys.hasMoreElements()) {
            UnitHolder key = keys.nextElement();
            if (units.get(key) != null && units.get(key) != 0) {
                if (slowest == null) {
                    slowest = key;
                } else {
                    if (key.getSpeed() > slowest.getSpeed()) {
                        slowest = key;
                    }
                }
            }
        }
        return slowest;
    }

    private void removeSelection() {
        removeSelection(true);
    }

    private void removeSelection(boolean pAsk) {
        int[] selectedRows = jAttacksTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length < 1) {
            showInfo("Keine Angriffe ausgewählt");
            return;
        }

        if (!pAsk || JOptionPaneHelper.showQuestionConfirmBox(this, "Willst du " + ((selectedRows.length == 1) ? "den gewählten Angriff " : "die gewählten Angriffe ") + "wirklich löschen?", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            DefaultTableModel model = (DefaultTableModel) jAttacksTable.getModel();
            int numRows = selectedRows.length;
            for (int i = 0; i < numRows; i++) {
                model.removeRow(jAttacksTable.convertRowIndexToModel(jAttacksTable.getSelectedRow()));
            }
            showSuccess(((numRows == 1) ? "Angriff" : numRows + " Angriffe") + " gelöscht");
        }
    }

    private List<DefenseInformation> getSelectedRows() {
        List<DefenseInformation> infos = new LinkedList<DefenseInformation>();
        if (getModel().getRowCount() == 0) {
            showInfo("Keine SOS Anfragen vorhanden");
        } else {
            int[] rows = jAttacksTable.getSelectedRows();
            if (rows == null || rows.length == 0) {
                showInfo("Kein Angriff gewählt");
                return infos;
            } else {
                for (int row : rows) {
                    infos.add(getModel().getRows()[jAttacksTable.convertRowIndexToModel(row)]);
                }
            }
        }
        return infos;
    }

    /*
     * private List<Attack> getSelectedAttacks() { List<Attack> attacks = new LinkedList<Attack>(); if (currentRequests == null) {
     * showInfo("Keine SOS Anfragen vorhanden"); return attacks; }
     *
     * int[] rows = jResultTable.getSelectedRows(); if (rows == null || rows.length == 0) { showInfo("Kein Angriff gewählt"); return
     * attacks; }
     *
     * for (int row : rows) { Village target = (Village) jResultTable.getValueAt(row, 1); Village source = (Village)
     * jResultTable.getValueAt(row, 3); String arrive = (String) jResultTable.getValueAt(row, 4); int type = (Integer)
     * jResultTable.getValueAt(row, 8); SimpleDateFormat f = null; if (!ServerSettings.getSingleton().isMillisArrival()) { f = new
     * SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format")); } else { f = new
     * SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms")); }
     *
     * Attack a = new Attack(); a.setSource(source); a.setTarget(target); if (type == Attack.SNOB_TYPE) {
     * a.setUnit(DataHolder.getSingleton().getUnitByPlainName("snob")); } else if (type == Attack.FAKE_TYPE) {
     * a.setUnit(DataHolder.getSingleton().getUnitByPlainName("ram")); } try { a.setArriveTime(f.parse(arrive)); attacks.add(a); } catch
     * (ParseException ex) { showError("Fehler beim Lesen der Ankunftszeit"); } } return attacks; }
     */
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSOSInputPanel = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButton1 = new javax.swing.JButton();
        jResultPanel = new org.jdesktop.swingx.JXPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXInfoLabel = new org.jdesktop.swingx.JXLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jAttacksTable = new org.jdesktop.swingx.JXTable();
        jDefenseSettingsFrame = new javax.swing.JFrame();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jAcceptButton = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jSupportsTable = new org.jdesktop.swingx.JXTable();
        jSOSPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();

        jSOSInputPanel.setMinimumSize(new java.awt.Dimension(500, 400));
        jSOSInputPanel.setPreferredSize(new java.awt.Dimension(500, 400));
        jSOSInputPanel.setLayout(new java.awt.GridBagLayout());

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/analyze.png"))); // NOI18N
        jButton3.setMaximumSize(new java.awt.Dimension(73, 50));
        jButton3.setMinimumSize(new java.awt.Dimension(73, 50));
        jButton3.setPreferredSize(new java.awt.Dimension(73, 50));
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAnalyzeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSOSInputPanel.add(jButton3, gridBagConstraints);

        jProgressBar1.setMinimumSize(new java.awt.Dimension(100, 20));
        jProgressBar1.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSOSInputPanel.add(jProgressBar1, gridBagConstraints);

        jButton1.setMaximumSize(new java.awt.Dimension(73, 60));
        jButton1.setMinimumSize(new java.awt.Dimension(73, 60));
        jButton1.setPreferredSize(new java.awt.Dimension(73, 60));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireReadDataFromClipboardEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSOSInputPanel.add(jButton1, gridBagConstraints);

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

        jAttacksTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane6.setViewportView(jAttacksTable);

        jResultPanel.add(jScrollPane6, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 10.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSOSInputPanel.add(jResultPanel, gridBagConstraints);

        jDefenseSettingsFrame.setTitle("Einstellungen");
        jDefenseSettingsFrame.getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Standard Off"));
        jPanel1.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDefenseSettingsFrame.getContentPane().add(jPanel1, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Einzelunterstützung"));
        jPanel2.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDefenseSettingsFrame.getContentPane().add(jPanel2, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Sonstige Einstellungen"));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("%");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel3.add(jLabel3, gridBagConstraints);

        jLabel1.setText("Max. Simulationsrunden");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel1, gridBagConstraints);

        jLabel2.setText("Max. Verlustrate");
        jLabel2.setMaximumSize(new java.awt.Dimension(114, 14));
        jLabel2.setMinimumSize(new java.awt.Dimension(114, 14));
        jLabel2.setPreferredSize(new java.awt.Dimension(114, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel2, gridBagConstraints);

        jTextField1.setText("500");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jTextField1, gridBagConstraints);

        jTextField2.setText("30");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jTextField2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDefenseSettingsFrame.getContentPane().add(jPanel3, gridBagConstraints);

        jButton2.setText("Abbrechen");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAcceptSettingsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        jDefenseSettingsFrame.getContentPane().add(jButton2, gridBagConstraints);

        jAcceptButton.setText("Übernehmen");
        jAcceptButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAcceptSettingsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDefenseSettingsFrame.getContentPane().add(jAcceptButton, gridBagConstraints);

        jButton4.setBackground(new java.awt.Color(255, 102, 102));
        jButton4.setText("Truppeneinstellungen bearbeiten");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireEditTroopsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDefenseSettingsFrame.getContentPane().add(jButton4, gridBagConstraints);

        jSupportsTable.setModel(new javax.swing.table.DefaultTableModel(
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

private void fireAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopEvent
    setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_fireAlwaysOnTopEvent

    private void fireReadDataFromClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireReadDataFromClipboardEvent
        readRequestFromClipboard();
    }//GEN-LAST:event_fireReadDataFromClipboardEvent

    private void fireEditTroopsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireEditTroopsEvent
        if (JOptionPaneHelper.showWarningConfirmBox(jDefenseSettingsFrame,
                "Das Ändern der Truppeneinstellungen führt dazu, dass alle bisher berechneten\n"
                + "Unterstützungen und Verteidigungsanalysen ungültig werden.\n"
                + "Aus Sicherheitsgründen wird DS Workbench sie daher LÖSCHEN!\n"
                + "Wurden noch keine Unterstützungen berechnet, kannst du diese Warnung ignorieren.\n"
                + "Willst du die Truppeneinstellungen wirklich ändern?", "Warnung", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            defensePanel.setEnabled(true);
            offensePanel.setEnabled(true);
        }
    }//GEN-LAST:event_fireEditTroopsEvent

    private void fireAcceptSettingsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAcceptSettingsEvent
        if (evt.getSource() == jAcceptButton) {
            if (offensePanel.isEnabled() || defensePanel.isEnabled()) {
                for (ManageableType t : SOSManager.getSingleton().getAllElements()) {
                    SOSRequest r = (SOSRequest) t;
                    r.resetDefenses();
                }
                getModel().fireTableDataChanged();
            }
            offensePanel.setEnabled(false);
            defensePanel.setEnabled(false);
        }

        jDefenseSettingsFrame.setVisible(false);
    }//GEN-LAST:event_fireAcceptSettingsEvent

    private void fireAnalyzeEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAnalyzeEvent
        if (a == null || !a.isRunning()) {
            jProgressBar1.setValue(0);
            a = new DefenseAnalyzer(new DefenseAnalyzer.DefenseAnalyzerListener() {

                @Override
                public void fireProceedEvent(double pStatus) {
                    int status = (int) Math.rint(pStatus * 100.0);
                    jProgressBar1.setValue((int) Math.rint(pStatus * 100.0));
                    if (status % 10 == 0) {
                        jAttacksTable.repaint();
                    }
                }

                @Override
                public void fireFinishedEvent() {
                    jButton3.setText("Analyze");
                    updateSupportTable();
                }
            }, offensePanel.getAmounts(), defensePanel.getAmounts(), UIHelper.parseIntFromField(jTextField1, 500), UIHelper.parseIntFromField(jTextField2, 30));
            a.start();
            jButton3.setText("Cancel");
        } else {
            if (jButton3.getText().equals("Cancel")) {
                a.abort();
            }
        }
    }//GEN-LAST:event_fireAnalyzeEvent

    public Hashtable<UnitHolder, Integer> getDefense() {
        return defensePanel.getAmounts();
    }

    private void updateSupportTable() {
        SupportsModel model = TableHelper.getTableModel(jSupportsTable);
        model.clear();
        for (ManageableType t : SOSManager.getSingleton().getAllElements()) {
            SOSRequest r = (SOSRequest) t;
            Enumeration<Village> targets = r.getTargets();
            while (targets.hasMoreElements()) {
                Village target = targets.nextElement();
                DefenseInformation d = r.getDefenseInformation(target);
                for (Defense i : d.getSupports()) {
                    model.addRow(i);
                }
            }
        }
        model.fireTableDataChanged();
    }

    private void readRequestFromClipboard() {
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            String data = (String) t.getTransferData(DataFlavor.stringFlavor);

            List<SOSRequest> requests = PluginManager.getSingleton().executeSOSParserParser(data);
            if (requests != null && !requests.isEmpty()) {
                for (SOSRequest request : requests) {
                    SOSManager.getSingleton().addRequest(request);
                    findFakes(request);

                }
            } else {
                showInfo("Keine gültigen SOS Anfrage gefunden");
            }
            updateView();
        } catch (HeadlessException he) {
            showInfo("Fehler beim Lesen aus der Zwischenablage");
        } catch (UnsupportedFlavorException usfe) {
            showInfo("Fehler beim Lesen aus der Zwischenablage");
        } catch (IOException ioe) {
            showInfo("Fehler beim Lesen aus der Zwischenablage");
        }
    }

    private void updateView() {
        DefenseToolModel model = getModel();
        model.clear();

        for (ManageableType e : SOSManager.getSingleton().getAllElements()) {
            SOSRequest r = (SOSRequest) e;
            Enumeration<Village> targets = r.getTargets();
            while (targets.hasMoreElements()) {
                model.addRow(r.getDefenseInformation(targets.nextElement()));
            }
        }
        model.fireTableDataChanged();
    }

    private void findFakes(SOSRequest pRequest) {
        Enumeration<Village> targets = pRequest.getTargets();
        // List<Village> foundSources = new LinkedList<Village>();
        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            TargetInformation targetInfo = pRequest.getTargetInformation(target);

            //check for multiple attacks from same source to same target
            Enumeration<Village> sources = targetInfo.getSources();
            while (sources.hasMoreElements()) {
                Village source = sources.nextElement();
                if (targetInfo.getAttackCountFromSource(source) > 1) {
                    for (TimedAttack att : targetInfo.getAttacksFromSource(source)) {
                        if (!att.isPossibleFake() && !att.isPossibleSnob()) {//check only once
                            long sendTime = att.getlArriveTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(source, target, DataHolder.getSingleton().getUnitByPlainName("ram").getSpeed()) * 1000);
                            if (sendTime < System.currentTimeMillis()) {
                                att.setPossibleSnob(false);
                                att.setPossibleFake(true);
                            } else {
                                att.setPossibleSnob(true);
                                att.setPossibleFake(false);
                            }
                        }
                    }
                }

                /*
                 * if (foundSources.contains(source)) { } else { foundSources.add(source); }
                 */
            }
        }
    }

    private static void createSampleRequests() {
        int wallLevel = 20;
        int supportCount = 20;
        int maxAttackCount = 10;
        int maxFakeCount = 0;

        Village[] villages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
        Village[] attackerVillages = DataHolder.getSingleton().getTribeByName("Alexander25").getVillageList();

        for (int i = 0; i < supportCount; i++) {
            int id = (int) Math.rint(Math.random() * (villages.length - 1));
            Village target = villages[id];
            SOSRequest r = new SOSRequest(target.getTribe());
            TargetInformation info = r.addTarget(target);
            info.setWallLevel(wallLevel);

            info.addTroopInformation(DataHolder.getSingleton().getUnitByPlainName("spear"), (int) Math.rint(Math.random() * 14000));
            info.addTroopInformation(DataHolder.getSingleton().getUnitByPlainName("sword"), (int) Math.rint(Math.random() * 14000));
            info.addTroopInformation(DataHolder.getSingleton().getUnitByPlainName("heavy"), (int) Math.rint(Math.random() * 5000));

            int cnt = (int) Math.rint(maxAttackCount * Math.random());
            for (int j = 0; j < cnt; j++) {
                int idx = (int) Math.rint(Math.random() * (attackerVillages.length - 2));
                Village v = attackerVillages[idx];
                info.addAttack(v, new Date(System.currentTimeMillis() + Math.round(DateUtils.MILLIS_PER_DAY * 7 * Math.random())));
                for (int k = 0; k < (int) Math.rint(maxFakeCount * Math.random()); k++) {
                    idx = (int) Math.rint(Math.random() * (attackerVillages.length - 2));
                    v = attackerVillages[idx];
                    info.addAttack(v, new Date(System.currentTimeMillis() + Math.round(3600 * Math.random())));
                }
            }
            SOSManager.getSingleton().addRequest(r);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);
        DataHolder.getSingleton().loadData(false);
        GlobalOptions.loadUserData();
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        createSampleRequests();
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
    private javax.swing.JButton jAcceptButton;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private org.jdesktop.swingx.JXTable jAttacksTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JFrame jDefenseSettingsFrame;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JProgressBar jProgressBar1;
    private org.jdesktop.swingx.JXPanel jResultPanel;
    private javax.swing.JPanel jSOSInputPanel;
    private javax.swing.JPanel jSOSPanel;
    private javax.swing.JScrollPane jScrollPane6;
    private org.jdesktop.swingx.JXTable jSupportsTable;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private org.jdesktop.swingx.JXLabel jXInfoLabel;
    // End of variables declaration//GEN-END:variables
}
