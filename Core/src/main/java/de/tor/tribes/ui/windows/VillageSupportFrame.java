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

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.UnitOrderBuilder;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.SupportCalculator;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.bb.AttackListFormatter;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * @author Charon
 */
public class VillageSupportFrame extends javax.swing.JFrame implements ActionListener {

    public enum TRANSFER_TYPE {

        CLIPBOARD_BB, CUT_TO_INTERNAL_CLIPBOARD, COPY_TO_INTERNAL_CLIPBOARD
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals("Copy")) {
            transferSelection(VillageSupportFrame.TRANSFER_TYPE.COPY_TO_INTERNAL_CLIPBOARD);
        } else if (e.getActionCommand().equals("BBCopy")) {
            transferSelection(VillageSupportFrame.TRANSFER_TYPE.CLIPBOARD_BB);
        } else if (e.getActionCommand().equals("Cut")) {
            transferSelection(VillageSupportFrame.TRANSFER_TYPE.CUT_TO_INTERNAL_CLIPBOARD);
        } else if (e.getActionCommand().equals("Delete")) {
            deleteSelection(true);
        }

    }
    private static Logger logger = Logger.getLogger("SupportDialog");
    private static VillageSupportFrame SINGLETON = null;
    private Village mCurrentVillage = null;

    public static synchronized VillageSupportFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new VillageSupportFrame();
        }
        return SINGLETON;
    }

    /** Creates new form VillageSupportFrame */
    VillageSupportFrame() {
        initComponents();

        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
        KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        jSupportTable.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jSupportTable.registerKeyboardAction(this, "Cut", cut, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        jSupportTable.registerKeyboardAction(this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jSupportTable.registerKeyboardAction(this, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jSupportTable.getActionMap().put("find", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //ignore find
            }
        });

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.support_tool", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>
    }

    public void transferSelection(TRANSFER_TYPE pType) {
        switch (pType) {
            case COPY_TO_INTERNAL_CLIPBOARD:
                copyToInternalClipboard();
                break;
            case CUT_TO_INTERNAL_CLIPBOARD:
                cutToInternalClipboard();
                break;
            case CLIPBOARD_BB:
                copyBBToExternalClipboardEvent();
                break;
        }
    }

    private void copyBBToExternalClipboardEvent() {
        try {
            List<Attack> supports = getSelectedSupports();
            if (supports.isEmpty()) {
                showInfo("Keine Unterstützungen ausgewählt");
                return;
            }
            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

            StringBuilder buffer = new StringBuilder();
            if (extended) {
                buffer.append("[u][size=12]Unterstützungsplan[/size][/u]\n\n");
            } else {
                buffer.append("[u]Unterstützungsplan[/u]\n\n");
            }

            buffer.append(new AttackListFormatter().formatElements(supports, extended));

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
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Unterstützungen benötigen mehr als 1000 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
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

    private boolean copyToInternalClipboard() {
        List<Attack> selection = getSelectedSupports();
        if (selection.isEmpty()) {
            showInfo("Keine Unterstützungen gewählt");
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
            showSuccess(cnt + ((cnt == 1) ? " Unterstützung kopiert" : " Unterstützungen kopiert"));
            return true;
        } catch (HeadlessException hex) {
            showError("Fehler beim Kopieren der Unterstützungen");
            return false;
        }
    }

    private void cutToInternalClipboard() {
        int size = getSelectedSupports().size();
        if (size == 0) {
            showInfo("Keine Unterstützungen gewählt");
            return;
        }
        if (copyToInternalClipboard() && deleteSelection(false)) {
            showSuccess(size + ((size == 1) ? " Unterstützung ausgeschnitten" : " Unterstützungen ausgeschnitten"));
        } else {
            showError("Fehler beim Ausschneiden der Unterstützungen");
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

    public void showSupportFrame(Village pCurrent) {
        /*mCurrentVillage = pCurrent;
        setTitle("Unterstützung für " + mCurrentVillage);
        DefaultListModel model = new DefaultListModel();
        model.addElement(NoTag.getSingleton());
        for (ManageableType e : TagManager.getSingleton().getAllElements()) {
            model.addElement((Tag) e);
        }
        jTagsList.setModel(model);
        //select all
        jTagsList.getSelectionModel().setSelectionInterval(0, TagManager.getSingleton().getElementCount() - 1);
        jResultFrame.pack();
        setVisible(true);*/
        showSupportFrame(pCurrent, System.currentTimeMillis());
        
    }

    public void showSupportFrame(Village pTarget, long pArriveTime) {
        mCurrentVillage = pTarget;
        setTitle("Unterstützung für " + mCurrentVillage);
        DefaultListModel model = new DefaultListModel();
        model.addElement(NoTag.getSingleton());
        for (ManageableType e : TagManager.getSingleton().getAllElements()) {
            model.addElement(e);
        }
        jTagsList.setModel(model);
        //select all
        jTagsList.getSelectionModel().setSelectionInterval(0, TagManager.getSingleton().getElementCount() - 1);
        dateTimeField.setDate(new Date(pArriveTime));
        jResultFrame.pack();
        setVisible(true);
    }

    private boolean deleteSelection(boolean pAsk) {
        List<Attack> selectedSupports = getSelectedSupports();
        if (pAsk) {
            String message = ((selectedSupports.size() == 1) ? "Unterstützung " : (selectedSupports.size() + " Unterstützungen ")) + "wirklich löschen?";
            if (selectedSupports.isEmpty() || JOptionPaneHelper.showQuestionConfirmBox(jResultFrame, message, "Angriffe löschen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        DefaultTableModel model = (DefaultTableModel) jSupportTable.getModel();
        int numRows = jSupportTable.getSelectedRows().length;
        for (int i = 0; i < numRows; i++) {
            model.removeRow(jSupportTable.convertRowIndexToModel(jSupportTable.getSelectedRow()));
        }
        showSuccess(((numRows == 1) ? "Unterstützung " : " Unterstützungen ") + " gelöscht");
        return true;
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
        jLabel5 = new javax.swing.JLabel();
        jTargetVillage = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jArriveTime = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jSupportTable = new org.jdesktop.swingx.JXTable();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jLabel1 = new javax.swing.JLabel();
        jDefOnlyBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTagsList = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jMinUnitCountSpinner = new javax.swing.JSpinner();
        dateTimeField = new de.tor.tribes.ui.components.DateTimeField();

        jResultFrame.setTitle("Mögliche Unterstützungen");

        jLabel5.setText("Zu unterstützendes Dorf");
        jLabel5.setMaximumSize(new java.awt.Dimension(118, 25));
        jLabel5.setMinimumSize(new java.awt.Dimension(118, 25));
        jLabel5.setPreferredSize(new java.awt.Dimension(118, 25));

        jTargetVillage.setEditable(false);
        jTargetVillage.setMinimumSize(new java.awt.Dimension(6, 25));
        jTargetVillage.setPreferredSize(new java.awt.Dimension(6, 25));

        jButton3.setBackground(new java.awt.Color(239, 235, 223));
        jButton3.setText("Schließen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCloseResultsEvent(evt);
            }
        });

        jLabel6.setText("Ankunftzeit");
        jLabel6.setMaximumSize(new java.awt.Dimension(55, 25));
        jLabel6.setMinimumSize(new java.awt.Dimension(55, 25));
        jLabel6.setPreferredSize(new java.awt.Dimension(55, 25));

        jArriveTime.setEditable(false);
        jArriveTime.setMinimumSize(new java.awt.Dimension(6, 25));
        jArriveTime.setPreferredSize(new java.awt.Dimension(6, 25));

        jButton5.setBackground(new java.awt.Color(239, 235, 223));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/speed.png"))); // NOI18N
        jButton5.setToolTipText("Einordnung der Laufzeit für die in der Tabelle gewählte Einheit");
        jButton5.setMaximumSize(new java.awt.Dimension(57, 33));
        jButton5.setMinimumSize(new java.awt.Dimension(57, 33));
        jButton5.setPreferredSize(new java.awt.Dimension(57, 33));
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowTroopListEvent(evt);
            }
        });

        jButton6.setBackground(new java.awt.Color(239, 235, 223));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ally.png"))); // NOI18N
        jButton6.setToolTipText("Anzeige der maximalen Kampfkraft (Späher, Ramme und AG werden in jedem Fall ignoriert)");
        jButton6.setMaximumSize(new java.awt.Dimension(57, 33));
        jButton6.setMinimumSize(new java.awt.Dimension(57, 33));
        jButton6.setPreferredSize(new java.awt.Dimension(57, 33));
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateForceEvent(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jSupportTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(jSupportTable);

        jPanel1.add(jScrollPane3, java.awt.BorderLayout.CENTER);

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

        jPanel1.add(infoPanel, java.awt.BorderLayout.SOUTH);

        capabilityInfoPanel1.setPastable(false);
        capabilityInfoPanel1.setSearchable(false);

        javax.swing.GroupLayout jResultFrameLayout = new javax.swing.GroupLayout(jResultFrame.getContentPane());
        jResultFrame.getContentPane().setLayout(jResultFrameLayout);
        jResultFrameLayout.setHorizontalGroup(
            jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                    .addGroup(jResultFrameLayout.createSequentialGroup()
                        .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTargetVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                            .addComponent(jArriveTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                        .addComponent(capabilityInfoPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 399, Short.MAX_VALUE)
                        .addComponent(jButton3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jResultFrameLayout.setVerticalGroup(
            jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTargetVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jArriveTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(capabilityInfoPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        setTitle("Unterstützung");

        jLabel1.setText("Ankunftzeit");
        jLabel1.setMaximumSize(new java.awt.Dimension(150, 25));
        jLabel1.setMinimumSize(new java.awt.Dimension(150, 25));
        jLabel1.setPreferredSize(new java.awt.Dimension(150, 25));

        jDefOnlyBox.setSelected(true);
        jDefOnlyBox.setToolTipText("Bei der Berechnung nur echte Deff-Einheiten (Speer, Schwert, Bogen, SKav) berücksichtigen. Rammen, Späher und AGs werden in jedem Fall ignoriert.");
        jDefOnlyBox.setAlignmentY(0.0F);
        jDefOnlyBox.setIconTextGap(0);
        jDefOnlyBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jDefOnlyBox.setMaximumSize(new java.awt.Dimension(17, 25));
        jDefOnlyBox.setMinimumSize(new java.awt.Dimension(17, 25));
        jDefOnlyBox.setOpaque(false);
        jDefOnlyBox.setPreferredSize(new java.awt.Dimension(17, 25));

        jLabel2.setText("Nur Deff berücksichtigen");
        jLabel2.setMaximumSize(new java.awt.Dimension(150, 25));
        jLabel2.setMinimumSize(new java.awt.Dimension(150, 25));
        jLabel2.setPreferredSize(new java.awt.Dimension(150, 25));

        jLabel3.setText("<html>Dörfer aus folgenden Gruppen berücksichtigen</html>");
        jLabel3.setMaximumSize(new java.awt.Dimension(150, 100));
        jLabel3.setMinimumSize(new java.awt.Dimension(150, 14));
        jLabel3.setPreferredSize(new java.awt.Dimension(150, 30));

        jScrollPane1.setViewportView(jTagsList);

        jLabel4.setText("Min. Anzahl Einheiten");
        jLabel4.setMaximumSize(new java.awt.Dimension(150, 25));
        jLabel4.setMinimumSize(new java.awt.Dimension(150, 25));
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 25));

        jButton1.setText("Berechnen");
        jButton1.setToolTipText("Starte die Berechnung der maximalen Kampfkraft");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateEvent(evt);
            }
        });

        jButton2.setText("Schließen");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelEvent(evt);
            }
        });

        jMinUnitCountSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        jMinUnitCountSpinner.setToolTipText("<html>Minimale Anzahl der Einheiten von einem Typ aus einem Dorf, die als Unterst&uuml;tzung ber&uuml;cksichtigt werden.<br/>Es wird empfohlen, diesen Wert auf 0 zu lassen, um die maximal m&ouml;glichen Unterst&uuml;tzungen zu erzielen.</html> ");
        jMinUnitCountSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(jMinUnitCountSpinner, ""));
        jMinUnitCountSpinner.setMinimumSize(new java.awt.Dimension(31, 25));
        jMinUnitCountSpinner.setPreferredSize(new java.awt.Dimension(31, 25));

        dateTimeField.setToolTipText("Datum und Uhrzeit des Zeitrahmens");
        dateTimeField.setMaximumSize(new java.awt.Dimension(32767, 25));
        dateTimeField.setMinimumSize(new java.awt.Dimension(64, 25));
        dateTimeField.setPreferredSize(new java.awt.Dimension(258, 25));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, 0, 1, Short.MAX_VALUE)
                    .addComponent(jLabel1, 0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel2, 0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 144, Short.MAX_VALUE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jDefOnlyBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jMinUnitCountSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                            .addComponent(dateTimeField, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)))
                        .addGap(21, 21, 21)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dateTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDefOnlyBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMinUnitCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireCancelEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCancelEvent
        setVisible(false);
    }//GEN-LAST:event_fireCancelEvent

    private void fireCalculateEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateEvent
        boolean defOnly = jDefOnlyBox.isSelected();
        Date arrive = dateTimeField.getSelectedDate();
        Integer minUnitCnt = (Integer) jMinUnitCountSpinner.getValue();
        List<Tag> allowedTags = new LinkedList<>();
        for (Object o : jTagsList.getSelectedValues()) {
            allowedTags.add((Tag) o);
        }

        List<SupportCalculator.SupportMovement> movements = SupportCalculator.calculateSupport(mCurrentVillage, arrive, defOnly, allowedTags, minUnitCnt);
        if ((movements == null) || (movements.size() == 0)) {
            JOptionPaneHelper.showWarningBox(this, "Mit den eingestellten Parametern ist keine Unterstützung möglich.", "Warnung");
        } else {
            buildResults(movements);
            jTargetVillage.setText(mCurrentVillage.toString());
            jArriveTime.setText(new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(dateTimeField.getSelectedDate()));
            jResultFrame.setVisible(true);
        }
    }//GEN-LAST:event_fireCalculateEvent

    private void fireShowTroopListEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowTroopListEvent
        UnitHolder selectedUnit = null;
        int row = jSupportTable.getSelectedRow();
        if (row >= 0) {
            //row = jSupportTable.convertRowIndexToModel(row);
            selectedUnit = (UnitHolder) jSupportTable.getValueAt(row, 1);
        }
        UnitOrderBuilder.showUnitOrder(null, selectedUnit);
    }//GEN-LAST:event_fireShowTroopListEvent

    private void fireCloseResultsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseResultsEvent
        jResultFrame.setVisible(false);
    }//GEN-LAST:event_fireCloseResultsEvent

    private void fireCalculateForceEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateForceEvent
        calculateForce();
    }//GEN-LAST:event_fireCalculateForceEvent

    private void jXLabel1fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXLabel1fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXLabel1fireHideInfoEvent

    private void buildResults(List<SupportCalculator.SupportMovement> pMovements) {
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunftsdorf", "Truppen", "Abschickzeit", "#Verwendungen"
                }) {

            Class[] types = new Class[]{
                Village.class, UnitHolder.class, Date.class, Integer.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };

        jSupportTable.setModel(model);
        jSupportTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());

        if (ServerSettings.getSingleton().isMillisArrival()) {
            jSupportTable.setDefaultRenderer(Date.class, new DateCellRenderer("dd.MM.yy HH:mm:ss.SSS"));
        } else {
            jSupportTable.setDefaultRenderer(Date.class, new DateCellRenderer("dd.MM.yy HH:mm:ss"));
        }
        jSupportTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jSupportTable.setColumnControlVisible(false);
        jSupportTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());

        for (SupportCalculator.SupportMovement movement : pMovements) {
            Village village = movement.getSource();
            UnitHolder unit = movement.getUnit();
            Date sendTime = movement.getSendTime();
            int usages = 0;
            Iterator<String> plans = AttackManager.getSingleton().getGroupIterator();
            while (plans.hasNext()) {
                String planId = plans.next();
                List<ManageableType> plan = AttackManager.getSingleton().getAllElements(planId);
                for (ManageableType e : plan) {
                    Attack a = (Attack) e;
                    if (a.getSource().equals(village) && a.getType() == Attack.SUPPORT_TYPE) {
                        usages++;
                    }
                }
            }
            model.addRow(new Object[]{village, unit, sendTime, usages});
        }
    }

    private void calculateForce() {
        UnitHolder[] units = DataHolder.getSingleton().getUnits().toArray(new UnitHolder[]{});
        //sort units descending
        Arrays.sort(units, new Comparator<UnitHolder>() {

            @Override
            public int compare(UnitHolder o1, UnitHolder o2) {
                if (o1.getSpeed() == o2.getSpeed()) {
                    return 0;
                } else if (o1.getSpeed() < o2.getSpeed()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        Hashtable<UnitHolder, Integer> forceTable = new Hashtable<>();
        for (int i = 0; i < jSupportTable.getRowCount(); i++) {
            int row = i;//jSupportTable.convertRowIndexToModel(i);
            Village v = (Village) jSupportTable.getValueAt(row, 0);
            UnitHolder u = (UnitHolder) jSupportTable.getValueAt(row, 1);
            VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN);
            boolean useUnits = false;
            for (UnitHolder unit : units) {
                if (!useUnits) {
                    //if no unit is used yet
                    if (unit.equals(u)) {
                        //use all following units
                        useUnits = true;
                    }
                }

                if (useUnits) {
                    if (jDefOnlyBox.isSelected()) {
                        if (unit.getPlainName().equals("spear") || unit.getPlainName().equals("sword") || unit.getPlainName().equals("archer") || unit.getPlainName().equals("heavy")) {

                            if (troops != null) {
                                int cnt = troops.getTroopsOfUnitInVillage(unit);
                                if (forceTable.get(unit) != null) {
                                    forceTable.put(unit, forceTable.get(unit) + cnt);
                                } else {
                                    forceTable.put(unit, cnt);
                                }
                            }
                        }
                    } else {
                        if (!unit.getPlainName().equals("spy") && !unit.getPlainName().equals("ram") && !unit.getPlainName().equals("snob")) {
                            if (troops != null) {
                                int cnt = troops.getTroopsOfUnitInVillage(unit);
                                if (forceTable.get(unit) != null) {
                                    forceTable.put(unit, forceTable.get(unit) + cnt);
                                } else {
                                    forceTable.put(unit, cnt);
                                }
                            }
                        }
                    }
                }
            }
        }

        //add units of current village
        for (UnitHolder unit : units) {
            if (jDefOnlyBox.isSelected()) {
                if (unit.getPlainName().equals("spear") || unit.getPlainName().equals("sword") || unit.getPlainName().equals("archer") || unit.getPlainName().equals("heavy")) {
                    VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(mCurrentVillage);
                    if (troops != null) {
                        int cnt = troops.getTroopsOfUnitInVillage(unit);
                        if (forceTable.get(unit) != null) {
                            forceTable.put(unit, forceTable.get(unit) + cnt);
                        } else {
                            forceTable.put(unit, cnt);
                        }
                    }
                }
            } else {
                if (!unit.getPlainName().equals("spy") && !unit.getPlainName().equals("ram") && !unit.getPlainName().equals("snob")) {
                    VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(mCurrentVillage);
                    if (troops != null) {
                        int cnt = troops.getTroopsOfUnitInVillage(unit);
                        if (forceTable.get(unit) != null) {
                            forceTable.put(unit, forceTable.get(unit) + cnt);
                        } else {
                            forceTable.put(unit, cnt);
                        }
                    }
                }
            }
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append("Die maximale Kampfkraft beträgt:\n");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
            Integer cnt = forceTable.get(u);
            if ((cnt != null) && (cnt > 0)) {
                buffer.append(nf.format(cnt));
                buffer.append(" ").append(u.getName()).append("\n");
            }
        }
        JOptionPaneHelper.showInformationBox(jResultFrame, buffer.toString(), "Maximale Kampfkraft");
    }

    private List<Attack> getSelectedSupports() {
        final List<Attack> selectedSupports = new LinkedList<>();
        int[] selectedRows = jSupportTable.getSelectedRows();
        if (selectedRows != null && selectedRows.length < 1) {
            return selectedSupports;
        }

        for (Integer selectedRow : selectedRows) {
            Village source = (Village) jSupportTable.getValueAt(selectedRow, 0);
            UnitHolder unit = (UnitHolder) jSupportTable.getValueAt(selectedRow, 1);
            Date sendTime = (Date) jSupportTable.getValueAt(selectedRow, 2);
            Attack a = new Attack();
            a.setSource(source);
            a.setTarget(mCurrentVillage);
            a.setUnit(unit);
            a.setArriveTime(new Date(sendTime.getTime() + (long) (DSCalculator.calculateMoveTimeInSeconds(source, mCurrentVillage, unit.getSpeed()) * 1000)));
            a.setType(Attack.SUPPORT_TYPE);
            selectedSupports.add(a);
        }
        return selectedSupports;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private de.tor.tribes.ui.components.DateTimeField dateTimeField;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JTextField jArriveTime;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JCheckBox jDefOnlyBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSpinner jMinUnitCountSpinner;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JFrame jResultFrame;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private org.jdesktop.swingx.JXTable jSupportTable;
    private javax.swing.JList jTagsList;
    private javax.swing.JTextField jTargetVillage;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    // End of variables declaration//GEN-END:variables
}
