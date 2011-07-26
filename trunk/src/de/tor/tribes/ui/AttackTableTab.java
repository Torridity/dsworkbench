/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AttackTablePanel.java
 *
 * Created on Mar 27, 2011, 3:50:19 PM
 */
package de.tor.tribes.ui;

import de.tor.tribes.ui.views.DSWorkbenchReTimerFrame;
import de.tor.tribes.ui.views.DSWorkbenchAttackFrame;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.ui.editors.AttackTypeCellEditor;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.models.AttackTableModel;
import de.tor.tribes.ui.renderer.AttackMiscInfoRenderer;
import de.tor.tribes.ui.renderer.AttackTypeCellRenderer;
import de.tor.tribes.ui.renderer.ColoredDateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.util.AttackIGMSender;
import de.tor.tribes.util.AttackToPlainTextFormatter;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.bb.AttackListFormatter;
import de.tor.tribes.util.html.AttackPlanHTMLExporter;
import de.tor.tribes.util.js.AttackScriptWriter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.decorator.PatternPredicate;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.HorizontalAlignment;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.VerticalAlignment;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author Torridity
 */
public class AttackTableTab extends javax.swing.JPanel implements ListSelectionListener {

    private static Logger logger = Logger.getLogger("AttackTableTab");

    public static enum TRANSFER_TYPE {

        CLIPBOARD_PLAIN, CLIPBOARD_BB, FILE_HTML, FILE_GM, BROWSER_IGM, DSWB_RETIME, BROWSER_LINK, CUT_TO_INTERNAL_CLIPBOARD, COPY_TO_INTERNAL_CLIPBOARD, FROM_INTERNAL_CLIPBOARD
    }
    private String sAttackPlan = null;
    private final static JXTable jxAttackTable = new JXTable();
    private static AttackTableModel attackModel = null;
    private static boolean KEY_LISTENER_ADDED = false;
    private PainterHighlighter highlighter = null;
    private ActionListener actionListener = null;

    static {
        jxAttackTable.setRowHeight(24);
        HighlightPredicate.ColumnHighlightPredicate colu = new HighlightPredicate.ColumnHighlightPredicate(0, 1, 2, 3, 4, 5, 6, 9, 10, 11, 12);
        jxAttackTable.setHighlighters(new CompoundHighlighter(colu, HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B)));
        jxAttackTable.setColumnControlVisible(true);
        jxAttackTable.setDefaultEditor(UnitHolder.class, new UnitCellEditor());
        jxAttackTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jxAttackTable.setDefaultRenderer(Integer.class, new AttackTypeCellRenderer());
        jxAttackTable.setDefaultRenderer(Date.class, new ColoredDateCellRenderer());
        jxAttackTable.setDefaultEditor(Date.class, new DateSpinEditor());
        jxAttackTable.setDefaultEditor(Integer.class, new AttackTypeCellEditor());
        jxAttackTable.setDefaultRenderer(Attack.class, new AttackMiscInfoRenderer());
        attackModel = new AttackTableModel(AttackManager.DEFAULT_GROUP);
        jxAttackTable.setModel(attackModel);
        BufferedImage back = ImageUtils.createCompatibleBufferedImage(5, 5, BufferedImage.BITMASK);
        Graphics2D g = back.createGraphics();
        GeneralPath p = new GeneralPath();
        p.moveTo(0, 0);
        p.lineTo(5, 0);
        p.lineTo(5, 5);
        p.closePath();
        g.setColor(Color.GREEN.darker());
        g.fill(p);
        g.dispose();
        jxAttackTable.addHighlighter(new PainterHighlighter(HighlightPredicate.EDITABLE, new ImagePainter(back, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)));
    }

    /** Creates new form AttackTablePanel
     * @param pParent
     * @param pAttackPlan
     * @param pActionListener
     */
    public AttackTableTab(String pAttackPlan, final ActionListener pActionListener) {
        actionListener = pActionListener;
        sAttackPlan = pAttackPlan;
        initComponents();
        jScrollPane1.setViewportView(jxAttackTable);
        if (!KEY_LISTENER_ADDED) {
            KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
            KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
            KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
            KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false);
            KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
            jxAttackTable.registerKeyboardAction(pActionListener, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxAttackTable.registerKeyboardAction(pActionListener, "Cut", cut, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxAttackTable.registerKeyboardAction(pActionListener, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxAttackTable.registerKeyboardAction(pActionListener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxAttackTable.registerKeyboardAction(pActionListener, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxAttackTable.getActionMap().put("find", new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    pActionListener.actionPerformed(new ActionEvent(jxAttackTable, 0, "Find"));
                }
            });

            KEY_LISTENER_ADDED = true;
        }
        jxAttackTable.getSelectionModel().addListSelectionListener(AttackTableTab.this);
        jDateField.setDate(new Date());
        String prop = GlobalOptions.getProperty("attack.script.attacks.in.village.info");
        jShowAttacksInVillageInfo.setSelected((prop == null) ? true : Boolean.parseBoolean(prop));
        prop = GlobalOptions.getProperty("attack.script.attacks.on.confirm.page");
        jShowAttacksOnConfirmPage.setSelected((prop == null) ? true : Boolean.parseBoolean(prop));
        prop = GlobalOptions.getProperty("attack.script.attacks.in.place");
        jShowAttacksInPlace.setSelected((prop == null) ? true : Boolean.parseBoolean(prop));
        prop = GlobalOptions.getProperty("attack.script.attacks.in.overview");
        jShowAttacksInOverview.setSelected((prop == null) ? true : Boolean.parseBoolean(prop));
        jTimeChangeDialog.pack();
        jSendAttacksIGMDialog.pack();
        jChangeAttackTypeDialog.pack();
        jScriptExportDialog.pack();
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(jTimeChangeDialog.getRootPane(), "pages.change_attack_times", GlobalOptions.getHelpBroker().getHelpSet());

        }
        // </editor-fold>
    }

    public void deregister() {
        jxAttackTable.getSelectionModel().removeListSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jxAttackTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Angriff gewählt" : " Angriffe gewählt"));
            }
        }
    }

    public void showSuccess(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.GREEN));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    public void showInfo(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(getBackground()));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    public void showError(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.RED));
        jXLabel1.setForeground(Color.WHITE);
        jXLabel1.setText(pMessage);
    }

    public String getAttackPlan() {
        return sAttackPlan;
    }

    public JXTable getAttackTable() {
        return jxAttackTable;
    }

    public void updateCountdown() {
        TableColumnExt col = jxAttackTable.getColumnExt("Verbleibend");
        if (col.isVisible()) {
            int startX = 0;
            for (int i = 0; i < jxAttackTable.getColumnCount(); i++) {
                if (jxAttackTable.getColumnExt(i).equals(col)) {
                    break;
                }
                startX += (jxAttackTable.getColumnExt(i).isVisible()) ? jxAttackTable.getColumnExt(i).getWidth() : 0;
            }
            jxAttackTable.repaint(startX, 0, startX + col.getWidth(), jxAttackTable.getHeight());
        }
    }

    public void updateTime() {
        TableColumnExt col = jxAttackTable.getColumnExt("Abschickzeit");
        if (col.isVisible()) {
            int startX = 0;
            for (int i = 0; i < jxAttackTable.getColumnCount(); i++) {
                if (jxAttackTable.getColumnExt(i).equals(col)) {
                    break;
                }
                startX += (jxAttackTable.getColumnExt(i).isVisible()) ? jxAttackTable.getColumnExt(i).getWidth() : 0;
            }
            jxAttackTable.repaint(startX, 0, startX + col.getWidth(), jxAttackTable.getHeight());
        }
    }

    public void updatePlan() {
        attackModel.setPlan(sAttackPlan);
        String[] cols = new String[]{"Einheit", "Typ", "Sonstiges"};
        for (String col : cols) {
            TableColumnExt columns = jxAttackTable.getColumnExt(col);
            columns.setPreferredWidth(80);
            columns.setMaxWidth(80);
            columns.setWidth(80);
        }

        jScrollPane1.setViewportView(jxAttackTable);
        jxAttackTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
    }

    public void updateFilter(final String pValue, final List<String> columns, final boolean pCaseSensitive, final boolean pFilterRows) {
        if (highlighter != null) {
            jxAttackTable.removeHighlighter(highlighter);
        }
        if (!pFilterRows) {
            jxAttackTable.setRowFilter(null);
            final List<Integer> relevantCols = new LinkedList<Integer>();
            List<TableColumn> cols = jxAttackTable.getColumns(true);
            for (int i = 0; i < jxAttackTable.getColumnCount(); i++) {
                TableColumnExt col = jxAttackTable.getColumnExt(i);
                if (col.isVisible() && columns.contains(col.getTitle())) {
                    relevantCols.add(cols.indexOf(col));
                }
            }
            for (Integer col : relevantCols) {
                PatternPredicate patternPredicate0 = new PatternPredicate((pCaseSensitive ? "" : "(?i)") + Pattern.quote(pValue), col);
                MattePainter mp = new MattePainter(new Color(0, 0, 0, 120));
                highlighter = new PainterHighlighter(new HighlightPredicate.NotHighlightPredicate(patternPredicate0), mp);
                jxAttackTable.addHighlighter(highlighter);
            }
        } else {
            jxAttackTable.setRowFilter(new RowFilter<TableModel, Integer>() {

                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                    final List<Integer> relevantCols = new LinkedList<Integer>();
                    List<TableColumn> cols = jxAttackTable.getColumns(true);
                    for (int i = 0; i < jxAttackTable.getColumnCount(); i++) {
                        TableColumnExt col = jxAttackTable.getColumnExt(i);
                        if (col.isVisible() && columns.contains(col.getTitle())) {
                            relevantCols.add(cols.indexOf(col));
                        }
                    }

                    for (Integer col : relevantCols) {
                        if (pCaseSensitive) {
                            if (entry.getStringValue(col).indexOf(pValue) > -1) {
                                return true;
                            }
                        } else {
                            if (entry.getStringValue(col).toLowerCase().indexOf(pValue.toLowerCase()) > -1) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
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

        jScriptExportDialog = new javax.swing.JDialog();
        jShowAttacksInVillageInfo = new javax.swing.JCheckBox();
        jShowAttacksOnConfirmPage = new javax.swing.JCheckBox();
        jShowAttacksInPlace = new javax.swing.JCheckBox();
        jShowAttacksInOverview = new javax.swing.JCheckBox();
        jDoScriptExportButton = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jSendAttacksIGMDialog = new javax.swing.JDialog();
        jLabel20 = new javax.swing.JLabel();
        jSubject = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jAPIKey = new javax.swing.JTextField();
        jSendButton = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jTimeChangeDialog = new javax.swing.JDialog();
        jOKButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jDayField = new javax.swing.JSpinner();
        jMinuteField = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jHourField = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        jSecondsField = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jDateField = new de.tor.tribes.ui.components.DateTimeField();
        jModifyArrivalOption = new javax.swing.JRadioButton();
        jMoveTimeOption = new javax.swing.JRadioButton();
        jRandomizeOption = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jRandomField = new javax.swing.JFormattedTextField();
        jLabel19 = new javax.swing.JLabel();
        jNotRandomToNightBonus = new javax.swing.JCheckBox();
        jModifySendOption = new javax.swing.JRadioButton();
        jChangeAttackTypeDialog = new javax.swing.JDialog();
        jPanel6 = new javax.swing.JPanel();
        jNoType = new javax.swing.JRadioButton();
        jLabel23 = new javax.swing.JLabel();
        jAttackType = new javax.swing.JRadioButton();
        jLabel28 = new javax.swing.JLabel();
        jEnobleType = new javax.swing.JRadioButton();
        jLabel24 = new javax.swing.JLabel();
        jDefType = new javax.swing.JRadioButton();
        jLabel25 = new javax.swing.JLabel();
        jFakeType = new javax.swing.JRadioButton();
        jLabel26 = new javax.swing.JLabel();
        jFakeDefType = new javax.swing.JRadioButton();
        jLabel27 = new javax.swing.JLabel();
        jAdeptUnitPanel = new javax.swing.JPanel();
        jUnitBox = new javax.swing.JComboBox();
        jAcceptChangeUnitTypeButton = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jAdeptTypeBox = new javax.swing.JCheckBox();
        jAdeptUnitBox = new javax.swing.JCheckBox();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jXTable1 = new org.jdesktop.swingx.JXTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();

        jScriptExportDialog.setTitle("Scripteinstellungen");

        jShowAttacksInVillageInfo.setSelected(true);
        jShowAttacksInVillageInfo.setText("Angriffe in den Dorfinformationen anzeigen");

        jShowAttacksOnConfirmPage.setSelected(true);
        jShowAttacksOnConfirmPage.setText("Angriffe auf der Angriffsbestätigungsseite anzeigen");

        jShowAttacksInPlace.setSelected(true);
        jShowAttacksInPlace.setText("Angriffe im Versammlungsplatz anzeigen");

        jShowAttacksInOverview.setSelected(true);
        jShowAttacksInOverview.setText("Angriffe in den Übersichten anzeigen");

        jDoScriptExportButton.setText("Script erstellen");
        jDoScriptExportButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireDoExportAsScriptEvent(evt);
            }
        });

        jButton14.setText("Abbrechen");
        jButton14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireDoExportAsScriptEvent(evt);
            }
        });

        javax.swing.GroupLayout jScriptExportDialogLayout = new javax.swing.GroupLayout(jScriptExportDialog.getContentPane());
        jScriptExportDialog.getContentPane().setLayout(jScriptExportDialogLayout);
        jScriptExportDialogLayout.setHorizontalGroup(
            jScriptExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jScriptExportDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jScriptExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jShowAttacksInPlace, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                    .addComponent(jShowAttacksInOverview, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                    .addGroup(jScriptExportDialogLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 133, Short.MAX_VALUE)
                        .addComponent(jButton14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDoScriptExportButton))
                    .addComponent(jShowAttacksOnConfirmPage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                    .addComponent(jShowAttacksInVillageInfo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))
                .addContainerGap())
        );
        jScriptExportDialogLayout.setVerticalGroup(
            jScriptExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jScriptExportDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jShowAttacksInVillageInfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jShowAttacksOnConfirmPage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jShowAttacksInPlace)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jShowAttacksInOverview)
                .addGap(18, 18, 18)
                .addGroup(jScriptExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDoScriptExportButton)
                    .addComponent(jButton14))
                .addContainerGap())
        );

        jLabel20.setText("Betreff");

        jLabel22.setText("API-Key");

        jSendButton.setText("IGMs senden");
        jSendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireSendIGMsEvent(evt);
            }
        });

        jButton13.setText("Abbrechen");
        jButton13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireSendIGMsEvent(evt);
            }
        });

        javax.swing.GroupLayout jSendAttacksIGMDialogLayout = new javax.swing.GroupLayout(jSendAttacksIGMDialog.getContentPane());
        jSendAttacksIGMDialog.getContentPane().setLayout(jSendAttacksIGMDialogLayout);
        jSendAttacksIGMDialogLayout.setHorizontalGroup(
            jSendAttacksIGMDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSendAttacksIGMDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSendAttacksIGMDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jSendAttacksIGMDialogLayout.createSequentialGroup()
                        .addGroup(jSendAttacksIGMDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel20)
                            .addComponent(jLabel22))
                        .addGap(18, 18, 18)
                        .addGroup(jSendAttacksIGMDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jAPIKey, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                            .addComponent(jSubject, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSendAttacksIGMDialogLayout.createSequentialGroup()
                        .addComponent(jButton13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSendButton)))
                .addContainerGap())
        );
        jSendAttacksIGMDialogLayout.setVerticalGroup(
            jSendAttacksIGMDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSendAttacksIGMDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSendAttacksIGMDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(jSubject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jSendAttacksIGMDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jAPIKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jSendAttacksIGMDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSendButton)
                    .addComponent(jButton13))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTimeChangeDialog.setTitle("Zeiten ändern");

        jOKButton.setText("OK");
        jOKButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCloseTimeChangeDialogEvent(evt);
            }
        });

        jCancelButton.setText("Abbrechen");
        jCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCloseTimeChangeDialogEvent(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setText("Tage");
        jLabel7.setMaximumSize(new java.awt.Dimension(24, 25));
        jLabel7.setMinimumSize(new java.awt.Dimension(24, 25));
        jLabel7.setPreferredSize(new java.awt.Dimension(24, 25));

        jLabel5.setText("Minuten");
        jLabel5.setMaximumSize(new java.awt.Dimension(38, 25));
        jLabel5.setMinimumSize(new java.awt.Dimension(38, 25));
        jLabel5.setPreferredSize(new java.awt.Dimension(38, 25));

        jDayField.setMinimumSize(new java.awt.Dimension(60, 25));
        jDayField.setPreferredSize(new java.awt.Dimension(60, 25));

        jMinuteField.setMinimumSize(new java.awt.Dimension(60, 25));
        jMinuteField.setPreferredSize(new java.awt.Dimension(60, 25));

        jLabel6.setText("Stunden");
        jLabel6.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel6.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel6.setPreferredSize(new java.awt.Dimension(40, 25));

        jHourField.setMinimumSize(new java.awt.Dimension(60, 25));
        jHourField.setPreferredSize(new java.awt.Dimension(60, 25));

        jLabel14.setText("Sekunden");
        jLabel14.setMaximumSize(new java.awt.Dimension(47, 25));
        jLabel14.setMinimumSize(new java.awt.Dimension(47, 25));
        jLabel14.setPreferredSize(new java.awt.Dimension(47, 25));

        jSecondsField.setMinimumSize(new java.awt.Dimension(60, 20));
        jSecondsField.setPreferredSize(new java.awt.Dimension(60, 25));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jMinuteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSecondsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jHourField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jDayField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jHourField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSecondsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDayField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMinuteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setText("Neue Ankunftzeit");
        jLabel8.setToolTipText("");
        jLabel8.setEnabled(false);

        jDateField.setEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jDateField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        buttonGroup2.add(jModifyArrivalOption);
        jModifyArrivalOption.setText("Ankunftzeit angleichen");
        jModifyArrivalOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireModifyTimeEvent(evt);
            }
        });

        buttonGroup2.add(jMoveTimeOption);
        jMoveTimeOption.setSelected(true);
        jMoveTimeOption.setText("Verschieben");
        jMoveTimeOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireModifyTimeEvent(evt);
            }
        });

        buttonGroup2.add(jRandomizeOption);
        jRandomizeOption.setText("Zufällig verschieben");
        jRandomizeOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireModifyTimeEvent(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel17.setText("Zeitfenster");
        jLabel17.setEnabled(false);
        jLabel17.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel17.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel17.setPreferredSize(new java.awt.Dimension(80, 25));

        jLabel18.setText("+/-");
        jLabel18.setEnabled(false);
        jLabel18.setMaximumSize(new java.awt.Dimension(16, 25));
        jLabel18.setMinimumSize(new java.awt.Dimension(16, 25));
        jLabel18.setPreferredSize(new java.awt.Dimension(16, 25));

        jRandomField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jRandomField.setToolTipText("<html>Zeitfenster in Stunden<br/>Wird hier 2 eingegeben, so werden alle Angriffe um einen zufälligen Wert<br/>\n in einem Bereich von -2 bis +2 Stunden,<br/> ausgehend von ihrer aktuellen Zeit, verschoben.</html>");
        jRandomField.setEnabled(false);
        jRandomField.setMinimumSize(new java.awt.Dimension(6, 25));
        jRandomField.setPreferredSize(new java.awt.Dimension(6, 25));

        jLabel19.setEnabled(false);

        jNotRandomToNightBonus.setSelected(true);
        jNotRandomToNightBonus.setText("Nicht in Nachtbonus verschieben");
        jNotRandomToNightBonus.setToolTipText("DS Workbench sorgt dafür, dass Angriffe nicht im Nachtbonus landen");
        jNotRandomToNightBonus.setEnabled(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jNotRandomToNightBonus, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRandomField, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel19)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRandomField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jNotRandomToNightBonus)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        buttonGroup2.add(jModifySendOption);
        jModifySendOption.setText("Abschickzeit angleichen");
        jModifySendOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireModifyTimeEvent(evt);
            }
        });

        javax.swing.GroupLayout jTimeChangeDialogLayout = new javax.swing.GroupLayout(jTimeChangeDialog.getContentPane());
        jTimeChangeDialog.getContentPane().setLayout(jTimeChangeDialogLayout);
        jTimeChangeDialogLayout.setHorizontalGroup(
            jTimeChangeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTimeChangeDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTimeChangeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRandomizeOption, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jTimeChangeDialogLayout.createSequentialGroup()
                        .addComponent(jModifyArrivalOption)
                        .addGap(18, 18, 18)
                        .addComponent(jModifySendOption))
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jMoveTimeOption, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jTimeChangeDialogLayout.createSequentialGroup()
                        .addComponent(jCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOKButton))
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jTimeChangeDialogLayout.setVerticalGroup(
            jTimeChangeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTimeChangeDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jMoveTimeOption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jTimeChangeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jModifyArrivalOption)
                    .addComponent(jModifySendOption))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jRandomizeOption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jTimeChangeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jOKButton)
                    .addComponent(jCancelButton))
                .addContainerGap())
        );

        jChangeAttackTypeDialog.setTitle("Angriffstyp anpassen");

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setLayout(new java.awt.GridLayout(6, 2));

        buttonGroup1.add(jNoType);
        jNoType.setSelected(true);
        jNoType.setText("Keiner");
        jPanel6.add(jNoType);
        jPanel6.add(jLabel23);

        buttonGroup1.add(jAttackType);
        jPanel6.add(jAttackType);

        jLabel28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/axe.png"))); // NOI18N
        jPanel6.add(jLabel28);

        buttonGroup1.add(jEnobleType);
        jPanel6.add(jEnobleType);

        jLabel24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/snob.png"))); // NOI18N
        jPanel6.add(jLabel24);

        buttonGroup1.add(jDefType);
        jPanel6.add(jDefType);

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ally.png"))); // NOI18N
        jPanel6.add(jLabel25);

        buttonGroup1.add(jFakeType);
        jPanel6.add(jFakeType);

        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/fake.png"))); // NOI18N
        jPanel6.add(jLabel26);

        buttonGroup1.add(jFakeDefType);
        jPanel6.add(jFakeDefType);

        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/def_fake.png"))); // NOI18N
        jPanel6.add(jLabel27);

        jAdeptUnitPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jUnitBox.setEnabled(false);

        javax.swing.GroupLayout jAdeptUnitPanelLayout = new javax.swing.GroupLayout(jAdeptUnitPanel);
        jAdeptUnitPanel.setLayout(jAdeptUnitPanelLayout);
        jAdeptUnitPanelLayout.setHorizontalGroup(
            jAdeptUnitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAdeptUnitPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jUnitBox, 0, 87, Short.MAX_VALUE)
                .addContainerGap())
        );
        jAdeptUnitPanelLayout.setVerticalGroup(
            jAdeptUnitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAdeptUnitPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jUnitBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(107, Short.MAX_VALUE))
        );

        jAcceptChangeUnitTypeButton.setText("Übernehmen");
        jAcceptChangeUnitTypeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeUnitTypeEvent(evt);
            }
        });

        jButton15.setText("Abbrechen");
        jButton15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeUnitTypeEvent(evt);
            }
        });

        jAdeptTypeBox.setSelected(true);
        jAdeptTypeBox.setText("Typ angleichen");
        jAdeptTypeBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jAdeptTypeBoxfireEnableDisableAdeptTypeEvent(evt);
            }
        });

        jAdeptUnitBox.setText("Einheit angleichen");
        jAdeptUnitBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jAdeptUnitBoxfireEnableDisableChangeUnitEvent(evt);
            }
        });

        javax.swing.GroupLayout jChangeAttackTypeDialogLayout = new javax.swing.GroupLayout(jChangeAttackTypeDialog.getContentPane());
        jChangeAttackTypeDialog.getContentPane().setLayout(jChangeAttackTypeDialogLayout);
        jChangeAttackTypeDialogLayout.setHorizontalGroup(
            jChangeAttackTypeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jChangeAttackTypeDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jChangeAttackTypeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAdeptTypeBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jChangeAttackTypeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAdeptUnitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAdeptUnitBox))
                .addGap(11, 11, 11))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jChangeAttackTypeDialogLayout.createSequentialGroup()
                .addContainerGap(70, Short.MAX_VALUE)
                .addComponent(jButton15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jAcceptChangeUnitTypeButton)
                .addContainerGap())
        );
        jChangeAttackTypeDialogLayout.setVerticalGroup(
            jChangeAttackTypeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jChangeAttackTypeDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jChangeAttackTypeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAdeptTypeBox)
                    .addComponent(jAdeptUnitBox))
                .addGap(10, 10, 10)
                .addGroup(jChangeAttackTypeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAdeptUnitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jChangeAttackTypeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAcceptChangeUnitTypeButton)
                    .addComponent(jButton15))
                .addContainerGap())
        );

        jXTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jXTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 724, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                .addContainerGap())
        );

        setBackground(new java.awt.Color(255, 255, 255));
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setForeground(new java.awt.Color(240, 240, 240));
        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXLabel1.setText("Keine Meldung");
        jXLabel1.setOpaque(true);
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        add(infoPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void fireDoExportAsScriptEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDoExportAsScriptEvent
        if (evt.getSource() == jDoScriptExportButton) {
            actionListener.actionPerformed(new ActionEvent(this, 0, "ExportScript"));
        }
        jScriptExportDialog.setVisible(false);
    }//GEN-LAST:event_fireDoExportAsScriptEvent

    private void fireSendIGMsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSendIGMsEvent
        if (evt.getSource() == jSendButton) {
            actionListener.actionPerformed(new ActionEvent(this, 0, "SendIGM"));
        }
        jSendAttacksIGMDialog.setVisible(false);
}//GEN-LAST:event_fireSendIGMsEvent

    private void fireCloseTimeChangeDialogEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseTimeChangeDialogEvent
        if (evt.getSource() == jOKButton) {
            actionListener.actionPerformed(new ActionEvent(this, 0, "TimeChange"));
        }

        jTimeChangeDialog.setVisible(false);
}//GEN-LAST:event_fireCloseTimeChangeDialogEvent

    private void fireModifyTimeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireModifyTimeEvent
        boolean moveMode = false;
        boolean arriveMode = false;
        boolean randomMode = false;
        if (evt.getSource() == jMoveTimeOption) {
            moveMode = true;
        } else if (evt.getSource() == jModifyArrivalOption || evt.getSource() == jModifySendOption) {
            arriveMode = true;
        } else if (evt.getSource() == jRandomizeOption) {
            randomMode = true;
        }
        jLabel5.setEnabled(moveMode);
        jLabel6.setEnabled(moveMode);
        jLabel7.setEnabled(moveMode);
        jSecondsField.setEnabled(moveMode);
        jMinuteField.setEnabled(moveMode);
        jHourField.setEnabled(moveMode);
        jDayField.setEnabled(moveMode);
        //set arrive options
        jLabel8.setEnabled(arriveMode);
        jDateField.setEnabled(arriveMode);
        //random options
        jLabel17.setEnabled(randomMode);
        jLabel18.setEnabled(randomMode);
        jLabel19.setEnabled(randomMode);
        jRandomField.setEnabled(randomMode);
        jNotRandomToNightBonus.setEnabled(randomMode);
}//GEN-LAST:event_fireModifyTimeEvent

    private void fireChangeUnitTypeEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeUnitTypeEvent
        if (evt.getSource() == jAcceptChangeUnitTypeButton) {
            actionListener.actionPerformed(new ActionEvent(this, 0, "UnitChange"));
        }
        jChangeAttackTypeDialog.setVisible(false);
}//GEN-LAST:event_fireChangeUnitTypeEvent

    private void jAdeptTypeBoxfireEnableDisableAdeptTypeEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jAdeptTypeBoxfireEnableDisableAdeptTypeEvent
        jNoType.setEnabled(jAdeptTypeBox.isSelected());
        jAttackType.setEnabled(jAdeptTypeBox.isSelected());
        jEnobleType.setEnabled(jAdeptTypeBox.isSelected());
        jDefType.setEnabled(jAdeptTypeBox.isSelected());
        jFakeType.setEnabled(jAdeptTypeBox.isSelected());
        jFakeDefType.setEnabled(jAdeptTypeBox.isSelected());
}//GEN-LAST:event_jAdeptTypeBoxfireEnableDisableAdeptTypeEvent

    private void jAdeptUnitBoxfireEnableDisableChangeUnitEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jAdeptUnitBoxfireEnableDisableChangeUnitEvent
        jUnitBox.setEnabled(jAdeptUnitBox.isSelected());
}//GEN-LAST:event_jAdeptUnitBoxfireEnableDisableChangeUnitEvent

    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        infoPanel.setCollapsed(true);
    }//GEN-LAST:event_fireHideInfoEvent
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private static javax.swing.JTextField jAPIKey;
    private static javax.swing.JButton jAcceptChangeUnitTypeButton;
    private static javax.swing.JCheckBox jAdeptTypeBox;
    private static javax.swing.JCheckBox jAdeptUnitBox;
    private javax.swing.JPanel jAdeptUnitPanel;
    private static javax.swing.JRadioButton jAttackType;
    private static javax.swing.JButton jButton13;
    private static javax.swing.JButton jButton14;
    private static javax.swing.JButton jButton15;
    private static javax.swing.JButton jCancelButton;
    private static javax.swing.JDialog jChangeAttackTypeDialog;
    private static de.tor.tribes.ui.components.DateTimeField jDateField;
    private static javax.swing.JSpinner jDayField;
    private static javax.swing.JRadioButton jDefType;
    private static javax.swing.JButton jDoScriptExportButton;
    private static javax.swing.JRadioButton jEnobleType;
    private static javax.swing.JRadioButton jFakeDefType;
    private static javax.swing.JRadioButton jFakeType;
    private static javax.swing.JSpinner jHourField;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private static javax.swing.JSpinner jMinuteField;
    private static javax.swing.JRadioButton jModifyArrivalOption;
    private static javax.swing.JRadioButton jModifySendOption;
    private static javax.swing.JRadioButton jMoveTimeOption;
    private static javax.swing.JRadioButton jNoType;
    private static javax.swing.JCheckBox jNotRandomToNightBonus;
    private static javax.swing.JButton jOKButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private static javax.swing.JFormattedTextField jRandomField;
    private static javax.swing.JRadioButton jRandomizeOption;
    private static javax.swing.JDialog jScriptExportDialog;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private static javax.swing.JSpinner jSecondsField;
    private static javax.swing.JDialog jSendAttacksIGMDialog;
    private static javax.swing.JButton jSendButton;
    private static javax.swing.JCheckBox jShowAttacksInOverview;
    private static javax.swing.JCheckBox jShowAttacksInPlace;
    private static javax.swing.JCheckBox jShowAttacksInVillageInfo;
    private static javax.swing.JCheckBox jShowAttacksOnConfirmPage;
    private static javax.swing.JTextField jSubject;
    private static javax.swing.JDialog jTimeChangeDialog;
    private static javax.swing.JComboBox jUnitBox;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXTable jXTable1;
    // End of variables declaration//GEN-END:variables

    public void fireChangeTimeEvent() {
        List<Attack> attacksToModify = getSelectedAttacks();
        if (jMoveTimeOption.isSelected()) {
            Integer sec = (Integer) jSecondsField.getValue();
            Integer min = (Integer) jMinuteField.getValue();
            Integer hour = (Integer) jHourField.getValue();
            Integer day = (Integer) jDayField.getValue();

            for (Attack attack : attacksToModify) {
                long arrive = attack.getArriveTime().getTime();
                long diff = sec * 1000 + min * 60000 + hour * 3600000 + day * 86400000;
                //later if first index is selected
                //if later, add diff to arrival, else remove diff from arrival
                arrive += diff;
                attack.setArriveTime(new Date(arrive));
            }

        } else if (jModifyArrivalOption.isSelected()) {
            Date arrive = jDateField.getSelectedDate();
            for (Attack attack : attacksToModify) {
                attack.setArriveTime(arrive);
            }

        } else if (jModifySendOption.isSelected()) {
            Date arrive = jDateField.getSelectedDate();
            for (Attack attack : attacksToModify) {
                attack.setSendTime(arrive);
            }

        } else if (jRandomizeOption.isSelected()) {
            long rand = (Long) jRandomField.getValue() * 60 * 60 * 1000;
            for (Attack attack : attacksToModify) {
                Calendar c = Calendar.getInstance();
                boolean valid = false;
                while (!valid) {
                    //random until valid value was found
                    long arrive = attack.getArriveTime().getTime();
                    //later if first index is selected
                    //if later, add diff to arrival, else remove diff from arrival
                    int sign = (Math.random() > .5) ? 1 : -1;
                    arrive = (long) (arrive + (sign * Math.random() * rand));

                    c.setTimeInMillis(arrive);
                    int hours = c.get(Calendar.HOUR_OF_DAY);
                    if (hours >= 0 && hours < 8 && jNotRandomToNightBonus.isSelected()) {
                        //only invalid if in night bonus and this is not allowed
                        valid = false;
                    } else {
                        valid = true;
                    }
                }
                attack.setArriveTime(c.getTime());
            }
        }
        attackModel.fireTableDataChanged();
    }

    public void fireChangeUnitEvent() {
        int newType = -2;
        if (jAdeptTypeBox.isSelected()) {
            if (jNoType.isSelected()) {
                newType = Attack.NO_TYPE;
            } else if (jAttackType.isSelected()) {
                newType = Attack.CLEAN_TYPE;
            } else if (jEnobleType.isSelected()) {
                newType = Attack.SNOB_TYPE;
            } else if (jDefType.isSelected()) {
                newType = Attack.SUPPORT_TYPE;
            } else if (jFakeType.isSelected()) {
                newType = Attack.FAKE_TYPE;
            } else if (jFakeDefType.isSelected()) {
                newType = Attack.FAKE_DEFF_TYPE;
            }
        }
        UnitHolder newUnit = null;
        if (jAdeptUnitBox.isSelected()) {
            newUnit = (UnitHolder) jUnitBox.getSelectedItem();
        }

        for (Attack attack : getSelectedAttacks()) {
            if (newType != -2) {
                attack.setType(newType);
            }
            if (newUnit != null) {
                attack.setUnit(newUnit);
            }
        }
        attackModel.fireTableDataChanged();
    }

    public void fireSendIGMEvent() {
        String subject = jSubject.getText();
        String apiKey = jAPIKey.getText().trim();
        AttackIGMSender.SenderResult result = AttackIGMSender.sendAttackNotifications(getSelectedAttacks(), subject, apiKey);
        switch (result.getCode()) {
            case AttackIGMSender.ID_ERROR_WHILE_SUBMITTING:
                // JOptionPaneHelper.showErrorBox(jSendAttacksIGMDialog, result.getMessage(), "Fehler");
                showError(result.getMessage());
                break;
            case AttackIGMSender.ID_TOO_MANY_IGMS_PER_TRIBE:
                //JOptionPaneHelper.showWarningBox(jSendAttacksIGMDialog, result.getMessage(), "Warnung");
                showError(result.getMessage());
                break;
            default:
                //JOptionPaneHelper.showInformationBox(jSendAttacksIGMDialog, result.getMessage(), "Information");
                showSuccess(result.getMessage());
        }
    }

    public void fireExportScriptEvent() {
        List<Attack> attacks = getSelectedAttacks();
        if (attacks.isEmpty()) {
            return;
        }

        if (AttackScriptWriter.writeAttackScript(attacks, false, 5, true, Color.GREEN, Color.RED, jShowAttacksInVillageInfo.isSelected(), jShowAttacksOnConfirmPage.isSelected(), jShowAttacksInPlace.isSelected(), jShowAttacksInOverview.isSelected())) {
            showSuccess("Script erfolgreich nach 'zz_attack_info.user.js' geschrieben.\nDenke bitte daran, das Script in deinem Browser einzufügen/zu aktualisieren!");
            if (System.getProperty("os.name").startsWith("Windows")) {
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Möchtest du das Speicherverzeichnis des Scripts nun im Explorer öffnen?", "Information", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                    try {
                        Runtime.getRuntime().exec("explorer.exe .\\");
                    } catch (Exception e) {
                        showError("Explorer konnte nicht geöffnet werden.");
                    }
                }
            }
        } else {
            showError("Fehler beim Schreiben des Scripts.");
        }
        //store properties
        GlobalOptions.addProperty("attack.script.draw.vectors", Boolean.toString(false));
        GlobalOptions.addProperty("attack.script.attacks.in.village.info", Boolean.toString(jShowAttacksInVillageInfo.isSelected()));
        GlobalOptions.addProperty("attack.script.attacks.on.confirm.page", Boolean.toString(jShowAttacksOnConfirmPage.isSelected()));
        GlobalOptions.addProperty("attack.script.attacks.in.place", Boolean.toString(jShowAttacksInPlace.isSelected()));
        GlobalOptions.addProperty("attack.script.attacks.in.overview", Boolean.toString(jShowAttacksInOverview.isSelected()));
    }

    public void cleanup() {
        List<ManageableType> elements = AttackManager.getSingleton().getAllElements(getAttackPlan());
        List<Attack> toRemove = new LinkedList<Attack>();
        for (ManageableType t : elements) {
            Attack a = (Attack) t;
            long sendTime = a.getArriveTime().getTime() - ((long) DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
            if (sendTime < System.currentTimeMillis()) {
                toRemove.add(a);
            }
        }
        if (toRemove.isEmpty()) {
            return;
        }
        String message = (toRemove.size() == 1) ? "1 Angriff entfernen?" : toRemove.size() + " Angriffe entfernen?";

        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Abgelaufene Angriffe entfernen", "Nein", "Ja") == JOptionPane.NO_OPTION) {
            return;
        }

        logger.debug("Cleaning up " + toRemove.size() + " attacks");

        AttackManager.getSingleton().removeElements(getAttackPlan(), toRemove);
        attackModel.fireTableDataChanged();
        showSuccess(toRemove.size() + " Angriff(e) entfernt");
    }

    public boolean deleteSelection(boolean pAsk) {
        List<Attack> selectedAttacks = getSelectedAttacks();

        if (pAsk) {
            String message = ((selectedAttacks.size() == 1) ? "Angriff " : (selectedAttacks.size() + " Angriffe ")) + "wirklich löschen?";
            if (selectedAttacks.isEmpty() || JOptionPaneHelper.showQuestionConfirmBox(this, message, "Angriffe löschen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        jxAttackTable.editingCanceled(new ChangeEvent(this));
        AttackManager.getSingleton().removeElements(getAttackPlan(), selectedAttacks);
        attackModel.fireTableDataChanged();
        showSuccess(selectedAttacks.size() + " Angriff(e) gelöscht");
        return true;
    }

    public void deleteSelection() {
        deleteSelection(true);
    }

    public void changeSelectionTime() {
        if (!getSelectedAttacks().isEmpty()) {
            jTimeChangeDialog.setVisible(true);
        } else {
            showInfo("Keine Angriffe gewählt");
        }
    }

    public void changeSelectionType() {
        if (!getSelectedAttacks().isEmpty()) {
            jChangeAttackTypeDialog.setLocationRelativeTo(this);
            jChangeAttackTypeDialog.pack();
            jChangeAttackTypeDialog.setVisible(true);
        } else {
            showInfo("Keine Angriffe gewählt");
        }
    }

    public void setSelectionUnsent() {
        if (!getSelectedAttacks().isEmpty()) {
            for (Attack a : getSelectedAttacks()) {
                a.setTransferredToBrowser(false);
            }
            attackModel.fireTableDataChanged();
        } else {
            showInfo("Keine Angriffe gewählt");
        }
    }

    public void changeSelectionDrawState() {
        if (!getSelectedAttacks().isEmpty()) {
            for (Attack a : getSelectedAttacks()) {
                a.setShowOnMap(!a.isShowOnMap());
            }
            attackModel.fireTableDataChanged();
        } else {
            showInfo("Keine Angriffe gewählt");
        }
    }

    public void transferToScript() {
        if (getSelectedAttacks().isEmpty()) {
            showInfo("Keine Angriffe gewählt");
            return;
        }
        jScriptExportDialog.pack();
        jScriptExportDialog.setLocationRelativeTo(this);
        jScriptExportDialog.setVisible(true);
    }

    public void transferSelection(TRANSFER_TYPE pType) {
        switch (pType) {
            case COPY_TO_INTERNAL_CLIPBOARD:
                copyToInternalClipboard();
                break;
            case CUT_TO_INTERNAL_CLIPBOARD:
                cutToInternalClipboard();
                break;
            case FROM_INTERNAL_CLIPBOARD:
                copyFromInternalClipboard();
                break;
            case CLIPBOARD_PLAIN:
                copyPlainToExternalClipboardEvent();
                break;
            case CLIPBOARD_BB:
                copyBBToExternalClipboardEvent();
                break;
            case BROWSER_LINK:
                sendAttacksToBrowser();
                break;
            case BROWSER_IGM:
                sendAttacksAsIGM();
                break;
            case FILE_HTML:
                copyHTMLToFileEvent();
                break;
            case DSWB_RETIME:
                sendAttackToRetimeFrame();
                break;
            case FILE_GM:
                transferToScript();
                break;
        }
    }

    private void copyPlainToExternalClipboardEvent() {
        try {
            List<Attack> attacks = getSelectedAttacks();
            if (attacks.isEmpty()) {
                showInfo("Keine Angriffe ausgewählt");
                return;
            }
            StringBuilder buffer = new StringBuilder();
            for (Attack a : getSelectedAttacks()) {
                buffer.append(AttackToPlainTextFormatter.formatAttack(a)).append("\n");
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(buffer.toString()), null);
            String result = "Daten in Zwischenablage kopiert.";
            //  JOptionPaneHelper.showInformationBox(this, result, "Information");
            showSuccess(result);
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            String result = "Fehler beim Kopieren in die Zwischenablage.";
            // JOptionPaneHelper.showErrorBox(this, result, "Fehler");
            showError(result);
        }
    }

    private void copyBBToExternalClipboardEvent() {
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

    private void copyHTMLToFileEvent() {
        List<Attack> toExport = getSelectedAttacks();

        if (toExport.isEmpty()) {
            showInfo("Keine Angriffe ausgewählt");
            return;
        }

        String dir = GlobalOptions.getProperty("screen.dir");
        if (dir == null) {
            dir = ".";
        }
        String selectedPlan = getAttackPlan();
        JFileChooser chooser = null;
        try {
            chooser = new JFileChooser(dir);
        } catch (Exception e) {
            JOptionPaneHelper.showErrorBox(this, "Konnte Dateiauswahldialog nicht öffnen.\nMöglicherweise verwendest du Windows Vista. Ist dies der Fall, beende DS Workbench, klicke mit der rechten Maustaste auf DSWorkbench.exe,\n" + "wähle 'Eigenschaften' und deaktiviere dort unter 'Kompatibilität' den Windows XP Kompatibilitätsmodus.", "Fehler");
            return;
        }

        chooser.setDialogTitle("Datei auswählen");

        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept(File f) {
                if ((f != null) && (f.isDirectory() || f.getName().endsWith(".html"))) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "*.html";
            }
        });
        chooser.setSelectedFile(new File(dir + "/" + selectedPlan + ".html"));
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            try {
                File f = chooser.getSelectedFile();
                String file = f.getCanonicalPath();
                if (!file.endsWith(".html")) {
                    file += ".html";
                }

                File target = new File(file);
                if (target.exists()) {
                    if (JOptionPaneHelper.showQuestionConfirmBox(this, "Bestehende Datei überschreiben?", "Überschreiben", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                        //do not overwrite
                        return;
                    }
                }


                AttackPlanHTMLExporter.doExport(target, selectedPlan, toExport);
                //store current directory
                GlobalOptions.addProperty("screen.dir", target.getParent());
                showSuccess("Angriffe erfolgreich gespeichert");
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Möchtest du die erstellte Datei jetzt im Browser betrachten?", "Information", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                    BrowserCommandSender.openPage(target.toURI().toURL().toString());
                }
            } catch (Exception e) {
                logger.error("Failed to write attacks to HTML", e);
                //JOptionPaneHelper.showErrorBox(this, "Fehler beim Speichern.", "Fehler");
                showError("Fehler beim Speichern der HTML Datei");
            }
        }
    }

    private void sendAttacksToBrowser() {
        List<Attack> attacks = getSelectedAttacks();
        if (attacks.isEmpty()) {
            showInfo("Keine Angriffe ausgewählt");
            return;
        }
        int sentAttacks = 0;
        UserProfile profile = DSWorkbenchAttackFrame.getSingleton().getQuickProfile();

        for (Attack a : attacks) {
            if (attacks.size() == 1 || DSWorkbenchAttackFrame.getSingleton().getClickAccountValue() > 0) {
                if (BrowserCommandSender.sendAttack(a, profile)) {
                    a.setTransferredToBrowser(true);
                    if (attacks.size() > 1) {
                        DSWorkbenchAttackFrame.getSingleton().decreaseClickAccountValue();
                    }
                    sentAttacks++;
                }
            }
        }
        String usedProfile = "";
        if (profile != null) {
            usedProfile = "als " + profile.toString();
        }
        showInfo(sentAttacks + " von " + attacks.size() + " Angriffe(n) " + usedProfile + " in den Browser übertragen");
    }

    private boolean copyToInternalClipboard() {
        List<Attack> selection = getSelectedAttacks();
        if (selection.isEmpty()) {
            showInfo("Keine Angriffe gewählt");
            return false;
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
            return true;
        } catch (HeadlessException hex) {
            showError("Fehler beim Kopieren der Angriffe");
            return false;
        }
    }

    private void cutToInternalClipboard() {
        int size = getSelectedAttacks().size();
        if (size == 0) {
            showInfo("Keine Angriffe gewählt");
            return;
        }
        if (copyToInternalClipboard() && deleteSelection(false)) {
            showSuccess(size + ((size == 1) ? " Angriff ausgeschnitten" : " Angriffe ausgeschnitten"));
        } else {
            showError("Fehler beim Ausschneiden der Angriffe");
        }
    }

    private void copyFromInternalClipboard() {
        try {
            String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);

            String[] lines = data.split("\n");
            int cnt = 0;
            for (String line : lines) {
                Attack a = Attack.fromInternalRepresentation(line);
                if (a != null) {
                    AttackManager.getSingleton().addManagedElement(getAttackPlan(), a);
                    cnt++;
                }
            }
            showSuccess(cnt + ((cnt == 1) ? " Angriff eingefügt" : " Angriffe eingefügt"));
        } catch (UnsupportedFlavorException ufe) {
            logger.error("Failed to copy attacks from internal clipboard", ufe);
            showError("Fehler beim Einfügen der Angriffe");
        } catch (IOException ioe) {
            logger.error("Failed to copy attacks from internal clipboard", ioe);
            showError("Fehler beim Einfügen der Angriffe");
        }
        attackModel.fireTableDataChanged();
    }

    private void sendAttacksAsIGM() {
        if (getSelectedAttacks().isEmpty()) {
            showInfo("Kein Angriffe gewählt");
            return;
        }

        jSubject.setText("Deine Angriffe (Plan: " + getAttackPlan() + ")");
        jSendAttacksIGMDialog.pack();
        jSendAttacksIGMDialog.setLocationRelativeTo(this);
        jSendAttacksIGMDialog.setVisible(true);
    }

    private void sendAttackToRetimeFrame() {
        if (getSelectedAttacks().isEmpty()) {
            showInfo("Kein Angriffe gewählt");
            return;
        }
        Attack attack = getSelectedAttacks().get(0);

        StringBuilder b = new StringBuilder();
        b.append("Herkunft: ").append(attack.getSource().toString()).append("\n");
        b.append("Ziel: ").append(attack.getTarget().toString()).append("\n");
        SimpleDateFormat f = null;
        if (ServerSettings.getSingleton().isMillisArrival()) {
            f = new SimpleDateFormat("dd.MM.yy HH:mm:ss:SSS");
        } else {
            f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        }
        b.append("Ankunft: ").append(f.format(attack.getArriveTime())).append("\n");
        DSWorkbenchReTimerFrame.getSingleton().setCustomAttack(b.toString());
        showSuccess("Angriff in Retimer übertragen");
        if (!DSWorkbenchReTimerFrame.getSingleton().isVisible()) {
            DSWorkbenchReTimerFrame.getSingleton().setVisible(true);
        }
    }

    private List<Attack> getSelectedAttacks() {
        final List<Attack> selectedAttacks = new LinkedList<Attack>();
        int[] selectedRows = jxAttackTable.getSelectedRows();
        if (selectedRows != null && selectedRows.length < 1) {
            return selectedAttacks;
        }
        for (Integer selectedRow : selectedRows) {
            Attack a = (Attack) AttackManager.getSingleton().getAllElements(getAttackPlan()).get(jxAttackTable.convertRowIndexToModel(selectedRow));
            if (a != null) {
                selectedAttacks.add(a);
            }
        }
        return selectedAttacks;
    }
}
