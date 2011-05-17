/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchReTimerFrame.java
 *
 * Created on 22.12.2009, 13:43:21
 */
package de.tor.tribes.ui.views;

import com.jidesoft.swing.JideTabbedPane;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.TroopFilterElement;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.GenericTestPanel;
import de.tor.tribes.ui.renderer.AlternatingColorCellRenderer;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.SortableTableHeaderRenderer;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.ui.renderer.UnitListCellRenderer;
import de.tor.tribes.util.BBCodeFormatter;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.bb.AttackListFormatter;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * @author Jejkal
 */
public class DSWorkbenchReTimerFrame extends AbstractDSWorkbenchFrame implements ListSelectionListener, ActionListener {

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jResultTable.getSelectedRowCount();

            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Angriff gewählt" : " Angriffe gewählt"));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Copy")) {
            copySelectionToInternalClipboard();
        } else if (e.getActionCommand().equals("Delete")) {
            removeSelection();
        }
    }
    private static Logger logger = Logger.getLogger("ReTimeTool");
    private static DSWorkbenchReTimerFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;

    public static synchronized DSWorkbenchReTimerFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchReTimerFrame();
        }
        return SINGLETON;
    }
    private Attack parsedAttack = null;

    /** Creates new form DSWorkbenchReTimerFrame */
    DSWorkbenchReTimerFrame() {
        initComponents();

        centerPanel = new GenericTestPanel(true);
        jReTimePanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildPanel(jideRetimeTabbedPane);
        jideRetimeTabbedPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
        jideRetimeTabbedPane.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
        jideRetimeTabbedPane.setBoldActiveTab(true);
        jideRetimeTabbedPane.addTab("Festlegen des Angriffs", jInputPanel);
        jideRetimeTabbedPane.addTab("Errechnete Gegenangriffe", jResultPanel);
        buildMenu();

        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.CTRL_MASK, false);
        jResultTable.registerKeyboardAction(DSWorkbenchReTimerFrame.this, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jResultTable.registerKeyboardAction(DSWorkbenchReTimerFrame.this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jResultTable.getSelectionModel().addListSelectionListener(DSWorkbenchReTimerFrame.this);
        jPossibleUnits.setCellRenderer(new UnitListCellRenderer());
        jPossibleUnits.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateAttackBBView();
                }
            }
        });

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        //  GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.retime_tool", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>
    }

    private void buildMenu() {
        JXTaskPane editPane = new JXTaskPane();
        editPane.setTitle("Bearbeiten");
        JXButton filterRetimes = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/filter_strength.png")));
        filterRetimes.setToolTipText("<html>Filtern der gefundenen ReTime Angriffe nach der bekannten Truppenst&auml;rke im Dorf<br/>Es ist ratsam, vor der Filterung zu pr&uuml;fen, ob die in DS Workbench importierten<br/>"
                + "Truppeninformationen aktuell sind</html>");
        filterRetimes.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                showFilterDialog();
            }
        });

        editPane.getContentPane().add(filterRetimes);
        JXTaskPane transferPane = new JXTaskPane();
        transferPane.setTitle("Übertragen");
        JXButton transferBB = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/att_clipboardBB.png")));
        transferBB.setToolTipText("ReTime Angriffe als BB-Code in die Zwischenablage kopieren");
        transferBB.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                copyAttacksToClipboardAsBBCode();
            }
        });
        transferPane.getContentPane().add(transferBB);
        JXTaskPane miscPane = new JXTaskPane();
        miscPane.setTitle("Sonstiges");
        JXButton calculate = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/att_validate.png")));
        calculate.setToolTipText("ReTime Angriffe für den eingestellten Angriff berechnen");
        calculate.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                startCalculation();
            }
        });
        miscPane.getContentPane().add(calculate);
        centerPanel.setupTaskPane(editPane, transferPane, miscPane);
    }

// <editor-fold defaultstate="collapsed" desc="Test data">

    /*OPERA
    Herkunft	Spieler:	Rattenfutter
    Dorf:	001 Rattennest (486|833) K84
    Ziel	Spieler:	Rattenfutter
    Dorf:	005 Rattennest (486|834) K84
    Ankunft:	22.12.09 13:57:44:321
    Ankunft in:	0:08:34
     *
     *
     *
     * FF
    Herkunft	Spieler:	Rattenfutter
    Dorf:	001 Rattennest (486|833) K84
    Ziel	Spieler:	Rattenfutter
    Dorf:	005 Rattennest (486|834) K84
    Dauer:	0:09:00
    Ankunft:	22.12.09 14:02:30:232
    Ankunft in:	0:08:41
    » abbrechen
     */
    /*
    Befehl
    Herkunft	Spieler:	Rattenfutter
    Dorf:	015 R.I.P. Frankfurt Lions 01 (382|891) K83
    Ziel	Spieler:	Rattenfutter
    Dorf:	Metropolis L06 (384|891) K83
    Dauer:	1:00:00
    Ankunft:	01.02.11 22:54:00:670
    Ankunft in:	0:59:57
    » abbrechen
    » Versammlungsplatz
     */
    // </editor-fold>
    @Override
    public void resetView() {
    }

    private void copySelectionToInternalClipboard() {
        jideRetimeTabbedPane.setSelectedIndex(1);
        List<Attack> selection = getSelectedAttacks();
        if (selection.isEmpty()) {
            showInfo("Keine Angriffe ausgewählt");
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

    private void copyAttacksToClipboardAsBBCode() {
        jideRetimeTabbedPane.setSelectedIndex(1);
        try {
            List<Attack> attacks = getSelectedAttacks();
            if (attacks.isEmpty()) {
                showInfo("Keine Angriffe ausgewählt");
                return;
            }
            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

            StringBuilder buffer = new StringBuilder();
            if (extended) {
                buffer.append("[u][size=12]Angriffsplan[/size][/u]\n\n");
            } else {
                buffer.append("[u]Angriffsplan[/u]\n\n");
            }

            buffer.append(new AttackListFormatter().formatElements(attacks, extended));

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
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Angriffe benötigen mehr als 1000 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
            String result = "Daten in Zwischenablage kopiert.";
            showSuccess(result);
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            String result = "Fehler beim Kopieren in die Zwischenablage.";
            showError(result);
        }
    }

    private void removeSelection() {
        jideRetimeTabbedPane.setSelectedIndex(1);
        int[] selectedRows = jResultTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length < 1) {
            showInfo("Keine Einträge ausgewählt");
            return;
        }

        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Willst du " + ((selectedRows.length == 1) ? "den gewählten Eintrag " : "die gewählten Einträge ") + "wirklich löschen?", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
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
        int[] selectedRows = jResultTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length < 1) {
            showInfo("Keine Einträge ausgewählt");
            return attacks;
        }

        for (Integer selectedRow : selectedRows) {
            int row = jResultTable.convertRowIndexToModel(selectedRow);
            Village source = (Village) jResultTable.getValueAt(row, 0);
            UnitHolder unit = (UnitHolder) jResultTable.getValueAt(row, 1);
            Village target = (Village) jResultTable.getValueAt(row, 2);
            Date sendTime = (Date) jResultTable.getValueAt(row, 3);
            double dist = DSCalculator.calculateDistance(source, target);
            long runtime = Math.round(dist * unit.getSpeed() * 60000);
            Date arriveTime = new Date(sendTime.getTime() + runtime);
            Attack a = new Attack();
            a.setSource(source);
            a.setTarget(target);
            a.setUnit(unit);
            a.setArriveTime(arriveTime);
            a.setType(Attack.CLEAN_TYPE);
            attacks.add(a);
        }

        return attacks;
    }

    private void startCalculation() {
        jideRetimeTabbedPane.setSelectedIndex(0);
        if (parsedAttack == null
                || parsedAttack.getSource() == null
                || parsedAttack.getTarget() == null
                || parsedAttack.getUnit() == null
                || parsedAttack.getArriveTime() == null) {

            JOptionPaneHelper.showInformationBox(this, "Bitte füge zuerst einen gültigen Angriff ein und wähle eine Einheit", "Information");
            return;
        }

        jCalculationSettingsDialog.pack();
        jCalculationSettingsDialog.setLocationRelativeTo(DSWorkbenchReTimerFrame.this);
        DefaultListModel tagModel = new DefaultListModel();
        tagModel.addElement(NoTag.getSingleton());
        for (ManageableType e : TagManager.getSingleton().getAllElements()) {
            tagModel.addElement((Tag) e);
        }
        jTagList.setModel(tagModel);
        jRelationBox.setSelected(true);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            model.addElement(unit);
        }
        jUnitBox.setModel(model);
        jUnitBox.setRenderer(new UnitListCellRenderer());
        // <editor-fold defaultstate="collapsed" desc="Build attack plan table">
        DefaultTableModel attackPlabTableModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Angriffsplan", "Abgleichen"}) {

            Class[] types = new Class[]{
                String.class, Boolean.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == 0) {
                    return false;
                }
                return true;
            }
        };

        String[] plans = AttackManager.getSingleton().getGroups();
        for (String plan : plans) {
            attackPlabTableModel.addRow(new Object[]{plan, false});
        }

        jAttackPlanTable.setModel(attackPlabTableModel);
        DefaultTableCellRenderer headerRenderer = new SortableTableHeaderRenderer();
        for (int i = 0; i < jAttackPlanTable.getColumnCount(); i++) {
            jAttackPlanTable.getColumn(jAttackPlanTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }
        // </editor-fold>
        jCalculationSettingsDialog.setVisible(true);
    }

    public void showInfo(String pMessage) {
        infoPanel.setCollapsed(false);
        jXInfoLabel.setBackgroundPainter(new MattePainter(getBackground()));
        jXInfoLabel.setForeground(Color.BLACK);
        jXInfoLabel.setText(pMessage);
    }

    public void showSuccess(String pMessage) {
        infoPanel.setCollapsed(false);
        jXInfoLabel.setBackgroundPainter(new MattePainter(Color.GREEN));
        jXInfoLabel.setForeground(Color.BLACK);
        jXInfoLabel.setText(pMessage);
    }

    public void showError(String pMessage) {
        infoPanel.setCollapsed(false);
        jXInfoLabel.setBackgroundPainter(new MattePainter(Color.RED));
        jXInfoLabel.setForeground(Color.WHITE);
        jXInfoLabel.setText(pMessage);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jButton3 = new javax.swing.JButton();
        jAttackPlanSelectionDialog = new javax.swing.JDialog();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jNewAttackPlanField = new javax.swing.JTextField();
        jExistingAttackPlanBox = new javax.swing.JComboBox();
        jInsertButton = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jFilterDialog = new javax.swing.JDialog();
        jPanel6 = new javax.swing.JPanel();
        jFilterUnitBox = new javax.swing.JComboBox();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jButton17 = new javax.swing.JButton();
        jMinValue = new javax.swing.JTextField();
        jMaxValue = new javax.swing.JTextField();
        jButton20 = new javax.swing.JButton();
        jApplyFiltersButton = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        jFilterList = new javax.swing.JList();
        jLabel28 = new javax.swing.JLabel();
        jButton18 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jParserInfo = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jSourceVillage = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTargetVillage = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jArriveField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jAxeBox = new javax.swing.JCheckBox();
        jSwordBox = new javax.swing.JCheckBox();
        jSpyBox = new javax.swing.JCheckBox();
        jLightBox = new javax.swing.JCheckBox();
        jHeavyBox = new javax.swing.JCheckBox();
        jRamBox = new javax.swing.JCheckBox();
        jPalaBox = new javax.swing.JCheckBox();
        jSnobBox = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jEstSendTime = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jReturnField = new javax.swing.JTextField();
        jInputPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jComandArea = new javax.swing.JTextArea();
        jScrollPane6 = new javax.swing.JScrollPane();
        jBBTextPane = new javax.swing.JTextPane();
        jScrollPane7 = new javax.swing.JScrollPane();
        jPossibleUnits = new javax.swing.JList();
        jideRetimeTabbedPane = new com.jidesoft.swing.JideTabbedPane();
        jResultPanel = new org.jdesktop.swingx.JXPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXInfoLabel = new org.jdesktop.swingx.JXLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jCalculationSettingsDialog = new javax.swing.JDialog();
        jSettingsPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTagList = new javax.swing.JList();
        jRelationBox = new javax.swing.JCheckBox();
        jUnitBox = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jAttackPlanTable = new javax.swing.JTable();
        jDoCalculateButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jReTimePanel = new javax.swing.JPanel();
        jMainAlwaysOnTopBox = new javax.swing.JCheckBox();
        capabilityInfoPanel1 = new de.tor.tribes.ui.CapabilityInfoPanel();

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_overview.png"))); // NOI18N
        jButton3.setToolTipText("Markierte Angriffe in die Angriffsübersicht einfügen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowAttackPlanSelectionDialogEvent(evt);
            }
        });

        jAttackPlanSelectionDialog.setTitle("Angriffsplanauswahl");
        jAttackPlanSelectionDialog.setAlwaysOnTop(true);

        jLabel10.setText("Existierender Plan");

        jLabel11.setText("Neuer Plan");

        jInsertButton.setText("Einfügen");
        jInsertButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferAttacksToAttackViewEvent(evt);
            }
        });

        jButton5.setText("Abbrechen");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferAttacksToAttackViewEvent(evt);
            }
        });

        javax.swing.GroupLayout jAttackPlanSelectionDialogLayout = new javax.swing.GroupLayout(jAttackPlanSelectionDialog.getContentPane());
        jAttackPlanSelectionDialog.getContentPane().setLayout(jAttackPlanSelectionDialogLayout);
        jAttackPlanSelectionDialogLayout.setHorizontalGroup(
            jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackPlanSelectionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jAttackPlanSelectionDialogLayout.createSequentialGroup()
                        .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jNewAttackPlanField, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                            .addComponent(jExistingAttackPlanBox, 0, 284, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAttackPlanSelectionDialogLayout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jInsertButton)))
                .addContainerGap())
        );
        jAttackPlanSelectionDialogLayout.setVerticalGroup(
            jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackPlanSelectionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jExistingAttackPlanBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jNewAttackPlanField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jInsertButton)
                    .addComponent(jButton5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("TribeTribeAttackFrame.jPanel3.border.title"))); // NOI18N

        jFilterUnitBox.setMaximumSize(new java.awt.Dimension(51, 25));
        jFilterUnitBox.setMinimumSize(new java.awt.Dimension(51, 25));
        jFilterUnitBox.setPreferredSize(new java.awt.Dimension(51, 25));

        jLabel25.setText(bundle.getString("TribeTribeAttackFrame.jLabel25.text")); // NOI18N

        jLabel26.setText(bundle.getString("TribeTribeAttackFrame.jLabel26.text")); // NOI18N

        jLabel27.setText(bundle.getString("TribeTribeAttackFrame.jLabel27.text")); // NOI18N

        jButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton17.setText(bundle.getString("TribeTribeAttackFrame.jButton17.text")); // NOI18N
        jButton17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddTroopFilterEvent(evt);
            }
        });

        jMinValue.setText(bundle.getString("TribeTribeAttackFrame.jMinValue.text")); // NOI18N
        jMinValue.setMaximumSize(new java.awt.Dimension(51, 25));
        jMinValue.setMinimumSize(new java.awt.Dimension(51, 25));
        jMinValue.setPreferredSize(new java.awt.Dimension(51, 25));

        jMaxValue.setText(bundle.getString("TribeTribeAttackFrame.jMaxValue.text")); // NOI18N
        jMaxValue.setMaximumSize(new java.awt.Dimension(51, 25));
        jMaxValue.setMinimumSize(new java.awt.Dimension(51, 25));
        jMaxValue.setPreferredSize(new java.awt.Dimension(51, 25));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addComponent(jLabel26))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jMinValue, 0, 0, Short.MAX_VALUE)
                            .addComponent(jFilterUnitBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                        .addComponent(jLabel27)
                        .addGap(18, 18, 18)
                        .addComponent(jMaxValue, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton17, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(jFilterUnitBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMinValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMaxValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton17)
                .addContainerGap())
        );

        jButton20.setText(bundle.getString("TribeTribeAttackFrame.jButton20.text")); // NOI18N
        jButton20.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireApplyTroopFiltersEvent(evt);
            }
        });

        jApplyFiltersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jApplyFiltersButton.setText(bundle.getString("TribeTribeAttackFrame.jApplyFiltersButton.text")); // NOI18N
        jApplyFiltersButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireApplyTroopFiltersEvent(evt);
            }
        });

        jScrollPane14.setViewportView(jFilterList);

        jLabel28.setText(bundle.getString("TribeTribeAttackFrame.jLabel28.text")); // NOI18N

        jButton18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jButton18.setText(bundle.getString("TribeTribeAttackFrame.jButton18.text")); // NOI18N
        jButton18.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton18.toolTipText")); // NOI18N
        jButton18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveTroopFilterEvent(evt);
            }
        });

        javax.swing.GroupLayout jFilterDialogLayout = new javax.swing.GroupLayout(jFilterDialog.getContentPane());
        jFilterDialog.getContentPane().setLayout(jFilterDialogLayout);
        jFilterDialogLayout.setHorizontalGroup(
            jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFilterDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jFilterDialogLayout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFilterDialogLayout.createSequentialGroup()
                                .addComponent(jButton20)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButton18, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jApplyFiltersButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jFilterDialogLayout.setVerticalGroup(
            jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFilterDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel28)
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jApplyFiltersButton)
                    .addComponent(jButton20))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 203, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 169, Short.MAX_VALUE)
        );

        jLabel5.setText("Status");

        jScrollPane2.setBorder(null);
        jScrollPane2.setMaximumSize(new java.awt.Dimension(32767, 50));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(21, 50));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(2, 50));

        jParserInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jParserInfo.setEditable(false);
        jParserInfo.setToolTipText("Statusmeldungen über das Einlesen des Angriffsbefehls");
        jScrollPane2.setViewportView(jParserInfo);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Gelesene Werte"));
        jPanel2.setOpaque(false);

        jLabel2.setText("Herkunft");

        jSourceVillage.setToolTipText("Gelesene Herkunft des Angriffs");

        jLabel3.setText("Ziel");

        jTargetVillage.setToolTipText("Gelesenes Ziel des Angriffs");

        jLabel4.setText("Ankunft");

        jArriveField.setToolTipText("Gelesene Ankunftszeit des Angriffs");

        jPanel3.setOpaque(false);
        jPanel3.setLayout(new java.awt.GridLayout(4, 2));

        buttonGroup1.add(jAxeBox);
        jAxeBox.setText("Axt");
        jAxeBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jAxeBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jAxeBox.setDoubleBuffered(true);
        jAxeBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jAxeBox.setOpaque(false);
        jAxeBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jAxeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jAxeBox);

        buttonGroup1.add(jSwordBox);
        jSwordBox.setText("Schwert");
        jSwordBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSwordBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSwordBox.setDoubleBuffered(true);
        jSwordBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jSwordBox.setOpaque(false);
        jSwordBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jSwordBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jSwordBox);

        buttonGroup1.add(jSpyBox);
        jSpyBox.setText("Späher");
        jSpyBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSpyBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSpyBox.setDoubleBuffered(true);
        jSpyBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jSpyBox.setOpaque(false);
        jSpyBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jSpyBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jSpyBox);

        buttonGroup1.add(jLightBox);
        jLightBox.setText("LKav");
        jLightBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jLightBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jLightBox.setDoubleBuffered(true);
        jLightBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jLightBox.setOpaque(false);
        jLightBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jLightBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jLightBox);

        buttonGroup1.add(jHeavyBox);
        jHeavyBox.setText("SKav");
        jHeavyBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jHeavyBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jHeavyBox.setDoubleBuffered(true);
        jHeavyBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jHeavyBox.setOpaque(false);
        jHeavyBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jHeavyBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jHeavyBox);

        buttonGroup1.add(jRamBox);
        jRamBox.setText("Ramme");
        jRamBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jRamBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jRamBox.setDoubleBuffered(true);
        jRamBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jRamBox.setOpaque(false);
        jRamBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jRamBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jRamBox);

        buttonGroup1.add(jPalaBox);
        jPalaBox.setText("Paladin");
        jPalaBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jPalaBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jPalaBox.setDoubleBuffered(true);
        jPalaBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jPalaBox.setOpaque(false);
        jPalaBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jPalaBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jPalaBox);

        buttonGroup1.add(jSnobBox);
        jSnobBox.setText("AG");
        jSnobBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSnobBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSnobBox.setDoubleBuffered(true);
        jSnobBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jSnobBox.setOpaque(false);
        jSnobBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jSnobBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jSnobBox);

        jLabel6.setText("Abschickzeit");

        jEstSendTime.setToolTipText("Abschickzeit des Angriffs unter Verwendung der gewählten Einheit");
        jEstSendTime.setEnabled(false);

        jLabel9.setText("Rückkehr");

        jReturnField.setToolTipText("Rückkehr der Truppen unter Verwendung der gewählten Einheit");
        jReturnField.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jReturnField, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                    .addComponent(jArriveField, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                    .addComponent(jSourceVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                    .addComponent(jTargetVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                    .addComponent(jEstSendTime, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jSourceVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jTargetVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jEstSendTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jArriveField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jReturnField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jScrollPane1.setToolTipText("");

        jComandArea.setColumns(20);
        jComandArea.setRows(5);
        jComandArea.setText("<Kopierten Angriffsbefehl hier einfügen>");
        jComandArea.setToolTipText("Angriffsbefehl hierhin kopieren");
        jComandArea.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireComandDataChangedEvent(evt);
            }
        });
        jScrollPane1.setViewportView(jComandArea);

        jBBTextPane.setContentType("text/html");
        jScrollPane6.setViewportView(jBBTextPane);

        jPossibleUnits.setBorder(javax.swing.BorderFactory.createTitledBorder("Mögliche Einheiten"));
        jScrollPane7.setViewportView(jPossibleUnits);

        javax.swing.GroupLayout jInputPanelLayout = new javax.swing.GroupLayout(jInputPanel);
        jInputPanel.setLayout(jInputPanelLayout);
        jInputPanelLayout.setHorizontalGroup(
            jInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInputPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jInputPanelLayout.setVerticalGroup(
            jInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
                .addContainerGap())
        );

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
        jScrollPane5.setViewportView(jResultTable);

        jResultPanel.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        jCalculationSettingsDialog.setTitle("ReTime Einstellungen");
        jCalculationSettingsDialog.setModal(true);

        jSettingsPanel.setBackground(new java.awt.Color(239, 235, 223));

        jScrollPane3.setBackground(new java.awt.Color(239, 235, 223));
        jScrollPane3.setOpaque(false);

        jTagList.setBorder(javax.swing.BorderFactory.createTitledBorder("Verwendete Dorfgruppen"));
        jTagList.setToolTipText("Zu verwendende Gruppen");
        jScrollPane3.setViewportView(jTagList);

        jRelationBox.setSelected(true);
        jRelationBox.setText("Verknüpfung (UND)");
        jRelationBox.setToolTipText("Verknüpfung der gewählten Dorfgruppen (UND = Dorf muss in allen Gruppen sein, ODER = Dorf muss in mindestens einer Gruppe sein)");
        jRelationBox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jRelationBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_or.png"))); // NOI18N
        jRelationBox.setOpaque(false);
        jRelationBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_and.png"))); // NOI18N
        jRelationBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireRelationChangedEvent(evt);
            }
        });

        jUnitBox.setToolTipText("Langsamste Einheit mit der gegengetimed wird");
        jUnitBox.setMaximumSize(new java.awt.Dimension(40, 25));
        jUnitBox.setMinimumSize(new java.awt.Dimension(40, 25));
        jUnitBox.setPreferredSize(new java.awt.Dimension(40, 25));

        jLabel8.setText("Langsamste Einheit");

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Dörfer aus folgenden Angriffsplänen ignorieren"));

        jAttackPlanTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Title 1", "Title 2"
            }
        ));
        jScrollPane4.setViewportView(jAttackPlanTable);

        javax.swing.GroupLayout jSettingsPanelLayout = new javax.swing.GroupLayout(jSettingsPanel);
        jSettingsPanel.setLayout(jSettingsPanelLayout);
        jSettingsPanelLayout.setHorizontalGroup(
            jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSettingsPanelLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRelationBox, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                    .addGroup(jSettingsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(jUnitBox, 0, 232, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jSettingsPanelLayout.setVerticalGroup(
            jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRelationBox)
                    .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jUnitBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jDoCalculateButton.setText("Berechnen");
        jDoCalculateButton.setToolTipText("");
        jDoCalculateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateReTimingsEvent(evt);
            }
        });

        jButton2.setText("Abbrechen");
        jButton2.setToolTipText("");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateReTimingsEvent(evt);
            }
        });

        javax.swing.GroupLayout jCalculationSettingsDialogLayout = new javax.swing.GroupLayout(jCalculationSettingsDialog.getContentPane());
        jCalculationSettingsDialog.getContentPane().setLayout(jCalculationSettingsDialogLayout);
        jCalculationSettingsDialogLayout.setHorizontalGroup(
            jCalculationSettingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jCalculationSettingsDialogLayout.createSequentialGroup()
                .addContainerGap(418, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDoCalculateButton)
                .addContainerGap())
            .addGroup(jCalculationSettingsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jCalculationSettingsDialogLayout.setVerticalGroup(
            jCalculationSettingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jCalculationSettingsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jCalculationSettingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDoCalculateButton)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        setTitle("Re-Time Werkzeug");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jReTimePanel.setBackground(new java.awt.Color(239, 235, 223));
        jReTimePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jReTimePanel, gridBagConstraints);

        jMainAlwaysOnTopBox.setText("Immer im Vordergrund");
        jMainAlwaysOnTopBox.setOpaque(false);
        jMainAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAlwaysOnTopChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jMainAlwaysOnTopBox, gridBagConstraints);

        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireComandDataChangedEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireComandDataChangedEvent
        parsedAttack = new Attack();
        List<Village> villages = PluginManager.getSingleton().executeVillageParser(jComandArea.getText());
        if (villages == null || villages.isEmpty() || villages.size() < 2) {
            parsedAttack = null;
        } else {
            Village source = villages.get(0);
            Village target = villages.get(1);
            if (jComandArea.getText().indexOf(PluginManager.getSingleton().getVariableValue("sos.arrive.time")) > -1) {
                //change village order for SOS requests
                source = villages.get(1);
                target = villages.get(0);
            }
            parsedAttack.setSource(source);
            parsedAttack.setTarget(target);

            Date arriveDate = null;
            try {
                String text = jComandArea.getText();
                String arrive = null;
                String arriveLine = null;
                if (text.indexOf(PluginManager.getSingleton().getVariableValue("attack.arrive.time")) > -1) {
                    arriveLine = text.substring(text.indexOf(PluginManager.getSingleton().getVariableValue("attack.arrive.time")));
                } else {
                    arriveLine = text.substring(text.indexOf(PluginManager.getSingleton().getVariableValue("sos.arrive.time")));
                }

                StringTokenizer tokenizer = new StringTokenizer(arriveLine, " \t");
                tokenizer.nextToken();
                String date = tokenizer.nextToken();
                String time = tokenizer.nextToken();
                arrive = date.trim() + " " + time.trim();
                SimpleDateFormat f = null;
                if (!ServerSettings.getSingleton().isMillisArrival()) {
                    f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format"));
                } else {
                    f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms"));
                }
                arriveDate = f.parse(arrive);
                parsedAttack.setArriveTime(arriveDate);
            } catch (Exception ignored) {
                parsedAttack = null;
            }

            //calc possible units
            double dist = DSCalculator.calculateDistance(source, target);
            String[] units = new String[]{"axe", "sword", "spy", "light", "heavy", "ram", "knight", "snob"};
            DefaultListModel model = new DefaultListModel();
            for (String unit : units) {
                UnitHolder unitHolder = DataHolder.getSingleton().getUnitByPlainName(unit);
                if (unit != null) {
                    long dur = (long) Math.floor(dist * unitHolder.getSpeed() * 60000.0);
                    if (arriveDate.getTime() - dur <= System.currentTimeMillis()) {
                        model.addElement(unitHolder);
                    }
                }
            }

            if (model.isEmpty()) {
                //no element 
                parsedAttack = null;
            }
            jPossibleUnits.setModel(model);

            UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
            UnitHolder axe = DataHolder.getSingleton().getUnitByPlainName("axe");
            UnitHolder spy = DataHolder.getSingleton().getUnitByPlainName("spy");
            if (model.contains(ram)) {
                jPossibleUnits.setSelectedValue(ram, true);
            } else if (model.contains(axe)) {
                jPossibleUnits.setSelectedValue(axe, true);
            } else {
                jPossibleUnits.setSelectedValue(spy, true);
            }
        }
        updateAttackBBView();
    }//GEN-LAST:event_fireComandDataChangedEvent

    private void fireEstUnitChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireEstUnitChangedEvent
        UnitHolder unit = null;
        if (evt.getSource() == jAxeBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("axe");
        } else if (evt.getSource() == jSwordBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("sword");
        } else if (evt.getSource() == jSpyBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("spy");
        } else if (evt.getSource() == jLightBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("light");
        } else if (evt.getSource() == jHeavyBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("heavy");
        } else if (evt.getSource() == jRamBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("ram");
        } else if (evt.getSource() == jPalaBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("knight");
        } else if (evt.getSource() == jSnobBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("snob");
        }
        SimpleDateFormat f = null;

        if (!ServerSettings.getSingleton().isMillisArrival()) {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format"));
        } else {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms"));
        }
        try {
            Date arrive = f.parse(jArriveField.getText());
            Village source = PluginManager.getSingleton().executeVillageParser(jSourceVillage.getText()).get(0);
            Village target = PluginManager.getSingleton().executeVillageParser(jTargetVillage.getText()).get(0);
            double dur = DSCalculator.calculateMoveTimeInSeconds(source, target, unit.getSpeed()) * 1000.0;
            long send = arrive.getTime() - (long) dur;
            double ret = (double) arrive.getTime() + dur;
            ret /= 1000;
            ret = Math.round(ret + .5);
            ret *= 1000;
            if (ServerSettings.getSingleton().isMillisArrival()) {
                f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss:SSS");
            } else {
                f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss");
            }
            jEstSendTime.setText("~ " + f.format(new Date(send)));
            jReturnField.setText("~ " + f.format(new Date((long) ret)));
        } catch (Exception e) {
            jEstSendTime.setText("(unbekannt)");
            jReturnField.setText("(unbekannt)");
        }
    }//GEN-LAST:event_fireEstUnitChangedEvent

    private void fireCalculateReTimingsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateReTimingsEvent
        if (evt.getSource() == jDoCalculateButton) {
            DefaultTableModel model = (DefaultTableModel) jAttackPlanTable.getModel();

            List<String> selectedPlans = new LinkedList<String>();
            for (int i = 0; i < jAttackPlanTable.getRowCount(); i++) {
                int row = jAttackPlanTable.convertRowIndexToModel(i);
                if ((Boolean) model.getValueAt(row, 1)) {
                    selectedPlans.add((String) model.getValueAt(row, 0));
                }
            }

            // Enumeration<String> plans = AttackManager.getSingleton().getPlans();
            List<Village> ignore = new LinkedList<Village>();
            //process all plans
            for (String plan : selectedPlans) {
                logger.debug("Checking plan '" + plan + "'");
                List<ManageableType> elements = AttackManager.getSingleton().getAllElements(plan);
                //process all attacks
                for (ManageableType element : elements) {
                    Attack a = (Attack) element;
                    if (!ignore.contains(a.getSource())) {
                        ignore.add(a.getSource());

                    }
                }
            }

            Object[] tags = jTagList.getSelectedValues();
            if (tags == null || tags.length == 0) {
                JOptionPaneHelper.showInformationBox(this, "Keine Dorfgruppe ausgewählt", "Information");
                return;
            }

            List<Village> candidates = new LinkedList<Village>();
            for (Object o : tags) {
                Tag t = (Tag) o;
                List<Integer> ids = t.getVillageIDs();
                for (Integer id : ids) {
                    //add all villages tagged by current tag
                    Village v = DataHolder.getSingleton().getVillagesById().get(id);
                    if (!candidates.contains(v) && !ignore.contains(v)) {
                        candidates.add(v);
                    }
                }
            }

            if (jRelationBox.isSelected()) {
                //remove all villages that are not tagges by the current tag
                boolean oneFailed = false;
                Village[] aCandidates = candidates.toArray(new Village[]{});
                for (Village v_tmp : aCandidates) {
                    for (Object o : tags) {
                        Tag t = (Tag) o;
                        if (!t.tagsVillage(v_tmp.getId())) {
                            oneFailed = true;
                            break;
                        }
                    }

                    if (oneFailed) {
                        //at least one tag is not valid for village
                        candidates.remove(v_tmp);
                        oneFailed = false;
                    }
                }
            }

            Village target = null;
            try {
                target = PluginManager.getSingleton().executeVillageParser(jSourceVillage.getText()).get(0);
            } catch (Exception e) {
                //no target set
                return;
            }
            UnitHolder unit = (UnitHolder) jUnitBox.getSelectedItem();
            Hashtable<Village, Date> timings = new Hashtable<Village, Date>();

            for (Village candidate : candidates) {
                double dist = DSCalculator.calculateDistance(candidate, target);
                long runtime = Math.round(dist * unit.getSpeed() * 60000.0);
                SimpleDateFormat f = null;
                if (ServerSettings.getSingleton().isMillisArrival()) {
                    f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss:SSS");
                } else {
                    f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss");
                }
                VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(candidate, TroopsManager.TROOP_TYPE.OWN);
                boolean useVillage = true;
                if (holder != null) {
                    if (holder.getTroopsOfUnitInVillage(unit) == 0) {
                        useVillage = false;
                    }
                }
                if (useVillage) {
                    try {
                        Date ret = f.parse(jReturnField.getText().replaceAll("~", "").trim());
                        long sendTime = ret.getTime() - runtime;
                        if (sendTime > System.currentTimeMillis() + 60000) {
                            timings.put(candidate, new Date(sendTime));
                        }
                    } catch (Exception e) {
                    }
                }
            }

            buildResults(timings, target, unit);
        }
        jCalculationSettingsDialog.setVisible(false);
    }//GEN-LAST:event_fireCalculateReTimingsEvent

    private void fireTransferAttacksToAttackViewEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferAttacksToAttackViewEvent
        if (evt.getSource() == jInsertButton) {
            String planName = jNewAttackPlanField.getText();
            if (planName.length() == 0) {
                planName = (String) jExistingAttackPlanBox.getSelectedItem();
            } else {
                AttackManager.getSingleton().addGroup(planName);
            }
            int[] rows = jResultTable.getSelectedRows();
            AttackManager.getSingleton().invalidate();
            for (int row : rows) {
                Village source = (Village) jResultTable.getValueAt(row, 0);
                UnitHolder unit = (UnitHolder) jResultTable.getValueAt(row, 1);
                Village target = (Village) jResultTable.getValueAt(row, 2);
                Date sendTime = (Date) jResultTable.getValueAt(row, 3);
                double dist = DSCalculator.calculateDistance(source, target);
                long runtime = Math.round(dist * unit.getSpeed() * 60000);
                AttackManager.getSingleton().addAttack(source, target, unit, new Date(sendTime.getTime() + runtime), false, planName, Attack.NO_TYPE, false);
            }
            AttackManager.getSingleton().revalidate();
        }
        jAttackPlanSelectionDialog.setVisible(false);
    }//GEN-LAST:event_fireTransferAttacksToAttackViewEvent

    private void fireShowAttackPlanSelectionDialogEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowAttackPlanSelectionDialogEvent
        /*int[] rows = jResultTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
        JOptionPaneHelper.showInformationBox(jResultFrame, "Keine Angriffe ausgewählt", "Information");
        return;
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(AttackManager.getSingleton().getGroups());
        jExistingAttackPlanBox.setModel(model);
        jExistingAttackPlanBox.setSelectedItem(AttackManager.DEFAULT_GROUP);
        jNewAttackPlanField.setText("");
        jAttackPlanSelectionDialog.pack();
        jAttackPlanSelectionDialog.setLocationRelativeTo(jResultFrame);
        jAttackPlanSelectionDialog.setVisible(true);*/
    }//GEN-LAST:event_fireShowAttackPlanSelectionDialogEvent

    private void fireAlwaysOnTopChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopChangedEvent
        setAlwaysOnTop(jMainAlwaysOnTopBox.isSelected());
    }//GEN-LAST:event_fireAlwaysOnTopChangedEvent

    private void fireRelationChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireRelationChangedEvent
        if (jRelationBox.isSelected()) {
            jRelationBox.setText("Verknüpfung (UND)");
        } else {
            jRelationBox.setText("Verknüpfung (ODER)");
        }
    }//GEN-LAST:event_fireRelationChangedEvent

    private void fireAddTroopFilterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddTroopFilterEvent
        UnitHolder unit = (UnitHolder) jFilterUnitBox.getSelectedItem();
        DefaultListModel filterModel = (DefaultListModel) jFilterList.getModel();
        TroopFilterElement elem = null;
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        try {
            min = Integer.parseInt(jMinValue.getText());
        } catch (Exception e) {
            min = Integer.MIN_VALUE;
        }
        try {
            max = Integer.parseInt(jMaxValue.getText());
        } catch (Exception e) {
            max = Integer.MAX_VALUE;
        }
        if (min > max) {
            int tmp = min;
            min = max;
            max = tmp;
            jMinValue.setText("" + min);
            jMaxValue.setText("" + max);
        }

        for (int i = 0; i < filterModel.size(); i++) {
            TroopFilterElement listElem = (TroopFilterElement) filterModel.get(i);
            if (listElem.getUnit().equals(unit)) {
                //update min and max and return
                listElem.setMin(min);
                listElem.setMax(max);
                jFilterList.repaint();
                return;
            }
        }
        if (elem == null) {
            elem = new TroopFilterElement(unit, min, max);
            filterModel.addElement(elem);
        }
}//GEN-LAST:event_fireAddTroopFilterEvent

    private void fireApplyTroopFiltersEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireApplyTroopFiltersEvent
        if (evt.getSource() == jApplyFiltersButton) {
            DefaultListModel filterModel = (DefaultListModel) jFilterList.getModel();
            List<Integer> rowsToRemove = new LinkedList<Integer>();
            int removeCount = 0;
            for (int i = 0; i < jResultTable.getRowCount(); i++) {
                //go through all rows in attack table and get source village
                Village v = (Village) jResultTable.getValueAt(i, 0);
                for (int j = 0; j < filterModel.size(); j++) {
                    //check for all filters if villag is allowed
                    if (!((TroopFilterElement) filterModel.get(j)).allowsVillage(v)) {
                        //village is not allowed, add to remove list
                        int row = jResultTable.convertRowIndexToModel(i);
                        rowsToRemove.add(row);
                        removeCount++;
                    }
                }
            }

            jResultTable.invalidate();
            for (int i = rowsToRemove.size() - 1; i >= 0; i--) {
                int row = rowsToRemove.get(i);
                ((DefaultTableModel) jResultTable.getModel()).removeRow(row);
            }
            jResultTable.revalidate();
            String message = "Es wurden keine Angriffe entfernt.";
            if (removeCount == 1) {
                message = "Es wurde ein Angriff entfernt.";
            } else if (removeCount > 1) {
                message = "Es wurden " + removeCount + " Angriffe entfernt.";
            }

            JOptionPaneHelper.showInformationBox(jFilterDialog, message, "Information");
        }
        jFilterDialog.setVisible(false);
}//GEN-LAST:event_fireApplyTroopFiltersEvent

    private void fireRemoveTroopFilterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveTroopFilterEvent
        Object[] selection = jFilterList.getSelectedValues();
        if (selection == null || selection.length == 0) {
            return;
        }
        List<TroopFilterElement> toRemove = new LinkedList<TroopFilterElement>();
        for (Object elem : selection) {
            toRemove.add((TroopFilterElement) elem);
        }
        DefaultListModel filterModel = (DefaultListModel) jFilterList.getModel();
        for (TroopFilterElement elem : toRemove) {
            filterModel.removeElement(elem);
        }
        jFilterList.repaint();
}//GEN-LAST:event_fireRemoveTroopFilterEvent

    private void jXInfoLabelfireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXInfoLabelfireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXInfoLabelfireHideInfoEvent

    private void showFilterDialog() {
        jideRetimeTabbedPane.setSelectedIndex(1);
        if (jResultTable.getRowCount() == 0) {
            showInfo("Keine ReTime Angriffe zur Filterung vorhanden");
            return;
        }
        // <editor-fold defaultstate="collapsed" desc="Build filter dialog">
        jFilterUnitBox.setModel(new DefaultComboBoxModel(DataHolder.getSingleton().getUnits().toArray(new UnitHolder[]{})));
        jFilterUnitBox.setRenderer(new UnitListCellRenderer());
        jFilterList.setModel(new DefaultListModel());
// </editor-fold>
        jFilterDialog.pack();
        jFilterDialog.setLocationRelativeTo(DSWorkbenchReTimerFrame.this);
        jFilterDialog.setVisible(true);
    }

    private void updateAttackBBView() {
        StringBuilder bbBuilder = new StringBuilder();
        if (parsedAttack != null
                && parsedAttack.getSource() != null
                && parsedAttack.getTarget() != null
                && jPossibleUnits.getSelectedValue() != null
                && parsedAttack.getArriveTime() != null) {
            SimpleDateFormat f = null;
            if (ServerSettings.getSingleton().isMillisArrival()) {
                f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss:SSS");
            } else {
                f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss");
            }

            bbBuilder.append("[table][**]Ziel[||]").append(parsedAttack.getTarget().toBBCode()).append("[/**]\n");
            bbBuilder.append("[*]Herkunft[|]").append(parsedAttack.getSource().toBBCode()).append("[/*]\n");

            bbBuilder.append("[*]Ankunft[|]").append(f.format(parsedAttack.getArriveTime())).append("[/*]\n");
            UnitHolder u = (UnitHolder) jPossibleUnits.getSelectedValue();
            parsedAttack.setUnit(u);
            bbBuilder.append("[*]Vermutete Einheit[|]" + "[unit]").append(u.getPlainName()).append("[/unit][/*]\n");

            double dur = DSCalculator.calculateMoveTimeInSeconds(parsedAttack.getSource(), parsedAttack.getTarget(), u.getSpeed()) * 1000.0;
            long send = parsedAttack.getArriveTime().getTime() - (long) dur;
            double ret = (double) parsedAttack.getArriveTime().getTime() + dur;
            ret /= 1000;
            ret = Math.round(ret + .5);
            ret *= 1000;

            bbBuilder.append("[*]Errechnete Abschickzeit[|]").append(f.format(new Date(send))).append("[/*]\n");
            bbBuilder.append("[*]Errechnete Rückkehr[|]").append(f.format(new Date((long) ret))).append("[/*][/table]\n");
        } else {
            bbBuilder.append("[b][color=\"FF0000\"]Kein Angriff gefunden[/color][/b]");
        }
        jBBTextPane.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(bbBuilder.toString()) + "</body></html>");
    }

    public void setCustomAttack(String pAttack) {
        jComandArea.setText(pAttack);
        fireComandDataChangedEvent(null);
    }

    private void buildResults(Hashtable<Village, Date> pTimings, Village pTarget, UnitHolder pUnit) {
        DefaultTableModel resultModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Einheit", "Ziel", "Startzeit"}) {

            Class[] types = new Class[]{
                Village.class, UnitHolder.class, Village.class, Date.class
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
        jResultTable.setModel(resultModel);
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(resultModel);
        jResultTable.setRowSorter(sorter);
        Enumeration<Village> sourceKeys = pTimings.keys();
        while (sourceKeys.hasMoreElements()) {
            Village source = sourceKeys.nextElement();
            Date send = pTimings.get(source);
            resultModel.addRow(new Object[]{source, pUnit, pTarget, send});
        }
        jResultTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        AlternatingColorCellRenderer rend = new AlternatingColorCellRenderer();
        jResultTable.setDefaultRenderer(Village.class, rend);
        jResultTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jResultTable.setRowHeight(20);
        DefaultTableCellRenderer headerRenderer = new SortableTableHeaderRenderer();
        for (int i = 0; i < jResultTable.getColumnCount(); i++) {
            jResultTable.getColumn(jResultTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }
        jideRetimeTabbedPane.setSelectedIndex(1);
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        DataHolder.getSingleton().loadData(false);
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }

        DSWorkbenchReTimerFrame.getSingleton().setSize(600, 400);
        DSWorkbenchReTimerFrame.getSingleton().resetView();
        DSWorkbenchReTimerFrame.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DSWorkbenchReTimerFrame.getSingleton().setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JButton jApplyFiltersButton;
    private javax.swing.JTextField jArriveField;
    private javax.swing.JDialog jAttackPlanSelectionDialog;
    private javax.swing.JTable jAttackPlanTable;
    private javax.swing.JCheckBox jAxeBox;
    private javax.swing.JTextPane jBBTextPane;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JDialog jCalculationSettingsDialog;
    private javax.swing.JTextArea jComandArea;
    private javax.swing.JButton jDoCalculateButton;
    private javax.swing.JTextField jEstSendTime;
    private javax.swing.JComboBox jExistingAttackPlanBox;
    private javax.swing.JDialog jFilterDialog;
    private javax.swing.JList jFilterList;
    private javax.swing.JComboBox jFilterUnitBox;
    private javax.swing.JCheckBox jHeavyBox;
    private javax.swing.JPanel jInputPanel;
    private javax.swing.JButton jInsertButton;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JCheckBox jLightBox;
    private javax.swing.JCheckBox jMainAlwaysOnTopBox;
    private javax.swing.JTextField jMaxValue;
    private javax.swing.JTextField jMinValue;
    private javax.swing.JTextField jNewAttackPlanField;
    private javax.swing.JCheckBox jPalaBox;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JTextPane jParserInfo;
    private javax.swing.JList jPossibleUnits;
    private javax.swing.JCheckBox jRamBox;
    private javax.swing.JPanel jReTimePanel;
    private javax.swing.JCheckBox jRelationBox;
    private org.jdesktop.swingx.JXPanel jResultPanel;
    private static final org.jdesktop.swingx.JXTable jResultTable = new org.jdesktop.swingx.JXTable();
    private javax.swing.JTextField jReturnField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JPanel jSettingsPanel;
    private javax.swing.JCheckBox jSnobBox;
    private javax.swing.JTextField jSourceVillage;
    private javax.swing.JCheckBox jSpyBox;
    private javax.swing.JCheckBox jSwordBox;
    private javax.swing.JList jTagList;
    private javax.swing.JTextField jTargetVillage;
    private javax.swing.JComboBox jUnitBox;
    private org.jdesktop.swingx.JXLabel jXInfoLabel;
    private com.jidesoft.swing.JideTabbedPane jideRetimeTabbedPane;
    // End of variables declaration//GEN-END:variables
}
