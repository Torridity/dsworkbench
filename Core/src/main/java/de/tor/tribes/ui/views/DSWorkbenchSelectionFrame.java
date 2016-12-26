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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.BarbarianAlly;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.types.test.DummyProfile;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.tree.AllyNode;
import de.tor.tribes.ui.tree.NodeCellRenderer;
import de.tor.tribes.ui.tree.SelectionTreeRootNode;
import de.tor.tribes.ui.tree.TagNode;
import de.tor.tribes.ui.tree.TribeNode;
import de.tor.tribes.ui.tree.VillageNode;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.UIHelper;
import de.tor.tribes.util.interfaces.VillageSelectionListener;
import de.tor.tribes.util.bb.VillageListFormatter;
import de.tor.tribes.util.html.SelectionHTMLExporter;
import de.tor.tribes.util.tag.TagManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * @author Charon
 */
public class DSWorkbenchSelectionFrame extends AbstractDSWorkbenchFrame implements VillageSelectionListener, ActionListener, TreeSelectionListener {

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        List<Village> selection = getSelectedElements();
        if (selection != null && !selection.isEmpty()) {
            showInfo(((selection.size() == 1) ? "Ein Dorf" : selection.size() + " Dörfer") + " ausgewählt");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Copy")) {
            copySelectionToInternalClipboard();
        } else if (e.getActionCommand().equals("BBCopy")) {
            copyBBToExternalClipboardEvent();
        } else if (e.getActionCommand().equals("Cut")) {
            cutSelectionToInternalClipboard();
        } else if (e.getActionCommand().equals("Paste")) {
            pasteFromInternalClipboard();
        } else if (e.getActionCommand().equals("Delete")) {
            removeSelection();
        }

    }
    private static Logger logger = Logger.getLogger("SelectionFrame");
    private static DSWorkbenchSelectionFrame SINGLETON = null;
    private SelectionTreeRootNode mRoot = null;
    private List<Village> treeData = null;
    private boolean treeMode = true;
    private GenericTestPanel centerPanel = null;

    public static synchronized DSWorkbenchSelectionFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchSelectionFrame();
        }
        return SINGLETON;
    }

    /**
     * Creates new form DSWorkbenchSelectionFrame
     */
    DSWorkbenchSelectionFrame() {
        initComponents();
        centerPanel = new GenericTestPanel(true);
        jSelectionPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jSelectionTreePanel);
        buildMenu();
        capabilityInfoPanel1.addActionListener(this);
        treeData = new LinkedList<>();
        jSelectionTree.setCellRenderer(new NodeCellRenderer());

        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false);
        jSelectionTree.registerKeyboardAction(DSWorkbenchSelectionFrame.this, "Copy", copy, JComponent.WHEN_FOCUSED);
        jSelectionTree.registerKeyboardAction(DSWorkbenchSelectionFrame.this, "BBCopy", bbCopy, JComponent.WHEN_FOCUSED);
        jSelectionTree.registerKeyboardAction(DSWorkbenchSelectionFrame.this, "Delete", delete, JComponent.WHEN_FOCUSED);
        jSelectionTree.registerKeyboardAction(DSWorkbenchSelectionFrame.this, "Paste", paste, JComponent.WHEN_FOCUSED);
        jSelectionTree.registerKeyboardAction(DSWorkbenchSelectionFrame.this, "Cut", cut, JComponent.WHEN_FOCUSED);
        jSelectionTree.getActionMap().put("find", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //ignore find
            }
        });
        jSelectionTree.getSelectionModel().addTreeSelectionListener(DSWorkbenchSelectionFrame.this);

        buildTree();

        //<editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.selection_tool", GlobalOptions.getHelpBroker().getHelpSet());
        }
        //</editor-fold>
    }

    @Override
    public void toBack() {
        jAlwaysOnTopBox.setSelected(false);
        fireAlwaysOnTopChangedEvent(null);
        super.toBack();
    }

    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTopBox.isSelected());

    }

    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));
        try {
            jAlwaysOnTopBox.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }

        setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
    }

    public String getPropertyPrefix() {
        return "selection.view";
    }

    private void filterByPoints(int pPoints) {
        int selected = 0;
        jSelectionTree.getSelectionModel().clearSelection();
        for (Village v : treeData.toArray(new Village[]{})) {
            if (v.getPoints() < pPoints) {
                TreePath p = findByName(jSelectionTree, v);
                if (p != null) {
                    jSelectionTree.getSelectionModel().addSelectionPath(p);
                    selected++;
                }
            }
        }
        jSelectionTree.requestFocus();
        showInfo(((selected == 1) ? "Ein Dorf" : selected + " Dörfer") + " ausgewählt");
    }

    public TreePath findByName(JTree tree, Village node) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        return find2(tree, new TreePath(root), node, 0);
    }

    private TreePath find2(JTree tree, TreePath parent, Village pNode, int depth) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        DefaultMutableTreeNode o = (DefaultMutableTreeNode) node;

        // If equal, go down the branch
        if (o.getUserObject().equals(pNode)) {
            // If at end, return match
            return parent;
        } else {
            // Traverse children
            if (node.getChildCount() >= 0) {
                for (Enumeration e = node.children(); e.hasMoreElements();) {
                    TreeNode n = (TreeNode) e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    TreePath result = find2(tree, path, pNode, depth + 1);
                    // Found a match
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        // No match at this branch
        return null;
    }

    @Override
    public void resetView() {
        treeData.clear();
        buildTree();
    }

    private void buildTree() {
        mRoot = new SelectionTreeRootNode("Auswahl");
        if (treeMode) {
            //tree view
            //add all villages
            Hashtable<Ally, AllyNode> allyNodes = new Hashtable<>();
            Hashtable<Tribe, TribeNode> tribeNodes = new Hashtable<>();
            Hashtable<Tribe, Hashtable<Tag, TagNode>> tagNodes = new Hashtable<>();

            List<Village> used = new LinkedList<>();

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
                        nodes = new Hashtable<>();
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

        jSelectionTree.setModel(new DefaultTreeModel(mRoot));
    }

    private void buildMenu() {
        JXTaskPane editPane = new JXTaskPane();
        editPane.setTitle("Bearbeiten");
        JXButton filter3k = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/3k.png")));
        filter3k.setToolTipText("Wählt alle Dörfer mit weniger als 3000 Punkten");
        filter3k.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                filterByPoints(3000);
            }
        });

        editPane.getContentPane().add(filter3k);

        JXButton filter5k = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/5k.png")));

        filter5k.setToolTipText("Wählt alle Dörfer mit weniger als 5000 Punkten");
        filter5k.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                filterByPoints(5000);
            }
        });

        editPane.getContentPane().add(filter5k);
        JXButton filter7k = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/7k.png")));

        filter7k.setToolTipText("Wählt alle Dörfer mit weniger als 7000 Punkten");
        filter7k.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                filterByPoints(7000);
            }
        });

        editPane.getContentPane().add(filter7k);

        JXButton filter9k = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/9k.png")));

        filter9k.setToolTipText("Wählt alle Dörfer mit weniger als 9000 Punkten");
        filter9k.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                filterByPoints(9000);
            }
        });

        editPane.getContentPane().add(filter9k);

        jApplyCustomFilter.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    filterByPoints(UIHelper.parseIntFromField(jCustomPointsField, 1500));
                } catch (NumberFormatException nfe) {
                    jCustomPointsField.setText(null);
                }
            }
        });

        editPane.getContentPane().add(jCustomPointsPanel);

        JXTaskPane transferPane = new JXTaskPane();
        transferPane.setTitle("Übertragen");
        JXButton toHtml = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/att_HTML.png")));

        toHtml.setToolTipText("Gewählte Dörfer als HTML Datei exportieren");
        toHtml.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                exportAsHTML();
            }
        });
        transferPane.getContentPane().add(toHtml);

        JXTaskPane miscPane = new JXTaskPane();
        miscPane.setTitle("Sonstiges");
        JXButton structure = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/branch.png")));

        structure.setToolTipText("Wechsel zwischen Baumstruktur und Liste");
        structure.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                switchViewType();
            }
        });
        miscPane.getContentPane().add(structure);
        JXButton region = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/region_select.png")));

        region.setToolTipText("Auswahl aller Dörfer innerhalb bestimmter Koordinaten");
        region.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                showRegionSelection();
            }
        });
        miscPane.getContentPane().add(region);

        JXButton substract = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/branch_remove.png")));

        substract.setToolTipText("Abziehen aller Dörfer aus der Zwischenablage von der Liste der ausgewählten Dörfer");
        substract.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                substractVillagesFromClipboard();
            }
        });
        miscPane.getContentPane().add(substract);


        centerPanel.setupTaskPane(editPane, transferPane, miscPane);
    }

    public void showInfo(String pMessage) {
        infoPanel.setCollapsed(false);
        jXInfoLabel.setBackgroundPainter(new MattePainter(getBackground()));
        jXInfoLabel.setForeground(Color.BLACK);
        jXInfoLabel.setText(pMessage);
    }

    public void showSuccess(String pMessage) {
        infoPanel.setCollapsed(false);
        jXInfoLabel.setBackgroundPainter(new MattePainter(Color.GREEN));
        jXInfoLabel.setForeground(Color.BLACK);
        jXInfoLabel.setText(pMessage);
    }

    public void showError(String pMessage) {
        infoPanel.setCollapsed(false);
        jXInfoLabel.setBackgroundPainter(new MattePainter(Color.RED));
        jXInfoLabel.setForeground(Color.WHITE);
        jXInfoLabel.setText(pMessage);
    }

    private boolean copySelectionToInternalClipboard() {
        List<Village> selection = getSelectedElements();
        if (selection.isEmpty()) {
            showInfo("Kein Dorf ausgewählt");
            return false;
        }

        StringBuilder b = new StringBuilder();
        for (Village v : selection) {
            b.append(v.getCoordAsString()).append("\n");
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
        } catch (HeadlessException he) {
            logger.error("Failed to copy data to clipboard", he);
            showError("Fehler beim Kopieren in die Zwischenablage");
        }
        showSuccess(((selection.size() == 1) ? "Dorf" : selection.size() + " Dörfer") + " in kopiert");
        return true;
    }

    private void cutSelectionToInternalClipboard() {
        List<Village> selection = getSelectedElements();
        if (copySelectionToInternalClipboard()) {
            removeSelection(false);
            showSuccess(((selection.size() == 1) ? "Dorf" : selection.size() + " Dörfer") + " ausgeschnitten");
        }
    }

    private void pasteFromInternalClipboard() {
        List<Village> villages = null;
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            villages = PluginManager.getSingleton().executeVillageParser((String) t.getTransferData(DataFlavor.stringFlavor));
        } catch (Exception e) {
            logger.error("Failed to read village data from clipboard", e);
            showError("Fehler beim Lesen aus der Zwischenablage");
        }

        if (villages == null || villages.isEmpty()) {
            showInfo("Keine Dörfer in der Zwischenablage gefunden");
        } else {
            addVillages(villages);
            showSuccess(((villages.size() == 1) ? "Dorf" : villages.size() + " Dörfer") + " eingefügt");
        }
    }

    private void copyBBToExternalClipboardEvent() {
        try {
            List<Village> selection = getSelectedElements();
            if (selection.isEmpty()) {
                showInfo("Keine Elemente ausgewählt");
                return;
            }
            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

            StringBuilder buffer = new StringBuilder();
            if (extended) {
                buffer.append("[u][size=12]Dorfliste[/size][/u]\n\n");
            } else {
                buffer.append("[u]Dorfliste[/u]\n\n");
            }
            buffer.append(new VillageListFormatter().formatElements(selection, extended));

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
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Dörfer benötigen mehr als 1000 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
            showSuccess("Daten in Zwischenablage kopiert");
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            showError("Fehler beim Kopieren in die Zwischenablage");
        }
    }

    private void removeSelection() {
        removeSelection(true);
    }

    private void removeSelection(boolean pAsk) {
        if (pAsk && JOptionPaneHelper.showQuestionConfirmBox(this, "Alle markierten Einträge und ihre untergeordneten Einträge löschen?", "Löschen", "Nein", "Ja") == JOptionPane.NO_OPTION) {
            return;
        }
        int cnt = 0;
        for (Village v : getSelectedElements()) {
            treeData.remove(v);
            cnt++;
        }
        buildTree();
        showSuccess(((cnt == 1) ? "Dorf" : cnt + " Dörfer") + " gelöscht");
    }

    private void exportAsHTML() {
        List<Village> selection = getSelectedElements();
        if (selection.isEmpty()) {
            showInfo("Keine Dörfer ausgewählt");
            return;
        }
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
                return (f != null) && (f.isDirectory() || f.getName().endsWith(".html"));
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
                showSuccess("Auswahl erfolgreich gespeichert");
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Willst du die erstellte Datei jetzt im Browser betrachten?", "Information", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                    BrowserCommandSender.openPage(target.toURI().toURL().toString());
                }
            } catch (Exception inner) {
                logger.error("Failed to write selection to HTML", inner);
                showError("Fehler beim Speichern");
            }
        }
    }

    private void showRegionSelection() {
        jRegionSelectDialog.pack();
        jRegionSelectDialog.setLocationRelativeTo(DSWorkbenchSelectionFrame.this);
        jRegionSelectDialog.setVisible(true);
    }

    private void substractVillagesFromClipboard() {
        List<Village> villages = null;
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            villages = PluginManager.getSingleton().executeVillageParser((String) t.getTransferData(DataFlavor.stringFlavor));
        } catch (Exception e) {
            logger.error("Failed to read village data from clipboard", e);
            showError("Fehler beim Lesen aus der Zwischenablage");
        }

        if (villages == null || villages.isEmpty()) {
            showInfo("Keine Dörfer in der Zwischenablage gefunden");
        } else {
            substractVillages(villages);
            showSuccess(((villages.size() == 1) ? "Dorf" : villages.size() + " Dörfer") + " entfernt");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSelectionTreePanel = new org.jdesktop.swingx.JXPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSelectionTree = new org.jdesktop.swingx.JXTree();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXInfoLabel = new org.jdesktop.swingx.JXLabel();
        jRegionSelectDialog = new javax.swing.JDialog();
        jLabel1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPerformSelection = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jStartX = new javax.swing.JSpinner();
        jStartY = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jEndX = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jEndY = new javax.swing.JSpinner();
        jCustomPointsPanel = new javax.swing.JPanel();
        jCustomPointsField = new org.jdesktop.swingx.JXTextField();
        jApplyCustomFilter = new javax.swing.JButton();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();
        jSelectionPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jSelectionTreePanel.setPreferredSize(new java.awt.Dimension(600, 400));
        jSelectionTreePanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(jSelectionTree);

        jSelectionTreePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXInfoLabel.setText("Keine Meldung");
        jXInfoLabel.setOpaque(true);
        jXInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jXInfoLabelfireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXInfoLabel, java.awt.BorderLayout.CENTER);

        jSelectionTreePanel.add(infoPanel, java.awt.BorderLayout.SOUTH);

        jLabel1.setText("Start");

        jLabel7.setText("Ende");

        jPerformSelection.setText("Auswählen");
        jPerformSelection.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                firePerformRegionSelectionEvent(evt);
            }
        });

        jButton3.setText("Abbrechen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                firePerformRegionSelectionEvent(evt);
            }
        });

        jStartX.setModel(new javax.swing.SpinnerNumberModel(0, 0, 999, 1));
        jStartX.setMinimumSize(new java.awt.Dimension(80, 25));
        jStartX.setPreferredSize(new java.awt.Dimension(80, 25));

        jStartY.setModel(new javax.swing.SpinnerNumberModel(0, 0, 999, 1));
        jStartY.setMinimumSize(new java.awt.Dimension(80, 25));
        jStartY.setPreferredSize(new java.awt.Dimension(80, 25));

        jLabel8.setText("|");

        jEndX.setModel(new javax.swing.SpinnerNumberModel(0, 0, 999, 1));
        jEndX.setMinimumSize(new java.awt.Dimension(80, 25));
        jEndX.setPreferredSize(new java.awt.Dimension(80, 25));

        jLabel9.setText("|");

        jEndY.setModel(new javax.swing.SpinnerNumberModel(0, 0, 999, 1));
        jEndY.setMinimumSize(new java.awt.Dimension(80, 25));
        jEndY.setPreferredSize(new java.awt.Dimension(80, 25));

        javax.swing.GroupLayout jRegionSelectDialogLayout = new javax.swing.GroupLayout(jRegionSelectDialog.getContentPane());
        jRegionSelectDialog.getContentPane().setLayout(jRegionSelectDialogLayout);
        jRegionSelectDialogLayout.setHorizontalGroup(
            jRegionSelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jRegionSelectDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jRegionSelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jRegionSelectDialogLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jStartX, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jStartY, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jRegionSelectDialogLayout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPerformSelection))
                    .addGroup(jRegionSelectDialogLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(jEndX, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jEndY, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jRegionSelectDialogLayout.setVerticalGroup(
            jRegionSelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jRegionSelectDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jRegionSelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jStartX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jStartY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jRegionSelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jEndX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jEndY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jRegionSelectDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jPerformSelection)
                    .addComponent(jButton3))
                .addContainerGap())
        );

        jCustomPointsPanel.setMaximumSize(new java.awt.Dimension(100, 50));
        jCustomPointsPanel.setMinimumSize(new java.awt.Dimension(100, 50));
        jCustomPointsPanel.setPreferredSize(new java.awt.Dimension(100, 50));
        jCustomPointsPanel.setLayout(new java.awt.BorderLayout());

        jCustomPointsField.setToolTipText("Punktezahl manuell angeben");
        jCustomPointsField.setMaximumSize(new java.awt.Dimension(30, 30));
        jCustomPointsField.setMinimumSize(new java.awt.Dimension(30, 30));
        jCustomPointsField.setPreferredSize(new java.awt.Dimension(30, 30));
        jCustomPointsField.setPrompt("Punkte");
        jCustomPointsPanel.add(jCustomPointsField, java.awt.BorderLayout.CENTER);

        jApplyCustomFilter.setText("Auswählen");
        jApplyCustomFilter.setToolTipText("Dörfer mit weniger als den angegebenen Punkte wählen");
        jApplyCustomFilter.setMaximumSize(new java.awt.Dimension(30, 20));
        jApplyCustomFilter.setMinimumSize(new java.awt.Dimension(30, 20));
        jApplyCustomFilter.setPreferredSize(new java.awt.Dimension(30, 20));
        jCustomPointsPanel.add(jApplyCustomFilter, java.awt.BorderLayout.PAGE_END);

        setTitle("Auswahl");
        setMinimumSize(new java.awt.Dimension(500, 400));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAlwaysOnTopChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTopBox, gridBagConstraints);

        jSelectionPanel.setBackground(new java.awt.Color(239, 235, 223));
        jSelectionPanel.setMinimumSize(new java.awt.Dimension(300, 100));
        jSelectionPanel.setPreferredSize(new java.awt.Dimension(600, 400));
        jSelectionPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jSelectionPanel, gridBagConstraints);

        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireAlwaysOnTopChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopChangedEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAlwaysOnTopChangedEvent

    private void jXInfoLabelfireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXInfoLabelfireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXInfoLabelfireHideInfoEvent

    private void firePerformRegionSelectionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_firePerformRegionSelectionEvent
        if (evt.getSource() == jPerformSelection) {
            Point start = new Point((Integer) jStartX.getValue(), (Integer) jStartY.getValue());
            Point end = new Point((Integer) jEndX.getValue(), (Integer) jEndY.getValue());
            if (start.x < 0 || end.x < 0 || start.y > ServerSettings.getSingleton().getMapDimension().height || end.y > ServerSettings.getSingleton().getMapDimension().getHeight()) {
                showError("Ungültiger Start- oder Endpunkt");
            } else if ((Math.abs(end.x - start.x) * (end.y - start.y)) > 30000) {
                showError("<html>Die angegebene Auswahl k&ouml;nnte mehr als 10.000 D&ouml;rfer umfassen.<br/>"
                        + "Die Auswahl k&ouml;nnte so sehr lange dauern. Bitte verkleinere den gew&auml;hlten Bereich.");
            } else {
                List<Village> selection = DataHolder.getSingleton().getVillagesInRegion(start, end);
                addVillages(selection);
            }
        }

        jRegionSelectDialog.setVisible(false);
    }//GEN-LAST:event_firePerformRegionSelectionEvent

    private void switchViewType() {
        treeMode = !treeMode;
        buildTree();
    }

    public List<Village> getSelectedElements() {
        TreePath[] paths = jSelectionTree.getSelectionModel().getSelectionPaths();
        List<Village> result = new LinkedList<>();
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
                        if (!result.contains(v)) {
                            result.add(v);
                        }
                    } else if (v.getTribe() != Barbarians.getSingleton() && v.getTribe().getAlly() == null && a.equals(NoAlly.getSingleton())) {
                        //remove no-ally member
                        if (!result.contains(v)) {
                            result.add(v);
                        }
                    } else if (v.getTribe() != Barbarians.getSingleton() && v.getTribe().getAlly() != null && a.equals(v.getTribe().getAlly())) {
                        //remove if ally is equal
                        if (!result.contains(v)) {
                            result.add(v);
                        }
                    }
                }
            } else if (o instanceof TribeNode) {
                Tribe t = ((TribeNode) o).getUserObject();
                Village[] copy = treeData.toArray(new Village[]{});
                for (Village v : copy) {
                    if (v.getTribe() == Barbarians.getSingleton() && t.equals(Barbarians.getSingleton())) {
                        //if village is barbarian village and selected tribe are barbs, remove village
                        if (!result.contains(v)) {
                            result.add(v);
                        }
                    } else if (v.getTribe() != null && !v.getTribe().equals(Barbarians.getSingleton()) && t != null && v.getTribe().getId() == t.getId()) {
                        //selected tribe are no barbs, so check tribes to be equal
                        if (!result.contains(v)) {
                            result.add(v);
                        }
                    }
                }
            } else if (o instanceof TagNode) {
                Tag t = ((TagNode) o).getUserObject();
                Village[] copy = treeData.toArray(new Village[]{});
                for (Village v : copy) {
                    if (v != null && t != null && t.tagsVillage(v.getId())) {
                        if (!result.contains(v)) {
                            result.add(v);
                        }
                    }
                }
            } else if (o instanceof VillageNode) {
                Village v = ((VillageNode) o).getUserObject();
                if (!result.contains(v)) {
                    result.add(v);
                }
            } else if (o != null && o.equals(mRoot)) {
                //remove all
                result = new LinkedList<>(treeData);
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

    public static void main(String[] args) {

        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        GlobalOptions.setSelectedProfile(new DummyProfile());
        DataHolder.getSingleton().loadData(false);
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }
        List<Village> selection = new LinkedList<>();
        for (int i = 0; i < 100; i++) {
            selection.add(DataHolder.getSingleton().getRandomVillage());
        }

        DSWorkbenchSelectionFrame.getSingleton().resetView();
        DSWorkbenchSelectionFrame.getSingleton().addVillages(selection);

        DSWorkbenchSelectionFrame.getSingleton().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        DSWorkbenchSelectionFrame.getSingleton().setVisible(true);

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JButton jApplyCustomFilter;
    private javax.swing.JButton jButton3;
    private org.jdesktop.swingx.JXTextField jCustomPointsField;
    private javax.swing.JPanel jCustomPointsPanel;
    private javax.swing.JSpinner jEndX;
    private javax.swing.JSpinner jEndY;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JButton jPerformSelection;
    private javax.swing.JDialog jRegionSelectDialog;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel jSelectionPanel;
    private org.jdesktop.swingx.JXTree jSelectionTree;
    private org.jdesktop.swingx.JXPanel jSelectionTreePanel;
    private javax.swing.JSpinner jStartX;
    private javax.swing.JSpinner jStartY;
    private org.jdesktop.swingx.JXLabel jXInfoLabel;
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
        treeData.clear();
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

    public void substractVillages(List<Village> pVillages) {
        for (Village v : pVillages.toArray(new Village[]{})) {
            if (v != null) {
                treeData.remove(v);
            }
        }
        buildTree();
    }
}
