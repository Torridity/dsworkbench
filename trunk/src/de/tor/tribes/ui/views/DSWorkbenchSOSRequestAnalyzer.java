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
import de.tor.tribes.ui.renderer.AlternatingColorCellRenderer;
import de.tor.tribes.ui.renderer.AttackTypeCellRenderer;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.SortableTableHeaderRenderer;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.ui.renderer.WalLevellCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.attack.AttackManager;
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
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
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
                    "Verteidiger", "Ziel", "Angreifer", "Herkunft", "Ankunft", "Angriffe", "Kampfkraft/Angriff", "Wall"}) {

            Class[] types = new Class[]{
                Tribe.class, Village.class, Tribe.class, Village.class, String.class, Integer.class, Double.class, Integer.class
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
        jResultTable.getColumnExt("Wall").setCellRenderer(new WalLevellCellRenderer());
        jResultTable.setColumnControlVisible(true);
        jResultTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jResultTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jResultTable.requestFocus();
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        // GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.sos_analyzer", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>
    }
    public void storeCustomProperties(Configuration pCconfig) {
    }
 public void restoreCustomProperties(Configuration pConfig) {
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
        copySelectionToInternalClipboard();
        removeSelection(false);
        showSuccess("Einträge ausgeschnitten");
    }

    private void removeSelection() {
        removeSelection(true);
    }

    private void removeSelection(boolean pAsk) {
        int[] selectedRows = jResultTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length < 1) {
            showInfo("Keine Einträge ausgewählt");
            return;
        }

        if (!pAsk || JOptionPaneHelper.showQuestionConfirmBox(this, "Willst du " + ((selectedRows.length == 1) ? "den gewählten Eintrag " : "die gewählten Einträge ") + "wirklich löschen?", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            DefaultTableModel model = (DefaultTableModel) jResultTable.getModel();
            int numRows = selectedRows.length;
            for (int i = 0; i < numRows; i++) {
                model.removeRow(jResultTable.convertRowIndexToModel(jResultTable.getSelectedRow()));
            }
            showSuccess("Einträge gelöscht");
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
            SimpleDateFormat f = null;
            if (!ServerSettings.getSingleton().isMillisArrival()) {
                f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format"));
            } else {
                f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms"));
            }

            Attack a = new Attack();
            a.setSource(source);
            a.setTarget(target);
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSosTextField = new javax.swing.JTextArea();
        jStatusLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jDefenderList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTargetList = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTroopsInfoField = new javax.swing.JTextPane();
        jLabel6 = new javax.swing.JLabel();
        jWallLevelBar = new javax.swing.JProgressBar();
        jWallLevelText = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jAttacksTableScrollPanel = new javax.swing.JScrollPane();
        jAttacksTable = new javax.swing.JTable();
        jTaskPane1 = new com.l2fprod.common.swing.JTaskPane();
        jTaskPaneGroup1 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jCleanupAttacksButton = new javax.swing.JButton();
        jCleanupAttacksButton1 = new javax.swing.JButton();
        jCleanupAttacksButton2 = new javax.swing.JButton();
        jCleanupAttacksButton3 = new javax.swing.JButton();
        jSOSInputPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jXInputArea = new org.jdesktop.swingx.JXTextArea();
        jResultPanel = new org.jdesktop.swingx.JXPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXInfoLabel = new org.jdesktop.swingx.JXLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jSOSPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.CapabilityInfoPanel();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jLabel1.setText("SOS Anfrage");

        jSosTextField.setColumns(20);
        jSosTextField.setRows(5);
        jSosTextField.setToolTipText("SOS Anfragen hierhin kopieren");
        jSosTextField.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireSosTextChangedEvent(evt);
            }
        });
        jScrollPane1.setViewportView(jSosTextField);

        jStatusLabel.setText("Bitte SOS Anfrage in Eingabefeld einfügen");
        jStatusLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Gefundene SOS Anfragen"));
        jPanel2.setOpaque(false);

        jScrollPane2.setMaximumSize(new java.awt.Dimension(130, 132));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(130, 132));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(130, 132));

        jDefenderList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fireDefenderSelectionChangedEvent(evt);
            }
        });
        jScrollPane2.setViewportView(jDefenderList);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Verteidiger");

        jScrollPane3.setMaximumSize(new java.awt.Dimension(130, 132));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(130, 132));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(130, 132));

        jTargetList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fireTargetChangedEvent(evt);
            }
        });
        jScrollPane3.setViewportView(jTargetList);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Ziele");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Verteidigungsstatus"));
        jPanel3.setOpaque(false);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/wall.png"))); // NOI18N

        jTroopsInfoField.setContentType("text/html");
        jTroopsInfoField.setToolTipText("Truppen im Dorf");
        jScrollPane5.setViewportView(jTroopsInfoField);

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/face.png"))); // NOI18N

        jWallLevelBar.setForeground(new java.awt.Color(0, 204, 0));
        jWallLevelBar.setMaximum(20);
        jWallLevelBar.setToolTipText("Wallstufe");
        jWallLevelBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jWallLevelBar.setBorderPainted(false);
        jWallLevelBar.setString("0");

        jWallLevelText.setText("0");
        jWallLevelText.setMaximumSize(new java.awt.Dimension(20, 14));
        jWallLevelText.setMinimumSize(new java.awt.Dimension(20, 14));
        jWallLevelText.setPreferredSize(new java.awt.Dimension(20, 14));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jWallLevelBar, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jWallLevelText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jWallLevelBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jWallLevelText, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Angriffe"));
        jPanel4.setOpaque(false);

        jAttacksTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Angreifer", "Herkunft", "Ankunft"
            }
        ));
        jAttacksTableScrollPanel.setViewportView(jAttacksTable);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jAttacksTableScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jAttacksTableScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTaskPane1.setOpaque(false);
        com.l2fprod.common.swing.PercentLayout percentLayout1 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout1.setGap(14);
        percentLayout1.setOrientation(1);
        jTaskPane1.setLayout(percentLayout1);

        jTaskPaneGroup1.setTitle("Übertragen");
        com.l2fprod.common.swing.PercentLayout percentLayout2 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout2.setOrientation(1);
        jTaskPaneGroup1.getContentPane().setLayout(percentLayout2);

        jCleanupAttacksButton.setBackground(new java.awt.Color(239, 235, 223));
        jCleanupAttacksButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboardBB.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jCleanupAttacksButton.setText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.text")); // NOI18N
        jCleanupAttacksButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.toolTipText")); // NOI18N
        jCleanupAttacksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopySupportsToClipboardEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jCleanupAttacksButton);

        jCleanupAttacksButton1.setBackground(new java.awt.Color(239, 235, 223));
        jCleanupAttacksButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/support_tool.png"))); // NOI18N
        jCleanupAttacksButton1.setText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.text")); // NOI18N
        jCleanupAttacksButton1.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.toolTipText")); // NOI18N
        jCleanupAttacksButton1.setMaximumSize(new java.awt.Dimension(59, 35));
        jCleanupAttacksButton1.setMinimumSize(new java.awt.Dimension(59, 35));
        jCleanupAttacksButton1.setPreferredSize(new java.awt.Dimension(59, 35));
        jCleanupAttacksButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireOpenSupportToolEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jCleanupAttacksButton1);

        jCleanupAttacksButton2.setBackground(new java.awt.Color(239, 235, 223));
        jCleanupAttacksButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/re-time.png"))); // NOI18N
        jCleanupAttacksButton2.setText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.text")); // NOI18N
        jCleanupAttacksButton2.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.toolTipText")); // NOI18N
        jCleanupAttacksButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveAttackToReTimeToolEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jCleanupAttacksButton2);

        jCleanupAttacksButton3.setBackground(new java.awt.Color(239, 235, 223));
        jCleanupAttacksButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_overview.png"))); // NOI18N
        jCleanupAttacksButton3.setText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.text")); // NOI18N
        jCleanupAttacksButton3.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.toolTipText")); // NOI18N
        jCleanupAttacksButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyToAttackViewEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jCleanupAttacksButton3);

        jTaskPane1.add(jTaskPaneGroup1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTaskPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTaskPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jStatusLabel)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

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
        setMinimumSize(new java.awt.Dimension(830, 500));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jSOSPanel.setBackground(new java.awt.Color(239, 235, 223));
        jSOSPanel.setMinimumSize(new java.awt.Dimension(500, 400));
        jSOSPanel.setPreferredSize(new java.awt.Dimension(600, 500));
        jSOSPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jSOSPanel, gridBagConstraints);

        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jAlwaysOnTopBoxfireAlwaysOnTopChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTopBox, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireSosTextChangedEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireSosTextChangedEvent
        List<SOSRequest> requests = PluginManager.getSingleton().executeSOSParserParser(jSosTextField.getText());
        if (requests != null && !requests.isEmpty()) {
            for (SOSRequest request : requests) {
                currentRequests.put(request.getDefender(), request);
            }
        } else {
            currentRequests = new Hashtable<Tribe, SOSRequest>();
        }

        if (currentRequests == null || currentRequests.isEmpty()) {
            jStatusLabel.setText("Keine gültigen SOS Anfrage(n) gefunden");
            jStatusLabel.setBackground(Color.RED);
        } else {
            jStatusLabel.setText("SOS Anfrage(n) erfolgreich gelesen");
            jStatusLabel.setBackground(Color.GREEN);
        }

        updateView();
    }//GEN-LAST:event_fireSosTextChangedEvent

    private void fireDefenderSelectionChangedEvent(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fireDefenderSelectionChangedEvent
        Tribe t = (Tribe) jDefenderList.getSelectedValue();
        if (t == null || currentRequests == null) {
            return;
        }
        SOSRequest request = currentRequests.get(t);
        DefaultListModel targetModel = new DefaultListModel();
        Enumeration<Village> targetVillages = request.getTargets();
        while (targetVillages.hasMoreElements()) {
            targetModel.addElement(targetVillages.nextElement());
        }

        jTargetList.setModel(targetModel);
    }//GEN-LAST:event_fireDefenderSelectionChangedEvent

    private void fireTargetChangedEvent(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fireTargetChangedEvent

        Object[] targets = jTargetList.getSelectedValues();
        if (targets == null || currentRequests == null) {
            return;
        }
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Angreifer", "Herkunft", "Ankunft", "Typ"
                }) {

            Class[] types = new Class[]{
                Tribe.class, Village.class, Date.class, Integer.class
            };

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        String troopInfo = "";
        for (Object o : targets) {
            Village target = (Village) o;
            SOSRequest request = currentRequests.get(target.getTribe());
            if (target == null) {
                return;
            }
            if (request == null) {
                jTroopsInfoField.setText("<html>Fehler beim Lesen der Anfrage. M&ouml;glicherweise wurde das Dorf geadelt.<BR/><u>Aktueller Besitzer:</u> " + target.getTribe() + "</html>");
                jWallLevelText.setText(Integer.toString(0));
                jWallLevelBar.setValue(0);
                jAttacksTable.setModel(model);

                TableRowSorter<TableModel> attackSorter = new TableRowSorter<TableModel>(jAttacksTable.getModel());
                jAttacksTable.setRowSorter(attackSorter);
                DefaultTableCellRenderer headerRenderer = new SortableTableHeaderRenderer();
                for (int i = 0; i < jAttacksTable.getColumnCount(); i++) {
                    jAttacksTable.getColumn(jAttacksTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
                }
                return;
            }
            jAttacksTable.setRowHeight(18);
            jAttacksTable.getTableHeader().setReorderingAllowed(false);
            SOSRequest.TargetInformation info = request.getTargetInformation(target);
            int wall = info.getWallLevel();
            jWallLevelText.setText(Integer.toString(wall));
            jWallLevelBar.setValue(wall);


            int r = 240 - ((wall >= 10) ? (wall - 1) * 12 : 0);
            int g = (wall >= 10) ? 120 + (wall - 10) * 12 : 120 - ((10 - wall) * 12);

            //240 0 0
            //240 120 0
            //0 240 *

            //wall = 240 - wall*12
            //wall > 10: g = 120 + (lev-10)*12, wall < 10: g = 120 - lev*12

            jWallLevelBar.setForeground(new Color(r, g, 0));
            troopInfo += info.getTroopInformationAsHTML() + "<BR/>";
            for (SOSRequest.TimedAttack attack : info.getAttacks()) {
                int guessedType = Attack.NO_TYPE;
                if (attack.isPossibleFake()) {
                    guessedType = Attack.FAKE_TYPE;
                } else if (attack.isPossibleSnob()) {
                    guessedType = Attack.SNOB_TYPE;
                }
                model.addRow(new Object[]{attack.getSource().getTribe(), attack.getSource(), new Date(attack.getlArriveTime()), guessedType});
            }
        }

        jTroopsInfoField.setText("<html>" + troopInfo + "</html>");
        jAttacksTable.setModel(model);
        jAttacksTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jAttacksTable.setDefaultRenderer(Integer.class, new AttackTypeCellRenderer());
        AlternatingColorCellRenderer rend = new AlternatingColorCellRenderer();
        jAttacksTable.setDefaultRenderer(Tribe.class, rend);
        jAttacksTable.setDefaultRenderer(Village.class, rend);
        TableRowSorter<TableModel> attackSorter = new TableRowSorter<TableModel>(jAttacksTable.getModel());
        jAttacksTable.setRowSorter(attackSorter);
        DefaultTableCellRenderer headerRenderer = new SortableTableHeaderRenderer();

        for (int i = 0; i < jAttacksTable.getColumnCount(); i++) {
            jAttacksTable.getColumn(jAttacksTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }
    }//GEN-LAST:event_fireTargetChangedEvent

    private void fireCopySupportsToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopySupportsToClipboardEvent
        if (currentRequests == null) {
            return;
        }
        boolean showDetails = (JOptionPaneHelper.showQuestionConfirmBox(this, "Sollen alle Einzelangriffe aufgeführt werden?", "Detailierter Export", "Nein", "Ja") == JOptionPane.YES_OPTION);

        try {
            Object[] targetVillages = jTargetList.getSelectedValues();
            StringBuffer buffer = new StringBuffer();
            if (targetVillages != null && targetVillages.length > 0) {
                //copy single target villages
                List<Village> relevantTargets = new LinkedList<Village>();
                for (Object o : targetVillages) {
                    relevantTargets.add((Village) o);
                }
                Enumeration<Tribe> requestKeys = currentRequests.keys();

                while (requestKeys.hasMoreElements()) {
                    Tribe t = requestKeys.nextElement();
                    SOSRequest request = currentRequests.get(t);
                    Enumeration<Village> targets = request.getTargets();
                    while (targets.hasMoreElements()) {
                        Village target = targets.nextElement();
                        if (relevantTargets.contains(target)) {
                            String bbCode = request.toBBCode(target, showDetails);
                            buffer.append(bbCode.trim() + "\n");
                        }
                    }
                }
            } else {
                Object[] targetTribes = jDefenderList.getSelectedValues();
                if (targetTribes == null && targetTribes.length == 0) {
                    JOptionPaneHelper.showInformationBox(this, "Keine Verteidiger oder Ziele ausgewählt.", "Fehler");
                    return;
                }


                for (Object o : targetTribes) {
                    Tribe t = (Tribe) o;
                    SOSRequest request = currentRequests.get(t);
                    String bbCode = request.toBBCode(showDetails);
                    buffer.append(bbCode.trim() + "\n");
                }
            }


            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(buffer.toString()), null);
            String result = "Daten in Zwischenablage kopiert.";
            JOptionPaneHelper.showInformationBox(this, result, "Information");
        } catch (Exception e) {
            logger.error("Error while copying support to clipboard", e);
            JOptionPaneHelper.showInformationBox(this, "Fehler beim kopieren in die Zwischenablage", "Fehler");
        }
    }//GEN-LAST:event_fireCopySupportsToClipboardEvent

    private void fireOpenSupportToolEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireOpenSupportToolEvent
        if (currentRequests == null) {
            return;
        }

        Village targetVillage = (Village) jTargetList.getSelectedValue();
        if (targetVillage == null) {
            return;
        }

        Tribe t = targetVillage.getTribe();
        SOSRequest request = currentRequests.get(t);
        if (request == null) {
            JOptionPaneHelper.showInformationBox(this, "Fehler beim Lesen der Anfrage. Möglicherweise wurde das Dorf bereits geadelt.", "Fehler");
            return;
        }
        SOSRequest.TargetInformation info = request.getTargetInformation(targetVillage);
        if (info.getAttacks() == null || info.getAttacks().size() == 0) {
            return;
        }
        SOSRequest.TimedAttack attack = info.getAttacks().get(0);

        VillageSupportFrame.getSingleton().showSupportFrame(targetVillage, attack.getlArriveTime());

    }//GEN-LAST:event_fireOpenSupportToolEvent

    private void fireMoveAttackToReTimeToolEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMoveAttackToReTimeToolEvent
        boolean haveTarget = false;
        boolean haveSource = false;
        boolean haveArrive = false;
        try {
            Village target = (Village) jTargetList.getSelectedValue();
            if (target == null) {
                throw new Exception("No target selected");
            }
            haveTarget = true;
            int row = jAttacksTable.convertRowIndexToModel(jAttacksTable.getSelectedRow());

            Village source = (Village) jAttacksTable.getValueAt(row, 1);
            haveSource = true;
            Date arrive = (Date) jAttacksTable.getValueAt(row, 2);
            haveArrive = true;
            StringBuffer b = new StringBuffer();

            b.append(PluginManager.getSingleton().getVariableValue("sos.source") + " " + source.toString() + "\n");
            b.append("Ziel: " + target.toString() + "\n");
            SimpleDateFormat f = null;
            if (!ServerSettings.getSingleton().isMillisArrival()) {
                f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format"));
            } else {
                f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms"));
            }
            b.append(PluginManager.getSingleton().getVariableValue("attack.arrive.time") + " " + f.format(arrive) + "\n");
            DSWorkbenchReTimerFrame.getSingleton().setCustomAttack(b.toString());
            if (!DSWorkbenchReTimerFrame.getSingleton().isVisible()) {
                DSWorkbenchReTimerFrame.getSingleton().setVisible(true);
            }

        } catch (Exception e) {
            logger.error("Failed to submit SOS attack to re-time tool", e);
            String message = "Fehler beim Übertragen in das ReTime Werkzeug.";
            if (!haveTarget) {
                message += "\nKein Ziel ausgewählt?";
            } else if (!haveSource || !haveArrive) {
                message += "\nKeinen Angriff ausgewählt?";
            }
            JOptionPaneHelper.showErrorBox(this, message, "Fehler");
        }

    }//GEN-LAST:event_fireMoveAttackToReTimeToolEvent

    private void fireCopyToAttackViewEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyToAttackViewEvent

        Tribe defender = (Tribe) jDefenderList.getSelectedValue();
        Object[] aTargets = jTargetList.getSelectedValues();
        int[] rows = jAttacksTable.getSelectedRows();

        List<Village> sources = new LinkedList<Village>();
        if (rows != null && rows.length > 0) {
            for (int row : rows) {
                int r = jAttacksTable.convertRowIndexToModel(row);
                Village source = (Village) jAttacksTable.getValueAt(r, 1);
                sources.add(source);
            }
        }

        List<Village> targets = new LinkedList<Village>();
        if (aTargets != null && aTargets.length > 0) {
            for (Object target : aTargets) {
                targets.add((Village) target);
            }
        }
        if (defender == null) {
            JOptionPaneHelper.showInformationBox(this, "Kein Verteidiger gewählt", "Fehler");
            return;
        }

        //build attacks
        List<Attack> attacks = new LinkedList<Attack>();
        SOSRequest request = currentRequests.get(defender);

        //use all targets
        Enumeration<Village> targetEnum = request.getTargets();
        while (targetEnum.hasMoreElements()) {
            Village target = targetEnum.nextElement();
            if (targets.isEmpty() || targets.contains(target)) {
                SOSRequest.TargetInformation info = request.getTargetInformation(target);
                List<SOSRequest.TimedAttack> targetAttacks = info.getAttacks();
                for (SOSRequest.TimedAttack ta : targetAttacks) {
                    if (!sources.isEmpty() && sources.contains(ta.getSource())) {
                        Attack a = new Attack();
                        a.setSource(ta.getSource());
                        a.setTarget(target);
                        a.setArriveTime(new Date(ta.getlArriveTime()));
                        a.setUnit(UnknownUnit.getSingleton());
                        if (ta.isPossibleFake()) {
                            a.setType(Attack.FAKE_TYPE);
                        } else {
                            a.setType(Attack.NO_TYPE);
                        }
                        attacks.add(a);
                    }
                }
            }
        }

        boolean doFilter = (JOptionPaneHelper.showQuestionConfirmBox(this, "Soll DS Workbench versuchen, bereits eingelesene Angriffe herauszufiltern?", "Angriffe übertragen", "Nein", "Ja") == JOptionPane.YES_OPTION);

        String plan = DSWorkbenchAttackFrame.getSingleton().getActivePlan();
        int added = 0;
        List<ManageableType> planAttacks = AttackManager.getSingleton().getAllElements(plan);

        AttackManager.getSingleton().invalidate();
        for (Attack newAttack : attacks) {

            boolean exists = false;
            if (doFilter) {
                for (ManageableType existingElement : planAttacks) {
                    Attack existingAttack = (Attack) existingElement;
                    if (newAttack.compareTo(existingAttack) == 0) {
                        exists = true;
                        break;
                    }
                }
            }
            if (!exists) {
                AttackManager.getSingleton().addAttack(newAttack.getSource(), newAttack.getTarget(), newAttack.getUnit(), newAttack.getArriveTime(), plan);
            }
            added++;
        }
        AttackManager.getSingleton().revalidate();
        if (added > 0) {
            JOptionPaneHelper.showInformationBox(this, added + ((added == 1) ? " Angriff" : " Angriffe") + " in Angriffsplan '" + plan + "' eingefügt.", "Information");
        }
    }//GEN-LAST:event_fireCopyToAttackViewEvent

    private void jAlwaysOnTopBoxfireAlwaysOnTopChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jAlwaysOnTopBoxfireAlwaysOnTopChangedEvent
        setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_jAlwaysOnTopBoxfireAlwaysOnTopChangedEvent

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
                    sosTableModel.addRow(new Object[]{defender,
                                target,
                                attack.getSource().getTribe(),
                                attack.getSource(),
                                f.format(new Date(attack.getlArriveTime())),
                                attackCount,
                                defensePower / attackCount, wall});
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
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JTable jAttacksTable;
    private javax.swing.JScrollPane jAttacksTableScrollPanel;
    private javax.swing.JButton jCleanupAttacksButton;
    private javax.swing.JButton jCleanupAttacksButton1;
    private javax.swing.JButton jCleanupAttacksButton2;
    private javax.swing.JButton jCleanupAttacksButton3;
    private javax.swing.JList jDefenderList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private org.jdesktop.swingx.JXPanel jResultPanel;
    private static final org.jdesktop.swingx.JXTable jResultTable = new org.jdesktop.swingx.JXTable();
    private javax.swing.JPanel jSOSInputPanel;
    private javax.swing.JPanel jSOSPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTextArea jSosTextField;
    private javax.swing.JLabel jStatusLabel;
    private javax.swing.JList jTargetList;
    private com.l2fprod.common.swing.JTaskPane jTaskPane1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup1;
    private javax.swing.JTextPane jTroopsInfoField;
    private javax.swing.JProgressBar jWallLevelBar;
    private javax.swing.JLabel jWallLevelText;
    private org.jdesktop.swingx.JXLabel jXInfoLabel;
    private org.jdesktop.swingx.JXTextArea jXInputArea;
    // End of variables declaration//GEN-END:variables
}
