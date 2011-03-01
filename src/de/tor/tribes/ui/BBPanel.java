/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BBPanel.java
 *
 * Created on Feb 17, 2011, 10:48:36 PM
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.InvalidTribe;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.BBChangeListener;
import de.tor.tribes.util.BBCodeFormatter;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.PluginManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultStyledDocument;
import net.java.dev.colorchooser.ColorChooser;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class BBPanel extends javax.swing.JPanel {

    private static Logger logger = Logger.getLogger("BBPanel");
    private String sBuffer = "";
    private ColorChooser mColorChooser = null;
    private BBChangeListener mListener = null;

    /** Creates new form BBPanel */
    public BBPanel(BBChangeListener pListener) {
        initComponents();
        mListener = pListener;
        //  ((HTMLEditorKit) jTextPane1.getEditorKit()).setLinkCursor(new Cursor(Cursor.HAND_CURSOR));
        mColorChooser = new ColorChooser();
        mColorChooser.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                jColorChooseDialog.setVisible(false);
                fireAddColorCodeEvent();
            }
        });
        jPanel2.add(mColorChooser);
        jColorChooseDialog.pack();
        jSizeChooseDialog.pack();

        jSlider1.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                jSizeChooseDialog.setVisible(false);
                fireSizeCodeEvent();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        jTextPane1.setBackground(Constants.DS_BACK_LIGHT);
        jTextPane1.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String desc = e.getDescription();
                    if (desc.startsWith("###")) {
                        //village
                        try {
                            Village v = PluginManager.getSingleton().executeVillageParser(desc.substring(3)).get(0);
                            //http://zz1.beta.tribalwars.net/game.php?village=11879&screen=info_village&id=11879
                            if (v != null) {
                                String url = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
                                url += "/game.php?village=" + v.getId() + "&screen=info_village&id=" + v.getId();
                                BrowserCommandSender.openPage(url);
                            }
                        } catch (Exception ex) {
                            logger.error("Failed open village link", ex);
                        }
                    } else if (desc.startsWith("##")) {
                        //ally
                        //http://zz1.beta.tribalwars.net/game.php?village=11879&screen=info_ally&id=1
                        try {
                            Ally a = DataHolder.getSingleton().getAllyByName(desc.substring(2));
                            if (a != null) {
                                String url = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
                                url += "/game.php?village=" + GlobalOptions.getSelectedProfile().getTribe().getVillageList()[0].getId() + "&screen=info_ally&id=" + a.getId();
                                BrowserCommandSender.openPage(url);
                            }
                        } catch (Exception ex) {
                            logger.error("Failed open ally link", ex);
                        }
                    } else if (desc.startsWith("#")) {
                        //tribe
                        //http://zz1.beta.tribalwars.net/game.php?village=11879&screen=info_player&id=15186
                        try {
                            Tribe t = DataHolder.getSingleton().getTribeByName(desc.substring(1));
                            if (!t.equals(InvalidTribe.getSingleton())) {
                                String url = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
                                url += "/game.php?village=" + GlobalOptions.getSelectedProfile().getTribe().getVillageList()[0].getId() + "&screen=info_player&id=" + t.getId();
                                BrowserCommandSender.openPage(url);
                            }
                        } catch (Exception ex) {
                            logger.error("Failed open tribe link", ex);
                        }
                    } else {
                        //normal URL
                        BrowserCommandSender.openPage(desc);
                    }
                }
            }
        });
    }

    public void setBBCode(String pText) {
        jToggleButton1.setSelected(false);
        jTextPane1.setContentType("text/plain");
        jTextPane1.setText(pText);
        setEditMode(false);
    }

    public String getText() {
        return sBuffer;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jColorChooseDialog = new javax.swing.JDialog();
        jPanel2 = new javax.swing.JPanel();
        jSizeChooseDialog = new javax.swing.JDialog();
        jSlider1 = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jTribeMenu = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jToggleButton1 = new javax.swing.JToggleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jBoldButton = new javax.swing.JButton();
        jItalicButton = new javax.swing.JButton();
        jUnderlineButton = new javax.swing.JButton();
        jStrokeButton = new javax.swing.JButton();
        jTribeButton = new javax.swing.JButton();
        jAllyButton = new javax.swing.JButton();
        jVillageButton = new javax.swing.JButton();
        jQuoteButton = new javax.swing.JButton();
        jLinkButton = new javax.swing.JButton();
        jImageButton = new javax.swing.JButton();
        jColorButton = new javax.swing.JButton();
        jSizeButton = new javax.swing.JButton();
        jRemoveButton = new javax.swing.JButton();

        jColorChooseDialog.setAlwaysOnTop(true);
        jColorChooseDialog.setModal(true);
        jColorChooseDialog.setUndecorated(true);

        jPanel2.setMaximumSize(new java.awt.Dimension(50, 50));
        jPanel2.setMinimumSize(new java.awt.Dimension(50, 50));
        jPanel2.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.setLayout(new java.awt.GridLayout(1, 1));

        javax.swing.GroupLayout jColorChooseDialogLayout = new javax.swing.GroupLayout(jColorChooseDialog.getContentPane());
        jColorChooseDialog.getContentPane().setLayout(jColorChooseDialogLayout);
        jColorChooseDialogLayout.setHorizontalGroup(
            jColorChooseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jColorChooseDialogLayout.setVerticalGroup(
            jColorChooseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jSizeChooseDialog.setUndecorated(true);

        jSlider1.setForeground(new java.awt.Color(0, 0, 0));
        jSlider1.setMaximum(40);
        jSlider1.setMinimum(8);
        jSlider1.setMinorTickSpacing(1);
        jSlider1.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setValue(12);
        jSlider1.setMaximumSize(new java.awt.Dimension(30, 130));
        jSlider1.setMinimumSize(new java.awt.Dimension(30, 130));
        jSlider1.setOpaque(false);
        jSlider1.setPreferredSize(new java.awt.Dimension(30, 130));
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireSliderChangedEvent(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("<html><span style='font-size:12;'>12</span></html>");
        jLabel1.setMaximumSize(new java.awt.Dimension(50, 130));
        jLabel1.setMinimumSize(new java.awt.Dimension(50, 130));
        jLabel1.setPreferredSize(new java.awt.Dimension(50, 130));

        javax.swing.GroupLayout jSizeChooseDialogLayout = new javax.swing.GroupLayout(jSizeChooseDialog.getContentPane());
        jSizeChooseDialog.getContentPane().setLayout(jSizeChooseDialogLayout);
        jSizeChooseDialogLayout.setHorizontalGroup(
            jSizeChooseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSizeChooseDialogLayout.createSequentialGroup()
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jSizeChooseDialogLayout.setVerticalGroup(
            jSizeChooseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
            .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jMenuItem1.setText("jMenuItem1");
        jTribeMenu.add(jMenuItem1);

        jMenuItem2.setText("jMenuItem2");
        jTribeMenu.add(jMenuItem2);

        jMenuItem3.setText("jMenuItem3");
        jTribeMenu.add(jMenuItem3);

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_edit.png"))); // NOI18N
        jToggleButton1.setToolTipText("Zwischen Bearbeitungs- und Betrachtungsmodus wechseln");
        jToggleButton1.setMaximumSize(new java.awt.Dimension(50, 20));
        jToggleButton1.setMinimumSize(new java.awt.Dimension(50, 20));
        jToggleButton1.setPreferredSize(new java.awt.Dimension(50, 20));
        jToggleButton1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_find.png"))); // NOI18N
        jToggleButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireStateChangeEvent(evt);
            }
        });

        jTextPane1.setEditable(false);
        jTextPane1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jScrollPane1.setViewportView(jTextPane1);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        jBoldButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bold.gif"))); // NOI18N
        jBoldButton.setEnabled(false);
        jBoldButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jBoldButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jBoldButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jBoldButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jBoldButton);

        jItalicButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/italic.gif"))); // NOI18N
        jItalicButton.setEnabled(false);
        jItalicButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jItalicButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jItalicButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jItalicButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jItalicButton);

        jUnderlineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/underline.gif"))); // NOI18N
        jUnderlineButton.setEnabled(false);
        jUnderlineButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jUnderlineButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jUnderlineButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jUnderlineButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jUnderlineButton);

        jStrokeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/stroked.gif"))); // NOI18N
        jStrokeButton.setEnabled(false);
        jStrokeButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jStrokeButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jStrokeButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jStrokeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jStrokeButton);

        jTribeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/tribe.gif"))); // NOI18N
        jTribeButton.setEnabled(false);
        jTribeButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jTribeButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jTribeButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jTribeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jTribeButton);

        jAllyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/ally.gif"))); // NOI18N
        jAllyButton.setEnabled(false);
        jAllyButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jAllyButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jAllyButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jAllyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jAllyButton);

        jVillageButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/village.gif"))); // NOI18N
        jVillageButton.setEnabled(false);
        jVillageButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jVillageButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jVillageButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jVillageButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jVillageButton);

        jQuoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/quote.gif"))); // NOI18N
        jQuoteButton.setEnabled(false);
        jQuoteButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jQuoteButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jQuoteButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jQuoteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jQuoteButton);

        jLinkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/link.gif"))); // NOI18N
        jLinkButton.setEnabled(false);
        jLinkButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jLinkButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jLinkButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jLinkButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jLinkButton);

        jImageButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/image.gif"))); // NOI18N
        jImageButton.setEnabled(false);
        jImageButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jImageButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jImageButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jImageButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jImageButton);

        jColorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/color.gif"))); // NOI18N
        jColorButton.setEnabled(false);
        jColorButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jColorButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jColorButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jColorButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jColorButton);

        jSizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/size.gif"))); // NOI18N
        jSizeButton.setEnabled(false);
        jSizeButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jSizeButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jSizeButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jSizeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jSizeButton);

        jRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/red_x.png"))); // NOI18N
        jRemoveButton.setToolTipText("Innersten BB-Code ab dem Cursor/der Auswahl löschen");
        jRemoveButton.setEnabled(false);
        jRemoveButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jRemoveButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jRemoveButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jRemoveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveCodeEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addComponent(jRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fireStateChangeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireStateChangeEvent
        setEditMode(jToggleButton1.isSelected());
        if (!jToggleButton1.isSelected() && mListener != null) {
            mListener.fireBBChangedEvent();
        }
    }//GEN-LAST:event_fireStateChangeEvent

    private void fireAddContentEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddContentEvent
        if (!((JButton) evt.getSource()).isEnabled()) {
            return;
        }
        if (evt.getSource() == jBoldButton) {
            insertBBCode("[b]", "[/b]");
        } else if (evt.getSource() == jItalicButton) {
            insertBBCode("[i]", "[/i]");
        } else if (evt.getSource() == jUnderlineButton) {
            insertBBCode("[u]", "[/u]");
        } else if (evt.getSource() == jStrokeButton) {
            insertBBCode("[s]", "[/s]");
        } else if (evt.getSource() == jTribeButton) {
            insertBBCode("[player]", "[/player]");
        } else if (evt.getSource() == jAllyButton) {
            insertBBCode("[ally]", "[/ally]");
        } else if (evt.getSource() == jVillageButton) {
            insertBBCode("[coord]", "[/coord]");
        } else if (evt.getSource() == jQuoteButton) {
            insertBBCode("[quote]", "[/quote]");
        } else if (evt.getSource() == jLinkButton) {
            insertBBCode("[url]", "[/url]");
        } else if (evt.getSource() == jImageButton) {
            insertBBCode("[img]", "[/img]");
        } else if (evt.getSource() == jColorButton) {
            jColorChooseDialog.setLocation(jColorButton.getLocationOnScreen().x, jColorButton.getLocationOnScreen().y);
            jColorChooseDialog.setVisible(true);
        } else if (evt.getSource() == jSizeButton) {
            jSizeChooseDialog.setLocation(jSizeButton.getLocationOnScreen().x, jSizeButton.getLocationOnScreen().y);
            jSizeChooseDialog.setVisible(true);
        }


    }//GEN-LAST:event_fireAddContentEvent

    private void fireSliderChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireSliderChangedEvent
        jLabel1.setText("<html><span style='font-size:" + jSlider1.getValue() + ";'>" + jSlider1.getValue() + "</span></html>");
    }//GEN-LAST:event_fireSliderChangedEvent

    private void fireRemoveCodeEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveCodeEvent
        try {
            int s = jTextPane1.getSelectionStart();
            int e = jTextPane1.getSelectionEnd();
            String text = jTextPane1.getText();
            String first = text.substring(0, s);
            String last = text.substring(e);
            int lastOpenInFirst = first.lastIndexOf("[");
            int lastCloseInFirst = first.lastIndexOf("]");
            int firstOpenInLast = last.indexOf("[");
            int firstCloseInLast = last.indexOf("]");
            if (lastOpenInFirst < 0 || lastCloseInFirst < 0 || firstOpenInLast < 0 || firstCloseInLast < 0) {
                //nothing valid found
                return;
            }
            String trimmedFirst = first.substring(0, lastOpenInFirst) + first.substring(lastCloseInFirst + 1);
            String trimmedLast = last.substring(0, firstOpenInLast) + last.substring(firstCloseInLast + 1);
            jTextPane1.setText(text.replaceAll(Pattern.quote(first), trimmedFirst).replaceAll(Pattern.quote(last), trimmedLast));
        } catch (Exception e) {
            //something strange happened
        }
    }//GEN-LAST:event_fireRemoveCodeEvent

    private void fireAddColorCodeEvent() {
        try {
            String rgb = Integer.toHexString(mColorChooser.getColor().getRGB());
            rgb = rgb.substring(2, rgb.length());
            String pOpenCode = "[color=#" + rgb + "]";
            String pCloseCode = "[/color]";
            int s = jTextPane1.getSelectionStart();
            int e = jTextPane1.getSelectionEnd();
            String t = ((DefaultStyledDocument) jTextPane1.getDocument()).getText(s, e - s);
            ((DefaultStyledDocument) jTextPane1.getDocument()).remove(s, e - s);
            ((DefaultStyledDocument) jTextPane1.getDocument()).insertString(s, pOpenCode + t + pCloseCode, null);
            if (t.length() == 0) {
                jTextPane1.setCaretPosition(s + pOpenCode.length());
            } else {
                jTextPane1.setCaretPosition(s + pOpenCode.length() + t.length() + pCloseCode.length());
            }
            jTextPane1.requestFocus();
        } catch (Exception ee) {
            logger.error("Failed to insert color BBCode", ee);
        }
    }

    private void fireSizeCodeEvent() {
        try {
            String size = Integer.toString(jSlider1.getValue());
            String pOpenCode = "[size=" + size + "]";
            String pCloseCode = "[/size]";
            int s = jTextPane1.getSelectionStart();
            int e = jTextPane1.getSelectionEnd();
            String t = ((DefaultStyledDocument) jTextPane1.getDocument()).getText(s, e - s);
            ((DefaultStyledDocument) jTextPane1.getDocument()).remove(s, e - s);
            ((DefaultStyledDocument) jTextPane1.getDocument()).insertString(s, pOpenCode + t + pCloseCode, null);
            if (t.length() == 0) {
                jTextPane1.setCaretPosition(s + pOpenCode.length());
            } else {
                jTextPane1.setCaretPosition(s + pOpenCode.length() + t.length() + pCloseCode.length());
            }
            jTextPane1.requestFocus();
        } catch (Exception ee) {
            logger.error("Failed to insert size BBCode", ee);
        }
    }

    private void insertBBCode(String pOpenCode, String pCloseCode) {
        try {
            int s = jTextPane1.getSelectionStart();
            int e = jTextPane1.getSelectionEnd();
            String t = ((DefaultStyledDocument) jTextPane1.getDocument()).getText(s, e - s);
            ((DefaultStyledDocument) jTextPane1.getDocument()).remove(s, e - s);
            ((DefaultStyledDocument) jTextPane1.getDocument()).insertString(s, pOpenCode + t + pCloseCode, null);
            if (t.length() == 0) {
                jTextPane1.setCaretPosition(s + pOpenCode.length());
            } else {
                jTextPane1.setCaretPosition(s + pOpenCode.length() + t.length() + pCloseCode.length());
            }
            jTextPane1.requestFocus();
        } catch (Exception ee) {
            logger.error("Failed to insert standard BBCode", ee);
        }
    }

    private void setEditMode(boolean pToEditMode) {
        jBoldButton.setEnabled(pToEditMode);
        jItalicButton.setEnabled(pToEditMode);
        jUnderlineButton.setEnabled(pToEditMode);
        jStrokeButton.setEnabled(pToEditMode);
        jTribeButton.setEnabled(pToEditMode);
        jAllyButton.setEnabled(pToEditMode);
        jVillageButton.setEnabled(pToEditMode);
        jQuoteButton.setEnabled(pToEditMode);
        jLinkButton.setEnabled(pToEditMode);
        jImageButton.setEnabled(pToEditMode);
        jColorButton.setEnabled(pToEditMode);
        jSizeButton.setEnabled(pToEditMode);
        jRemoveButton.setEnabled(pToEditMode);
        jTextPane1.setEditable(pToEditMode);
        if (pToEditMode) {
            jTextPane1.setContentType("text/plain");
            jTextPane1.setText(sBuffer);
        } else {
            sBuffer = jTextPane1.getText();
            buildFormattedCode();
        }
        jTextPane1.setEditable(pToEditMode);
    }

    private void buildFormattedCode() {
        jTextPane1.setContentType("text/html");
        jTextPane1.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(sBuffer) + "</body></html>");
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAllyButton;
    private javax.swing.JButton jBoldButton;
    private javax.swing.JButton jColorButton;
    private javax.swing.JDialog jColorChooseDialog;
    private javax.swing.JButton jImageButton;
    private javax.swing.JButton jItalicButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton jLinkButton;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jQuoteButton;
    private javax.swing.JButton jRemoveButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jSizeButton;
    private javax.swing.JDialog jSizeChooseDialog;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JButton jStrokeButton;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JButton jTribeButton;
    private javax.swing.JPopupMenu jTribeMenu;
    private javax.swing.JButton jUnderlineButton;
    private javax.swing.JButton jVillageButton;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(400, 300);
        f.add(new BBPanel(null));
        f.setVisible(true);
        /* String test = "[b]Tester[/b]";
        //"\\[b\\](.+?)\\[/b\\]", "<strong>$1</strong>")
        Pattern p = Pattern.compile("\\[b\\](.+?)\\[/b\\]");
        Matcher m = p.matcher(test);*/


    }
}
/*[tribe]Amr al as[/tribe]
[i]test test
[/i]
 */
