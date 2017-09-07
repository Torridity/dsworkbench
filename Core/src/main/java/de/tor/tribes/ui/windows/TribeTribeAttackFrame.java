/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.windows;

import com.jidesoft.swing.JideTabbedPane;
import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.*;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.algo.AlgorithmLogPanel;
import de.tor.tribes.ui.algo.SettingsChangedListener;
import de.tor.tribes.ui.algo.SettingsPanel;
import de.tor.tribes.ui.dnd.VillageTransferable;
import de.tor.tribes.ui.editors.FakeCellEditor;
import de.tor.tribes.ui.editors.NoteIconCellEditor;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.panels.DSWorkbenchAttackInfoPanel;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.renderer.*;
import de.tor.tribes.util.*;
import de.tor.tribes.util.algo.AbstractAttackAlgorithm;
import de.tor.tribes.util.algo.AlgorithmListener;
import de.tor.tribes.util.algo.BruteForce;
import de.tor.tribes.util.algo.Iterix;
import de.tor.tribes.util.algo.types.TimeFrame;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.bb.AttackListFormatter;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import org.apache.commons.lang.math.LongRange;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * @author Torridity
 */
public class TribeTribeAttackFrame extends DSWorkbenchGesturedFrame implements
        ActionListener,
        AlgorithmListener,
        DropTargetListener,
        DragGestureListener,
        DragSourceListener,
        SettingsChangedListener,
        GenericManagerListener, ListSelectionListener {

    public enum TRANSFER_TYPE {

        COPY_SOURCE_TO_INTERNAL_CLIPBOARD, CUT_SOURCE_TO_INTERNAL_CLIPBOARD, PASTE_SOURCE_FROM_INTERNAL_CLIPBOARD, DELETE_SOURCE,
        COPY_TARGET_TO_INTERNAL_CLIPBOARD, CUT_TARGET_TO_INTERNAL_CLIPBOARD, PASTE_TARGET_FROM_INTERNAL_CLIPBOARD, DELETE_TARGET,
        COPY_ATTACK_TO_INTERNAL_CLIPBOARD, ATTACK_TO_BB, DELETE_ATTACK
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = 0;
            boolean isResult = false;
            if (e.getSource() != null && e.getSource().equals(jSourcesTable.getSelectionModel())) {
                selectionCount = jSourcesTable.getSelectedRowCount();
            } else if (e.getSource() != null && e.getSource().equals(jVictimTable.getSelectionModel())) {
                selectionCount = jVictimTable.getSelectedRowCount();
            } else if (e.getSource() != null && e.getSource().equals(jResultsTable.getSelectionModel())) {
                selectionCount = jResultsTable.getSelectedRowCount();
                isResult = true;
            }

            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Eintrag gewählt" : " Einträge gewählt"), isResult);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Copy")) {
            if (e.getSource() != null && (e.getSource().equals(jSourcesTable) || e.getSource().equals(jVictimTable))) {
                if (jideTabbedPane1.getSelectedIndex() == 0) {
                    fireTransferEvent(TRANSFER_TYPE.COPY_SOURCE_TO_INTERNAL_CLIPBOARD);
                } else if (jideTabbedPane1.getSelectedIndex() == 1) {
                    fireTransferEvent(TRANSFER_TYPE.COPY_TARGET_TO_INTERNAL_CLIPBOARD);
                }
            } else if (e.getSource() != null && e.getSource().equals(jResultsTable)) {
                fireTransferEvent(TRANSFER_TYPE.COPY_ATTACK_TO_INTERNAL_CLIPBOARD);
            }
        } else if (e.getActionCommand().equals("Paste")) {
            if (e.getSource() != null && (e.getSource().equals(jSourcesTable) || e.getSource().equals(jVictimTable))) {
                if (jideTabbedPane1.getSelectedIndex() == 0) {
                    fireTransferEvent(TRANSFER_TYPE.PASTE_SOURCE_FROM_INTERNAL_CLIPBOARD);
                } else if (jideTabbedPane1.getSelectedIndex() == 1) {
                    fireTransferEvent(TRANSFER_TYPE.PASTE_TARGET_FROM_INTERNAL_CLIPBOARD);
                }
            }
        } else if (e.getActionCommand().equals("Cut")) {
            if (e.getSource() != null && (e.getSource().equals(jSourcesTable) || e.getSource().equals(jVictimTable))) {
                if (jideTabbedPane1.getSelectedIndex() == 0) {
                    fireTransferEvent(TRANSFER_TYPE.CUT_SOURCE_TO_INTERNAL_CLIPBOARD);
                } else if (jideTabbedPane1.getSelectedIndex() == 1) {
                    fireTransferEvent(TRANSFER_TYPE.CUT_TARGET_TO_INTERNAL_CLIPBOARD);
                }
            }
        } else if (e.getActionCommand().equals("Delete")) {
            if (e.getSource() != null && (e.getSource().equals(jSourcesTable) || e.getSource().equals(jVictimTable))) {
                if (jideTabbedPane1.getSelectedIndex() == 0) {
                    fireTransferEvent(TRANSFER_TYPE.DELETE_SOURCE);
                } else if (jideTabbedPane1.getSelectedIndex() == 1) {
                    fireTransferEvent(TRANSFER_TYPE.DELETE_TARGET);
                }
            } else if (e.getSource() != null && e.getSource().equals(jResultsTable)) {
                fireTransferEvent(TRANSFER_TYPE.DELETE_ATTACK);
            }
        } else if (e.getActionCommand().equals("BBCopy")) {
            fireTransferEvent(TRANSFER_TYPE.ATTACK_TO_BB);
        }
    }

    @Override
    public void fireTimeFrameChangedEvent() {
        updateInfo();
    }
    private static Logger logger = Logger.getLogger("AttackPlanner");
    private SettingsPanel mSettingsPanel = null;
    private AlgorithmLogPanel logPanel = null;
    private DragSource dragSource;
    private JFrame mLogFrame = null;
    private TroopSplitDialog mTroopSplitDialog = null;
    private GenericTestPanel centerPanel = null;
    private TroopFilterDialog filterDialog = null;

    /**
     * Creates new form TribeTribeAttackFrame
     */
    public TribeTribeAttackFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jMainPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jxAttackPlanerPanel);
        buildMenu();
        capabilityInfoPanel1.addActionListener(this, jSourcesTable);
        capabilityInfoPanel2.addActionListener(this, jResultsTable);

        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        jSourcesTable.registerKeyboardAction(TribeTribeAttackFrame.this, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jVictimTable.registerKeyboardAction(TribeTribeAttackFrame.this, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jResultsTable.registerKeyboardAction(TribeTribeAttackFrame.this, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jSourcesTable.registerKeyboardAction(TribeTribeAttackFrame.this, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jVictimTable.registerKeyboardAction(TribeTribeAttackFrame.this, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jSourcesTable.registerKeyboardAction(TribeTribeAttackFrame.this, "Cut", cut, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jVictimTable.registerKeyboardAction(TribeTribeAttackFrame.this, "Cut", cut, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jSourcesTable.registerKeyboardAction(TribeTribeAttackFrame.this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jVictimTable.registerKeyboardAction(TribeTribeAttackFrame.this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jResultsTable.registerKeyboardAction(TribeTribeAttackFrame.this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        jResultsTable.registerKeyboardAction(TribeTribeAttackFrame.this, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        Action noFind = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //no find
            }
        };
        jSourcesTable.getActionMap().put("find", noFind);
        jVictimTable.getActionMap().put("find", noFind);
        jResultsTable.getActionMap().put("find", noFind);

        jSourcesTable.getSelectionModel().addListSelectionListener(TribeTribeAttackFrame.this);
        jVictimTable.getSelectionModel().addListSelectionListener(TribeTribeAttackFrame.this);
        jResultsTable.getSelectionModel().addListSelectionListener(TribeTribeAttackFrame.this);

        jideTabbedPane1.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
        jideTabbedPane1.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
        jideTabbedPane1.setBoldActiveTab(true);
        TagManager.getSingleton().addManagerListener(TribeTribeAttackFrame.this);
        logPanel = new AlgorithmLogPanel();
        mLogFrame = new JFrame("Informationen zur Berechnung");
        mLogFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        mLogFrame.setLayout(new BorderLayout());
        mLogFrame.add(logPanel);
        mLogFrame.pack();
        mTroopSplitDialog = new TroopSplitDialog(TribeTribeAttackFrame.this, true);
        mSettingsPanel = new SettingsPanel(this);
        jSettingsContentPanel.add(mSettingsPanel, BorderLayout.CENTER);
        jAttackResultDetailsFrame.pack();
        jTargetResultDetailsFrame.pack();
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(TribeTribeAttackFrame.this, DnDConstants.ACTION_COPY_OR_MOVE, TribeTribeAttackFrame.this);
        new DropTarget(jSourcesTable, TribeTribeAttackFrame.this);
        new DropTarget(jVictimTable, TribeTribeAttackFrame.this);
        for (MouseListener l : jAllTargetsComboBox.getMouseListeners()) {
            jAllTargetsComboBox.removeMouseListener(l);
        }
        jAllTargetsComboBox.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                fireAddFilteredTargetVillages();
            }
        });

        filterDialog = new TroopFilterDialog(this, true);

        // <editor-fold defaultstate="collapsed" desc="Add selection listeners">
        jVillageGroupList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireFilterSourceVillagesByGroupEvent();
                }
            }
        });
        jSourceContinentList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireFilterSourceContinentEvent();
                }
            }
        });
        jTargetTribeList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireFilterTargetByTribeEvent();
                }
            }
        });
        jTargetContinentList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireFilterTargetByContinentEvent();
                }
            }
        });

        jTargetAllyList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireFilterTargetByAllyEvent();
                }
            }
        });
// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelp(jSourcePanel, "pages.attack_planer_source", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelp(jTargetPanel, "pages.attack_planer_target", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelp(mSettingsPanel, "pages.attack_planer_settings", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelpKey(jResultFrame.getRootPane(), "pages.attack_planer_results", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelpKey(jTargetResultDetailsFrame.getRootPane(), "pages.attack_planer_results_details_targets", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelpKey(jAttackResultDetailsFrame.getRootPane(), "pages.attack_planer_results_details_sources", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.attack_planer", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>
    }

    private void buildMenu() {
        ///////filter pane
        JXTaskPane filterPane = new JXTaskPane();
        filterPane.setTitle("Filtern");
        //filter by strength
        JXButton filterTroopsByStrength = new JXButton(new ImageIcon(TribeTribeAttackFrame.class.getResource("/res/ui/filter_strength.png")));
        filterTroopsByStrength.setToolTipText("<html>Filtert Herkunftsd&ouml;rfer nach der Anzahl der Truppen im Dorf<br/>Hierf&uuml;r werden die Truppenzahlen verwendet, die aus dem Spiel<br/>importiert wurden. Achte daher darauf, dass diese Daten immer aktuell sind.</html>");
        filterTroopsByStrength.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                filterByTroopStrength();
            }
        });
        filterPane.getContentPane().add(filterTroopsByStrength);
        //filter by attack plan
        JXButton filterByAttackPlan = new JXButton(new ImageIcon(TribeTribeAttackFrame.class.getResource("/res/ui/filter_off.png")));
        filterByAttackPlan.setToolTipText("Entfernt alle Herkunfts- oder Zieldörfer, die bereits in einem vorhandenen Angriffsplan auftauchen");
        filterByAttackPlan.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                filterByUsage();
            }
        });
        filterPane.getContentPane().add(filterByAttackPlan);
        ///////edit pane
        JXTaskPane editPane = new JXTaskPane();
        editPane.setTitle("Bearbeiten");
        //adapt unit snobs
        JXButton editUnit = new JXButton(new ImageIcon(TribeTribeAttackFrame.class.getResource("/res/ui/standard_attacks.png")));
        editUnit.setToolTipText("<html>Setzt die langsamste Einheit in den gew&auml;hlten Herkunftsd&ouml;rfern auf die Einheit,<br/>die im unteren Einstellungsbereich aktiv ist</html>");
        editUnit.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                editUnit();
            }
        });
        editPane.getContentPane().add(editUnit);

        //use snobs
        JXButton editUseSnobs = new JXButton(new ImageIcon(TribeTribeAttackFrame.class.getResource("/res/ui/use_snob.png")));
        editUseSnobs.setToolTipText("<html>Setzt in D&ouml;rfern, in denen sich laut Truppeninformationen AGs befinden, die langsamste Einheit auf AG<br/>Taucht ein Dorf mehrfach auf werden so oft AGs verwendet wie sich AGs im Dorf befinden.<br/>Hierf&uuml;r werden die Truppenzahlen verwendet, die aus dem Spiel<br/>importiert wurden. Achte daher darauf, dass diese Daten immer aktuell sind.</html>");
        editUseSnobs.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                editUseSnobs();
            }
        });
        editPane.getContentPane().add(editUseSnobs);
        //set fake
        JXButton setFake = new JXButton(new ImageIcon(TribeTribeAttackFrame.class.getResource("/res/ui/fake.png")));
        setFake.setToolTipText("Setzt den Angriffstyp für die markierten Dörfer auf 'Fake'");
        setFake.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                editSetFake(true);
            }
        });
        setFake.setSize(editUseSnobs.getSize());
        setFake.setPreferredSize(editUseSnobs.getPreferredSize());
        setFake.setMinimumSize(editUseSnobs.getMinimumSize());
        setFake.setMaximumSize(editUseSnobs.getMaximumSize());

        editPane.getContentPane().add(setFake);
        //set fake
        JXButton setNoFake = new JXButton(new ImageIcon(TribeTribeAttackFrame.class.getResource("/res/ui/no_fake.png")));
        setNoFake.setToolTipText("Setzt den Angriffstyp für die markierten Dörfer auf 'Kein Fake'");
        setNoFake.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                editSetFake(false);
            }
        });
        setNoFake.setSize(editUseSnobs.getSize());
        setNoFake.setPreferredSize(editUseSnobs.getPreferredSize());
        setNoFake.setMinimumSize(editUseSnobs.getMinimumSize());
        setNoFake.setMaximumSize(editUseSnobs.getMaximumSize());

        editPane.getContentPane().add(setNoFake);
        //increase attack count
        JXButton incAttacks = new JXButton(new ImageIcon(TribeTribeAttackFrame.class.getResource("/res/ui/add_attack.png")));
        incAttacks.setToolTipText("Erhöht die Anzahl der Angriffe auf die markierten Ziele um 1");
        incAttacks.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                editChangeAttacks(1);
            }
        });
        editPane.getContentPane().add(incAttacks);
        //decrease attack count
        JXButton decAttacks = new JXButton(new ImageIcon(TribeTribeAttackFrame.class.getResource("/res/ui/remove_attack.png")));
        decAttacks.setToolTipText("Verringert die Anzahl der Angriffe auf die markierten Ziele um 1");
        decAttacks.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                editChangeAttacks(-1);
            }
        });
        editPane.getContentPane().add(decAttacks);
        //reset attack count
        JXButton resetAttacks = new JXButton(new ImageIcon(TribeTribeAttackFrame.class.getResource("/res/ui/reset_attacks.png")));
        resetAttacks.setToolTipText("Setzt die Anzahl der Angriffe auf die markierten Ziele auf den Wert, der im Feld 'Max. Angriffe pro Dorf' steht");
        resetAttacks.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                editChangeAttacks(0);
            }
        });
        editPane.getContentPane().add(resetAttacks);
        ///////edit pane
        JXTaskPane miscPane = new JXTaskPane();
        miscPane.setTitle("Sonstiges");
        //troop split
        JXButton splitTroops = new JXButton(new ImageIcon(TribeTribeAttackFrame.class.getResource("/res/ui/branch.png")));
        splitTroops.setToolTipText("<html>Markierte Herkunftsd&ouml;rfer nach Truppenzahlen aufsplitten<br/>Hierf&uuml;r werden die Truppenzahlen verwendet, die aus dem Spiel<br/>importiert wurden. Achte daher darauf, dass diese Daten immer aktuell sind.<br/>F&uuml;r mehr Informationen zu diesem Feature schau bitte in der Hilfe (F1) nach.</html>");
        splitTroops.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                miscSplit();
            }
        });
        miscPane.getContentPane().add(splitTroops);
        //refresh possible attacks
        JXButton refreshAttackPossibility = new JXButton(new ImageIcon(TribeTribeAttackFrame.class.getResource("/res/ui/replace2.png")));
        refreshAttackPossibility.setToolTipText("Aktualisiert die Werte für die Anzahl der möglichen Angriffe aus Herkunftsdörfern und auf Zieldörfer, abhängig von den aktuellen Zeiteinstellungen");
        refreshAttackPossibility.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                miscRefreshPossibleAttacks();
            }
        });

        miscPane.getContentPane().add(refreshAttackPossibility);
        centerPanel.setupTaskPane(filterPane, editPane, miscPane, jCalculateButton);
    }

    /**
     * Setup attack frame (clear entries, fill lists and set initial values)
     */
    protected void setup() {
        // <editor-fold defaultstate="collapsed" desc=" Attack table setup ">
        DefaultTableModel attackModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Einheit", "Fake", "Anwendbar"
                }) {

            private Class[] types = new Class[]{
                Village.class, UnitHolder.class, Boolean.class, Integer.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 1 || column == 2);
            }
        };
        jSourcesTable.setModel(attackModel);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc=" Victim table setup ">
        DefaultTableModel victimModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Spieler", "Dorf", "Fake", "Angriffe", "Anwendbar"
                }) {

            private Class[] types = new Class[]{
                Tribe.class, Village.class, Boolean.class, Integer.class, Integer.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == 2 || column == 3);
            }
        };
        jVictimTable.setModel(victimModel);
        // </editor-fold>
        dataChangedEvent();
        filterDialog.reset();
        jEnableWarnBox.setSelected(GlobalOptions.getProperties().getBoolean("attack.planer.enable.check"));

        jTextField1.setText("" + GlobalOptions.getProperties().getInt("attack.planer.check.amount"));

        jSourcesTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jVictimTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        UIHelper.initTableColums(jSourcesTable, "Einheit", "Fake", "Anwendbar");
        UIHelper.initTableColums(jVictimTable, "Fake", "Angriffe", "Anwendbar");

        jSourcesTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jVictimTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jResultsTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));

        jAttackPlanSelectionDialog.pack();

        try {
            // <editor-fold defaultstate="collapsed" desc=" Build target allies list ">
            fireTargetAllyFilterChangedEvent(null);

            //select first ally and initialize all lists
            jTargetAllyList.setSelectedIndex(0);
            // </editor-fold>
            mSettingsPanel.reset();
            AlternatingColorCellRenderer rend = new AlternatingColorCellRenderer();
            jSourcesTable.setDefaultEditor(UnitHolder.class, new UnitCellEditor());
            jSourcesTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
            jSourcesTable.setDefaultEditor(Boolean.class, new FakeCellEditor());
            jSourcesTable.setDefaultRenderer(Boolean.class, new FakeCellRenderer());
            jSourcesTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
            jSourcesTable.setDefaultRenderer(String.class, rend);
            jSourcesTable.setRowHeight(24);
            jVictimTable.setDefaultRenderer(Tribe.class, new TribeCellRenderer());
            jVictimTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
            jVictimTable.setDefaultEditor(Boolean.class, new FakeCellEditor());
            jVictimTable.setDefaultRenderer(Boolean.class, new FakeCellRenderer());
            jVictimTable.setDefaultRenderer(Integer.class, rend);
            jVictimTable.setDefaultRenderer(String.class, rend);
            jVictimTable.setRowHeight(24);
            DefaultComboBoxModel unitModel = new DefaultComboBoxModel(DataHolder.getSingleton().getUnits().toArray(new UnitHolder[]{}));
            jTroopsList.setModel(unitModel);
            jTroopsList.setRenderer(new UnitListCellRenderer());
            jTroopsList.setSelectedItem(DataHolder.getSingleton().getUnitByPlainName("ram"));
            jResultFrame.pack();
        } catch (Exception e) {
            logger.error("Failed to initialize TribeAttackFrame", e);
        }
    }

    private void filterByTroopStrength() {
        int idx = jideTabbedPane1.getSelectedIndex();
        if (idx == 0) {
            int[] selectedRows = jSourcesTable.getSelectedRows();
            if (selectedRows == null || selectedRows.length == 0) {
                showInfo("Keine Herkunftsdörfer gewählt");
                return;
            }

            List<Village> sources = new LinkedList<>();
            List<Village> selection = new LinkedList<>();
            for (int i : selectedRows) {
                //go through selected rows in attack table and get source village
                sources.add((Village) jSourcesTable.getValueAt(i, 0));
                selection.add((Village) jSourcesTable.getValueAt(i, 0));
            }

            int sizeBefore = sources.size();

            if (sizeBefore == 0) {
                showInfo("Keine Herkunftsdörfer vorhanden");
                return;
            }
            filterDialog.show(sources);

            for (int i = jSourcesTable.getRowCount() - 1; i >= 0; i--) {
                //go through all rows in attack table and get source village
                Village v = (Village) jSourcesTable.getValueAt(i, 0);
                if (selection.contains(v)
                        && !sources.contains(v)) {
                    //remove entry if village was selected before and is not in list after filtering
                    ((DefaultTableModel) jSourcesTable.getModel()).removeRow(jSourcesTable.convertRowIndexToModel(i));
                }
            }

            int diff = sizeBefore - sources.size();
            if (diff == 0) {
                showSuccess("Keine Dörfer entfernt");
            } else {
                showSuccess(((diff == 1) ? "Ein Dorf entfernt" : diff + " Dörfer entfernt"));
            }
            updateInfo();
        } else {
            //no valid tab    
            showInfo("Diese Funktion ist nur für Herkunftsdörfer verfügbar");
        }
    }

    private void filterByUsage() {
        int idx = jideTabbedPane1.getSelectedIndex();
        if (idx == 0 || idx == 1) {
            DefaultTableModel model = new javax.swing.table.DefaultTableModel(
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
                    return col != 0;
                }
            };
            String[] plans = AttackManager.getSingleton().getGroups();
            for (String plan : plans) {
                model.addRow(new Object[]{plan, false});
            }
            jAttackPlanTable.setModel(model);
            jAttackPlanTable.repaint();
            jAttackPlanTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
            // jAttackPlanSelectionDialog.setLocationRelativeTo(DSWorkbenchMainFrame.getSingleton().getAttackPlaner());
            jAttackPlanSelectionDialog.setVisible(true);
        } else {
            showInfo("Diese Funktion ist nur für Herkunftsdörfer verfügbar");
        }
    }

    private void editUnit() {
        int idx = jideTabbedPane1.getSelectedIndex();
        if (idx == 0) {
            int[] rows = jSourcesTable.getSelectedRows();
            if (rows == null || rows.length == 0) {
                //no row selected
                showInfo("Keine Einträge ausgewählt");
                return;
            }
            UnitHolder unit = (UnitHolder) jTroopsList.getSelectedItem();
            if (unit == null) {
                showInfo("Keine Einheit ausgewählt");
                return;
            }
            for (int row : rows) {
                jSourcesTable.setValueAt(unit, row, 1);
            }
            String message = ((rows.length == 1) ? "Eintrag " : rows.length + " Einträge ") + "auf '" + unit.getName() + "' geändert";
            showSuccess(message);
        } else {
            showInfo("Diese Funktion ist nur für Herkunftsdörfer verfügbar");
        }
    }

    private void editUseSnobs() {
        int idx = jideTabbedPane1.getSelectedIndex();
        if (idx == 0) {
            //use snobs in villages where snobs exist
            DefaultTableModel model = (DefaultTableModel) jSourcesTable.getModel();
            UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
            jSourcesTable.invalidate();
            Hashtable<Village, Integer> assignedTroops = new Hashtable<>();
            for (int row = 0; row < model.getRowCount(); row++) {
                Village v = (Village) model.getValueAt(row, jSourcesTable.convertColumnIndexToModel(0));
                VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.IN_VILLAGE);
                if (troops != null) {
                    int availSnobs = troops.getTroopsOfUnitInVillage(snob);
                    Integer assignedSnobs = assignedTroops.get(v);
                    if (assignedSnobs == null) {
                        assignedSnobs = 0;
                    } else {
                        assignedSnobs += 1;
                    }
                    availSnobs -= assignedSnobs;
                    assignedTroops.put(v, assignedSnobs);
                    //snob avail
                    if (availSnobs > 0) {
                        model.setValueAt(snob, row, jSourcesTable.convertColumnIndexToModel(1));
                    }
                }
            }
            showInfo("Vorhandene AGs eingetragen");
        } else {
            showInfo("Diese Funktion ist nur für Herkunftsdörfer verfügbar");
        }
    }

    private void editSetFake(boolean pFake) {
        //change marked attacks to fake/no fake
        int idx = jideTabbedPane1.getSelectedIndex();

        JXTable table = null;
        if (idx == 0) {
            table = jSourcesTable;
        } else if (idx == 1) {
            table = jVictimTable;
        } else {
            showInfo("Diese Funktion ist nur für Herkunftsdörfer und Ziele verfügbar");
            return;
        }
        if (table == null) {
            //no valid tab seleted
            return;
        }
        int[] rows = table.getSelectedRows();
        if (rows == null || rows.length == 0) {
            //no row selected
            showInfo("Keine Einträge ausgewählt");
            return;
        }
        for (int row : rows) {
            table.setValueAt(pFake, row, 2);
        }

        String message = ((rows.length == 1) ? "Eintrag " : rows.length + " Einträge ") + "als " + ((pFake) ? "Fake" : "kein Fake") + " markiert";
        showSuccess(message);

        updateInfo();
    }

    private void editChangeAttacks(int pDirection) {
        int idx = jideTabbedPane1.getSelectedIndex();
        if (idx == 1) {
            int[] rows = jVictimTable.getSelectedRows();
            if (rows == null || rows.length == 0) {
                showInfo("Keine Ziele ausgewählt");
                return;
            }
            DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();

            for (int r : rows) {
                int row = jVictimTable.convertRowIndexToModel(r);
                int amount = (Integer) victimModel.getValueAt(row, jVictimTable.convertColumnIndexToModel(3));
                if (pDirection != 0) {
                    amount += pDirection;
                } else {
                    try {
                        amount = (Integer) jMaxAttacksPerVillage.getValue();
                    } catch (ClassCastException cce) {
                        amount = 1;
                    }
                }
                if (amount > 0) {
                    victimModel.setValueAt(amount, row, jVictimTable.convertColumnIndexToModel(3));
                }
            }

            String message = "Angriffe für " + ((rows.length == 1) ? " ein Ziel" : rows.length + " Ziele ") + " angepasst";
            showSuccess(message);

            updateInfo();
        } else {
            //invalid tab
            showInfo("Diese Funktion ist nur für Ziele verfügbar");
        }
    }

    private void miscSplit() {
        if (jideTabbedPane1.getSelectedIndex() == 0) {
            DefaultTableModel model = (DefaultTableModel) jSourcesTable.getModel();
            int sources = model.getRowCount();
            if (sources == 0) {
                showInfo("Keine Herkunftsdörfer eingetragen");
                return;
            }
            List<Village> sourceVillages = new LinkedList<>();
            Hashtable<Village, UnitHolder> attTable = new Hashtable<>();
            Hashtable<Village, UnitHolder> fakeTable = new Hashtable<>();
            for (int i = 0; i < sources; i++) {
                Village sourceVillage = (Village) model.getValueAt(i, jSourcesTable.convertColumnIndexToModel(0));
                if (!sourceVillages.contains(sourceVillage)) {
                    sourceVillages.add(sourceVillage);
                    boolean fake = (Boolean) jSourcesTable.getValueAt(i, jSourcesTable.convertColumnIndexToModel(2));
                    UnitHolder unit = (UnitHolder) jSourcesTable.getValueAt(i, jSourcesTable.convertColumnIndexToModel(1));
                    if (fake) {
                        fakeTable.put(sourceVillage, unit);
                    } else {
                        attTable.put(sourceVillage, unit);
                    }
                }
            }
            mTroopSplitDialog.setupAndShow(sourceVillages);
            TroopSplit[] splits = mTroopSplitDialog.getSplits();
            if (splits.length == 0) {
                //canceled
                return;
            }
            for (int i = sources - 1; i >= 0; i--) {
                model.removeRow(i);
            }
            int overallSplitCount = 0;
            for (TroopSplit split : splits) {
                overallSplitCount += split.getSplitCount();
                for (int i = 0; i < split.getSplitCount(); i++) {
                    boolean isFake = false;
                    UnitHolder unit = attTable.get(split.getVillage());
                    if (unit == null) {
                        unit = fakeTable.get(split.getVillage());
                        isFake = true;
                    }
                    model.addRow(new Object[]{split.getVillage(), unit, isFake, 0});
                }
            }

            String message = ((sourceVillages.size() == 1) ? "Herkunftsdorf " : sourceVillages.size() + " Herkunftsdörfer ")
                    + ((overallSplitCount == 1) ? "einmal" : overallSplitCount + " mal ") + " geteilt";
            showSuccess(message);
        } else {
            showInfo("Diese Funktion ist nur für Herkunftsdörfer verfügbar");
        }
    }

    private void miscRefreshPossibleAttacks() {
        jRefreshProgressDialog.pack();
        jRefreshProgressDialog.setLocationRelativeTo(TribeTribeAttackFrame.this);
        jProgressBar1.setString("Aktualisiere mögliche Angriffe");
        jProgressBar1.setMinimum(0);
        jProgressBar1.setMaximum(jSourcesTable.getRowCount() * jVictimTable.getRowCount());
        jProgressBar1.setValue(0);
        jideTabbedPane1.setSelectedIndex(0);
        new RefreshThread(jRefreshProgressDialog, jProgressBar1, mSettingsPanel, jSourcesTable, jVictimTable).start();
        jRefreshProgressDialog.setVisible(true);
    }

    public void showSuccess(String pMessage) {
        showSuccess(pMessage, false);
    }

    public void showSuccess(String pMessage, boolean pResult) {
        showMessage(pMessage, 0, pResult);
    }

    public void showInfo(String pMessage, boolean pResult) {
        showMessage(pMessage, 1, pResult);
    }

    public void showInfo(String pMessage) {
        showInfo(pMessage, false);
    }

    public void showError(String pMessage, boolean pResult) {
        showMessage(pMessage, 2, false);
    }

    public void showError(String pMessage) {
        showError(pMessage, false);
    }

    private void showMessage(String pMessage, int pType, boolean pResult) {
        JXCollapsiblePane panel = null;
        JXLabel label = null;

        if (pResult) {
            panel = resultInfoPanel;
            label = jxResultInfoLabel;
        } else {
            switch (jideTabbedPane1.getSelectedIndex()) {
                case 0:
                    panel = sourceInfoPanel;
                    label = jxSourceInfoLabel;
                    break;
                case 1:
                    panel = targetInfoPanel;
                    label = jxTargetInfoLabel;
                    break;
                case 2:
                    panel = settingsInfoPanel;
                    label = jxSettingsInfoLabel;
                    break;
                default:
            }
        }

        if (panel == null || label == null) {
            return;
        }

        panel.setCollapsed(false);
        label.setBackgroundPainter(new MattePainter(getBackground()));
        switch (pType) {
            case 0:
                label.setBackgroundPainter(new MattePainter(Color.GREEN));
                label.setForeground(Color.BLACK);
                break;
            case 2:
                label.setBackgroundPainter(new MattePainter(Color.RED));
                label.setForeground(Color.WHITE);
                break;
            default:
                label.setBackgroundPainter(new MattePainter(getBackground()));
                label.setForeground(Color.BLACK);
                break;
        }

        label.setText(pMessage);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jResultFrame = new javax.swing.JFrame();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTargetsBar = new javax.swing.JProgressBar();
        jLabel9 = new javax.swing.JLabel();
        jFullOffsBar = new javax.swing.JProgressBar();
        jAttacksBar = new javax.swing.JProgressBar();
        jLabel10 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jCloseResultsButton = new javax.swing.JButton();
        jAddToAttacksButton1 = new javax.swing.JButton();
        jFullTargetsOnly = new javax.swing.JCheckBox();
        capabilityInfoPanel2 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jResultsTable = new org.jdesktop.swingx.JXTable();
        resultInfoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jxResultInfoLabel = new org.jdesktop.swingx.JXLabel();
        jAddToAttacksButton2 = new javax.swing.JButton();
        jAttackResultDetailsFrame = new javax.swing.JFrame();
        jHideAttackDetailsButton = new javax.swing.JButton();
        jScrollPane11 = new javax.swing.JScrollPane();
        jNotAssignedSourcesTable = new javax.swing.JTable();
        jTargetResultDetailsFrame = new javax.swing.JFrame();
        jScrollPane12 = new javax.swing.JScrollPane();
        jTargetDetailsTable = new javax.swing.JTable();
        jHideTargetDetailsButton = new javax.swing.JButton();
        jAttackPlanSelectionDialog = new javax.swing.JDialog();
        jDoSyncButton = new javax.swing.JButton();
        jCancelSyncButton = new javax.swing.JButton();
        jScrollPane13 = new javax.swing.JScrollPane();
        jAttackPlanTable = new org.jdesktop.swingx.JXTable();
        jCalculateButton = new javax.swing.JButton();
        jRefreshProgressDialog = new javax.swing.JDialog();
        jxAttackPlanerPanel = new javax.swing.JPanel();
        jideTabbedPane1 = new com.jidesoft.swing.JideTabbedPane();
        jSourcePanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jTroopsList = new javax.swing.JComboBox();
        jScrollPane4 = new javax.swing.JScrollPane();
        jVillageGroupList = new javax.swing.JList();
        jScrollPane5 = new javax.swing.JScrollPane();
        jSourceContinentList = new javax.swing.JList();
        jLabel22 = new javax.swing.JLabel();
        jSourceGroupRelation = new javax.swing.JRadioButton();
        jMarkAsFakeBox = new javax.swing.JCheckBox();
        jPlayerSourcesOnlyBox = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel7 = new javax.swing.JPanel();
        jSelectionStart = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jSelectionEnd = new javax.swing.JTextField();
        jSelectionBeginButton = new javax.swing.JButton();
        jPrevSelectionButton = new javax.swing.JButton();
        jSelectButton = new javax.swing.JButton();
        jNextSelectionButton = new javax.swing.JButton();
        jSelectionEndButton = new javax.swing.JButton();
        jSourceListScrollPane = new javax.swing.JScrollPane();
        jSourceVillageList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jEnableWarnBox = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jAllSources = new javax.swing.JButton();
        jSelectedSources = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSourcesTable = new org.jdesktop.swingx.JXTable();
        sourceInfoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jxSourceInfoLabel = new org.jdesktop.swingx.JXLabel();
        jTargetPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTargetAllyList = new javax.swing.JList();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTargetTribeList = new javax.swing.JList();
        jScrollPane9 = new javax.swing.JScrollPane();
        jTargetContinentList = new javax.swing.JList();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTargetVillageList = new javax.swing.JList();
        jLabel11 = new javax.swing.JLabel();
        jTargetAllyFilter = new javax.swing.JTextField();
        jMarkTargetAsFake = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jMaxAttacksPerVillage = new javax.swing.JSpinner();
        jAllTargetsComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jAllTargets = new javax.swing.JButton();
        jSelectedTargets = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jVictimTable = new org.jdesktop.swingx.JXTable();
        targetInfoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jxTargetInfoLabel = new org.jdesktop.swingx.JXLabel();
        jSettingsPanel = new javax.swing.JPanel();
        jSettingsContentPanel = new javax.swing.JPanel();
        settingsInfoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jxSettingsInfoLabel = new org.jdesktop.swingx.JXLabel();
        jResultTransferDialog = new javax.swing.JDialog();
        jLabel14 = new javax.swing.JLabel();
        jExistingPlanBox = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        jNewPlanName = new org.jdesktop.swingx.JXTextField();
        jCancelTransferButton = new javax.swing.JButton();
        jDoTransferButton = new javax.swing.JButton();
        jInfoLabel = new javax.swing.JLabel();
        jMainPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jResultFrame.setTitle("Angriffsplan");

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/target.png"))); // NOI18N
        jLabel6.setMaximumSize(new java.awt.Dimension(18, 18));
        jLabel6.setMinimumSize(new java.awt.Dimension(18, 18));
        jLabel6.setPreferredSize(new java.awt.Dimension(18, 18));

        jTargetsBar.setToolTipText("Angegriffene Ziele / Gewählte Ziele");
        jTargetsBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTargetsBar.setStringPainted(true);
        jTargetsBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowResultDetailsEvent(evt);
            }
        });

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/ram.png"))); // NOI18N

        jFullOffsBar.setToolTipText("Angriffe mit max. Off Anzahl / Angriffe gesamt");
        jFullOffsBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jFullOffsBar.setStringPainted(true);

        jAttacksBar.setToolTipText("Angreifende Dörfer / Gewählte Herkunftsdörfer");
        jAttacksBar.setValue(50);
        jAttacksBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jAttacksBar.setStringPainted(true);
        jAttacksBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowResultDetailsEvent(evt);
            }
        });

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png"))); // NOI18N

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("(Klicken für Details)");

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("(Klicken für Details)");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                    .addComponent(jTargetsBar, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(jAttacksBar, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jFullOffsBar, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(2, 2, 2)))
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTargetsBar, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jAttacksBar, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFullOffsBar, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jCloseResultsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jCloseResultsButton.setText("Schließen");
        jCloseResultsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideResultsEvent(evt);
            }
        });

        jAddToAttacksButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_align_justified.png"))); // NOI18N
        jAddToAttacksButton1.setToolTipText("Informationen zur Berechnung anzeigen");
        jAddToAttacksButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireReOpenLogPanelEvent(evt);
            }
        });

        jFullTargetsOnly.setText("Nur komplett belegte Ziele übertragen");
        jFullTargetsOnly.setToolTipText("<html>Beim Kopieren in die Zwischenablage (Angriffe und BB-Codes) werden nur Ziele ber&uuml;cksichtigt,<br/>auf welche die volle Anzahl der gew&uuml;nschten Offs geplant wurde</html>");
        jFullTargetsOnly.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jFullTargetsOnly.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jFullTargetsOnly.setIconTextGap(10);

        capabilityInfoPanel2.setPastable(false);
        capabilityInfoPanel2.setSearchable(false);

        jPanel8.setLayout(new java.awt.BorderLayout());

        jResultsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jResultsTable);

        jPanel8.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        resultInfoPanel.setCollapsed(true);
        resultInfoPanel.setInheritAlpha(false);

        jxResultInfoLabel.setText("Keine Meldung");
        jxResultInfoLabel.setOpaque(true);
        jxResultInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        resultInfoPanel.add(jxResultInfoLabel, java.awt.BorderLayout.CENTER);

        jPanel8.add(resultInfoPanel, java.awt.BorderLayout.SOUTH);

        jAddToAttacksButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_overview.png"))); // NOI18N
        jAddToAttacksButton2.setToolTipText("Angriffe in die Angriffsübersicht übertragen");
        jAddToAttacksButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferAttacksToAttackViewEvent(evt);
            }
        });

        javax.swing.GroupLayout jResultFrameLayout = new javax.swing.GroupLayout(jResultFrame.getContentPane());
        jResultFrame.getContentPane().setLayout(jResultFrameLayout);
        jResultFrameLayout.setHorizontalGroup(
            jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
                    .addGroup(jResultFrameLayout.createSequentialGroup()
                        .addComponent(capabilityInfoPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jFullTargetsOnly)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                        .addComponent(jAddToAttacksButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jAddToAttacksButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCloseResultsButton))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jResultFrameLayout.setVerticalGroup(
            jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jAddToAttacksButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAddToAttacksButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(capabilityInfoPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jFullTargetsOnly, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCloseResultsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jAttackResultDetailsFrame.setTitle("Nicht zugewiesene Herkunftsdörfer");

        jHideAttackDetailsButton.setBackground(new java.awt.Color(239, 235, 223));
        jHideAttackDetailsButton.setText("Schließen");
        jHideAttackDetailsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideResultDetailsEvent(evt);
            }
        });

        jNotAssignedSourcesTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane11.setViewportView(jNotAssignedSourcesTable);

        javax.swing.GroupLayout jAttackResultDetailsFrameLayout = new javax.swing.GroupLayout(jAttackResultDetailsFrame.getContentPane());
        jAttackResultDetailsFrame.getContentPane().setLayout(jAttackResultDetailsFrameLayout);
        jAttackResultDetailsFrameLayout.setHorizontalGroup(
            jAttackResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackResultDetailsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jHideAttackDetailsButton)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE))
                .addContainerGap())
        );
        jAttackResultDetailsFrameLayout.setVerticalGroup(
            jAttackResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackResultDetailsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jHideAttackDetailsButton)
                .addContainerGap())
        );

        jTargetResultDetailsFrame.setTitle("Angriffe pro Ziel");

        jTargetDetailsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane12.setViewportView(jTargetDetailsTable);

        jHideTargetDetailsButton.setText("Schließen");
        jHideTargetDetailsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideResultDetailsEvent(evt);
            }
        });

        javax.swing.GroupLayout jTargetResultDetailsFrameLayout = new javax.swing.GroupLayout(jTargetResultDetailsFrame.getContentPane());
        jTargetResultDetailsFrame.getContentPane().setLayout(jTargetResultDetailsFrameLayout);
        jTargetResultDetailsFrameLayout.setHorizontalGroup(
            jTargetResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTargetResultDetailsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTargetResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jHideTargetDetailsButton)
                    .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE))
                .addContainerGap())
        );
        jTargetResultDetailsFrameLayout.setVerticalGroup(
            jTargetResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTargetResultDetailsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jHideTargetDetailsButton)
                .addContainerGap())
        );

        jAttackPlanSelectionDialog.setTitle("Befehlsabgleich");
        jAttackPlanSelectionDialog.setAlwaysOnTop(true);

        jDoSyncButton.setBackground(new java.awt.Color(239, 235, 223));
        jDoSyncButton.setText("OK");
        jDoSyncButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSynchWithAttackPlansEvent(evt);
            }
        });

        jCancelSyncButton.setBackground(new java.awt.Color(239, 235, 223));
        jCancelSyncButton.setText("Abbrechen");
        jCancelSyncButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSynchWithAttackPlansEvent(evt);
            }
        });

        jAttackPlanTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane13.setViewportView(jAttackPlanTable);

        javax.swing.GroupLayout jAttackPlanSelectionDialogLayout = new javax.swing.GroupLayout(jAttackPlanSelectionDialog.getContentPane());
        jAttackPlanSelectionDialog.getContentPane().setLayout(jAttackPlanSelectionDialogLayout);
        jAttackPlanSelectionDialogLayout.setHorizontalGroup(
            jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAttackPlanSelectionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                    .addGroup(jAttackPlanSelectionDialogLayout.createSequentialGroup()
                        .addComponent(jCancelSyncButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDoSyncButton)))
                .addContainerGap())
        );
        jAttackPlanSelectionDialogLayout.setVerticalGroup(
            jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAttackPlanSelectionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDoSyncButton)
                    .addComponent(jCancelSyncButton))
                .addContainerGap())
        );

        jCalculateButton.setBackground(new java.awt.Color(239, 235, 223));
        jCalculateButton.setText("<html><p align=\"center\">Berechnung<br/>starten</p></html>");
        jCalculateButton.setToolTipText("Angriffsplan berechnen");
        jCalculateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateAttackEvent(evt);
            }
        });

        jRefreshProgressDialog.setModal(true);
        jRefreshProgressDialog.setUndecorated(true);

        jProgressBar1.setValue(50);
        jProgressBar1.setString("Aktualisiere Kombinationen");
        jProgressBar1.setStringPainted(true);

        javax.swing.GroupLayout jRefreshProgressDialogLayout = new javax.swing.GroupLayout(jRefreshProgressDialog.getContentPane());
        jRefreshProgressDialog.getContentPane().setLayout(jRefreshProgressDialogLayout);
        jRefreshProgressDialogLayout.setHorizontalGroup(
            jRefreshProgressDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jRefreshProgressDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addContainerGap())
        );
        jRefreshProgressDialogLayout.setVerticalGroup(
            jRefreshProgressDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jRefreshProgressDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jxAttackPlanerPanel.setPreferredSize(new java.awt.Dimension(970, 600));
        jxAttackPlanerPanel.setLayout(new java.awt.BorderLayout());

        jSourcePanel.setBackground(new java.awt.Color(239, 235, 223));
        jSourcePanel.setPreferredSize(new java.awt.Dimension(703, 535));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setOpaque(false);

        jTroopsList.setToolTipText("Langsamste Einheit des Angriffs");
        jTroopsList.setMaximumSize(new java.awt.Dimension(500, 23));
        jTroopsList.setMinimumSize(new java.awt.Dimension(20, 23));
        jTroopsList.setPreferredSize(new java.awt.Dimension(150, 23));

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Gruppen"));

        jScrollPane4.setViewportView(jVillageGroupList);

        jScrollPane5.setBorder(javax.swing.BorderFactory.createTitledBorder("Kontinent"));
        jScrollPane5.setMaximumSize(new java.awt.Dimension(80, 132));
        jScrollPane5.setMinimumSize(new java.awt.Dimension(80, 132));
        jScrollPane5.setPreferredSize(new java.awt.Dimension(80, 132));

        jScrollPane5.setViewportView(jSourceContinentList);

        jLabel22.setText("Truppen");

        jSourceGroupRelation.setSelected(true);
        jSourceGroupRelation.setText("Verknüpfung (ODER)");
        jSourceGroupRelation.setToolTipText("Verknüpfung der gewählten Dorfgruppen (UND = Dorf muss in allen Gruppen sein, ODER = Dorf muss in mindestens einer Gruppe sein)");
        jSourceGroupRelation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_and.png"))); // NOI18N
        jSourceGroupRelation.setOpaque(false);
        jSourceGroupRelation.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_or.png"))); // NOI18N
        jSourceGroupRelation.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireSourceRelationChangedEvent(evt);
            }
        });

        jMarkAsFakeBox.setText("Als Fake einfügen");
        jMarkAsFakeBox.setToolTipText("Markiert im folgenden eingefügten Angriffe als Fakes");
        jMarkAsFakeBox.setOpaque(false);

        jPlayerSourcesOnlyBox.setSelected(true);
        jPlayerSourcesOnlyBox.setText("Nur Dörfer des aktiven Spielers anzeigen");
        jPlayerSourcesOnlyBox.setToolTipText("Nur Dörfer des in den Einstellungen gewählten Spielers werden in der Dorfliste angezeigt");
        jPlayerSourcesOnlyBox.setOpaque(false);
        jPlayerSourcesOnlyBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireShowPlayerSourcesOnlyChangedEvent(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setPreferredSize(new java.awt.Dimension(2, 23));

        jPanel7.setOpaque(false);
        jPanel7.setLayout(new java.awt.GridLayout(1, 0, 2, 0));

        jSelectionStart.setText("1");
        jSelectionStart.setMaximumSize(new java.awt.Dimension(40, 25));
        jSelectionStart.setMinimumSize(new java.awt.Dimension(40, 25));
        jSelectionStart.setPreferredSize(new java.awt.Dimension(40, 25));
        jPanel7.add(jSelectionStart);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("bis");
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel7.add(jLabel4);

        jSelectionEnd.setText("10");
        jSelectionEnd.setMaximumSize(new java.awt.Dimension(40, 25));
        jSelectionEnd.setMinimumSize(new java.awt.Dimension(40, 25));
        jSelectionEnd.setPreferredSize(new java.awt.Dimension(40, 25));
        jPanel7.add(jSelectionEnd);

        jSelectionBeginButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectionBeginButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/beginning.png"))); // NOI18N
        jSelectionBeginButton.setToolTipText("Bereich vom ersten Eintrag aus wählen");
        jSelectionBeginButton.setEnabled(false);
        jSelectionBeginButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jSelectionBeginButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jSelectionBeginButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jSelectionBeginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateSelectionEvent(evt);
            }
        });
        jPanel7.add(jSelectionBeginButton);

        jPrevSelectionButton.setBackground(new java.awt.Color(239, 235, 223));
        jPrevSelectionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/prev.png"))); // NOI18N
        jPrevSelectionButton.setToolTipText("Den vorherigen Bereich wählen");
        jPrevSelectionButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jPrevSelectionButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jPrevSelectionButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jPrevSelectionButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateSelectionEvent(evt);
            }
        });
        jPanel7.add(jPrevSelectionButton);

        jSelectButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/select.png"))); // NOI18N
        jSelectButton.setToolTipText("Eingestellten Bereich wählen");
        jSelectButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jSelectButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jSelectButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jSelectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateSelectionEvent(evt);
            }
        });
        jPanel7.add(jSelectButton);

        jNextSelectionButton.setBackground(new java.awt.Color(239, 235, 223));
        jNextSelectionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/next.png"))); // NOI18N
        jNextSelectionButton.setToolTipText("Den nächsten Bereich wählen");
        jNextSelectionButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jNextSelectionButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jNextSelectionButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jNextSelectionButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateSelectionEvent(evt);
            }
        });
        jPanel7.add(jNextSelectionButton);

        jSelectionEndButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectionEndButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/end.png"))); // NOI18N
        jSelectionEndButton.setToolTipText("Bereich vom Ende aus wählen");
        jSelectionEndButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jSelectionEndButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jSelectionEndButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jSelectionEndButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateSelectionEvent(evt);
            }
        });
        jPanel7.add(jSelectionEndButton);

        jSourceListScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Dörfer"));
        jSourceListScrollPane.setPreferredSize(new java.awt.Dimension(100, 130));

        jSourceVillageList.setDragEnabled(true);
        jSourceListScrollPane.setViewportView(jSourceVillageList);

        jLabel2.setForeground(new java.awt.Color(153, 153, 153));
        jLabel2.setText("(Dörfer markieren und per Drag&Drop in die Tabelle ziehen)");

        jEnableWarnBox.setSelected(true);
        jEnableWarnBox.setText("Warnen, wenn zu wenig verfügbare Truppen im Herkunftsdorf stationiert sind ");
        jEnableWarnBox.setOpaque(false);
        jEnableWarnBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEnableWarningEvent(evt);
            }
        });

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Warnen bei weniger als");
        jLabel12.setMaximumSize(new java.awt.Dimension(140, 23));
        jLabel12.setMinimumSize(new java.awt.Dimension(140, 23));
        jLabel12.setPreferredSize(new java.awt.Dimension(140, 23));

        jTextField1.setText("20000");
        jTextField1.setMaximumSize(new java.awt.Dimension(60, 23));
        jTextField1.setMinimumSize(new java.awt.Dimension(60, 23));
        jTextField1.setPreferredSize(new java.awt.Dimension(60, 23));

        jLabel13.setText("Einheiten");
        jLabel13.setMaximumSize(new java.awt.Dimension(80, 23));
        jLabel13.setMinimumSize(new java.awt.Dimension(80, 23));
        jLabel13.setPreferredSize(new java.awt.Dimension(80, 23));

        jAllSources.setText("Alle");
        jAllSources.setToolTipText("Alle Dörfer einfügen");
        jAllSources.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireAddVillagesEvent(evt);
            }
        });

        jSelectedSources.setText("Markierte");
        jSelectedSources.setToolTipText("Markierte Dörfer einfügen");
        jSelectedSources.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireAddVillagesEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jSourceGroupRelation, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                                .addGap(39, 39, 39))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jEnableWarnBox)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jMarkAsFakeBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGap(8, 8, 8)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel22)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jTroopsList, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPlayerSourcesOnlyBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE))
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSourceListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jAllSources, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSelectedSources, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jAllSources)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSelectedSources))
                            .addComponent(jSourceListScrollPane, 0, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addComponent(jSourceGroupRelation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jPlayerSourcesOnlyBox)
                    .addComponent(jEnableWarnBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jMarkAsFakeBox, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTroopsList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jLabel20.setText("<html><b>Hinweis</b>: Abhängig vom gewählten Angriffstyp sind evtl. nur Rammen, Katapulte und AGs als Truppentypen zulässig.</html>");

        jPanel4.setLayout(new java.awt.BorderLayout());

        jSourcesTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jSourcesTable);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        sourceInfoPanel.setCollapsed(true);
        sourceInfoPanel.setInheritAlpha(false);

        jxSourceInfoLabel.setText("Keine Meldung");
        jxSourceInfoLabel.setOpaque(true);
        jxSourceInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        sourceInfoPanel.add(jxSourceInfoLabel, java.awt.BorderLayout.CENTER);

        jPanel4.add(sourceInfoPanel, java.awt.BorderLayout.SOUTH);

        javax.swing.GroupLayout jSourcePanelLayout = new javax.swing.GroupLayout(jSourcePanel);
        jSourcePanel.setLayout(jSourcePanelLayout);
        jSourcePanelLayout.setHorizontalGroup(
            jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 944, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 944, Short.MAX_VALUE))
                .addContainerGap())
        );
        jSourcePanelLayout.setVerticalGroup(
            jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jideTabbedPane1.addTab("Herkunft", new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png")), jSourcePanel); // NOI18N

        jTargetPanel.setBackground(new java.awt.Color(239, 235, 223));
        jTargetPanel.setPreferredSize(new java.awt.Dimension(703, 535));

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setOpaque(false);

        jScrollPane7.setBorder(javax.swing.BorderFactory.createTitledBorder("Stamm"));
        jScrollPane7.setMaximumSize(new java.awt.Dimension(260, 140));
        jScrollPane7.setMinimumSize(new java.awt.Dimension(130, 140));
        jScrollPane7.setPreferredSize(new java.awt.Dimension(260, 140));

        jTargetAllyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane7.setViewportView(jTargetAllyList);

        jScrollPane8.setBorder(javax.swing.BorderFactory.createTitledBorder("Spieler"));
        jScrollPane8.setMinimumSize(new java.awt.Dimension(100, 23));

        jTargetTribeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane8.setViewportView(jTargetTribeList);

        jScrollPane9.setBorder(javax.swing.BorderFactory.createTitledBorder("Kontinent"));
        jScrollPane9.setMaximumSize(new java.awt.Dimension(80, 132));
        jScrollPane9.setMinimumSize(new java.awt.Dimension(80, 132));
        jScrollPane9.setPreferredSize(new java.awt.Dimension(80, 132));

        jScrollPane9.setViewportView(jTargetContinentList);

        jScrollPane10.setBorder(javax.swing.BorderFactory.createTitledBorder("Dörfer"));
        jScrollPane10.setMaximumSize(new java.awt.Dimension(220, 132));
        jScrollPane10.setMinimumSize(new java.awt.Dimension(100, 132));
        jScrollPane10.setPreferredSize(new java.awt.Dimension(240, 132));

        jTargetVillageList.setDragEnabled(true);
        jScrollPane10.setViewportView(jTargetVillageList);

        jLabel11.setText("Filter");

        jTargetAllyFilter.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireTargetAllyFilterChangedEvent(evt);
            }
        });

        jMarkTargetAsFake.setText("Als Fake einfügen");
        jMarkTargetAsFake.setMaximumSize(new java.awt.Dimension(110, 23));
        jMarkTargetAsFake.setMinimumSize(new java.awt.Dimension(110, 23));
        jMarkTargetAsFake.setOpaque(false);
        jMarkTargetAsFake.setPreferredSize(new java.awt.Dimension(110, 23));

        jLabel7.setText("Max. Angriffe pro Dorf");

        jMaxAttacksPerVillage.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));

        jAllTargetsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alle", ">= 3.000 Punkte", ">= 5.000 Punkte", ">= 7.000 Punkte", " ", " ", " " }));
        jAllTargetsComboBox.setToolTipText("Ziele mit gewählter Mindestpunktzahl einfügen");

        jLabel1.setText("Punkte");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel3.setForeground(new java.awt.Color(153, 153, 153));
        jLabel3.setText("(Dörfer markieren und per Drag&Drop in die Tabelle ziehen)");

        jAllTargets.setText("Alle");
        jAllTargets.setToolTipText("Alle Dörfer einfügen");
        jAllTargets.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireAddVillagesEvent(evt);
            }
        });

        jSelectedTargets.setText("Markierte");
        jSelectedTargets.setToolTipText("Markierte Dörfer einfügen");
        jSelectedTargets.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireAddVillagesEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(12, 12, 12)
                        .addComponent(jTargetAllyFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                            .addComponent(jMarkTargetAsFake, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jAllTargetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jMaxAttacksPerVillage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jAllTargets, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSelectedTargets, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jTargetAllyFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jAllTargets)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSelectedTargets))
                            .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jAllTargetsComboBox, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jMarkTargetAsFake, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jMaxAttacksPerVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jPanel5.setLayout(new java.awt.BorderLayout());

        jVictimTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(jVictimTable);

        jPanel5.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        targetInfoPanel.setCollapsed(true);
        targetInfoPanel.setInheritAlpha(false);

        jxTargetInfoLabel.setText("Keine Meldung");
        jxTargetInfoLabel.setOpaque(true);
        jxTargetInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        targetInfoPanel.add(jxTargetInfoLabel, java.awt.BorderLayout.CENTER);

        jPanel5.add(targetInfoPanel, java.awt.BorderLayout.SOUTH);

        javax.swing.GroupLayout jTargetPanelLayout = new javax.swing.GroupLayout(jTargetPanel);
        jTargetPanel.setLayout(jTargetPanelLayout);
        jTargetPanelLayout.setHorizontalGroup(
            jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTargetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 944, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jTargetPanelLayout.setVerticalGroup(
            jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTargetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jideTabbedPane1.addTab("Ziele", new javax.swing.ImageIcon(getClass().getResource("/res/ally.png")), jTargetPanel); // NOI18N

        jSettingsPanel.setPreferredSize(new java.awt.Dimension(703, 535));
        jSettingsPanel.setLayout(new java.awt.BorderLayout());

        jSettingsContentPanel.setLayout(new java.awt.BorderLayout());

        settingsInfoPanel.setCollapsed(true);
        settingsInfoPanel.setInheritAlpha(false);

        jxSettingsInfoLabel.setText("Keine Meldung");
        jxSettingsInfoLabel.setOpaque(true);
        jxSettingsInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        settingsInfoPanel.add(jxSettingsInfoLabel, java.awt.BorderLayout.CENTER);

        jSettingsContentPanel.add(settingsInfoPanel, java.awt.BorderLayout.SOUTH);

        jSettingsPanel.add(jSettingsContentPanel, java.awt.BorderLayout.CENTER);

        jideTabbedPane1.addTab("Einstellungen", new javax.swing.ImageIcon(getClass().getResource("/res/settings.png")), jSettingsPanel); // NOI18N

        jxAttackPlanerPanel.add(jideTabbedPane1, java.awt.BorderLayout.CENTER);

        jResultTransferDialog.setTitle("Angriffe übertragen");
        jResultTransferDialog.getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel14.setText("Angriffsplan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jResultTransferDialog.getContentPane().add(jLabel14, gridBagConstraints);

        jExistingPlanBox.setMinimumSize(new java.awt.Dimension(200, 20));
        jExistingPlanBox.setPreferredSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jResultTransferDialog.getContentPane().add(jExistingPlanBox, gridBagConstraints);

        jLabel15.setText("Neuer Plan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jResultTransferDialog.getContentPane().add(jLabel15, gridBagConstraints);

        jNewPlanName.setMinimumSize(new java.awt.Dimension(200, 20));
        jNewPlanName.setPreferredSize(new java.awt.Dimension(200, 20));
        jNewPlanName.setPrompt("Bei Bedarf Name eingeben");
        jNewPlanName.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireNewResultTargetPlanChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jResultTransferDialog.getContentPane().add(jNewPlanName, gridBagConstraints);

        jCancelTransferButton.setText("Abbrechen");
        jCancelTransferButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireTransferResultsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jResultTransferDialog.getContentPane().add(jCancelTransferButton, gridBagConstraints);

        jDoTransferButton.setText("OK");
        jDoTransferButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireTransferResultsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jResultTransferDialog.getContentPane().add(jDoTransferButton, gridBagConstraints);

        setTitle("Angriffsplaner");
        setBackground(new java.awt.Color(239, 235, 223));
        setMinimumSize(new java.awt.Dimension(350, 226));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                fireClosingEvent(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jInfoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/information.png"))); // NOI18N
        jInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showAttackInfoEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jInfoLabel, gridBagConstraints);

        jMainPanel.setBackground(new java.awt.Color(239, 235, 223));
        jMainPanel.setMinimumSize(new java.awt.Dimension(350, 200));
        jMainPanel.setPreferredSize(new java.awt.Dimension(1000, 650));
        jMainPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jMainPanel, gridBagConstraints);

        capabilityInfoPanel1.setBbSupport(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireCalculateAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateAttackEvent
    if (!jCalculateButton.isEnabled()) {
        logger.debug("Button disabled. Calculation is still running...");
        return;
    }
    //algorithm calculation
    //pre check
    DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
    DefaultTableModel attackModel = (DefaultTableModel) jSourcesTable.getModel();
    if (attackModel.getRowCount() == 0) {
        logger.warn("Validation of attacker tab failed");
        JOptionPaneHelper.showErrorBox(this, "Keine Herkunftsdörfer ausgewählt", "Fehler");
        jideTabbedPane1.setSelectedIndex(0);
        return;
    }
    if (victimModel.getRowCount() == 0) {
        logger.warn("Validation of victim tab failed");
        JOptionPaneHelper.showErrorBox(this, "Keine Ziele ausgewählt", "Fehler");
        jideTabbedPane1.setSelectedIndex(1);
        return;
    }
    if (!mSettingsPanel.validatePanel()) {
        logger.warn("Validation of settings tab failed");
        jideTabbedPane1.setSelectedIndex(2);
        return;
    }
    //reading values
    List<Village> victimVillages = new LinkedList<>();
    List<Village> victimVillagesFaked = new LinkedList<>();
    Hashtable<Village, Integer> maxAttacksTable = new Hashtable<>();
    for (int i = 0; i < victimModel.getRowCount(); i++) {
        if (victimModel.getValueAt(i, 2) == Boolean.TRUE) {
            victimVillagesFaked.add((Village) victimModel.getValueAt(i, 1));
        } else {
            victimVillages.add((Village) victimModel.getValueAt(i, 1));
        }
        maxAttacksTable.put((Village) victimModel.getValueAt(i, 1), (Integer) victimModel.getValueAt(i, 3));
    }
//build source-unit map
    int snobSources = 0;
    // <editor-fold defaultstate="collapsed" desc=" Build attacks and fakes">
    Hashtable<UnitHolder, List<Village>> sources = new Hashtable<>();
    Hashtable<UnitHolder, List<Village>> fakes = new Hashtable<>();
    for (int i = 0; i < attackModel.getRowCount(); i++) {
        Village vSource = (Village) attackModel.getValueAt(i, 0);
        UnitHolder uSource = (UnitHolder) attackModel.getValueAt(i, 1);
        boolean fake = (Boolean) attackModel.getValueAt(i, 2);
        if (!fake) {
            List<Village> sourcesForUnit = sources.get(uSource);
            if (uSource.getPlainName().equals("snob")) {
                if (sourcesForUnit == null) {
                    snobSources = 0;
                } else {
                    snobSources = sourcesForUnit.size();
                }
            }
            TroopHelper.fillSourcesWithAttacksForUnit(vSource, sources, sourcesForUnit, uSource);
        } else {
            TroopHelper.fillSourcesWithAttacksForUnit(vSource, fakes, null, uSource);
        }
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Check for units not supported by the algorithm">
    boolean useMiscUnits = false;
    Enumeration<UnitHolder> involvedUnits = sources.keys();
    while (involvedUnits.hasMoreElements()) {
        UnitHolder u = involvedUnits.nextElement();
        //check for misc unit
        if (!u.getPlainName().equals("ram") && !u.getPlainName().equals("catapult")) {
            useMiscUnits = true;
            break;
        }
    }
    if (!useMiscUnits) {
        involvedUnits = fakes.keys();
        while (involvedUnits.hasMoreElements()) {
            UnitHolder u = involvedUnits.nextElement();
            //check for misc unit
            if (!u.getPlainName().equals("ram") && !u.getPlainName().equals("catapult")) {
                useMiscUnits = true;
                break;
            }
        }
    }
    // </editor-fold>
    boolean fakeOffTargets = mSettingsPanel.fakeOffTargets();
    //mSettingsPanel.getAttacksPerVillage();
    TimeFrame timeFrame = mSettingsPanel.getTimeFrame();
    //start processing
    AbstractAttackAlgorithm algo = null;
    boolean supportMiscUnits = false;
    if (mSettingsPanel.useBruteForce()) {
        logger.info("Using 'BruteForce' calculation");
        algo = new BruteForce();
        //algo = new Recurrection();
        supportMiscUnits = true;
        logPanel.setAbortable(false);
    } else {
        logger.info("Using 'systematic' calculation");
        algo = new Iterix();
        supportMiscUnits = false;
        logPanel.setAbortable(true);
    }
    //check misc-units criteria
    if (useMiscUnits && !supportMiscUnits) {
        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Der gewählte Algorithmus unterstützt nur Rammen und Katapulte als angreifende Einheiten.\n" + "Dörfer für die eine andere Einheit gewählt wurde werden ignoriert.\n" + "Trotzdem fortfahren?", "Warnung", "Nein", "Ja") == JOptionPane.NO_OPTION) {
            logger.debug("User aborted calculation due to algorithm");
            return;
        }
    }
    mSettingsPanel.storeProperties();
    logPanel.clear();
    algo.initialize(sources,
            fakes,
            victimVillages,
            victimVillagesFaked,
            maxAttacksTable,
            timeFrame,
            fakeOffTargets,
            logPanel);
    SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
            try {
                jCalculateButton.setEnabled(false);
                mLogFrame.setVisible(true);
                mLogFrame.toFront();
            } catch (Exception ignored) {
            }
        }
    });
    algo.execute(this);
}//GEN-LAST:event_fireCalculateAttackEvent

private void fireHideResultsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideResultsEvent
    jResultFrame.setVisible(false);
}//GEN-LAST:event_fireHideResultsEvent

private void fireShowResultDetailsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowResultDetailsEvent
    if (evt.getSource() == jAttacksBar) {
        jAttackResultDetailsFrame.setVisible(true);
    } else if (evt.getSource() == jTargetsBar) {
        jTargetResultDetailsFrame.setVisible(true);
    }
}//GEN-LAST:event_fireShowResultDetailsEvent

private void fireHideResultDetailsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideResultDetailsEvent
    if (evt.getSource() == jHideAttackDetailsButton) {
        jAttackResultDetailsFrame.setVisible(false);
    } else if (evt.getSource() == jHideTargetDetailsButton) {
        jTargetResultDetailsFrame.setVisible(false);
    }
}//GEN-LAST:event_fireHideResultDetailsEvent

private void fireSynchWithAttackPlansEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSynchWithAttackPlansEvent
    jAttackPlanSelectionDialog.setVisible(false);
    if (evt.getSource() == jCancelSyncButton) {
        return;
    }

    int idx = jideTabbedPane1.getSelectedIndex();

    JXTable table = null;
    int villageIndex = 0;
    if (idx == 0) {
        table = jSourcesTable;
    } else if (idx == 1) {
        table = jVictimTable;
        villageIndex = 1;
    } else {
        showInfo("Diese Funktion ist nur für Herkunftsdörfer und Ziele verfügbar");
        return;
    }

    DefaultTableModel model = (DefaultTableModel) jAttackPlanTable.getModel();
    List<String> selectedPlans = new LinkedList<>();
    for (int i = 0; i < jAttackPlanTable.getRowCount(); i++) {
        int row = jAttackPlanTable.convertRowIndexToModel(i);
        if ((Boolean) model.getValueAt(row, jAttackPlanTable.convertColumnIndexToModel(1))) {
            selectedPlans.add((String) model.getValueAt(row, jAttackPlanTable.convertColumnIndexToModel(0)));
        }
    }
    List<Integer> toRemove = new LinkedList<>();
    //process all plans
    for (String plan : selectedPlans) {
        logger.debug("Checking plan '" + plan + "'");
        List<ManageableType> elements = AttackManager.getSingleton().getAllElements(plan);
        //process all attacks
        for (ManageableType e : elements) {
            Attack a = (Attack) e;
            //search attack source village in all table rows
            for (int i = 0; i < table.getRowCount(); i++) {
                Village v = (Village) table.getValueAt(i, villageIndex);
                if (villageIndex == 0 && a.getSource().equals(v)) {
                    if (!toRemove.contains(i)) {
                        toRemove.add(i);
                    }
                } else if (villageIndex == 1 && a.getTarget().equals(v)) {
                    if (!toRemove.contains(i)) {
                        toRemove.add(i);
                    }
                }
            }
        }
    }
    String message = "";
    if (toRemove.isEmpty()) {
        showInfo("Keine Dörfer zu entfernen");
        return;
    } else {
        message = (toRemove.size() == 1) ? "Ein Dorf " : toRemove.size() + " Dörfer ";
    }
    if (JOptionPaneHelper.showQuestionConfirmBox(this, message + "entfernen?", "Entfernen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
        try {
            logger.debug("Removing " + toRemove.size() + " villages");
            Collections.sort(toRemove);
            while (toRemove.size() > 0) {
                Integer row = toRemove.remove(toRemove.size() - 1);
                row = table.convertRowIndexToModel(row);
                ((DefaultTableModel) table.getModel()).removeRow(row);
            }
            showSuccess(message + "entfernt");
        } catch (Exception e) {
            logger.error("Removal failed", e);
            showError("Fehler beim Entfernen");
        }
    }
    updateInfo();
}//GEN-LAST:event_fireSynchWithAttackPlansEvent

private void fireTargetAllyFilterChangedEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireTargetAllyFilterChangedEvent
    Ally[] allies = AllyUtils.getAlliesByFilter(jTargetAllyFilter.getText(), Ally.CASE_INSENSITIVE_ORDER);
    DefaultListModel targetAllyModel = new DefaultListModel();
    for (Ally a : allies) {
        targetAllyModel.addElement(a);
    }
    jTargetAllyList.setModel(targetAllyModel);
}//GEN-LAST:event_fireTargetAllyFilterChangedEvent

private void fireClosingEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_fireClosingEvent
    mSettingsPanel.storeProperties();
}//GEN-LAST:event_fireClosingEvent

private void fireReOpenLogPanelEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireReOpenLogPanelEvent
    mLogFrame.setVisible(true);
    mLogFrame.toFront();
}//GEN-LAST:event_fireReOpenLogPanelEvent

private void showAttackInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showAttackInfoEvent
    DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
    List<Village> victimVillages = new LinkedList<>();
    List<Village> victimVillagesFaked = new LinkedList<>();
    for (int i = 0; i
            < victimModel.getRowCount(); i++) {
        if (victimModel.getValueAt(i, 2) == Boolean.TRUE) {
            victimVillagesFaked.add((Village) victimModel.getValueAt(i, 1));
        } else {
            victimVillages.add((Village) victimModel.getValueAt(i, 1));
        }
    }
    DefaultTableModel attackModel = (DefaultTableModel) jSourcesTable.getModel();
    Hashtable<UnitHolder, List<Village>> sources = new Hashtable<>();
    Hashtable<UnitHolder, List<Village>> fakes = new Hashtable<>();
    for (int i = 0; i
            < attackModel.getRowCount(); i++) {
        Village vSource = (Village) attackModel.getValueAt(i, 0);
        UnitHolder uSource = (UnitHolder) attackModel.getValueAt(i, 1);
        boolean fake = (Boolean) attackModel.getValueAt(i, 2);
        if (!fake) {
            TroopHelper.fillSourcesWithAttacksForUnit(vSource, sources, null, uSource);
        } else {
            TroopHelper.fillSourcesWithAttacksForUnit(vSource, fakes, null, uSource);
        }
    }
    DSWorkbenchAttackInfoPanel info = new DSWorkbenchAttackInfoPanel();
    info.setVillages(sources, victimVillages, fakes, victimVillagesFaked);
    JFrame f = new JFrame();
    f.add(info);
    info.refresh();
    f.setSize(info.getWidth(), info.getHeight());
    f.pack();
    f.setVisible(true);



}//GEN-LAST:event_showAttackInfoEvent

private void fireShowPlayerSourcesOnlyChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireShowPlayerSourcesOnlyChangedEvent
    fireFilterSourceVillagesByGroupEvent();
}//GEN-LAST:event_fireShowPlayerSourcesOnlyChangedEvent

private void fireUpdateSelectionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUpdateSelectionEvent
    int start = 1;
    int end = 10;

    start = UIHelper.parseIntFromField(jSelectionStart, 1);
    end = UIHelper.parseIntFromField(jSelectionEnd, 10);

    try {
        //switch numbers if start larger than end
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        int diff = end - start + 1;
        if (evt == null || evt.getSource() == jSelectButton) {
            //do nothing
        } else if (evt.getSource() == jSelectionBeginButton && jSelectionBeginButton.isEnabled()) {
            start = 1;
            end = diff;
        } else if (evt.getSource() == jPrevSelectionButton) {
            start -= diff;
            if (start <= 0) {
                start = 1;
            }
            end = start + diff - 1;
        } else if (evt.getSource() == jNextSelectionButton) {
            start = end + 1;
            end = (start + diff - 1 > jSourceVillageList.getModel().getSize()) ? jSourceVillageList.getModel().getSize() : (start + diff - 1);
        } else if (evt.getSource() == jSelectionEndButton && jSelectionEnd.isEnabled()) {
            end = jSourceVillageList.getModel().getSize();
            start = end - diff + 1;
        }
        jSelectionEndButton.setEnabled(!(end == jSourceVillageList.getModel().getSize()));
        jSelectionBeginButton.setEnabled(!(start == 1));
        jSelectionStart.setText(Integer.toString(start));
        jSelectionEnd.setText(Integer.toString(end));
        jSourceVillageList.getSelectionModel().setSelectionInterval(start - 1, end - 1);
        jSourceVillageList.scrollRectToVisible(jSourceVillageList.getCellBounds(start - 1, end - 1));
    } catch (Exception e) {
        logger.warn("Error while calculating source selection step", e);
    }
}//GEN-LAST:event_fireUpdateSelectionEvent

private void fireSourceRelationChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireSourceRelationChangedEvent
    if (jSourceGroupRelation.isSelected()) {
        jSourceGroupRelation.setText("Verknüpfung (ODER)");
    } else {
        jSourceGroupRelation.setText("Verknüpfung (UND)");
    }
    fireFilterSourceVillagesByGroupEvent();
}//GEN-LAST:event_fireSourceRelationChangedEvent

private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
    if (evt.getSource().equals(jxSourceInfoLabel)) {
        sourceInfoPanel.setCollapsed(true);
    } else if (evt.getSource().equals(jxTargetInfoLabel)) {
        targetInfoPanel.setCollapsed(true);
    } else if (evt.getSource().equals(jxResultInfoLabel)) {
        resultInfoPanel.setCollapsed(true);
    }
}//GEN-LAST:event_fireHideInfoEvent

    private void fireTransferAttacksToAttackViewEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferAttacksToAttackViewEvent
        List<Attack> results = getAllResults();
        if (results == null || results.isEmpty()) {
            showInfo("Keine Angriffe verfügbar", true);
            return;
        }

        jExistingPlanBox.setModel(new DefaultComboBoxModel(AttackManager.getSingleton().getGroups()));
        jNewPlanName.setText(null);
        jResultTransferDialog.setLocationRelativeTo(jResultFrame);
        jResultTransferDialog.pack();
        jResultTransferDialog.setVisible(true);
    }//GEN-LAST:event_fireTransferAttacksToAttackViewEvent

    private void fireEnableWarningEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireEnableWarningEvent
        jLabel12.setEnabled(jEnableWarnBox.isSelected());
        jLabel13.setEnabled(jEnableWarnBox.isSelected());
        jTextField1.setEnabled(jEnableWarnBox.isSelected());
        GlobalOptions.addProperty("attack.planer.enable.check", Boolean.toString(jEnableWarnBox.isSelected()));
        GlobalOptions.addProperty("attack.planer.check.amount", jTextField1.getText());
    }//GEN-LAST:event_fireEnableWarningEvent

    private void fireAddVillagesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddVillagesEvent
        List<Village> villages = new ArrayList<>();
        if (evt.getSource() == jAllSources) {
            DefaultListModel model = (DefaultListModel) jSourceVillageList.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                villages.add((Village) model.getElementAt(i));
            }
            fireAddSourcesEvent(villages);
        } else if (evt.getSource() == jSelectedSources) {
            List selection = jSourceVillageList.getSelectedValuesList();
            if (selection == null || selection.isEmpty()) {
                showInfo("Keine Dörfer gewählt");
                return;
            }
            for (Object v : selection) {
                villages.add((Village) v);
            }
            fireAddSourcesEvent(villages);
        } else if (evt.getSource() == jAllTargets) {
            DefaultListModel model = (DefaultListModel) jTargetVillageList.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                villages.add((Village) model.getElementAt(i));
            }
            fireAddTargetsEvent(villages);
        } else if (evt.getSource() == jSelectedTargets) {
            List selection = jTargetVillageList.getSelectedValuesList();
            if (selection == null || selection.isEmpty()) {
                showInfo("Keine Dörfer gewählt");
                return;
            }
            for (Object v : selection) {
                villages.add((Village) v);
            }
            fireAddTargetsEvent(villages);
        }
        showInfo(villages.size() + ((villages.size() == 1) ? " Dorf " : " Dörfer ") + "eingefügt");

    }//GEN-LAST:event_fireAddVillagesEvent

    private void fireTransferResultsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferResultsEvent
        if (evt.getSource() == jDoTransferButton) {
            List<Attack> results = getAllResults();
            if (results == null || results.isEmpty()) {
                showInfo("Keine Angriffe verfügbar", true);
                return;
            } else {
                String plan = null;
                if (jExistingPlanBox.isEnabled()) {
                    plan = (String) jExistingPlanBox.getSelectedItem();
                } else {
                    plan = jNewPlanName.getText();
                    AttackManager.getSingleton().addGroup(plan);
                }

                AttackManager.getSingleton().invalidate();
                for (Attack a : results) {
                    AttackManager.getSingleton().addManagedElement(plan, a);
                }
                AttackManager.getSingleton().revalidate(plan, true);

                showInfo(((results.size() == 1) ? "Angriff " : results.size() + " Angriffe ") + "in Angriffsplan '" + plan + "' übertragen", true);
            }
        }
        jResultTransferDialog.setVisible(false);
    }//GEN-LAST:event_fireTransferResultsEvent

    private void fireNewResultTargetPlanChangedEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireNewResultTargetPlanChangedEvent
        boolean enableExisting = !(jNewPlanName.getText() != null && !jNewPlanName.getText().isEmpty());
        jExistingPlanBox.setEnabled(enableExisting);
        jLabel14.setEnabled(enableExisting);
    }//GEN-LAST:event_fireNewResultTargetPlanChangedEvent

    private void fireTransferEvent(TRANSFER_TYPE pType) {
        switch (pType) {
            case COPY_SOURCE_TO_INTERNAL_CLIPBOARD:
                sourceToInternalClipboardAction(pType);
                break;
            case CUT_SOURCE_TO_INTERNAL_CLIPBOARD:
                sourceToInternalClipboardAction(pType);
                break;
            case PASTE_SOURCE_FROM_INTERNAL_CLIPBOARD:
                sourceFromInternalClipboardAction();
                break;
            case DELETE_SOURCE:
                deleteAction(jSourcesTable);
                break;
            case COPY_TARGET_TO_INTERNAL_CLIPBOARD:
                targetToInternalClipboardAction(pType);
                break;
            case CUT_TARGET_TO_INTERNAL_CLIPBOARD:
                targetToInternalClipboardAction(pType);
                break;
            case PASTE_TARGET_FROM_INTERNAL_CLIPBOARD:
                targetFromInternalClipboardAction();
                break;
            case DELETE_TARGET:
                deleteAction(jVictimTable);
                break;
            case ATTACK_TO_BB:
                attackToBBAction();
                break;
            case COPY_ATTACK_TO_INTERNAL_CLIPBOARD:
                attackToInternalFormatAction();
                break;
            case DELETE_ATTACK:
                deleteAction(jResultsTable);
                break;
        }
        updateInfo();
    }

    private void sourceToInternalClipboardAction(TRANSFER_TYPE pType) {
        int[] rows = jSourcesTable.getSelectedRows();

        if (rows == null || rows.length == 0) {
            showInfo("Keine Einträge ausgewählt");
            return;
        }
        StringBuilder b = new StringBuilder();
        for (int row : rows) {
            Village v = (Village) jSourcesTable.getValueAt(row, 0);
            UnitHolder unit = (UnitHolder) jSourcesTable.getValueAt(row, 1);
            Boolean fake = (Boolean) jSourcesTable.getValueAt(row, 2);
            b.append(v.getId()).append(";").append(unit.getPlainName()).append(";").append(fake).append("\n");
        }

        if(copyToClipboard(b.toString(), rows)) {
            if (pType.equals(TRANSFER_TYPE.CUT_SOURCE_TO_INTERNAL_CLIPBOARD)) {
                deleteAction(jSourcesTable);
                showSuccess(rows.length + ((rows.length == 1) ? " Eintrag ausgeschnitten" : " Einträge ausgeschnitten"));
            }
        }
    }

    private void targetToInternalClipboardAction(TRANSFER_TYPE pType) {
        int[] rows = jVictimTable.getSelectedRows();

        if (rows == null || rows.length == 0) {
            showInfo("Keine Einträge ausgewählt");
            return;
        }
        StringBuilder b = new StringBuilder();
        for (int row : rows) {
            Village v = (Village) jVictimTable.getValueAt(row, 1);
            Boolean fake = (Boolean) jVictimTable.getValueAt(row, 2);
            Integer attacks = (Integer) jVictimTable.getValueAt(row, 3);
            b.append(v.getId()).append(";").append(fake).append(";").append(attacks).append("\n");
        }

        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
            showSuccess(rows.length + ((rows.length == 1) ? " Eintrag kopiert" : " Einträge kopiert"));
            //  return true;
        } catch (HeadlessException hex) {
            showError("Fehler beim Kopieren der Einträge");
            return;
        }

        if (copyToClipboard(b.toString(), rows)) {
            if (pType.equals(TRANSFER_TYPE.CUT_SOURCE_TO_INTERNAL_CLIPBOARD)) {
                deleteAction(jVictimTable);
                showSuccess(rows.length + ((rows.length == 1) ? " Eintrag ausgeschnitten" : " Einträge ausgeschnitten"));
            }
        }
    }

    private boolean copyToClipboard(String s, int[] rows) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
            showSuccess(rows.length + ((rows.length == 1) ? " Eintrag kopiert" : " Einträge kopiert"));

            return true;
        } catch (HeadlessException hex) {
            showError("Fehler beim Kopieren der Einträge");

            return false;
        }
    }

    private void sourceFromInternalClipboardAction() {
        String data = "";
        try {
            data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);

            String[] lines = data.split("\n");
            int cnt = 0;
            DefaultTableModel theModel = (DefaultTableModel) jSourcesTable.getModel();
            for (String line : lines) {
                String[] split = line.split(";");
                Village v = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(split[0]));
                if (v.getTribe() != Barbarians.getSingleton()) {
                    UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(split[1]);
                    Boolean fake = Boolean.parseBoolean(split[2]);
                    if (v != null && unit != null) {
                        theModel.addRow(new Object[]{v, unit, fake, 0});
                        cnt++;
                    }
                }
            }

            showSuccess(cnt + ((cnt == 1) ? " Eintrag eingefügt" : " Einträge eingefügt"));
        } catch (UnsupportedFlavorException | IOException ufe) {
            logger.error("Failed to copy data from internal clipboard", ufe);
            showError("Fehler beim Einfügen aus der Zwischenablage");
        } catch (NumberFormatException nfe) {
            //invalid paste, try village parser       
            List<Village> villages = PluginManager.getSingleton().executeVillageParser(data);
            if (!villages.isEmpty()) {
                UnitHolder unit = (UnitHolder) jTroopsList.getSelectedItem();
                if (unit == null) {
                    unit = DataHolder.getSingleton().getUnitByPlainName("ram");
                }
                addSourceVillages(villages, (UnitHolder) jTroopsList.getSelectedItem(), jMarkAsFakeBox.isSelected());
            } else {
                showInfo("Keine verwendbaren Daten in der Zwischenablage gefunden");
            }
        }
    }

    private void targetFromInternalClipboardAction() {
        String data = "";
        try {
            data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);

            String[] lines = data.split("\n");
            int cnt = 0;
            DefaultTableModel theModel = (DefaultTableModel) jVictimTable.getModel();
            for (String line : lines) {
                String[] split = line.split(";");
                Village v = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(split[0]));
                if (v != null) {
                    boolean added = false;
                    for (int i = 0; i < theModel.getRowCount(); i++) {
                        if (jVictimTable.getValueAt(i, 1).equals(v)) {
                            Integer amount = (Integer) jVictimTable.getValueAt(i, 3);
                            jVictimTable.setValueAt(amount + 1, i, 3);
                            cnt++;
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        Boolean fake = Boolean.parseBoolean(split[1]);
                        Integer attacks = Integer.parseInt(split[2]);
                        if (attacks != null) {
                            theModel.addRow(new Object[]{v.getTribe(), v, fake, attacks, 0});
                            cnt++;
                        }
                    }
                }
            }
            showSuccess(cnt + ((cnt == 1) ? " Eintrag eingefügt" : " Einträge eingefügt"));
        } catch (UnsupportedFlavorException | IOException ufe) {
            logger.error("Failed to copy data from internal clipboard", ufe);
            showError("Fehler beim Einfügen aus der Zwischenablage");
        } catch (NumberFormatException nfe) {
            //invalid paste, try village parser       
            List<Village> villages = PluginManager.getSingleton().executeVillageParser(data);
            if (!villages.isEmpty()) {
                addTargetVillages(villages);
            } else {
                showInfo("Keine verwendbaren Daten in der Zwischenablage gefunden");
            }
        }
    }

    private void deleteAction(JXTable pTable) {
        int removed = TableHelper.deleteSelectedRows(pTable);
        String message = "Keine Einträge ausgewählt";
        boolean bRemoved = false;
        if (removed == 1) {
            message = "Eintrag gelöscht ";
            bRemoved = true;
        } else if (removed > 1) {
            message = removed + " Einträge gelöscht";
            bRemoved = true;
        }

        boolean isResult = false;

        if (pTable.equals(jResultsTable)) {
            isResult = true;
        }

        if (bRemoved) {
            showSuccess(message, isResult);
        } else {
            showInfo(message, isResult);
        }
    }

    private void attackToInternalFormatAction() {
        List<Attack> selection = getSelectedResults();
        StringBuilder b = new StringBuilder();
        int cnt = 0;
        for (Attack a : selection) {
            b.append(Attack.toInternalRepresentation(a)).append("\n");
            cnt++;
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
            showSuccess(cnt + ((cnt == 1) ? " Angriff kopiert" : " Angriffe kopiert"), true);
        } catch (HeadlessException hex) {
            showError("Fehler beim Kopieren der Angriffe", true);
        }
    }

    private void attackToBBAction() {
        try {
            List<Attack> attacks = getSelectedResults();
            if (attacks.isEmpty()) {
                showInfo("Keine Angriffe ausgewählt", true);
                return;
            }
            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(jResultFrame, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

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
                buffer.append(" mit DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/size]\n");
            } else {
                buffer.append("\nErstellt am ");
                buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                buffer.append(" mit DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "\n");
            }

            String b = buffer.toString();
            StringTokenizer t = new StringTokenizer(b, "[");
            int cnt = t.countTokens();
            if (cnt > 1000) {
                if (JOptionPaneHelper.showQuestionConfirmBox(jResultFrame, "Die ausgewählten Angriffe benötigen mehr als 1000 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
            showInfo("BB-Codes in Zwischenablage kopiert", true);
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            showError("Fehler beim Kopieren in die Zwischenablage", true);
        }
    }

    private List<Attack> getSelectedResults() {
        List<Attack> attacks = new LinkedList<>();
        int[] rows = jResultsTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return attacks;
        }

        List<Village> notFullTargets = new LinkedList<>();
        if (jFullTargetsOnly.isSelected()) {
            logger.debug("Getting targets that does not have the requested amount of attacks");
            for (int i = 0; i < jTargetDetailsTable.getRowCount(); i++) {
                String attsForTarget = (String) jTargetDetailsTable.getValueAt(i, 2);
                String[] split = attsForTarget.split("/");
                if (Integer.parseInt(split[0]) != Integer.parseInt(split[1])) {
                    notFullTargets.add((Village) jTargetDetailsTable.getValueAt(i, 1));
                }
            }
        }

        for (int row : rows) {
            Village t = (Village) jResultsTable.getValueAt(row, 2);
            if (!notFullTargets.contains(t)) {
                Village s = (Village) jResultsTable.getValueAt(row, 0);
                UnitHolder unit = (UnitHolder) jResultsTable.getValueAt(row, 1);

                Date d = (Date) jResultsTable.getValueAt(row, 3);
                long arriveTime = d.getTime() + (long) (DSCalculator.calculateMoveTimeInSeconds(s, t, unit.getSpeed()) * 1000);

                int type = (Integer) jResultsTable.getValueAt(row, 5);
                Attack a = new Attack();
                a.setSource(s);
                a.setTarget(t);
                a.setUnit(unit);
                a.setArriveTime(new Date(arriveTime));
                a.setType(type);
                attacks.add(a);
            }
        }
        return attacks;
    }

    private List<Attack> getAllResults() {
        List<Attack> attacks = new LinkedList<>();

        List<Village> notFullTargets = new LinkedList<>();
        if (jFullTargetsOnly.isSelected()) {
            logger.debug("Getting targets that does not have the requested amount of attacks");
            for (int i = 0; i < jTargetDetailsTable.getRowCount(); i++) {
                String attsForTarget = (String) jTargetDetailsTable.getValueAt(i, 2);
                String[] split = attsForTarget.split("/");
                if (Integer.parseInt(split[0]) != Integer.parseInt(split[1])) {
                    notFullTargets.add((Village) jTargetDetailsTable.getValueAt(i, 1));
                }
            }
        }

        for (int row = 0; row < jResultsTable.getRowCount(); row++) {
            Village t = (Village) jResultsTable.getValueAt(row, 2);
            if (!notFullTargets.contains(t)) {
                Village s = (Village) jResultsTable.getValueAt(row, 0);
                UnitHolder unit = (UnitHolder) jResultsTable.getValueAt(row, 1);

                Date d = (Date) jResultsTable.getValueAt(row, 3);
                long arriveTime = d.getTime() + (long) (DSCalculator.calculateMoveTimeInSeconds(s, t, unit.getSpeed()) * 1000);

                int type = (Integer) jResultsTable.getValueAt(row, 5);
                Attack a = new Attack();
                a.setSource(s);
                a.setTarget(t);
                a.setUnit(unit);
                a.setArriveTime(new Date(arriveTime));
                a.setType(type);
                attacks.add(a);
            }
        }
        return attacks;
    }

    private int getRequiredTroopAmount() {
        int result = 20000;
        try {
            result = Integer.parseInt(jTextField1.getText());
        } catch (Exception e) {
            result = 20000;
            jTextField1.setText("20000");
        }
        return result;
    }

    private void addSourceVillages(List<Village> pSourceVillages, UnitHolder pUnit, boolean pAsFake) {
        List<Village> villagesWithSmallTroopCount = new LinkedList<>();
        if (jEnableWarnBox.isSelected()) {
            for (Village pSource : pSourceVillages) {
                VillageTroopsHolder troopsForVillage = TroopsManager.getSingleton().getTroopsForVillage(pSource, TroopsManager.TROOP_TYPE.OWN);
                if (troopsForVillage != null) {
                    int ownTroopsInVillage = troopsForVillage.getTroopPopCount();
                    if (ownTroopsInVillage < getRequiredTroopAmount()) {
                        if (!villagesWithSmallTroopCount.contains(pSource)) {
                            villagesWithSmallTroopCount.add(pSource);
                        }
                    }
                } else {
                    if (!villagesWithSmallTroopCount.contains(pSource)) {
                        villagesWithSmallTroopCount.add(pSource);
                    }
                }
            }
        }
        boolean ignoreSmallTroopCountVillages = false;
        if (!villagesWithSmallTroopCount.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("Laut internen Truppeninformationen ");
            builder.append((villagesWithSmallTroopCount.size() == 1) ? "enthält " : "enthalten ");
            builder.append(Integer.toString(villagesWithSmallTroopCount.size()));
            builder.append((villagesWithSmallTroopCount.size() == 1) ? " Dorf " : " Dörfer ");
            builder.append("weniger als ").append(getRequiredTroopAmount()).append(" verfügbare Einheiten oder es sind keine Informationen bekannt.\n");
            builder.append((villagesWithSmallTroopCount.size() == 1) ? "Soll dieses Dorf ignoriert werden?" : "Sollen diese Dörfer ignoriert werden?");
            ignoreSmallTroopCountVillages = (JOptionPaneHelper.showQuestionConfirmBox(this, builder.toString(), "Information", "Nein", "Ja") == JOptionPane.YES_OPTION);
        }
        for (Village pSource : pSourceVillages) {
            if (!(ignoreSmallTroopCountVillages && villagesWithSmallTroopCount.contains(pSource))) {
                ((DefaultTableModel) jSourcesTable.getModel()).addRow(new Object[]{pSource, pUnit, pAsFake, 0});
            }
        }
    }

    private void addTargetVillages(List<Village> pVillages) {
        int maxAttacksPerVillage = 1;
        try {
            maxAttacksPerVillage = (Integer) jMaxAttacksPerVillage.getValue();
        } catch (Exception e) {
            maxAttacksPerVillage = 1;
        }

        DefaultTableModel theModel = (DefaultTableModel) jVictimTable.getModel();
        for (Village v : pVillages) {
            if (v.getTribe() != null) {
                boolean added = false;
                for (int i = 0; i < theModel.getRowCount(); i++) {
                    if (jVictimTable.getValueAt(i, 1).equals(v)) {
                        Integer amount = (Integer) jVictimTable.getValueAt(i, 3);
                        jVictimTable.setValueAt(amount + 1, i, 3);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    ((DefaultTableModel) jVictimTable.getModel()).addRow(new Object[]{v.getTribe(), v, jMarkTargetAsFake.isSelected(), maxAttacksPerVillage, 0});
                }
            }
        }
    }

    public void prepareForDefense(DefenseInformation[] pElements) {
        for (DefenseInformation elem : pElements) {
            fireAddTargetEvent(elem.getTarget(), elem.getNeededSupports());
            mSettingsPanel.addTimeSpanExternally(new DefenseTimeSpan(elem.getTarget(), new LongRange(elem.getFirstAttack().getTime(), elem.getLastAttack().getTime())));
        }
    }

    /**
     * Add selected target villages filtered by points
     */
    private void fireAddFilteredTargetVillages() {
        Tribe target = (Tribe) jTargetTribeList.getSelectedValue();
        if (target == null) {
            return;
        }
        jVictimTable.invalidate();
        int size = jTargetVillageList.getModel().getSize();
        List<Village> validTargets = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            Village victimVillage = (Village) jTargetVillageList.getModel().getElementAt(i);
            int idx = jAllTargetsComboBox.getSelectedIndex();
            boolean add = false;
            if (idx == 0) {
                add = true;
            } else if (idx == 1 && victimVillage.getPoints() >= 3000) {
                add = true;
            } else if (idx == 2 && victimVillage.getPoints() >= 5000) {
                add = true;
            } else if (idx == 3 && victimVillage.getPoints() >= 7000) {
                add = true;
            }
            if (add) {
                validTargets.add(victimVillage);
            }
        }
        fireAddTargetsEvent(validTargets);
    }

    /**
     * Add source villages externally (see DSWorkbenchMainFrame)
     *
     * @param pSources
     */
    public void fireAddSourcesEvent(List<Village> pSources) {
        UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
        addSourceVillages(pSources, uSource, jMarkAsFakeBox.isSelected());
        updateInfo();
    }

    private void updateInfo() {
        if (jVictimTable.getModel().getColumnCount() < 5) {
            //not set up yet
            return;
        }
        // <editor-fold defaultstate="collapsed" desc="Update status bar info">
        StringBuilder builder = new StringBuilder();
        builder.append("<html><nobr><b>Herkunft: </b>");
        int sources = jSourcesTable.getRowCount();
        int fakes = 0;
        List<Village> sourceVillages = new LinkedList<>();
        for (int i = 0; i < sources; i++) {
            Village sourceVillage = (Village) jSourcesTable.getValueAt(i, 0);
            if (sourceVillage != null && !sourceVillages.contains(sourceVillage)) {
                sourceVillages.add(sourceVillage);
            }

            Object val = jSourcesTable.getValueAt(i, 2);
            if (val != null) {
                if ((Boolean) val) {
                    fakes++;
                }
            }
        }
        builder.append(Integer.toString(sources));
        if (sources == 1) {
            builder.append(" Eintrag (");
        } else {
            builder.append(" Einträge (");
        }
        builder.append(Integer.toString(sources - fakes));
        if (sources - fakes == 1) {
            builder.append(" Off, ");
        } else {
            builder.append(" Offs, ");
        }
        builder.append(Integer.toString(fakes));
        if (fakes == 1) {
            builder.append(" Fake)");
        } else {
            builder.append(" Fakes)");
        }
        int targets = jVictimTable.getRowCount();
        int targetAttacks = 0;
        int targetFake = 0;
        for (int i = 0; i < targets; i++) {
            Object val = jVictimTable.getValueAt(i, 3);
            int attacksOnVillage = 0;
            if (val != null) {
                attacksOnVillage = (Integer) val;
            }
            targetAttacks += attacksOnVillage;
            val = jVictimTable.getValueAt(i, 2);
            if (val != null) {
                targetFake += ((Boolean) val) ? attacksOnVillage : 0;
            } else {
                targetFake = 0;
            }
        }
        builder.append(" <b>Ziele: </b>");
        if (sources < targetAttacks) {
            //more targets than sources
            builder.append("<b color='#FF0000'>");
            builder.append(Integer.toString(targetAttacks));
            if (targetAttacks == 1) {
                builder.append(" Eintrag ");
            } else {
                builder.append(" Einträge ");
            }
            builder.append("</b>");
            builder.append("(");
        } else {
            builder.append(Integer.toString(targetAttacks));
            if (targetAttacks == 1) {
                builder.append(" Eintrag (");
            } else {
                builder.append(" Einträge (");
            }
        }
        builder.append(Integer.toString(targets));
        if (targets == 1) {
            builder.append(" Ziel, ");
        } else {
            builder.append(" Ziele, ");
        }
        builder.append(Integer.toString(targetAttacks - targetFake));
        if (targetAttacks - targetFake == 1) {
            builder.append(" Off, ");
        } else {
            builder.append(" Offs, ");
        }
        builder.append(Integer.toString(targetFake));
        if (targetFake == 1) {
            builder.append(" Fake)");
        } else {
            builder.append(" Fakes)");
        }
        builder.append("</nobr></html>");
        jInfoLabel.setText(builder.toString());
// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Update table-attacks column info">
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (mSettingsPanel != null) {
                    TimeFrame f = mSettingsPanel.getTimeFrame();
                    if (f.isValid()) {
                        for (int j = 0; j < jVictimTable.getRowCount(); j++) {
                            jVictimTable.setValueAt(0, j, 4);
                        }
                        for (int j = 0; j < jSourcesTable.getRowCount(); j++) {
                            jSourcesTable.setValueAt(0, j, 3);
                        }
                    }
                }
            }
        });
        // </editor-fold>
    }

    /**
     * Add target villages externally (see DSWorkbenchMainFrame)
     *
     * @param pVillages
     */
    public void fireAddTargetsEvent(List<Village> pVillages) {
        DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
        for (Village v : pVillages) {
            if (v != null) {
                boolean contains = false;
                for (int row = 0; row < victimModel.getRowCount(); row++) {
                    if (victimModel.getValueAt(row, 1).equals(v)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    int maxAttacks = 1;
                    try {
                        maxAttacks = (Integer) jMaxAttacksPerVillage.getValue();
                    } catch (Exception e) {
                        maxAttacks = 1;
                    }
                    if (v != null) {
                        victimModel.addRow(new Object[]{v.getTribe(), v, jMarkTargetAsFake.isSelected(), maxAttacks, 0});
                    }
                }
            }
        }
        updateInfo();
        ((DefaultTableModel) jVictimTable.getModel()).fireTableDataChanged();
    }

    public void fireAddTargetEvent(Village pTarget, int pAmount) {
        DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();

        if (pTarget != null) {
            boolean contains = false;
            for (int row = 0; row < jVictimTable.getRowCount(); row++) {
                if (jVictimTable.getValueAt(row, 1).equals(pTarget)) {
                    contains = true;
                    jVictimTable.setValueAt(pAmount, row, 3);
                    break;
                }
            }
            if (!contains) {
                victimModel.addRow(new Object[]{pTarget.getTribe(), pTarget, jMarkTargetAsFake.isSelected(), pAmount, 0});
            }
        }

        updateInfo();
        victimModel.fireTableDataChanged();
    }

    /**
     * Show result frame for calculated attacks
     */
    private void showResults(List<Attack> pAttacks) {
        mLogFrame.setVisible(false);
        jResultsTable.invalidate();
        DefaultTableModel resultModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Einheit", "Ziel", "Start", "Ankunft", "Typ", ""}) {

            Class[] types = new Class[]{
                Village.class, UnitHolder.class, Village.class, Date.class, Date.class, Integer.class, Boolean.class
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

        //renderer, which marks send times red if attack is impossible to send
        DefaultTableRenderer renderer = new DefaultTableRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DateCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                JLabel l = (JLabel) c;
                Boolean impossible = (Boolean) table.getModel().getValueAt(row, 6);
                if (impossible) {
                    l.setText("<html><nobr><font color='#FF0000'>" + l.getText() + "</font></nobr></html>");
                }
                return c;
            }
        };

        jResultsTable.setDefaultRenderer(Date.class, renderer);
        jResultsTable.setDefaultRenderer(Integer.class, new NoteIconCellRenderer(NoteIconCellRenderer.ICON_TYPE.NOTE));
        jResultsTable.setDefaultEditor(Integer.class, new NoteIconCellEditor(NoteIconCellEditor.ICON_TYPE.NOTE));
        jResultsTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jResultsTable.setDefaultEditor(UnitHolder.class, new UnitCellEditor());
        jResultsTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
        jResultsTable.setRowHeight(24);
        List<Long> startTimes = new LinkedList<>();
        int impossibleAttacks = 0;

        for (Attack a : pAttacks) {
            long targetTime = a.getArriveTime().getTime();
            long startTime = targetTime - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
            boolean impossible = false;
            if (!startTimes.contains(startTime)) {
                startTimes.add(startTime);
            } else {
                impossibleAttacks++;
                impossible = true;
            }
            resultModel.addRow(new Object[]{a.getSource(), a.getUnit(), a.getTarget(), new Date(startTime), a.getArriveTime(), a.getType(), impossible});
        }

        jResultsTable.setModel(resultModel);

        TableColumnExt columns = jResultsTable.getColumnExt(5);
        columns.setVisible(false);
        jResultsTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());

        jResultFrame.setVisible(true);

        if (impossibleAttacks > 0) {
            String message = "";
            if (impossibleAttacks == 1) {
                message = "<html>Ein berechneter Angriff hat einen bereits verwendeten Abschickzeitpunkt.<br/>Der entsprechende Angriff ist in der Tabelle rot markiert</html>";
            } else {
                message = "<html>" + impossibleAttacks + " berechnete Angriffe haben identische Abschickzeitpunkte.<br/>Die entsprechenden Angriffe sind in der Tabelle rot markiert</html>";
            }
            showInfo(message, true);
        }
    }

    /**
     * Create detail frames shown after calculation
     */
    private void buildDetailedStatistics(HashMap<Village, String> attackMappings, List<Village> pNotAssignedVillages) {
        // <editor-fold defaultstate="collapsed" desc="Build not assigned source table">
        Collections.sort(pNotAssignedVillages);
        DefaultTableModel sourcesModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Spieler", "Dorf"}) {

            private Class[] cTypes = new Class[]{
                Tribe.class, Village.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return cTypes[columnIndex];
            }
        };
        for (Village notAssigned : pNotAssignedVillages) {
            Tribe t = notAssigned.getTribe();
            if (t == null) {
                sourcesModel.addRow(new Object[]{Barbarians.getSingleton(), notAssigned});
            } else {
                sourcesModel.addRow(new Object[]{t, notAssigned});
            }
        }
        jNotAssignedSourcesTable.setModel(sourcesModel);
        TableRowSorter<TableModel> sourcesSorter = new TableRowSorter<TableModel>(sourcesModel);
        jNotAssignedSourcesTable.setRowSorter(sourcesSorter);
        DefaultTableCellRenderer headerRenderer = new SortableTableHeaderRenderer();
        for (int i = 0; i < jNotAssignedSourcesTable.getColumnCount(); i++) {
            jNotAssignedSourcesTable.getColumn(jNotAssignedSourcesTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }
        jNotAssignedSourcesTable.revalidate();
        //</editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Build attacks per target table">
        DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Spieler", "Dorf", "Angriffe"}) {

            Class[] types = new Class[]{
                Tribe.class, Village.class, String.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        List<Village> notFullTargets = new LinkedList<>();
        for (Village key : attackMappings.keySet()) {
            Tribe t = key.getTribe();
            //int notAssignedAmount = attackMappings.get(key);
            String attackCount = attackMappings.get(key);
            String[] split = attackCount.split("/");
            int notAssignedAmount = Integer.parseInt(split[1]) - Integer.parseInt(split[0]);
            if (t != Barbarians.getSingleton()) {
                tableModel.addRow(new Object[] {t, key, attackCount});
            } else {
                tableModel.addRow(new Object[] {"Barbaren", key, attackCount});
            }
            if (notAssignedAmount > 0) {
                notFullTargets.add(key);
            }
        }
        jTargetDetailsTable.setModel(tableModel);
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
        jTargetDetailsTable.setRowSorter(sorter);
        DefaultTableCellRenderer coloredRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, row);
                String t = ((DefaultTableCellRenderer) c).getText();
                ((DefaultTableCellRenderer) c).setText(t);
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                int r = table.convertRowIndexToModel(row);
                String sVal = (String) model.getValueAt(r, 2);
                String[] split = sVal.split("/");
                long max = Long.parseLong(split[1]);
                long v = Long.parseLong(split[0]);
                long diff = max - v;
                Color back = Color.RED;
                if (v == 0) {
                    //color stays red
                } else if (v == max) {
                    back = Color.GREEN;
                } else {
                    float posv = 100.0f * (float) diff / (float) max;
                    posv = ((int) posv / 10) * 10;
                    posv /= 100;
                    Color LAST_SEGMENT = new Color(255, 100, 0);
                    int red = (int) Math.rint((float) LAST_SEGMENT.getRed() * (1.0f - posv) + (float) Color.YELLOW.getRed() * posv);
                    int green = (int) Math.rint((float) LAST_SEGMENT.getGreen() * (1.0f - posv) + (float) Color.YELLOW.getGreen() * posv);
                    int blue = (int) Math.rint((float) LAST_SEGMENT.getBlue() * (1.0f - posv) + (float) Color.YELLOW.getBlue() * posv);
                    if (red < 0) {
                        red = 0;
                    }
                    if (green < 0) {
                        green = 0;
                    }
                    if (blue < 0) {
                        blue = 0;
                    }
                    if (red > 254) {
                        red = 254;
                    }
                    if (green > 254) {
                        green = 254;
                    }
                    if (blue > 254) {
                        blue = 254;
                    }
                    back = new Color(red, green, blue);
                }
                DefaultTableCellRenderer renderer = ((DefaultTableCellRenderer) c);
                if (!isSelected) {
                    renderer.setBackground(back);
                }
                return c;
            }
        };
        jTargetDetailsTable.setDefaultRenderer(Village.class, coloredRenderer);
        jTargetDetailsTable.setDefaultRenderer(Integer.class, coloredRenderer);
        jTargetDetailsTable.setDefaultRenderer(String.class, coloredRenderer);
        jTargetDetailsTable.setDefaultRenderer(Tribe.class, coloredRenderer);
        for (int i = 0; i < jTargetDetailsTable.getColumnCount(); i++) {
            jTargetDetailsTable.getColumn(jTargetDetailsTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }
        jTargetDetailsTable.revalidate();
        //</editor-fold>
    }
    // <editor-fold defaultstate="collapsed" desc="Source selection handlers">

    /**
     * Filter source lists by selected groups
     */
    private void fireFilterSourceVillagesByGroupEvent() {
        List<Village> villageList = getGroupFilteredSourceVillages();
        String[] continents = VillageUtils.getContinents(villageList.toArray(new Village[villageList.size()]));
        DefaultListModel contModel = new DefaultListModel();
        for (String cont : continents) {
            contModel.addElement(cont);
        }
        //set continents list -> village list updates automatically via continent list listener
        jSourceContinentList.setModel(contModel);
        if (continents.length > 0) {
            jSourceContinentList.getSelectionModel().setSelectionInterval(0, continents.length - 1);
        }
    }

    /**
     * Get source villages filtered by selected groups
     */
    private List<Village> getGroupFilteredSourceVillages() {
        List values = jVillageGroupList.getSelectedValuesList();

        List<Tag> tags = new LinkedList<>();
        for (Object o : values) {
            tags.add((Tag) o);
        }
        Village[] villages = VillageUtils.getVillagesByTag(tags.toArray(new Tag[tags.size()]), (jPlayerSourcesOnlyBox.isSelected()) ? GlobalOptions.getSelectedProfile().getTribe() : null,
                (jSourceGroupRelation.isSelected()) ? VillageUtils.RELATION.OR : VillageUtils.RELATION.AND, false, null);
        return Arrays.asList(villages);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Target selection handlers">

    /**
     * Filter target lists by selected allies
     */
    private void fireFilterTargetByAllyEvent() {
        Ally a = (Ally) jTargetAllyList.getSelectedValue();
        Tribe[] tribes = AllyUtils.getTribes(a, Tribe.CASE_INSENSITIVE_ORDER);
        DefaultListModel model = new DefaultListModel();
        for (Tribe t : tribes) {
            model.addElement(t);
        }
        jTargetTribeList.setModel(model);
        jTargetTribeList.setSelectedIndex(0);
    }

    /**
     * Filter target lists by selected tribes
     */
    private void fireFilterTargetByTribeEvent() {
        Tribe t = (Tribe) jTargetTribeList.getSelectedValue();
        String[] continents = VillageUtils.getContinents(t);
        DefaultListModel contModel = new DefaultListModel();
        for (String cont : continents) {
            contModel.addElement(cont);
        }
        jTargetContinentList.setModel(contModel);
        if (continents.length > 0) {
            jTargetContinentList.getSelectionModel().setSelectionInterval(0, continents.length - 1);
        }
    }

    /**
     * Filter source list by selected continents
     */
    private void fireFilterSourceContinentEvent() {
        List continents = jSourceContinentList.getSelectedValuesList();
        //build list of allowed continents
        List<Integer> allowedContinents = new LinkedList<>();
        for (Object cont : continents) {
            int contId = Integer.parseInt(((String) cont).replaceAll("K", ""));
            allowedContinents.add(contId);
        }
        List<Village> villageList = getGroupFilteredSourceVillages();
        Village[] filtered = VillageUtils.getVillagesByContinent(villageList.toArray(new Village[villageList.size()]),
                allowedContinents.toArray(new Integer[allowedContinents.size()]), Village.CASE_INSENSITIVE_ORDER);

        DefaultListModel villageModel = new DefaultListModel();
        for (Village v : filtered) {
            villageModel.addElement(v);
        }
        jSourceVillageList.setModel(villageModel);
    }

    /**
     * Filter target lists by selected continents
     */
    private void fireFilterTargetByContinentEvent() {
        List continents = jTargetContinentList.getSelectedValuesList();
        //build list of allowed continents
        List<Integer> allowedContinents = new LinkedList<>();
        for (Object cont : continents) {
            int contId = Integer.parseInt(((String) cont).replaceAll("K", ""));
            allowedContinents.add(contId);
        }

        List<Tribe> tribes = new ArrayList<>();
        for (Object tribe : jTargetTribeList.getSelectedValuesList()) {
            tribes.add((Tribe) tribe);
        }

        Village[] filtered = VillageUtils.getVillagesByContinent(VillageUtils.getVillages(tribes.toArray(new Tribe[tribes.size()])),
                allowedContinents.toArray(new Integer[allowedContinents.size()]),
                Village.CASE_INSENSITIVE_ORDER);

        DefaultListModel<Village> villageModel = new DefaultListModel<>();
        for (Village v : filtered) {
            villageModel.addElement(v);
        }
        jTargetVillageList.setModel(villageModel);
    }
    // </editor-fold>

    @Override
    public void fireCalculationFinishedEvent(AbstractAttackAlgorithm pParent) {
        //disable "calculating" progress bar
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                jCalculateButton.setEnabled(true);
            }
        });
        List<Attack> attackList = new LinkedList<>();
        List<Village> targets = new LinkedList<>();
        logger.debug("Transferring calculated attacks and its targets to separate lists");
        int fullOffs = 0;
        int cnt = 0;
        for (AbstractTroopMovement movement : pParent.getResults()) {
            List<Attack> atts = null;
            atts = movement.getAttacks(pParent.getTimeFrame(), new LinkedList<Long>());
            if (atts.size() == movement.getMaxOffs()) {
                fullOffs++;
            }
            for (Attack attack : atts) {
                cnt++;
                attackList.add(attack);
                if (!targets.contains(attack.getTarget())) {
                    targets.add(attack.getTarget());
                }
            }
        }

        logger.debug("Adding input targets to map");
        HashMap<Village, String> attackMappings = new HashMap<>();
        //get targets and attack count
        for (int i = 0; i < jVictimTable.getRowCount(); i++) {
            attackMappings.put((Village) jVictimTable.getValueAt(i, 1), "0/" + jVictimTable.getValueAt(i, 3));
        }
        logger.debug("Calculating attack amount per village");
        for (Attack a : attackList) {
            Village v = a.getTarget();
            String val = attackMappings.get(v);
            String[] split = val.split("/");
            attackMappings.put(v, Integer.parseInt(split[0]) + 1 + "/" + split[1]);
        }
        // </editor-fold>
        int numOutputTargets = targets.size();
        int calculatedAttacks = attackList.size();
        jTargetsBar.setMaximum(jVictimTable.getRowCount());
        jTargetsBar.setValue(numOutputTargets);
        jTargetsBar.setString(numOutputTargets + " / " + jVictimTable.getRowCount());
        jFullOffsBar.setMaximum(pParent.getResults().size());
        jFullOffsBar.setValue(fullOffs);
        jFullOffsBar.setString(fullOffs + " / " + pParent.getResults().size());
        jAttacksBar.setMaximum(jSourcesTable.getRowCount());
        jAttacksBar.setValue(calculatedAttacks);
        jAttacksBar.setString(calculatedAttacks + " / " + jSourcesTable.getRowCount());
        //get not assigned offs
        List<Village> notAssigned = new LinkedList<>();
        for (int i = 0; i < jSourcesTable.getRowCount(); i++) {
            Village source = (Village) jSourcesTable.getValueAt(i, 0);
            //if (!notAssigned.contains(source)) {
            notAssigned.add(source);
            // }
        }
        for (Attack a : attackList) {
            notAssigned.remove(a.getSource());
        }

        // <editor-fold defaultstate="collapsed" desc="Building details tables">
        buildDetailedStatistics(attackMappings, notAssigned);
        //</editor-fold>
        logger.debug("Sorting attacks by runtime");
        //sort result by start time
        Collections.sort(attackList, AbstractTroopMovement.RUNTIME_SORT);
        logger.debug("Building results...");
        showResults(attackList);
    }

    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        List<ManageableType> elements = TagManager.getSingleton().getAllElements();

        List<Tag> tags = new ArrayList<>();
        for (ManageableType e : elements) {
            Tag t = (Tag) e;
            tags.add(t);
        }

        Collections.sort(tags, new Comparator<Tag>() {

            @Override
            public int compare(Tag o1, Tag o2) {
                try {
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                } catch (Exception e) {
                    return 0;
                }
            }
        });

        DefaultListModel tagModel = new DefaultListModel();
        tagModel.addElement(NoTag.getSingleton());
        for (Tag t : tags) {
            tagModel.addElement(t);
        }
        jVillageGroupList.setModel(tagModel);
        //select all groups and initialize lists
        jVillageGroupList.getSelectionModel().setSelectionInterval(0, (elements.size() > 0) ? elements.size() : 0);

    }

    // <editor-fold defaultstate="collapsed" desc="Drag&Drop handling">
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(VillageTransferable.villageDataFlavor) || dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            if (dtde.getDropTargetContext().getComponent() == jSourcesTable || dtde.getDropTargetContext().getComponent() == jVictimTable) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            } else {
                dtde.rejectDrag();
            }
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        Transferable t = dtde.getTransferable();
        List<Village> villages = new LinkedList<>();
        if (dtde.getDropTargetContext().getComponent() == jSourcesTable || dtde.getDropTargetContext().getComponent() == jVictimTable) {
            if (dtde.isDataFlavorSupported(VillageTransferable.villageDataFlavor)) {
                //village dnd
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                try {
                    villages = (List<Village>) t.getTransferData(VillageTransferable.villageDataFlavor);
                } catch (Exception ignored) {
                }
            } else if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                //string dnd
                try {
                    villages = PluginManager.getSingleton().executeVillageParser((String) t.getTransferData(DataFlavor.stringFlavor));
                } catch (Exception ignored) {
                }
            } else {
                dtde.rejectDrop();
                return;
            }
        } else {
            dtde.rejectDrop();
            return;
        }

        MapPanel.getSingleton().setCurrentCursor(MapPanel.getSingleton().getCurrentCursor());

        if (!villages.isEmpty()) {
            if (jideTabbedPane1.getSelectedIndex() == 0) {
                fireAddSourcesEvent(villages);
            } else if (jideTabbedPane1.getSelectedIndex() == 1) {
                fireAddTargetsEvent(villages);
            }
        }
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
    }
// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mouse gesture handling">

    @Override
    public void fireCloseGestureEvent() {
        setVisible(false);
    }

    @Override
    public void fireExportAsBBGestureEvent() {
    }

    @Override
    public void firePlainExportGestureEvent() {
    }

    @Override
    public void fireRenameGestureEvent() {
    }

    @Override
    public void fireToBackgroundGestureEvent() {
        toBack();
    }

    @Override
    public void fireNextPageGestureEvent() {
        int idx = jideTabbedPane1.getSelectedIndex();
        idx += 1;
        if (idx > jideTabbedPane1.getTabCount() - 1) {
            idx = 0;
        }
        jideTabbedPane1.setSelectedIndex(idx);
    }

    @Override
    public void firePreviousPageGestureEvent() {
        int idx = jideTabbedPane1.getSelectedIndex();
        idx -= 1;
        if (idx < 0) {
            idx = jideTabbedPane1.getTabCount() - 1;
        }
        jideTabbedPane1.setSelectedIndex(idx);
    }
// </editor-fold>

    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));

        GlobalOptions.setSelectedServer("de43");
        DataHolder.getSingleton().loadData(false);
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);
        TroopsManager.getSingleton().initialize();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                MouseGestures mMouseGestures = new MouseGestures();
                mMouseGestures.setMouseButton(MouseEvent.BUTTON3_MASK);
                mMouseGestures.addMouseGesturesListener(new MouseGestureHandler());
                mMouseGestures.start();
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    //  UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                    //JFrame.setDefaultLookAndFeelDecorated(true);

                    // SubstanceLookAndFeel.setSkin(SubstanceLookAndFeel.getAllSkins().get("Twilight").getClassName());
                    //  UIManager.put(SubstanceLookAndFeel.FOCUS_KIND, FocusKind.NONE);
                } catch (Exception ignored) {
                }
                Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));


                TribeTribeAttackFrame f = new TribeTribeAttackFrame();
                f.setup();
                f.setSize(600, 400);
                f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                f.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel2;
    private javax.swing.JButton jAddToAttacksButton1;
    private javax.swing.JButton jAddToAttacksButton2;
    private javax.swing.JButton jAllSources;
    private javax.swing.JButton jAllTargets;
    private javax.swing.JComboBox jAllTargetsComboBox;
    private javax.swing.JDialog jAttackPlanSelectionDialog;
    private org.jdesktop.swingx.JXTable jAttackPlanTable;
    private javax.swing.JFrame jAttackResultDetailsFrame;
    private javax.swing.JProgressBar jAttacksBar;
    private javax.swing.JButton jCalculateButton;
    private javax.swing.JButton jCancelSyncButton;
    private javax.swing.JButton jCancelTransferButton;
    private javax.swing.JButton jCloseResultsButton;
    private javax.swing.JButton jDoSyncButton;
    private javax.swing.JButton jDoTransferButton;
    private javax.swing.JCheckBox jEnableWarnBox;
    private javax.swing.JComboBox jExistingPlanBox;
    private javax.swing.JProgressBar jFullOffsBar;
    private javax.swing.JCheckBox jFullTargetsOnly;
    private javax.swing.JButton jHideAttackDetailsButton;
    private javax.swing.JButton jHideTargetDetailsButton;
    private javax.swing.JLabel jInfoLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jMainPanel;
    private javax.swing.JCheckBox jMarkAsFakeBox;
    private javax.swing.JCheckBox jMarkTargetAsFake;
    private javax.swing.JSpinner jMaxAttacksPerVillage;
    private org.jdesktop.swingx.JXTextField jNewPlanName;
    private javax.swing.JButton jNextSelectionButton;
    private javax.swing.JTable jNotAssignedSourcesTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JCheckBox jPlayerSourcesOnlyBox;
    private javax.swing.JButton jPrevSelectionButton;
    private final javax.swing.JProgressBar jProgressBar1 = new javax.swing.JProgressBar();
    private javax.swing.JDialog jRefreshProgressDialog;
    private javax.swing.JFrame jResultFrame;
    private javax.swing.JDialog jResultTransferDialog;
    private org.jdesktop.swingx.JXTable jResultsTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JButton jSelectButton;
    private javax.swing.JButton jSelectedSources;
    private javax.swing.JButton jSelectedTargets;
    private javax.swing.JButton jSelectionBeginButton;
    private javax.swing.JTextField jSelectionEnd;
    private javax.swing.JButton jSelectionEndButton;
    private javax.swing.JTextField jSelectionStart;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel jSettingsContentPanel;
    private javax.swing.JPanel jSettingsPanel;
    private javax.swing.JList jSourceContinentList;
    private javax.swing.JRadioButton jSourceGroupRelation;
    private javax.swing.JScrollPane jSourceListScrollPane;
    private javax.swing.JPanel jSourcePanel;
    private javax.swing.JList jSourceVillageList;
    private org.jdesktop.swingx.JXTable jSourcesTable;
    private javax.swing.JTextField jTargetAllyFilter;
    private javax.swing.JList jTargetAllyList;
    private javax.swing.JList jTargetContinentList;
    private javax.swing.JTable jTargetDetailsTable;
    private javax.swing.JPanel jTargetPanel;
    private javax.swing.JFrame jTargetResultDetailsFrame;
    private javax.swing.JList jTargetTribeList;
    private javax.swing.JList<Village> jTargetVillageList;
    private javax.swing.JProgressBar jTargetsBar;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JComboBox jTroopsList;
    private org.jdesktop.swingx.JXTable jVictimTable;
    private javax.swing.JList jVillageGroupList;
    private com.jidesoft.swing.JideTabbedPane jideTabbedPane1;
    private javax.swing.JPanel jxAttackPlanerPanel;
    private org.jdesktop.swingx.JXLabel jxResultInfoLabel;
    private org.jdesktop.swingx.JXLabel jxSettingsInfoLabel;
    private org.jdesktop.swingx.JXLabel jxSourceInfoLabel;
    private org.jdesktop.swingx.JXLabel jxTargetInfoLabel;
    private org.jdesktop.swingx.JXCollapsiblePane resultInfoPanel;
    private org.jdesktop.swingx.JXCollapsiblePane settingsInfoPanel;
    private org.jdesktop.swingx.JXCollapsiblePane sourceInfoPanel;
    private org.jdesktop.swingx.JXCollapsiblePane targetInfoPanel;
    // End of variables declaration//GEN-END:variables

    static class RefreshThread extends Thread {

        private SettingsPanel mSettingsPanel;
        private JXTable jSourcesTable;
        private JXTable jVictimTable;
        private JDialog jDialog;
        private JProgressBar mBar;

        public RefreshThread(JDialog dialog, JProgressBar bar, SettingsPanel pPanel, JXTable source, JXTable target) {
            setName("AttackCalculationRefreshThread");
            jDialog = dialog;
            mSettingsPanel = pPanel;
            jSourcesTable = source;
            jVictimTable = target;
            mBar = bar;
            setDaemon(true);
        }

        public void run() {
            if (mSettingsPanel != null) {
                TimeFrame f = mSettingsPanel.getTimeFrame();
                DefaultTableModel sourceModel = (DefaultTableModel) jSourcesTable.getModel();
                DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
                if (f.isValid()) {
                    int victimAmountCol = jVictimTable.convertColumnIndexToModel(4);
                    for (int j = 0; j < victimModel.getRowCount(); j++) {
                        victimModel.setValueAt(0, j, victimAmountCol);
                    }

                    int sourceVillageCol = jSourcesTable.convertColumnIndexToModel(0);
                    int victimVillageCol = jVictimTable.convertColumnIndexToModel(1);
                    int sourceUnitCol = jSourcesTable.convertColumnIndexToModel(1);
                    int sourceAmountCol = jSourcesTable.convertColumnIndexToModel(3);

                    for (int i = 0; i < sourceModel.getRowCount(); i++) {
                        int targets = 0;
                        for (int j = 0; j < victimModel.getRowCount(); j++) {
                            Village source = (Village) sourceModel.getValueAt(i, sourceVillageCol);
                            Village target = (Village) victimModel.getValueAt(j, victimVillageCol);
                            UnitHolder unit = (UnitHolder) sourceModel.getValueAt(i, sourceUnitCol);
                            long run = (long) DSCalculator.calculateMoveTimeInSeconds(source, target, unit.getSpeed()) * 1000;
                            if (f.isMovementPossible(run, source)) {
                                targets++;
                                victimModel.setValueAt((Integer) victimModel.getValueAt(j, victimAmountCol) + 1, j, victimAmountCol);
                            }
                        }
                        if (i % 10 == 0) {
                            mBar.setValue(i * victimModel.getRowCount());
                        }

                        sourceModel.setValueAt(targets, i, sourceAmountCol);
                    }
                }
                //showSuccess("Mögliche Angriffe aktualisiert");
                mBar.setString("Mögliche Angriffe aktualisiert");
                try {
                    Thread.sleep(100);
                } catch (Exception ignored) {
                }
                // jideTabbedPane1.setSelectedIndex(0);
                jDialog.setVisible(false);
            }
        }
    }
}
