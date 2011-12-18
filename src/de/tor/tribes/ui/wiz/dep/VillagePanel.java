/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AttackSourcePanel.java
 *
 * Created on Oct 15, 2011, 9:54:36 AM
 */
package de.tor.tribes.ui.wiz.dep;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.TroopSplitDialog.TroopSplit;
import de.tor.tribes.util.AllyUtils;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.TagUtils;
import de.tor.tribes.util.TribeUtils;
import de.tor.tribes.util.VillageUtils;
import de.tor.tribes.util.map.FormManager;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanel;
import org.netbeans.spi.wizard.WizardPanelNavResult;

/**
 *
 * @author Torridity
 */
public class VillagePanel extends javax.swing.JPanel implements WizardPanel {
    
    protected static final String NO_DATA_AVAILABLE = "Keine Daten vorhanden";
    protected static final String ALL_DATA = "Alle";
    private static final String GENERAL_INFO = "Du befindest dich in der Dorfauswahl. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, "
            + "mit denen du verteidigen m&ouml;chtest. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten: "
            + "<ul> <li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li> "
            + "<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Gruppen der Gruppen&uuml;bersicht</li> "
            + "<li>Einf&uuml;gen der Herkunftsd&ouml;rfer mit Hilfe von Zeichnungen. Hier k&ouml;nnen D&ouml;rfer gew&auml;hlt werden, die von einer Zeichnung eingeschlossen sind.</li>"
            + "<li>Manuelle Eingabe unter Verwendung aller Weltdaten</li> "
            + "</ul> </html>";
    private static final String GROUP_INFO = "<html><h2>Datenquelle Gruppenübersicht</h2><br/>Hier k&ouml;nnen gezielt D&ouml;rfer verwendet werden, die sich in bestimmten Gruppen befinden. Die Auswahl der zu verwendenden Gruppe ist im Feld Gruppe/Zeichnung durchzuf&uuml;hren.</html>";
    private static final String DRAWING_INFO = "<html><h2>Datenquelle Zeichnungen</h2><br/>Hier k&ouml;nnen die D&ouml;rfer verwendet werden, die sich innerhalb einer bestimmten Zeichnung auf der Hauptkarte befinden. Die Auswahl der zu verwendenden Zeichnung ist im Feld Gruppe/Zeichnung durchzuf&uuml;hren.</html>";
    private static final String WORLDDATA_INFO = "<html><h2>Datenquelle Weltdaten</h2><br/>Mit dieser Option k&ouml;nnen D&ouml;rfer ausgehend von den kompletten Weltdaten gewählt werden.</html>";
    private List<Village> allowedVillages = new LinkedList<Village>();
    private List<Village> usedVillages = new LinkedList<Village>();
    private Hashtable<Village, Integer> supportsPerVillage = new Hashtable<Village, Integer>();
    private static VillagePanel singleton = null;
    private WizardController controller = null;
    
    public static synchronized VillagePanel getSingleton() {
        if (singleton == null) {
            singleton = new VillagePanel();
        }
        return singleton;
    }
    
    public void setController(WizardController pWizCtrl) {
        controller = pWizCtrl;
    }

    /** Creates new form AttackSourcePanel */
    VillagePanel() {
        initComponents();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jXCollapsiblePane2.setLayout(new BorderLayout());
        jXCollapsiblePane2.add(jDateSourcesPanel, BorderLayout.CENTER);
        
        updateVillageTable();
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        jVillageTable.registerKeyboardAction(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteFromClipboard();
                
            }
        }, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        jVillageTable.registerKeyboardAction(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelection();
            }
        }, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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

        jDateSourcesPanel = new javax.swing.JPanel();
        jGroupSource = new javax.swing.JRadioButton();
        jDrawingSource = new javax.swing.JRadioButton();
        jWorlddataSource = new javax.swing.JRadioButton();
        jSetBox = new javax.swing.JComboBox();
        jSetLabel = new javax.swing.JLabel();
        jInfoScrollPane = new javax.swing.JScrollPane();
        jInfoTextPane = new javax.swing.JTextPane();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel1 = new javax.swing.JLabel();
        jXCollapsiblePane2 = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTableScrollPane = new javax.swing.JScrollPane();
        jVillageTable = new org.jdesktop.swingx.JXTable();
        jDataPanel = new javax.swing.JPanel();
        jAllyScrollPane = new javax.swing.JScrollPane();
        jAllyList = new org.jdesktop.swingx.JXList();
        jTribeScrollPane = new javax.swing.JScrollPane();
        jTribeList = new org.jdesktop.swingx.JXList();
        jContinentScrollPane = new javax.swing.JScrollPane();
        jContinentList = new org.jdesktop.swingx.JXList();
        jVillageScrollPane = new javax.swing.JScrollPane();
        jVillageList = new org.jdesktop.swingx.JXList();
        jXTextField1 = new org.jdesktop.swingx.JXTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jAddAllVillages = new javax.swing.JButton();
        jAddSelectedVillages = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        jDateSourcesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Datenquelle"));
        jDateSourcesPanel.setMinimumSize(new java.awt.Dimension(810, 155));
        jDateSourcesPanel.setPreferredSize(new java.awt.Dimension(815, 155));
        jDateSourcesPanel.setLayout(new java.awt.GridBagLayout());

        buttonGroup1.add(jGroupSource);
        jGroupSource.setText("Gruppenübersicht");
        jGroupSource.setMaximumSize(new java.awt.Dimension(140, 23));
        jGroupSource.setMinimumSize(new java.awt.Dimension(140, 23));
        jGroupSource.setPreferredSize(new java.awt.Dimension(140, 23));
        jGroupSource.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireShowDataSourceInfoEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireHideDataSourceInfoEvent(evt);
            }
        });
        jGroupSource.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireDataSourceChangeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDateSourcesPanel.add(jGroupSource, gridBagConstraints);

        buttonGroup1.add(jDrawingSource);
        jDrawingSource.setText("Zeichnungen");
        jDrawingSource.setMaximumSize(new java.awt.Dimension(140, 23));
        jDrawingSource.setMinimumSize(new java.awt.Dimension(140, 23));
        jDrawingSource.setPreferredSize(new java.awt.Dimension(140, 23));
        jDrawingSource.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireShowDataSourceInfoEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireHideDataSourceInfoEvent(evt);
            }
        });
        jDrawingSource.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireDataSourceChangeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDateSourcesPanel.add(jDrawingSource, gridBagConstraints);

        buttonGroup1.add(jWorlddataSource);
        jWorlddataSource.setText("Alle Weltdaten");
        jWorlddataSource.setMaximumSize(new java.awt.Dimension(140, 23));
        jWorlddataSource.setMinimumSize(new java.awt.Dimension(140, 23));
        jWorlddataSource.setPreferredSize(new java.awt.Dimension(140, 23));
        jWorlddataSource.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireShowDataSourceInfoEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireHideDataSourceInfoEvent(evt);
            }
        });
        jWorlddataSource.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireDataSourceChangeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDateSourcesPanel.add(jWorlddataSource, gridBagConstraints);

        jSetBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireSetSelectionChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDateSourcesPanel.add(jSetBox, gridBagConstraints);

        jSetLabel.setText("Gruppe/Zeichnung");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDateSourcesPanel.add(jSetLabel, gridBagConstraints);

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html");
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        setLayout(new java.awt.GridBagLayout());

        jXCollapsiblePane1.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jXCollapsiblePane1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Informationen ausblenden");
        jLabel1.setToolTipText("Blendet Informationen zu dieser Ansicht und zu den Datenquellen ein/aus");
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel1, gridBagConstraints);

        jXCollapsiblePane2.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(jXCollapsiblePane2, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Datenquellen ausblenden");
        jLabel2.setToolTipText("Blendet die Auswahl möglicher Datenquellen für Stämme, Spieler und Dörfer ein/aus");
        jLabel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideDateSourcesEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabel2, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jTableScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Verwendete Dörfer"));
        jTableScrollPane.setMinimumSize(new java.awt.Dimension(23, 100));
        jTableScrollPane.setPreferredSize(new java.awt.Dimension(23, 100));

        jVillageTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableScrollPane.setViewportView(jVillageTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.7;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanel2.add(jTableScrollPane, gridBagConstraints);

        jDataPanel.setMinimumSize(new java.awt.Dimension(0, 130));
        jDataPanel.setPreferredSize(new java.awt.Dimension(0, 130));
        jDataPanel.setLayout(new java.awt.GridBagLayout());

        jAllyScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Stamm"));
        jAllyScrollPane.setMinimumSize(new java.awt.Dimension(150, 48));
        jAllyScrollPane.setPreferredSize(new java.awt.Dimension(150, 48));

        jAllyList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fireAllySelectionChangedEvent(evt);
            }
        });
        jAllyScrollPane.setViewportView(jAllyList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jDataPanel.add(jAllyScrollPane, gridBagConstraints);

        jTribeScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Spieler"));
        jTribeScrollPane.setMinimumSize(new java.awt.Dimension(150, 44));
        jTribeScrollPane.setPreferredSize(new java.awt.Dimension(150, 44));

        jTribeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fireTribeSelectionEvent(evt);
            }
        });
        jTribeScrollPane.setViewportView(jTribeList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jDataPanel.add(jTribeScrollPane, gridBagConstraints);

        jContinentScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Kontinent"));
        jContinentScrollPane.setMinimumSize(new java.awt.Dimension(80, 60));
        jContinentScrollPane.setPreferredSize(new java.awt.Dimension(80, 60));

        jContinentList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fireContinentSelectionEvent(evt);
            }
        });
        jContinentScrollPane.setViewportView(jContinentList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jDataPanel.add(jContinentScrollPane, gridBagConstraints);

        jVillageScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Dörfer"));
        jVillageScrollPane.setMinimumSize(new java.awt.Dimension(150, 44));
        jVillageScrollPane.setPreferredSize(new java.awt.Dimension(150, 44));

        jVillageScrollPane.setViewportView(jVillageList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jDataPanel.add(jVillageScrollPane, gridBagConstraints);

        jXTextField1.setToolTipText("Erlaubt eine Filterung der Stammesnamen");
        jXTextField1.setPrompt("Filter angeben");
        jXTextField1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireAllyFilterEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDataPanel.add(jXTextField1, gridBagConstraints);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        jDataPanel.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.weighty = 0.3;
        jPanel2.add(jDataPanel, gridBagConstraints);

        jMenuPanel.setMinimumSize(new java.awt.Dimension(110, 130));
        jMenuPanel.setPreferredSize(new java.awt.Dimension(110, 130));
        jMenuPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setMinimumSize(new java.awt.Dimension(123, 70));
        jPanel1.setPreferredSize(new java.awt.Dimension(123, 70));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jAddAllVillages.setText("Alle");
        jAddAllVillages.setToolTipText("<html>F&uuml;gt alle D&ouml;rfer die sich aktuell in der D&ouml;rfer-Liste befinden<br/>\nin die Tabelle der zu verwendenden D&ouml;rfer ein</html>");
        jAddAllVillages.setMaximumSize(new java.awt.Dimension(60, 23));
        jAddAllVillages.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireAddVillagesEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(jAddAllVillages, gridBagConstraints);

        jAddSelectedVillages.setText("Markierte");
        jAddSelectedVillages.setToolTipText("<html>F&uuml;gt alle markierten D&ouml;rfer aus der D&ouml;rfer-Liste<br/>\nin die Tabelle der zu verwendenden D&ouml;rfer ein</html>");
        jAddSelectedVillages.setMaximumSize(new java.awt.Dimension(60, 23));
        jAddSelectedVillages.setMinimumSize(new java.awt.Dimension(60, 23));
        jAddSelectedVillages.setPreferredSize(new java.awt.Dimension(60, 23));
        jAddSelectedVillages.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireAddVillagesEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(jAddSelectedVillages, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.3;
        jPanel2.add(jMenuPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        if (jXCollapsiblePane1.isCollapsed()) {
            jXCollapsiblePane1.setCollapsed(false);
            jLabel1.setText("Informationen ausblenden");
        } else {
            jXCollapsiblePane1.setCollapsed(true);
            jLabel1.setText("Informationen einblenden");
        }
    }//GEN-LAST:event_fireHideInfoEvent
    
    private void fireHideDateSourcesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideDateSourcesEvent
        if (jXCollapsiblePane2.isCollapsed()) {
            jXCollapsiblePane2.setCollapsed(false);
            jLabel2.setText("Datenquellen ausblenden");
        } else {
            jXCollapsiblePane2.setCollapsed(true);
            jLabel2.setText("Datenquellen einblenden");
        }
    }//GEN-LAST:event_fireHideDateSourcesEvent
    
    private void fireDataSourceChangeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireDataSourceChangeEvent
        if (evt.getSource() == jGroupSource) {
            updateSetSelection(TagUtils.getTags(Tag.CASE_INSENSITIVE_ORDER));
        } else if (evt.getSource() == jDrawingSource) {
            List<ManageableType> forms = FormManager.getSingleton().getAllElements();
            updateSetSelection(forms.toArray(new ManageableType[forms.size()]));
        } else if (evt.getSource() == jWorlddataSource) {
            updateSetSelection(null);
        }
    }//GEN-LAST:event_fireDataSourceChangeEvent
    
    private void fireShowDataSourceInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowDataSourceInfoEvent
        if (evt.getSource() == jGroupSource) {
            jInfoTextPane.setText(GROUP_INFO);
        } else if (evt.getSource() == jDrawingSource) {
            jInfoTextPane.setText(DRAWING_INFO);
        } else if (evt.getSource() == jWorlddataSource) {
            jInfoTextPane.setText(WORLDDATA_INFO);
        }
    }//GEN-LAST:event_fireShowDataSourceInfoEvent
    
    private void fireHideDataSourceInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideDataSourceInfoEvent
        jInfoTextPane.setText(GENERAL_INFO);
    }//GEN-LAST:event_fireHideDataSourceInfoEvent
    
    private void fireSetSelectionChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireSetSelectionChangedEvent
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (jGroupSource.isSelected()) {
                updateDataForGroupSource(jSetBox.getSelectedItem());
            } else if (jDrawingSource.isSelected()) {
                updateDataForDrawingSource(jSetBox.getSelectedItem());
            }
        }
    }//GEN-LAST:event_fireSetSelectionChangedEvent
    
    private void fireAllySelectionChangedEvent(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fireAllySelectionChangedEvent
        if (!evt.getValueIsAdjusting()) {
            updateTribeBox();
        }
    }//GEN-LAST:event_fireAllySelectionChangedEvent
    
    private void fireTribeSelectionEvent(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fireTribeSelectionEvent
        if (!evt.getValueIsAdjusting()) {
            updateContinentBox();
        }
    }//GEN-LAST:event_fireTribeSelectionEvent
    
    private void fireContinentSelectionEvent(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fireContinentSelectionEvent
        if (!evt.getValueIsAdjusting()) {
            updateVillageBox();
        }
    }//GEN-LAST:event_fireContinentSelectionEvent
    
    protected void updateSetSelection(Object[] pElements) {
        allowedVillages.clear();
        if (pElements == null) {
            jSetLabel.setEnabled(false);
            jSetBox.setEnabled(false);
            if (!jWorlddataSource.isSelected()) {
                //should be drawing selected but no drawing available
                jSetBox.setModel(new DefaultComboBoxModel(new Object[]{"Nicht verfügbar"}));
            }
            rebuildAllyBox();
        } else {
            jSetLabel.setEnabled(true);
            jSetBox.setEnabled(true);
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            if (pElements.length == 0) {
                jSetLabel.setEnabled(false);
                jSetBox.setEnabled(false);
                model.addElement(NO_DATA_AVAILABLE);
            } else {
                jSetLabel.setEnabled(true);
                jSetBox.setEnabled(true);
                model.addElement(ALL_DATA);
                for (Object set : pElements) {
                    model.addElement(set);
                }
            }
            
            jSetBox.setModel(model);
            jSetBox.setSelectedIndex(0);
            if (jGroupSource.isSelected()) {
                updateDataForGroupSource(pElements[0]);
            } else if (jDrawingSource.isSelected()) {
                updateDataForDrawingSource(pElements[0]);
            }
        }
    }
    
    private void updateDataForGroupSource(Object pSelection) {
        allowedVillages.clear();
        if (pSelection instanceof Tag) {
            Collections.addAll(allowedVillages, VillageUtils.getVillagesByTag((Tag) pSelection, null, Village.CASE_INSENSITIVE_ORDER));
        } else {
            String special = (String) pSelection;
            if (special.equals(ALL_DATA)) {
                Collections.addAll(allowedVillages, VillageUtils.getVillagesByTag(TagUtils.getTags(null), null));
            } else {
                //no data
            }
        }
        rebuildAllyBox();
    }
    
    private void updateDataForDrawingSource(Object pSelection) {
        allowedVillages.clear();
        if (pSelection instanceof AbstractForm) {
        } else {
            String special = (String) pSelection;
            if (special.equals(ALL_DATA)) {
            } else {
                //no data
            }
        }
        rebuildAllyBox();
    }
    
    private void rebuildAllyBox() {
        Ally[] allies = AllyUtils.getAlliesByVillage(allowedVillages.toArray(new Village[allowedVillages.size()]), false, null);
        allies = AllyUtils.filterAllies(allies, jXTextField1.getText(), Ally.CASE_INSENSITIVE_ORDER);
        DefaultListModel model = new DefaultListModel();
        for (Ally a : allies) {
            model.addElement(a);
        }
        jAllyList.setModel(model);
        jAllyList.getSelectionModel().setSelectionInterval(0, 0);
    }
    
    private void updateTribeBox() {
        if (jAllyList.getElementCount() == 0 || jAllyList.getSelectedValues().length == 0) {
            jTribeList.setModel(new DefaultListModel());
        } else {
            Object[] allySelection = jAllyList.getSelectedValues();
            List<Ally> allies = new ArrayList<Ally>();
            for (Object ally : allySelection) {
                allies.add((Ally) ally);
            }
            Village[] villages = VillageUtils.getVillagesByAlly(allies.toArray(new Ally[allies.size()]), null);
            
            List<Village> filtered = new LinkedList<Village>();
            if (!allowedVillages.isEmpty()) {
                for (Village village : villages) {
                    if (allowedVillages.contains(village)) {
                        filtered.add(village);
                    }
                }
            } else {
                Collections.addAll(filtered, villages);
            }
            
            Tribe[] tribes = TribeUtils.getTribeByVillage(filtered.toArray(new Village[filtered.size()]), false, Tribe.CASE_INSENSITIVE_ORDER);
            DefaultListModel model = new DefaultListModel();
            for (Tribe t : tribes) {
                model.addElement(t);
            }
            jTribeList.setModel(model);
            jTribeList.getSelectionModel().setSelectionInterval(0, 0);
        }
    }
    
    private void updateContinentBox() {
        if (jTribeList.getElementCount() == 0 || jTribeList.getSelectedValues().length == 0) {
            jContinentList.setModel(new DefaultListModel());
        } else {
            Object[] tribeSelection = jTribeList.getSelectedValues();
            List<Tribe> tribes = new ArrayList<Tribe>();
            for (Object tribe : tribeSelection) {
                tribes.add((Tribe) tribe);
            }
            
            List<Village> filtered = new ArrayList<Village>();
            Village[] villageByTribes = VillageUtils.getVillages(tribes.toArray(new Tribe[tribes.size()]));
            for (Village village : villageByTribes) {
                if (allowedVillages.isEmpty() || allowedVillages.contains(village)) {
                    filtered.add(village);
                }
            }
            
            String[] continents = VillageUtils.getContinents(filtered.toArray(new Village[filtered.size()]));
            
            DefaultListModel model = new DefaultListModel();
            for (String continent : continents) {
                model.addElement(continent);
            }
            jContinentList.setModel(model);
            jContinentList.getSelectionModel().setSelectionInterval(0, 0);
        }
    }
    
    private void updateVillageBox() {
        if (jContinentList.getElementCount() == 0 || jContinentList.getSelectedValues().length == 0) {
            jVillageList.setModel(new DefaultListModel());
        } else {
            Object[] continentSelection = jContinentList.getSelectedValues();
            List<Integer> continents = new LinkedList<Integer>();
            for (Object continent : continentSelection) {
                String sContinent = (String) continent;
                sContinent = sContinent.replaceFirst("K", "");
                continents.add(Integer.parseInt(sContinent));
            }
            Object[] tribeSelection = jTribeList.getSelectedValues();
            List<Tribe> tribes = new ArrayList<Tribe>();
            for (Object tribe : tribeSelection) {
                tribes.add((Tribe) tribe);
            }
            
            Village[] villagesByTribe = VillageUtils.getVillages(tribes.toArray(new Tribe[tribes.size()]));
            Village[] villages = VillageUtils.getVillagesByContinent(villagesByTribe, continents.toArray(new Integer[continents.size()]), Village.CASE_INSENSITIVE_ORDER);
            
            DefaultListModel model = new DefaultListModel();
            for (Village village : villages) {
                model.addElement(village);
            }
            jVillageList.setModel(model);
            jVillageList.getSelectionModel().setSelectionInterval(0, villages.length - 1);
        }
    }
    
    private void fireAllyFilterEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireAllyFilterEvent
        rebuildAllyBox();
    }//GEN-LAST:event_fireAllyFilterEvent
    
    private void fireAddVillagesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddVillagesEvent
        List<Village> toAdd = new LinkedList<Village>();
        int size = usedVillages.size();
        if (jVillageList.getElementCount() <= 0 || jVillageList.getSelectedValues().length <= 0) {
            return;
        }
        if (evt.getSource() == jAddAllVillages) {
            for (int i = 0; i < jVillageList.getElementCount(); i++) {
                toAdd.add((Village) jVillageList.getElementAt(i));
            }
        } else if (evt.getSource() == jAddSelectedVillages) {
            Object[] values = jVillageList.getSelectedValues();
            for (Object o : values) {
                toAdd.add((Village) o);
            }
        }
        for (Village village : toAdd) {
            if (!usedVillages.contains(village)) {
                usedVillages.add(village);
                supportsPerVillage.put(village, getSupportsForVillage(village));
            }
        }
        
        if (usedVillages.size() != size) {
            updateVillageTable();
        }
    }//GEN-LAST:event_fireAddVillagesEvent
    
    private int getSupportsForVillage(Village pVillage) {
        TroopSplit split = new TroopSplit(pVillage);
        split.update(AnalysePanel.getSingleton().getDefenseAmount(), 10);
        return split.getSplitCount();
    }
    
    private void pasteFromClipboard() {
        String data = "";
        int villagesBefore = usedVillages.size();
        try {
            data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            List<Village> villages = PluginManager.getSingleton().executeVillageParser(data);
            if (!villages.isEmpty()) {
                for (Village village : villages) {
                    if (!usedVillages.contains(village)) {
                        usedVillages.add(village);
                        supportsPerVillage.put(village, getSupportsForVillage(village));
                    }
                }
            }
        } catch (HeadlessException he) {
        } catch (UnsupportedFlavorException ufe) {
        } catch (IOException ioe) {
        }
        
        if (usedVillages.size() != villagesBefore) {
            updateVillageTable();
        }
    }
    
    private void deleteSelection() {
        int[] selection = jVillageTable.getSelectedRows();
        if (selection.length > 0) {
            for (int row : selection) {
                Village toDelete = (Village) jVillageTable.getValueAt(row, 1);
                usedVillages.remove(toDelete);
                supportsPerVillage.remove(toDelete);
            }
        }
        updateVillageTable();
    }
    
    private void updateVillageTable() {
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Spieler", "Dorf", "Einzelverteidigungen"
                }) {
            
            private Class[] types = new Class[]{
                Tribe.class, Village.class, Integer.class
            };
            
            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        for (Village village : usedVillages) {
            model.addRow(new Object[]{village.getTribe(), village, supportsPerVillage.get(village)});
        }
        
        jVillageTable.setModel(model);
    }
    
    public Village[] getUsedVillages() {
        return usedVillages.toArray(new Village[usedVillages.size()]);
    }
    
    public Hashtable<Village, Integer> getSplits() {
        return supportsPerVillage;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jAddAllVillages;
    private javax.swing.JButton jAddSelectedVillages;
    private org.jdesktop.swingx.JXList jAllyList;
    private javax.swing.JScrollPane jAllyScrollPane;
    private org.jdesktop.swingx.JXList jContinentList;
    private javax.swing.JScrollPane jContinentScrollPane;
    private javax.swing.JPanel jDataPanel;
    private javax.swing.JPanel jDateSourcesPanel;
    protected javax.swing.JRadioButton jDrawingSource;
    protected javax.swing.JRadioButton jGroupSource;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jMenuPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    protected javax.swing.JComboBox jSetBox;
    protected javax.swing.JLabel jSetLabel;
    private javax.swing.JScrollPane jTableScrollPane;
    private org.jdesktop.swingx.JXList jTribeList;
    private javax.swing.JScrollPane jTribeScrollPane;
    private org.jdesktop.swingx.JXList jVillageList;
    private javax.swing.JScrollPane jVillageScrollPane;
    private org.jdesktop.swingx.JXTable jVillageTable;
    protected javax.swing.JRadioButton jWorlddataSource;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane2;
    private org.jdesktop.swingx.JXTextField jXTextField1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (!usedVillages.isEmpty()) {
            controller.setProblem(null);
            FilterPanel.getSingleton().updateFilterPanel();
            return WizardPanelNavResult.PROCEED;
        }
        
        controller.setProblem("Keine Dörfer gewählt");
        return WizardPanelNavResult.REMAIN_ON_PAGE;
    }
    
    @Override
    public WizardPanelNavResult allowBack(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;
        
    }
    
    @Override
    public WizardPanelNavResult allowFinish(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;
    }
}
