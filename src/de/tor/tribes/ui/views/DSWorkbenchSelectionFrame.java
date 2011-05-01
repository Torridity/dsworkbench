/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchSelectionFrame.java
 *
 * Created on 25.06.2009, 21:18:43
 */
package de.tor.tribes.ui.views;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.BarbarianAlly;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.NoAlly;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.dnd.VillageTransferable;
import de.tor.tribes.ui.tree.AllyNode;
import de.tor.tribes.ui.tree.NodeCellRenderer;
import de.tor.tribes.ui.tree.SelectionTreeRootNode;
import de.tor.tribes.ui.tree.TagNode;
import de.tor.tribes.ui.tree.TribeNode;
import de.tor.tribes.ui.tree.VillageNode;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.VillageListFormatter;
import de.tor.tribes.util.VillageSelectionListener;
import de.tor.tribes.util.html.SelectionHTMLExporter;
import de.tor.tribes.util.tag.TagManager;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;

/**
 * @TODO (DIFF) Fixed problem when one tag was applied to villages of two users
 * @author Charon
 */
public class DSWorkbenchSelectionFrame extends AbstractDSWorkbenchFrame implements VillageSelectionListener, DragGestureListener {

    private static Logger logger = Logger.getLogger("SelectionFrame");
    private static DSWorkbenchSelectionFrame SINGLETON = null;
    private SelectionTreeRootNode mRoot = null;
    private List<Village> treeData = null;
    private boolean treeMode = true;
    private DragSource dragSource;

    public static synchronized DSWorkbenchSelectionFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchSelectionFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchSelectionFrame */
    DSWorkbenchSelectionFrame() {
        initComponents();
        String format = GlobalOptions.getProperty("village.format");
        if (format == null) {
            format = "%VILLAGE% (%POINTS%)";
        }
        jExportFormatField.setText(format);
        try {
            jAlwaysOnTopBox.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("selection.frame.alwaysOnTop")));
            setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        treeData = new LinkedList<Village>();
        jSelectionTree.setCellRenderer(new NodeCellRenderer());
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(jSelectionTree, // What component
                DnDConstants.ACTION_COPY_OR_MOVE, // What drag types?
                this);// the listener

        buildTree();
        for (MouseListener l : jVillagePointsFilterComboBox.getMouseListeners()) {
            jVillagePointsFilterComboBox.removeMouseListener(l);
        }

        jVillagePointsFilterComboBox.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                fireFilterSelectionByPointsEvent();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        //<editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.selection_tool", GlobalOptions.getHelpBroker().getHelpSet());
        //</editor-fold>
    }

    private void fireFilterSelectionByPointsEvent() {
        int min = 9000;
        switch (jVillagePointsFilterComboBox.getSelectedIndex()) {
            case 0:
                min = 3000;
                break;
            case 1:
                min = 5000;
                break;
            case 2:
                min = 7000;
                break;
            default:
                min = 9000;
        }
        int removed = 0;
        for (Village v : treeData.toArray(new Village[]{})) {
            if (v.getPoints() < min) {
                treeData.remove(v);
                removed++;
            }
        }

        buildTree();
        String message = removed + ((removed == 1) ? " Dorf" : " Dörfer") + " entfernt";
        JOptionPaneHelper.showInformationBox(this, message, "Information");
    }

    public void resetView() {
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
            Hashtable<Tribe, Hashtable<Tag, TagNode>> tagNodes = new Hashtable<Tribe, Hashtable<Tag, TagNode>>();

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

                    Hashtable<Tag, TagNode> nodes = tagNodes.get(t);
                    if (nodes == null) {
                        nodes = new Hashtable<Tag, TagNode>();
                        tagNodes.put(t, nodes);
                    }
                    TagNode tagNode = nodes.get(tag);
                    if (tagNode == null) {
                        //new tribe
                        tagNode = new TagNode(tag);
                        nodes.put(tag, tagNode);
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
        jExportHTMLButton = new javax.swing.JButton();
        jExportFormatField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
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
        jViewTypeButton1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jVillagePointsFilterComboBox = new javax.swing.JComboBox();
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

        jExportHTMLButton.setBackground(new java.awt.Color(239, 235, 223));
        jExportHTMLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_HTML.png"))); // NOI18N
        jExportHTMLButton.setToolTipText("Markierte Elemente als HTML in einer Datei speichern");
        jExportHTMLButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireExportEvent(evt);
            }
        });

        jExportFormatField.setText("%TRIBE% %VILLAGE% %POINTS%");
        jExportFormatField.setToolTipText("<html>\n<b>%TRIBE%</b>: Besitzer<br/>\n<b>%ALLY%</b>: Stamm<br/>\n<b>%VILLAGE%</b>: Dorfname und -koordinaten<br/>\n<b>%X%</b>: X-Koordinate<br/>\n<b>%Y%</b>: Y-Koordinate<br/>\n<b>%POINTS%</b>: Dorfpunkte\n</html>");

        jLabel5.setText("Format");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addGap(30, 30, 30)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jExportHTMLButton)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jExportFormatField, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                    .addComponent(jExportUnformattedButton, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jExportHTMLButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jExportFormatField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(61, 61, 61))
        );

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 119, Short.MAX_VALUE)
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

        jViewTypeButton1.setBackground(new java.awt.Color(239, 235, 223));
        jViewTypeButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/clipboard.png"))); // NOI18N
        jViewTypeButton1.setToolTipText("Dörfer in der Zwischenablage suchen");
        jViewTypeButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireFindVillageInClipboardEvent(evt);
            }
        });

        jLabel6.setText("Punktschwache Dörfer entfernen");

        jVillagePointsFilterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "< 3.000 Punkte", "< 5.000 Punkte", "< 7.000 Punkte", "< 9.000 Punkte", " ", " ", " " }));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jVillagePointsFilterComboBox.setToolTipText(bundle.getString("TribeTribeAttackFrame.jAllTargetsComboBox.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jVillagePointsFilterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jViewTypeButton)
                    .addComponent(jViewTypeButton1))
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
                        .addComponent(jViewTypeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jViewTypeButton1))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jVillagePointsFilterComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                    .addComponent(jLabel6))
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
                boolean bbCode = (evt.getSource() == jExportBBButton);
                String result = VillageListFormatter.format(selection, jExportFormatField.getText(), bbCode);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result.toString()), null);
                JOptionPaneHelper.showInformationBox(this, "Dorfdaten in die Zwischenablage kopiert.", "Daten kopiert");
                GlobalOptions.addProperty("village.format", jExportFormatField.getText());
            } catch (Exception e) {
                logger.error("Failed to copy data to clipboard", e);
                JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren der Daten.", "Fehler");
            }
        }
    }//GEN-LAST:event_fireExportEvent

    private void fireAlwaysOnTopChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopChangedEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAlwaysOnTopChangedEvent

    private void fireSelectRegionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectRegionEvent
        try {
            int xs = (Integer) jXStart.getValue();
            int ys = (Integer) jYStart.getValue();
            int xe = (Integer) jXEnd.getValue();
            int ye = (Integer) jYEnd.getValue();

            fireSelectionFinishedEvent(new Point(xs, ys), new Point(xe, ye));
        } catch (Exception e) {
            logger.error("Error during selection", e);
        }

    }//GEN-LAST:event_fireSelectRegionEvent

    private void fireSwitchViewEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSwitchViewEvent
        treeMode = !treeMode;
        buildTree();
    }//GEN-LAST:event_fireSwitchViewEvent

    private void fireFindVillageInClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireFindVillageInClipboardEvent
        try {
            Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            List<Village> villages = PluginManager.getSingleton().executeVillageParser((String) t.getTransferData(DataFlavor.stringFlavor));
            if (villages == null || villages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this, "Es konnten keine Dorfkoodinaten in der Zwischenablage gefunden werden.", "Information");
                return;
            } else {
                addVillages(villages);
            }
        } catch (Exception e) {
            logger.error("Failed to parse villages from clipboard", e);
        }
    }//GEN-LAST:event_fireFindVillageInClipboardEvent

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
                    if (v.getTribe() == Barbarians.getSingleton() && a.equals(BarbarianAlly.getSingleton())) {
                        //remove barbarian ally member
                        result.add(v);
                    } else if (v.getTribe() != Barbarians.getSingleton() && v.getTribe().getAlly() == null && a.equals(NoAlly.getSingleton())) {
                        //remove no-ally member
                        result.add(v);
                    } else if (v.getTribe() != Barbarians.getSingleton() && v.getTribe().getAlly() != null && a.equals(v.getTribe().getAlly())) {
                        //remove if ally is equal
                        result.add(v);
                    }
                }
            } else if (o instanceof TribeNode) {
                Tribe t = ((TribeNode) o).getUserObject();
                Village[] copy = treeData.toArray(new Village[]{});
                for (Village v : copy) {
                    if (v.getTribe() == Barbarians.getSingleton() && t.equals(Barbarians.getSingleton())) {
                        //if village is barbarian village and selected tribe are barbs, remove village
                        result.add(v);
                    } else if (v.getTribe() != Barbarians.getSingleton() && v.getTribe().equals(t)) {
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

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
        addVillages(pVillages);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jExportBBButton;
    private javax.swing.JTextField jExportFormatField;
    private javax.swing.JButton jExportHTMLButton;
    private javax.swing.JButton jExportUnformattedButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jSelectionTree;
    private javax.swing.JButton jViewTypeButton;
    private javax.swing.JButton jViewTypeButton1;
    private javax.swing.JComboBox jVillagePointsFilterComboBox;
    private javax.swing.JSpinner jXEnd;
    private javax.swing.JSpinner jXStart;
    private javax.swing.JSpinner jYEnd;
    private javax.swing.JSpinner jYStart;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireSelectionFinishedEvent(Point pStart, Point pEnd) {
        addVillages(DataHolder.getSingleton().getVillagesInRegion(pStart, pEnd));
    }

    public void addVillages(List<Village> pVillages) {
        boolean showBarbarian = true;
        try {
            showBarbarian = Boolean.parseBoolean(GlobalOptions.getProperty("show.barbarian"));
        } catch (Exception e) {
            showBarbarian = true;
        }
        for (Village v : pVillages.toArray(new Village[]{})) {
            if ((v != null && v.getTribe() == Barbarians.getSingleton()) && !showBarbarian) {
                //dont select barbarians if they are not visible
            } else {
                if (v != null && !treeData.contains(v)) {
                    treeData.add(v);
                }
            }
        }
        Collections.sort(treeData, Village.ALLY_TRIBE_VILLAGE_COMPARATOR);
        buildTree();
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        List<Village> v = getSelectedElements();
        if (v == null) {
            return;
        }
        Cursor c = null;
        if (!v.isEmpty()) {
            c = ImageManager.createVillageDragCursor(v.size());
            setCursor(c);
            dge.startDrag(c, new VillageTransferable(v), this);
        }
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        setCursor(Cursor.getDefaultCursor());
    }
}
