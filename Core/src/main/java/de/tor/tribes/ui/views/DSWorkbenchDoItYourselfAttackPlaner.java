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
package de.tor.tribes.ui.views;

import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.editors.NoteIconCellEditor;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.editors.VillageCellEditor;
import de.tor.tribes.ui.models.DoItYourselfAttackTableModel;
import de.tor.tribes.ui.renderer.*;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.MouseGestureHandler;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.PropertyHelper;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.bb.AttackListFormatter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.HorizontalAlignment;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.VerticalAlignment;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * @author Torridity
 */
public class DSWorkbenchDoItYourselfAttackPlaner extends AbstractDSWorkbenchFrame implements GenericManagerListener, ActionListener, ListSelectionListener {

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jAttackTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Angriff gewählt" : " Angriffe gewählt"));
            }
        }
    }

    public enum TRANSFER_TYPE {

        CUT_TO_INTERNAL_CLIPBOARD, COPY_TO_INTERNAL_CLIPBOARD, FROM_INTERNAL_CLIPBOARD, BB_TO_CLIPBOARD
    }

    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        ((DoItYourselfAttackTableModel) jAttackTable.getModel()).fireTableDataChanged();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() != null) {
            if (e.getActionCommand().equals("Copy")) {
                transferSelection(TRANSFER_TYPE.COPY_TO_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Cut")) {
                transferSelection(TRANSFER_TYPE.CUT_TO_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Paste")) {
                transferSelection(TRANSFER_TYPE.FROM_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Delete")) {
                deleteSelection(true);
            } else if (e.getActionCommand().equals("BBCopy")) {
                transferSelection(TRANSFER_TYPE.BB_TO_CLIPBOARD);
            }
        }
    }
    private static Logger logger = Logger.getLogger("DoItYourselflAttackPlaner");
    private static DSWorkbenchDoItYourselfAttackPlaner SINGLETON = null;

    /**
     * Creates new form DSWorkbenchDoItYourselflAttackPlaner
     */
    DSWorkbenchDoItYourselfAttackPlaner() {
        initComponents();

        jAttackTable.setModel(new DoItYourselfAttackTableModel());
        jAttackTable.getSelectionModel().addListSelectionListener(DSWorkbenchDoItYourselfAttackPlaner.this);

        jArriveTime.setDate(Calendar.getInstance().getTime());
        jNewArriveSpinner.setDate(Calendar.getInstance().getTime());
        capabilityInfoPanel1.addActionListener(this);
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        jAttackTable.registerKeyboardAction(DSWorkbenchDoItYourselfAttackPlaner.this, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jAttackTable.registerKeyboardAction(DSWorkbenchDoItYourselfAttackPlaner.this, "Cut", cut, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jAttackTable.registerKeyboardAction(DSWorkbenchDoItYourselfAttackPlaner.this, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jAttackTable.registerKeyboardAction(DSWorkbenchDoItYourselfAttackPlaner.this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jAttackTable.registerKeyboardAction(DSWorkbenchDoItYourselfAttackPlaner.this, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jAttackTable.getActionMap().put("find", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //no find
            }
        });

        DoItYourselfCountdownThread thread = new DoItYourselfCountdownThread();
        thread.start();

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.manual_attack_planer", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>
        pack();
    }

    @Override
    public void toBack() {
        jAlwaysOnTopBox.setSelected(false);
        fireAttackFrameOnTopEvent(null);
        super.toBack();
    }

    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTopBox.isSelected());
        PropertyHelper.storeTableProperties(jAttackTable, pConfig, getPropertyPrefix());

    }

    public void restoreCustomProperties(Configuration pConfig) {
        try {
            jAlwaysOnTopBox.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }

        setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        PropertyHelper.restoreTableProperties(jAttackTable, pConfig, getPropertyPrefix());
    }

    public String getPropertyPrefix() {
        return "manual.attack.planer.view";
    }

    public static synchronized DSWorkbenchDoItYourselfAttackPlaner getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchDoItYourselfAttackPlaner();
        }
        return SINGLETON;
    }

    @Override
    public void resetView() {
        AttackManager.getSingleton().addManagerListener(this);
        //setup renderer and general view
       // ((DoItYourselfAttackTableModel) jAttackTable.getModel()).clear();

        HighlightPredicate.ColumnHighlightPredicate colu = new HighlightPredicate.ColumnHighlightPredicate(0, 1, 2, 3, 6);
        jAttackTable.setRowHeight(24);
        jAttackTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jAttackTable.setHighlighters(new CompoundHighlighter(colu, HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B)));
        jAttackTable.setColumnControlVisible(true);
        jAttackTable.setDefaultEditor(UnitHolder.class, new UnitCellEditor());
        jAttackTable.setDefaultEditor(Village.class, new VillageCellEditor());
        jAttackTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jAttackTable.setDefaultRenderer(Integer.class, new NoteIconCellRenderer(NoteIconCellRenderer.ICON_TYPE.NOTE));
        jAttackTable.setDefaultRenderer(Date.class, new ColoredDateCellRenderer());
        jAttackTable.setDefaultRenderer(Long.class, new ColoredCoutdownCellRenderer());
        jAttackTable.setDefaultEditor(Date.class, new DateSpinEditor());
        jAttackTable.setDefaultEditor(Integer.class, new NoteIconCellEditor(NoteIconCellEditor.ICON_TYPE.NOTE));
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
        jAttackTable.addHighlighter(new PainterHighlighter(HighlightPredicate.EDITABLE, new ImagePainter(back, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)));

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        DefaultComboBoxModel model2 = new DefaultComboBoxModel();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            model.addElement(unit);
            model2.addElement(unit);
        }
        jUnitBox.setModel(model);
        jUnitComboBox.setModel(model2);
        jUnitBox.setSelectedItem(DataHolder.getSingleton().getUnitByPlainName("ram"));
        jUnitComboBox.setSelectedItem(DataHolder.getSingleton().getUnitByPlainName("ram"));
        jUnitBox.setRenderer(new UnitListCellRenderer());
        DefaultComboBoxModel typeModel = new DefaultComboBoxModel();
        jAttackTypeComboBox.setRenderer(new AttackTypeListCellRenderer());
        typeModel.addElement(Attack.NO_TYPE);
        typeModel.addElement(Attack.CLEAN_TYPE);
        typeModel.addElement(Attack.SNOB_TYPE);
        typeModel.addElement(Attack.FAKE_TYPE);
        typeModel.addElement(Attack.FAKE_DEFF_TYPE);
        typeModel.addElement(Attack.SUPPORT_TYPE);
        jAttackTypeComboBox.setModel(typeModel);

        jUnitComboBox.setRenderer(new UnitListCellRenderer());

        //@TODO implement XX:YY:ZZ if needed...currently no server has this system

        jSourceVillage.setValue(new Point(500, 500));
        jTargetVillage.setValue(new Point(500, 500));
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                jSourceVillage.updateUI();
                jTargetVillage.updateUI();
            }
        });

    }

    protected void updateCountdown() {
        TableColumnExt col = jAttackTable.getColumnExt("Verbleibend");
        if (col.isVisible()) {
            int startX = 0;
            for (int i = 0; i < jAttackTable.getColumnCount(); i++) {
                if (jAttackTable.getColumnExt(i).equals(col)) {
                    break;
                }
                startX += (jAttackTable.getColumnExt(i).isVisible()) ? jAttackTable.getColumnExt(i).getWidth() : 0;
            }

            jAttackTable.repaint(startX, 0, startX + col.getWidth(), jAttackTable.getHeight());
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

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jAlwaysOnTopBox = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jAdeptTimeButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jUnitComboBox = new javax.swing.JComboBox();
        jAdeptUnitButton = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jAttackTypeComboBox = new javax.swing.JComboBox();
        jAdeptTypeButton = new javax.swing.JButton();
        jNewArriveSpinner = new de.tor.tribes.ui.components.DateTimeField();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jUnitBox = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jArriveTime = new de.tor.tribes.ui.components.DateTimeField();
        jSourceVillage = new de.tor.tribes.ui.components.CoordinateSpinner();
        jTargetVillage = new de.tor.tribes.ui.components.CoordinateSpinner();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jAttackTable = new org.jdesktop.swingx.JXTable();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        setTitle("Manueller Angriffsplaner");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAttackFrameOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTopBox, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Anpassen"));
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(350, 152));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel7.setText("Ankunftszeit");
        jLabel7.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel7.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel7, gridBagConstraints);

        jAdeptTimeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jAdeptTimeButton.setToolTipText("Ankunftszeit für alle markierten Angriffe anpassen");
        jAdeptTimeButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jAdeptTimeButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jAdeptTimeButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jAdeptTimeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAdeptEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jAdeptTimeButton, gridBagConstraints);

        jLabel8.setText("Einheit");
        jLabel8.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel8.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel8.setPreferredSize(new java.awt.Dimension(80, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel8, gridBagConstraints);

        jUnitComboBox.setMinimumSize(new java.awt.Dimension(23, 25));
        jUnitComboBox.setPreferredSize(new java.awt.Dimension(28, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jUnitComboBox, gridBagConstraints);

        jAdeptUnitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jAdeptUnitButton.setToolTipText("Einheit für alle markierten Angriffe anpassen");
        jAdeptUnitButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jAdeptUnitButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jAdeptUnitButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jAdeptUnitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAdeptEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jAdeptUnitButton, gridBagConstraints);

        jLabel9.setText("Angriffstyp");
        jLabel9.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel9.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel9.setPreferredSize(new java.awt.Dimension(80, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel9, gridBagConstraints);

        jAttackTypeComboBox.setMinimumSize(new java.awt.Dimension(23, 25));
        jAttackTypeComboBox.setPreferredSize(new java.awt.Dimension(28, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jAttackTypeComboBox, gridBagConstraints);

        jAdeptTypeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jAdeptTypeButton.setToolTipText("Angriffstyp für alle markierten Angriffe anpassen");
        jAdeptTypeButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jAdeptTypeButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jAdeptTypeButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jAdeptTypeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAdeptEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jAdeptTypeButton, gridBagConstraints);

        jNewArriveSpinner.setMinimumSize(new java.awt.Dimension(64, 25));
        jNewArriveSpinner.setPreferredSize(new java.awt.Dimension(258, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jNewArriveSpinner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jPanel3, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Neuer Angriff"));
        jPanel2.setOpaque(false);
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Herkunft");
        jLabel1.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel1.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel1.setPreferredSize(new java.awt.Dimension(80, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel1, gridBagConstraints);

        jLabel2.setText("Ziel");
        jLabel2.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel2.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel2.setPreferredSize(new java.awt.Dimension(80, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Einheit");
        jLabel3.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel3.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel3.setPreferredSize(new java.awt.Dimension(80, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel3, gridBagConstraints);

        jLabel4.setText("Ankunft");
        jLabel4.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel4.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel4.setPreferredSize(new java.awt.Dimension(80, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel4, gridBagConstraints);

        jUnitBox.setToolTipText("Langsamste Einheit");
        jUnitBox.setMinimumSize(new java.awt.Dimension(23, 25));
        jUnitBox.setPreferredSize(new java.awt.Dimension(28, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jUnitBox, gridBagConstraints);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton1.setText("Hinzufügen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddAttackEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jButton1, gridBagConstraints);

        jArriveTime.setMinimumSize(new java.awt.Dimension(64, 25));
        jArriveTime.setPreferredSize(new java.awt.Dimension(258, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jArriveTime, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jSourceVillage, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jTargetVillage, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jPanel2, gridBagConstraints);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jAttackTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jAttackTable);

        jPanel5.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXLabel1.setText("Keine Meldung");
        jXLabel1.setOpaque(true);
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jXLabel1fireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        jPanel5.add(infoPanel, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jPanel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel4, gridBagConstraints);

        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireAttackFrameOnTopEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAttackFrameOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAttackFrameOnTopEvent

    private void fireAddAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAttackEvent
        Village source = jSourceVillage.getVillage();
        if (source == null) {
            showError("Kein gültiges Herkunftsdorf gewählt");
            return;
        }

        Village target = jTargetVillage.getVillage();
        if (target == null) {
            showError("Kein gültiges Zieldorf gewählt");
            return;
        }

        Date arrive = jArriveTime.getSelectedDate();
        if (arrive.getTime() < System.currentTimeMillis()) {
            showError("Ankunftszeit darf nicht in der Vergangenheit liegen");
            return;
        }
        UnitHolder unit = (UnitHolder) jUnitBox.getSelectedItem();
        int type = Attack.NO_TYPE;
        if (unit.equals(DataHolder.getSingleton().getUnitByPlainName("snob"))) {
            type = Attack.SNOB_TYPE;
        } else if (unit.equals(DataHolder.getSingleton().getUnitByPlainName("ram"))) {
            type = Attack.CLEAN_TYPE;
        }
        AttackManager.getSingleton().addDoItYourselfAttack(source, target, unit, arrive, type);
        showSuccess("Angriff hinzugefügt");
    }//GEN-LAST:event_fireAddAttackEvent

    private void fireAdeptEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAdeptEvent
        int[] rows = jAttackTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            showInfo("Keine Angriffe ausgewählt");
            return;
        }
        if (evt.getSource() == jAdeptTimeButton) {
            Date newArrive = jNewArriveSpinner.getSelectedDate();
            if (newArrive.getTime() < System.currentTimeMillis()) {
                showError("Ankunftszeit darf nicht in der Vergangenheit liegen");
                return;
            }
            for (int r = rows.length - 1; r >= 0; r--) {
                int row = jAttackTable.convertRowIndexToModel(rows[r]);
                Attack a = (Attack) AttackManager.getSingleton().getDoItYourselfAttacks().get(row);
                a.setArriveTime(newArrive);
            }

        } else if (evt.getSource() == jAdeptUnitButton) {
            UnitHolder newUnit = (UnitHolder) jUnitComboBox.getSelectedItem();
            if (newUnit == null) {
                showError("Keine Einheit ausgewählt");
                return;
            }

            for (int r = rows.length - 1; r >= 0; r--) {
                int row = jAttackTable.convertRowIndexToModel(rows[r]);
                Attack a = (Attack) AttackManager.getSingleton().getDoItYourselfAttacks().get(row);
                a.setUnit(newUnit);
            }
        } else if (evt.getSource() == jAdeptTypeButton) {
            Integer newType = (Integer) jAttackTypeComboBox.getSelectedItem();
            if (newType == null) {
                showError("Kein Angriffstyp ausgewählt");
                return;
            }

            for (int r = rows.length - 1; r >= 0; r--) {
                int row = jAttackTable.convertRowIndexToModel(rows[r]);
                Attack a = (Attack) AttackManager.getSingleton().getDoItYourselfAttacks().get(row);
                a.setType(newType);
            }
        }
        AttackManager.getSingleton().revalidate(true);
    }//GEN-LAST:event_fireAdeptEvent

    private void jXLabel1fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXLabel1fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXLabel1fireHideInfoEvent

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
            case BB_TO_CLIPBOARD:
                copyBBToExternalClipboardEvent();
                break;
        }
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    public boolean deleteSelection(boolean pAsk) {
        List<Attack> selectedAttacks = getSelectedAttacks();

        if (pAsk) {
            String message = ((selectedAttacks.size() == 1) ? "Angriff " : (selectedAttacks.size() + " Angriffe ")) + "wirklich löschen?";
            if (selectedAttacks.isEmpty() || JOptionPaneHelper.showQuestionConfirmBox(this, message, "Angriffe löschen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        jAttackTable.editingCanceled(new ChangeEvent(this));
        AttackManager.getSingleton().removeElements(AttackManager.MANUAL_ATTACK_PLAN, selectedAttacks);
        ((DoItYourselfAttackTableModel) jAttackTable.getModel()).fireTableDataChanged();
        showSuccess(selectedAttacks.size() + " Angriff(e) gelöscht");
        return true;
    }

    private boolean copyToInternalClipboard() {
        List<Attack> selection = getSelectedAttacks();
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
            AttackManager.getSingleton().invalidate();
            for (String line : lines) {
                Attack a = Attack.fromInternalRepresentation(line);
                if (a != null) {
                    AttackManager.getSingleton().addManagedElement(AttackManager.MANUAL_ATTACK_PLAN, a);
                    cnt++;
                }
            }
            showSuccess(cnt + ((cnt == 1) ? " Angriff eingefügt" : " Angriffe eingefügt"));
        } catch (UnsupportedFlavorException | IOException ufe) {
            logger.error("Failed to copy attacks from internal clipboard", ufe);
            showError("Fehler beim Einfügen der Angriffe");
        }
        ((DoItYourselfAttackTableModel) jAttackTable.getModel()).fireTableDataChanged();
        AttackManager.getSingleton().revalidate();
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

    private List<Attack> getSelectedAttacks() {
        final List<Attack> selectedAttacks = new LinkedList<>();
        int[] selectedRows = jAttackTable.getSelectedRows();
        if (selectedRows != null && selectedRows.length < 1) {
            return selectedAttacks;
        }
        for (Integer selectedRow : selectedRows) {
            Attack a = (Attack) AttackManager.getSingleton().getAllElements(AttackManager.MANUAL_ATTACK_PLAN).get(jAttackTable.convertRowIndexToModel(selectedRow));
            if (a != null) {
                selectedAttacks.add(a);
            }
        }
        return selectedAttacks;
    }

    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        MouseGestures mMouseGestures = new MouseGestures();
        mMouseGestures.setMouseButton(MouseEvent.BUTTON3_MASK);
        mMouseGestures.addMouseGesturesListener(new MouseGestureHandler());
        mMouseGestures.start();
        GlobalOptions.setSelectedServer("de43");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);

        DataHolder.getSingleton().loadData(false);
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }
        DSWorkbenchDoItYourselfAttackPlaner.getSingleton().resetView();
        //  DSWorkbenchAttackFrame.getSingleton().setSize(800, 600);
        DSWorkbenchAttackFrame.getSingleton().pack();
        AttackManager.getSingleton().invalidate();
        for (int i = 0; i < 100; i++) {
            AttackManager.getSingleton().addDoItYourselfAttack(DataHolder.getSingleton().getRandomVillage(), DataHolder.getSingleton().getRandomVillage(), DataHolder.getSingleton().getRandomUnit(), new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY), Attack.CLEAN_TYPE);
        }

        AttackManager.getSingleton().revalidate(AttackManager.MANUAL_ATTACK_PLAN, true);
        DSWorkbenchDoItYourselfAttackPlaner.getSingleton().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        DSWorkbenchDoItYourselfAttackPlaner.getSingleton().setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JButton jAdeptTimeButton;
    private javax.swing.JButton jAdeptTypeButton;
    private javax.swing.JButton jAdeptUnitButton;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private de.tor.tribes.ui.components.DateTimeField jArriveTime;
    private org.jdesktop.swingx.JXTable jAttackTable;
    private javax.swing.JComboBox jAttackTypeComboBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private de.tor.tribes.ui.components.DateTimeField jNewArriveSpinner;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private de.tor.tribes.ui.components.CoordinateSpinner jSourceVillage;
    private de.tor.tribes.ui.components.CoordinateSpinner jTargetVillage;
    private javax.swing.JComboBox jUnitBox;
    private javax.swing.JComboBox jUnitComboBox;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    // End of variables declaration//GEN-END:variables
}

class DoItYourselfCountdownThread extends Thread {

    public DoItYourselfCountdownThread() {
        setName("DoItYourselfCountdownUpdater");
        setPriority(MIN_PRIORITY);
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (DSWorkbenchDoItYourselfAttackPlaner.getSingleton().isVisible()) {
                    DSWorkbenchDoItYourselfAttackPlaner.getSingleton().updateCountdown();
                    sleep(100);
                } else {
                    sleep(1000);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
