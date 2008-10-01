/*
 * ToolBoxFrame.java
 *
 * Created on 26. Juni 2008, 15:42
 */
package de.tor.tribes.ui;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 *
 * @author  Jejkal
 */
public class ToolBoxFrame extends javax.swing.JFrame {

    private int dx = 0;
    private int dy = 0;
    private int layout = javax.swing.BoxLayout.Y_AXIS;
    private final int iWidth = 30;
    private final int iHeight = 600;
    private final int iLabelHeight = 4;
    private final int iLabelWidth = 30;

    /** Creates new form ToolBoxFrame */
    public ToolBoxFrame() {
        initComponents();
        buildButtons();
        initListeners();
        pack();
    }

    private void buildButtons() {
        try {
            jDragLabel.setIcon(new ImageIcon("./graphics/icons/tools.png"));
            jMeasureButton.setIcon(new ImageIcon("./graphics/icons/measure.png"));
            jMarkButton.setIcon(new ImageIcon("./graphics/icons/mark.png"));
            jSendTroopsIngameButton.setIcon(new ImageIcon("./graphics/icons/def.png"));
            jSendResIngameButton.setIcon(new ImageIcon("./graphics/icons/booty.png"));
            jAttackAxeButton.setIcon(new ImageIcon("./graphics/icons/attack_axe.png"));
            jAttackRamButton.setIcon(new ImageIcon("./graphics/icons/attack_ram.png"));
            jAttackSnobButton.setIcon(new ImageIcon("./graphics/icons/attack_snob.png"));
            jAttackSpyButton.setIcon(new ImageIcon("./graphics/icons/attack_spy.png"));
            jAttackLightButton.setIcon(new ImageIcon("./graphics/icons/attack_light.png"));
            jAttackHeavyButton.setIcon(new ImageIcon("./graphics/icons/attack_heavy.png"));
            jAttackSwordButton.setIcon(new ImageIcon("./graphics/icons/attack_sword.png"));
            jMoveButton.setIcon(new ImageIcon("./graphics/icons/move.png"));
            jZoomButton.setIcon(new ImageIcon("./graphics/icons/zoom.png"));
            jShotButton.setIcon(new ImageIcon("./graphics/icons/camera.png"));
            jSearchButton.setIcon(new ImageIcon("./graphics/icons/search.png"));
            jSettingsButton.setIcon(new ImageIcon("./graphics/icons/settings.png"));
            jClockButton.setIcon(new ImageIcon("./graphics/icons/clock.png"));
            jTagButton.setIcon(new ImageIcon("./graphics/icons/tag.png"));
        } catch (Exception e) {
        }
    }

    private int getCurrentLayout() {
        return layout;
    }

    private void setCurrentLayout(int pLayout) {
        layout = pLayout;
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), layout));
        if (layout == javax.swing.BoxLayout.Y_AXIS) {
            setSize(iWidth, iHeight);
            Dimension dim = new Dimension(iLabelWidth, iLabelHeight);
            jLabel1.setMaximumSize(dim);
            jLabel1.setMinimumSize(dim);
            jLabel1.setPreferredSize(dim);
            jLabel2.setMaximumSize(dim);
            jLabel2.setMinimumSize(dim);
            jLabel2.setPreferredSize(dim);
            jLabel3.setMaximumSize(dim);
            jLabel3.setMinimumSize(dim);
            jLabel3.setPreferredSize(dim);
        } else {
            setSize(iHeight, iWidth);
            Dimension dim = new Dimension(iLabelHeight, iLabelWidth);
            jLabel1.setMaximumSize(dim);
            jLabel1.setMinimumSize(dim);
            jLabel1.setPreferredSize(dim);
            jLabel2.setMaximumSize(dim);
            jLabel2.setMinimumSize(dim);
            jLabel2.setPreferredSize(dim);
            jLabel3.setMaximumSize(dim);
            jLabel3.setMinimumSize(dim);
            jLabel3.setPreferredSize(dim);
        }
    }

    private void initListeners() {
        jDragLabel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        if (getCurrentLayout() == javax.swing.BoxLayout.X_AXIS) {
                            setCurrentLayout(javax.swing.BoxLayout.Y_AXIS);
                        } else {
                            setCurrentLayout(javax.swing.BoxLayout.X_AXIS);
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dx = e.getX();
                dy = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dx = 0;
                dy = 0;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        jDragLabel.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getLocationOnScreen().x - dx, e.getLocationOnScreen().y - dy);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDragLabel = new javax.swing.JLabel();
        jMeasureButton = new javax.swing.JButton();
        jMarkButton = new javax.swing.JButton();
        jTagButton = new javax.swing.JButton();
        jSendTroopsIngameButton = new javax.swing.JButton();
        jSendResIngameButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jAttackAxeButton = new javax.swing.JButton();
        jAttackRamButton = new javax.swing.JButton();
        jAttackSnobButton = new javax.swing.JButton();
        jAttackSpyButton = new javax.swing.JButton();
        jAttackSwordButton = new javax.swing.JButton();
        jAttackLightButton = new javax.swing.JButton();
        jAttackHeavyButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jMoveButton = new javax.swing.JButton();
        jZoomButton = new javax.swing.JButton();
        jShotButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jSearchButton = new javax.swing.JButton();
        jClockButton = new javax.swing.JButton();
        jSettingsButton = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        setTitle(bundle.getString("ToolBoxFrame.title")); // NOI18N
        setAlwaysOnTop(true);
        setMinimumSize(new java.awt.Dimension(30, 30));
        setUndecorated(true);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jDragLabel.setBackground(new java.awt.Color(153, 153, 153));
        jDragLabel.setMaximumSize(new java.awt.Dimension(30, 30));
        jDragLabel.setMinimumSize(new java.awt.Dimension(30, 30));
        jDragLabel.setPreferredSize(new java.awt.Dimension(30, 30));
        getContentPane().add(jDragLabel);

        jMeasureButton.setToolTipText(bundle.getString("ToolBoxFrame.jMeasureButton.toolTipText")); // NOI18N
        jMeasureButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jMeasureButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jMeasureButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jMeasureButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jMeasureButton);

        jMarkButton.setToolTipText(bundle.getString("ToolBoxFrame.jMarkButton.toolTipText")); // NOI18N
        jMarkButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jMarkButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jMarkButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jMarkButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jMarkButton);

        jTagButton.setToolTipText(bundle.getString("ToolBoxFrame.jTagButton.toolTipText")); // NOI18N
        jTagButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jTagButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jTagButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jTagButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jTagButton);

        jSendTroopsIngameButton.setToolTipText(bundle.getString("ToolBoxFrame.jSendTroopsIngameButton.toolTipText")); // NOI18N
        jSendTroopsIngameButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jSendTroopsIngameButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jSendTroopsIngameButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jSendTroopsIngameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jSendTroopsIngameButton);

        jSendResIngameButton.setToolTipText(bundle.getString("ToolBoxFrame.jSendResIngameButton.toolTipText")); // NOI18N
        jSendResIngameButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jSendResIngameButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jSendResIngameButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jSendResIngameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jSendResIngameButton);

        jLabel2.setBackground(new java.awt.Color(102, 102, 102));
        jLabel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel2.setMaximumSize(new java.awt.Dimension(30, 4));
        jLabel2.setMinimumSize(new java.awt.Dimension(30, 4));
        jLabel2.setPreferredSize(new java.awt.Dimension(30, 4));
        getContentPane().add(jLabel2);

        jAttackAxeButton.setToolTipText(bundle.getString("ToolBoxFrame.jAttackAxeButton.toolTipText")); // NOI18N
        jAttackAxeButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jAttackAxeButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jAttackAxeButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jAttackAxeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jAttackAxeButton);

        jAttackRamButton.setToolTipText(bundle.getString("ToolBoxFrame.jAttackRamButton.toolTipText")); // NOI18N
        jAttackRamButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jAttackRamButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jAttackRamButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jAttackRamButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jAttackRamButton);

        jAttackSnobButton.setToolTipText(bundle.getString("ToolBoxFrame.jAttackSnobButton.toolTipText")); // NOI18N
        jAttackSnobButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jAttackSnobButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jAttackSnobButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jAttackSnobButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jAttackSnobButton);

        jAttackSpyButton.setToolTipText(bundle.getString("ToolBoxFrame.jAttackSpyButton.toolTipText")); // NOI18N
        jAttackSpyButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jAttackSpyButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jAttackSpyButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jAttackSpyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jAttackSpyButton);

        jAttackSwordButton.setToolTipText(bundle.getString("ToolBoxFrame.jAttackSwordButton.toolTipText")); // NOI18N
        jAttackSwordButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jAttackSwordButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jAttackSwordButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jAttackSwordButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jAttackSwordButton);

        jAttackLightButton.setToolTipText(bundle.getString("ToolBoxFrame.jAttackLightButton.toolTipText")); // NOI18N
        jAttackLightButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jAttackLightButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jAttackLightButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jAttackLightButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jAttackLightButton);

        jAttackHeavyButton.setToolTipText(bundle.getString("ToolBoxFrame.jAttackHeavyButton.toolTipText")); // NOI18N
        jAttackHeavyButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jAttackHeavyButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jAttackHeavyButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jAttackHeavyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jAttackHeavyButton);

        jLabel1.setBackground(new java.awt.Color(102, 102, 102));
        jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel1.setMaximumSize(new java.awt.Dimension(30, 4));
        jLabel1.setMinimumSize(new java.awt.Dimension(30, 4));
        jLabel1.setPreferredSize(new java.awt.Dimension(30, 4));
        getContentPane().add(jLabel1);

        jMoveButton.setToolTipText(bundle.getString("ToolBoxFrame.jMoveButton.toolTipText")); // NOI18N
        jMoveButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jMoveButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jMoveButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jMoveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jMoveButton);

        jZoomButton.setToolTipText(bundle.getString("ToolBoxFrame.jZoomButton.toolTipText")); // NOI18N
        jZoomButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jZoomButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jZoomButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jZoomButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jZoomButton);

        jShotButton.setToolTipText(bundle.getString("ToolBoxFrame.jShotButton.toolTipText")); // NOI18N
        jShotButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jShotButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jShotButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jShotButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jShotButton);

        jLabel3.setBackground(new java.awt.Color(102, 102, 102));
        jLabel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel3.setMaximumSize(new java.awt.Dimension(30, 4));
        jLabel3.setMinimumSize(new java.awt.Dimension(30, 4));
        jLabel3.setPreferredSize(new java.awt.Dimension(30, 4));
        getContentPane().add(jLabel3);

        jSearchButton.setToolTipText(bundle.getString("ToolBoxFrame.jSearchButton.toolTipText")); // NOI18N
        jSearchButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jSearchButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jSearchButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jSearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jSearchButton);

        jClockButton.setToolTipText(bundle.getString("ToolBoxFrame.jClockButton.toolTipText")); // NOI18N
        jClockButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jClockButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jClockButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jClockButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jClockButton);

        jSettingsButton.setToolTipText(bundle.getString("ToolBoxFrame.jSettingsButton.toolTipText")); // NOI18N
        jSettingsButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jSettingsButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jSettingsButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireToolClickedEvent(evt);
            }
        });
        getContentPane().add(jSettingsButton);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireToolClickedEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireToolClickedEvent
    if (evt.getSource() == jMeasureButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MEASURE);
    } else if (evt.getSource() == jMarkButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MARK);
    } else if (evt.getSource() == jTagButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_TAG);
    } else if (evt.getSource() == jSendTroopsIngameButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_INGAME);
    } else if (evt.getSource() == jSendResIngameButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SEND_RES_INGAME);
    } else if (evt.getSource() == jAttackAxeButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_AXE);
    } else if (evt.getSource() == jAttackRamButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_RAM);
    } else if (evt.getSource() == jAttackSnobButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SNOB);
    } else if (evt.getSource() == jAttackSpyButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SPY);
    } else if (evt.getSource() == jAttackLightButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_LIGHT);
    } else if (evt.getSource() == jAttackHeavyButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_HEAVY);
    } else if (evt.getSource() == jAttackSwordButton) {
        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SWORD);
    } else if (evt.getSource() == jMoveButton) {
        MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MOVE);
    } else if (evt.getSource() == jZoomButton) {
        MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ZOOM);
    } else if (evt.getSource() == jShotButton) {
        MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SHOT);
    } else if (evt.getSource() == jSearchButton) {
        SearchFrame.getSingleton().setVisible(true);
    } else if (evt.getSource() == jSettingsButton) {
        DSWorkbenchSettingsDialog.getSingleton().setVisible(true);
    } else if (evt.getSource() == jClockButton) {
        ClockFrame.getSingleton().setVisible(true);
    }
}//GEN-LAST:event_fireToolClickedEvent

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAttackAxeButton;
    private javax.swing.JButton jAttackHeavyButton;
    private javax.swing.JButton jAttackLightButton;
    private javax.swing.JButton jAttackRamButton;
    private javax.swing.JButton jAttackSnobButton;
    private javax.swing.JButton jAttackSpyButton;
    private javax.swing.JButton jAttackSwordButton;
    private javax.swing.JButton jClockButton;
    private javax.swing.JLabel jDragLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton jMarkButton;
    private javax.swing.JButton jMeasureButton;
    private javax.swing.JButton jMoveButton;
    private javax.swing.JButton jSearchButton;
    private javax.swing.JButton jSendResIngameButton;
    private javax.swing.JButton jSendTroopsIngameButton;
    private javax.swing.JButton jSettingsButton;
    private javax.swing.JButton jShotButton;
    private javax.swing.JButton jTagButton;
    private javax.swing.JButton jZoomButton;
    // End of variables declaration//GEN-END:variables
}

