/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchSelectionFrame.java
 *
 * Created on 25.06.2009, 21:18:43
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.BarbarianAlly;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.NoAlly;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.tree.AllyNode;
import de.tor.tribes.ui.tree.NodeCellRenderer;
import de.tor.tribes.ui.tree.SelectionTreeRootNode;
import de.tor.tribes.ui.tree.TagNode;
import de.tor.tribes.ui.tree.TribeNode;
import de.tor.tribes.ui.tree.VillageNode;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.VillageSelectionListener;
import de.tor.tribes.util.html.SelectionHTMLExporter;
import de.tor.tribes.util.tag.TagManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;

/**
 *@TODO (DIFF) Manual region selection added
 *@TODO (DIFF) Barbarians are not selected if not visible
 * @author Charon
 */
public class DSWorkbenchSelectionFrame extends AbstractDSWorkbenchFrame implements VillageSelectionListener {

    private static Logger logger = Logger.getLogger("SelectionFrame");
    private static DSWorkbenchSelectionFrame SINGLETON = null;
    private SelectionTreeRootNode mRoot = null;
    private List<Village> treeData = null;
    private boolean treeMode = true;

    public static synchronized DSWorkbenchSelectionFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchSelectionFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchSelectionFrame */
    DSWorkbenchSelectionFrame() {
        initComponents();
        try {
            jAlwaysOnTopBox.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("selection.frame.alwaysOnTop")));
            setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        treeData = new LinkedList<Village>();
        jSelectionTree.setCellRenderer(new NodeCellRenderer());
        buildTree();
        //<editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.selection_tool", GlobalOptions.getHelpBroker().getHelpSet());
    //</editor-fold>
    }

    public void clear() {
        treeData.clear();
        buildTree();
    }

    private void buildTree() {
        mRoot = new SelectionTreeRootNode("Auswahl");
        if (treeMode) {
            //tree view
            //add all villages
            Hashtable<Ally, AllyNode> allyNodes = new Hashtable<Ally, AllyNode>();
            Hashtable<Tribe, TribeNode> tribeNodes = new Hashtable<Tribe, TribeNode>();
            Hashtable<Tag, TagNode> tagNodes = new Hashtable<Tag, TagNode>();
            List<Village> used = new LinkedList<Village>();

            for (Village v : treeData) {
                Tribe t = v.getTribe();
                if (t == null) {
                    t = Barbarians.getSingleton();
                }
                Ally a = t.getAlly();
                if (a == null) {
                    a = NoAlly.getSingleton();
                }

                AllyNode aNode = allyNodes.get(a);
                if (aNode == null) {
                    //new ally
                    aNode = new AllyNode(a);
                    allyNodes.put(a, aNode);
                    mRoot.add(aNode);
                }
                TribeNode tNode = tribeNodes.get(t);
                if (tNode == null) {
                    //new tribe
                    tNode = new TribeNode(t);
                    tribeNodes.put(t, tNode);
                    aNode.add(tNode);
                }
                boolean hasTag = false;
                for (Tag tag : TagManager.getSingleton().getTags(v)) {
                    hasTag = true;
                    TagNode tagNode = tagNodes.get(tag);
                    if (tagNode == null) {
                        //new tribe
                        tagNode = new TagNode(tag);
                        tagNodes.put(tag, tagNode);
                        tNode.add(tagNode);
                    }
                    tagNode.add(new VillageNode(v));
                }

                if (!hasTag) {
                    //only add directly if not added to any tag node
                    tNode.add(new VillageNode(v));
                }
                used.add(v);
            }

        } else {
            //simple view
            for (Village v : treeData) {
                mRoot.add(new VillageNode(v));
            }
        }
        ((DefaultTreeModel) jSelectionTree.getModel()).setRoot(mRoot);
    //jSelectionTree.setCellRenderer(new NodeCellRenderer());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSelectionTree = new javax.swing.JTree();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jExportUnformattedButton = new javax.swing.JButton();
        jExportBBButton = new javax.swing.JButton();
        jExportOwnerBox = new javax.swing.JCheckBox();
        jExportAllyBox = new javax.swing.JCheckBox();
        jExportPointsBox = new javax.swing.JCheckBox();
        jExportHTMLButton = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jXStart = new javax.swing.JSpinner();
        jYStart = new javax.swing.JSpinner();
        jXEnd = new javax.swing.JSpinner();
        jYEnd = new javax.swing.JSpinner();
        jButton2 = new javax.swing.JButton();
        jViewTypeButton = new javax.swing.JButton();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();

        setTitle("Auswahl");

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Auswahl (2 Stämme)");
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Stamm1 (2 Spieler)");
        javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Spieler1 (3 Dörfer)");
        javax.swing.tree.DefaultMutableTreeNode treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Dorf1");
        treeNode3.add(treeNode4);
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Dorf2");
        treeNode3.add(treeNode4);
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Dorf3");
        treeNode3.add(treeNode4);
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Spieler2 (2 Dörfer)");
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Dorf1");
        treeNode3.add(treeNode4);
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Dorf2");
        treeNode3.add(treeNode4);
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Stamm2 (1 Spieler)");
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Spieler1 (2 Dörfer)");
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Dorf1");
        treeNode3.add(treeNode4);
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Dorf2");
        treeNode3.add(treeNode4);
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        jSelectionTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(jSelectionTree);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton1.setToolTipText("Gewählte Elemente löschen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveNodeEvent(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Export"));
        jPanel2.setOpaque(false);

        jExportUnformattedButton.setBackground(new java.awt.Color(239, 235, 223));
        jExportUnformattedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboard.png"))); // NOI18N
        jExportUnformattedButton.setToolTipText("Markierte Elemente unformatiert in die Zwischenablage kopieren");
        jExportUnformattedButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireExportEvent(evt);
            }
        });

        jExportBBButton.setBackground(new java.awt.Color(239, 235, 223));
        jExportBBButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboardBB.png"))); // NOI18N
        jExportBBButton.setToolTipText("Markierte Elemente als BB-Codes in die Zwischenablage kopieren");
        jExportBBButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireExportEvent(evt);
            }
        });

        jExportOwnerBox.setText("Besitzer");
        jExportOwnerBox.setToolTipText("Besitzer exportieren");
        jExportOwnerBox.setOpaque(false);

        jExportAllyBox.setText("Stamm");
        jExportAllyBox.setToolTipText("Stammname exportieren");
        jExportAllyBox.setOpaque(false);

        jExportPointsBox.setText("Dorfpunkte");
        jExportPointsBox.setToolTipText("Dorfpunkte exportieren");
        jExportPointsBox.setOpaque(false);

        jExportHTMLButton.setBackground(new java.awt.Color(239, 235, 223));
        jExportHTMLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_HTML.png"))); // NOI18N
        jExportHTMLButton.setToolTipText("Markierte Elemente als HTML in einer Datei speichern");
        jExportHTMLButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireExportEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jExportHTMLButton)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jExportOwnerBox)
                        .addGap(18, 18, 18)
                        .addComponent(jExportAllyBox)
                        .addGap(18, 18, 18)
                        .addComponent(jExportPointsBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                        .addComponent(jExportUnformattedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jExportBBButton)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jExportBBButton, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jExportOwnerBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jExportUnformattedButton)
                        .addComponent(jExportAllyBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jExportPointsBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jExportHTMLButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jButton4.setBackground(new java.awt.Color(239, 235, 223));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_overview.png"))); // NOI18N
        jButton4.setToolTipText("Markierte Elemente in Angriffsplaner übertragen");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectionToAttackPlannerEvent(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Bereichsauswahl"));
        jPanel3.setOpaque(false);

        jLabel1.setText("X-Start");

        jLabel2.setText("Y-Start");

        jLabel3.setText("X-Ende");

        jLabel4.setText("Y-Ende");

        jXStart.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        jXStart.setMaximumSize(new java.awt.Dimension(60, 18));

        jYStart.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        jYStart.setMaximumSize(new java.awt.Dimension(60, 18));

        jXEnd.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        jXEnd.setMaximumSize(new java.awt.Dimension(60, 18));

        jYEnd.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        jYEnd.setMaximumSize(new java.awt.Dimension(60, 18));

        jButton2.setBackground(new java.awt.Color(239, 235, 223));
        jButton2.setText("Wählen");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectRegionEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jXStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jYStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 87, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jYEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jXEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jXStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jYStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(jXEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jYEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel4)))
                        .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jViewTypeButton.setBackground(new java.awt.Color(239, 235, 223));
        jViewTypeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/branch.png"))); // NOI18N
        jViewTypeButton.setToolTipText("Baumstruktur an/aus");
        jViewTypeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSwitchViewEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1)
                    .addComponent(jButton4)
                    .addComponent(jViewTypeButton))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jViewTypeButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAlwaysOnTopChangedEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAlwaysOnTopBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jAlwaysOnTopBox)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireRemoveNodeEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveNodeEvent

        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Alle markierten Einträge und ihre untergeordneten Einträge löschen?", "Löschen", "Nein", "Ja") == JOptionPane.NO_OPTION) {
            return;
        }

        for (Village v : getSelectedElements()) {
            treeData.remove(v);
        }
        buildTree();
    }//GEN-LAST:event_fireRemoveNodeEvent

    private void fireExportEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireExportEvent
        List<Village> selection = new LinkedList<Village>();
        for (Village v : getSelectedElements()) {
            selection.add(v);
        }

        //check if sth is selected
        if (selection.isEmpty()) {
            JOptionPaneHelper.showInformationBox(this, "Keine Elemente ausgewählt.", "Fehler");
            return;
        }

        if (evt.getSource() == jExportHTMLButton) {
            //do HTML export
            String dir = GlobalOptions.getProperty("screen.dir");
            if (dir == null) {
                dir = ".";
            }

            JFileChooser chooser = null;
            try {
                //handle vista problem
                chooser = new JFileChooser(dir);
            } catch (Exception e) {
                JOptionPaneHelper.showErrorBox(this, "Konnte Dateiauswahldialog nicht öffnen.\nMöglicherweise verwendest du Windows Vista. Ist dies der Fall, beende DS Workbench, klicke mit der rechten Maustaste auf DSWorkbench.exe,\n" +
                        "wähle 'Eigenschaften' und deaktiviere dort unter 'Kompatibilität' den Windows XP Kompatibilitätsmodus.", "Fehler");
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
            //open dialog
            chooser.setSelectedFile(new File(dir + "/Dorfliste.html"));
            int ret = chooser.showSaveDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    //check extension
                    File f = chooser.getSelectedFile();
                    String file = f.getCanonicalPath();
                    if (!file.endsWith(".html")) {
                        file += ".html";
                    }

                    //check overwrite
                    File target = new File(file);
                    if (target.exists()) {
                        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Bestehende Datei überschreiben?", "Überschreiben", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                            //do not overwrite
                            return;
                        }
                    }
                    //do export
                    SelectionHTMLExporter.doExport(target, selection);
                    GlobalOptions.addProperty("screen.dir", target.getParent());
                    if (JOptionPaneHelper.showQuestionConfirmBox(this, "Auswahl erfolgreich gespeichert.\nWillst du die erstellte Datei jetzt im Browser betrachten?", "Information", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                        BrowserCommandSender.openPage(target.toURI().toURL().toString());
                    }
                } catch (Exception inner) {
                    logger.error("Failed to write selection to HTML", inner);
                    JOptionPaneHelper.showErrorBox(this, "Fehler beim Speichern.", "Fehler");
                }
            }
        } else {
            //export to clipboard
            try {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumFractionDigits(0);
                nf.setMaximumFractionDigits(0);
                boolean exported = false;
                if (evt.getSource() == jExportBBButton) {
                    StringBuffer result = new StringBuffer();

                    for (Village v : selection) {
                        exported = true;
                        result.append(v.toBBCode());
                        if (jExportPointsBox.isSelected()) {
                            result.append(" (" + nf.format(v.getPoints()) + ") ");
                        } else {
                            result.append("\t");
                        }
                        if (jExportOwnerBox.isSelected() && v.getTribe() != null) {
                            result.append(v.getTribe().toBBCode() + " ");
                        } else {
                            if (jExportOwnerBox.isSelected()) {
                                result.append("Barbaren ");
                            } else {
                                result.append("\t");
                            }
                        }
                        if (jExportAllyBox.isSelected() && v.getTribe() != null && v.getTribe().getAlly() != null) {
                            result.append(v.getTribe().getAlly().toBBCode() + "\n");
                        } else {
                            if (jExportAllyBox.isSelected()) {
                                result.append("(kein Stamm)\n");
                            } else {
                                result.append("\n");
                            }
                        }
                    }
                    if (exported) {
                        String res = result.toString();
                        StringTokenizer t = new StringTokenizer(res, "[");
                        int cnt = t.countTokens();
                        boolean doExport = true;
                        if (cnt > 500) {
                            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Dörfer benötigen mehr als 500 BB-Codes\n" +
                                    "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                                doExport = false;
                            }
                        }
                        if (doExport) {
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(res), null);
                            JOptionPaneHelper.showInformationBox(this, "Dorfdaten in die Zwischenablage kopiert.", "Daten kopiert");
                        }
                    } else {
                        JOptionPaneHelper.showInformationBox(this, "Mit den gewählten Einstellungen werden keine Dörfer kopiert.", "Information");
                        return;
                    }
                } else if (evt.getSource() == jExportUnformattedButton) {
                    StringBuffer result = new StringBuffer();
                    for (Village v : selection) {
                        exported = true;
                        result.append(v + "\t");
                        if (jExportPointsBox.isSelected()) {
                            result.append(nf.format(v.getPoints()) + "\t");
                        } else {
                            result.append("\t");
                        }
                        if (jExportOwnerBox.isSelected() && v.getTribe() != null) {
                            result.append(v.getTribe() + "\t");
                        } else {
                            if (jExportOwnerBox.isSelected()) {
                                result.append("Barbaren\t");
                            } else {
                                result.append("\t");
                            }
                        }
                        if (jExportAllyBox.isSelected() && v.getTribe() != null && v.getTribe().getAlly() != null) {
                            result.append(v.getTribe().getAlly() + "\n");
                        } else {
                            if (jExportAllyBox.isSelected()) {
                                result.append("(kein Stamm)\n");
                            } else {
                                result.append("\n");
                            }
                        }
                    }
                    if (exported) {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result.toString()), null);
                        JOptionPaneHelper.showInformationBox(this, "Dorfdaten in die Zwischenablage kopiert.", "Daten kopiert");
                    } else {
                        JOptionPaneHelper.showInformationBox(this, "Mit den gewählten Einstellungen werden keine Dörfer kopiert.", "Information");
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to copy data to clipboard", e);
                JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren der Daten.", "Fehler");
            }
        }
    }//GEN-LAST:event_fireExportEvent

    private void fireAlwaysOnTopChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopChangedEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAlwaysOnTopChangedEvent

    private void fireSelectionToAttackPlannerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectionToAttackPlannerEvent
        DSWorkbenchMainFrame.getSingleton().getAttackPlaner().fireSelectionTransferEvent(getSelectedElements());
    }//GEN-LAST:event_fireSelectionToAttackPlannerEvent

    private void fireSelectRegionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectRegionEvent
        try {
            int xs = (Integer) jXStart.getValue();
            int ys = (Integer) jYStart.getValue();
            int xe = (Integer) jXEnd.getValue();
            int ye = (Integer) jYEnd.getValue();

            fireSelectionFinishedEvent(new Point(xs, ys), new Point(xe, ye));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_fireSelectRegionEvent

    private void fireSwitchViewEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSwitchViewEvent
        treeMode = !treeMode;
        buildTree();
    }//GEN-LAST:event_fireSwitchViewEvent

    public List<Village> getSelectedElements() {
        TreePath[] paths = jSelectionTree.getSelectionModel().getSelectionPaths();
        List<Village> result = new LinkedList<Village>();
        if (paths == null) {
            return result;
        }
        for (TreePath p : paths) {
            Object o = p.getLastPathComponent();
            if (o instanceof AllyNode) {
                Ally a = ((AllyNode) o).getUserObject();
                Village[] copy = treeData.toArray(new Village[]{});
                for (Village v : copy) {
                    if (v.getTribe() == null && a.equals(BarbarianAlly.getSingleton())) {
                        //remove barbarian ally member
                        result.add(v);
                    } else if (v.getTribe() != null && v.getTribe().getAlly() == null && a.equals(NoAlly.getSingleton())) {
                        //remove no-ally member
                        result.add(v);
                    } else if (v.getTribe() != null && v.getTribe().getAlly() != null && a.equals(v.getTribe().getAlly())) {
                        //remove if ally is equal
                        result.add(v);
                    }
                }
            } else if (o instanceof TribeNode) {
                Tribe t = ((TribeNode) o).getUserObject();
                Village[] copy = treeData.toArray(new Village[]{});
                for (Village v : copy) {
                    if (v.getTribe() == null && t.equals(Barbarians.getSingleton())) {
                        //if village is barbarian village and selected tribe are barbs, remove village
                        result.add(v);
                    } else if (v.getTribe() != null && v.getTribe().equals(t)) {
                        //selected tribe are no barbs, so check tribes to be equal
                        result.add(v);
                    }
                }
            } else if (o instanceof TagNode) {
                Tag t = ((TagNode) o).getUserObject();
                Village[] copy = treeData.toArray(new Village[]{});
                for (Village v : copy) {
                    if (v != null && t != null && t.tagsVillage(v.getId())) {
                        result.add(v);
                    }
                }
            } else if (o instanceof VillageNode) {
                Village v = ((VillageNode) o).getUserObject();
                result.add(v);
            } else if (o != null && o.equals(mRoot)) {
                //remove all
                result = new LinkedList<Village>(treeData);
                //nothing more than everything can be removed
                return result;
            } else {
                //remove nothing
            }
        }
        return result;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchSelectionFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBox jExportAllyBox;
    private javax.swing.JButton jExportBBButton;
    private javax.swing.JButton jExportHTMLButton;
    private javax.swing.JCheckBox jExportOwnerBox;
    private javax.swing.JCheckBox jExportPointsBox;
    private javax.swing.JButton jExportUnformattedButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jSelectionTree;
    private javax.swing.JButton jViewTypeButton;
    private javax.swing.JSpinner jXEnd;
    private javax.swing.JSpinner jXStart;
    private javax.swing.JSpinner jYEnd;
    private javax.swing.JSpinner jYStart;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireSelectionFinishedEvent(Point pStart, Point pEnd) {
        try {
            int xStart = (pStart.x < pEnd.x) ? pStart.x : pEnd.x;
            int xEnd = (pEnd.x > pStart.x) ? pEnd.x : pStart.x;
            int yStart = (pStart.y < pEnd.y) ? pStart.y : pEnd.y;
            int yEnd = (pEnd.y > pStart.y) ? pEnd.y : pStart.y;
            boolean showBarbarian = true;
            try {
                showBarbarian = Boolean.parseBoolean(GlobalOptions.getProperty("show.barbarian"));
            } catch (Exception e) {
                showBarbarian = true;
            }

            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
                    Village v = DataHolder.getSingleton().getVillages()[x][y];
                    if ((v != null && v.getTribe() == null) && !showBarbarian) {
                        //dont select barbarians if they are not visible
                    } else {
                        if (v != null && !treeData.contains(v)) {
                            treeData.add(v);
                        }
                    }
                }
            }
            Collections.sort(treeData, Village.ALLY_TRIBE_VILLAGE_COMPARATOR);
            buildTree();
        } catch (Exception e) {
            //occurs if no rect was opened by selection tool -> ignore
        }
    }
}









