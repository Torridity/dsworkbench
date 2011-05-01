/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchNotepad.java
 *
 * Created on 28.06.2009, 12:02:37
 */
package de.tor.tribes.ui.views;

import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.swing.TabEditingEvent;
import com.jidesoft.swing.TabEditingListener;
import com.jidesoft.swing.TabEditingValidator;
import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.test.DummyVillage;
import de.tor.tribes.types.Note;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.BBPanel;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.GenericTestPanel;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.NoteTableTab;
import de.tor.tribes.ui.dnd.VillageTransferable;
import de.tor.tribes.util.BBChangeListener;
import de.tor.tribes.util.BBCodeFormatter;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.note.NoteManager;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import org.apache.log4j.Logger;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.MouseGestureHandler;
import de.tor.tribes.util.VillageListFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import org.apache.log4j.ConsoleAppender;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * @author Charon
 */
public class DSWorkbenchNotepad extends AbstractDSWorkbenchFrame implements GenericManagerListener, ActionListener, DragGestureListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        NoteTableTab activeTab = getActiveTab();
        if (e.getActionCommand() != null && activeTab != null) {
            if (e.getActionCommand().equals("Copy")) {
                activeTab.transferSelection(NoteTableTab.TRANSFER_TYPE.COPY_TO_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Cut")) {
                activeTab.transferSelection(NoteTableTab.TRANSFER_TYPE.CUT_TO_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Paste")) {
                activeTab.transferSelection(NoteTableTab.TRANSFER_TYPE.FROM_INTERNAL_CLIPBOARD);
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


                /* jXColumnList.setModel(model);
                jXColumnList.setSelectedIndex(0);*/
                jxSearchPane.setVisible(true);
            }
        }
    }

    @Override
    public void dataChangedEvent() {
        generateNoteTabs();
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        NoteTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateSet();
        }
    }
    private static Logger logger = Logger.getLogger("Notepad");
    private static DSWorkbenchNotepad SINGLETON = null;
    private DragSource dragSource;
    private BBPanel jNotePane = null;
    private GenericTestPanel centerPanel = null;

    public static synchronized DSWorkbenchNotepad getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchNotepad();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchNotepad */
    DSWorkbenchNotepad() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jNotesPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildPanel(jXNotePanel);
        buildMenu();
        jScrollPane2.setViewportView(jNotePane);
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(jNotesList, // What component
                DnDConstants.ACTION_COPY_OR_MOVE, // What drag types?
                this);// the listener

        try {
            jAlwaysOnTopBox.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("notepad.frame.alwaysOnTop")));
            setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        } catch (Exception e) {
            //setting not available
        }

        //setup map marker box
        for (int i = 0; i <= ImageManager.ID_NOTE_ICON_13; i++) {
            jIconBox.addItem(i);
        }

        ListCellRenderer r = new ListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = new DefaultListCellRenderer().getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                try {
                    JLabel label = ((JLabel) c);
                    label.setText("");
                    BufferedImage symbol = ImageManager.getNoteIcon((Integer) value);
                    label.setIcon(new ImageIcon(symbol.getScaledInstance(18, 18, BufferedImage.SCALE_FAST)));
                } catch (Exception e) {
                }
                return c;
            }
        };
        jIconBox.setRenderer(r);

        //setup note symbol box
        for (int i = -1; i <= ImageManager.NOTE_SYMBOL_WALL; i++) {
            jNoteSymbolBox.addItem(i);
        }

        jNoteSymbolBox.setRenderer(new ListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = new DefaultListCellRenderer().getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                try {
                    JLabel label = ((JLabel) c);
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setText("");
                    int val = (Integer) value;
                    if (val != -1) {
                        BufferedImage symbol = ImageManager.getNoteSymbol(val);
                        label.setIcon(new ImageIcon(symbol.getScaledInstance(18, 18, BufferedImage.SCALE_FAST)));
                    } else {
                        //no symbol
                        label.setIcon(null);
                        label.setText("-");
                    }
                } catch (Exception e) {
                }
                return c;
            }
        });


        jNoteTabbedPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
        jNoteTabbedPane.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
        jNoteTabbedPane.setBoldActiveTab(true);
        jNoteTabbedPane.addTabEditingListener(new TabEditingListener() {

            @Override
            public void editingStarted(TabEditingEvent tee) {
            }

            @Override
            public void editingStopped(TabEditingEvent tee) {
                NoteManager.getSingleton().renameGroup(tee.getOldTitle(), tee.getNewTitle());
            }

            @Override
            public void editingCanceled(TabEditingEvent tee) {
            }
        });
        jNoteTabbedPane.setTabEditingValidator(new TabEditingValidator() {

            @Override
            public boolean alertIfInvalid(int tabIndex, String tabText) {
                if (tabText.trim().length() == 0) {
                    JOptionPaneHelper.showWarningBox(jNoteTabbedPane, "'" + tabText + "' ist ein ungültiger Name für ein Notizset", "Fehler");
                    return false;
                }

                if (NoteManager.getSingleton().groupExists(tabText)) {
                    JOptionPaneHelper.showWarningBox(jNoteTabbedPane, "Es existiert bereits ein Notizset mit dem Namen '" + tabText + "'", "Fehler");
                    return false;
                }
                return true;
            }

            @Override
            public boolean isValid(int tabIndex, String tabText) {
                if (tabText.trim().length() == 0) {
                    return false;
                }

                if (NoteManager.getSingleton().groupExists(tabText)) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean shouldStartEdit(int tabIndex, MouseEvent event) {
                return !(tabIndex == 0 || tabIndex == 1);
            }
        });
        jNoteTabbedPane.setCloseAction(new AbstractAction("closeAction") {

            public void actionPerformed(ActionEvent e) {
                NoteTableTab tab = (NoteTableTab) e.getSource();
                if (JOptionPaneHelper.showQuestionConfirmBox(jNoteTabbedPane, "Das Notizset '" + tab.getNoteSet() + "' und alle darin enthaltenen Notizen wirklich löschen? ", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                    NoteManager.getSingleton().removeGroup(tab.getNoteSet());
                }
            }
        });

        jNoteTabbedPane.getModel().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                NoteTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.updateSet();
                }
            }
        });

        setGlassPane(jxSearchPane);

        //<editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        //  GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.notes_view", GlobalOptions.getHelpBroker().getHelpSet());
        //</editor-fold>

    }

    private void buildMenu() {
        JXTaskPane transferTaskPane = new JXTaskPane();
        transferTaskPane.setTitle("Übertragen");
        JXButton transferVillageList = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/center_24x24.png")));
        transferVillageList.setToolTipText("Zentriert das gewählte Notizdorf auf der Hauptkarte");
        transferVillageList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                centerNoteVillage();
            }
        });
        transferTaskPane.getContentPane().add(transferVillageList);
        JXButton transferNotes = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/att_clipboardBB.png")));
        transferNotes.setToolTipText("Überträgt die gewählten Notizen in die Zwischenablage");
        transferNotes.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                NoteTableTab tab = getActiveTab();
                if (tab != null) {
                    tab.transferSelection(NoteTableTab.TRANSFER_TYPE.CLIPBOARD_BB);
                }
            }
        });
        transferTaskPane.getContentPane().add(transferNotes);


        JXTaskPane miscPane = new JXTaskPane();
        miscPane.setTitle("Sonstiges");
        JXButton centerVillage = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/center_24x24.png")));
        centerVillage.setToolTipText("Zentriert das gewählte Notizdorf auf der Hauptkarte");
        centerVillage.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                centerNoteVillage();
            }
        });

        miscPane.getContentPane().add(centerVillage);

        JXButton exportEditor = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/center_24x24.png")));
        exportEditor.setToolTipText("Öffnet den Editor für das Format des BB-Code Exports");
        exportEditor.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                jExportFormatDialog.pack();
                bBPanel1.setBBChangeListener(new BBChangeListener() {

                    @Override
                    public void fireBBChangedEvent() {
                        jTextPane1.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body><nobr>" + BBCodeFormatter.toHtml(bBPanel1.getText()) + "</nobr></body></html>");
                    }
                });
                jExportFormatDialog.setVisible(true);

            }
        });

        miscPane.getContentPane().add(exportEditor);
        centerPanel.setupTaskPane(transferTaskPane, miscPane);
    }

    private void centerNoteVillage() {
        Village v = (Village) jVillageList.getSelectedValue();
        DSWorkbenchMainFrame.getSingleton().centerVillage(v);
    }

    /**Get the currently selected tab*/
    private NoteTableTab getActiveTab() {
        try {
            if (jNoteTabbedPane.getModel().getSelectedIndex() < 0) {
                return null;
            }
            return ((NoteTableTab) jNoteTabbedPane.getComponentAt(jNoteTabbedPane.getModel().getSelectedIndex()));
        } catch (ClassCastException cce) {
            return null;
        }
    }

    /**Initialize and add one tab for each note set to jTabbedPane1*/
    public void generateNoteTabs() {
        jNoteTabbedPane.invalidate();
        while (jNoteTabbedPane.getTabCount() > 0) {
            NoteTableTab tab = (NoteTableTab) jNoteTabbedPane.getComponentAt(0);
            tab.deregister();
            jNoteTabbedPane.removeTabAt(0);
        }

        LabelUIResource lr = new LabelUIResource();
        lr.setLayout(new BorderLayout());
        lr.add(jNewSetPanel, BorderLayout.CENTER);
        jNoteTabbedPane.setTabLeadingComponent(lr);
        String[] plans = NoteManager.getSingleton().getGroups();

        //insert default tab to first place
        int cnt = 0;
        for (String plan : plans) {
            NoteTableTab tab = new NoteTableTab(plan, this);
            jNoteTabbedPane.addTab(plan, tab);
            cnt++;
        }
        jNoteTabbedPane.setTabClosableAt(0, false);
        jNoteTabbedPane.revalidate();
        jNoteTabbedPane.setSelectedIndex(0);
        NoteTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateSet();
        }
    }

    @Override
    public void resetView() {
        NoteManager.getSingleton().addManagerListener(this);
        generateNoteTabs();
        //refreshNoteList();
    }

    public void setVillageFieldExternally(Village pVillage) {
        if (pVillage == null) {
            return;
        }
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            int[] coord = DSCalculator.xyToHierarchical(pVillage.getX(), pVillage.getY());
            jAddVillageField.setText(coord[0] + ":" + coord[1] + ":" + coord[2]);
        } else {
            jAddVillageField.setText("(" + pVillage.getX() + "|" + pVillage.getY() + ")");
        }
    }

    public void setSearchTermByVillageExternally(Village pVillage) {
        if (pVillage == null) {
            return;
        }
        jSearchField.setText(pVillage.toString());
    }

    /* public void addNoteForVillage(Village pVillage) {
    Note n = new Note();
    n.addVillage(pVillage);
    n.setNoteText("(kein Text)");
    n.setMapMarker(0);
    NoteManager.getSingleton().addNote(n);
    currentNote = n;
    refreshNoteList();
    }
    
    public void addNoteForVillages(List<Village> pVillages) {
    Note n = new Note();
    for (Village v : pVillages) {
    n.addVillage(v);
    }
    n.setNoteText("(kein Text)");
    n.setMapMarker(0);
    NoteManager.getSingleton().addNote(n);
    currentNote = n;
    refreshNoteList();
    }
    
    public boolean addVillageToCurrentNote(Village pVillage) {
    if (currentNote != null) {
    currentNote.addVillage(pVillage);
    showCurrentNote();
    return true;
    }
    return false;
    }
    
    public boolean addVillagesToCurrentNote(List<Village> pVillages) {
    if (currentNote != null) {
    for (Village v : pVillages) {
    currentNote.addVillage(v);
    }
    showCurrentNote();
    return true;
    }
    return false;
    }
     */
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jFormatPanel = new javax.swing.JPanel();
        jBoldButton = new javax.swing.JButton();
        jItalicButton = new javax.swing.JButton();
        jUnderlineButton = new javax.swing.JButton();
        jLeftButton = new javax.swing.JButton();
        jCenterButton = new javax.swing.JButton();
        jRightButton = new javax.swing.JButton();
        jColorPanel = new javax.swing.JPanel();
        jBlackColorButton = new javax.swing.JButton();
        jRedColorButton = new javax.swing.JButton();
        jGreenColorButton = new javax.swing.JButton();
        jBlueColorButton = new javax.swing.JButton();
        jOrangeColorButton = new javax.swing.JButton();
        jVioletColorButton = new javax.swing.JButton();
        jPinkColorButton = new javax.swing.JButton();
        jCyanColorButton = new javax.swing.JButton();
        jSizePanel = new javax.swing.JPanel();
        jSize10Button = new javax.swing.JButton();
        jSize12Button = new javax.swing.JButton();
        jSize14Button = new javax.swing.JButton();
        jSize18Button = new javax.swing.JButton();
        jSize20Button = new javax.swing.JButton();
        jSize28Button = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jRightPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jSearchField = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jAddVillageField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jVillageList = new javax.swing.JList();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLastModified = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jIconBox = new javax.swing.JComboBox();
        jNoteSymbolBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLeftPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jNotesList = new javax.swing.JList();
        jButton9 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jXNotePanel = new org.jdesktop.swingx.JXPanel();
        jNoteTabbedPane = new com.jidesoft.swing.JideTabbedPane();
        jNewSetPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jxSearchPane = new org.jdesktop.swingx.JXPanel();
        jXPanel3 = new org.jdesktop.swingx.JXPanel();
        jButton16 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jFilterRows = new javax.swing.JCheckBox();
        jFilterCaseSensitive = new javax.swing.JCheckBox();
        jExportFormatDialog = new javax.swing.JDialog();
        bBPanel1 = new de.tor.tribes.ui.BBPanel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();
        jNotesPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.CapabilityInfoPanel();

        jFormatPanel.setMaximumSize(new java.awt.Dimension(121, 48));
        jFormatPanel.setLayout(new java.awt.GridLayout(2, 3, 2, 2));

        jBoldButton.setBackground(new java.awt.Color(239, 235, 223));
        jBoldButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_bold.png"))); // NOI18N
        jBoldButton.setMaximumSize(new java.awt.Dimension(39, 25));
        jBoldButton.setMinimumSize(new java.awt.Dimension(39, 25));
        jBoldButton.setPreferredSize(new java.awt.Dimension(39, 25));
        jFormatPanel.add(jBoldButton);

        jItalicButton.setBackground(new java.awt.Color(239, 235, 223));
        jItalicButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_italics.png"))); // NOI18N
        jItalicButton.setMaximumSize(new java.awt.Dimension(39, 25));
        jItalicButton.setMinimumSize(new java.awt.Dimension(39, 25));
        jItalicButton.setPreferredSize(new java.awt.Dimension(39, 25));
        jFormatPanel.add(jItalicButton);

        jUnderlineButton.setBackground(new java.awt.Color(239, 235, 223));
        jUnderlineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_underlined.png"))); // NOI18N
        jUnderlineButton.setMaximumSize(new java.awt.Dimension(39, 25));
        jUnderlineButton.setMinimumSize(new java.awt.Dimension(39, 25));
        jUnderlineButton.setPreferredSize(new java.awt.Dimension(39, 25));
        jFormatPanel.add(jUnderlineButton);

        jLeftButton.setBackground(new java.awt.Color(239, 235, 223));
        jLeftButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_align_left.png"))); // NOI18N
        jLeftButton.setMaximumSize(new java.awt.Dimension(39, 25));
        jLeftButton.setMinimumSize(new java.awt.Dimension(39, 25));
        jLeftButton.setPreferredSize(new java.awt.Dimension(39, 25));
        jFormatPanel.add(jLeftButton);

        jCenterButton.setBackground(new java.awt.Color(239, 235, 223));
        jCenterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_align_center.png"))); // NOI18N
        jCenterButton.setMaximumSize(new java.awt.Dimension(39, 25));
        jCenterButton.setMinimumSize(new java.awt.Dimension(39, 25));
        jCenterButton.setPreferredSize(new java.awt.Dimension(39, 25));
        jFormatPanel.add(jCenterButton);

        jRightButton.setBackground(new java.awt.Color(239, 235, 223));
        jRightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_align_right.png"))); // NOI18N
        jRightButton.setMaximumSize(new java.awt.Dimension(39, 25));
        jRightButton.setMinimumSize(new java.awt.Dimension(39, 25));
        jRightButton.setPreferredSize(new java.awt.Dimension(39, 25));
        jFormatPanel.add(jRightButton);

        jColorPanel.setLayout(new java.awt.GridLayout(2, 2, 2, 2));

        jBlackColorButton.setBackground(new java.awt.Color(0, 0, 0));
        jBlackColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jBlackColorButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jBlackColorButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jBlackColorButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jColorPanel.add(jBlackColorButton);

        jRedColorButton.setBackground(new java.awt.Color(255, 0, 0));
        jRedColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jRedColorButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jRedColorButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jRedColorButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jColorPanel.add(jRedColorButton);

        jGreenColorButton.setBackground(new java.awt.Color(0, 255, 0));
        jGreenColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jGreenColorButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jGreenColorButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jGreenColorButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jColorPanel.add(jGreenColorButton);

        jBlueColorButton.setBackground(new java.awt.Color(0, 0, 255));
        jBlueColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jBlueColorButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jBlueColorButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jBlueColorButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jColorPanel.add(jBlueColorButton);

        jOrangeColorButton.setBackground(new java.awt.Color(255, 102, 0));
        jOrangeColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jOrangeColorButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jOrangeColorButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jOrangeColorButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jColorPanel.add(jOrangeColorButton);

        jVioletColorButton.setBackground(new java.awt.Color(153, 153, 255));
        jVioletColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jVioletColorButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jVioletColorButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jVioletColorButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jColorPanel.add(jVioletColorButton);

        jPinkColorButton.setBackground(new java.awt.Color(255, 0, 204));
        jPinkColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jPinkColorButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jPinkColorButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jPinkColorButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jColorPanel.add(jPinkColorButton);

        jCyanColorButton.setBackground(new java.awt.Color(51, 255, 255));
        jCyanColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jCyanColorButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jCyanColorButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jCyanColorButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jColorPanel.add(jCyanColorButton);

        jSizePanel.setMaximumSize(new java.awt.Dimension(121, 48));
        jSizePanel.setLayout(new java.awt.GridLayout(2, 2, 2, 2));

        jSize10Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize10Button.setFont(new java.awt.Font("SansSerif", 0, 11));
        jSize10Button.setText("10");
        jSize10Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jSizePanel.add(jSize10Button);

        jSize12Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize12Button.setFont(new java.awt.Font("SansSerif", 0, 11));
        jSize12Button.setText("12");
        jSize12Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jSizePanel.add(jSize12Button);

        jSize14Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize14Button.setFont(new java.awt.Font("SansSerif", 0, 11));
        jSize14Button.setText("14");
        jSize14Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jSizePanel.add(jSize14Button);

        jSize18Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize18Button.setFont(new java.awt.Font("SansSerif", 0, 11));
        jSize18Button.setText("18");
        jSize18Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jSizePanel.add(jSize18Button);

        jSize20Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize20Button.setFont(new java.awt.Font("SansSerif", 0, 11));
        jSize20Button.setText("20");
        jSize20Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jSizePanel.add(jSize20Button);

        jSize28Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize28Button.setFont(new java.awt.Font("SansSerif", 0, 11));
        jSize28Button.setText("28");
        jSize28Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jSizePanel.add(jSize28Button);

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setDividerSize(10);
        jSplitPane1.setToolTipText("");
        jSplitPane1.setOpaque(false);

        jRightPanel.setOpaque(false);

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(448, 254));

        jSearchField.setText("<Suchbegriff>");
        jSearchField.setToolTipText("Suchbegriff");
        jSearchField.setMaximumSize(new java.awt.Dimension(200, 25));
        jSearchField.setMinimumSize(new java.awt.Dimension(200, 25));
        jSearchField.setPreferredSize(new java.awt.Dimension(200, 25));

        jButton3.setBackground(new java.awt.Color(239, 235, 223));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_find.png"))); // NOI18N
        jButton3.setToolTipText("Notiz suchen");
        jButton3.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton3.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton3.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireFindNoteEvent(evt);
            }
        });

        jAddVillageField.setToolTipText("Koordinate des Dorfes, das der Notizliste hinzugefügt werden soll");
        jAddVillageField.setMaximumSize(new java.awt.Dimension(6, 25));
        jAddVillageField.setMinimumSize(new java.awt.Dimension(6, 25));
        jAddVillageField.setPreferredSize(new java.awt.Dimension(6, 25));

        jVillageList.setBorder(javax.swing.BorderFactory.createTitledBorder("Zugehörige Dörfer"));
        jVillageList.setDragEnabled(true);
        jScrollPane1.setViewportView(jVillageList);

        jButton2.setBackground(new java.awt.Color(239, 235, 223));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jButton2.setToolTipText("Ausgewähltes Dorf aus der Notizliste löschen");
        jButton2.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton2.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton2.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveVillageEvent(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton1.setToolTipText("Dorf zur Notizliste hinzufügen");
        jButton1.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton1.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton1.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddVillageEvent(evt);
            }
        });

        jButton10.setBackground(new java.awt.Color(239, 235, 223));
        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/center.png"))); // NOI18N
        jButton10.setToolTipText("Ausgewähltes Dorf auf der Karte zentrieren");
        jButton10.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton10.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton10.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCenterVillageEvent(evt);
            }
        });

        jButton12.setBackground(new java.awt.Color(239, 235, 223));
        jButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/to_clipboard.png"))); // NOI18N
        jButton12.setToolTipText("Dörfer als BB-Code in die Zwischenablage");
        jButton12.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton12.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton12.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireVillagesToClipboardEvent(evt);
            }
        });

        jButton11.setBackground(new java.awt.Color(239, 235, 223));
        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/clipboard_empty.png"))); // NOI18N
        jButton11.setToolTipText("Dorfliste aus der Zwischenablage lesen");
        jButton11.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton11.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton11.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireVillagesFromClipboardEvent(evt);
            }
        });

        jPanel2.setOpaque(false);

        jLabel1.setFont(new java.awt.Font("SansSerif", 0, 11));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Letzte Änderung");

        jLastModified.setEditable(false);
        jLastModified.setToolTipText("Letztes Änderungsdatum");
        jLastModified.setMaximumSize(new java.awt.Dimension(150, 25));
        jLastModified.setMinimumSize(new java.awt.Dimension(150, 25));
        jLastModified.setPreferredSize(new java.awt.Dimension(150, 25));

        jLabel2.setFont(new java.awt.Font("SansSerif", 0, 11));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Kartensymbol");
        jLabel2.setMaximumSize(new java.awt.Dimension(66, 20));
        jLabel2.setMinimumSize(new java.awt.Dimension(66, 20));
        jLabel2.setPreferredSize(new java.awt.Dimension(66, 20));

        jIconBox.setToolTipText("Symbol, das auf der Karte angezeigt wird");
        jIconBox.setMaximumSize(new java.awt.Dimension(70, 25));
        jIconBox.setMinimumSize(new java.awt.Dimension(70, 25));
        jIconBox.setPreferredSize(new java.awt.Dimension(70, 25));
        jIconBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireMapMarkerChangedEvent(evt);
            }
        });

        jNoteSymbolBox.setToolTipText("Symbol, das im Kartenpopup angezeigt wird");
        jNoteSymbolBox.setMaximumSize(new java.awt.Dimension(70, 25));
        jNoteSymbolBox.setMinimumSize(new java.awt.Dimension(70, 25));
        jNoteSymbolBox.setPreferredSize(new java.awt.Dimension(70, 25));
        jNoteSymbolBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireNoteSymbolChangedEvent(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("SansSerif", 0, 11));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Notizsymbol");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jIconBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLastModified, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                        .addGap(53, 53, 53)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jNoteSymbolBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jIconBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jNoteSymbolBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLastModified, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jRightPanelLayout = new javax.swing.GroupLayout(jRightPanel);
        jRightPanel.setLayout(jRightPanelLayout);
        jRightPanelLayout.setHorizontalGroup(
            jRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jRightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                    .addComponent(jAddVillageField, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jRightPanelLayout.createSequentialGroup()
                        .addGroup(jRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jRightPanelLayout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(jSearchField, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jRightPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jRightPanelLayout.setVerticalGroup(
            jRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jRightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                    .addGroup(jRightPanelLayout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jRightPanelLayout.createSequentialGroup()
                                .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAddVillageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jRightPanel);

        jLeftPanel.setOpaque(false);

        jNotesList.setBorder(javax.swing.BorderFactory.createTitledBorder("Notizen"));
        jNotesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fireSelectedNoteChangedEvent(evt);
            }
        });
        jScrollPane3.setViewportView(jNotesList);

        jButton9.setBackground(new java.awt.Color(239, 235, 223));
        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_new.png"))); // NOI18N
        jButton9.setToolTipText("Neue Notiz erstellen");
        jButton9.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton9.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton9.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireNewNoteEvent(evt);
            }
        });

        jButton8.setBackground(new java.awt.Color(239, 235, 223));
        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_delete.png"))); // NOI18N
        jButton8.setToolTipText("Notiz löschen");
        jButton8.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton8.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton8.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDeleteNoteEvent(evt);
            }
        });

        jButton14.setBackground(new java.awt.Color(239, 235, 223));
        jButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_copy.png"))); // NOI18N
        jButton14.setToolTipText("Gewählte Notiz kopieren");
        jButton14.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton14.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton14.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyNoteEvent(evt);
            }
        });

        jButton13.setBackground(new java.awt.Color(239, 235, 223));
        jButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/to_clipboard.png"))); // NOI18N
        jButton13.setToolTipText("Gewählte Notizen in die Zwischenablage kopieren (Einzelnotizen)");
        jButton13.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton13.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton13.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireNotesToClipboardEvent(evt);
            }
        });

        jButton15.setBackground(new java.awt.Color(239, 235, 223));
        jButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/to_clipboard2.png"))); // NOI18N
        jButton15.setToolTipText("Gewählte Notizen in die Zwischenablage kopieren (Nach Dörfern sortiert)");
        jButton15.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton15.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton15.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireNotesToClipboardByVillageEvent(evt);
            }
        });

        javax.swing.GroupLayout jLeftPanelLayout = new javax.swing.GroupLayout(jLeftPanel);
        jLeftPanel.setLayout(jLeftPanelLayout);
        jLeftPanelLayout.setHorizontalGroup(
            jLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLeftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLeftPanelLayout.createSequentialGroup()
                        .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jLeftPanelLayout.setVerticalGroup(
            jLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLeftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jLeftPanel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 811, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                .addContainerGap())
        );

        jXNotePanel.setLayout(new java.awt.BorderLayout());

        jNoteTabbedPane.setShowCloseButton(true);
        jNoteTabbedPane.setShowCloseButtonOnTab(true);
        jNoteTabbedPane.setShowGripper(true);
        jNoteTabbedPane.setTabEditingAllowed(true);
        jXNotePanel.add(jNoteTabbedPane, java.awt.BorderLayout.CENTER);

        jNewSetPanel.setLayout(new java.awt.BorderLayout());

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_new_24x24.png"))); // NOI18N
        jLabel4.setToolTipText("Leeren Angriffsplan erstellen");
        jLabel4.setMaximumSize(new java.awt.Dimension(40, 40));
        jLabel4.setMinimumSize(new java.awt.Dimension(40, 40));
        jLabel4.setOpaque(true);
        jLabel4.setPreferredSize(new java.awt.Dimension(40, 40));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireEnterEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireMouseExitEvent(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCreateNoteSetEvent(evt);
            }
        });
        jNewSetPanel.add(jLabel4, java.awt.BorderLayout.CENTER);

        jxSearchPane.setOpaque(false);
        jxSearchPane.setLayout(new java.awt.GridBagLayout());

        jXPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jXPanel3.setInheritAlpha(false);

        jButton16.setText("Anwenden");
        jButton16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jButton16fireHideGlassPaneEvent(evt);
            }
        });

        jTextField1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField1fireHighlightEvent(evt);
            }
        });

        jLabel21.setText("Suchbegriff");

        jFilterRows.setText("Nur gefilterte Zeilen anzeigen");
        jFilterRows.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jFilterRowsfireUpdateFilterEvent(evt);
            }
        });

        jFilterCaseSensitive.setText("Groß-/Kleinschreibung beachten");
        jFilterCaseSensitive.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jFilterCaseSensitivefireUpdateFilterEvent(evt);
            }
        });

        javax.swing.GroupLayout jXPanel3Layout = new javax.swing.GroupLayout(jXPanel3);
        jXPanel3.setLayout(jXPanel3Layout);
        jXPanel3Layout.setHorizontalGroup(
            jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jXPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21)
                .addGap(18, 18, 18)
                .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jXPanel3Layout.createSequentialGroup()
                        .addGap(176, 176, 176)
                        .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jFilterRows, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jFilterCaseSensitive, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jButton16)))
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jXPanel3Layout.setVerticalGroup(
            jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jXPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFilterCaseSensitive)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jFilterRows)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton16)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jxSearchPane.add(jXPanel3, new java.awt.GridBagConstraints());

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nicht gruppieren", "Gruppieren nach Dörfern", "Gruppieren nach Notizsymbolen" }));

        jLabel5.setText("Gruppierung");

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Vorschau"));

        jTextPane1.setContentType("text/html");
        jTextPane1.setEditable(false);
        jScrollPane4.setViewportView(jTextPane1);

        jButton4.setText("Übernehmen");

        jButton5.setText("Abbrechen");

        javax.swing.GroupLayout jExportFormatDialogLayout = new javax.swing.GroupLayout(jExportFormatDialog.getContentPane());
        jExportFormatDialog.getContentPane().setLayout(jExportFormatDialogLayout);
        jExportFormatDialogLayout.setHorizontalGroup(
            jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jExportFormatDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jExportFormatDialogLayout.createSequentialGroup()
                        .addGroup(jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(bBPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jExportFormatDialogLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jExportFormatDialogLayout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)))
                .addContainerGap())
        );
        jExportFormatDialogLayout.setVerticalGroup(
            jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jExportFormatDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jExportFormatDialogLayout.createSequentialGroup()
                        .addComponent(bBPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addGroup(jExportFormatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jButton5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setTitle("Notizen");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAlwaysOnTopChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTopBox, gridBagConstraints);

        jNotesPanel.setBackground(new java.awt.Color(239, 235, 223));
        jNotesPanel.setPreferredSize(new java.awt.Dimension(500, 400));
        jNotesPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 500;
        gridBagConstraints.ipady = 400;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jNotesPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireAddVillageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddVillageEvent
        /*String text = jAddVillageField.getText();
        List<Village> villages = PluginManager.getSingleton().executeVillageParser(text);
        if (villages == null || villages.isEmpty()) {
        JOptionPaneHelper.showInformationBox(this, "Keine Dorfkoordinaten gefunden.", "Information");
        return;
        
        }
        
        NotifierFrame.doNotification("DS Workbench hat " + villages.size() + ((villages.size() == 1) ? " Dorf " : " Dörfer ") + "in der Zwischenablage gefunden.", NotifierFrame.NOTIFY_INFO);
        boolean added = false;
        if (currentNote != null) {
        //add village to note
        for (Village v : villages) {
        if (currentNote != null) {
        added = (currentNote.addVillage(v)) ? true : added;
        }
        
        }
        } else {
        JOptionPaneHelper.showWarningBox(this, "Es ist keine Notiz ausgewählt.", "Fehler");
        }
        
        if (added) {
        showCurrentNote();
        } else {
        JOptionPaneHelper.showInformationBox(this, "Die Notiz ist diesem Dorf/diesen Dörfern bereits zugeordnet.", "Information");
        }*/
    }//GEN-LAST:event_fireAddVillageEvent

    private void fireNewNoteEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireNewNoteEvent
        /*   Note n = new Note();
        NoteManager.getSingleton().addNote(n);
        currentNote = n;
        refreshNoteList();*/
    }//GEN-LAST:event_fireNewNoteEvent

    private void fireFindNoteEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireFindNoteEvent
        /*  if (NoteManager.getSingleton().getNotes().size() <= 0) {
        JOptionPaneHelper.showWarningBox(this, "Keine Notizen vorhanden.", "Fehler");
        return;
        }
        
        String text = jSearchField.getText();
        if (text.length() <= 0) {
        return;
        }
        
        Note n = NoteManager.getSingleton().findNote(currentNote, text);
        if (n != null) {
        currentNote = n;
        showCurrentNote();
        
        } else {
        JOptionPaneHelper.showInformationBox(this, "Die Suche nach '" + text + "' lieferte keine Ergebnisse.", "Information");
        }*/
    }//GEN-LAST:event_fireFindNoteEvent

    private void fireDeleteNoteEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDeleteNoteEvent
        /*  Object[] selection = jNotesList.getSelectedValues();
        if (selection == null || selection.length == 0) {
        return;
        }
        
        String message = ((selection.length == 1) ? "Gewählte Notiz " : selection.length + " Notizen ") + "wirklich löschen?";
        
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
        for (Object selectedObject : selection) {
        Note selectedNote = (Note) selectedObject;
        NoteManager.getSingleton().removeNote(selectedNote);
        }
        refreshNoteList();
        currentNote = NoteManager.getSingleton().getFirstNote();
        }*/
    }//GEN-LAST:event_fireDeleteNoteEvent

    private void fireRemoveVillageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveVillageEvent
        /*   Object[] values = jVillageList.getSelectedValues();
        if (values == null || values.length == 0) {
        return;
        }
        
        String message = values.length + ((values.length > 1) ? " Dörfer " : " Dorf ") + "entfernen?";
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Dörfer entfernen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
        for (Object o : values) {
        currentNote.removeVillage((Village) o);
        }
        showCurrentNote();
        }*/
    }//GEN-LAST:event_fireRemoveVillageEvent

    private void fireMapMarkerChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireMapMarkerChangedEvent
        /*if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        if (currentNote != null) {
        currentNote.setMapMarker((Integer) jIconBox.getSelectedItem());
        }
        }*/
    }//GEN-LAST:event_fireMapMarkerChangedEvent

    private void fireAlwaysOnTopChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopChangedEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAlwaysOnTopChangedEvent

    private void fireNoteSymbolChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireNoteSymbolChangedEvent
        /* if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        if (currentNote != null) {
        currentNote.setNoteSymbol((Integer) jNoteSymbolBox.getSelectedItem());
        }
        }*/
    }//GEN-LAST:event_fireNoteSymbolChangedEvent

    private void fireCenterVillageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterVillageEvent
        try {
            Village selection = (Village) jVillageList.getSelectedValue();
            if (selection != null) {
                DSWorkbenchMainFrame.getSingleton().centerVillage(selection);
            }

        } catch (Exception e) {
            logger.error("Failed to center village", e);
        }
    }//GEN-LAST:event_fireCenterVillageEvent

    private void fireVillagesFromClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireVillagesFromClipboardEvent
        /*  try {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        List<Village> villages = PluginManager.getSingleton().executeVillageParser((String) t.getTransferData(DataFlavor.stringFlavor));
        if (villages == null || villages.isEmpty()) {
        JOptionPaneHelper.showInformationBox(this, "Es konnten keine Dorfkoodinaten in der Zwischenablage gefunden werden.", "Information");
        return;
        } else {
        NotifierFrame.doNotification("DS Workbench hat " + villages.size() + ((villages.size() == 1) ? " Dorf " : " Dörfer ") + "in der Zwischenablage gefunden.", NotifierFrame.NOTIFY_INFO);
        
        for (Village v : villages) {
        currentNote.addVillage(v);
        }
        
        setCurrentNote(currentNote);
        }
        
        } catch (Exception e) {
        logger.error("Failed to parse source villages from clipboard", e);
        JOptionPaneHelper.showErrorBox(this, "Fehler beim Lesen der Zwischenablage.", "Fehler");
        }*/
    }//GEN-LAST:event_fireVillagesFromClipboardEvent

    private void fireVillagesToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireVillagesToClipboardEvent
        if (jVillageList.getModel().getSize() == 0) {
            return;
        }
        List<Village> selection = new LinkedList<Village>();
        for (int i = 0; i < jVillageList.getModel().getSize(); i++) {
            selection.add((Village) jVillageList.getModel().getElementAt(i));
        }
        try {
            String result = VillageListFormatter.format(selection, "%VILLAGE%", true);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result.toString()), null);
            JOptionPaneHelper.showInformationBox(this, "Dorfdaten in die Zwischenablage kopiert.", "Daten kopiert");
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren der Daten.", "Fehler");
        }
    }//GEN-LAST:event_fireVillagesToClipboardEvent

    private void fireNotesToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireNotesToClipboardEvent
        Object[] selection = jNotesList.getSelectedValues();
        if (selection == null || selection.length == 0) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (Object selectedObject : selection) {
            Note selectedNote = (Note) selectedObject;
            builder.append(selectedNote.toBBCode()).append("\n");
        }
        String b = builder.toString();
        StringTokenizer t = new StringTokenizer(b, "[");
        int cnt = t.countTokens();
        if (cnt > 500) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Notizen benötigen mehr als 500 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                return;
            }
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
            JOptionPaneHelper.showInformationBox(this, "Notizen in die Zwischenablage kopiert.", "Daten kopiert");
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren der Daten.", "Fehler");
        }
    }//GEN-LAST:event_fireNotesToClipboardEvent

    private void fireSelectedNoteChangedEvent(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fireSelectedNoteChangedEvent
        /*  Note note = (Note) jNotesList.getSelectedValue();
        if (note == null) {
        return;
        }
        if (!evt.getValueIsAdjusting()) {
        currentNote = note;
        showCurrentNote();
        }*/
    }//GEN-LAST:event_fireSelectedNoteChangedEvent

    private void fireCopyNoteEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyNoteEvent
        /*  if (currentNote == null) {
        return;
        }
        Note n = new Note();
        n.setNoteText(currentNote.getNoteText());
        n.setMapMarker(currentNote.getMapMarker());
        n.setNoteSymbol(currentNote.getNoteSymbol());
        List<Integer> villageIds = new LinkedList<Integer>();
        
        for (Integer villageId : currentNote.getVillageIds()) {
        villageIds.add(villageId);
        }
        n.setVillageIds(villageIds);
        NoteManager.getSingleton().addNote(n);
        currentNote = n;
        refreshNoteList();
         */
    }//GEN-LAST:event_fireCopyNoteEvent

    private void fireNotesToClipboardByVillageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireNotesToClipboardByVillageEvent

        Object[] selection = jNotesList.getSelectedValues();
        if (selection == null || selection.length == 0) {
            return;
        }

        Hashtable<Village, List<Note>> noteMap = new Hashtable<Village, List<Note>>();

        for (Object selectedObject : selection) {
            Note selectedNote = (Note) selectedObject;
            for (Integer villageId : selectedNote.getVillageIds()) {
                Village noteVillage = DataHolder.getSingleton().getVillagesById().get(villageId);
                List<Note> notesForVillage = noteMap.get(noteVillage);
                if (notesForVillage == null) {
                    notesForVillage = new LinkedList<Note>();
                    noteMap.put(noteVillage, notesForVillage);
                }
                notesForVillage.add(selectedNote);
            }
        }

        StringBuilder builder = new StringBuilder();
        Village[] villageKeys = noteMap.keySet().toArray(new Village[]{});
        Arrays.sort(villageKeys);
        for (Village noteVillage : villageKeys) {
            List<Note> notesForVillage = noteMap.get(noteVillage);
            builder.append(noteVillage.toBBCode()).append(": ");
            boolean isNext = false;
            for (Note note : notesForVillage) {
                if (isNext) {
                    builder.append(", ");
                }
                builder.append(note.getNoteText());
                if (note.getNoteSymbol() != -1) {
                    builder.append(" [img]").append(ImageManager.getNoteImageURLOnServer(note.getNoteSymbol())).append("[/img]");
                }
                isNext = true;
            }
            builder.append("\n");
        }

        String b = builder.toString();
        StringTokenizer t = new StringTokenizer(b, "[");
        int cnt = t.countTokens();
        if (cnt > 500) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Notizen benötigen mehr als 500 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                return;
            }
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
            JOptionPaneHelper.showInformationBox(this, "Notizen in die Zwischenablage kopiert.", "Daten kopiert");
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren der Daten.", "Fehler");
        }



    }//GEN-LAST:event_fireNotesToClipboardByVillageEvent

    private void fireEnterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireEnterEvent
        jLabel4.setBackground(getBackground().darker());
}//GEN-LAST:event_fireEnterEvent

    private void fireMouseExitEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMouseExitEvent
        jLabel4.setBackground(getBackground());
}//GEN-LAST:event_fireMouseExitEvent

    private void fireCreateNoteSetEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCreateNoteSetEvent
        int unusedId = 1;
        while (unusedId < 1000) {
            if (NoteManager.getSingleton().addGroup("Neues Set " + unusedId)) {
                break;
            }
            unusedId++;
        }
        if (unusedId == 1000) {
            JOptionPaneHelper.showErrorBox(DSWorkbenchNotepad.this, "Du hast mehr als 1000 Notizsets. Bitte lösche zuerst ein paar bevor du Neue erstellst.", "Fehler");
            return;
        }
}//GEN-LAST:event_fireCreateNoteSetEvent

    private void jButton16fireHideGlassPaneEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton16fireHideGlassPaneEvent
        jxSearchPane.setBackgroundPainter(null);
        jxSearchPane.setVisible(false);
}//GEN-LAST:event_jButton16fireHideGlassPaneEvent

    private void jTextField1fireHighlightEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField1fireHighlightEvent
        updateFilter();
}//GEN-LAST:event_jTextField1fireHighlightEvent

    private void jFilterRowsfireUpdateFilterEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jFilterRowsfireUpdateFilterEvent
        updateFilter();
}//GEN-LAST:event_jFilterRowsfireUpdateFilterEvent

    private void jFilterCaseSensitivefireUpdateFilterEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jFilterCaseSensitivefireUpdateFilterEvent
        updateFilter();
}//GEN-LAST:event_jFilterCaseSensitivefireUpdateFilterEvent

    /**Update the attack plan filter*/
    private void updateFilter() {
        NoteTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateFilter(jTextField1.getText(), jFilterCaseSensitive.isSelected(), jFilterRows.isSelected());
        }
    }

    private void showCurrentNote() {
        /* if (currentNote == null) {
        jVillageList.setModel(new DefaultListModel());
        jNotePane.setBBCode("");
        } else {
        DefaultListModel model = new DefaultListModel();
        for (Integer id : currentNote.getVillageIds()) {
        Village v = DataHolder.getSingleton().getVillagesById().get(id);
        model.addElement(v);
        }
        
        jVillageList.setModel(model);
        jNotePane.setBBCode(currentNote.getNoteText());
        jLastModified.setText(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(new Date(currentNote.getTimestamp())));
        jIconBox.setSelectedItem(currentNote.getMapMarker());
        jNoteSymbolBox.setSelectedItem(currentNote.getNoteSymbol());
        jNotesList.setSelectedValue(currentNote, true);
        }
        
        List<Note> n = NoteManager.getSingleton().getNotes();
        setTitle("Notizblock - Notiz " + (n.indexOf(currentNote) + 1) + " von " + n.size());*/
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
        /* if (currentNote == null) {
        return;
        }
        try {
        Rectangle bounds = jVillageList.getBounds();
        Point locationWithinNotepad = jVillageList.getLocationOnScreen();
        Point notepadLocation = this.getLocationOnScreen();
        locationWithinNotepad.translate(-notepadLocation.x, -notepadLocation.y);
        bounds.setLocation(locationWithinNotepad);
        if (bounds.contains(pDropLocation)) {
        for (Village v : pVillages) {
        currentNote.addVillage(v);
        }
        }
        showCurrentNote();
        try {
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.NOTE_LAYER);
        } catch (Exception e) {
        }
        } catch (Exception e) {
        logger.error("Failed to insert dropped villages", e);
        }*/
    }

    public static void main(String[] args) {
        MouseGestures mMouseGestures = new MouseGestures();
        mMouseGestures.setMouseButton(MouseEvent.BUTTON3_MASK);
        mMouseGestures.addMouseGesturesListener(new MouseGestureHandler());
        mMouseGestures.start();
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        DSWorkbenchNotepad.getSingleton().setSize(600, 400);

        NoteManager.getSingleton().addGroup("test1");
        NoteManager.getSingleton().addGroup("asd2");
        NoteManager.getSingleton().addGroup("awe3");
        for (int i = 0; i < 5; i++) {
            Note n = new Note();
            n.setNoteText("Test");
            n.setTimestamp(System.currentTimeMillis());
            n.setMapMarker(ImageManager.ID_NOTE_ICON_0);
            n.setNoteSymbol(ImageManager.NOTE_SYMBOL_AXE);
            n.addVillage(new DummyVillage());
            n.addVillage(new DummyVillage());
            n.addVillage(new DummyVillage());
            Note n2 = new Note();
            n2.setNoteText("Test2");
            n2.setTimestamp(System.currentTimeMillis());
            n2.setMapMarker(ImageManager.ID_NOTE_ICON_1);
            n2.setNoteSymbol(ImageManager.NOTE_SYMBOL_SNOB);
            n2.addVillage(new DummyVillage());
            n2.addVillage(new DummyVillage());
            Note n3 = new Note();
            n3.setNoteText("Test3");
            n3.setTimestamp(System.currentTimeMillis());
            n3.setMapMarker(ImageManager.ID_NOTE_ICON_1);
            n3.setNoteSymbol(ImageManager.NOTE_SYMBOL_SPEAR);
            n3.addVillage(new DummyVillage());
            n3.addVillage(new DummyVillage());
            n3.addVillage(new DummyVillage());
            n3.addVillage(new DummyVillage());

            NoteManager.getSingleton().addManagedElement(n);
            NoteManager.getSingleton().addManagedElement("test1", n2);
            NoteManager.getSingleton().addManagedElement("asd2", n3);
        }
        DSWorkbenchNotepad.getSingleton().resetView();
        DSWorkbenchNotepad.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DSWorkbenchNotepad.getSingleton().setVisible(true);

    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        Note selectedNote = (Note) jNotesList.getSelectedValue();

        List<Village> villageList = new LinkedList<Village>();
        for (Integer villageId : selectedNote.getVillageIds()) {
            Village v = DataHolder.getSingleton().getVillagesById().get(villageId);
            if (!villageList.contains(v)) {
                villageList.add(v);
            }
        }
        if (villageList.isEmpty()) {
            return;
        }
        Cursor c = ImageManager.createVillageDragCursor(villageList.size());
        setCursor(c);
        dge.startDrag(c, new VillageTransferable(villageList), this);
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

    // <editor-fold defaultstate="collapsed" desc="Gesture handling">
    @Override
    public void fireExportAsBBGestureEvent() {
        fireNotesToClipboardEvent(null);
    }

    @Override
    public void fireNextPageGestureEvent() {
        int current = jNotesList.getSelectedIndex();
        int size = jNotesList.getModel().getSize();
        if (current + 1 > size - 1) {
            current = 0;
        } else {
            current += 1;
        }
        jNotesList.setSelectedIndex(current);
        fireSelectedNoteChangedEvent(new ListSelectionEvent(jNotesList, 0, 0, false));
    }

    @Override
    public void firePlainExportGestureEvent() {
        fireNotesToClipboardByVillageEvent(null);
    }

    @Override
    public void firePreviousPageGestureEvent() {
        int current = jNotesList.getSelectedIndex();
        int size = jNotesList.getModel().getSize();
        if (current - 1 < 0) {
            current = size - 1;
        } else {
            current -= 1;
        }
        jNotesList.setSelectedIndex(current);
        fireSelectedNoteChangedEvent(new ListSelectionEvent(jNotesList, 0, 0, false));
    }
// </editor-fold>
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.BBPanel bBPanel1;
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JTextField jAddVillageField;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JButton jBlackColorButton;
    private javax.swing.JButton jBlueColorButton;
    private javax.swing.JButton jBoldButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jCenterButton;
    private javax.swing.JPanel jColorPanel;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JButton jCyanColorButton;
    private javax.swing.JDialog jExportFormatDialog;
    private javax.swing.JCheckBox jFilterCaseSensitive;
    private javax.swing.JCheckBox jFilterRows;
    private javax.swing.JPanel jFormatPanel;
    private javax.swing.JButton jGreenColorButton;
    private javax.swing.JComboBox jIconBox;
    private javax.swing.JButton jItalicButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField jLastModified;
    private javax.swing.JButton jLeftButton;
    private javax.swing.JPanel jLeftPanel;
    private javax.swing.JPanel jNewSetPanel;
    private javax.swing.JComboBox jNoteSymbolBox;
    private com.jidesoft.swing.JideTabbedPane jNoteTabbedPane;
    private javax.swing.JList jNotesList;
    private javax.swing.JPanel jNotesPanel;
    private javax.swing.JButton jOrangeColorButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jPinkColorButton;
    private javax.swing.JButton jRedColorButton;
    private javax.swing.JButton jRightButton;
    private javax.swing.JPanel jRightPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jSearchField;
    private javax.swing.JButton jSize10Button;
    private javax.swing.JButton jSize12Button;
    private javax.swing.JButton jSize14Button;
    private javax.swing.JButton jSize18Button;
    private javax.swing.JButton jSize20Button;
    private javax.swing.JButton jSize28Button;
    private javax.swing.JPanel jSizePanel;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JButton jUnderlineButton;
    private javax.swing.JList jVillageList;
    private javax.swing.JButton jVioletColorButton;
    private org.jdesktop.swingx.JXPanel jXNotePanel;
    private org.jdesktop.swingx.JXPanel jXPanel3;
    private org.jdesktop.swingx.JXPanel jxSearchPane;
    // End of variables declaration//GEN-END:variables
}
