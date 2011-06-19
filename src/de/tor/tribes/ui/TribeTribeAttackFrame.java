/*
 * AllyAllyAttackFrame.java
 *
 * Created on 29. Juli 2008, 11:17
 */
package de.tor.tribes.ui;

import com.jidesoft.swing.JideTabbedPane;
import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.TroopFilterElement;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.TroopSplitDialog.TroopSplit;
import de.tor.tribes.ui.algo.AlgorithmLogPanel;
import de.tor.tribes.ui.algo.SettingsChangedListener;
import de.tor.tribes.ui.algo.SettingsPanel;
import de.tor.tribes.ui.dnd.VillageTransferable;
import de.tor.tribes.ui.editors.AttackTypeCellEditor;
import de.tor.tribes.ui.editors.FakeCellEditor;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.renderer.AlternatingColorCellRenderer;
import de.tor.tribes.ui.renderer.AttackTypeCellRenderer;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.FakeCellRenderer;
import de.tor.tribes.ui.renderer.SortableTableHeaderRenderer;
import de.tor.tribes.ui.renderer.TribeCellRenderer;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.ui.renderer.UnitListCellRenderer;
import de.tor.tribes.ui.renderer.VillageCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.DSWorkbenchGesturedFrame;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.MouseGestureHandler;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.TableHelper;
import de.tor.tribes.util.algo.AbstractAttackAlgorithm;
import de.tor.tribes.util.algo.AlgorithmListener;
import de.tor.tribes.util.algo.BruteForce;
import de.tor.tribes.util.algo.Iterix;
import de.tor.tribes.util.algo.TimeFrame;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.bb.AttackListFormatter;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import java.awt.Color;
import java.util.StringTokenizer;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Collections;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.apache.log4j.ConsoleAppender;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * @author Jejkal
 */
public class TribeTribeAttackFrame extends DSWorkbenchGesturedFrame implements
        ActionListener,
        AlgorithmListener,
        DropTargetListener,
        DragGestureListener,
        DragSourceListener,
        SettingsChangedListener,
        GenericManagerListener, ListSelectionListener {

    public static enum TRANSFER_TYPE {

        COPY_SOURCE_TO_INTERNAL_CLIPBOARD, CUT_SOURCE_TO_INTERNAL_CLIPBOARD, PASTE_SOURCE_FROM_INTERNAL_CLIPBOARD, DELETE_SOURCE,
        COPY_TARGET_TO_INTERNAL_CLIPBOARD, CUT_TARGET_TO_INTERNAL_CLIPBOARD, PASTE_TARGET_FROM_INTERNAL_CLIPBOARD, DELETE_TARGET,
        COPY_ATTACK_TO_INTERNAL_CLIPBOARD, ATTACK_TO_BB, DELETE_ATTACK
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            JXCollapsiblePane panel = null;
            JXLabel label = null;
            int selectionCount = 0;
            if (e.getSource() != null && e.getSource().equals(jSourcesTable.getSelectionModel())) {
                panel = sourceInfoPanel;
                label = jxSourceInfoLabel;
                selectionCount = jSourcesTable.getSelectedRowCount();
            } else if (e.getSource() != null && e.getSource().equals(jVictimTable.getSelectionModel())) {
                panel = targetInfoPanel;
                label = jxTargetInfoLabel;
                selectionCount = jVictimTable.getSelectedRowCount();
            } else if (e.getSource() != null && e.getSource().equals(jResultsTable.getSelectionModel())) {
                panel = resultInfoPanel;
                label = jxResultInfoLabel;
                selectionCount = jResultsTable.getSelectedRowCount();
            }

            if (selectionCount != 0) {
                showInfo(panel, label, selectionCount + ((selectionCount == 1) ? " Eintrag gewählt" : " Einträge gewählt"));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Copy")) {
            if (e.getSource() != null && e.getSource().equals(jSourcesTable)) {
                fireTransferEvent(TRANSFER_TYPE.COPY_SOURCE_TO_INTERNAL_CLIPBOARD);
            } else if (e.getSource() != null && e.getSource().equals(jVictimTable)) {
                fireTransferEvent(TRANSFER_TYPE.COPY_TARGET_TO_INTERNAL_CLIPBOARD);
            } else if (e.getSource() != null && e.getSource().equals(jResultsTable)) {
                fireTransferEvent(TRANSFER_TYPE.COPY_ATTACK_TO_INTERNAL_CLIPBOARD);
            }
        } else if (e.getActionCommand().equals("Paste")) {
            if (e.getSource() != null && e.getSource().equals(jSourcesTable)) {
                fireTransferEvent(TRANSFER_TYPE.PASTE_SOURCE_FROM_INTERNAL_CLIPBOARD);
            } else if (e.getSource() != null && e.getSource().equals(jVictimTable)) {
                fireTransferEvent(TRANSFER_TYPE.PASTE_TARGET_FROM_INTERNAL_CLIPBOARD);
            }
        } else if (e.getActionCommand().equals("Cut")) {
            if (e.getSource() != null && e.getSource().equals(jSourcesTable)) {
                fireTransferEvent(TRANSFER_TYPE.CUT_SOURCE_TO_INTERNAL_CLIPBOARD);
            } else if (e.getSource() != null && e.getSource().equals(jVictimTable)) {
                fireTransferEvent(TRANSFER_TYPE.CUT_TARGET_TO_INTERNAL_CLIPBOARD);
            }
        } else if (e.getActionCommand().equals("Delete")) {
            if (e.getSource() != null && e.getSource().equals(jSourcesTable)) {
                fireTransferEvent(TRANSFER_TYPE.DELETE_SOURCE);
            } else if (e.getSource() != null && e.getSource().equals(jVictimTable)) {
                fireTransferEvent(TRANSFER_TYPE.DELETE_TARGET);
            } else if (e.getSource() != null && e.getSource().equals(jResultsTable)) {
                fireTransferEvent(TRANSFER_TYPE.DELETE_ATTACK);
            } else if (e.getSource() != null && e.getSource().equals(jFilterList)) {
                removeSelectedFilters();
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

    /** Creates new form TribeTribeAttackFrame */
    public TribeTribeAttackFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jMainPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildPanel(jxAttackPlanerPanel);

        buildMenu();

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
        jFilterList.registerKeyboardAction(TribeTribeAttackFrame.this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

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

        jSourcesTable.getSelectionModel().addListSelectionListener(this);
        jVictimTable.getSelectionModel().addListSelectionListener(this);
        jResultsTable.getSelectionModel().addListSelectionListener(this);

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
        mTroopSplitDialog = new TroopSplitDialog(this, true);
        mSettingsPanel = new SettingsPanel(this);
        jSettingsPanel.add(mSettingsPanel, BorderLayout.CENTER);
        jAttackResultDetailsFrame.pack();
        jTargetResultDetailsFrame.pack();
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(TribeTribeAttackFrame.this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        new DropTarget(jSourcesTable, this);
        new DropTarget(jVictimTable, this);
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

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
     /*   GlobalOptions.getHelpBroker().enableHelp(jSourcePanel, "pages.attack_planer_source", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelp(jTargetPanel, "pages.attack_planer_target", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelp(mSettingsPanel, "pages.attack_planer_settings", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelpKey(jResultFrame.getRootPane(), "pages.attack_planer_results", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelpKey(jTargetResultDetailsFrame.getRootPane(), "pages.attack_planer_results_details_targets", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelpKey(jAttackResultDetailsFrame.getRootPane(), "pages.attack_planer_results_details_sources", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.attack_planer", GlobalOptions.getHelpBroker().getHelpSet());
         */  // </editor-fold>
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
        filterByAttackPlan.setToolTipText("Entfernt aller Herkunfts- oder Zieldörfer, die bereits in einem vorhandenen Angriffsplan auftauchen");
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

    /**Setup attack frame (clear entries, fill lists and set initial values)*/
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
        jSourcesTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jVictimTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        String[] cols = new String[]{"Einheit", "Fake", "Anwendbar"};
        for (String col : cols) {
            TableColumnExt columns = jSourcesTable.getColumnExt(col);
            columns.setPreferredWidth(80);
            columns.setMaxWidth(80);
            columns.setWidth(80);
        }

        cols = new String[]{"Fake", "Angriffe", "Anwendbar"};
        for (String col : cols) {
            TableColumnExt columns = jVictimTable.getColumnExt(col);
            columns.setPreferredWidth(80);
            columns.setMaxWidth(80);
            columns.setWidth(80);
        }

        jSourcesTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jVictimTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jResultsTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));

        jAttackPlanSelectionDialog.pack();
        // <editor-fold defaultstate="collapsed" desc="Build filter dialog">
        jFilterUnitBox.setModel(new DefaultComboBoxModel(DataHolder.getSingleton().getUnits().toArray(new UnitHolder[]{})));
        jFilterUnitBox.setRenderer(new UnitListCellRenderer());
        jFilterList.setModel(new DefaultListModel());
// </editor-fold>
        try {
            // <editor-fold defaultstate="collapsed" desc=" Build target allies list ">
            fireTargetAllyFilterChangedEvent(null);
            jTargetTribeList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    fireFilterTargetByTribeEvent();
                }
            });
            jTargetContinentList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    fireFilterTargetByContinentEvent();
                }
            });
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
            /*   jFilterFrame.pack();
            jFilterFrame.setLocationRelativeTo(this);
            jFilterFrame.setVisible(true);*/

            List<Village> sources = new LinkedList<Village>();
            for (int i = 0; i < jSourcesTable.getRowCount(); i++) {
                //go through all rows in attack table and get source village
                sources.add((Village) jSourcesTable.getValueAt(i, 0));
            }
            int sizeBefore = sources.size();

            if (sizeBefore == 0) {
                showInfo(sourceInfoPanel, jxSourceInfoLabel, "Keine Herkunftsdörfer vorhanden");
                return;
            }
            filterDialog.show(sources);

            for (int i = jSourcesTable.getRowCount() - 1; i >= 0; i--) {
                //go through all rows in attack table and get source village
                Village v = (Village) jSourcesTable.getValueAt(i, 0);
                if (!sources.contains(v)) {
                    ((DefaultTableModel) jSourcesTable.getModel()).removeRow(jSourcesTable.convertRowIndexToModel(i));
                }
            }

            int diff = sizeBefore - sources.size();
            if (diff == 0) {
                showSuccess(sourceInfoPanel, jxSourceInfoLabel, "Keine Dörfer entfernt");
            } else {
                showSuccess(sourceInfoPanel, jxSourceInfoLabel, ((diff == 1) ? "Ein Dorf entfernt" : diff + " Dörfer entfernt"));
            }
        } else {
            //no valid tab    
            showInfo(targetInfoPanel, jxTargetInfoLabel, "Diese Funktion ist nur für Herkunftsdörfer verfügbar");
        }
    }

    private void filterByUsage() {
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
                if (col == 0) {
                    return false;
                }
                return true;
            }
        };
        String[] plans = AttackManager.getSingleton().getGroups();
        for (String plan : plans) {
            model.addRow(new Object[]{plan, false});
        }
        jAttackPlanTable.setModel(model);
        jAttackPlanTable.repaint();
        jAttackPlanTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jAttackPlanSelectionDialog.setLocationRelativeTo(DSWorkbenchMainFrame.getSingleton().getAttackPlaner());
        jAttackPlanSelectionDialog.setVisible(true);
    }

    private void editUnit() {
        int idx = jideTabbedPane1.getSelectedIndex();
        if (idx == 0) {
            int[] rows = jSourcesTable.getSelectedRows();
            if (rows == null || rows.length == 0) {
                //no row selected
                showInfo(sourceInfoPanel, jxSourceInfoLabel, "Keine Einträge ausgewählt");
                return;
            }
            UnitHolder unit = (UnitHolder) jTroopsList.getSelectedItem();
            if (unit == null) {
                showInfo(sourceInfoPanel, jxSourceInfoLabel, "Keine Einheit ausgewählt");
                return;
            }
            for (int row : rows) {
                jSourcesTable.setValueAt(unit, row, 1);
            }
            String message = ((rows.length == 1) ? "Eintrag " : rows.length + " Einträge ") + "auf '" + unit.getName() + "' geändert";
            showSuccess(sourceInfoPanel, jxSourceInfoLabel, message);
        } else {
            showInfo(targetInfoPanel, jxTargetInfoLabel, "Diese Funktion ist nur für Herkunftsdörfer verfügbar");
        }
    }

    private void editUseSnobs() {
        int idx = jideTabbedPane1.getSelectedIndex();
        if (idx == 0) {
            //use snobs in villages where snobs exist
            DefaultTableModel model = (DefaultTableModel) jSourcesTable.getModel();
            UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
            jSourcesTable.invalidate();
            Hashtable<Village, Integer> assignedTroops = new Hashtable<Village, Integer>();
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
            showInfo(sourceInfoPanel, jxSourceInfoLabel, "Vorhandene AGs eingetragen");
        } else {
            showInfo(targetInfoPanel, jxTargetInfoLabel, "Diese Funktion ist nur für Herkunftsdörfer verfügbar");
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
        }
        if (table == null) {
            //no valid tab seleted
            return;
        }
        int[] rows = table.getSelectedRows();
        if (rows == null || rows.length == 0) {
            //no row selected
            if (idx == 0) {
                showInfo(sourceInfoPanel, jxSourceInfoLabel, "Keine Einträge ausgewählt");
            } else if (idx == 1) {
                showInfo(targetInfoPanel, jxTargetInfoLabel, "Keine Einträge ausgewählt");
            }
            return;
        }
        for (int row : rows) {
            table.setValueAt(pFake, row, 2);
        }

        String message = ((rows.length == 1) ? "Eintrag " : rows.length + " Einträge ") + "als Fake markiert";
        if (idx == 0) {
            showSuccess(sourceInfoPanel, jxSourceInfoLabel, message);
        } else if (idx == 1) {
            showSuccess(targetInfoPanel, jxTargetInfoLabel, message);
        }

        updateInfo();
    }

    private void editChangeAttacks(int pDirection) {
        int idx = jideTabbedPane1.getSelectedIndex();
        if (idx == 1) {
            int[] rows = jVictimTable.getSelectedRows();
            if (rows == null || rows.length == 0) {
                showInfo(targetInfoPanel, jxTargetInfoLabel, "Keine Ziele ausgewählt");
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
            showSuccess(targetInfoPanel, jxTargetInfoLabel, message);

            updateInfo();
        } else {
            //invalid tab
            showInfo(sourceInfoPanel, jxSourceInfoLabel, "Diese Funktion ist nur für Ziele verfügbar");
        }
    }

    private void miscSplit() {
        if (jideTabbedPane1.getSelectedIndex() == 0) {
            DefaultTableModel model = (DefaultTableModel) jSourcesTable.getModel();
            int sources = model.getRowCount();
            if (sources == 0) {
                showInfo(sourceInfoPanel, jxSourceInfoLabel, "Keine Herkunftsdörfer eingetragen");
                return;
            }
            List<Village> sourceVillages = new LinkedList<Village>();
            Hashtable<Village, UnitHolder> attTable = new Hashtable<Village, UnitHolder>();
            Hashtable<Village, UnitHolder> fakeTable = new Hashtable<Village, UnitHolder>();
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

            String message = ((sourceVillages.size() == 1) ? "Herkunftsdorf" : sourceVillages.size() + " Herkunftsdörfer ")
                    + ((overallSplitCount == 1) ? "einmal" : overallSplitCount + " mal ") + " geteilt";
            showSuccess(sourceInfoPanel, jxSourceInfoLabel, message);

        } else {
            showInfo(targetInfoPanel, jxTargetInfoLabel, "Diese Funktion ist nur für Herkunftsdörfer verfügbar");
        }
    }

    private void miscRefreshPossibleAttacks() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
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
                                if (f.isMovementPossible(run, source.getTribe())) {
                                    targets++;
                                    victimModel.setValueAt((Integer) victimModel.getValueAt(j, victimAmountCol) + 1, j, victimAmountCol);
                                }
                            }
                            sourceModel.setValueAt(targets, i, sourceAmountCol);
                        }
                    }
                    showInfo(sourceInfoPanel, jxSourceInfoLabel, "Mögliche Angriffe aktualisiert");
                    showInfo(targetInfoPanel, jxTargetInfoLabel, "Mögliche Angriffe aktualisiert");
                }
            }
        });
    }

    public void showSuccess(JXCollapsiblePane pPanel, JXLabel pLabel, String pMessage) {
        pPanel.setCollapsed(false);
        pLabel.setBackgroundPainter(new MattePainter(Color.GREEN));
        pLabel.setForeground(Color.BLACK);
        pLabel.setText(pMessage);
    }

    public void showInfo(JXCollapsiblePane pPanel, JXLabel pLabel, String pMessage) {
        pPanel.setCollapsed(false);
        pLabel.setBackgroundPainter(new MattePainter(getBackground()));
        pLabel.setForeground(Color.BLACK);
        pLabel.setText(pMessage);
    }

    public void showError(JXCollapsiblePane pPanel, JXLabel pLabel, String pMessage) {
        pPanel.setCollapsed(false);
        pLabel.setBackgroundPainter(new MattePainter(Color.RED));
        pLabel.setForeground(Color.WHITE);
        pLabel.setText(pMessage);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        capabilityInfoPanel2 = new de.tor.tribes.ui.CapabilityInfoPanel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jResultsTable = new org.jdesktop.swingx.JXTable();
        resultInfoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jxResultInfoLabel = new org.jdesktop.swingx.JXLabel();
        attackTypeGroup = new javax.swing.ButtonGroup();
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
        jFilterFrame = new javax.swing.JFrame();
        jScrollPane14 = new javax.swing.JScrollPane();
        jFilterList = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        jFilterUnitBox = new javax.swing.JComboBox();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jButton17 = new javax.swing.JButton();
        jMinValue = new javax.swing.JTextField();
        jMaxValue = new javax.swing.JTextField();
        jApplyFiltersButton = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jStrictFilter = new javax.swing.JCheckBox();
        capabilityInfoPanel3 = new de.tor.tribes.ui.CapabilityInfoPanel();
        jxAttackPlanerPanel = new org.jdesktop.swingx.JXPanel();
        jideTabbedPane1 = new com.jidesoft.swing.JideTabbedPane();
        jSourcePanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jTroopsList = new javax.swing.JComboBox();
        jScrollPane4 = new javax.swing.JScrollPane();
        jVillageGroupList = new javax.swing.JList();
        jScrollPane5 = new javax.swing.JScrollPane();
        jSourceContinentList = new javax.swing.JList();
        jScrollPane6 = new javax.swing.JScrollPane();
        jSourceVillageList = new javax.swing.JList();
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
        jTargetTribeFilter = new javax.swing.JTextField();
        jMarkTargetAsFake = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jMaxAttacksPerVillage = new javax.swing.JSpinner();
        jAllTargetsComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jVictimTable = new org.jdesktop.swingx.JXTable();
        targetInfoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jxTargetInfoLabel = new org.jdesktop.swingx.JXLabel();
        jSettingsPanel = new javax.swing.JPanel();
        jCalculateButton = new javax.swing.JButton();
        jInfoLabel = new javax.swing.JLabel();
        jMainPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.CapabilityInfoPanel();

        jResultFrame.setTitle("Angriffsplan");

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/target.png"))); // NOI18N
        jLabel6.setMaximumSize(new java.awt.Dimension(18, 18));
        jLabel6.setMinimumSize(new java.awt.Dimension(18, 18));
        jLabel6.setPreferredSize(new java.awt.Dimension(18, 18));

        jTargetsBar.setBackground(new java.awt.Color(255, 255, 51));
        jTargetsBar.setForeground(new java.awt.Color(51, 153, 0));
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

        jFullOffsBar.setBackground(new java.awt.Color(255, 255, 51));
        jFullOffsBar.setForeground(new java.awt.Color(51, 153, 0));
        jFullOffsBar.setToolTipText("Angriffe mit max. Off Anzahl / Angriffe gesamt");
        jFullOffsBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jFullOffsBar.setStringPainted(true);

        jAttacksBar.setBackground(new java.awt.Color(255, 0, 0));
        jAttacksBar.setForeground(new java.awt.Color(51, 153, 0));
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
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                    .addComponent(jTargetsBar, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(jAttacksBar, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                    .addComponent(jFullOffsBar, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
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

        jxResultInfoLabel.setOpaque(true);
        jxResultInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        resultInfoPanel.add(jxResultInfoLabel, java.awt.BorderLayout.CENTER);

        jPanel8.add(resultInfoPanel, java.awt.BorderLayout.SOUTH);

        javax.swing.GroupLayout jResultFrameLayout = new javax.swing.GroupLayout(jResultFrame.getContentPane());
        jResultFrame.getContentPane().setLayout(jResultFrameLayout);
        jResultFrameLayout.setHorizontalGroup(
            jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                    .addGroup(jResultFrameLayout.createSequentialGroup()
                        .addComponent(capabilityInfoPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jFullTargetsOnly)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 202, Short.MAX_VALUE)
                        .addComponent(jAddToAttacksButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(capabilityInfoPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jFullTargetsOnly, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAddToAttacksButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCloseResultsButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jFilterFrame.setTitle("Truppenfilter");

        jScrollPane14.setBorder(javax.swing.BorderFactory.createTitledBorder("Verwendete Filter"));

        jScrollPane14.setViewportView(jFilterList);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Neuer Filter"));

        jFilterUnitBox.setMaximumSize(new java.awt.Dimension(51, 25));
        jFilterUnitBox.setMinimumSize(new java.awt.Dimension(51, 25));
        jFilterUnitBox.setPreferredSize(new java.awt.Dimension(51, 25));

        jLabel25.setText("Einheit");

        jLabel26.setText("Min");
        jLabel26.setMaximumSize(new java.awt.Dimension(20, 25));
        jLabel26.setMinimumSize(new java.awt.Dimension(20, 25));
        jLabel26.setPreferredSize(new java.awt.Dimension(20, 25));

        jLabel27.setText("Max");
        jLabel27.setMaximumSize(new java.awt.Dimension(20, 25));
        jLabel27.setMinimumSize(new java.awt.Dimension(20, 25));
        jLabel27.setPreferredSize(new java.awt.Dimension(20, 25));

        jButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton17.setText("Hinzufügen");
        jButton17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddTroopFilterEvent(evt);
            }
        });

        jMinValue.setMaximumSize(new java.awt.Dimension(51, 25));
        jMinValue.setMinimumSize(new java.awt.Dimension(51, 25));
        jMinValue.setPreferredSize(new java.awt.Dimension(51, 25));

        jMaxValue.setMaximumSize(new java.awt.Dimension(51, 25));
        jMaxValue.setMinimumSize(new java.awt.Dimension(51, 25));
        jMaxValue.setPreferredSize(new java.awt.Dimension(51, 25));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jMinValue, 0, 0, Short.MAX_VALUE)
                            .addComponent(jFilterUnitBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                        .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMaxValue, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton17, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(jFilterUnitBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMinValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMaxValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton17)
                .addContainerGap())
        );

        jApplyFiltersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jApplyFiltersButton.setText("Anwenden");
        jApplyFiltersButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireApplyTroopFiltersEvent(evt);
            }
        });

        jButton20.setText("Abbrechen");
        jButton20.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireApplyTroopFiltersEvent(evt);
            }
        });

        jStrictFilter.setSelected(true);
        jStrictFilter.setText("Strenge Filterung");
        jStrictFilter.setToolTipText("<html>Alle Filterbedingungen müssen erf&uuml;llt sein, damit ein Dorf zugelassen wird.<br/>\nIst dieses Feld deaktiviert reicht mindestens eine Bedingung.</html>");
        jStrictFilter.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jStrictFilter.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        capabilityInfoPanel3.setBbSupport(false);
        capabilityInfoPanel3.setCopyable(false);
        capabilityInfoPanel3.setPastable(false);
        capabilityInfoPanel3.setSearchable(false);

        javax.swing.GroupLayout jFilterFrameLayout = new javax.swing.GroupLayout(jFilterFrame.getContentPane());
        jFilterFrame.getContentPane().setLayout(jFilterFrameLayout);
        jFilterFrameLayout.setHorizontalGroup(
            jFilterFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFilterFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFilterFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jFilterFrameLayout.createSequentialGroup()
                        .addGroup(jFilterFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(capabilityInfoPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton20))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jFilterFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jFilterFrameLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jStrictFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                            .addComponent(jApplyFiltersButton, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        jFilterFrameLayout.setVerticalGroup(
            jFilterFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFilterFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jFilterFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(capabilityInfoPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jStrictFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jFilterFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jApplyFiltersButton)
                    .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

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

        jScrollPane6.setBorder(javax.swing.BorderFactory.createTitledBorder("Dörfer"));
        jScrollPane6.setPreferredSize(new java.awt.Dimension(100, 130));

        jSourceVillageList.setDragEnabled(true);
        jScrollPane6.setViewportView(jSourceVillageList);

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSourceGroupRelation, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jMarkAsFakeBox, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                        .addGap(8, 8, 8)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTroopsList, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                    .addComponent(jPlayerSourcesOnlyBox, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSourceGroupRelation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPlayerSourcesOnlyBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTroopsList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jMarkAsFakeBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE))
                .addContainerGap())
        );
        jSourcePanelLayout.setVerticalGroup(
            jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jideTabbedPane1.addTab("Herkunft", new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png")), jSourcePanel); // NOI18N

        jTargetPanel.setBackground(new java.awt.Color(239, 235, 223));
        jTargetPanel.setPreferredSize(new java.awt.Dimension(920, 503));

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

        jTargetTribeFilter.addCaretListener(new javax.swing.event.CaretListener() {
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
                        .addComponent(jTargetTribeFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE))
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                            .addComponent(jMarkTargetAsFake, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jAllTargetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jMaxAttacksPerVillage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
                            .addComponent(jTargetTribeFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                        .addGap(7, 7, 7)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jAllTargetsComboBox, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jTargetPanelLayout.setVerticalGroup(
            jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTargetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jideTabbedPane1.addTab("Ziele", new javax.swing.ImageIcon(getClass().getResource("/res/ally.png")), jTargetPanel); // NOI18N

        jSettingsPanel.setLayout(new java.awt.BorderLayout());
        jideTabbedPane1.addTab("Einstellungen", jSettingsPanel);

        jxAttackPlanerPanel.add(jideTabbedPane1, java.awt.BorderLayout.CENTER);

        jCalculateButton.setBackground(new java.awt.Color(239, 235, 223));
        jCalculateButton.setText("<html><p align=\"center\">Berechnung<br/>starten</p></html>");
        jCalculateButton.setToolTipText("Angriffsplan berechnen");
        jCalculateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateAttackEvent(evt);
            }
        });

        setTitle("Angriffsplaner");
        setBackground(new java.awt.Color(239, 235, 223));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                fireClosingEvent(evt);
            }
        });

        jInfoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/information.png"))); // NOI18N
        jInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showAttackInfoEvent(evt);
            }
        });

        jMainPanel.setLayout(new java.awt.BorderLayout());

        capabilityInfoPanel1.setBbSupport(false);
        capabilityInfoPanel1.setSearchable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(capabilityInfoPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 873, Short.MAX_VALUE))
                    .addComponent(jMainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 931, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jMainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(capabilityInfoPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

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
    List<Village> victimVillages = new LinkedList<Village>();
    List<Village> victimVillagesFaked = new LinkedList<Village>();
    Hashtable<Village, Integer> maxAttacksTable = new Hashtable<Village, Integer>();
    for (int i = 0; i < victimModel.getRowCount(); i++) {
        if ((Boolean) victimModel.getValueAt(i, 2) == Boolean.TRUE) {
            victimVillagesFaked.add((Village) victimModel.getValueAt(i, 1));
        } else {
            victimVillages.add((Village) victimModel.getValueAt(i, 1));
        }
        maxAttacksTable.put((Village) victimModel.getValueAt(i, 1), (Integer) victimModel.getValueAt(i, 3));
    }
//build source-unit map
    int snobSources = 0;
    // <editor-fold defaultstate="collapsed" desc=" Build attacks and fakes">
    Hashtable<UnitHolder, List<Village>> sources = new Hashtable<UnitHolder, List<Village>>();
    Hashtable<UnitHolder, List<Village>> fakes = new Hashtable<UnitHolder, List<Village>>();
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
            if (sourcesForUnit == null) {
                sourcesForUnit = new LinkedList<Village>();
                sourcesForUnit.add(vSource);
                sources.put(uSource, sourcesForUnit);
            } else {
                sourcesForUnit.add(vSource);
            }
        } else {
            List<Village> fakesForUnit = fakes.get(uSource);
            if (fakesForUnit == null) {
                fakesForUnit = new LinkedList<Village>();
                fakesForUnit.add(vSource);
                fakes.put(uSource, fakesForUnit);
            } else {
                fakesForUnit.add(vSource);
            }
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
            } catch (Exception e) {
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
    if (idx == 0) {
        table = jSourcesTable;
    } else if (idx == 1) {
        table = jVictimTable;
    }
    if (table == null) {
        //no valid tab seleted
        return;
    }


    DefaultTableModel model = (DefaultTableModel) jAttackPlanTable.getModel();
    List<String> selectedPlans = new LinkedList<String>();
    for (int i = 0; i < jAttackPlanTable.getRowCount(); i++) {
        int row = jAttackPlanTable.convertRowIndexToModel(i);
        if ((Boolean) model.getValueAt(row, jAttackPlanTable.convertColumnIndexToModel(1))) {
            selectedPlans.add((String) model.getValueAt(row, jAttackPlanTable.convertColumnIndexToModel(0)));
        }
    }
    List<Integer> toRemove = new LinkedList<Integer>();
    //process all plans
    for (String plan : selectedPlans) {
        logger.debug("Checking plan '" + plan + "'");
        List<ManageableType> elements = AttackManager.getSingleton().getAllElements(plan);
        //process all attacks
        for (ManageableType e : elements) {
            Attack a = (Attack) e;
            //search attack source village in all table rows
            for (int i = 0; i < table.getRowCount(); i++) {
                Village v = (Village) table.getValueAt(i, table.convertColumnIndexToModel(0));
                if (a.getSource().equals(v)) {
                    if (!toRemove.contains(i)) {
                        toRemove.add(i);
                    }
                }
            }
        }
    }
    String message = "";
    if (toRemove.isEmpty()) {
        if (idx == 0) {
            showInfo(sourceInfoPanel, jxSourceInfoLabel, "Keine Dörfer zu entfernen");
        } else if (idx == 1) {
            showInfo(targetInfoPanel, jxTargetInfoLabel, "Keine Dörfer zu entfernen");
        }
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
            //  ((DefaultTableModel) table.getModel()).fireTableDataChanged();
            if (idx == 0) {
                showSuccess(sourceInfoPanel, jxSourceInfoLabel, message + "entfernt");
            } else if (idx == 1) {
                showSuccess(targetInfoPanel, jxTargetInfoLabel, message + "entfernt");
            }

        } catch (Exception e) {
            logger.error("Removal failed", e);
            if (idx == 0) {
                showError(sourceInfoPanel, jxSourceInfoLabel, "Fehler beim Entfernen");
            } else if (idx == 1) {
                showError(targetInfoPanel, jxTargetInfoLabel, "Fehler beim Entfernen");
            }
        }
    }
    updateInfo();
}//GEN-LAST:event_fireSynchWithAttackPlansEvent

private void fireTargetAllyFilterChangedEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireTargetAllyFilterChangedEvent
    String text = jTargetTribeFilter.getText();
    if (text.length() > 0) {
        text = text.toLowerCase();
        Enumeration<Integer> allyKeys = DataHolder.getSingleton().getAllies().keys();
        List<Ally> allies = new LinkedList<Ally>();
        while (allyKeys.hasMoreElements()) {
            Ally a = DataHolder.getSingleton().getAllies().get(allyKeys.nextElement());
            if (a.getName() != null && a.getTag() != null) {
                if (a.getName().toLowerCase().indexOf(text) >= 0 || a.getTag().toLowerCase().indexOf(text) >= 0) {
                    allies.add(a);
                }
            }
        }
        Ally[] aAllies = allies.toArray(new Ally[]{});
        allies = null;
        Arrays.sort(aAllies, Ally.CASE_INSENSITIVE_ORDER);
        DefaultListModel targetAllyModel = new DefaultListModel();
        for (Ally a : aAllies) {
            targetAllyModel.addElement(a);
        }
        jTargetAllyList.setModel(targetAllyModel);
        jTargetAllyList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                fireFilterTargetByAllyEvent();
            }
        });
    } else {
        Enumeration<Integer> allyKeys = DataHolder.getSingleton().getAllies().keys();
        List<Ally> allies = new LinkedList<Ally>();
        while (allyKeys.hasMoreElements()) {
            allies.add(DataHolder.getSingleton().getAllies().get(allyKeys.nextElement()));
        }
        Ally[] aAllies = allies.toArray(new Ally[]{});
        allies = null;
        Arrays.sort(aAllies, Ally.CASE_INSENSITIVE_ORDER);
        DefaultListModel targetAllyModel = new DefaultListModel();
        targetAllyModel.addElement("<Kein Stamm>");
        for (Ally a : aAllies) {
            targetAllyModel.addElement(a);
        }
        jTargetAllyList.setModel(targetAllyModel);
        jTargetAllyList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                fireFilterTargetByAllyEvent();
            }
        });
    }
}//GEN-LAST:event_fireTargetAllyFilterChangedEvent

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
    for (int i = 0; i
            < filterModel.size(); i++) {
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
        for (int i = 0; i < jSourcesTable.getRowCount(); i++) {
            boolean villageAllowed = false;
            //go through all rows in attack table and get source village
            Village v = (Village) jSourcesTable.getValueAt(i, 0);
            for (int j = 0; j < filterModel.size(); j++) {
                //check for all filters if villag is allowed
                if (!((TroopFilterElement) filterModel.get(j)).allowsVillage(v)) {
                    if (jStrictFilter.isSelected()) {
                        //village is not allowed, add to remove list if strict filtering is enabled
                        int row = jSourcesTable.convertRowIndexToModel(i);
                        if (!rowsToRemove.contains(row)) {
                            rowsToRemove.add(row);
                            removeCount++;
                            break;
                        }
                    }
                } else {
                    villageAllowed = true;
                    if (!jStrictFilter.isSelected()) {
                        break;
                    }
                }
            }
            if (!jStrictFilter.isSelected()) {
                //if strict filtering is disabled add village only if it is not allowed
                if (!villageAllowed) {
                    int row = jSourcesTable.convertRowIndexToModel(i);
                    if (!rowsToRemove.contains(row)) {
                        rowsToRemove.add(row);
                        removeCount++;
                    }
                }
            }
        }
        Collections.sort(rowsToRemove);
        for (int i = rowsToRemove.size() - 1; i >= 0; i--) {
            int row = rowsToRemove.get(i);
            ((DefaultTableModel) jSourcesTable.getModel()).removeRow(row);
        }
        String message = "Es wurden keine Angriffe entfernt.";
        if (removeCount == 1) {
            message = "Es wurde ein Angriff entfernt.";
        } else if (removeCount > 1) {
            message = "Es wurden " + removeCount + " Angriffe entfernt.";
        } else {
            showInfo(sourceInfoPanel, jxSourceInfoLabel, message);
            return;
        }
        showSuccess(sourceInfoPanel, jxSourceInfoLabel, message);
    }
    jFilterFrame.setVisible(false);
    updateInfo();
}//GEN-LAST:event_fireApplyTroopFiltersEvent
private void fireClosingEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_fireClosingEvent
    mSettingsPanel.storeProperties();
}//GEN-LAST:event_fireClosingEvent

private void fireReOpenLogPanelEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireReOpenLogPanelEvent
    mLogFrame.setVisible(true);
    mLogFrame.toFront();
}//GEN-LAST:event_fireReOpenLogPanelEvent

private void showAttackInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showAttackInfoEvent
    DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
    List<Village> victimVillages = new LinkedList<Village>();
    List<Village> victimVillagesFaked = new LinkedList<Village>();
    for (int i = 0; i
            < victimModel.getRowCount(); i++) {
        if ((Boolean) victimModel.getValueAt(i, 2) == Boolean.TRUE) {
            victimVillagesFaked.add((Village) victimModel.getValueAt(i, 1));
        } else {
            victimVillages.add((Village) victimModel.getValueAt(i, 1));
        }
    }
    DefaultTableModel attackModel = (DefaultTableModel) jSourcesTable.getModel();
    Hashtable<UnitHolder, List<Village>> sources = new Hashtable<UnitHolder, List<Village>>();
    Hashtable<UnitHolder, List<Village>> fakes = new Hashtable<UnitHolder, List<Village>>();
    for (int i = 0; i
            < attackModel.getRowCount(); i++) {
        Village vSource = (Village) attackModel.getValueAt(i, 0);
        UnitHolder uSource = (UnitHolder) attackModel.getValueAt(i, 1);
        boolean fake = (Boolean) attackModel.getValueAt(i, 2);
        if (!fake) {
            List<Village> sourcesForUnit = sources.get(uSource);
            if (sourcesForUnit == null) {
                sourcesForUnit = new LinkedList<Village>();
                sourcesForUnit.add(vSource);
                sources.put(uSource, sourcesForUnit);
            } else {
                sourcesForUnit.add(vSource);
            }
        } else {
            List<Village> fakesForUnit = fakes.get(uSource);
            if (fakesForUnit == null) {
                fakesForUnit = new LinkedList<Village>();
                fakesForUnit.add(vSource);
                fakes.put(uSource, fakesForUnit);
            } else {
                fakesForUnit.add(vSource);
            }
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
    try {
        start = Integer.parseInt(jSelectionStart.getText());
        end = Integer.parseInt(jSelectionEnd.getText());
    } catch (Exception e) {
    }
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
            start = start - diff;
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

    private void removeSelectedFilters() {
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
    }

    private void fireTransferEvent(TRANSFER_TYPE pType) {
        switch (pType) {
            case COPY_SOURCE_TO_INTERNAL_CLIPBOARD:
                sourceToInternalClipboardAction(pType);
                break;
            case CUT_SOURCE_TO_INTERNAL_CLIPBOARD:
                sourceToInternalClipboardAction(pType);
                break;
            case PASTE_SOURCE_FROM_INTERNAL_CLIPBOARD:
                sourceFromInternalClipboardAction(pType);
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
                targetFromInternalClipboardAction(pType);
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
    }

    private void sourceToInternalClipboardAction(TRANSFER_TYPE pType) {
        int[] rows = jSourcesTable.getSelectedRows();

        if (rows == null || rows.length == 0) {
            showInfo(sourceInfoPanel, jxSourceInfoLabel, "Keine Einträge ausgewählt");
            return;
        }
        StringBuilder b = new StringBuilder();
        for (int row : rows) {
            Village v = (Village) jSourcesTable.getValueAt(row, 0);
            UnitHolder unit = (UnitHolder) jSourcesTable.getValueAt(row, 1);
            Boolean fake = (Boolean) jSourcesTable.getValueAt(row, 2);
            b.append(v.getId()).append(";").append(unit.getPlainName()).append(";").append(fake).append("\n");
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
            showSuccess(sourceInfoPanel, jxSourceInfoLabel, rows.length + ((rows.length == 1) ? " Eintrag kopiert" : " Einträge kopiert"));
        } catch (HeadlessException hex) {
            showError(sourceInfoPanel, jxSourceInfoLabel, "Fehler beim Kopieren der Einträge");
            return;
        }

        if (pType.equals(TRANSFER_TYPE.CUT_SOURCE_TO_INTERNAL_CLIPBOARD)) {
            deleteAction(jSourcesTable);
            showSuccess(sourceInfoPanel, jxSourceInfoLabel, rows.length + ((rows.length == 1) ? " Eintrag ausgeschnitten" : " Einträge ausgeschnitten"));
        }
    }

    private void targetToInternalClipboardAction(TRANSFER_TYPE pType) {
        int[] rows = jVictimTable.getSelectedRows();

        if (rows == null || rows.length == 0) {
            showInfo(targetInfoPanel, jxTargetInfoLabel, "Keine Einträge ausgewählt");
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
            showSuccess(targetInfoPanel, jxTargetInfoLabel, rows.length + ((rows.length == 1) ? " Eintrag kopiert" : " Einträge kopiert"));
            //  return true;
        } catch (HeadlessException hex) {
            showError(targetInfoPanel, jxTargetInfoLabel, "Fehler beim Kopieren der Einträge");
            return;
        }

        if (pType.equals(TRANSFER_TYPE.CUT_TARGET_TO_INTERNAL_CLIPBOARD)) {
            deleteAction(jVictimTable);
            showSuccess(targetInfoPanel, jxTargetInfoLabel, rows.length + ((rows.length == 1) ? " Eintrag ausgeschnitten" : " Einträge ausgeschnitten"));
        }
    }

    private void sourceFromInternalClipboardAction(TRANSFER_TYPE pType) {
        String data = "";
        try {
            data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);

            String[] lines = data.split("\n");
            int cnt = 0;
            DefaultTableModel theModel = (DefaultTableModel) jSourcesTable.getModel();
            for (String line : lines) {
                String[] split = line.split(";");
                Village v = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(split[0]));
                UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(split[1]);
                Boolean fake = Boolean.parseBoolean(split[2]);
                if (v != null && unit != null) {
                    theModel.addRow(new Object[]{v, unit, fake, 0});
                    cnt++;
                }
            }
            showSuccess(sourceInfoPanel, jxSourceInfoLabel, cnt + ((cnt == 1) ? " Eintrag eingefügt" : " Einträge eingefügt"));
        } catch (UnsupportedFlavorException ufe) {
            logger.error("Failed to copy data from internal clipboard", ufe);
            showError(sourceInfoPanel, jxSourceInfoLabel, "Fehler beim Einfügen aus der Zwischenablage");
        } catch (IOException ioe) {
            logger.error("Failed to copy data from internal clipboard", ioe);
            showError(sourceInfoPanel, jxSourceInfoLabel, "Fehler beim Einfügen aus der Zwischenablage");
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
                showInfo(sourceInfoPanel, jxSourceInfoLabel, "Keine verwendbaren Daten in der Zwischenablage gefunden");
            }
        }
    }

    private void targetFromInternalClipboardAction(TRANSFER_TYPE pType) {
        String data = "";
        try {
            data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);

            String[] lines = data.split("\n");
            int cnt = 0;
            DefaultTableModel theModel = (DefaultTableModel) jSourcesTable.getModel();
            for (String line : lines) {
                String[] split = line.split(";");
                Village v = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(split[0]));
                Boolean fake = Boolean.parseBoolean(split[1]);
                Integer attacks = Integer.parseInt(split[2]);
                if (v != null && attacks != null) {
                    theModel.addRow(new Object[]{v, fake, attacks, 0});
                    cnt++;
                }
            }
            showSuccess(targetInfoPanel, jxTargetInfoLabel, cnt + ((cnt == 1) ? " Eintrag eingefügt" : " Einträge eingefügt"));
        } catch (UnsupportedFlavorException ufe) {
            logger.error("Failed to copy data from internal clipboard", ufe);
            showError(targetInfoPanel, jxTargetInfoLabel, "Fehler beim Einfügen aus der Zwischenablage");
        } catch (IOException ioe) {
            logger.error("Failed to copy data from internal clipboard", ioe);
            showError(targetInfoPanel, jxTargetInfoLabel, "Fehler beim Einfügen aus der Zwischenablage");
        } catch (NumberFormatException nfe) {
            //invalid paste, try village parser       
            List<Village> villages = PluginManager.getSingleton().executeVillageParser(data);
            if (!villages.isEmpty()) {
                addTargetVillages(villages);
            } else {
                showInfo(targetInfoPanel, jxTargetInfoLabel, "Keine verwendbaren Daten in der Zwischenablage gefunden");
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

        JXCollapsiblePane panel = null;
        JXLabel label = null;

        if (pTable.equals(jSourcesTable)) {
            panel = sourceInfoPanel;
            label = jxSourceInfoLabel;
        } else if (pTable.equals(jVictimTable)) {
            panel = targetInfoPanel;
            label = jxTargetInfoLabel;
        } else if (pTable.equals(jResultsTable)) {
            panel = resultInfoPanel;
            label = jxResultInfoLabel;
        }

        if (bRemoved) {
            showSuccess(panel, label, message);
        } else {
            showInfo(panel, label, message);
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
            showSuccess(resultInfoPanel, jxResultInfoLabel, cnt + ((cnt == 1) ? " Angriff kopiert" : " Angriffe kopiert"));
        } catch (HeadlessException hex) {
            showError(resultInfoPanel, jxResultInfoLabel, "Fehler beim Kopieren der Angriffe");
        }
    }

    private void attackToBBAction() {
        try {
            List<Attack> attacks = getSelectedResults();
            if (attacks.isEmpty()) {
                showInfo(resultInfoPanel, jxResultInfoLabel, "Keine Angriffe ausgewählt");
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
                if (JOptionPaneHelper.showQuestionConfirmBox(jResultFrame, "Die ausgewählten Angriffe benötigen mehr als 1000 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
            showInfo(resultInfoPanel, jxResultInfoLabel, "BB-Codes in Zwischenablage kopiert");
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            showError(resultInfoPanel, jxResultInfoLabel, "Fehler beim Kopieren in die Zwischenablage");
        }
    }

    private List<Attack> getSelectedResults() {
        List<Attack> attacks = new LinkedList<Attack>();
        int[] rows = jResultsTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return attacks;
        }

        List<Village> notFullTargets = new LinkedList<Village>();
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

                int type = (Integer) jResultsTable.getValueAt(row, 4);
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

    private void addSourceVillages(List<Village> pSourceVillages, UnitHolder pUnit, boolean pAsFake) {
        List<Village> villagesWithSmallTroopCount = new LinkedList<Village>();
        for (Village pSource : pSourceVillages) {
            VillageTroopsHolder troopsForVillage = TroopsManager.getSingleton().getTroopsForVillage(pSource);
            if (troopsForVillage != null) {
                int ownTroopsInVillage = troopsForVillage.getTroopPopCount();
                if (ownTroopsInVillage < 20000) {
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
            builder.append("weniger als 20.000 verfügbare Einheiten.\n");
            builder.append((villagesWithSmallTroopCount.size() == 1) ? "Soll dieses Dorf ignoriert werden?" : "Sollen diese Dörfer ignoriert werden?");
            ignoreSmallTroopCountVillages = (JOptionPaneHelper.showQuestionConfirmBox(this, builder.toString(), "Information", "Nein", "Ja") == JOptionPane.YES_OPTION);
        }
        for (Village pSource : pSourceVillages) {
            if (!(ignoreSmallTroopCountVillages && villagesWithSmallTroopCount.contains(pSource))) {
                ((DefaultTableModel) jSourcesTable.getModel()).addRow(new Object[]{pSource, pUnit, pAsFake, 0});
                mSettingsPanel.addTribe(pSource.getTribe());
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
        for (Village v : pVillages) {
            if (v.getTribe() != null) {
                ((DefaultTableModel) jVictimTable.getModel()).addRow(new Object[]{v.getTribe(), v, jMarkTargetAsFake.isSelected(), maxAttacksPerVillage, 0});
            }
        }
    }

    /**Add selected target villages filtered by points*/
    private void fireAddFilteredTargetVillages() {
        Tribe target = (Tribe) jTargetTribeList.getSelectedValue();
        if (target == null) {
            return;
        }
        jVictimTable.invalidate();
        int size = jTargetVillageList.getModel().getSize();
        List<Village> validTargets = new LinkedList<Village>();
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

    /**Add source villages externally (see DSWorkbenchMainFrame)
     * @param pSources
     */
    public void fireAddSourcesEvent(List<Village> pSources) {
        UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
        for (Village v : pSources) {
            if (v != null && v.getTribe() != Barbarians.getSingleton()) {
                ((DefaultTableModel) jSourcesTable.getModel()).addRow(new Object[]{v, uSource, jMarkAsFakeBox.isSelected(), 0});
                mSettingsPanel.addTribe(v.getTribe());
            }
        }
        updateInfo();
    }

    private void updateInfo() {
        // <editor-fold defaultstate="collapsed" desc="Update status bar info">
        StringBuilder builder = new StringBuilder();
        builder.append("<html><nobr><b>Herkunft: </b>");
        int sources = jSourcesTable.getRowCount();
        int fakes = 0;
        List<Village> sourceVillages = new LinkedList<Village>();
        for (int i = 0; i < sources; i++) {
            Village sourceVillage = (Village) jSourcesTable.getValueAt(i, 0);
            if (!sourceVillages.contains(sourceVillage)) {
                sourceVillages.add(sourceVillage);
            }
            if ((Boolean) jSourcesTable.getValueAt(i, 2)) {
                fakes++;
            }
        }
        builder.append(Integer.toString(sources));
        if (sources == 1) {
            builder.append(" Eintrag (");
        } else {
            builder.append(" Einträge (");
        }
        builder.append(Integer.toString(sourceVillages.size() - fakes));
        if (sourceVillages.size() - fakes == 1) {
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
            int attacksOnVillage = (Integer) jVictimTable.getValueAt(i, 3);
            targetAttacks += attacksOnVillage;
            targetFake += ((Boolean) jVictimTable.getValueAt(i, 2)) ? attacksOnVillage : 0;
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

    /**Add target villages externally (see DSWorkbenchMainFrame)
     * @param pVillages
     */
    public void fireAddTargetsEvent(List<Village> pVillages) {
        DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
        jVictimTable.invalidate();
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
        jVictimTable.revalidate();
        jVictimTable.repaint();
    }

    /**Show result frame for calculated attacks*/
    private void showResults(List<Attack> pAttacks) {
        mLogFrame.setVisible(false);
        jResultsTable.invalidate();
        DefaultTableModel resultModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Einheit", "Ziel", "Startzeit", "Typ", ""}) {

            Class[] types = new Class[]{
                Village.class, UnitHolder.class, Village.class, Date.class, Integer.class, Boolean.class
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
        //renderer, which hides the boolean table column
        /*DefaultTableCellRenderer invis = new DefaultTableCellRenderer() {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = new AlternatingColorCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setForeground(Color.WHITE);
        return c;
        }
        };*/
        //renderer, which marks send times red if attack is impossible to send
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DateCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Boolean impossible = (Boolean) table.getModel().getValueAt(row, 5);
                if (impossible.booleanValue()) {
                    c.setBackground(Color.RED);
                }
                return c;
            }
        };

        jResultsTable.setDefaultRenderer(Date.class, renderer);
        //jResultsTable.setDefaultRenderer(Boolean.class, invis);
        jResultsTable.setDefaultRenderer(Integer.class, new AttackTypeCellRenderer());
        jResultsTable.setDefaultEditor(Integer.class, new AttackTypeCellEditor());
        jResultsTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jResultsTable.setDefaultEditor(UnitHolder.class, new UnitCellEditor());
        jResultsTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
        jResultsTable.setRowHeight(24);
        List<Long> startTimes = new LinkedList<Long>();
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
            resultModel.addRow(new Object[]{a.getSource(), a.getUnit(), a.getTarget(), new Date(startTime), a.getType(), impossible});
        }

        jResultsTable.setModel(resultModel);

        TableColumnExt columns = jResultsTable.getColumnExt(5);
        columns.setVisible(false);
        jResultsTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());

        jResultFrame.setVisible(true);

        if (impossibleAttacks > 0) {
            String message = "";
            if (impossibleAttacks == 1) {
                message = "<html>Ein berechneter Angriff kann vermutlich nicht abgeschickt werden.<br/>Der entsprechende Angriff ist in der Tabelle rot markiert</html>";
            } else {
                message = "<html>" + impossibleAttacks + " berechnete Angriffe k&ouml;nnen vermutlich nicht abgeschickt werden.<br/>Die entsprechenden Angriffe sind in der Tabelle rot markiert</html>";
            }
            showInfo(resultInfoPanel, jxResultInfoLabel, message);
        }
    }

    /**Create detail frames shown after calculation*/
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
        List<Village> notFullTargets = new LinkedList<Village>();
        Iterator<Village> keys = attackMappings.keySet().iterator();
        while (keys.hasNext()) {
            Village key = keys.next();
            Tribe t = key.getTribe();
            //int notAssignedAmount = attackMappings.get(key);
            String attackCount = attackMappings.get(key);
            String[] split = attackCount.split("/");
            int notAssignedAmount = Integer.parseInt(split[1]) - Integer.parseInt(split[0]);
            if (t != Barbarians.getSingleton()) {
                tableModel.addRow(new Object[]{t, key, attackCount});
            } else {
                tableModel.addRow(new Object[]{"Barbaren", key, attackCount});
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

    /**Filter source lists by selected groups*/
    private void fireFilterSourceVillagesByGroupEvent() {
        List<Village> villageList = getGroupFilteredSourceVillages();
        //build continents list
        List<String> continentList = new LinkedList<String>();
        for (Village v : villageList) {
            int iCont = v.getContinent();
            String cont = "K" + ((iCont < 10) ? "0" + iCont : iCont);
            if (!continentList.contains(cont)) {
                continentList.add(cont);
            }
        }
        Collections.sort(continentList, String.CASE_INSENSITIVE_ORDER);
        DefaultListModel contModel = new DefaultListModel();
        for (String cont : continentList) {
            contModel.addElement(cont);
        }
        //set continents list -> village list updates automatically via continent list listener
        jSourceContinentList.setModel(contModel);
        jSourceContinentList.getSelectionModel().setSelectionInterval(0, continentList.size() - 1);
    }

    /**Get source villages filtered by selected groups*/
    private List<Village> getGroupFilteredSourceVillages() {
        Object[] values = jVillageGroupList.getSelectedValues();
        List<Village> villageList = new LinkedList<Village>();
        if (jVillageGroupList.isEnabled()) {
            List<Tag> tags = new LinkedList<Tag>();
            boolean useNoTag = false;
            for (Object o : values) {
                try {
                    tags.add((Tag) o);
                } catch (Exception e) {
                    //no-tag villages contained
                    useNoTag = true;
                }
            }
            boolean onlyPlayerVillages = jPlayerSourcesOnlyBox.isSelected();
            Tribe current = GlobalOptions.getSelectedProfile().getTribe();
            if (jSourceGroupRelation.isSelected()) {
                //default OR relation
                //tags available, use them
                for (Tag t : tags) {
                    for (Integer vId : t.getVillageIDs()) {
                        Village v = DataHolder.getSingleton().getVillagesById().get(vId);
                        if (v != null && v.getTribe() != Barbarians.getSingleton() && !villageList.contains(v)) {
                            //add only if a players villages is tagged
                            if (!onlyPlayerVillages || (onlyPlayerVillages && v.getTribe().equals(current))) {
                                //use village if all villages are allowed or if owner is current player
                                villageList.add(v);
                            }
                        }
                    }
                }
                if (useNoTag) {
                    //use villages of current user which are not tagged
                    Village[] villages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
                    for (Village v : villages) {
                        List<Tag> vtags = TagManager.getSingleton().getTags(v);
                        if (vtags == null || vtags.isEmpty() && !villageList.contains(v)) {
                            if (!onlyPlayerVillages || (onlyPlayerVillages && v.getTribe().equals(current))) {
                                //use village if all villages are allowed or if owner is current player
                                villageList.add(v);
                            }
                        }
                    }
                }
            } else {
                //AND relation
                if (tags.isEmpty() && useNoTag) {
                    //only use non tagges villages
                    //-> if one tag is selected for AND relation, ignore non-tagged due to logical error
                    if (useNoTag) {
                        //use villages of current user which are not tagged
                        Village[] villages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
                        for (Village v : villages) {
                            List<Tag> vtags = TagManager.getSingleton().getTags(v);
                            if (vtags == null || vtags.isEmpty() && !villageList.contains(v)) {
                                if (!onlyPlayerVillages || (onlyPlayerVillages && v.getTribe().equals(current))) {
                                    //use village if all villages are allowed or if owner is current player
                                    villageList.add(v);
                                }
                            }
                        }
                    }
                } else {
                    //perform AND relation
                    List<Integer> villageIds = new LinkedList<Integer>();
                    //get all villages tagged by any tag
                    for (ManageableType e : TagManager.getSingleton().getAllElements()) {
                        Tag t = (Tag) e;
                        for (Integer i : t.getVillageIDs()) {
                            if (!villageIds.contains(i)) {
                                villageIds.add(i);
                            }
                        }
                    }
                    //check for all villages, if they are tagged by all selected tags
                    for (Integer i : villageIds) {
                        boolean use = true;
                        for (Tag t : tags) {
                            if (!t.tagsVillage(i)) {
                                //at least one tag does not tag current village
                                use = false;
                                break;
                            }
                        }
                        if (use) {
                            //all tags tag current village, so use them
                            Village v = DataHolder.getSingleton().getVillagesById().get(i);
                            if (!villageList.contains(v)) {
                                if (!onlyPlayerVillages || (onlyPlayerVillages && v.getTribe().equals(current))) {
                                    //use village if all villages are allowed or if owner is current player
                                    villageList.add(v);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            //no tags available, take current users villages
            UserProfile profile = GlobalOptions.getSelectedProfile();
            Village[] villages = profile.getTribe().getVillageList();
            villageList.addAll(Arrays.asList(villages));
        }
        return villageList;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Target selection handlers">

    /**Filter target lists by selected allies*/
    private void fireFilterTargetByAllyEvent() {
        Ally a = null;
        try {
            a = (Ally) jTargetAllyList.getSelectedValue();
        } catch (Exception e) {
        }
        if (a != null) {
            //ally selected
            Tribe[] tribes = a.getTribes().toArray(new Tribe[]{});
            if ((tribes != null) && (tribes.length != 0)) {
                Arrays.sort(tribes, Tribe.CASE_INSENSITIVE_ORDER);
                jTargetTribeList.setModel(new DefaultComboBoxModel(tribes));
                jTargetTribeList.setSelectedIndex(0);
            } else {
                //not tribes in ally -> should never happen
                jTargetTribeList.setModel(new DefaultListModel());
                jTargetContinentList.setModel(new DefaultListModel());
                jTargetVillageList.setModel(new DefaultListModel());
            }
        } else {
            //no ally selected, show no-ally tribes
            Enumeration<Integer> tribeIDs = DataHolder.getSingleton().getTribes().keys();
            List<Tribe> noAlly = new LinkedList<Tribe>();
            while (tribeIDs.hasMoreElements()) {
                Tribe t = DataHolder.getSingleton().getTribes().get(tribeIDs.nextElement());
                if (t.getAlly() == null && t.getVillageList() != null && t.getVillageList().length > 0) {
                    //only add tribes which are attackable
                    noAlly.add(t);
                }
            }
            Tribe[] noAllyTribes = noAlly.toArray(new Tribe[]{});
            Arrays.sort(noAllyTribes, Tribe.CASE_INSENSITIVE_ORDER);
            jTargetTribeList.setModel(new DefaultComboBoxModel(noAllyTribes));
            jTargetTribeList.setSelectedIndex(0);
        }
    }

    /**Filter target lists by selected tribes*/
    private void fireFilterTargetByTribeEvent() {
        try {
            Tribe t = (Tribe) jTargetTribeList.getSelectedValue();
            if (t != Barbarians.getSingleton() && t.getVillageList() != null && t.getVillageList().length > 0) {
                Village[] villages = t.getVillageList();
                List<String> continents = new LinkedList<String>();
                for (Village v : villages) {
                    int cont = 0;
                    if (ServerSettings.getSingleton().getCoordType() != 2) {
                        cont = DSCalculator.xyToHierarchical(v.getX(), v.getY())[0];
                    } else {
                        cont = DSCalculator.getContinent(v.getX(), v.getY());
                    }
                    String contString = "K" + cont;
                    if (!continents.contains(contString)) {
                        continents.add(contString);
                    }
                }
                Collections.sort(continents, String.CASE_INSENSITIVE_ORDER);
                DefaultListModel contModel = new DefaultListModel();
                for (String cont : continents) {
                    contModel.addElement(cont);
                }
                jTargetContinentList.setModel(contModel);
                jTargetContinentList.repaint();
                jTargetContinentList.getSelectionModel().setSelectionInterval(0, continents.size() - 1);
            } else {
                //no tribe selected -> should never happen!
                jTargetContinentList.setModel(new DefaultListModel());
                jTargetVillageList.setModel(new DefaultListModel());
            }
        } catch (Exception e) {
            jTargetContinentList.setModel(new DefaultListModel());
            jTargetVillageList.setModel(new DefaultListModel());
        }
    }

    /**Filter source list by selected continents*/
    private void fireFilterSourceContinentEvent() {
        int[] conts = jSourceContinentList.getSelectedIndices();
        if (conts == null) {
            return;
        }
        //build list of allowed continents
        List<Integer> allowedContinents = new LinkedList<Integer>();
        for (Integer cont : conts) {
            int contId = Integer.parseInt(((String) jSourceContinentList.getModel().getElementAt(cont)).replaceAll("K", ""));
            allowedContinents.add(contId);
        }
        List<Village> villageList = getGroupFilteredSourceVillages();
        try {
            List<Village> toRemove = new LinkedList<Village>();
            for (Village v : villageList) {
                int vCont = 0;
                if (ServerSettings.getSingleton().getCoordType() != 2) {
                    vCont = DSCalculator.xyToHierarchical(v.getX(), v.getY())[0];
                } else {
                    vCont = DSCalculator.getContinent(v.getX(), v.getY());
                }
                if (!allowedContinents.contains(vCont)) {
                    toRemove.add(v);
                }
            }
            //remove villages with wrong continent
            for (Village v : toRemove) {
                villageList.remove(v);
            }
            //build village list
            DefaultListModel villageModel = new DefaultListModel();
            for (Village v : villageList) {
                villageModel.addElement(v);
            }
            jSourceVillageList.setModel(villageModel);
            jSourceVillageList.repaint();
        } catch (Exception e) {
        }
    }

    /**Filter target lists by selected continents*/
    private void fireFilterTargetByContinentEvent() {
        //build list of selected/valid continents
        Object[] conts = jTargetContinentList.getSelectedValues();
        List<Integer> validConts = new LinkedList<Integer>();
        for (Object cont : conts) {
            String c = (String) cont;
            c = c.replaceAll("K", "").trim();
            validConts.add(Integer.parseInt(c));
        }
        Tribe t = (Tribe) jTargetTribeList.getSelectedValue();
        if (t == null) {
            return;
        }
        Village[] villages = t.getVillageList();
        Arrays.sort(villages, Village.CASE_INSENSITIVE_ORDER);
        DefaultListModel villageModel = new DefaultListModel();
        for (Village v : villages) {
            int cont = 0;
            if (ServerSettings.getSingleton().getCoordType() != 2) {
                cont = DSCalculator.xyToHierarchical(v.getX(), v.getY())[0];
            } else {
                cont = DSCalculator.getContinent(v.getX(), v.getY());
            }
            if (validConts.contains(cont)) {
                villageModel.addElement(v);
            }
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
        List<Attack> attackList = new LinkedList<Attack>();
        List<Village> targets = new LinkedList<Village>();
        logger.debug("Transferring calculated attacks and its targets to separate lists");
        List<Long> usedSendTimes = new LinkedList<Long>();
        int fullOffs = 0;
        for (AbstractTroopMovement movement : pParent.getResults()) {
            List<Attack> atts = null;
            atts = movement.getAttacks(pParent.getTimeFrame(), usedSendTimes);
            if (atts != null && atts.size() == movement.getMaxOffs()) {
                fullOffs++;
            }
            for (Attack attack : atts) {
                attackList.add(attack);
                if (!targets.contains(attack.getTarget())) {
                    targets.add(attack.getTarget());
                }
            }
        }
        logger.debug("Adding input targets to map");
        HashMap<Village, String> attackMappings = new HashMap<Village, String>();
        //get targets and attack count
        for (int i = 0; i < jVictimTable.getRowCount(); i++) {
            attackMappings.put((Village) jVictimTable.getValueAt(i, 1), "0/" + (Integer) jVictimTable.getValueAt(i, 3));
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
        List<Village> notAssigned = new LinkedList<Village>();
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
        DefaultListModel tagModel = new DefaultListModel();
        tagModel.addElement("Keinen Tag");
        for (ManageableType e : elements) {
            Tag t = (Tag) e;
            tagModel.addElement(t);
        }
        jVillageGroupList.setModel(tagModel);
        jVillageGroupList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                fireFilterSourceVillagesByGroupEvent();
            }
        });
        jSourceContinentList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                fireFilterSourceContinentEvent();
            }
        });
        if (elements.isEmpty()) {
            jVillageGroupList.setEnabled(false);
        } else {
            jVillageGroupList.setEnabled(true);
        }
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
        List<Village> villages = new LinkedList<Village>();
        if (dtde.getDropTargetContext().getComponent() == jSourcesTable || dtde.getDropTargetContext().getComponent() == jVictimTable) {
            if (dtde.isDataFlavorSupported(VillageTransferable.villageDataFlavor)) {
                //village dnd
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                try {
                    villages = (List<Village>) t.getTransferData(VillageTransferable.villageDataFlavor);
                } catch (Exception ex) {
                }
            } else if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                //string dnd
                try {
                    villages = PluginManager.getSingleton().executeVillageParser((String) t.getTransferData(DataFlavor.stringFlavor));
                } catch (Exception e) {
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
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                MouseGestures mMouseGestures = new MouseGestures();
                mMouseGestures.setMouseButton(MouseEvent.BUTTON3_MASK);
                mMouseGestures.addMouseGesturesListener(new MouseGestureHandler());
                mMouseGestures.start();
                try {
                    // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                    //JFrame.setDefaultLookAndFeelDecorated(true);

                    // SubstanceLookAndFeel.setSkin(SubstanceLookAndFeel.getAllSkins().get("Twilight").getClassName());
                    //  UIManager.put(SubstanceLookAndFeel.FOCUS_KIND, FocusKind.NONE);
                } catch (Exception e) {
                }
                Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));


                TribeTribeAttackFrame f = new TribeTribeAttackFrame();
                f.setup();
                f.setSize(600, 400);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup attackTypeGroup;
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel1;
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel2;
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel3;
    private javax.swing.JButton jAddToAttacksButton1;
    private javax.swing.JComboBox jAllTargetsComboBox;
    private javax.swing.JButton jApplyFiltersButton;
    private javax.swing.JDialog jAttackPlanSelectionDialog;
    private org.jdesktop.swingx.JXTable jAttackPlanTable;
    private javax.swing.JFrame jAttackResultDetailsFrame;
    private javax.swing.JProgressBar jAttacksBar;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jCalculateButton;
    private javax.swing.JButton jCancelSyncButton;
    private javax.swing.JButton jCloseResultsButton;
    private javax.swing.JButton jDoSyncButton;
    private javax.swing.JFrame jFilterFrame;
    private javax.swing.JList jFilterList;
    private javax.swing.JComboBox jFilterUnitBox;
    private javax.swing.JProgressBar jFullOffsBar;
    private javax.swing.JCheckBox jFullTargetsOnly;
    private javax.swing.JButton jHideAttackDetailsButton;
    private javax.swing.JButton jHideTargetDetailsButton;
    private javax.swing.JLabel jInfoLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
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
    private javax.swing.JTextField jMaxValue;
    private javax.swing.JTextField jMinValue;
    private javax.swing.JButton jNextSelectionButton;
    private javax.swing.JTable jNotAssignedSourcesTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JCheckBox jPlayerSourcesOnlyBox;
    private javax.swing.JButton jPrevSelectionButton;
    private javax.swing.JFrame jResultFrame;
    private org.jdesktop.swingx.JXTable jResultsTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JButton jSelectButton;
    private javax.swing.JButton jSelectionBeginButton;
    private javax.swing.JTextField jSelectionEnd;
    private javax.swing.JButton jSelectionEndButton;
    private javax.swing.JTextField jSelectionStart;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel jSettingsPanel;
    private javax.swing.JList jSourceContinentList;
    private javax.swing.JRadioButton jSourceGroupRelation;
    private javax.swing.JPanel jSourcePanel;
    private javax.swing.JList jSourceVillageList;
    private org.jdesktop.swingx.JXTable jSourcesTable;
    private javax.swing.JCheckBox jStrictFilter;
    private javax.swing.JList jTargetAllyList;
    private javax.swing.JList jTargetContinentList;
    private javax.swing.JTable jTargetDetailsTable;
    private javax.swing.JPanel jTargetPanel;
    private javax.swing.JFrame jTargetResultDetailsFrame;
    private javax.swing.JTextField jTargetTribeFilter;
    private javax.swing.JList jTargetTribeList;
    private javax.swing.JList jTargetVillageList;
    private javax.swing.JProgressBar jTargetsBar;
    private javax.swing.JComboBox jTroopsList;
    private org.jdesktop.swingx.JXTable jVictimTable;
    private javax.swing.JList jVillageGroupList;
    private com.jidesoft.swing.JideTabbedPane jideTabbedPane1;
    private org.jdesktop.swingx.JXPanel jxAttackPlanerPanel;
    private org.jdesktop.swingx.JXLabel jxResultInfoLabel;
    private org.jdesktop.swingx.JXLabel jxSourceInfoLabel;
    private org.jdesktop.swingx.JXLabel jxTargetInfoLabel;
    private org.jdesktop.swingx.JXCollapsiblePane resultInfoPanel;
    private org.jdesktop.swingx.JXCollapsiblePane sourceInfoPanel;
    private org.jdesktop.swingx.JXCollapsiblePane targetInfoPanel;
    // End of variables declaration//GEN-END:variables
}
