/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchNotepad.java
 *
 * Created on 28.06.2009, 12:02:37
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Note;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.note.NoteManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLDocument;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class DSWorkbenchNotepad extends AbstractDSWorkbenchFrame {

    private static Logger logger = Logger.getLogger("Notepad");
    private static DSWorkbenchNotepad SINGLETON = null;
    private Action boldAction = new StyledEditorKit.BoldAction();
    private Action underlineAction = new StyledEditorKit.UnderlineAction();
    private Action italicAction = new StyledEditorKit.ItalicAction();
    private Note currentNote = null;

    public static synchronized DSWorkbenchNotepad getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchNotepad();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchNotepad */
    DSWorkbenchNotepad() {
        initComponents();
        jBoldButton.setAction(boldAction);
        jBoldButton.setText("");
        jBoldButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_bold.png")));
        jItalicButton.setAction(italicAction);
        jItalicButton.setText("");
        jItalicButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_italics.png")));
        jUnderlineButton.setAction(underlineAction);
        jUnderlineButton.setText("");
        jUnderlineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_underlined.png")));
        jSize10Button.setAction(new StyledEditorKit.FontSizeAction("10", 10));
        jSize12Button.setAction(new StyledEditorKit.FontSizeAction("12", 12));
        jSize14Button.setAction(new StyledEditorKit.FontSizeAction("14", 14));
        jSize18Button.setAction(new StyledEditorKit.FontSizeAction("18", 18));
        jSize20Button.setAction(new StyledEditorKit.FontSizeAction("20", 20));
        jSize28Button.setAction(new StyledEditorKit.FontSizeAction("28", 28));
        jRedColorButton.setAction(new StyledEditorKit.ForegroundAction("Red", Color.RED));
        jRedColorButton.setText("");
        jGreenColorButton.setAction(new StyledEditorKit.ForegroundAction("Green", Color.GREEN));
        jGreenColorButton.setText("");
        jBlueColorButton.setAction(new StyledEditorKit.ForegroundAction("Blue", Color.BLUE));
        jBlueColorButton.setText("");
        jBlackColorButton.setAction(new StyledEditorKit.ForegroundAction("Black", Color.BLACK));
        jBlackColorButton.setText("");
        jCyanColorButton.setAction(new StyledEditorKit.ForegroundAction("Cyan", Color.CYAN));
        jCyanColorButton.setText("");
        jOrangeColorButton.setAction(new StyledEditorKit.ForegroundAction("Orange", new Color(255, 102, 0)));
        jOrangeColorButton.setText("");
        jPinkColorButton.setAction(new StyledEditorKit.ForegroundAction("Pink", new Color(255, 0, 204)));
        jPinkColorButton.setText("");
        jVioletColorButton.setAction(new StyledEditorKit.ForegroundAction("Violet", new Color(152, 153, 255)));
        jVioletColorButton.setText("");
        jLeftButton.setAction(new StyledEditorKit.AlignmentAction("<", StyleConstants.ALIGN_LEFT));
        jLeftButton.setText("");
        jLeftButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_align_left.png")));
        jCenterButton.setAction(new StyledEditorKit.AlignmentAction("|", StyleConstants.ALIGN_CENTER));
        jCenterButton.setText("");
        jCenterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_align_center.png")));
        jRightButton.setAction(new StyledEditorKit.AlignmentAction(">", StyleConstants.ALIGN_RIGHT));
        jRightButton.setText("");
        jRightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_align_right.png")));

        jIconBox.addItem(new Integer(0));
        jIconBox.addItem(new Integer(1));
        jIconBox.addItem(new Integer(2));
        jIconBox.addItem(new Integer(3));
        jIconBox.addItem(new Integer(4));
        jIconBox.setUI((ComboBoxUI) MyComboBoxUI.createUI(jIconBox));

    }

    static class MyComboBoxUI extends BasicComboBoxUI {

        public static ComponentUI createUI(JComponent c) {
            return new MyComboBoxUI();
        }

        protected JButton createArrowButton() {
            JButton button = new BasicArrowButton(BasicArrowButton.EAST);
            button.setMinimumSize(new Dimension(10,36));
            button.setMaximumSize(new Dimension(10,36));
            button.setPreferredSize(new Dimension(10,36));
            return button;
        }
    }

    public void setCurrentNote(Note pNote) {
        currentNote = pNote;
        showCurrentNote();

        ListCellRenderer r = new ListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = new DefaultListCellRenderer().getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                try {
                    JLabel label = ((JLabel) c);
                    label.setIcon(new ImageIcon(ImageManager.getNoteIcon((Integer) value)));
                } catch (Exception e) {
                }
                return c;
            }
        };
        jIconBox.setRenderer(r);
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
        jVillageList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jNotePane = new javax.swing.JEditorPane();
        jPanel2 = new javax.swing.JPanel();
        jBlackColorButton = new javax.swing.JButton();
        jRedColorButton = new javax.swing.JButton();
        jGreenColorButton = new javax.swing.JButton();
        jBlueColorButton = new javax.swing.JButton();
        jOrangeColorButton = new javax.swing.JButton();
        jVioletColorButton = new javax.swing.JButton();
        jPinkColorButton = new javax.swing.JButton();
        jCyanColorButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jSize10Button = new javax.swing.JButton();
        jSize12Button = new javax.swing.JButton();
        jSize14Button = new javax.swing.JButton();
        jSize18Button = new javax.swing.JButton();
        jSize20Button = new javax.swing.JButton();
        jSize28Button = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jBoldButton = new javax.swing.JButton();
        jItalicButton = new javax.swing.JButton();
        jUnderlineButton = new javax.swing.JButton();
        jLeftButton = new javax.swing.JButton();
        jCenterButton = new javax.swing.JButton();
        jRightButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jAddVillageField = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jSearchField = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jLastModified = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jIconBox = new javax.swing.JComboBox();
        jCheckBox1 = new javax.swing.JCheckBox();

        setTitle("Notizen");

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jVillageList.setDragEnabled(true);
        jScrollPane1.setViewportView(jVillageList);

        jNotePane.setBackground(new java.awt.Color(255, 255, 204));
        jNotePane.setContentType("text/html");
        jNotePane.setText("<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n    </p>\r\n  </body>\r\n</html>\r\n");
        jNotePane.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireCaretUpdateEvent(evt);
            }
        });
        jScrollPane2.setViewportView(jNotePane);

        jPanel2.setLayout(new java.awt.GridLayout(2, 2, 2, 2));

        jBlackColorButton.setBackground(new java.awt.Color(0, 0, 0));
        jBlackColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jBlackColorButton.setMaximumSize(new java.awt.Dimension(23, 23));
        jBlackColorButton.setMinimumSize(new java.awt.Dimension(23, 23));
        jBlackColorButton.setPreferredSize(new java.awt.Dimension(23, 23));
        jPanel2.add(jBlackColorButton);

        jRedColorButton.setBackground(new java.awt.Color(255, 0, 0));
        jRedColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jRedColorButton.setMaximumSize(new java.awt.Dimension(23, 23));
        jRedColorButton.setMinimumSize(new java.awt.Dimension(23, 23));
        jRedColorButton.setPreferredSize(new java.awt.Dimension(23, 23));
        jPanel2.add(jRedColorButton);

        jGreenColorButton.setBackground(new java.awt.Color(0, 255, 0));
        jGreenColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jGreenColorButton.setMaximumSize(new java.awt.Dimension(23, 23));
        jGreenColorButton.setMinimumSize(new java.awt.Dimension(23, 23));
        jGreenColorButton.setPreferredSize(new java.awt.Dimension(23, 23));
        jPanel2.add(jGreenColorButton);

        jBlueColorButton.setBackground(new java.awt.Color(0, 0, 255));
        jBlueColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jBlueColorButton.setMaximumSize(new java.awt.Dimension(23, 23));
        jBlueColorButton.setMinimumSize(new java.awt.Dimension(23, 23));
        jBlueColorButton.setPreferredSize(new java.awt.Dimension(23, 23));
        jPanel2.add(jBlueColorButton);

        jOrangeColorButton.setBackground(new java.awt.Color(255, 102, 0));
        jOrangeColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jOrangeColorButton.setMaximumSize(new java.awt.Dimension(23, 23));
        jOrangeColorButton.setMinimumSize(new java.awt.Dimension(23, 23));
        jOrangeColorButton.setPreferredSize(new java.awt.Dimension(23, 23));
        jPanel2.add(jOrangeColorButton);

        jVioletColorButton.setBackground(new java.awt.Color(153, 153, 255));
        jVioletColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jVioletColorButton.setMaximumSize(new java.awt.Dimension(23, 23));
        jVioletColorButton.setMinimumSize(new java.awt.Dimension(23, 23));
        jVioletColorButton.setPreferredSize(new java.awt.Dimension(23, 23));
        jPanel2.add(jVioletColorButton);

        jPinkColorButton.setBackground(new java.awt.Color(255, 0, 204));
        jPinkColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jPinkColorButton.setMaximumSize(new java.awt.Dimension(23, 23));
        jPinkColorButton.setMinimumSize(new java.awt.Dimension(23, 23));
        jPinkColorButton.setPreferredSize(new java.awt.Dimension(23, 23));
        jPanel2.add(jPinkColorButton);

        jCyanColorButton.setBackground(new java.awt.Color(51, 255, 255));
        jCyanColorButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jCyanColorButton.setMaximumSize(new java.awt.Dimension(23, 23));
        jCyanColorButton.setMinimumSize(new java.awt.Dimension(23, 23));
        jCyanColorButton.setPreferredSize(new java.awt.Dimension(23, 23));
        jPanel2.add(jCyanColorButton);

        jPanel3.setLayout(new java.awt.GridLayout(2, 2, 2, 2));

        jSize10Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize10Button.setText("10");
        jSize10Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jPanel3.add(jSize10Button);

        jSize12Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize12Button.setText("12");
        jSize12Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jPanel3.add(jSize12Button);

        jSize14Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize14Button.setText("14");
        jSize14Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jPanel3.add(jSize14Button);

        jSize18Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize18Button.setText("18");
        jSize18Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jPanel3.add(jSize18Button);

        jSize20Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize20Button.setText("20");
        jSize20Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jPanel3.add(jSize20Button);

        jSize28Button.setBackground(new java.awt.Color(239, 235, 223));
        jSize28Button.setText("28");
        jSize28Button.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jPanel3.add(jSize28Button);

        jPanel4.setLayout(new java.awt.GridLayout(2, 3, 2, 2));

        jBoldButton.setBackground(new java.awt.Color(239, 235, 223));
        jBoldButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_bold.png"))); // NOI18N
        jBoldButton.setMaximumSize(new java.awt.Dimension(39, 23));
        jBoldButton.setMinimumSize(new java.awt.Dimension(39, 23));
        jBoldButton.setPreferredSize(new java.awt.Dimension(39, 23));
        jPanel4.add(jBoldButton);

        jItalicButton.setBackground(new java.awt.Color(239, 235, 223));
        jItalicButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_italics.png"))); // NOI18N
        jItalicButton.setMaximumSize(new java.awt.Dimension(39, 23));
        jItalicButton.setMinimumSize(new java.awt.Dimension(39, 23));
        jItalicButton.setPreferredSize(new java.awt.Dimension(39, 23));
        jPanel4.add(jItalicButton);

        jUnderlineButton.setBackground(new java.awt.Color(239, 235, 223));
        jUnderlineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_underlined.png"))); // NOI18N
        jUnderlineButton.setMaximumSize(new java.awt.Dimension(39, 23));
        jUnderlineButton.setMinimumSize(new java.awt.Dimension(39, 23));
        jUnderlineButton.setPreferredSize(new java.awt.Dimension(39, 23));
        jPanel4.add(jUnderlineButton);

        jLeftButton.setBackground(new java.awt.Color(239, 235, 223));
        jLeftButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_align_left.png"))); // NOI18N
        jLeftButton.setMaximumSize(new java.awt.Dimension(39, 23));
        jLeftButton.setMinimumSize(new java.awt.Dimension(39, 23));
        jLeftButton.setPreferredSize(new java.awt.Dimension(39, 23));
        jPanel4.add(jLeftButton);

        jCenterButton.setBackground(new java.awt.Color(239, 235, 223));
        jCenterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_align_center.png"))); // NOI18N
        jCenterButton.setMaximumSize(new java.awt.Dimension(39, 23));
        jCenterButton.setMinimumSize(new java.awt.Dimension(39, 23));
        jCenterButton.setPreferredSize(new java.awt.Dimension(39, 23));
        jPanel4.add(jCenterButton);

        jRightButton.setBackground(new java.awt.Color(239, 235, 223));
        jRightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_align_right.png"))); // NOI18N
        jRightButton.setMaximumSize(new java.awt.Dimension(39, 23));
        jRightButton.setMinimumSize(new java.awt.Dimension(39, 23));
        jRightButton.setPreferredSize(new java.awt.Dimension(39, 23));
        jPanel4.add(jRightButton);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton1.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton1.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddVillageEvent(evt);
            }
        });

        jAddVillageField.setText("(234|234)");
        jAddVillageField.setMaximumSize(new java.awt.Dimension(6, 25));
        jAddVillageField.setMinimumSize(new java.awt.Dimension(6, 25));
        jAddVillageField.setPreferredSize(new java.awt.Dimension(6, 25));

        jButton2.setBackground(new java.awt.Color(239, 235, 223));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jButton2.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton2.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton2.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveVillageEvent(evt);
            }
        });

        jSearchField.setMaximumSize(new java.awt.Dimension(200, 25));
        jSearchField.setMinimumSize(new java.awt.Dimension(200, 25));
        jSearchField.setPreferredSize(new java.awt.Dimension(200, 25));

        jButton3.setBackground(new java.awt.Color(239, 235, 223));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/text_find.png"))); // NOI18N
        jButton3.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton3.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton3.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireFindNoteEvent(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(239, 235, 223));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/prev.png"))); // NOI18N
        jButton4.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton4.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton4.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                firePrevNoteEvent(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(239, 235, 223));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/next.png"))); // NOI18N
        jButton5.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton5.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton5.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireNextNoteEvent(evt);
            }
        });

        jButton6.setBackground(new java.awt.Color(239, 235, 223));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/end.png"))); // NOI18N
        jButton6.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton6.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton6.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireLastNoteEvent(evt);
            }
        });

        jButton7.setBackground(new java.awt.Color(239, 235, 223));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/beginning.png"))); // NOI18N
        jButton7.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton7.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton7.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireFirstNoteEvent(evt);
            }
        });

        jButton8.setBackground(new java.awt.Color(239, 235, 223));
        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_delete.png"))); // NOI18N
        jButton8.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton8.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton8.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDeleteNoteEvent(evt);
            }
        });

        jButton9.setBackground(new java.awt.Color(239, 235, 223));
        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_new.png"))); // NOI18N
        jButton9.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton9.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton9.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireNewNoteEvent(evt);
            }
        });

        jLastModified.setEditable(false);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Letzte Änderung");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jAddVillageField, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                    .addComponent(jLastModified, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jIconBox, 0, 71, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLastModified, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jIconBox, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jAddVillageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jCheckBox1.setText("Immer im Vordergrund");
        jCheckBox1.setOpaque(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireAddVillageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddVillageEvent
        String text = jAddVillageField.getText();
        Village v = null;
        if (ServerSettings.getSingleton().getCoordType() != 2) {

            if (!text.trim().matches("[0-9]{1,3}\\:[0-9]{1,3}:[0-9]{1,3}")) {
                JOptionPaneHelper.showWarningBox(this, "Koordinaten müssen im Format Con:Sec:Sub angegeben werden.", "Fehler");
                return;

            }









            String[] split = text.trim().split("[\\:]");
            if (split == null || split.length != 3) {
                logger.warn("Invalid value '" + text + "'");
                JOptionPaneHelper.showWarningBox(this, "Koordinaten müssen im Format Con:Sec:Sub angegeben werden.", "Fehler");
                return;

            }








            int[] xy = DSCalculator.hierarchicalToXy(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            v =
                    DataHolder.getSingleton().getVillages()[xy[0]][xy[1]];
        } else {
            if (!text.trim().matches("\\([0-9]{1,3}\\|[0-9]{1,3}\\)")) {
                JOptionPaneHelper.showWarningBox(this, "Koordinaten müssen im Format (X|Y) angegeben werden.", "Fehler");
                return;

            }








            String[] split = text.replaceAll("\\(", "").replaceAll("\\)", "").trim().split("[\\|]");
            logger.warn("Invalid value '" + text + "'");
            if (split == null || split.length != 2) {
                JOptionPaneHelper.showWarningBox(this, "Koordinaten müssen im Format (X|Y) angegeben werden.", "Fehler");
                return;

            }








            v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
        }

        if (v == null) {
            JOptionPaneHelper.showWarningBox(this, "An den angegebenen Koordinaten befindet sich kein Dorf.", "Fehler");
            return;

        }

        //add village to note

        if (currentNote != null) {
            if (currentNote.addVillage(v)) {
                showCurrentNote();
            } else {
                JOptionPaneHelper.showInformationBox(this, "Die Notiz ist diesem Dorf bereits zugeordnet.", "Information");
            }

        } else {
            JOptionPaneHelper.showWarningBox(this, "Es ist keine Notiz ausgewählt.", "Fehler");
        }
    }//GEN-LAST:event_fireAddVillageEvent

    private void fireNewNoteEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireNewNoteEvent
        Note n = new Note();
        NoteManager.getSingleton().addNote(n);
        currentNote =
                n;
        showCurrentNote();
    }//GEN-LAST:event_fireNewNoteEvent

    private void firePrevNoteEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_firePrevNoteEvent
        currentNote = NoteManager.getSingleton().getPreviousNote(currentNote);
        showCurrentNote();
    }//GEN-LAST:event_firePrevNoteEvent

    private void fireFirstNoteEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireFirstNoteEvent
        currentNote = NoteManager.getSingleton().getFirstNote();
        showCurrentNote();
    }//GEN-LAST:event_fireFirstNoteEvent

    private void fireNextNoteEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireNextNoteEvent
        currentNote = NoteManager.getSingleton().getNextNote(currentNote);
        showCurrentNote();
    }//GEN-LAST:event_fireNextNoteEvent

    private void fireLastNoteEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireLastNoteEvent
        currentNote = NoteManager.getSingleton().getLastNote();
        showCurrentNote();
    }//GEN-LAST:event_fireLastNoteEvent

    private void fireFindNoteEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireFindNoteEvent
        if (NoteManager.getSingleton().getNotes().size() <= 0) {
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
        }

    }//GEN-LAST:event_fireFindNoteEvent

    private void fireCaretUpdateEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireCaretUpdateEvent
        if (currentNote != null) {
            //only update if cursor is in caret
            if (jNotePane.hasFocus()) {
                currentNote.setNoteText(jNotePane.getText());
            }

        }
    }//GEN-LAST:event_fireCaretUpdateEvent

    private void fireDeleteNoteEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDeleteNoteEvent
        if (currentNote != null) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Notiz wirklich löschen?", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                NoteManager.getSingleton().removeNote(currentNote);
                currentNote =
                        NoteManager.getSingleton().getFirstNote();
            }

            showCurrentNote();
        }
    }//GEN-LAST:event_fireDeleteNoteEvent

    private void fireRemoveVillageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveVillageEvent
        Object[] values = jVillageList.getSelectedValues();
        if (values == null || values.length == 0) {
            return;
        }

        String message = values.length + ((values.length > 1) ? " Dörfer " : " Dorf ") + "entfernen?";
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Dörfer entfernen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            for (Object o : values) {
                currentNote.removeVillage((Village) o);
            }

            showCurrentNote();
        }

    }//GEN-LAST:event_fireRemoveVillageEvent

    private void showCurrentNote() {

        if (currentNote == null) {
            jVillageList.setModel(new DefaultListModel());
            jNotePane.setDocument(new HTMLDocument());
        } else {
            DefaultListModel model = new DefaultListModel();
            for (Integer id : currentNote.getVillageIds()) {
                Village v = DataHolder.getSingleton().getVillagesById().get(id);
                model.addElement(v);
            }

            jVillageList.setModel(model);
            jNotePane.setText(currentNote.getNoteText());
            jLastModified.setText(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(new Date(currentNote.getTimestamp())));
        }

        List<Note> n = NoteManager.getSingleton().getNotes();
        setTitle("Notizblock - Notiz " + (n.indexOf(currentNote) + 1) + " von " + n.size());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchNotepad().setVisible(true);
            }
        });

    /*   String text = "24:24:3";
    System.out.println(text.matches("\\([0-9]{1,3}\\|[0-9]{1,3}\\)"));
    System.out.println(text.matches("[0-9]{1,3}\\:[0-9]{1,3}:[0-9]{1,3}"));/´*/
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField jAddVillageField;
    private javax.swing.JButton jBlackColorButton;
    private javax.swing.JButton jBlueColorButton;
    private javax.swing.JButton jBoldButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jCenterButton;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JButton jCyanColorButton;
    private javax.swing.JButton jGreenColorButton;
    private javax.swing.JComboBox jIconBox;
    private javax.swing.JButton jItalicButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jLastModified;
    private javax.swing.JButton jLeftButton;
    private javax.swing.JEditorPane jNotePane;
    private javax.swing.JButton jOrangeColorButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton jPinkColorButton;
    private javax.swing.JButton jRedColorButton;
    private javax.swing.JButton jRightButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jSearchField;
    private javax.swing.JButton jSize10Button;
    private javax.swing.JButton jSize12Button;
    private javax.swing.JButton jSize14Button;
    private javax.swing.JButton jSize18Button;
    private javax.swing.JButton jSize20Button;
    private javax.swing.JButton jSize28Button;
    private javax.swing.JButton jUnderlineButton;
    private javax.swing.JList jVillageList;
    private javax.swing.JButton jVioletColorButton;
    // End of variables declaration//GEN-END:variables
}
