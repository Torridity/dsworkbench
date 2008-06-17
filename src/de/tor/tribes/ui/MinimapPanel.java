/*
 * MinimapPanel.java
 *
 * Created on 11. September 2007, 17:41
 */
package de.tor.tribes.ui;

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
    private MapFrame mParent;
    private MinimapZoomFrame mZoomFrame = null;
    private int iCurrentCursor = GlobalOptions.CURSOR_MOVE;

    /** Creates new form MinimapPanel */
    public MinimapPanel(MapFrame pParent) {
        initComponents();
        setSize(100, 100);
        mParent = pParent;

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
                mZoomFrame.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mZoomFrame.setVisible(false);
            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                mParent.updateLocationByMinimap(x, y);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mZoomFrame.setLocation(0, 0);
                int x = (int) Math.rint((double) 1000 / (double) getWidth() * (double) e.getX());
                int y = (int) Math.rint((double) 1000 / (double) getHeight() * (double) e.getY());
                mZoomFrame.updatePosition(x, y);
            }
        });

        addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                iCurrentCursor += e.getWheelRotation();
                if (iCurrentCursor < GlobalOptions.CURSOR_MOVE) {
                    iCurrentCursor = GlobalOptions.CURSOR_ZOOM;
                } else if (iCurrentCursor > GlobalOptions.CURSOR_ZOOM) {
                    iCurrentCursor = GlobalOptions.CURSOR_MOVE;
                }
                setCursor(GlobalOptions.getCursor(iCurrentCursor));
            }
        });

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

    protected void updateComplete(BufferedImage pBuffer) {
        if (mZoomFrame == null) {
            mZoomFrame = new MinimapZoomFrame(pBuffer);
            mZoomFrame.setSize(300, 300);
        }

        if (mBuffer == null) {
            mBuffer = pBuffer;
            mBuffer = mBuffer.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);
        } else if ((mBuffer.getWidth(null) != getWidth()) || (mBuffer.getHeight(null) != getHeight())) {
            mBuffer = pBuffer;
            mBuffer = mBuffer.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);
        }
        repaint();
    }

    public void redraw() {
        mPaintThread.update();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
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
                            mark = GlobalOptions.getMarkers().get(v.getTribe().getName());
                            if (mark == null) {
                                mark = GlobalOptions.getMarkers().get(v.getTribe().getAlly().getName());
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
        g2d.setColor(Color.BLACK);
        Composite c = g2d.getComposite();
        Composite a = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
        Font f = g2d.getFont();
        Font t = new Font("Serif", Font.BOLD, 30);
        g2d.setFont(t);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                g2d.setComposite(a);
                String conti = "K" + (i * 10 + j);
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
}

