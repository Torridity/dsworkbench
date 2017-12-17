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

import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.swing.TabEditingEvent;
import com.jidesoft.swing.TabEditingListener;
import com.jidesoft.swing.TabEditingValidator;
import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.test.DummyUnit;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.ClickAccountPanel;
import de.tor.tribes.ui.components.ProfileQuickChangePanel;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.panels.AttackTableTab;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.MouseGestureHandler;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.PropertyHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.UIResource;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

// -Dsun.java2d.d3d=true -Dsun.java2d.translaccel=true -Dsun.java2d.ddforcevram=true
/**
 * @author Charon
 */
public class DSWorkbenchAttackFrame extends AbstractDSWorkbenchFrame implements GenericManagerListener, ActionListener, Serializable {

    @Override
    public void actionPerformed(ActionEvent e) {
        AttackTableTab activeTab = getActiveTab();
        int idx = jAttackTabPane.getSelectedIndex();
        if (e.getActionCommand() != null && activeTab != null) {
            if (e.getActionCommand().equals("TimeChange")) {
                activeTab.fireChangeTimeEvent();
            } else if (e.getActionCommand().equals("UnitChange")) {
                activeTab.fireChangeUnitEvent();
            } else if (e.getActionCommand().equals("Recolor")) {
                activeTab.updateSortHighlighter();
            } else if (e.getActionCommand().equals("ExportScript")) {
                activeTab.fireExportScriptEvent();
            } else if (e.getActionCommand().equals("Copy")) {
                activeTab.transferSelection(AttackTableTab.TRANSFER_TYPE.COPY_TO_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("BBCopy")) {
                activeTab.transferSelection(AttackTableTab.TRANSFER_TYPE.CLIPBOARD_BB);
            } else if (e.getActionCommand().equals("Cut")) {
                activeTab.transferSelection(AttackTableTab.TRANSFER_TYPE.CUT_TO_INTERNAL_CLIPBOARD);
                jAttackTabPane.setSelectedIndex(idx);
            } else if (e.getActionCommand().equals("Paste")) {
                activeTab.transferSelection(AttackTableTab.TRANSFER_TYPE.FROM_INTERNAL_CLIPBOARD);
                jAttackTabPane.setSelectedIndex(idx);
            } else if (e.getActionCommand().equals("Delete")) {
                activeTab.deleteSelection(true);
            } else if (e.getActionCommand().equals("Find")) {
                BufferedImage back = ImageUtils.createCompatibleBufferedImage(3, 3, BufferedImage.TRANSLUCENT);
                Graphics g = back.getGraphics();
                g.setColor(new Color(120, 120, 120, 120));
                g.fillRect(0, 0, back.getWidth(), back.getHeight());
                g.setColor(new Color(120, 120, 120));
                g.drawLine(0, 0, 3, 3);
                g.dispose();
                TexturePaint paint = new TexturePaint(back, new Rectangle2D.Double(0, 0, back.getWidth(), back.getHeight()));

                jxSearchPane.setBackgroundPainter(new MattePainter(paint));
                DefaultListModel model = new DefaultListModel();

                for (int i = 0; i < activeTab.getAttackTable().getColumnCount(); i++) {
                    TableColumnExt col = activeTab.getAttackTable().getColumnExt(i);
                    if (col.isVisible()) {
                        if (!col.getTitle().equals("Einheit") && !col.getTitle().equals("Typ") && !col.getTitle().equals("Sonstiges")
                                && !col.getTitle().equals("Abschickzeit") && !col.getTitle().equals("Ankunftzeit") && !col.getTitle().equals("Verbleibend")) {
                            model.addElement(col.getTitle());
                        }
                    }
                }
                jXColumnList.setModel(model);
                jXColumnList.setSelectedIndex(0);
                jxSearchPane.setVisible(true);
            }
        }
    }
    private static Logger logger = Logger.getLogger("AttackView");
    private static DSWorkbenchAttackFrame SINGLETON = null;
    private CountdownThread mCountdownThread = null;
    private GenericTestPanel centerPanel = null;
    private ClickAccountPanel clickAccount = null;
    private ProfileQuickChangePanel profilePanel = null;

    public static synchronized DSWorkbenchAttackFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchAttackFrame();
        }
        return SINGLETON;
    }

    /**
     * Creates new form DSWorkbenchAttackFrame
     */
    DSWorkbenchAttackFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jAttackPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jXAttackPanel);
        buildMenu();
        capabilityInfoPanel1.addActionListener(this);
        jAttackTabPane.setCloseAction(new AbstractAction("closeAction") {

            public void actionPerformed(ActionEvent e) {
                AttackTableTab tab = (AttackTableTab) e.getSource();
                if (JOptionPaneHelper.showQuestionConfirmBox(jAttackTabPane, "Befehlsplan '" + tab.getAttackPlan() + "' und alle darin enthaltenen Befehle wirklich löschen? ", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                    AttackManager.getSingleton().removeGroup(tab.getAttackPlan());
                }
            }
        });
        jAttackTabPane.addTabEditingListener(new TabEditingListener() {

            @Override
            public void editingStarted(TabEditingEvent tee) {
            }

            @Override
            public void editingStopped(TabEditingEvent tee) {
                AttackManager.getSingleton().renameGroup(tee.getOldTitle(), tee.getNewTitle());
            }

            @Override
            public void editingCanceled(TabEditingEvent tee) {
            }
        });
        jAttackTabPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
        jAttackTabPane.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
        jAttackTabPane.setBoldActiveTab(true);
        jAttackTabPane.setTabEditingValidator(new TabEditingValidator() {

            @Override
            public boolean alertIfInvalid(int tabIndex, String tabText) {
                if (tabText.trim().length() == 0) {
                    JOptionPaneHelper.showWarningBox(jAttackTabPane, "'" + tabText + "' ist ein ungültiger Planname", "Fehler");
                    return false;
                }

                if (AttackManager.getSingleton().groupExists(tabText)) {
                    JOptionPaneHelper.showWarningBox(jAttackTabPane, "Es existiert bereits ein Plan mit dem Namen '" + tabText + "'", "Fehler");
                    return false;
                }
                return true;
            }

            @Override
            public boolean isValid(int tabIndex, String tabText) {
                return tabText.trim().length() != 0 && !AttackManager.getSingleton().groupExists(tabText);

            }

            @Override
            public boolean shouldStartEdit(int tabIndex, MouseEvent event) {
                return !(tabIndex == 0 || tabIndex == 1);
            }
        });

        new ColorUpdateThread().start();
        mCountdownThread = new CountdownThread();
        mCountdownThread.start();
        jXColumnList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateFilter();
            }
        });


        jAttackTabPane.getModel().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.updatePlan();
                }
            }
        });

        setGlassPane(jxSearchPane);
        pack();
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.attack_view", GlobalOptions.getHelpBroker().getHelpSet());
        }       // </editor-fold>
    }

    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAttackFrameAlwaysOnTop.isSelected());

        int selectedIndex = jAttackTabPane.getModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            pConfig.setProperty(getPropertyPrefix() + ".tab.selection", selectedIndex);
        }

        AttackTableTab tab = ((AttackTableTab) jAttackTabPane.getComponentAt(0));
        PropertyHelper.storeTableProperties(tab.getAttackTable(), pConfig, getPropertyPrefix());
    }

    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));
        try {
            jAttackTabPane.setSelectedIndex(pConfig.getInteger(getPropertyPrefix() + ".tab.selection", 0));
        } catch (Exception ignored) {
        }
        try {
            jAttackFrameAlwaysOnTop.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }

        setAlwaysOnTop(jAttackFrameAlwaysOnTop.isSelected());

        AttackTableTab tab = ((AttackTableTab) jAttackTabPane.getComponentAt(0));
        PropertyHelper.restoreTableProperties(tab.getAttackTable(), pConfig, getPropertyPrefix());
    }

    @Override
    public String getPropertyPrefix() {
        return "attack.view";
    }

    /**
     * Get the currently selected tab
     */
    private AttackTableTab getActiveTab() {
        try {
            if (jAttackTabPane.getModel().getSelectedIndex() < 0) {
                return null;
            }
            return ((AttackTableTab) jAttackTabPane.getComponentAt(jAttackTabPane.getModel().getSelectedIndex()));
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**
     * Get the currently active attack plan
     *
     * @return
     */
    public String getActivePlan() {
        AttackTableTab tab = getActiveTab();
        if (tab == null) {
            return AttackManager.DEFAULT_GROUP;
        }
        return tab.getAttackPlan();
    }

    /**
     * Build the main menu for this frame
     */
    private void buildMenu() {
        // <editor-fold defaultstate="collapsed" desc="Edit task pane">
        JXTaskPane editTaskPane = new JXTaskPane();
        editTaskPane.setTitle("Bearbeiten");
        editTaskPane.getContentPane().add(factoryButton("/res/ui/garbage.png", "Abgelaufene Befehle entfernen", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.cleanup();
                }
            }
        }));
        editTaskPane.getContentPane().add(factoryButton("/res/ui/att_changeTime.png", "Ankunfts-/Abschickzeit für markierte Befehle &auml;ndern. "
                + "Die Startzeit der Befehle wird dabei entsprechend der Laufzeit angepasst", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.changeSelectionTime();
                }
            }
        }));
        editTaskPane.getContentPane().add(factoryButton("/res/ui/standard_attacks.png", "Einheit und Befehlstyp für markierte Befehle &auml;ndern. "
                + "Bitte beachte, dass sich beim &Auml;ndern der Einheit auch die Startzeit der Befehle &auml;ndern kann", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.changeSelectionType();
                }
            }
        }));
        editTaskPane.getContentPane().add(factoryButton("/res/ui/att_browser_unsent.png", "'&Uuml;bertragen' Feld für markierte Befehle l&ouml;schen", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.setSelectionUnsent();
                }
            }
        }));
        editTaskPane.getContentPane().add(factoryButton("/res/ui/pencil2.png", "Markierte Befehle auf der Karte einzeichen. "
                + "Ist ein gewählter Befehl bereits eingezeichnet, so wird er nach Bet&auml;tigung dieses Buttons nicht mehr eingezeichnet", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.changeSelectionDrawState();
                }
            }
        }));
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Transfer task pane">
        JXTaskPane transferTaskPane = new JXTaskPane();
        transferTaskPane.setTitle("Übertragen");
        transferTaskPane.getContentPane().add(factoryButton("/res/ui/att_clipboard.png", "Markierte Befehle im Klartext in die Zwischenablage kopieren. "
                + "Der Inhalt der Zwischenablage kann dann z.B. in Excel oder OpenOffice eingef&uuml;gt werden", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.transferSelection(AttackTableTab.TRANSFER_TYPE.CLIPBOARD_PLAIN);
                }
            }
        }));

        transferTaskPane.getContentPane().add(factoryButton("/res/ui/att_HTML.png", "Markierte Befehle in eine HTML Datei kopieren.<br/>"
                + "Die erstellte Datei kann dann per eMail verschickt oder zum Abschicken von Befehlen ohne ge&ouml;ffnetesDS Workbench verwendet werden", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.transferSelection(AttackTableTab.TRANSFER_TYPE.FILE_HTML);
                }
            }
        }));
        
         transferTaskPane.getContentPane().add(factoryButton("/res/ui/toTextFile.png", "Markierte Befehle auf mehrere Textdateien aufteilen.<br/>"
                + "Es werden f&uuml;r jeden Spieler mehrere Textdateien erstellt, die eine einstellbare Anzahl an Angriffe in BB-Codes enthalten.<br/>"
                 + "Diese k&ouml;nnen dann per Mail zugeschickt und weiterverarbeitet werden. Alternativ k&ouml;nnen die Textdateien f&uuml;r jeden Spieler<br/>"
                 + "auch in eine ZIP-Datei gepackt werden, um sie einfacher zu versenden.", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.transferSelection(AttackTableTab.TRANSFER_TYPE.FILE_TEXT);
                }
            }
        }));
        
        transferTaskPane.getContentPane().add(factoryButton("/res/ui/re-time.png", "Markierten Befehl in das Werkzeug 'Retimer' einfügen. Im Anschluss daran muss im Retimer noch die vermutete Einheit gewählt werden.", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.transferSelection(AttackTableTab.TRANSFER_TYPE.DSWB_RETIME);
                }
            }
        }));
        transferTaskPane.getContentPane().add(factoryButton("/res/ui/att_browser.png", "Markierte Befehle in den Browser &uuml;bertragen. "
                + "Im Normalfall werden nur einzelne Befehle &uuml;bertragen. F&uuml;r das &Uuml;bertragen mehrerer Befehle ist zuerst das Klickkonto entsprechend zu f&uuml;llen", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.transferSelection(AttackTableTab.TRANSFER_TYPE.BROWSER_LINK);
                }
            }
        }));
        transferTaskPane.getContentPane().add(factoryButton("/res/ui/export_js.png", "Markierte Befehle in ein Userscript schreiben. "
                + "Das erstellte Userscript muss im Anschluss manuell im Browser installiert werden. "
                + "Als Ergebnis bekommt man an verschiedenen Stellen im Spiel Informationen &uuml;ber geplante Befehle angezeigt.", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.transferSelection(AttackTableTab.TRANSFER_TYPE.FILE_GM);
                }
            }
        }));

        transferTaskPane.getContentPane().add(factoryButton("/res/ui/to_selection.png", "Herkunfts- oder Zield&ouml;rfer markierter Befehle in die Auswahl&uuml;bersicht übertragen.", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.transferSelection(AttackTableTab.TRANSFER_TYPE.SELECTION_TOOL);
                }
            }
        }));

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Misc task pane">

        JXTaskPane miscTaskPane = new JXTaskPane();
        miscTaskPane.setTitle("Sonstiges");

        miscTaskPane.getContentPane().add(factoryButton("/res/ui/colorize.gif", "F&auml;rbt zusammengeh&ouml;rigen Befehle entsprechend der aktuellen Tabellensortierung ein<br/>"
                + "<i>Farbalgorithmus &copy;bodhiBrute</i>", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab tab = getActiveTab();
                if (tab != null) {
                    tab.setUseSortColoring();
                }
            }
        }));

        miscTaskPane.getContentPane().add(factoryButton("/res/ui/att_alert_off.png", "Einen Alarm für den gewählten Befehl erstellen", new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                AttackTableTab tab = getActiveTab();
                if (tab != null) {
                    tab.addAttackTimer();
                }
            }
        }));
        // </editor-fold>

        clickAccount = new ClickAccountPanel();
        profilePanel = new ProfileQuickChangePanel();
        centerPanel.setupTaskPane(clickAccount, profilePanel, editTaskPane, transferTaskPane, miscTaskPane);
    }

    /**
     * Factory a new button
     */
    private JXButton factoryButton(String pIconResource, String pTooltip, MouseListener pListener) {
        JXButton button = new JXButton(new ImageIcon(DSWorkbenchAttackFrame.class.getResource(pIconResource)));
        if (pTooltip != null) {
            button.setToolTipText("<html><div width='150px'>" + pTooltip + "</div></html>");
        }
        button.addMouseListener(pListener);
        return button;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jxSearchPane = new org.jdesktop.swingx.JXPanel();
        jXPanel2 = new org.jdesktop.swingx.JXPanel();
        jButton12 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jFilterRows = new javax.swing.JCheckBox();
        jFilterCaseSensitive = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXColumnList = new org.jdesktop.swingx.JXList();
        jLabel22 = new javax.swing.JLabel();
        jXAttackPanel = new org.jdesktop.swingx.JXPanel();
        jAttackTabPane = new com.jidesoft.swing.JideTabbedPane();
        jNewPlanPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jAttackPanel = new javax.swing.JPanel();
        jAttackFrameAlwaysOnTop = new javax.swing.JCheckBox();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jxSearchPane.setOpaque(false);
        jxSearchPane.setLayout(new java.awt.GridBagLayout());

        jXPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jXPanel2.setInheritAlpha(false);

        jButton12.setText("Anwenden");
        jButton12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideGlassPaneEvent(evt);
            }
        });

        jTextField1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireHighlightEvent(evt);
            }
        });

        jLabel21.setText("Suchbegriff");

        jFilterRows.setText("Nur gefilterte Zeilen anzeigen");
        jFilterRows.setOpaque(false);
        jFilterRows.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireUpdateFilterEvent(evt);
            }
        });

        jFilterCaseSensitive.setText("Groß-/Kleinschreibung beachten");
        jFilterCaseSensitive.setOpaque(false);
        jFilterCaseSensitive.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireUpdateFilterEvent(evt);
            }
        });

        jXColumnList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jXColumnList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jXColumnList);

        jLabel22.setText("Spalten");

        javax.swing.GroupLayout jXPanel2Layout = new javax.swing.GroupLayout(jXPanel2);
        jXPanel2.setLayout(jXPanel2Layout);
        jXPanel2Layout.setHorizontalGroup(
            jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jXPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jXPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jFilterRows, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jFilterCaseSensitive, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jButton12)))
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jXPanel2Layout.setVerticalGroup(
            jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jXPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jXPanel2Layout.createSequentialGroup()
                            .addComponent(jFilterCaseSensitive)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jFilterRows)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton12))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel22))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jxSearchPane.add(jXPanel2, new java.awt.GridBagConstraints());

        jXAttackPanel.setLayout(new java.awt.BorderLayout());

        jAttackTabPane.setScrollSelectedTabOnWheel(true);
        jAttackTabPane.setShowCloseButtonOnTab(true);
        jAttackTabPane.setShowGripper(true);
        jAttackTabPane.setTabEditingAllowed(true);
        jXAttackPanel.add(jAttackTabPane, java.awt.BorderLayout.CENTER);

        jNewPlanPanel.setOpaque(false);
        jNewPlanPanel.setLayout(new java.awt.BorderLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_new_24x24.png"))); // NOI18N
        jLabel1.setToolTipText("Leeren Plan erstellen");
        jLabel1.setEnabled(false);
        jLabel1.setMaximumSize(new java.awt.Dimension(40, 40));
        jLabel1.setMinimumSize(new java.awt.Dimension(40, 40));
        jLabel1.setPreferredSize(new java.awt.Dimension(40, 40));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireEnterEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireMouseExitEvent(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCreateAttackPlanEvent(evt);
            }
        });
        jNewPlanPanel.add(jLabel1, java.awt.BorderLayout.CENTER);

        setTitle("Befehle");
        setMinimumSize(new java.awt.Dimension(700, 500));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jAttackPanel.setBackground(new java.awt.Color(239, 235, 223));
        jAttackPanel.setPreferredSize(new java.awt.Dimension(700, 500));
        jAttackPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jAttackPanel, gridBagConstraints);

        jAttackFrameAlwaysOnTop.setText("Immer im Vordergrund");
        jAttackFrameAlwaysOnTop.setOpaque(false);
        jAttackFrameAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAttackFrameAlwaysOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAttackFrameAlwaysOnTop, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireHideGlassPaneEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideGlassPaneEvent
    jxSearchPane.setBackgroundPainter(null);
    jxSearchPane.setVisible(false);
}//GEN-LAST:event_fireHideGlassPaneEvent
private void fireHighlightEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireHighlightEvent
    updateFilter();
}//GEN-LAST:event_fireHighlightEvent

private void fireUpdateFilterEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireUpdateFilterEvent
    updateFilter();
}//GEN-LAST:event_fireUpdateFilterEvent

private void fireAttackFrameAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAttackFrameAlwaysOnTopEvent
    setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_fireAttackFrameAlwaysOnTopEvent

private void fireEnterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireEnterEvent
    jLabel1.setEnabled(true);
}//GEN-LAST:event_fireEnterEvent

private void fireMouseExitEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMouseExitEvent
    jLabel1.setEnabled(false);
}//GEN-LAST:event_fireMouseExitEvent

private void fireCreateAttackPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCreateAttackPlanEvent
    int unusedId = 1;
    while (unusedId < 1000) {
        if (AttackManager.getSingleton().addGroup("Neuer Plan " + unusedId)) {
            break;
        }
        unusedId++;
    }
    if (unusedId == 1000) {
        JOptionPaneHelper.showErrorBox(DSWorkbenchAttackFrame.this, "Du hast mehr als 1000 Befehlspläne. Bitte lösche zuerst ein paar bevor du Neue erstellst.", "Fehler");
    }
}//GEN-LAST:event_fireCreateAttackPlanEvent

    /**
     * Update the attack plan filter
     */
    private void updateFilter() {
        AttackTableTab tab = getActiveTab();
        if (tab != null) {
            final List<String> selection = new LinkedList<>();
            for (Object o : jXColumnList.getSelectedValues()) {
                selection.add((String) o);
            }
            tab.updateFilter(jTextField1.getText(), selection, jFilterCaseSensitive.isSelected(), jFilterRows.isSelected());
        }
    }

    @Override
    public void toBack() {
        jAttackFrameAlwaysOnTop.setSelected(false);
        fireAttackFrameAlwaysOnTopEvent(null);
        super.toBack();
    }

    public boolean decreaseClickAccountValue() {
        return clickAccount.useClick();
    }

    public void increaseClickAccountValue() {
        clickAccount.giveClickBack();
    }

    public UserProfile getQuickProfile() {
        return profilePanel.getSelectedProfile();
    }

    @Override
    public void resetView() {
        AttackManager.getSingleton().addManagerListener(this);
        generateAttackTabs();
    }

    /**
     * Initialize and add one tab for each attack plan to jTabbedPane1
     */
    public void generateAttackTabs() {
        jAttackTabPane.invalidate();
        while (jAttackTabPane.getTabCount() > 0) {
            AttackTableTab tab = (AttackTableTab) jAttackTabPane.getComponentAt(0);
            tab.deregister();
            jAttackTabPane.removeTabAt(0);
        }
        LabelUIResource lr = new LabelUIResource();
        lr.setLayout(new BorderLayout());
        lr.add(jNewPlanPanel, BorderLayout.CENTER);
        jAttackTabPane.setTabLeadingComponent(lr);
        String[] plans = AttackManager.getSingleton().getGroups();

        //insert default tab to first place
        int cnt = 0;

        for (String plan : plans) {
            AttackTableTab tab = new AttackTableTab(plan, this);
            jAttackTabPane.addTab(plan, tab);
            cnt++;
        }

        jAttackTabPane.setTabClosableAt(0, false);
        jAttackTabPane.setTabClosableAt(1, false);
        jAttackTabPane.revalidate();
        AttackTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updatePlan();
        }
    }

    @Override
    public void dataChangedEvent() {
        generateAttackTabs();
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        AttackTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updatePlan();
        }
    }

    public void updateCountdownSettings() {
        if (mCountdownThread != null) {
            mCountdownThread.updateSettings();
        }
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    /**
     * Redraw the countdown col
     */
    protected void updateCountdown() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    ((AttackTableTab) jAttackTabPane.getSelectedComponent()).updateCountdown();
                } catch (Exception ignored) {
                }
            }
        });

    }

    /**
     * Redraw the time col
     */
    protected void updateTime() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    ((AttackTableTab) jAttackTabPane.getSelectedComponent()).updateTime();
                } catch (Exception ignored) {
                }
            }
        });

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

        //  DSWorkbenchAttackFrame.getSingleton().setSize(800, 600);
        DSWorkbenchAttackFrame.getSingleton().pack();
        AttackManager.getSingleton().addGroup("test1");
        AttackManager.getSingleton().addGroup("asd2");
        AttackManager.getSingleton().addGroup("awe3");
        for (int i = 0; i < 100; i++) {
            Attack a = new Attack();
            a.setSource(DataHolder.getSingleton().getRandomVillage());
            a.setTarget(DataHolder.getSingleton().getRandomVillage());
            a.setArriveTime(new Date(Math.round(Math.random() * System.currentTimeMillis())));
            a.setUnit(new DummyUnit());
            Attack a1 = new Attack();
            a1.setSource(DataHolder.getSingleton().getRandomVillage());
            a1.setTarget(DataHolder.getSingleton().getRandomVillage());
            a1.setArriveTime(new Date(Math.round(Math.random() * System.currentTimeMillis())));
            a1.setUnit(new DummyUnit());
            Attack a2 = new Attack();
            a2.setSource(DataHolder.getSingleton().getRandomVillage());
            a2.setTarget(DataHolder.getSingleton().getRandomVillage());
            a2.setArriveTime(new Date(Math.round(Math.random() * System.currentTimeMillis())));
            a2.setUnit(new DummyUnit());
            AttackManager.getSingleton().addManagedElement(a);
            AttackManager.getSingleton().addManagedElement("test1", a1);
            AttackManager.getSingleton().addManagedElement("asd2", a2);
        }
        DSWorkbenchAttackFrame.getSingleton().resetView();
        DSWorkbenchAttackFrame.getSingleton().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        DSWorkbenchAttackFrame.getSingleton().setVisible(true);
    }
    // <editor-fold defaultstate="collapsed" desc="Gesture Handling">

    @Override
    public void fireExportAsBBGestureEvent() {
        AttackTableTab tab = getActiveTab();
        if (tab != null) {
            tab.transferSelection(AttackTableTab.TRANSFER_TYPE.CLIPBOARD_BB);
        }
    }

    @Override
    public void firePlainExportGestureEvent() {
        AttackTableTab tab = getActiveTab();
        if (tab != null) {
            tab.transferSelection(AttackTableTab.TRANSFER_TYPE.CLIPBOARD_PLAIN);
        }
    }

    @Override
    public void fireNextPageGestureEvent() {
        int current = jAttackTabPane.getSelectedIndex();
        int size = jAttackTabPane.getTabCount();
        if (current + 1 > size - 1) {
            current = 0;
        } else {
            current += 1;
        }
        jAttackTabPane.setSelectedIndex(current);
    }

    @Override
    public void firePreviousPageGestureEvent() {
        int current = jAttackTabPane.getSelectedIndex();
        int size = jAttackTabPane.getTabCount();
        if (current - 1 < 0) {
            current = size - 1;
        } else {
            current -= 1;
        }
        jAttackTabPane.setSelectedIndex(current);
    }

    @Override
    public void fireRenameGestureEvent() {
        int idx = jAttackTabPane.getSelectedIndex();
        if (idx != 0 && idx != 1) {
            jAttackTabPane.editTabAt(idx);
        }
    }
// </editor-fold>
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JCheckBox jAttackFrameAlwaysOnTop;
    private javax.swing.JPanel jAttackPanel;
    private com.jidesoft.swing.JideTabbedPane jAttackTabPane;
    private javax.swing.JButton jButton12;
    private javax.swing.JCheckBox jFilterCaseSensitive;
    private javax.swing.JCheckBox jFilterRows;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JPanel jNewPlanPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private org.jdesktop.swingx.JXPanel jXAttackPanel;
    private org.jdesktop.swingx.JXList jXColumnList;
    private org.jdesktop.swingx.JXPanel jXPanel2;
    private org.jdesktop.swingx.JXPanel jxSearchPane;
    // End of variables declaration//GEN-END:variables
}

class ColorUpdateThread extends Thread {

    public ColorUpdateThread() {
        setName("AttackColorUpdater");
        setPriority(MIN_PRIORITY);
        setDaemon(true);
    }

    public void run() {
        while (true) {
            try {
                DSWorkbenchAttackFrame.getSingleton().updateTime();
                try {
                    Thread.sleep(10000);
                } catch (Exception ignored) {
                }
            } catch (Throwable ignored) {
            }
        }
    }
}

class CountdownThread extends Thread {

    private boolean showCountdown = true;

    public CountdownThread() {
        setName("AttackTableCountdownUpdater");
        setPriority(MIN_PRIORITY);
        setDaemon(true);
    }

    public void updateSettings() {
        showCountdown = GlobalOptions.getProperties().getBoolean("show.live.countdown");
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (showCountdown && DSWorkbenchAttackFrame.getSingleton().isVisible()) {
                    DSWorkbenchAttackFrame.getSingleton().updateCountdown();
                    //yield();
                    sleep(100);
                } else {
                    // yield();
                    sleep(1000);
                }
            } catch (Exception ignored) {
            }
        }
    }
}

class LabelUIResource extends JPanel implements UIResource {

    public LabelUIResource() {
        super();
    }
}
