/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchDoItYourselflAttackPlaner.java
 *
 * Created on Nov 25, 2009, 10:27:45 PM
 */
package de.tor.tribes.ui.views;

import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.editors.AttackTypeCellEditor;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.models.DoItYourselfAttackTableModel;
import de.tor.tribes.ui.renderer.AttackMiscInfoRenderer;
import de.tor.tribes.ui.renderer.AttackTypeCellRenderer;
import de.tor.tribes.ui.renderer.AttackTypeListCellRenderer;
import de.tor.tribes.ui.renderer.ColoredDateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.ui.renderer.UnitListCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.MouseGestureHandler;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.PropertyHelper;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.attack.AttackManager;
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
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultFormatter;
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

    public static enum TRANSFER_TYPE {

        CUT_TO_INTERNAL_CLIPBOARD, COPY_TO_INTERNAL_CLIPBOARD, FROM_INTERNAL_CLIPBOARD
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
            }
        }
    }
    private static Logger logger = Logger.getLogger("DoItYourselflAttackPlaner");
    private static DSWorkbenchDoItYourselfAttackPlaner SINGLETON = null;

    /** Creates new form DSWorkbenchDoItYourselflAttackPlaner */
    DSWorkbenchDoItYourselfAttackPlaner() {
        initComponents();

        jAttackTable.setModel(new DoItYourselfAttackTableModel());
        jAttackTable.getSelectionModel().addListSelectionListener(DSWorkbenchDoItYourselfAttackPlaner.this);

        jArriveTime.setDate(Calendar.getInstance().getTime());
        jNewArriveSpinner.setDate(Calendar.getInstance().getTime());

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

    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTopBox.isSelected());
        PropertyHelper.storeTableProperties(jAttackTable, pConfig, getPropertyPrefix());

    }

    public void restoreCustomProperties(Configuration pConfig) {
        try {
            jAlwaysOnTopBox.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception e) {
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
        jAttackTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jAttackTable.setDefaultRenderer(Integer.class, new AttackTypeCellRenderer());
        jAttackTable.setDefaultRenderer(Date.class, new ColoredDateCellRenderer());
        jAttackTable.setDefaultEditor(Date.class, new DateSpinEditor());
        jAttackTable.setDefaultEditor(Integer.class, new AttackTypeCellEditor());
        jAttackTable.setDefaultRenderer(Attack.class, new AttackMiscInfoRenderer());
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

        if (ServerSettings.getSingleton().getCoordType() != 2) {
            jSourceVillage.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new DefaultFormatter()));
            jSourceVillage.setText("00:00:00");
            jTargetVillage.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new DefaultFormatter()));
            jTargetVillage.setText("00:00:00");
        } else {
            try {
                jSourceVillage.setText("000|000");
                jSourceVillage.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###|###")));
                jTargetVillage.setText("000|000");
                jTargetVillage.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###|###")));
            } catch (java.text.ParseException ex) {
            }
        }
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
        jSourceVillage = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        jTargetVillage = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jUnitBox = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jArriveTime = new de.tor.tribes.ui.components.DateTimeField();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jAttackTable = new org.jdesktop.swingx.JXTable();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.CapabilityInfoPanel();

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

        jLabel7.setText("Ankunftszeit");
        jLabel7.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel7.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 25));

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

        jLabel8.setText("Einheit");
        jLabel8.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel8.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel8.setPreferredSize(new java.awt.Dimension(80, 25));

        jUnitComboBox.setMinimumSize(new java.awt.Dimension(23, 25));
        jUnitComboBox.setPreferredSize(new java.awt.Dimension(28, 25));

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

        jLabel9.setText("Angriffstyp");
        jLabel9.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel9.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel9.setPreferredSize(new java.awt.Dimension(80, 25));

        jAttackTypeComboBox.setMinimumSize(new java.awt.Dimension(23, 25));
        jAttackTypeComboBox.setPreferredSize(new java.awt.Dimension(28, 25));

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

        jNewArriveSpinner.setMinimumSize(new java.awt.Dimension(64, 25));
        jNewArriveSpinner.setPreferredSize(new java.awt.Dimension(258, 25));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jAttackTypeComboBox, 0, 215, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jAdeptTypeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jNewArriveSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                            .addComponent(jUnitComboBox, 0, 215, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jAdeptUnitButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jAdeptTimeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAdeptTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jNewArriveSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAdeptUnitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jUnitComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAdeptTypeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jAttackTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(85, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jPanel3, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Neuer Angriff"));
        jPanel2.setOpaque(false);

        jLabel1.setText("Herkunft");
        jLabel1.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel1.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel1.setPreferredSize(new java.awt.Dimension(80, 25));

        jSourceVillage.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSourceVillage.setToolTipText("Herkunftsdorf");
        jSourceVillage.setMinimumSize(new java.awt.Dimension(6, 25));
        jSourceVillage.setPreferredSize(new java.awt.Dimension(6, 25));

        jLabel2.setText("Ziel");
        jLabel2.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel2.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel2.setPreferredSize(new java.awt.Dimension(80, 25));

        jTargetVillage.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTargetVillage.setToolTipText("Zieldorf");
        jTargetVillage.setMinimumSize(new java.awt.Dimension(6, 25));
        jTargetVillage.setPreferredSize(new java.awt.Dimension(6, 25));

        jLabel3.setText("Einheit");
        jLabel3.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel3.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel3.setPreferredSize(new java.awt.Dimension(80, 25));

        jLabel4.setText("Ankunft");
        jLabel4.setMaximumSize(new java.awt.Dimension(80, 25));
        jLabel4.setMinimumSize(new java.awt.Dimension(80, 25));
        jLabel4.setPreferredSize(new java.awt.Dimension(80, 25));

        jUnitBox.setToolTipText("Langsamste Einheit");
        jUnitBox.setMinimumSize(new java.awt.Dimension(23, 25));
        jUnitBox.setPreferredSize(new java.awt.Dimension(28, 25));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton1.setText("Hinzufügen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddAttackEvent(evt);
            }
        });

        jArriveTime.setMinimumSize(new java.awt.Dimension(64, 25));
        jArriveTime.setPreferredSize(new java.awt.Dimension(258, 25));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTargetVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addComponent(jArriveTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jUnitBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 258, Short.MAX_VALUE)
                    .addComponent(jSourceVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jSourceVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTargetVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jArriveTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jUnitBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        capabilityInfoPanel1.setBbSupport(false);
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
        String source = jSourceVillage.getText();

        List<Village> sourceList = PluginManager.getSingleton().executeVillageParser(source);
        if (sourceList.isEmpty()) {
            showError("Kein gültiges Herkunftsdorf gewählt");
            return;
        }
        String target = jTargetVillage.getText();
        List<Village> targetList = PluginManager.getSingleton().executeVillageParser(target);
        if (targetList.isEmpty()) {
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
        AttackManager.getSingleton().addDoItYourselfAttack(sourceList.get(0), targetList.get(0), unit, arrive, type);
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
        } catch (UnsupportedFlavorException ufe) {
            logger.error("Failed to copy attacks from internal clipboard", ufe);
            showError("Fehler beim Einfügen der Angriffe");
        } catch (IOException ioe) {
            logger.error("Failed to copy attacks from internal clipboard", ioe);
            showError("Fehler beim Einfügen der Angriffe");
        }
        ((DoItYourselfAttackTableModel) jAttackTable.getModel()).fireTableDataChanged();
        AttackManager.getSingleton().revalidate();
    }

    private List<Attack> getSelectedAttacks() {
        final List<Attack> selectedAttacks = new LinkedList<Attack>();
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
        } catch (Exception e) {
        }
        DSWorkbenchDoItYourselfAttackPlaner.getSingleton().resetView();
        //  DSWorkbenchAttackFrame.getSingleton().setSize(800, 600);
        DSWorkbenchAttackFrame.getSingleton().pack();
        AttackManager.getSingleton().invalidate();
        for (int i = 0; i < 100; i++) {
            AttackManager.getSingleton().addDoItYourselfAttack(DataHolder.getSingleton().getRandomVillage(), DataHolder.getSingleton().getRandomVillage(), DataHolder.getSingleton().getRandomUnit(), new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY), Attack.CLEAN_TYPE);
        }

        AttackManager.getSingleton().revalidate(AttackManager.MANUAL_ATTACK_PLAN, true);
        DSWorkbenchDoItYourselfAttackPlaner.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DSWorkbenchDoItYourselfAttackPlaner.getSingleton().setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel1;
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
    private javax.swing.JFormattedTextField jSourceVillage;
    private javax.swing.JFormattedTextField jTargetVillage;
    private javax.swing.JComboBox jUnitBox;
    private javax.swing.JComboBox jUnitComboBox;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    // End of variables declaration//GEN-END:variables
}

class DoItYourselfCountdownThread extends Thread {

    public DoItYourselfCountdownThread() {
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
            } catch (Exception e) {
            }
        }
    }
}
