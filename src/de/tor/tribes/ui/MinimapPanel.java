/*
 * MinimapPanel.java
 *
 * Created on 11. September 2007, 17:41
 */
package de.tor.tribes.ui;

import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.GlobalOptions;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author  jejkal
 */
public class MinimapPanel extends javax.swing.JPanel {

    private Image mBuffer = null;
    private MinimapRepaintThread mPaintThread = null;
    private int iX = 0;
    private int iY = 0;
    private int iWidth = 0;
    private int iHeight = 0;
    private DSWorkbenchMainFrame mParent;
    private MinimapZoomFrame mZoomFrame = null;
    private int iCurrentCursor = GlobalOptions.CURSOR_DEFAULT;
    private static MinimapPanel GLOBAL_MINIMAP = null;
    private ScreenshotPanel mScreenshotPanel = null;
    private boolean doRedraw = false;

    public static MinimapPanel getGlobalMinimap() {
        return GLOBAL_MINIMAP;
    }

    public static void initGlobalMinimap(DSWorkbenchMainFrame pParent) {
        GLOBAL_MINIMAP = new MinimapPanel(pParent);
    }

    /** Creates new form MinimapPanel */
    MinimapPanel(DSWorkbenchMainFrame pParent) {
        initComponents();
        setSize(270, 233);
        mParent = pParent;
        setCursor(GlobalOptions.getCursor(iCurrentCursor));
        /* mScreenshotPanel = new ScreenshotPanel();
        jPanel1.add(mScreenshotPanel);*/
        mPaintThread = new MinimapRepaintThread(this);
        mPaintThread.start();
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                mParent.updateLocationByMinimap(x, y);
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                switch (iCurrentCursor) {
                    case GlobalOptions.CURSOR_ZOOM: {
                        mZoomFrame.setVisible(true);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (mZoomFrame.isVisible()) {
                    mZoomFrame.setVisible(false);
                }
            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                switch (iCurrentCursor) {
                    case GlobalOptions.CURSOR_MOVE:
                        int x = e.getX();
                        int y = e.getY();
                        mParent.updateLocationByMinimap(x, y);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                switch (iCurrentCursor) {
                    case GlobalOptions.CURSOR_ZOOM: {
                        if (!mZoomFrame.isVisible()) {
                            mZoomFrame.setVisible(true);
                        }

                        int x = (int) Math.rint((double) 1000 / (double) getWidth() * (double) e.getX());
                        int y = (int) Math.rint((double) 1000 / (double) getHeight() * (double) e.getY());
                        mZoomFrame.updatePosition(x, y);
                        break;
                    }
                    default: {
                        if (mZoomFrame.isVisible()) {
                            mZoomFrame.setVisible(false);
                        }
                    }
                }
            }
        });

        addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                iCurrentCursor += e.getWheelRotation();
                //MOve: 10, Zoom: 11
                if (iCurrentCursor == GlobalOptions.CURSOR_DEFAULT + e.getWheelRotation()) {
                    if (e.getWheelRotation() < 0) {
                        iCurrentCursor = GlobalOptions.CURSOR_ZOOM;
                    } else {
                        iCurrentCursor = GlobalOptions.CURSOR_MOVE;
                    }
                } else if (iCurrentCursor < GlobalOptions.CURSOR_MOVE) {
                    iCurrentCursor = GlobalOptions.CURSOR_DEFAULT;
                } else if (iCurrentCursor > GlobalOptions.CURSOR_ZOOM) {
                    iCurrentCursor = GlobalOptions.CURSOR_DEFAULT;
                }
                if (iCurrentCursor != GlobalOptions.CURSOR_ZOOM) {
                    if (mZoomFrame.isVisible()) {
                        mZoomFrame.setVisible(false);
                    }
                } else {
                    mZoomFrame.setVisible(true);
                }
                setCursor(GlobalOptions.getCursor(iCurrentCursor));
            }
        });
    }

    public void setCurrentCursor(int pCurrentCursor) {
        iCurrentCursor = pCurrentCursor;
        setCursor(GlobalOptions.getCursor(iCurrentCursor));
    }

    public void setSelection(int pX, int pY, int pWidth, int pHeight) {
        iX = pX;
        iY = pY;
        iWidth = pWidth;
        iHeight = pHeight;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(mBuffer, 0, 0, null);
        g2d.setColor(Color.YELLOW);
        int w = (int) Math.rint(((double) getWidth() / 1000) * (double) iWidth);
        int h = (int) Math.rint(((double) getHeight() / 1000) * (double) iHeight);

        double posX = ((double) getWidth() / 1000 * (double) iX) - w / 2;
        double posY = ((double) getHeight() / 1000 * (double) iY) - h / 2;

        g2d.drawRect((int) Math.rint(posX), (int) Math.rint(posY), w, h);
        g2d.setColor(Color.BLACK);
    }

    public void makeScreenshot() {
        jScreenshotPreview.setVisible(true);
    }

    public void resetBuffer() {
        mBuffer = null;
    }

    protected void updateComplete(BufferedImage pBuffer) {
        if (mZoomFrame == null) {
            mZoomFrame = new MinimapZoomFrame(pBuffer);
            mZoomFrame.setSize(300, 300);
            mZoomFrame.setLocation(0, 0);
        }
        // mScreenshotPanel.setBuffer(pBuffer);
        if (mBuffer == null) {
            mBuffer = pBuffer;
            mBuffer = mBuffer.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);
        } else if ((mBuffer.getWidth(null) != getWidth()) || (mBuffer.getHeight(null) != getHeight())) {
            mBuffer = pBuffer;
            mBuffer = mBuffer.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);
        } else if (doRedraw) {
            mBuffer = pBuffer;
            mBuffer = mBuffer.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);
        }
        doRedraw = false;
        repaint();
    }

    public void redraw() {
        doRedraw = true;
        mPaintThread.update();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScreenshotPreview = new javax.swing.JFrame();
        jPanel1 = new javax.swing.JPanel();

        jPanel1.setMaximumSize(new java.awt.Dimension(1000, 1000));
        jPanel1.setMinimumSize(new java.awt.Dimension(1000, 1000));
        jPanel1.setPreferredSize(new java.awt.Dimension(1000, 1000));
        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jScreenshotPreviewLayout = new javax.swing.GroupLayout(jScreenshotPreview.getContentPane());
        jScreenshotPreview.getContentPane().setLayout(jScreenshotPreviewLayout);
        jScreenshotPreviewLayout.setHorizontalGroup(
            jScreenshotPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jScreenshotPreviewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jScreenshotPreviewLayout.setVerticalGroup(
            jScreenshotPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jScreenshotPreviewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1020, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1051, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JFrame jScreenshotPreview;
    // End of variables declaration//GEN-END:variables
}

class MinimapRepaintThread extends Thread {

    private BufferedImage mBuffer = null;
    private MinimapPanel mParent = null;
    private boolean drawn = false;

    public MinimapRepaintThread(MinimapPanel pParent) {
        mParent = pParent;
        mBuffer = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
    }

    public void update() {
        drawn = false;
    }

    @Override
    public void run() {
        while (true) {
            if (!drawn) {
                redraw();
                drawn = true;
            }
            mParent.updateComplete(mBuffer);

            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
    }

    private void redraw() {
        long s = System.currentTimeMillis();
        int x = 0;
        int y = 0;
        Graphics2D g2d = (Graphics2D) mBuffer.getGraphics();

        g2d.setColor(new Color(35, 125, 0));
        g2d.fillRect(0, 0, mBuffer.getWidth(null), mBuffer.getHeight(null));
        int cx = 0;
        int cy = 0;
        Village[][] mVisibleVillages = GlobalOptions.getDataHolder().getVillages();

        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                Village v = mVisibleVillages[i][j];
                if (v != null) {
                    Color mark = null;
                    boolean isLeft = false;
                    if (v.getTribe() == null) {
                        isLeft = true;
                    } else {
                        try {
                            Marker m  = GlobalOptions.getMarkerByValue(v.getTribe().getName());
                            if (m == null) {
                                m = GlobalOptions.getMarkerByValue(v.getTribe().getAlly().getName());
                                if(m != null){
                                    mark = m.getMarkerColor();
                                }
                            }else{
                                mark = m.getMarkerColor();
                            }
                        } catch (Exception e) {
                            mark = null;
                        }
                    }

                    if (!isLeft) {
                        if (mark != null) {
                            g2d.setColor(mark);
                        } else {
                            g2d.setColor(Color.RED);
                        }
                        g2d.fillRect(i, j, 1, 1);
                    } else {
                        g2d.setColor(Color.BLACK);
                        g2d.fillRect(i, j, 1, 1);
                    }
                }
            }
        }

        try {
            if (Boolean.parseBoolean(GlobalOptions.getProperty("minimap.showcontinents"))) {
                g2d.setColor(Color.BLACK);
                Composite c = g2d.getComposite();
                Composite a = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
                Font f = g2d.getFont();
                Font t = new Font("Serif", Font.BOLD, 30);
                g2d.setFont(t);

                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        g2d.setComposite(a);
                        String conti = "K" + (j * 10 + i);
                        Rectangle2D bounds = g2d.getFontMetrics(t).getStringBounds(conti, g2d);

                        g2d.drawString(conti, (int) Math.rint(i * 100 + 50 - bounds.getWidth() / 2), (int) Math.rint(j * 100 + 80 - bounds.getHeight() / 2));
                        g2d.setComposite(c);
                        int wk = 100;
                        int hk = 100;
                        if (i == 9) {
                            wk = 99;
                        }
                        if (j == 9) {
                            hk = 99;
                        }
                        g2d.drawRect(i * 100, j * 100, wk, hk);
                    }
                }
                g2d.setFont(f);
            }
        } catch (Exception e) {
        }
        g2d.dispose();
    }
}

