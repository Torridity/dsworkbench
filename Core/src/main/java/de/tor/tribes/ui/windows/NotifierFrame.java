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
package de.tor.tribes.ui.windows;

import de.tor.tribes.ui.views.DSWorkbenchAttackFrame;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.swing.JScrollPane;

/**
 *
 * @author Torridity
 */
public class NotifierFrame extends javax.swing.JDialog {

    public final static int NOTIFY_DEFAULT = -1;
    public final static int NOTIFY_UPDATE = 0;
    public final static int NOTIFY_INFO = 1;
    public final static int NOTIFY_WARNING = 2;
    public final static int NOTIFY_ERROR = 3;
    public final static int NOTIFY_ATTACK = 4;
    private BufferedImage CLOSE_ICON = null;
    private BufferedImage OVERLAY = null;
    private Rectangle CLOSE_REGION = null;
    private Rectangle ACTION_REGION = null;
    private boolean disposed = false;
    private boolean maxed = false;
    private int notifyType = NOTIFY_DEFAULT;
    private final static Stack<NotifierFrame> INSTANCES = new Stack<NotifierFrame>();

    public static void doNotification(String pMessage, int pType) {
        final String message = pMessage;
        final int type = pType;

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                NotifierFrame frame = new NotifierFrame(message, type);
                frame.setVisible(true);
                INSTANCES.push(frame);
            }
        });
    }

    /**
     * Creates new form NotifierFrame
     */
    NotifierFrame(String pMessage, int pType) {
        initComponents();
        notifyType = pType;
        jTextPane1.setText("\n" + pMessage);
        pack();

        BufferedImage typeIcon = null;
        try {
            switch (pType) {
                case 0: {
                    //update
                    typeIcon = ImageIO.read(getClass().getResource("/res/ui/bullet_ball_green.png"));
                    break;
                }
                case 1: {
                    //info
                    typeIcon = ImageIO.read(getClass().getResource("/res/ui/bullet_ball_blue.png"));
                    break;
                }
                case 2: {
                    //warn
                    typeIcon = ImageIO.read(getClass().getResource("/res/ui/bullet_ball_yellow.png"));
                    break;
                }
                case 3: {
                    //error
                    typeIcon = ImageIO.read(getClass().getResource("/res/ui/bullet_ball_red.png"));
                    break;
                }
                case 4: {
                    //attack
                    typeIcon = ImageIO.read(new File("graphics/icons/axe.png"));
                    break;
                }
                default: {
                    //type icon is null
                }
            }
        } catch (Exception e) {
        }

        try {
            CLOSE_ICON = ImageIO.read(this.getClass().getResource("/res/remove.gif"));
            OVERLAY = new BufferedImage(400, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) OVERLAY.getGraphics();
            g2d.setColor(Constants.DS_BACK);
            CLOSE_REGION = new Rectangle(400 - CLOSE_ICON.getWidth() - 2, 0, CLOSE_ICON.getWidth() + 2, CLOSE_ICON.getHeight() + 2);
            if (typeIcon != null) {
                ACTION_REGION = new Rectangle(0, 0, typeIcon.getWidth() + 2, typeIcon.getHeight() + 2);
            }
            g2d.fillOval(CLOSE_REGION.x, CLOSE_REGION.y, CLOSE_REGION.width, CLOSE_REGION.height);
            g2d.drawImage(CLOSE_ICON, CLOSE_REGION.x + 2, 0, null);

            if (typeIcon != null) {
                g2d.fillOval(0, 0, typeIcon.getWidth() + 2, typeIcon.getHeight() + 2);
                g2d.drawImage(typeIcon, 0, 0, null);
            }

            g2d.dispose();
            addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (CLOSE_REGION.contains(e.getPoint())) {
                        disposed = true;
                        dispose();
                    } else if (ACTION_REGION != null && ACTION_REGION.contains(e.getPoint())) {
                        switch (notifyType) {
                            case 0: {
                                BrowserCommandSender.openPage("https://github.com/Torridity/dsworkbench/releases");
                                break;
                            }
                            case 4: {
                                //attack
                                DSWorkbenchAttackFrame.getSingleton().setVisible(true);
                                break;
                            }
                            default: {
                                //type icon is null
                            }
                        }
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
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
        } catch (Exception e) {
        }

        Insets i = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        setBounds(Toolkit.getDefaultToolkit().getScreenSize().width - 400 - i.right, Toolkit.getDefaultToolkit().getScreenSize().height - i.bottom, 400, 10);

        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                int height = 0;
                boolean inv = false;
                int y = getLocation().y;
                int max = 100;
                while (true) {
                    try {
                        height += (inv) ? - 10 : 10;
                        setBounds(getLocation().x, y - height, getWidth(), height);

                        if (height >= max) {
                            maxed = true;
                            jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                            jScrollPane1.getViewport().setViewPosition(new Point(0, 0));
                            repaint();

                            int dur = 1;
                            try {
                                dur = Integer.parseInt(GlobalOptions.getProperty("notify.duration"));
                            } catch (Exception e) {
                            }

                            if (dur > 0) {
                                //set duration to 'dur' ten-seconds
                                dur = dur * 10 * 1000;
                            } else {
                                //set duration to "forever"
                                dur = Integer.MAX_VALUE;
                            }

                            while (true) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ie) {
                                }
                                //substract 500ms from duration
                                dur -= 500;
                                if (disposed) {
                                    //if user has closed notification, return
                                    return;
                                } else if (dur <= 0) {
                                    //if max duration was reched, dispose
                                    dispose();
                                    return;
                                }
                            }
                        } else {
                            //still in create loop
                            try {
                                Thread.sleep(80);
                            } catch (InterruptedException ie) {
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setOpaque(false);

        jTextPane1.setEditable(false);
        jTextPane1.setBackground(new java.awt.Color(255, 0, 204));
        jTextPane1.setDisabledTextColor(new java.awt.Color(255, 255, 153));
        jTextPane1.setOpaque(false);
        jScrollPane1.setViewportView(jTextPane1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void dispose() {
        super.dispose();
        //repaint all remaining instances
        INSTANCES.pop();
        if (!INSTANCES.isEmpty()) {
            //repaint item which is currently shown
            INSTANCES.peek().repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Constants.DS_BACK);
        g2d.fillRect(0, 0, 400, 100);
        if (maxed) {
            super.paint(g2d);
        }

        g2d.drawImage(OVERLAY, 0, 0, null);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        NotifierFrame.doNotification("Ein Update ist verfügbar!", NotifierFrame.NOTIFY_UPDATE);
        /*  NotifierFrame.doNotification("Dies ist eine Information.", NotifierFrame.NOTIFY_INFO);
         NotifierFrame.doNotification("Dies ist eine Warnung.", NotifierFrame.NOTIFY_WARNING);
         NotifierFrame.doNotification("Die ist ein Fehler!", NotifierFrame.NOTIFY_ERROR);*/
        // NotifierFrame.doNotification("Es müssen Angriffe raus.", NotifierFrame.NOTIFY_ATTACK);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
