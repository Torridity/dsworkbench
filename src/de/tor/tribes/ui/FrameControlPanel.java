/*
 * FrameControlPanel.java
 *
 * Created on 18. Juni 2008, 18:03
 */
package de.tor.tribes.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author  Charon
 */
public class FrameControlPanel extends javax.swing.JPanel {

    private JFrame parent = null;
    private transient final ImageIcon MINIMIZE_ICON = new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_blue.png"));
    private transient final ImageIcon CLOSE_ICON = new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"));
    private transient final ImageIcon MAXIMIZE_ICON = new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_yellow.png"));
    private transient final ImageIcon STICKY_ICON = new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"));
    private transient final ImageIcon UNSTICKY_ICON = new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"));
    private int dx = 0;
    private int dy = 0;
    private boolean bAllowMaximize = false;
    private boolean titleSetOnNullParent = false;
    private boolean scaling = false;
    private int minWidth = 0;
    private int minHeight = 0;
    private final Color ACTIVE_COLOR = new Color(153, 153, 153);
    private final Color INACTIVE_COLOR = new Color(240, 240, 240);
    /**Listeners for buttons*/
    private MouseListener minimizeListener = null;
    private MouseListener stickyListener = null;
    private MouseListener maximizeListener = null;
    private MouseListener closeListener = null;
    /**Listener for parent resize*/
    private MouseListener resizeListener = null;
    private MouseMotionListener resizeMotionListener = null;
    /**Listener for dragging*/
    private MouseListener dragListener = null;
    private MouseMotionListener dragMotionListener = null;

    /** Creates new form FrameControlPanel */
    public FrameControlPanel() {
        initComponents();
    }

    public void setupPanel(JFrame pParent, boolean pAllowSticky, boolean pAllowMaximize) {
        parent = pParent;
        bAllowMaximize = pAllowMaximize;
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        parent.setMaximizedBounds(graphicsEnvironment.getMaximumWindowBounds());
        setupListeners();
        //add resize listeners
        parent.addMouseListener(resizeListener);
        parent.addMouseMotionListener(resizeMotionListener);
        //add drag&drop listeners
        addMouseListener(dragListener);
        jTitleLabel.addMouseListener(dragListener);
        addMouseMotionListener(dragMotionListener);
        jTitleLabel.addMouseMotionListener(dragMotionListener);
        //setup control panel
        if (!parent.isDisplayable()) {
            parent.setUndecorated(true);
        }
        if (titleSetOnNullParent) {
            parent.setTitle(jTitleLabel.getText());
            titleSetOnNullParent = false;
        } else {
            setTitle(parent.getTitle());
        }
        if (pAllowMaximize && pAllowSticky) {
            configureFullControl();
        } else if (!pAllowMaximize && pAllowSticky) {
            configureNoMaxControl();
        } else if (pAllowMaximize && !pAllowSticky) {
            configureNoStickyControl();
        } else {
            configureMinControl();
        }

        minWidth = (int) jTitleLabel.getMinimumSize().getWidth() + 4 * jButton1.getWidth();
        minHeight = getHeight() * 2;
    }

    private void setupListeners() {
        parent.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                setBackground(ACTIVE_COLOR);
                jTitleLabel.setEnabled(true);
                jButton1.setEnabled(true);
                jButton2.setEnabled(true);
                jButton3.setEnabled(true);
                jButton4.setEnabled(true);

            }

            @Override
            public void focusLost(FocusEvent e) {
                setBackground(INACTIVE_COLOR);
                jTitleLabel.setEnabled(false);
                jButton1.setEnabled(false);
                jButton2.setEnabled(false);
                jButton3.setEnabled(false);
                jButton4.setEnabled(false);
            }
        });

        minimizeListener = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                parent.setState(JFrame.ICONIFIED);
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ((JLabel) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((JLabel) e.getSource()).setCursor(Cursor.getDefaultCursor());
            }
        };
        stickyListener = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (parent.isAlwaysOnTop()) {
                    ((JLabel) e.getSource()).setIcon(UNSTICKY_ICON);
                    parent.setAlwaysOnTop(false);
                } else {
                    ((JLabel) e.getSource()).setIcon(STICKY_ICON);
                    parent.setAlwaysOnTop(true);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ((JLabel) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((JLabel) e.getSource()).setCursor(Cursor.getDefaultCursor());
            }
        };
        maximizeListener = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (parent.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    parent.setExtendedState(JFrame.NORMAL);
                } else {
                    parent.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ((JLabel) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((JLabel) e.getSource()).setCursor(Cursor.getDefaultCursor());
            }
        };
        closeListener = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                switch (parent.getDefaultCloseOperation()) {
                    case JFrame.DISPOSE_ON_CLOSE:
                        parent.dispose();
                        break;
                    case JFrame.HIDE_ON_CLOSE:
                        parent.setVisible(false);
                        break;
                    case JFrame.EXIT_ON_CLOSE:
                        System.exit(0);
                        break;
                    default://JFrame.DO_NOTHING_ON_CLOSE

                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ((JLabel) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((JLabel) e.getSource()).setCursor(Cursor.getDefaultCursor());
            }
        };
        resizeListener = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                /*if (parent.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                //don't drag in maximized mode
                return;
                }*/
                dx = e.getLocationOnScreen().x;
                dy = e.getLocationOnScreen().y;
                scaling = true;
                parent.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dx = 0;
                dy = 0;
                scaling = false;
                parent.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };
        dragListener = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        if (bAllowMaximize) {
                            if (parent.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                                parent.setExtendedState(JFrame.NORMAL);
                            } else {
                                parent.setExtendedState(JFrame.MAXIMIZED_BOTH);
                            }
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (parent.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    //don't drag in maximized mode
                    return;
                }
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
        };
        dragMotionListener = new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (parent.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    //don't drag in maximized mode
                    return;
                }
                Point l = e.getLocationOnScreen();
                parent.setLocation(l.x - dx, l.y - dy);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        };
        resizeMotionListener = new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (scaling) {
                    int width = parent.getWidth() + e.getLocationOnScreen().x - dx;
                    int height = parent.getHeight() + e.getLocationOnScreen().y - dy;
                    if ((width >= parent.getMinimumSize().getWidth()) && (height >= parent.getMinimumSize().getHeight())) {
                        if ((width <= parent.getMaximumSize().getWidth()) && (height <= parent.getMaximumSize().getHeight())) {
                            if (parent.isResizable()) {
                                parent.setSize(width, height);
                            }
                        }
                    }
                    dx = e.getLocationOnScreen().x;
                    dy = e.getLocationOnScreen().y;
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        };
    }

    public void setTitle(String pTitle) {
        jTitleLabel.setText(pTitle);
        if (parent != null) {
            parent.setTitle(pTitle);
        } else {
            titleSetOnNullParent = true;
        }
    }

    private void configureFullControl() {
        //all buttons shown and active
        if (parent.isAlwaysOnTop()) {
            jButton3.setIcon(STICKY_ICON);
        } else {
            jButton3.setIcon(UNSTICKY_ICON);
        }

        jButton1.addMouseListener(minimizeListener);
        jButton2.addMouseListener(maximizeListener);
        jButton3.addMouseListener(stickyListener);
        jButton4.addMouseListener(closeListener);
    }

    private void configureNoMaxControl() {
        if (parent.isAlwaysOnTop()) {
            jButton3.setIcon(STICKY_ICON);
        } else {
            jButton3.setIcon(UNSTICKY_ICON);
        }

        //buttons shift one step right
        jButton1.setVisible(false);
        jButton2.setIcon(MINIMIZE_ICON);

        jButton2.addMouseListener(minimizeListener);
        jButton3.addMouseListener(stickyListener);
        jButton4.addMouseListener(closeListener);
    }

    private void configureNoStickyControl() {
        //all buttons shown and active

        jButton1.setVisible(false);
        jButton2.setIcon(MINIMIZE_ICON);
        jButton3.setIcon(MAXIMIZE_ICON);

        jButton2.addMouseListener(minimizeListener);
        jButton3.addMouseListener(maximizeListener);
        jButton4.addMouseListener(closeListener);
    }

    private void configureMinControl() {
        //all buttons shown and active
        //setup minimize button
        jButton1.setVisible(false);
        jButton2.setVisible(false);
        jButton3.setIcon(MINIMIZE_ICON);

        jButton3.addMouseListener(minimizeListener);
        jButton4.addMouseListener(closeListener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton2 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JLabel();
        jTitleLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(153, 153, 153));
        setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_yellow.png"))); // NOI18N

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_blue.png"))); // NOI18N

        jTitleLabel.setMaximumSize(new java.awt.Dimension(2000, 16));
        jTitleLabel.setMinimumSize(new java.awt.Dimension(34, 16));
        jTitleLabel.setPreferredSize(new java.awt.Dimension(34, 16));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addGap(0, 0, 0)
                .addComponent(jButton2)
                .addGap(0, 0, 0)
                .addComponent(jButton3)
                .addGap(0, 0, 0)
                .addComponent(jButton4)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton2)
                        .addComponent(jButton1)
                        .addComponent(jButton3)
                        .addComponent(jButton4))
                    .addComponent(jTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jButton1;
    private javax.swing.JLabel jButton2;
    private javax.swing.JLabel jButton3;
    private javax.swing.JLabel jButton4;
    private javax.swing.JLabel jTitleLabel;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        JFrame f = new JFrame("test");
        f.add(new FrameControlPanel());
        f.pack();
        f.setVisible(true);
    }
}
