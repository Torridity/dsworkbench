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
package de.tor.tribes.ui.panels;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.InvalidTribe;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.interfaces.BBChangeListener;
import de.tor.tribes.util.BBCodeFormatter;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.PluginManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.regex.Matcher;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import net.java.dev.colorchooser.ColorChooser;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class BBPanel extends javax.swing.JPanel {

    private static Logger logger = Logger.getLogger("BBPanel");
    private String buffer = "";
    private ColorChooser colorChooser = null;
    private BBChangeListener changeListener = null;

    public BBPanel() {
        this(null);
    }

    /** Creates new form BBPanel */
    public BBPanel(BBChangeListener pListener) {
        initComponents();
        setEditMode(true);
        changeListener = pListener;
        colorChooser = new ColorChooser();
        colorChooser.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                jColorChooseDialog.setVisible(false);
                fireAddColorCodeEvent();
            }
        });
        jPanel2.add(colorChooser);
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

    public void setBBChangeListener(BBChangeListener pListener) {
        changeListener = pListener;
    }

    public void setBBCode(String pText) {
        jTextPane1.setContentType("text/plain");
        jTextPane1.setText(pText);
        setEditMode(false);
    }

    public String getBBCode() {
        return jTextPane1.getText();
    }

    public String getText() {
        return buffer;
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
        jBBPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jPanel3 = new javax.swing.JPanel();
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
        jTableButton = new javax.swing.JButton();
        jRemoveButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        jColorChooseDialog.setAlwaysOnTop(true);
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

        setMinimumSize(new java.awt.Dimension(363, 251));
        setLayout(new java.awt.BorderLayout());

        jBBPanel.setBackground(new java.awt.Color(239, 235, 223));
        jBBPanel.setLayout(new java.awt.BorderLayout());

        jTextPane1.setEditable(false);
        jTextPane1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jTextPane1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                caretupdate(evt);
            }
        });
        jScrollPane1.setViewportView(jTextPane1);

        jBBPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        infoPanel.setAnimated(false);
        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jScrollPane2.setMinimumSize(new java.awt.Dimension(23, 150));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(8, 150));

        jTextPane2.setContentType("text/html");
        jScrollPane2.setViewportView(jTextPane2);

        infoPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jBBPanel.add(infoPanel, java.awt.BorderLayout.SOUTH);

        add(jBBPanel, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

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

        jTableButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/table.gif"))); // NOI18N
        jTableButton.setEnabled(false);
        jTableButton.setMaximumSize(new java.awt.Dimension(20, 20));
        jTableButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jTableButton.setPreferredSize(new java.awt.Dimension(20, 20));
        jTableButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddContentEvent(evt);
            }
        });
        jPanel1.add(jTableButton);

        jPanel3.add(jPanel1);

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
        jPanel3.add(jRemoveButton);

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/search.png"))); // NOI18N
        jButton1.setToolTipText("Blendet die Vorschau in BB-Formatierung ein");
        jButton1.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButton1.setMaximumSize(new java.awt.Dimension(60, 20));
        jButton1.setMinimumSize(new java.awt.Dimension(60, 20));
        jButton1.setPreferredSize(new java.awt.Dimension(60, 20));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangePreviewEvent(evt);
            }
        });
        jPanel3.add(jButton1);

        add(jPanel3, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

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
        } else if (evt.getSource() == jTableButton) {
            try {
                jTextPane1.getDocument().insertString(jTextPane1.getCaretPosition(), "[table]\n[**]head1[||]head2[/**]\n[*]test1[|]test2\n[/table]", null);
            } catch (BadLocationException ble) {
            }
            /**
            
            [table][**]head1[||]head2[/**][*]test1[|]test2[/*][/table]
             */
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
            jTextPane1.setText(text.replaceAll(Matcher.quoteReplacement(first), trimmedFirst).replaceAll(Matcher.quoteReplacement(last), trimmedLast));
        } catch (Exception e) {
            //something strange happened
        }
    }//GEN-LAST:event_fireRemoveCodeEvent

    private void caretupdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_caretupdate
        jTextPane2.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(jTextPane1.getText()) + "</body></html>");
        if (changeListener != null) {
            changeListener.fireBBChangedEvent();
        }
    }//GEN-LAST:event_caretupdate

    private void fireChangePreviewEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangePreviewEvent
        infoPanel.setCollapsed(!infoPanel.isCollapsed());
    }//GEN-LAST:event_fireChangePreviewEvent

    private void fireAddColorCodeEvent() {
        try {
            String rgb = Integer.toHexString(colorChooser.getColor().getRGB());
            rgb = rgb.substring(2, rgb.length());
            String pOpenCode = "[color=#" + rgb + "]";
            String pCloseCode = "[/color]";
            int s = jTextPane1.getSelectionStart();
            int e = jTextPane1.getSelectionEnd();
            String t = jTextPane1.getDocument().getText(s, e - s);
            jTextPane1.getDocument().remove(s, e - s);
            jTextPane1.getDocument().insertString(s, pOpenCode + t + pCloseCode, null);
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
            String t = jTextPane1.getDocument().getText(s, e - s);
            jTextPane1.getDocument().remove(s, e - s);
            jTextPane1.getDocument().insertString(s, pOpenCode + t + pCloseCode, null);
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
            String t = jTextPane1.getDocument().getText(s, e - s);
            jTextPane1.getDocument().remove(s, e - s);
            jTextPane1.getDocument().insertString(s, pOpenCode + t + pCloseCode, null);
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

    public void setEditMode(boolean pToEditMode) {
        if (jBoldButton.isEnabled()) {
            return;
        }
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
        jTableButton.setEnabled(pToEditMode);
        jRemoveButton.setEnabled(pToEditMode);
        jTextPane1.setEditable(pToEditMode);

        if (pToEditMode) {
            jTextPane1.setContentType("text/plain");
            jTextPane1.setText(buffer);
        } else {
            buffer = jTextPane1.getText();
            buildFormattedCode();
        }
        jTextPane1.setEditable(pToEditMode);
    }

    private void buildFormattedCode() {
        jTextPane1.setContentType("text/html");
        jTextPane1.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(buffer) + "</body></html>");
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JButton jAllyButton;
    private javax.swing.JPanel jBBPanel;
    private javax.swing.JButton jBoldButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jColorButton;
    private javax.swing.JDialog jColorChooseDialog;
    private javax.swing.JButton jImageButton;
    private javax.swing.JButton jItalicButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton jLinkButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton jQuoteButton;
    private javax.swing.JButton jRemoveButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton jSizeButton;
    private javax.swing.JDialog jSizeChooseDialog;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JButton jStrokeButton;
    private javax.swing.JButton jTableButton;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JButton jTribeButton;
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
